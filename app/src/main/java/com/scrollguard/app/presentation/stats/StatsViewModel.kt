package com.scrollguard.app.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollguard.app.core.database.dao.AppBlockStat
import com.scrollguard.app.core.database.dao.DailyBlockStat
import com.scrollguard.app.domain.repository.BlockLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StatsUiState(
    val weeklyStats: List<DailyBlockStat> = emptyList(),
    val topApps: List<AppBlockStat> = emptyList(),
    val totalBlocks: Int = 0,
    val totalTimeSavedMinutes: Long = 0,
    val todayBlocks: Int = 0,
    val todayTimeSavedMinutes: Long = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val blockLogRepository: BlockLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val sevenDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
            }.timeInMillis

            combine(
                blockLogRepository.getDailyStats(sevenDaysAgo),
                blockLogRepository.getTopBlockedApps(sevenDaysAgo, 5),
                blockLogRepository.getTotalBlockCount(),
                blockLogRepository.getTotalTimeSavedSeconds(),
                blockLogRepository.getTodayBlockCount(),
                blockLogRepository.getTodayTimeSavedSeconds()
            ) { daily, topApps, totalCount, totalSecs, todayCount, todaySecs ->
                StatsUiState(
                    weeklyStats = daily,
                    topApps = topApps,
                    totalBlocks = totalCount,
                    totalTimeSavedMinutes = totalSecs / 60,
                    todayBlocks = todayCount,
                    todayTimeSavedMinutes = todaySecs / 60
                )
            }.collect {
                _uiState.value = it
            }
        }
    }
}
