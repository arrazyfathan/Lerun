package com.arrazyfathan.lerun.presentation.onboarding

import com.arrazyfathan.lerun.ui.components.UiText


sealed interface LoginEvent {
    data class Error(val error: UiText): LoginEvent
    data object LoginSuccess: LoginEvent
}