package com.scrollguard.app.service.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.scrollguard.app.MainActivity
import com.scrollguard.app.R
import com.scrollguard.app.core.datastore.UserPreferencesRepository
import com.scrollguard.app.core.util.PermissionHelper
import com.scrollguard.app.service.accessibility.ScrollGuardAccessibilityService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ServiceHealthWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "scrollguard_health_channel"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        if (!prefs.isOnboardingCompleted || !prefs.isGuardianActive) {
            return Result.success()
        }

        val isServiceActive = ScrollGuardAccessibilityService.isServiceRunning &&
                PermissionHelper.isAccessibilityServiceEnabled(appContext)

        if (!isServiceActive) {
            notifyServiceDown()
        }

        return Result.success()
    }

    private fun notifyServiceDown() {
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alerta de Proteção ScrollGuard",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisa quando a proteção de rolagem foi desativada pelo sistema operacional"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("Alerta: Proteção ScrollGuard Inativa!")
            .setContentText("O sistema pode ter encerrado o serviço de acessibilidade. Toque para reativar seu escudo.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
