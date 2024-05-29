package com.androiddevs.lerun.utils

object Constants {
    // name db
    const val RUNNING_DATABASE_NAME = "running_db"

    // request code permission
    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    // action to send a command to service
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    // action pending intent
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    // notification channel
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    // location request
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    // Color polyline
    const val POLYLINE_WIDTH = 12f
    const val MAP_CAMERA_ZOOM = 16f

    // timer
    const val TIMER_UPDATE_INTERVAL = 50L

    // shared preferences
    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
}
