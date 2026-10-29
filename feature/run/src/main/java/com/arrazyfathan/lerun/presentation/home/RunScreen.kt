package com.arrazyfathan.lerun.presentation.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunDarkAccentText
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import com.arrazyfathan.lerun.ui.designsystem.LerunGreenDarkAccent
import com.arrazyfathan.lerun.ui.designsystem.LerunMainDark
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import com.arrazyfathan.lerun.utils.BitmapConverter
import com.arrazyfathan.lerun.utils.LabelChartFormatter
import com.arrazyfathan.lerun.utils.TrackingUtility
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.round
import android.graphics.Color as AndroidColor
import com.arrazyfathan.lerun.core.ui.R as UiR

private const val REMOTE_CONFIG_KEY_BANNER = "title_banner"

@Composable
fun RunRoute(
    onOpenAllRuns: () -> Unit,
    onOpenRunDetail: (Run) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    RunScreen(
        onOpenAllRuns = onOpenAllRuns,
        onOpenRunDetail = onOpenRunDetail,
        viewModel = viewModel,
        modifier = modifier,
    )
}

@Composable
fun RunScreen(
    onOpenAllRuns: () -> Unit,
    onOpenRunDetail: (Run) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showPermissionDialog by remember { mutableStateOf(false) }

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    val locationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!fineGranted && !coarseGranted) {
            showPermissionDialog = true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !TrackingUtility.hasLocationPermissions(context)
        ) {
            backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onAction(RunAction.OnStarted)
        runCatching { Firebase.remoteConfig.getString(REMOTE_CONFIG_KEY_BANNER) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.let { viewModel.onAction(RunAction.OnBannerTitleUpdated(it)) }

        if (!TrackingUtility.hasLocationPermissions(context)) {
            locationPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAction(RunAction.OnStarted)
                runCatching { Firebase.remoteConfig.getString(REMOTE_CONFIG_KEY_BANNER) }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { viewModel.onAction(RunAction.OnBannerTitleUpdated(it)) }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = "Location Permission Required",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = "You need to accept location permissions to use this app.",
                    fontFamily = Inter,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        )
                        context.startActivity(intent)
                    },
                ) {
                    Text("Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        locationPermissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    },
                ) {
                    Text("Retry")
                }
            },
        )
    }

    RunScreenContent(
        greeting = getGreetingText(),
        profileName = state.profileName,
        profileImageBase64 = state.profileImageBase64,
        bannerTitle = state.bannerTitle,
        totalCalories = state.totalCaloriesBurned,
        totalAvgSpeed = state.totalAvgSpeedInKMH,
        totalDistanceMeters = state.totalDistanceInMeters,
        totalTimeMillis = state.totalTimeInMillis,
        chartRuns = state.runsSortedByDateAsc,
        latestRuns = state.runs.take(3),
        onOpenAllRuns = onOpenAllRuns,
        onOpenRunDetail = { run ->
            viewModel.onAction(RunAction.OnSelectRun(run))
            onOpenRunDetail(run)
        },
        modifier = modifier,
    )
}

@Composable
fun RunScreenContent(
    greeting: String,
    profileName: String,
    profileImageBase64: String?,
    bannerTitle: String,
    totalCalories: Int,
    totalAvgSpeed: Float,
    totalDistanceMeters: Int,
    totalTimeMillis: Long,
    chartRuns: List<Run>,
    latestRuns: List<Run>,
    onOpenAllRuns: () -> Unit,
    onOpenRunDetail: (Run) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val hazeState = rememberHazeState()
    var selectedChartRun by remember(chartRuns) { mutableStateOf<Run?>(null) }

    val formattedCalories = "$totalCalories kcal"
    val formattedAvgSpeed = "${round(totalAvgSpeed * 10f) / 10} km/h"
    val formattedDistance = "${round((totalDistanceMeters / 1000f) * 10f) / 10} km"
    val formattedTime = TrackingUtility.formatDuration(totalTimeMillis)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 88.dp, bottom = 120.dp),
        ) {
            PromoBanner(
                bannerTitle = bannerTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Text(
                text = "Statistics",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                DashboardStatCard(
                    iconRes = UiR.drawable.ic_calories_burned,
                    value = formattedCalories,
                    label = "Calories",
                    modifier = Modifier.weight(1f),
                )
                DashboardStatCard(
                    iconRes = UiR.drawable.ic_average_speed,
                    value = formattedAvgSpeed,
                    label = "Average Speed",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                DashboardStatCard(
                    iconRes = UiR.drawable.ic_route_distance,
                    value = formattedDistance,
                    label = "Total Distance",
                    modifier = Modifier.weight(1f),
                )
                DashboardStatCard(
                    iconRes = UiR.drawable.ic_timer,
                    value = formattedTime,
                    label = "Total Time",
                    modifier = Modifier.weight(1f),
                )
            }

            if (chartRuns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                selectedChartRun?.let { markerRun ->
                    ChartMarkerCard(
                        run = markerRun,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 8.dp),
                    )
                }

                SpeedOverTimeLineChart(
                    runs = chartRuns,
                    onRunSelected = { selectedChartRun = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 10.dp),
                )
            }

            if (latestRuns.isNotEmpty()) {
                Text(
                    text = "Recent Activities",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 8.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    latestRuns.forEach { run ->
                        LatestRunItem(
                            run = run,
                            onClick = { onOpenRunDetail(run) },
                        )
                    }
                }

                Button(
                    onClick = onOpenAllRuns,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LerunGreen,
                        contentColor = LerunMainDark,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(60.dp),
                ) {
                    Text(
                        text = "See More",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = LerunMainDark,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Build With \u2764\uFE0F",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = LerunDarkAccentText,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ar Razy Fathan Rabbani",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = LerunDarkAccentText,
                    textAlign = TextAlign.Center,
                )
            }
        }

        StickyProfileHeader(
            greeting = greeting,
            profileName = profileName,
            profileImageBase64 = profileImageBase64,
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun StickyProfileHeader(
    greeting: String,
    profileName: String,
    profileImageBase64: String?,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val decodedBitmap = remember(profileImageBase64) {
        if (!profileImageBase64.isNullOrBlank()) {
            BitmapConverter.converterStringToBitmap(profileImageBase64)
        } else {
            null
        }
    }

    Row(
        modifier = modifier
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(MaterialTheme.colorScheme.background),
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(40.dp),
        ) {
            if (decodedBitmap != null) {
                Image(
                    bitmap = decodedBitmap.asImageBitmap(),
                    contentDescription = stringResource(id = UiR.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Image(
                    painter = painterResource(id = UiR.drawable.profile_n),
                    contentDescription = stringResource(id = UiR.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = greeting,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = profileName,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun PromoBanner(
    bannerTitle: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(200.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LerunGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(150.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = bannerTitle,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    color = LerunGreenDarkAccent,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "Go Premium",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Image(
            painter = painterResource(id = UiR.drawable.men),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun DashboardStatCard(
    iconRes: Int,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier.height(170.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = LerunGreen,
                modifier = Modifier.size(30.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = value,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = LerunDarkAccentText,
            )
        }
    }
}

@Composable
private fun ChartMarkerCard(
    run: Run,
    modifier: Modifier = Modifier,
) {
    val dateFormatted = remember(run.timestamp) {
        val calendar = Calendar.getInstance().apply { timeInMillis = run.timestamp }
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dateFormatted,
                fontFamily = Inter,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Duration: ${TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)} | Avg Speed: ${run.avgSpeedInKMH} km/h",
                fontFamily = Inter,
                fontSize = 11.sp,
                color = LerunDarkAccentText,
            )
            Text(
                text = "Distance: ${run.distanceInMeters / 1000f} Km | Calories: ${run.caloriesBurned} kcal",
                fontFamily = Inter,
                fontSize = 11.sp,
                color = LerunDarkAccentText,
            )
        }
    }
}

@Composable
private fun SpeedOverTimeLineChart(
    runs: List<Run>,
    onRunSelected: (Run?) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                val accentColor = ContextCompat.getColor(context, UiR.color.dark_accent_text)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawLabels(false)
                    axisLineColor = accentColor
                    textColor = accentColor
                    setDrawGridLines(false)
                }
                axisLeft.apply {
                    axisLineColor = accentColor
                    textColor = accentColor
                    setDrawGridLines(false)
                }
                axisRight.apply {
                    axisLineColor = accentColor
                    textColor = accentColor
                    setDrawGridLines(false)
                }
                description.text = "Avg Speed Over Time"
                description.textColor = AndroidColor.WHITE
                legend.isEnabled = false
            }
        },
        update = { chart ->
            val context = chart.context
            val entries = runs.indices.map { i -> BarEntry(i.toFloat(), runs[i].avgSpeedInKMH) }
            val lineDataSet = LineDataSet(entries, "Average Speed Overtime").apply {
                valueTextColor = AndroidColor.WHITE
                valueTextSize = 8f
                lineWidth = 2f
                valueFormatter = LabelChartFormatter()
                color = ContextCompat.getColor(context, UiR.color.ijo)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setCircleColor(ContextCompat.getColor(context, UiR.color.ijo))
                setDrawCircleHole(false)
                setDrawFilled(true)
                fillDrawable = ContextCompat.getDrawable(context, UiR.drawable.gradient_green)
            }
            chart.setOnChartValueSelectedListener(
                object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        val index = e?.x?.toInt() ?: return
                        onRunSelected(runs.getOrNull(index))
                    }

                    override fun onNothingSelected() {
                        onRunSelected(null)
                    }
                },
            )
            chart.data = LineData(lineDataSet)
            chart.invalidate()
        },
    )
}

@Composable
fun LatestRunItem(
    run: Run,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedDate = remember(run.timestamp) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(60.dp),
            ) {
                if (run.img != null) {
                    Image(
                        bitmap = run.img!!.asImageBitmap(),
                        contentDescription = run.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LerunMainDark),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = run.title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = LerunDarkAccentText,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "${run.distanceInMeters / 1000f} Km",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis),
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun getGreetingText(): String {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    return when (hour) {
        in 0..11 -> stringResource(id = UiR.string.good_morning)
        in 12..15 -> stringResource(id = UiR.string.good_afternoon)
        in 16..20 -> stringResource(id = UiR.string.good_evening)
        else -> stringResource(id = UiR.string.good_night)
    }
}

@Preview(showBackground = true)
@Composable
private fun RunScreenPreview() {
    LerunTheme(darkTheme = true) {
        RunScreenContent(
            greeting = "Good Morning",
            profileName = "Ar Razy Fathan",
            profileImageBase64 = null,
            bannerTitle = "Try full access for free now",
            totalCalories = 450,
            totalAvgSpeed = 10.5f,
            totalDistanceMeters = 5200,
            totalTimeMillis = 1800000L,
            chartRuns = emptyList(),
            latestRuns = listOf(
                Run(
                    id = 1,
                    title = "Morning Run",
                    timestamp = System.currentTimeMillis(),
                    avgSpeedInKMH = 10.5f,
                    distanceInMeters = 5200,
                    timeInMillis = 1800000L,
                    caloriesBurned = 450,
                ),
            ),
            onOpenAllRuns = {},
            onOpenRunDetail = {},
        )
    }
}
