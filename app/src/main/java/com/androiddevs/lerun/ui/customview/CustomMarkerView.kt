package com.androiddevs.lerun.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.androiddevs.lerun.R
import com.androiddevs.lerun.data.local.db.Run
import com.androiddevs.lerun.utils.TrackingUtility
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("ViewConstructor")
class CustomMarkerView(
    private val runs: List<Run>,
    context: Context,
    layoutId: Int
) : MarkerView(context, layoutId) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }

        val tvDate = findViewById<TextView>(R.id.tvDate)
        val distanceDetail = findViewById<TextView>(R.id.tvDistance)
        val durationDetail = findViewById<TextView>(R.id.tvDuration)
        val caloriesDetail = findViewById<TextView>(R.id.tvCaloriesBurned)
        val averageSpeedDetail = findViewById<TextView>(R.id.tvAvgSpeed)

        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]

        val calendar =
            Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(calendar.time)

        val distanceInKm = "${run.distanceInMeters / 1000f} Km"
        distanceDetail.text = distanceInKm

        durationDetail.text =
            TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val calories = "${run.caloriesBurned} kcal"
        caloriesDetail.text = calories

        val speed = "${run.avgSpeedInKMH} km/h"
        averageSpeedDetail.text = speed
    }
}