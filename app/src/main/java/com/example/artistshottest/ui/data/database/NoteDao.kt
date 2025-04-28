package com.example.artistshottest.ui.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

// data/local/dao/NoteDao.kt
@Dao
interface NoteDao {
    // 1. Get all notes (sorted by last updated)
    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAllNotes(): List<NoteEntity>

    // 2. Insert/Replace a note
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    // 3. Update a note
    @Update
    suspend fun updateNote(note: NoteEntity)

    // 4. Delete a note
    @Delete
    suspend fun deleteNote(note: NoteEntity)
}