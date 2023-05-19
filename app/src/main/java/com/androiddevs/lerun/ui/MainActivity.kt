package com.androiddevs.lerun.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.lerun.R
import com.androiddevs.lerun.databinding.NewActivityMainBinding
import com.androiddevs.lerun.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    private lateinit var binding: NewActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewActivityMainBinding.inflate(layoutInflater)
        setupRemoteConfig()
        setTheme(R.style.AppTheme)
        setContentView(binding.root)
        navigateTrackingFragmentIfNeeded(intent)
        generateNewToken()

        /*setSupportActionBar(toolbar)*/
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnNavigationItemReselectedListener { /* No operation */ }
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false
        binding.bottomNavigationView.itemIconTintList = null

        binding.fabinside.setOnClickListener {
            findNavController(R.id.navHostFragment).navigate(R.id.from_main_activity_to_tracking)
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
            // Log and toast
            Timber.d(token)
        }
    }

    private fun setupRemoteConfig() {
        remoteConfig = Firebase.remoteConfig
        val configSetting = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 * 12
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateTrackingFragmentIfNeeded(intent)
    }

    private fun navigateTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate(R.id.action_global_tackingFragment)
        }
    }
}
