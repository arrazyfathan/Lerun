package com.arrazyfathan.lerun.presentation.onboarding

sealed interface LoginAction {
    data object OnContinueClick: LoginAction
}