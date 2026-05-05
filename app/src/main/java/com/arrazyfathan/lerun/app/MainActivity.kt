package com.arrazyfathan.lerun.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import com.arrazyfathan.lerun.R
import com.arrazyfathan.lerun.core.ui.R as UiR
import com.arrazyfathan.lerun.databinding.NewActivityMainBinding
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.navigation.AppNavigator
import com.arrazyfathan.lerun.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AppNavigator {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var binding: NewActivityMainBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val navController: NavController
        get() = (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = NewActivityMainBinding.inflate(layoutInflater)
        setupRemoteConfig()
        setTheme(UiR.style.Base_Theme_Lerun)
        setContentView(binding.root)
        generateNewToken()
        requestPermissions()

        navigateTrackingFragmentIfNeeded(intent)
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemReselectedListener {}
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false
        binding.bottomNavigationView.itemIconTintList = null

        binding.fabinside.setOnClickListener {
            openTracking()
        }

        navController
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment -> {
                        binding.bottomNavigationView.visibility = View.VISIBLE
                        binding.containerNavMenu.visibility = View.VISIBLE
                        binding.fabinside.visibility = View.VISIBLE
                        binding.overlay.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.bottomNavigationView.visibility = View.GONE
                        binding.containerNavMenu.visibility = View.GONE
                        binding.fabinside.visibility = View.GONE
                        binding.overlay.visibility = View.GONE
                    }
                }
            }
    }

    private fun generateNewToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.w("Fetching FCM refistration token failed ${task.exception}")
                return@addOnCompleteListener
            }
            val token = task.result
            Timber.d(token)
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

        remoteConfig.fetchAndActivate()
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
        navigateTrackingFragmentIfNeeded(intent)
    }

    private fun navigateTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            openTracking()
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun openRun(clearOnboardingFromBackStack: Boolean) {
        navController.navigate(
            R.id.runFragment,
            null,
            navOptions {
                launchSingleTop = true
                if (clearOnboardingFromBackStack) {
                    popUpTo(R.id.setupFragment) {
                        inclusive = true
                    }
                }
            },
        )
    }

    override fun openTracking() {
        navController.navigate(
            R.id.trackingFragment,
            null,
            navOptions {
                launchSingleTop = true
            },
        )
    }

    override fun openAllRuns() {
        navController.navigate(R.id.allRunFragment)
    }

    override fun openRunDetail(run: Run) {
        navController.navigate(
            R.id.detailRunFragment,
            bundleOf("latestrun" to run),
        )
    }

    override fun closeTrackingToRun() {
        navController.navigate(
            R.id.runFragment,
            null,
            navOptions {
                launchSingleTop = true
                popUpTo(R.id.runFragment) {
                    inclusive = true
                }
            },
        )
    }
}
