package com.example.artistshottest.ui.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artistshottest.ui.data.repositoty.NoteRepository
import com.example.artistshottest.ui.domain.model.Note
import com.example.artistshottest.ui.domain.usecases.CreateNoteUseCase
import com.example.artistshottest.ui.domain.usecases.DeleteNoteUseCase
import com.example.artistshottest.ui.domain.usecases.EditNoteUseCase
import com.example.artistshottest.ui.domain.usecases.FilterNotesByDateUseCase
import com.example.artistshottest.ui.domain.usecases.GetNotesUseCase
import com.example.artistshottest.ui.domain.usecases.LoginUseCase
import com.example.artistshottest.ui.domain.usecases.SyncNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val syncNotesUseCase: SyncNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val editNoteUseCase: EditNoteUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val filterNotesByDateUseCase: FilterNotesByDateUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // Authentication state
    sealed class AuthState {
        object Unauthenticated : AuthState()
        object Authenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState
    var selectedDate by mutableStateOf<Long?>(null)
    private val _filteredNotes = mutableStateListOf<Note>()
    val filteredNotes: List<Note> get() = _filteredNotes
    private val _tagQuery = MutableStateFlow("")
    val tagQuery: StateFlow<String> = _tagQuery

    // Notes state
    sealed class NotesState {
        object Loading : NotesState()
        data class Success(val notes: List<Note>) : NotesState()
        data class Error(val message: String) : NotesState()
        object Empty : NotesState()
    }

    private val _notesState = MutableStateFlow<NotesState>(NotesState.Loading)
    val notesState: StateFlow<NotesState> = _notesState

    // Login function
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                if (loginUseCase(username, password)) {
                    _authState.value = AuthState.Authenticated
                    loadNotes() // Load notes after successful login
                } else {
                    _authState.value = AuthState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login failed: ${e.message}")
            }
        }
    }

    // Load all notes from database
    fun loadNotes() {
        viewModelScope.launch {
            _notesState.value = NotesState.Loading
            try {
                val localNotes = getNotesUseCase()

                if (localNotes.isNotEmpty()) {
                    val sortedNotes = localNotes.sortedByDescending { it.updatedAt }
                    _notesState.value = NotesState.Success(sortedNotes)

                    // Initialize filtered notes
                    _filteredNotes.clear()
                    _filteredNotes.addAll(sortedNotes)

                } else {
                    syncNotesUseCase()
                    val syncedNotes = getNotesUseCase()

                    if (syncedNotes.isNotEmpty()) {
                        val sortedSyncedNotes = syncedNotes.sortedByDescending { it.updatedAt }
                        _notesState.value = NotesState.Success(sortedSyncedNotes)

                        // Initialize filtered notes
                        _filteredNotes.clear()
                        _filteredNotes.addAll(sortedSyncedNotes)

                    } else {
                        _notesState.value = NotesState.Empty
                        _filteredNotes.clear()
                    }
                }
            } catch (e: Exception) {
                _notesState.value = NotesState.Error(
                    "Failed to load notes: ${e.message ?: "Unknown error"}"
                )
                _filteredNotes.clear()
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            if (deleteNoteUseCase(note)) {
                showToast("Note deleted successfully")
                loadNotes()
            } else {
                showToast("Failed to delete note")
            }
        }
    }

    fun createNote(title: String, body: String, tags: List<String>) {
        viewModelScope.launch {
            _notesState.value = NotesState.Loading
            try {
                val newNote = Note(
                    id = 0, // 0 will trigger auto-generation in Room
                    title = title,
                    body = body,
                    tags = tags,
                    updatedAt = System.currentTimeMillis()
                )

                when (val noteId = createNoteUseCase(newNote)) {
                    null -> {
                        _notesState.value = NotesState.Error("Failed to create note")
                        showToast("Failed to create note")
                    }
                    else -> {
                        showToast("Note created successfully (ID: $noteId)")
                        loadNotes() // Refresh the notes list
                    }
                }
            } catch (e: Exception) {
                _notesState.value = NotesState.Error(
                    "Failed to create note: ${e.message ?: "Unknown error"}"
                )
                showToast("Error creating note: ${e.message}")
            }
        }
    }

    fun editNote(noteId: Long, newTitle: String, newBody: String, newTags: List<String>) {
        viewModelScope.launch {
            val updatedNote = Note(
                id = noteId,
                title = newTitle,
                body = newBody,
                tags = newTags,
                updatedAt = System.currentTimeMillis()
            )

            if (editNoteUseCase(updatedNote)) {
                showToast("Note updated successfully")
                loadNotes()
            } else {
                showToast("Failed to update note")
            }
        }
    }

    fun filterByDate(date: Long?, tag: String) {
        selectedDate = date

        val notes = (_notesState.value as? NotesState.Success)?.notes ?: emptyList()

        val filtered = filterNotesByDateUseCase(notes, date, _tagQuery.value)
        Log.d("filtereddate", filtered.size.toString())
        _filteredNotes.clear()
        _filteredNotes.addAll(filtered)
    }



    fun onTagQueryChange(newQuery: String) {
        _tagQuery.value = newQuery
        filterByDate(selectedDate, newQuery)
    }

    private fun showToast(message: String) {
        viewModelScope.launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}