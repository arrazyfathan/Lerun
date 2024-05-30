package com.androiddevs.lerun.presentation.onboarding

import androidx.compose.foundation.text.input.TextFieldState

data class OnBoardingState(
    val username: TextFieldState = TextFieldState(),
    val weight: TextFieldState = TextFieldState()
)
