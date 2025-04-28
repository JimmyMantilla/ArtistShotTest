package com.example.artistshottest.ui.domain.usecases

import javax.inject.Inject

// domain/usecase/LoginUseCase.kt
class LoginUseCase @Inject constructor() {
    operator fun invoke(username: String, password: String): Boolean {
        return username == "artist1" && password == "artist123"
    }
}