package com.androiddevs.lerun.presentation.onboarding

sealed interface LoginAction {
    data object OnContinueClick: LoginAction
}