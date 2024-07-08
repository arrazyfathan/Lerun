package com.arrazyfathan.lerun.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.arrazyfathan.lerun.R
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    private var isFirstRun = true
    private var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSecond = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoint = MutableLiveData<Polylines>()
        val timeRunInMillis = MutableLiveData<Long>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoint.postValue(mutableListOf())
        timeRunInSecond.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(
            this,
        ) {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        }
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        // stopForeground(true)
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
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time different between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted

                // post new lap time
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! > lastSecondTimeStamp + 1000L) {
                    timeRunInSecond.postValue(timeRunInSecond.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent =
            if (Build.VERSION.SDK_INT >= 31) {
                if (isTracking) {
                    val pauseIntent =
                        Intent(this, TrackingService::class.java).apply {
                            action = ACTION_PAUSE_SERVICE
                        }
                    PendingIntent.getService(this, 1, pauseIntent, FLAG_IMMUTABLE)
                } else {
                    val resumeIntent =
                        Intent(this, TrackingService::class.java).apply {
                            action = ACTION_START_OR_RESUME_SERVICE
                        }
                    PendingIntent.getService(this, 2, resumeIntent, FLAG_IMMUTABLE)
                }
            } else {
                if (isTracking) {
                    val pauseIntent =
                        Intent(this, TrackingService::class.java).apply {
                            action = ACTION_PAUSE_SERVICE
                        }
                    PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
                } else {
                    val resumeIntent =
                        Intent(this, TrackingService::class.java).apply {
                            action = ACTION_START_OR_RESUME_SERVICE
                        }
                    PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
                }
            }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            currentNotificationBuilder =
                baseNotificationBuilder
                    .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request =
                    LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        LOCATION_UPDATE_INTERVAL,
                    )
                        .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                        .build()

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
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

    private val locationCallback =
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if (isTracking.value!!) {
                    result.locations.let { locations ->
                        for (location in locations) {
                            addPathPoint(location)
                            Timber.d("New location : ${location.latitude}, ${location.longitude}")
                        }
                    }
                }
            }
        }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoint.value?.apply {
                last().add(position)
                pathPoint.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() =
        pathPoint.value?.apply {
            add(mutableListOf())
            pathPoint.postValue(this)
        } ?: pathPoint.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSecond.observe(
            this,
        ) {
            if (!serviceKilled) {
                val notification =
                    currentNotificationBuilder
                        .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }
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
