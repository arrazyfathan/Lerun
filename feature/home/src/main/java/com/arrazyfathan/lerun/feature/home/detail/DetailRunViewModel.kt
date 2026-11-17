package com.arrazyfathan.lerun.feature.home.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.usecase.GetRunByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class DetailRunState(
    val selectedRun: Run? = null,
)

sealed interface DetailRunAction {
    data class OnLoadRunById(
        val runId: Int,
    ) : DetailRunAction
}

@HiltViewModel
class DetailRunViewModel
    @Inject
    constructor(
        private val getRunByIdUseCase: GetRunByIdUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(DetailRunState())
        val state = _state.asStateFlow()

        fun onAction(action: DetailRunAction) {
            when (action) {
                is DetailRunAction.OnLoadRunById -> {
                    viewModelScope.launch {
                        val existing =
                            _state.value.selectedRun?.takeIf { it.id == action.runId }
                                ?: getRunByIdUseCase(action.runId)
                        if (existing != null) {
                            _state.update { it.copy(selectedRun = existing) }
                        }
                    }
                }
            }
        }
    }
