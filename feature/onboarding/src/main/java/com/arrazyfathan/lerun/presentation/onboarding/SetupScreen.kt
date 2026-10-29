package com.arrazyfathan.lerun.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import com.arrazyfathan.lerun.ui.designsystem.LerunMainDark
import com.arrazyfathan.lerun.ui.designsystem.LerunSecondaryDark
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import com.arrazyfathan.lerun.core.ui.R as UiR

@Composable
fun SetupRoute(
    onNavigateToRun: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(),
) {
    SetupScreen(
        onNavigateToRun = onNavigateToRun,
        viewModel = viewModel,
        modifier = modifier,
    )
}

@Composable
fun SetupScreen(
    onNavigateToRun: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.onAction(OnboardingAction.OnStarted)
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                OnboardingEvent.NavigateToRun -> {
                    onNavigateToRun()
                }
            }
        }
    }

    SetupScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun SetupScreenContent(
    state: OnboardingState,
    snackbarHostState: SnackbarHostState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize(),
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(id = UiR.drawable.setup_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Image(
                painter = painterResource(id = UiR.drawable.logo_with_text),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 50.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.TopCenter),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    text = "Your name :",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 10.dp),
                )

                SetupInputField(
                    value = state.name,
                    onValueChange = { onAction(OnboardingAction.OnNameChanged(it)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                )

                Text(
                    text = "Weight (Kg) :",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 10.dp),
                )

                SetupInputField(
                    value = state.weight,
                    onValueChange = { onAction(OnboardingAction.OnWeightChanged(it)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                )

                Button(
                    onClick = { onAction(OnboardingAction.OnContinueClick) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LerunGreen,
                        contentColor = LerunMainDark,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
                    contentPadding = PaddingValues(horizontal = 30.dp),
                    modifier = Modifier
                        .height(60.dp)
                        .align(Alignment.End),
                ) {
                    Text(
                        text = "Continue ->",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = LerunMainDark,
                    )
                }
            }
        }
    }
}

@Composable
fun SetupScreenContent(
    snackbarHostState: SnackbarHostState,
    onContinueClick: (name: String, weight: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }

    SetupScreenContent(
        state = OnboardingState(name = name, weight = weight),
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when (action) {
                is OnboardingAction.OnNameChanged -> name = action.name
                is OnboardingAction.OnWeightChanged -> weight = action.weight
                OnboardingAction.OnContinueClick -> onContinueClick(name, weight)
                OnboardingAction.OnStarted -> Unit
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun SetupInputField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(Color.White),
        textStyle = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color.White,
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(LerunSecondaryDark)
            .padding(horizontal = 24.dp, vertical = 14.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    LerunTheme(darkTheme = true) {
        SetupScreenContent(
            state = OnboardingState(),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}
