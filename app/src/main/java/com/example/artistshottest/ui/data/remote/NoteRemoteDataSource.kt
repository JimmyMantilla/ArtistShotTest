package com.example.artistshottest.ui.data.remote

import android.util.Log
import com.example.artistshottest.ui.data.service.NotesApiService
import com.example.artistshottest.ui.domain.model.Note
import com.example.artistshottest.ui.domain.model.NoteDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class NotesRemoteDataSource @Inject constructor(
    private val apiService: NotesApiService
) {
    suspend fun getNotes(): Result<List<NoteDto>> {
        return try {
            Log.d("API_CALL", "Attempting to fetch notes from API")
            val response = apiService.getNotes()

            if (response.isNotEmpty()) {
                Log.d("API_SUCCESS", "Successfully fetched ${response.size} notes")
                Result.success(response)
            } else {
                Log.w("API_EMPTY", "API returned empty list")
                Result.success(emptyList()) // Return success with empty list
            }
        } catch (e: HttpException) {
            val errorMsg = "HTTP error ${e.code()}: ${e.response()?.errorBody()?.string() ?: e.message()}"
            Log.e("API_ERROR", errorMsg)
            Result.failure(IOException(errorMsg))
        } catch (e: IOException) {
            val errorMsg = "Network error: ${e.message ?: "Unknown network error"}"
            Log.e("NETWORK_ERROR", errorMsg)
            Result.failure(e)
        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.stackTraceToString()}"
            Log.e("UNKNOWN_ERROR", errorMsg)
            Result.failure(IOException("Unexpected error occurred"))
        }
    }
}