package com.arrazyfathan.lerun.presentation.run

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.feature.run.R
import com.arrazyfathan.lerun.presentation.home.MainViewModel
import com.arrazyfathan.lerun.presentation.home.RunAction
import com.arrazyfathan.lerun.services.TrackingService
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunDarkAccentText
import com.arrazyfathan.lerun.ui.designsystem.LerunErrorColor
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import com.arrazyfathan.lerun.ui.designsystem.LerunGreenDarkAccent
import com.arrazyfathan.lerun.ui.designsystem.LerunMainDark
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import com.arrazyfathan.lerun.utils.Constants.ACTION_PAUSE_SERVICE
import com.arrazyfathan.lerun.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.arrazyfathan.lerun.utils.Constants.ACTION_STOP_SERVICE
import com.arrazyfathan.lerun.utils.Constants.LOCATION_SMOOTH_ANIMATION_DURATION_MS
import com.arrazyfathan.lerun.utils.Constants.MAP_CAMERA_ZOOM
import com.arrazyfathan.lerun.utils.Constants.POLYLINE_WIDTH
import com.arrazyfathan.lerun.utils.TrackingUtility
import com.arrazyfathan.lerun.utils.toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import timber.log.Timber
import java.util.Calendar
import kotlin.math.round
import com.arrazyfathan.lerun.core.ui.R as UiR

@Composable
fun TrackingRoute(
    onCloseTrackingToRun: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    TrackingScreen(
        onCloseTrackingToRun = onCloseTrackingToRun,
        viewModel = viewModel,
        modifier = modifier,
    )
}

@Composable
fun TrackingScreen(
    onCloseTrackingToRun: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isTracking by TrackingService.isTracking.observeAsState(false)
    val pathPoints by TrackingService.pathPoint.observeAsState(mutableListOf())
    val timeRunInMillis by TrackingService.timeRunInMillis.observeAsState(0L)

    var currentTimeMillis by remember { mutableLongStateOf(0L) }
    currentTimeMillis = timeRunInMillis

    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    var showFinishBottomSheet by rememberSaveable { mutableStateOf(false) }

    val mapView = rememberMapViewWithLifecycle()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    val renderedPolylines = remember { mutableListOf<Polyline>() }
    val headProgress = remember { Animatable(1f) }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            googleMap = map
            loadMapTheme(context, map, state.isDarkTheme)
            map.setOnMapLoadedCallback {
                isMapReady = true
            }
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = false
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null && pathPoints.isEmpty()) {
                            val myLocation = LatLng(location.latitude, location.longitude)
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(myLocation, MAP_CAMERA_ZOOM),
                            )
                        }
                    }
            }
        }
    }

    LaunchedEffect(googleMap, state.isDarkTheme) {
        val map = googleMap ?: return@LaunchedEffect
        loadMapTheme(context, map, state.isDarkTheme)
    }

    val polylineRevision = pathPoints.sumOf { it.size }
    val lastCoordinate = pathPoints.lastOrNull()?.lastOrNull()

    LaunchedEffect(googleMap, pathPoints.size, polylineRevision, lastCoordinate) {
        val map = googleMap ?: return@LaunchedEffect

        if (pathPoints.isEmpty()) {
            renderedPolylines.forEach { it.remove() }
            renderedPolylines.clear()
            return@LaunchedEffect
        }

        while (renderedPolylines.size > pathPoints.size) {
            renderedPolylines.removeLast().remove()
        }

        while (renderedPolylines.size < pathPoints.size) {
            val newPolyline = map.addPolyline(createSmoothRoundedPolylineOptions())
            renderedPolylines.add(newPolyline)
        }

        // Update all completed polylines prior to the active segment
        for (i in 0 until pathPoints.lastIndex) {
            renderedPolylines[i].points = smoothPolylinePoints(pathPoints[i])
        }

        val activePath = pathPoints.last()
        val activeGooglePolyline = renderedPolylines.last()

        if (activePath.isEmpty()) {
            activeGooglePolyline.points = emptyList()
            return@LaunchedEffect
        }

        val targetLatLng = activePath.last()
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(targetLatLng, MAP_CAMERA_ZOOM),
            LOCATION_SMOOTH_ANIMATION_DURATION_MS,
            null,
        )

        if (activePath.size >= 2) {
            val prevLatLng = activePath[activePath.lastIndex - 1]
            val prefixPoints = activePath.dropLast(1)
            headProgress.snapTo(0f)
            headProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = LOCATION_SMOOTH_ANIMATION_DURATION_MS,
                    easing = LinearOutSlowInEasing,
                ),
            ) {
                val fraction = value.toDouble()
                val interpolatedPoint = LatLng(
                    prevLatLng.latitude + (targetLatLng.latitude - prevLatLng.latitude) * fraction,
                    prevLatLng.longitude + (targetLatLng.longitude - prevLatLng.longitude) * fraction,
                )
                activeGooglePolyline.points = smoothPolylinePoints(prefixPoints + interpolatedPoint)
            }
        } else {
            activeGooglePolyline.points = activePath
        }
    }

    fun sendCommandToService(action: String) {
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context.startService(it)
        }
    }

    fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        onCloseTrackingToRun()
    }

    fun toggleRun() {
        if (!isMapReady && !isTracking) return
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    fun endRunAndSaveToDatabase(title: String) {
        val map = googleMap
        val hasPoints = pathPoints.any { it.isNotEmpty() }
        if (map != null && hasPoints && mapView.width > 0 && mapView.height > 0) {
            val boundsBuilder = LatLngBounds.Builder()
            for (polyline in pathPoints) {
                for (pos in polyline) {
                    boundsBuilder.include(pos)
                }
            }
            runCatching {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(),
                        mapView.width,
                        mapView.height,
                        (mapView.height * 0.05f).toInt(),
                    ),
                )
            }
        }

        val saveWithBitmap: (android.graphics.Bitmap?) -> Unit = { bitmap ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val hours = currentTimeMillis / 1000f / 60f / 60f
            val averageSpeed = if (hours > 0f) {
                round((distanceInMeters / 1000f) / hours * 10f) / 10f
            } else {
                0f
            }
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned =
                ((distanceInMeters / 1000f) * state.profileWeight.toFloat()).toInt()

            val run = Run(
                img = bitmap,
                title = title,
                timestamp = dateTimeStamp,
                avgSpeedInKMH = averageSpeed,
                distanceInMeters = distanceInMeters,
                timeInMillis = currentTimeMillis,
                caloriesBurned = caloriesBurned,
            )
            viewModel.onAction(RunAction.OnSaveRun(run))
            context.toast("Run saved successfully")
            stopRun()
        }

        if (map != null) {
            map.snapshot { bitmap ->
                saveWithBitmap(bitmap)
            }
        } else {
            saveWithBitmap(null)
        }
    }

    if (showCancelDialog) {
        CancelTrackingAlertDialog(
            onConfirmCancel = {
                showCancelDialog = false
                stopRun()
            },
            onDismiss = { showCancelDialog = false },
        )
    }

    if (showFinishBottomSheet) {
        var distanceInMeters = 0
        for (polyline in pathPoints) {
            distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
        }
        val hours = currentTimeMillis / 1000f / 60f / 60f
        val averageSpeed = if (hours > 0f) {
            round((distanceInMeters / 1000f) / hours * 10f) / 10f
        } else {
            0f
        }
        val caloriesBurned =
            ((distanceInMeters / 1000f) * state.profileWeight.toFloat()).toInt()

        FinishRunBottomSheet(
            formattedDuration = TrackingUtility.getFormattedStopWatchTime(currentTimeMillis, true),
            caloriesBurned = caloriesBurned.toString(),
            distanceInMeters = distanceInMeters.toString(),
            averageSpeed = averageSpeed.toString(),
            onSave = { title ->
                showFinishBottomSheet = false
                endRunAndSaveToDatabase(title)
            },
            onDismiss = { showFinishBottomSheet = false },
        )
    }

    TrackingScreenContent(
        formattedTimer = TrackingUtility.getFormattedStopWatchTime(currentTimeMillis, true),
        isTracking = isTracking,
        hasStarted = currentTimeMillis > 0L,
        isMapReady = isMapReady,
        onToggleRun = ::toggleRun,
        onFinishClick = { showFinishBottomSheet = true },
        onCancelClick = { showCancelDialog = true },
        mapContent = {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize(),
            )
        },
        modifier = modifier,
    )
}

@Composable
fun TrackingScreenContent(
    formattedTimer: String,
    isTracking: Boolean,
    hasStarted: Boolean,
    onToggleRun: () -> Unit,
    onFinishClick: () -> Unit,
    onCancelClick: () -> Unit,
    mapContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isMapReady: Boolean = true,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(LerunGreen)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (hasStarted) {
                    CancelRunPillButton(
                        onClick = onCancelClick,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(bottom = 16.dp),
                    )
                }
                Text(
                    text = "Duration",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = LerunGreenDarkAccent,
                )
                Text(
                    text = formattedTimer,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 40.sp,
                    color = LerunMainDark,
                )
                Spacer(modifier = Modifier.height(24.dp))
                TrackingControlButtons(
                    isTracking = isTracking,
                    showFinish = !isTracking && hasStarted,
                    isMapReady = isMapReady,
                    onToggleRun = onToggleRun,
                    onFinishClick = onFinishClick,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                mapContent()
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            mapContent()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    ),
            )

            if (hasStarted) {
                CancelRunPillButton(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 16.dp, end = 24.dp),
                )
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LerunGreen),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(24.dp)
                    .fillMaxWidth()
                    .height(200.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Duration",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = LerunGreenDarkAccent,
                        )
                        Text(
                            text = formattedTimer,
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 40.sp,
                            color = LerunMainDark,
                        )
                    }

                    TrackingControlButtons(
                        isTracking = isTracking,
                        showFinish = !isTracking && hasStarted,
                        isMapReady = isMapReady,
                        onToggleRun = onToggleRun,
                        onFinishClick = onFinishClick,
                        modifier = Modifier.padding(horizontal = 40.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CancelRunPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = LerunErrorColor,
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable(onClick = onClick),
    ) {
        Text(
            text = "Cancel Run",
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun TrackingControlButtons(
    isTracking: Boolean,
    showFinish: Boolean,
    isMapReady: Boolean,
    onToggleRun: () -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToggleEnabled = isMapReady || isTracking

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Card(
            onClick = onToggleRun,
            enabled = isToggleEnabled,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = LerunMainDark,
                disabledContainerColor = LerunMainDark.copy(alpha = 0.45f),
            ),
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .alpha(if (isToggleEnabled) 1f else 0.5f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isTracking) UiR.drawable.ic_stop else UiR.drawable.ic_play,
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = if (isTracking) {
                        stringResource(id = UiR.string.stop_label)
                    } else {
                        stringResource(id = UiR.string.start_label)
                    },
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        }

        if (showFinish) {
            Card(
                onClick = onFinishClick,
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LerunMainDark),
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_stop),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Finish",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

private fun createSmoothRoundedPolylineOptions(): PolylineOptions = PolylineOptions()
    .color(LerunGreen.toArgb())
    .width(POLYLINE_WIDTH)
    .startCap(RoundCap())
    .endCap(RoundCap())
    .jointType(JointType.ROUND)
    .geodesic(true)

/**
 * Applies Chaikin's corner-cutting subdivision algorithm to smooth sharp GPS vertex corners
 * while keeping the exact start and end coordinates fixed.
 */
internal fun smoothPolylinePoints(
    points: List<LatLng>,
    iterations: Int = 2,
): List<LatLng> {
    if (points.size < 3 || iterations <= 0) return points
    var current = points
    repeat(iterations) {
        if (current.size < 3) return current
        val next = ArrayList<LatLng>(current.size * 2)
        next.add(current.first())
        for (i in 0 until current.lastIndex) {
            val p0 = current[i]
            val p1 = current[i + 1]
            val q = LatLng(
                0.75 * p0.latitude + 0.25 * p1.latitude,
                0.75 * p0.longitude + 0.25 * p1.longitude,
            )
            val r = LatLng(
                0.25 * p0.latitude + 0.75 * p1.latitude,
                0.25 * p0.longitude + 0.75 * p1.longitude,
            )
            next.add(q)
            next.add(r)
        }
        next.add(current.last())
        current = next
    }
    return current
}

@Composable
fun CancelTrackingAlertDialog(
    onConfirmCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cancel the run?",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = "Are you sure to cancel the run and delete the data",
                fontFamily = Inter,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmCancel) {
                Text(
                    text = "Yes",
                    fontFamily = Inter,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "No",
                    fontFamily = Inter,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishRunBottomSheet(
    formattedDuration: String,
    caloriesBurned: String,
    distanceInMeters: String,
    averageSpeed: String,
    onSave: (title: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by rememberSaveable { mutableStateOf("") }
    var showValidationError by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = UiR.string.run_deatail_label),
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.clip(CircleShape),
                ) {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_close_white),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp)

            if (showValidationError) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LerunErrorColor)
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Title cannot be empty",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            BasicTextField(
                value = title,
                onValueChange = {
                    title = it
                    showValidationError = it.isBlank()
                },
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                textStyle = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                text = stringResource(id = UiR.string.hint_run_title),
                                fontFamily = Inter,
                                fontSize = 16.sp,
                                color = LerunDarkAccentText,
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 10.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Duration",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = LerunDarkAccentText,
                    )
                    Text(
                        text = formattedDuration,
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 38.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FinishSummarySmallCard(
                    label = "Calories",
                    value = caloriesBurned,
                    modifier = Modifier.weight(1f),
                )
                FinishSummarySmallCard(
                    label = "Distance",
                    value = distanceInMeters,
                    modifier = Modifier.weight(1f),
                )
                FinishSummarySmallCard(
                    label = "Speed",
                    value = averageSpeed,
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        showValidationError = true
                    } else {
                        onSave(title.trim())
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LerunGreen,
                    contentColor = LerunMainDark,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp)
                    .height(50.dp),
            ) {
                Text(
                    text = "Save",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = LerunMainDark,
                )
            }
        }
    }
}

@Composable
private fun FinishSummarySmallCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = LerunDarkAccentText,
            )
            Text(
                text = value,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
    }
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
    return mapView
}

private fun loadMapTheme(context: Context, googleMap: GoogleMap, isDarkTheme: Boolean) {
    val styleRes = if (isDarkTheme) {
        R.raw.style_json
    } else {
        R.raw.style_json_light
    }
    try {
        val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, styleRes))
        if (!success) {
            Timber.e("Style parsing failed.")
        }
    } catch (e: Exception) {
        Timber.e("Can't find style. Error: $e")
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackingScreenPreview() {
    LerunTheme(darkTheme = true) {
        TrackingScreenContent(
            formattedTimer = "00:12:34:56",
            isTracking = false,
            hasStarted = true,
            isMapReady = true,
            onToggleRun = {},
            onFinishClick = {},
            onCancelClick = {},
            mapContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray),
                )
            },
        )
    }
}
