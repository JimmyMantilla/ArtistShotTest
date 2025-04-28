package com.example.artistshottest.ui.data.repositoty

import android.util.Log
import com.example.artistshottest.ui.data.database.AppDatabase
import com.example.artistshottest.ui.data.database.NoteDao
import com.example.artistshottest.ui.data.database.NoteEntity
import com.example.artistshottest.ui.data.database.TagEntity
import com.example.artistshottest.ui.data.remote.NotesRemoteDataSource
import com.example.artistshottest.ui.data.service.NotesApiService
import com.example.artistshottest.ui.domain.model.Note
import com.example.artistshottest.ui.domain.model.NoteDto
import javax.inject.Inject

// data/repository/NoteRepositoryImpl.kt
class NoteRepositoryImpl @Inject constructor(
    private val localDataSource: NoteDao, // Room or other local DB
    private val remoteDataSource: NotesRemoteDataSource // API service
) : NoteRepository {

    override suspend fun getAllNotes(): List<Note> {
        val noteEntities = localDataSource.getAllNotes() // Esto devuelve List<NoteEntity>
        return noteEntities.map { it.toDomain() } // Convierte cada entidad a dominio
    }

    override suspend fun createNote(note: Note): Long {
        return localDataSource.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        localDataSource.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) {
        localDataSource.deleteNote(note.toEntity())
    }

    override suspend fun getNotesFromApi(): List<Note> {
        val result = remoteDataSource.getNotes()

        return result.fold(
            onSuccess = { dtoList ->
                dtoList.map { dto -> dto.toDomain() }
            },
            onFailure = { exception ->
                Log.d("NoteRepositoryImpl", "Failed to fetch notes from API", exception)
                emptyList()
            }
        )
    }

    // Extension functions for mapping
    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            title = title,
            body = body,
            tags = tags,
            updatedAt = updatedAt
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            title = title,
            body = body,
            tags = tags,
            updatedAt = updatedAt
        )
    }

    fun NoteDto.toDomain(): Note {
        return Note(
            id = id,
            title = title,
            body = body,
            tags = emptyList(), // Or map from DTO if available
            updatedAt = System.currentTimeMillis()
        )
    }
}