package com.example.artistshottest.ui.data.service

import com.example.artistshottest.ui.domain.model.NoteDto
import retrofit2.http.GET

interface NotesApiService {
    @GET("posts")  // Changed from "notes" to "posts"
    suspend fun getNotes(): List<NoteDto>
}