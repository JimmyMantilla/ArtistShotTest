package com.example.artistshottest

import androidx.compose.runtime.Composable
import com.example.artistshottest.ui.viewmodel.NotesViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.artistshottest.ui.theme.ArtistShotTestTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.artistshottest.ui.screens.LoginScreen
import com.example.artistshottest.ui.screens.NotesScreen

// ArtistShotApp.kt
@Composable
fun ArtistShotApp() {
    // Get the ViewModel instance using Hilt
    val viewModel: NotesViewModel = hiltViewModel()

    // Collect the authentication state
    val authState by viewModel.authState.collectAsState()

    // Apply your app theme
    ArtistShotTestTheme {
        // Navigation based on auth state
        when (authState) {
            NotesViewModel.AuthState.Unauthenticated -> LoginScreen(viewModel)
            NotesViewModel.AuthState.Authenticated -> NotesScreen(viewModel)
            is NotesViewModel.AuthState.Error -> {
                // Show login screen with error
                LoginScreen(viewModel)
            }
        }
    }
}