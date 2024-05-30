package com.androiddevs.lerun.presentation.onboarding

import com.androiddevs.lerun.ui.components.UiText


sealed interface LoginEvent {
    data class Error(val error: UiText): LoginEvent
    data object LoginSuccess: LoginEvent
}