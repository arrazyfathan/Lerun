package com.arrazyfathan.lerun.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

object TrackingUtility {
    fun hasLocationPermissions(context: Context): Boolean {
        val hasFine =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return hasFine && hasCoarse
        }

        val hasBackground =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        return hasFine && hasCoarse && hasBackground
    }

    fun getFormattedStopWatchTime(
        ms: Long,
        includeMillis: Boolean = false,
    ): String {
        var millisecond = ms
        val hours = TimeUnit.MILLISECONDS.toHours(millisecond)
        millisecond -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisecond)
        millisecond -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisecond)
        if (!includeMillis) {
            return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
        }
        millisecond -= TimeUnit.SECONDS.toMillis(seconds)
        millisecond /= 10
        return "${if (hours < 10) "0" else ""}$hours:" +
            "${if (minutes < 10) "0" else ""}$minutes:" +
            "${if (seconds < 10) "0" else ""}$seconds:" +
            "${if (millisecond < 10) "0" else ""}$millisecond"
    }

    fun formatDuration(ms: Long): String {
        var millisecond = ms
        val hours = TimeUnit.MILLISECONDS.toHours(millisecond)
        millisecond -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisecond)
        millisecond -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisecond)
        return if (hours < 1) {
            "${if (minutes < 10) "" else ""}${minutes}m " + "${if (seconds < 10) "0" else ""}${seconds}s"
        } else {
            "${if (hours < 10) "" else ""}${hours}h" + "${if (minutes < 10) "0" else ""}${minutes}m"
        }
    }
}
