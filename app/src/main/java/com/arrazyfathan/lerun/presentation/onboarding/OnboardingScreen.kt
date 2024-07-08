package com.arrazyfathan.lerun.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arrazyfathan.lerun.R
import com.arrazyfathan.lerun.ui.components.LerunActionButton
import com.arrazyfathan.lerun.ui.components.LerunTextField

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onUserFilledProfile: (Boolean) -> Unit,
    onSuccess: () -> Unit
) {

    LaunchedEffect(Unit) {
        onUserFilledProfile(viewModel.isUserFilledUserProfile())
    }

    val usernameState = rememberTextFieldState()
    val weightState = rememberTextFieldState()

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.setup_bg),
            contentDescription = "Bacground",
            contentScale = ContentScale.Crop
        )

        Image(
            modifier = Modifier
                .padding(top = 60.dp)
                .height(60.dp)
                .align(Alignment.TopCenter),
            painter = painterResource(id = R.drawable.logo_with_text),
            contentDescription = "Logo",
            alignment = Alignment.TopCenter
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
        ) {
            LerunTextField(
                state = usernameState,
                startIcon = null,
                endIcon = null,
                hint = "Input your name",
                title = "Your Name : "
            )
            Spacer(modifier = Modifier.height(16.dp))
            LerunTextField(
                state = weightState,
                startIcon = null,
                endIcon = null,
                hint = "Input your white",
                title = "Weight (Kg) :",
                keyboardType = KeyboardType.Decimal
            )
            Spacer(modifier = Modifier.height(34.dp))
            LerunActionButton(
                modifier = Modifier.padding(bottom = 16.dp),
                text = "Continue",
                isLoading = false,
                enabled = usernameState.text.isNotBlank() && weightState.text.isNotBlank(),
                onClick = {
                    viewModel.setUserProfile(
                        name = usernameState.text.toString(),
                        weight = weightState.text.toString()
                    )
                },
            )
        }
    }
}