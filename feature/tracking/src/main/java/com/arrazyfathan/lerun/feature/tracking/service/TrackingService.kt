package com.arrazyfathan.lerun.feature.tracking.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.arrazyfathan.lerun.utils.Constants.ACTION_PAUSE_SERVICE
import com.arrazyfathan.lerun.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.arrazyfathan.lerun.utils.Constants.ACTION_STOP_SERVICE
import com.arrazyfathan.lerun.utils.Constants.FASTEST_LOCATION_INTERVAL
import com.arrazyfathan.lerun.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.arrazyfathan.lerun.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.arrazyfathan.lerun.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.arrazyfathan.lerun.utils.Constants.NOTIFICATION_ID
import com.arrazyfathan.lerun.utils.Constants.TIMER_UPDATE_INTERVAL
import com.arrazyfathan.lerun.utils.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import com.arrazyfathan.lerun.core.ui.R as UiR

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    private var isFirstRun = true
    private var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSecond = MutableStateFlow(0L)

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private var timerJob: Job? = null
    private var notificationTimerJob: Job? = null

    companion object {
        private val _isTracking = MutableStateFlow(false)
        val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

        private val _pathPoint = MutableStateFlow<Polylines>(mutableListOf())
        val pathPoint: StateFlow<Polylines> = _pathPoint.asStateFlow()

        private val _timeRunInMillis = MutableStateFlow(0L)
        val timeRunInMillis: StateFlow<Long> = _timeRunInMillis.asStateFlow()
    }

    private fun postInitialValues() {
        _isTracking.value = false
        _pathPoint.value = mutableListOf()
        timeRunInSecond.value = 0L
        _timeRunInMillis.value = 0L
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking
            .onEach { tracking ->
                updateLocationTracking(tracking)
                updateNotificationTrackingState(tracking)
            }.launchIn(lifecycleScope)
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        notificationTimerJob?.cancel()
        notificationTimerJob = null
        postInitialValues()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming services...")
                        startTimer()
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service")
                    pauseService()
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped Service")
                    killService()
                }

                else -> {
                    Unit
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer() {
        addEmptyPolyline()
        _isTracking.value = true
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        timerJob?.cancel()
        timerJob =
            lifecycleScope.launch {
                while (_isTracking.value) {
                    lapTime = System.currentTimeMillis() - timeStarted
                    val totalTimeMillis = timeRun + lapTime
                    _timeRunInMillis.value = totalTimeMillis
                    if (totalTimeMillis >= lastSecondTimeStamp + 1000L) {
                        timeRunInSecond.update { it + 1L }
                        lastSecondTimeStamp += 1000L
                    }
                    delay(TIMER_UPDATE_INTERVAL.milliseconds)
                }
                timeRun += lapTime
            }
    }

    private fun pauseService() {
        _isTracking.value = false
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntentFlags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                FLAG_IMMUTABLE
            } else {
                FLAG_UPDATE_CURRENT
            }
        val pendingIntent =
            if (isTracking) {
                val pauseIntent =
                    Intent(this, TrackingService::class.java).apply {
                        action = ACTION_PAUSE_SERVICE
                    }
                PendingIntent.getService(this, 1, pauseIntent, pendingIntentFlags)
            } else {
                val resumeIntent =
                    Intent(this, TrackingService::class.java).apply {
                        action = ACTION_START_OR_RESUME_SERVICE
                    }
                PendingIntent.getService(this, 2, resumeIntent, pendingIntentFlags)
            }

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.clearActions()

        if (!serviceKilled) {
            currentNotificationBuilder =
                baseNotificationBuilder
                    .clearActions()
                    .addAction(
                        UiR.drawable.ic_pause_black_24dp,
                        notificationActionText,
                        pendingIntent,
                    )
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this) && hasForegroundLocationPermission()) {
                val request =
                    LocationRequest
                        .Builder(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            LOCATION_UPDATE_INTERVAL,
                        ).setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                        .setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL)
                        .setMinUpdateDistanceMeters(1f)
                        .setWaitForAccurateLocation(false)
                        .build()

                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper(),
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun hasForegroundLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    private val locationCallback =
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if (_isTracking.value) {
                    for (location in result.locations) {
                        addPathPoint(location)
                        Timber.d("New location : ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            _pathPoint.update { currentPolylines ->
                val updatedPolylines =
                    currentPolylines
                        .map { polyline -> polyline.toMutableList() }
                        .toMutableList()

                if (updatedPolylines.isEmpty()) {
                    updatedPolylines.add(mutableListOf())
                }

                val lastPolyline = updatedPolylines.last()
                if (lastPolyline.lastOrNull() != position) {
                    lastPolyline.add(position)
                }
                updatedPolylines
            }
        }
    }

    private fun addEmptyPolyline() {
        _pathPoint.update { currentPolylines ->
            val updatedPolylines =
                currentPolylines
                    .map { polyline -> polyline.toMutableList() }
                    .toMutableList()
            updatedPolylines.add(mutableListOf())
            updatedPolylines
        }
    }

    private fun startForegroundService() {
        startTimer()
        _isTracking.value = true

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        notificationTimerJob?.cancel()
        notificationTimerJob =
            timeRunInSecond
                .onEach { seconds ->
                    if (!serviceKilled) {
                        val notification =
                            currentNotificationBuilder
                                .setContentText(TrackingUtility.getFormattedStopWatchTime(seconds * 1000L))
                        notificationManager.notify(NOTIFICATION_ID, notification.build())
                    }
                }.launchIn(lifecycleScope)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW,
            )

        notificationManager.createNotificationChannel(channel)
    }
}
