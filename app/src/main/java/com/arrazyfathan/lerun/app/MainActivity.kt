package com.arrazyfathan.lerun.app

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.lerun.R
import com.arrazyfathan.lerun.navigation.MainApp
import com.arrazyfathan.lerun.ui.AppUiAction
import com.arrazyfathan.lerun.ui.AppUiEvent
import com.arrazyfathan.lerun.ui.AppUiViewModel
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import com.arrazyfathan.lerun.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import timber.log.Timber
import com.arrazyfathan.lerun.core.ui.R as UiR

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val appUiViewModel: AppUiViewModel by viewModels()

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ),
            navigationBarStyle =
                SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ),
        )
        window.disableNavigationBarContrastEnforcement(Build.VERSION.SDK_INT)

        setupRemoteConfig()
        setTheme(UiR.style.Base_Theme_Lerun)
        requestPermissions()

        if (savedInstanceState == null) {
            consumeTrackingIntent(intent)
        }

        setContent {
            val appUiState by appUiViewModel.state.collectAsStateWithLifecycle()
            val trackingIntentEvents =
                remember(appUiViewModel) {
                    appUiViewModel.events
                        .filterIsInstance<AppUiEvent.NavigateToTracking>()
                        .map { }
                }

            LerunTheme(darkTheme = appUiState.isDarkTheme) {
                MainApp(
                    isProfileCompleted = appUiState.isProfileCompleted,
                    trackingIntentEvents = trackingIntentEvents,
                )
            }
        }
    }

    private fun consumeTrackingIntent(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            intent.action = null
            appUiViewModel.onAction(AppUiAction.OnTrackingIntentReceived)
        }
    }

    private fun setupRemoteConfig() {
        remoteConfig = Firebase.remoteConfig
        val configSetting =
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 60
            }

        remoteConfig.setConfigSettingsAsync(configSetting)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig
            .fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Timber.d("Config params updated: $updated")
                } else {
                    Timber.d("Failed fetch config")
                }
            }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeTrackingIntent(intent)
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

internal fun Window.disableNavigationBarContrastEnforcement(sdkInt: Int) {
    if (sdkInt >= Build.VERSION_CODES.Q) {
        isNavigationBarContrastEnforced = false
    }
}
