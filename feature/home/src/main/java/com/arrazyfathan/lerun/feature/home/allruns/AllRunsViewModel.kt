package com.arrazyfathan.lerun.feature.home.allruns

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.model.SortType
import com.arrazyfathan.lerun.domain.usecase.ObserveRunsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Immutable
data class AllRunsState(
    val runs: List<Run> = emptyList(),
    val sortType: SortType = SortType.DATE,
    val isSortDropdownExpanded: Boolean = false,
    val expandedRunIds: Set<Int> = emptySet(),
)

sealed interface AllRunsAction {
    data class OnSortDropdownToggled(
        val expanded: Boolean,
    ) : AllRunsAction

    data class OnSortTypeChanged(
        val sortType: SortType,
    ) : AllRunsAction

    data class OnToggleExpandRun(
        val runId: Int,
    ) : AllRunsAction
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AllRunsViewModel
    @Inject
    constructor(
        private val observeRunsUseCase: ObserveRunsUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(AllRunsState())
        val state = _state.asStateFlow()

        init {
            observeSortedRuns()
        }

        fun onAction(action: AllRunsAction) {
            when (action) {
                is AllRunsAction.OnSortDropdownToggled -> {
                    _state.update { it.copy(isSortDropdownExpanded = action.expanded) }
                }

                is AllRunsAction.OnSortTypeChanged -> {
                    _state.update {
                        it.copy(
                            sortType = action.sortType,
                            isSortDropdownExpanded = false,
                        )
                    }
                }

                is AllRunsAction.OnToggleExpandRun -> {
                    _state.update { current ->
                        val updatedIds =
                            if (action.runId in current.expandedRunIds) {
                                current.expandedRunIds - action.runId
                            } else {
                                current.expandedRunIds + action.runId
                            }
                        current.copy(expandedRunIds = updatedIds)
                    }
                }
            }
        }

        private fun observeSortedRuns() {
            _state
                .map { it.sortType }
                .distinctUntilChanged()
                .flatMapLatest { sortType ->
                    observeRunsUseCase(sortType)
                }.onEach { sortedRuns ->
                    _state.update { it.copy(runs = sortedRuns) }
                }.launchIn(viewModelScope)
        }
    }
