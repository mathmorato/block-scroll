package com.scrollguard.app.domain.usecase

import com.scrollguard.app.core.datastore.UserPreferencesRepository
import com.scrollguard.app.domain.model.StatsSummary
import com.scrollguard.app.domain.repository.BlockLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val blockLogRepository: BlockLogRepository,
    private val preferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<StatsSummary> {
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.timeInMillis

        return combine(
            blockLogRepository.getTodayBlockCount(),
            blockLogRepository.getTodayTimeSavedSeconds(),
            blockLogRepository.getTotalBlockCount(),
            blockLogRepository.getTotalTimeSavedSeconds(),
            preferencesRepository.userPreferencesFlow,
            blockLogRepository.getTopBlockedApps(sevenDaysAgo, 5),
            blockLogRepository.getDailyStats(sevenDaysAgo)
        ) { todayCount, todaySeconds, totalCount, totalSeconds, prefs, topApps, dailyStats ->
            StatsSummary(
                todayBlocksCount = todayCount,
                todayTimeSavedMinutes = todaySeconds / 60,
                totalBlocksCount = totalCount,
                totalTimeSavedMinutes = totalSeconds / 60,
                streakDays = prefs.streakDays,
                topApps = topApps.map { it.appName to it.count },
                weeklyStats = dailyStats.map { it.dateKey to it.count }
            )
        }
    }
}
