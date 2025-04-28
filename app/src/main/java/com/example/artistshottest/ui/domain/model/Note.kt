package com.example.artistshottest.ui.domain.model

data class Note(
    val id: Long = 0,
    val title: String,
    val body: String,
    val tags: List<String>,
    val updatedAt: Long = System.currentTimeMillis()
)