package com.arrazyfathan.lerun.presentation.statistic

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunDarkAccentText
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import com.arrazyfathan.lerun.utils.TrackingUtility
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlin.math.round
import android.graphics.Color as AndroidColor
import com.arrazyfathan.lerun.core.ui.R as UiR

@Composable
fun StatisticsRoute(
    viewModel: StatisticViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StatisticsScreen(state = state)
}

@Composable
fun StatisticsScreen(
    state: StatisticState,
    modifier: Modifier = Modifier,
) {
    val formattedTotalTime = TrackingUtility.getFormattedStopWatchTime(state.totalTimeRun)

    val formattedTotalDistance = run {
        val km = state.totalDistance / 1000f
        val rounded = round(km * 10f) / 10
        "${rounded}km"
    }

    val formattedAverageSpeed = run {
        val rounded = round(state.totalAverageSpeed * 10f) / 10
        "${rounded}km/h"
    }

    val formattedCalories = "${state.totalCaloriesBurned}kcal"

    StatisticsScreenContent(
        totalTime = formattedTotalTime,
        totalDistance = formattedTotalDistance,
        totalCalories = formattedCalories,
        averageSpeed = formattedAverageSpeed,
        runs = state.runsSortedByDate,
        modifier = modifier,
    )
}

@Composable
fun StatisticsScreenContent(
    totalTime: String,
    totalDistance: String,
    totalCalories: String,
    averageSpeed: String,
    runs: List<Run>,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            StatisticsMetricsGrid(
                totalTime = totalTime,
                totalDistance = totalDistance,
                totalCalories = totalCalories,
                averageSpeed = averageSpeed,
                valueFontSize = 30.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            StatisticsBarChart(
                runs = runs,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        ) {
            StatisticsMetricsGrid(
                totalTime = totalTime,
                totalDistance = totalDistance,
                totalCalories = totalCalories,
                averageSpeed = averageSpeed,
                valueFontSize = 36.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f),
            )
            StatisticsBarChart(
                runs = runs,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f),
            )
        }
    }
}

@Composable
private fun StatisticsMetricsGrid(
    totalTime: String,
    totalDistance: String,
    totalCalories: String,
    averageSpeed: String,
    valueFontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatisticMetricItem(
                value = totalTime,
                label = "Total Time",
                valueFontSize = valueFontSize,
                modifier = Modifier.weight(1f),
            )
            StatisticMetricItem(
                value = totalDistance,
                label = "Total Distance",
                valueFontSize = valueFontSize,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatisticMetricItem(
                value = totalCalories,
                label = "Total Calories Burned",
                valueFontSize = valueFontSize,
                modifier = Modifier.weight(1f),
            )
            StatisticMetricItem(
                value = averageSpeed,
                label = "Average Speed",
                valueFontSize = valueFontSize,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatisticMetricItem(
    value: String,
    label: String,
    valueFontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = valueFontSize,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = LerunDarkAccentText,
        )
    }
}

@Composable
private fun StatisticsBarChart(
    runs: List<Run>,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawLabels(false)
                    axisLineColor = AndroidColor.WHITE
                    textColor = AndroidColor.WHITE
                    setDrawGridLines(false)
                }
                axisLeft.apply {
                    axisLineColor = AndroidColor.WHITE
                    textColor = AndroidColor.WHITE
                    setDrawGridLines(false)
                }
                axisRight.apply {
                    axisLineColor = AndroidColor.WHITE
                    textColor = AndroidColor.WHITE
                    setDrawGridLines(false)
                }
                description.text = "Average Speed Over Time"
                description.textColor = AndroidColor.WHITE
                legend.isEnabled = false
            }
        },
        update = { barChart ->
            val entries = runs.indices.map { i ->
                BarEntry(i.toFloat(), runs[i].avgSpeedInKMH)
            }
            val barDataSet = BarDataSet(entries, "Average Speed Over Time").apply {
                valueTextColor = AndroidColor.WHITE
                color = ContextCompat.getColor(barChart.context, UiR.color.colorAccent)
            }
            barChart.data = BarData(barDataSet)
            barChart.invalidate()
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenPreview() {
    LerunTheme(darkTheme = true) {
        StatisticsScreenContent(
            totalTime = "01:24:10",
            totalDistance = "12.4km",
            totalCalories = "840kcal",
            averageSpeed = "9.8km/h",
            runs = emptyList(),
        )
    }
}
