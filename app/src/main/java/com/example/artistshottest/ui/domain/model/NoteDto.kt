package com.example.artistshottest.ui.domain.model

import com.google.gson.annotations.SerializedName

data class NoteDto(
    @SerializedName("userId") val userId: Long,
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String  // Changed from 'content' to 'body'
) {
    fun toDomain(): Note {
        return Note(
            id = id,
            title = title,
            body = body, // Now properly maps JSON 'body' to domain 'body'
            tags = emptyList(), // Default value
            updatedAt = System.currentTimeMillis()
        )
    }
}