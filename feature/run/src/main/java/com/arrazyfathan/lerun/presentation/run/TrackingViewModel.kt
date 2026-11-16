package com.arrazyfathan.lerun.presentation.run

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.usecase.InsertRunUseCase
import com.arrazyfathan.lerun.domain.usecase.ObserveUserSettingsUseCase
import com.arrazyfathan.lerun.services.TrackingService
import com.arrazyfathan.lerun.utils.Constants.ACTION_PAUSE_SERVICE
import com.arrazyfathan.lerun.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.arrazyfathan.lerun.utils.Constants.ACTION_STOP_SERVICE
import com.arrazyfathan.lerun.utils.TrackingUtility
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

@Immutable
data class TrackingState(
    val isTracking: Boolean = false,
    val pathPoints: List<List<LatLng>> = emptyList(),
    val timeRunInMillis: Long = 0L,
    val formattedTimer: String = "00:00:00:00",
    val distanceInMeters: Int = 0,
    val averageSpeedInKMH: Float = 0f,
    val caloriesBurned: Int = 0,
    val profileWeight: Int = 0,
    val isDarkTheme: Boolean = true,
    val isMapReady: Boolean = false,
    val hasInitialCameraPositioned: Boolean = false,
    val isAutoFollowEnabled: Boolean = true,
    val currentLocation: LatLng? = null,
    val isBlueDotRendered: Boolean = false,
    val isUserPinVisibleInViewport: Boolean = false,
    val isRecenteringToUserPin: Boolean = false,
    val showCancelDialog: Boolean = false,
    val showFinishBottomSheet: Boolean = false,
    val runTitle: String = "",
    val showTitleValidationError: Boolean = false,
) {
    val hasStarted: Boolean
        get() = timeRunInMillis > 0L

    val isUserPinVisible: Boolean
        get() = isMapReady && isBlueDotRendered && isUserPinVisibleInViewport

    val isLoadingLocation: Boolean
        get() = !isUserPinVisible || isRecenteringToUserPin

    val polylineRevision: Int
        get() = pathPoints.sumOf { it.size }

    val lastCoordinate: LatLng?
        get() = pathPoints.lastOrNull()?.lastOrNull()
}

sealed interface TrackingAction {
    data object OnToggleRun : TrackingAction
    data object OnShowCancelDialog : TrackingAction
    data object OnDismissCancelDialog : TrackingAction
    data object OnConfirmCancelRun : TrackingAction
    data object OnShowFinishSheet : TrackingAction
    data object OnDismissFinishSheet : TrackingAction
    data class OnRunTitleChanged(val title: String) : TrackingAction
    data object OnSubmitSaveRun : TrackingAction
    data class OnSaveRunWithSnapshot(val bitmap: Bitmap?) : TrackingAction
    data object OnMapLoaded : TrackingAction
    data object OnInitialCameraPositioned : TrackingAction
    data object OnUserGesturePan : TrackingAction
    data class OnMyLocationClicked(val isPinCurrentlyVisible: Boolean) : TrackingAction
    data class OnLocationUpdated(
        val location: LatLng,
        val isBlueDotRendered: Boolean = true,
    ) : TrackingAction

    data object OnBlueDotRendered : TrackingAction
    data class OnPinViewportVisibilityChanged(val isVisible: Boolean) : TrackingAction
    data object OnRecenterFinished : TrackingAction
}

sealed interface TrackingEvent {
    data class SendServiceCommand(val action: String) : TrackingEvent
    data class AnimateToMyLocation(val target: LatLng?) : TrackingEvent
    data object CaptureMapSnapshotAndSave : TrackingEvent
    data class ShowMessage(val message: String) : TrackingEvent
    data object CloseTrackingScreen : TrackingEvent
}

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val insertRunUseCase: InsertRunUseCase,
    observeUserSettingsUseCase: ObserveUserSettingsUseCase,
) : ViewModel() {

    private val initialSettings = observeUserSettingsUseCase.current()

    private val _state = MutableStateFlow(
        TrackingState(
            profileWeight = initialSettings.weightInKg,
            isDarkTheme = initialSettings.isDarkTheme,
        ),
    )
    val state = _state.asStateFlow()

    private val _event = Channel<TrackingEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        observeUserSettingsUseCase()
            .onEach { settings ->
                _state.update { current ->
                    current
                        .copy(
                            profileWeight = settings.weightInKg,
                            isDarkTheme = settings.isDarkTheme,
                        )
                        .withRecalculatedMetrics()
                }
            }
            .launchIn(viewModelScope)

        TrackingService.isTracking
            .onEach { isTracking ->
                _state.update { it.copy(isTracking = isTracking) }
            }
            .launchIn(viewModelScope)

        TrackingService.pathPoint
            .onEach { polylines ->
                val immutablePolylines = polylines.map { it.toList() }
                val latestTrackPoint = immutablePolylines.lastOrNull()?.lastOrNull()
                _state.update { current ->
                    current
                        .copy(
                            pathPoints = immutablePolylines,
                            currentLocation = latestTrackPoint ?: current.currentLocation,
                        )
                        .withRecalculatedMetrics()
                }
            }
            .launchIn(viewModelScope)

        TrackingService.timeRunInMillis
            .onEach { timeMillis ->
                _state.update { current ->
                    current
                        .copy(
                            timeRunInMillis = timeMillis,
                            formattedTimer = TrackingUtility.getFormattedStopWatchTime(
                                timeMillis,
                                true,
                            ),
                        )
                        .withRecalculatedMetrics()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: TrackingAction) {
        when (action) {
            TrackingAction.OnToggleRun -> handleToggleRun()

            TrackingAction.OnShowCancelDialog -> {
                _state.update { it.copy(showCancelDialog = true) }
            }

            TrackingAction.OnDismissCancelDialog -> {
                _state.update { it.copy(showCancelDialog = false) }
            }

            TrackingAction.OnConfirmCancelRun -> {
                _state.update { it.copy(showCancelDialog = false) }
                stopRunAndClose()
            }

            TrackingAction.OnShowFinishSheet -> {
                _state.update {
                    it.copy(
                        showFinishBottomSheet = true,
                        showTitleValidationError = false,
                    ).withRecalculatedMetrics()
                }
            }

            TrackingAction.OnDismissFinishSheet -> {
                _state.update {
                    it.copy(
                        showFinishBottomSheet = false,
                        showTitleValidationError = false,
                    )
                }
            }

            is TrackingAction.OnRunTitleChanged -> {
                _state.update {
                    it.copy(
                        runTitle = action.title,
                        showTitleValidationError = action.title.isBlank(),
                    )
                }
            }

            TrackingAction.OnSubmitSaveRun -> {
                val trimmedTitle = _state.value.runTitle.trim()
                if (trimmedTitle.isBlank()) {
                    _state.update { it.copy(showTitleValidationError = true) }
                } else {
                    _state.update {
                        it.copy(
                            runTitle = trimmedTitle,
                            showFinishBottomSheet = false,
                            showTitleValidationError = false,
                        )
                    }
                    viewModelScope.launch {
                        _event.send(TrackingEvent.CaptureMapSnapshotAndSave)
                    }
                }
            }

            is TrackingAction.OnSaveRunWithSnapshot -> {
                saveRunToDatabase(action.bitmap)
            }

            TrackingAction.OnMapLoaded -> {
                _state.update { it.copy(isMapReady = true) }
            }

            TrackingAction.OnInitialCameraPositioned -> {
                _state.update {
                    it.copy(
                        hasInitialCameraPositioned = true,
                        isUserPinVisibleInViewport = true,
                        isRecenteringToUserPin = false,
                    )
                }
            }

            TrackingAction.OnUserGesturePan -> {
                _state.update { it.copy(isAutoFollowEnabled = false) }
            }

            is TrackingAction.OnMyLocationClicked -> {
                val current = _state.value
                val target = current.lastCoordinate ?: current.currentLocation
                _state.update {
                    it.copy(
                        isAutoFollowEnabled = true,
                        isRecenteringToUserPin = !action.isPinCurrentlyVisible,
                    )
                }
                viewModelScope.launch {
                    _event.send(TrackingEvent.AnimateToMyLocation(target))
                }
            }

            is TrackingAction.OnLocationUpdated -> {
                _state.update {
                    it.copy(
                        currentLocation = action.location,
                        isBlueDotRendered = it.isBlueDotRendered || action.isBlueDotRendered,
                    )
                }
            }

            TrackingAction.OnBlueDotRendered -> {
                _state.update { it.copy(isBlueDotRendered = true) }
            }

            is TrackingAction.OnPinViewportVisibilityChanged -> {
                _state.update {
                    it.copy(
                        isUserPinVisibleInViewport = action.isVisible,
                        isRecenteringToUserPin = if (action.isVisible) {
                            false
                        } else {
                            it.isRecenteringToUserPin
                        },
                    )
                }
            }

            TrackingAction.OnRecenterFinished -> {
                _state.update {
                    it.copy(
                        isUserPinVisibleInViewport = true,
                        isRecenteringToUserPin = false,
                    )
                }
            }
        }
    }

    private fun handleToggleRun() {
        val current = _state.value
        if (!current.isMapReady && !current.isTracking) return
        viewModelScope.launch {
            if (current.isTracking) {
                _event.send(TrackingEvent.SendServiceCommand(ACTION_PAUSE_SERVICE))
            } else {
                _state.update { it.copy(isAutoFollowEnabled = true) }
                _event.send(TrackingEvent.SendServiceCommand(ACTION_START_OR_RESUME_SERVICE))
            }
        }
    }

    private fun saveRunToDatabase(bitmap: Bitmap?) {
        val current = _state.value.withRecalculatedMetrics()
        val run = Run(
            img = bitmap,
            title = current.runTitle.ifBlank { "Run" },
            timestamp = Calendar.getInstance().timeInMillis,
            avgSpeedInKMH = current.averageSpeedInKMH,
            distanceInMeters = current.distanceInMeters,
            timeInMillis = current.timeRunInMillis,
            caloriesBurned = current.caloriesBurned,
        )
        viewModelScope.launch {
            insertRunUseCase(run)
            _event.send(TrackingEvent.ShowMessage("Run saved successfully"))
            _event.send(TrackingEvent.SendServiceCommand(ACTION_STOP_SERVICE))
            _event.send(TrackingEvent.CloseTrackingScreen)
        }
    }

    private fun stopRunAndClose() {
        viewModelScope.launch {
            _event.send(TrackingEvent.SendServiceCommand(ACTION_STOP_SERVICE))
            _event.send(TrackingEvent.CloseTrackingScreen)
        }
    }

    private fun TrackingState.withRecalculatedMetrics(): TrackingState {
        var totalDistance = 0
        for (polyline in pathPoints) {
            totalDistance += TrackingUtility.calculatePolylineLength(polyline).toInt()
        }
        val hours = timeRunInMillis / 1000f / 60f / 60f
        val avgSpeed = if (hours > 0f) {
            round((totalDistance / 1000f) / hours * 10f) / 10f
        } else {
            0f
        }
        val calories = ((totalDistance / 1000f) * profileWeight.toFloat()).toInt()
        return copy(
            distanceInMeters = totalDistance,
            averageSpeedInKMH = avgSpeed,
            caloriesBurned = calories,
        )
    }
}
