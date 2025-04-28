package com.example.artistshottest.ui.data.repositoty

import com.example.artistshottest.ui.domain.model.Note

interface NoteRepository {
    suspend fun getAllNotes(): List<Note>
    suspend fun createNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun getNotesFromApi(): List<Note>
}