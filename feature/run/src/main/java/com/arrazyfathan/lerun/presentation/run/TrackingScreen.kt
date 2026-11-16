package com.arrazyfathan.lerun.presentation.run

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.arrazyfathan.lerun.feature.run.R
import com.arrazyfathan.lerun.services.TrackingService
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunDarkAccentText
import com.arrazyfathan.lerun.ui.designsystem.LerunErrorColor
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import com.arrazyfathan.lerun.ui.designsystem.LerunGreenDarkAccent
import com.arrazyfathan.lerun.ui.designsystem.LerunMainDark
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import com.arrazyfathan.lerun.utils.Constants.FASTEST_LOCATION_INTERVAL
import com.arrazyfathan.lerun.utils.Constants.LOCATION_SMOOTH_ANIMATION_DURATION_MS
import com.arrazyfathan.lerun.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.arrazyfathan.lerun.utils.Constants.MAP_CAMERA_ZOOM
import com.arrazyfathan.lerun.utils.Constants.POLYLINE_WIDTH
import com.arrazyfathan.lerun.utils.toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.cos
import com.arrazyfathan.lerun.core.ui.R as UiR

private const val USER_LOCATION_BOUNDS_RADIUS_METERS = 120.0
private const val CAMERA_ANIMATE_TO_MY_LOCATION_DURATION_MS = 600

@Composable
fun TrackingRoute(
    onCloseTrackingToRun: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackingScreen(
        onCloseTrackingToRun = onCloseTrackingToRun,
        modifier = modifier,
    )
}

@SuppressLint("MissingPermission")
@Composable
fun TrackingScreen(
    onCloseTrackingToRun: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val viewModel: TrackingViewModel = viewModel()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val state by viewModel.state.collectAsStateWithLifecycle()

    val mapView = rememberMapViewWithLifecycle()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var latestAndroidLocation by remember { mutableStateOf<Location?>(null) }
    var mapLocationListener by remember {
        mutableStateOf<LocationSource.OnLocationChangedListener?>(null)
    }

    val renderedPolylines = remember { mutableListOf<Polyline>() }
    val headProgress = remember { Animatable(1f) }
    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun dispatchUserLocationToBlueDot(location: Location) {
        latestAndroidLocation = location
        val latLng = LatLng(location.latitude, location.longitude)
        val listener = mapLocationListener
        if (listener != null) {
            listener.onLocationChanged(location)
        }
        viewModel.onAction(
            TrackingAction.OnLocationUpdated(
                location = latLng,
                isBlueDotRendered = listener != null,
            ),
        )
    }

    fun updatePinViewportVisibility(map: GoogleMap?, target: LatLng?) {
        if (map == null || target == null) return
        val visible = isLocationVisibleOnMap(map, target)
        viewModel.onAction(TrackingAction.OnPinViewportVisibilityChanged(visible))
    }

    fun sendCommandToService(action: String) {
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context.startService(it)
        }
    }

    fun captureSnapshotAndSaveRun() {
        val map = googleMap
        val hasPoints = state.pathPoints.any { it.isNotEmpty() }
        if (map != null && hasPoints && mapView.width > 0 && mapView.height > 0) {
            val boundsBuilder = LatLngBounds.Builder()
            for (polyline in state.pathPoints) {
                for (pos in polyline) {
                    boundsBuilder.include(pos)
                }
            }
            runCatching {
                map.setPadding(0, 0, 0, 0)
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

        if (map != null) {
            map.snapshot { bitmap ->
                viewModel.onAction(TrackingAction.OnSaveRunWithSnapshot(bitmap))
            }
        } else {
            viewModel.onAction(TrackingAction.OnSaveRunWithSnapshot(null))
        }
    }

    fun animateCameraToUserLocation(target: LatLng?) {
        val map = googleMap ?: return
        if (target != null) {
            focusCameraOnCenteredBounds(
                map = map,
                mapView = mapView,
                target = target,
                animate = true,
                durationMs = CAMERA_ANIMATE_TO_MY_LOCATION_DURATION_MS,
                onCameraFocused = {
                    viewModel.onAction(TrackingAction.OnRecenterFinished)
                },
            )
        } else if (hasLocationPermission(context)) {
            val tokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.token,
            ).addOnSuccessListener { location ->
                if (location != null) {
                    dispatchUserLocationToBlueDot(location)
                    val myLatLng = LatLng(location.latitude, location.longitude)
                    focusCameraOnCenteredBounds(
                        map = map,
                        mapView = mapView,
                        target = myLatLng,
                        animate = true,
                        durationMs = CAMERA_ANIMATE_TO_MY_LOCATION_DURATION_MS,
                        onCameraFocused = {
                            viewModel.onAction(TrackingAction.OnRecenterFinished)
                        },
                    )
                } else {
                    viewModel.onAction(TrackingAction.OnRecenterFinished)
                }
            }.addOnFailureListener {
                viewModel.onAction(TrackingAction.OnRecenterFinished)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is TrackingEvent.SendServiceCommand -> sendCommandToService(event.action)
                is TrackingEvent.AnimateToMyLocation -> animateCameraToUserLocation(event.target)
                TrackingEvent.CaptureMapSnapshotAndSave -> captureSnapshotAndSaveRun()
                is TrackingEvent.ShowMessage -> context.toast(event.message)
                TrackingEvent.CloseTrackingScreen -> onCloseTrackingToRun()
            }
        }
    }

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val mapTopPaddingPx = remember(isLandscape, statusBarTopPadding, density) {
        if (isLandscape) {
            0
        } else {
            with(density) { (statusBarTopPadding + 16.dp).roundToPx() }
        }
    }
    val mapBottomPaddingPx = remember(isLandscape, navBarBottomPadding, density) {
        if (isLandscape) {
            0
        } else {
            // Reserves space for the bottom control card (200.dp + 24.dp*2 padding + nav bar)
            // so the current user location pin is centered in the unobstructed map area.
            with(density) { (248.dp + navBarBottomPadding).roundToPx() }
        }
    }

    // Fallback timeout so the map becomes interactive even if tiles fail to load offline
    LaunchedEffect(googleMap, state.isMapReady) {
        if (googleMap != null && !state.isMapReady) {
            delay(1500L)
            viewModel.onAction(TrackingAction.OnMapLoaded)
        }
    }

    // Observe real-time user location while on TrackingScreen so the blue dot and camera stay
    // synchronized with the user's current position both before and during a run.
    DisposableEffect(context, fusedLocationClient) {
        var cancellationTokenSource: CancellationTokenSource? = null
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                dispatchUserLocationToBlueDot(loc)
            }
        }

        if (hasLocationPermission(context)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && latestAndroidLocation == null) {
                    dispatchUserLocationToBlueDot(loc)
                }
            }

            cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token,
            ).addOnSuccessListener { loc ->
                if (loc != null) {
                    dispatchUserLocationToBlueDot(loc)
                }
            }

            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL,
            )
                .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                .setMinUpdateDistanceMeters(1f)
                .setWaitForAccurateLocation(false)
                .build()

            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper(),
            )
        }

        onDispose {
            cancellationTokenSource?.cancel()
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            googleMap = map
            loadMapTheme(context, map, state.isDarkTheme)
            map.uiSettings.apply {
                isMyLocationButtonEnabled = false
                isCompassEnabled = false
                isMapToolbarEnabled = false
                isRotateGesturesEnabled = true
                isTiltGesturesEnabled = false
            }
            map.setPadding(0, mapTopPaddingPx, 0, mapBottomPaddingPx)

            map.setLocationSource(
                object : LocationSource {
                    override fun activate(listener: LocationSource.OnLocationChangedListener) {
                        mapLocationListener = listener
                        latestAndroidLocation?.let { loc ->
                            listener.onLocationChanged(loc)
                            viewModel.onAction(TrackingAction.OnBlueDotRendered)
                        }
                    }

                    override fun deactivate() {
                        mapLocationListener = null
                    }
                },
            )

            map.setOnCameraMoveStartedListener { reason ->
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    viewModel.onAction(TrackingAction.OnUserGesturePan)
                }
            }

            map.setOnCameraIdleListener {
                updatePinViewportVisibility(map, state.currentLocation)
            }

            map.setOnMapLoadedCallback {
                viewModel.onAction(TrackingAction.OnMapLoaded)
                updatePinViewportVisibility(map, state.currentLocation)
            }

            if (hasLocationPermission(context)) {
                map.isMyLocationEnabled = true
                val initialTarget = state.lastCoordinate ?: state.currentLocation
                if (initialTarget != null) {
                    if (latestAndroidLocation == null) {
                        dispatchUserLocationToBlueDot(initialTarget.toAndroidLocation())
                    }
                    focusCameraOnCenteredBounds(
                        map = map,
                        mapView = mapView,
                        target = initialTarget,
                        animate = false,
                        onCameraFocused = {
                            viewModel.onAction(TrackingAction.OnInitialCameraPositioned)
                        },
                    )
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null && state.pathPoints.isEmpty()) {
                            dispatchUserLocationToBlueDot(location)
                            val myLocation = LatLng(location.latitude, location.longitude)
                            focusCameraOnCenteredBounds(
                                map = map,
                                mapView = mapView,
                                target = myLocation,
                                animate = state.hasInitialCameraPositioned,
                                durationMs = CAMERA_ANIMATE_TO_MY_LOCATION_DURATION_MS,
                                onCameraFocused = {
                                    viewModel.onAction(TrackingAction.OnInitialCameraPositioned)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(googleMap, mapTopPaddingPx, mapBottomPaddingPx) {
        val map = googleMap ?: return@LaunchedEffect
        map.setPadding(0, mapTopPaddingPx, 0, mapBottomPaddingPx)
        val target = state.lastCoordinate ?: state.currentLocation
        if (target != null && state.isAutoFollowEnabled) {
            focusCameraOnCenteredBounds(
                map = map,
                mapView = mapView,
                target = target,
                animate = state.hasInitialCameraPositioned,
                durationMs = LOCATION_SMOOTH_ANIMATION_DURATION_MS,
                onCameraFocused = {
                    viewModel.onAction(TrackingAction.OnInitialCameraPositioned)
                },
            )
        }
    }

    LaunchedEffect(googleMap, state.isDarkTheme) {
        val map = googleMap ?: return@LaunchedEffect
        loadMapTheme(context, map, state.isDarkTheme)
    }

    // Auto-match camera with current user location when not actively drawing a new polyline point
    LaunchedEffect(googleMap, state.currentLocation, state.isAutoFollowEnabled) {
        val map = googleMap ?: return@LaunchedEffect
        val target = state.currentLocation ?: return@LaunchedEffect
        if (!state.isBlueDotRendered) {
            dispatchUserLocationToBlueDot(latestAndroidLocation ?: target.toAndroidLocation())
        }
        val hasActiveTrackPoint = state.pathPoints.lastOrNull()?.isNotEmpty() == true
        if (state.isAutoFollowEnabled && (!state.isTracking || !hasActiveTrackPoint)) {
            focusCameraOnCenteredBounds(
                map = map,
                mapView = mapView,
                target = target,
                animate = state.hasInitialCameraPositioned && state.isMapReady,
                durationMs = LOCATION_SMOOTH_ANIMATION_DURATION_MS,
                onCameraFocused = {
                    viewModel.onAction(TrackingAction.OnInitialCameraPositioned)
                },
            )
        }
    }

    LaunchedEffect(
        googleMap,
        state.pathPoints.size,
        state.polylineRevision,
        state.lastCoordinate,
    ) {
        val map = googleMap ?: return@LaunchedEffect
        val pathPoints = state.pathPoints

        if (pathPoints.isEmpty()) {
            renderedPolylines.forEach { it.remove() }
            renderedPolylines.clear()
            return@LaunchedEffect
        }

        while (renderedPolylines.size > pathPoints.size) {
            renderedPolylines.removeAt(renderedPolylines.lastIndex).remove()
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
        dispatchUserLocationToBlueDot(targetLatLng.toAndroidLocation())

        if (state.isAutoFollowEnabled) {
            focusCameraOnCenteredBounds(
                map = map,
                mapView = mapView,
                target = targetLatLng,
                animate = state.hasInitialCameraPositioned && state.isMapReady,
                durationMs = LOCATION_SMOOTH_ANIMATION_DURATION_MS,
                onCameraFocused = {
                    viewModel.onAction(TrackingAction.OnInitialCameraPositioned)
                },
            )
        }

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

    if (state.showCancelDialog) {
        CancelTrackingAlertDialog(
            onConfirmCancel = {
                viewModel.onAction(TrackingAction.OnConfirmCancelRun)
            },
            onDismiss = {
                viewModel.onAction(TrackingAction.OnDismissCancelDialog)
            },
        )
    }

    if (state.showFinishBottomSheet) {
        FinishRunBottomSheet(
            title = state.runTitle,
            showValidationError = state.showTitleValidationError,
            formattedDuration = state.formattedTimer,
            caloriesBurned = state.caloriesBurned.toString(),
            distanceInMeters = state.distanceInMeters.toString(),
            averageSpeed = state.averageSpeedInKMH.toString(),
            onTitleChange = { viewModel.onAction(TrackingAction.OnRunTitleChanged(it)) },
            onSave = { viewModel.onAction(TrackingAction.OnSubmitSaveRun) },
            onDismiss = { viewModel.onAction(TrackingAction.OnDismissFinishSheet) },
        )
    }

    val mapAlpha by animateFloatAsState(
        targetValue = if (state.isMapReady) 1f else 0f,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing,
        ),
        label = "MapSmoothFadeIn",
    )

    val isLoadingLocation = hasLocationPermission(context) && state.isLoadingLocation

    TrackingScreenContent(
        formattedTimer = state.formattedTimer,
        isTracking = state.isTracking,
        hasStarted = state.hasStarted,
        isMapReady = state.isMapReady,
        isLoadingLocation = isLoadingLocation,
        isAutoFollowEnabled = state.isAutoFollowEnabled,
        onToggleRun = { viewModel.onAction(TrackingAction.OnToggleRun) },
        onFinishClick = { viewModel.onAction(TrackingAction.OnShowFinishSheet) },
        onCancelClick = { viewModel.onAction(TrackingAction.OnShowCancelDialog) },
        onMyLocationClick = {
            val map = googleMap
            val target = state.lastCoordinate ?: state.currentLocation
            val pinCurrentlyVisible = map != null &&
                    target != null &&
                    state.isBlueDotRendered &&
                    isLocationVisibleOnMap(map, target)
            viewModel.onAction(TrackingAction.OnMyLocationClicked(pinCurrentlyVisible))
        },
        mapContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(mapAlpha),
                )
            }
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
    isLoadingLocation: Boolean = false,
    isAutoFollowEnabled: Boolean = true,
    onMyLocationClick: () -> Unit = {},
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

                LocationLoadingBanner(
                    visible = isLoadingLocation,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 24.dp, end = 24.dp),
                )

                MyLocationButton(
                    isCentered = isAutoFollowEnabled,
                    onClick = onMyLocationClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(24.dp),
                )
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp),
            ) {
                LocationLoadingBanner(
                    visible = isLoadingLocation,
                    modifier = Modifier.align(
                        if (hasStarted) Alignment.CenterStart else Alignment.Center,
                    ),
                )

                if (hasStarted) {
                    CancelRunPillButton(
                        onClick = onCancelClick,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }

            MyLocationButton(
                isCentered = isAutoFollowEnabled,
                onClick = onMyLocationClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 24.dp, bottom = 240.dp),
            )

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
private fun LocationLoadingBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) +
                slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                ),
        exit = fadeOut(animationSpec = tween(180)) +
                slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                ),
        modifier = modifier,
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = LerunMainDark.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, LerunGreen.copy(alpha = 0.3f)),
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = LerunGreen,
                    trackColor = LerunGreen.copy(alpha = 0.22f),
                )
                Text(
                    text = "Finding your location...",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun MyLocationButton(
    isCentered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.surface
    val iconColor = if (isCentered) {
        LerunGreen
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = modifier
            .size(48.dp)
            .shadow(elevation = 6.dp, shape = CircleShape),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Canvas(modifier = Modifier.size(22.dp)) {
                val strokeWidth = 2.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)
                val ringRadius = size.minDimension * 0.34f
                val crosshairLength = size.minDimension * 0.16f

                // Outer ring
                drawCircle(
                    color = iconColor,
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = strokeWidth),
                )

                // Center dot
                drawCircle(
                    color = iconColor,
                    radius = size.minDimension * 0.14f,
                    center = center,
                )

                // Top tick
                drawLine(
                    color = iconColor,
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, crosshairLength),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                // Bottom tick
                drawLine(
                    color = iconColor,
                    start = Offset(center.x, size.height - crosshairLength),
                    end = Offset(center.x, size.height),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                // Left tick
                drawLine(
                    color = iconColor,
                    start = Offset(0f, center.y),
                    end = Offset(crosshairLength, center.y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                // Right tick
                drawLine(
                    color = iconColor,
                    start = Offset(size.width - crosshairLength, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

/**
 * Builds a symmetric [LatLngBounds] centered on [center] with [radiusMeters] padding in all
 * directions, and moves or animates the [GoogleMap] camera so the user's location pin stays
 * centered inside the bounded map viewport.
 */
private fun focusCameraOnCenteredBounds(
    map: GoogleMap,
    mapView: MapView,
    target: LatLng,
    animate: Boolean,
    durationMs: Int = LOCATION_SMOOTH_ANIMATION_DURATION_MS,
    onCameraFocused: (() -> Unit)? = null,
) {
    val bounds = createCenteredBounds(target, USER_LOCATION_BOUNDS_RADIUS_METERS)
    val cameraUpdate = if (mapView.width > 0 && mapView.height > 0) {
        CameraUpdateFactory.newLatLngBounds(bounds, 0)
    } else {
        CameraUpdateFactory.newLatLngZoom(target, MAP_CAMERA_ZOOM)
    }

    val callback = if (onCameraFocused != null) {
        object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                onCameraFocused()
            }

            override fun onCancel() {
                onCameraFocused()
            }
        }
    } else {
        null
    }

    runCatching {
        if (animate) {
            map.animateCamera(cameraUpdate, durationMs, callback)
        } else {
            map.moveCamera(cameraUpdate)
            onCameraFocused?.invoke()
        }
    }.onFailure {
        val fallback = CameraUpdateFactory.newLatLngZoom(target, MAP_CAMERA_ZOOM)
        if (animate) {
            map.animateCamera(fallback, durationMs, callback)
        } else {
            map.moveCamera(fallback)
            onCameraFocused?.invoke()
        }
    }
}

private fun isLocationVisibleOnMap(map: GoogleMap, target: LatLng): Boolean =
    runCatching {
        map.projection.visibleRegion.latLngBounds.contains(target)
    }.getOrDefault(false)

private fun LatLng.toAndroidLocation(accuracyMeters: Float = 5f): Location =
    Location("TrackingScreen").apply {
        latitude = this@toAndroidLocation.latitude
        longitude = this@toAndroidLocation.longitude
        accuracy = accuracyMeters
        time = System.currentTimeMillis()
    }

private fun createCenteredBounds(
    center: LatLng,
    radiusMeters: Double,
): LatLngBounds {
    val latDelta = radiusMeters / 111_320.0
    val cosLat = cos(Math.toRadians(center.latitude)).coerceAtLeast(0.0001)
    val lngDelta = radiusMeters / (111_320.0 * cosLat)

    val southwest = LatLng(center.latitude - latDelta, center.longitude - lngDelta)
    val northeast = LatLng(center.latitude + latDelta, center.longitude + lngDelta)
    return LatLngBounds(southwest, northeast)
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
    title: String,
    showValidationError: Boolean,
    formattedDuration: String,
    caloriesBurned: String,
    distanceInMeters: String,
    averageSpeed: String,
    onTitleChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                onValueChange = onTitleChange,
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
                onClick = onSave,
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
    val mapView = remember(context) {
        MapsInitializer.initialize(
            context.applicationContext,
            MapsInitializer.Renderer.LATEST,
            null,
        )
        MapView(context)
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
