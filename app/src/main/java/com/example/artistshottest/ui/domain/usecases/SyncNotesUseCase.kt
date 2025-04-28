package com.example.artistshottest.ui.domain.usecases

import android.util.Log
import com.example.artistshottest.ui.data.repositoty.NoteRepository
import com.example.artistshottest.ui.domain.model.Note
import javax.inject.Inject

class SyncNotesUseCase @Inject constructor(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(): List<Note> {
        return try {
            val remoteNotes = notesRepository.getNotesFromApi()
            if (remoteNotes.isNotEmpty()) {
                // Save notes to local database
                remoteNotes.forEach { note ->
                    notesRepository.createNote(note)
                }
                Log.d("SyncNotes", "Successfully synced ${remoteNotes.size} notes")
                remoteNotes // Return the fetched notes
            } else {
                Log.d("SyncNotes", "Received empty notes list from API")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("SyncNotes", "Failed to sync notes", e)
            emptyList() // Return empty list on error
        }
    }
}