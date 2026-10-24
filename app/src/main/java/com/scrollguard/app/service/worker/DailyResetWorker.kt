package com.scrollguard.app.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.scrollguard.app.core.datastore.UserPreferencesRepository
import com.scrollguard.app.domain.repository.BlockLogRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferencesRepository: UserPreferencesRepository,
    private val blockLogRepository: BlockLogRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val prefs = preferencesRepository.userPreferencesFlow.first()

            if (prefs.lastActiveDate != todayStr) {
                val newStreak = prefs.streakDays + 1
                preferencesRepository.updateStreak(newStreak, todayStr)
            }

            // Cleanup logs older than 30 days to keep the database lightweight
            val thirtyDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
            }.timeInMillis
            blockLogRepository.deleteOldLogs(thirtyDaysAgo)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
