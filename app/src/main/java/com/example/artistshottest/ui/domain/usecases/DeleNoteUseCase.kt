package com.example.artistshottest.ui.domain.usecases

import com.example.artistshottest.ui.data.repositoty.NoteRepository
import com.example.artistshottest.ui.domain.model.Note
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Boolean {
        return try {
            notesRepository.deleteNote(note)
            true
        } catch (e: Exception) {
            false
        }
    }
}