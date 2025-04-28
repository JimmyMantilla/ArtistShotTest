package com.example.artistshottest.ui.domain.usecases

import com.example.artistshottest.ui.data.repositoty.NoteRepository
import com.example.artistshottest.ui.domain.model.Note
import javax.inject.Inject

class EditNoteUseCase @Inject constructor(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(updatedNote: Note): Boolean {
        return try {
            notesRepository.updateNote(updatedNote)
            true
        } catch (e: Exception) {
            false
        }
    }
}