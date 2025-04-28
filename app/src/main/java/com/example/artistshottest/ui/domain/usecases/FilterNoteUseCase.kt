package com.example.artistshottest.ui.domain.usecases

import android.util.Log
import com.example.artistshottest.ui.domain.model.Note
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date


class FilterNotesByDateUseCase {

    operator fun invoke(notes: List<Note>, selectedDate: Long?, selectedTag: String?): List<Note> {
        if (selectedDate == null && selectedTag == null) return notes
        Log.d("tagsfilter","entro")
        return try {
            notes.filter { note ->

                val matchesDate = selectedDate?.let {
                    millisToLocalDate(note.updatedAt) == millisToLocalDate(it)
                } ?: true

                val matchesTag = selectedTag?.let { tag ->
                    note.tags.any { it.contains(tag, ignoreCase = true) }
                } ?: true

                matchesDate && matchesTag
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun millisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}