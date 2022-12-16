package com.androiddevs.lerun.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.lerun.R
import com.androiddevs.lerun.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.bottomNavigationView
import kotlinx.android.synthetic.main.activity_main.navHostFragment
import kotlinx.android.synthetic.main.new_activity_main.*
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRemoteConfig()
        setTheme(R.style.AppTheme)
        setContentView(R.layout.new_activity_main)
        navigateTrackingFragmentIfNeeded(intent)

        /*setSupportActionBar(toolbar)*/
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener { /* No operation */ }
        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(1).isEnabled = false
        bottomNavigationView.itemIconTintList = null

        fabinside.setOnClickListener {
            findNavController(R.id.navHostFragment).navigate(R.id.from_main_activity_to_tracking)
        }

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment -> {
                        bottomNavigationView.visibility = View.VISIBLE
                        container_nav_menu.visibility = View.VISIBLE
                        fabinside.visibility = View.VISIBLE
                        overlay.visibility = View.VISIBLE
                    }
                    else -> {
                        bottomNavigationView.visibility = View.GONE
                        container_nav_menu.visibility = View.GONE
                        fabinside.visibility = View.GONE
                        overlay.visibility = View.GONE
                    }
                }
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
                    Timber.d(TAG, "Config params updated: $updated")
                } else {
                    Timber.d(TAG, "Failed fetch config")
                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateTrackingFragmentIfNeeded(intent)
    }

    private fun navigateTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_tackingFragment)
        }
    }
}
