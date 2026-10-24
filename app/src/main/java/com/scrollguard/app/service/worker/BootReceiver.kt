package com.scrollguard.app.service.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            scheduleWorkers(context)
        }
    }

    companion object {
        fun scheduleWorkers(context: Context) {
            val workManager = WorkManager.getInstance(context)

            // 1. Health Worker runs every 1 hour
            val healthRequest = PeriodicWorkRequestBuilder<ServiceHealthWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            ).build()

            workManager.enqueueUniquePeriodicWork(
                "ScrollGuardServiceHealth",
                ExistingPeriodicWorkPolicy.KEEP,
                healthRequest
            )

            // 2. Daily Reset Worker runs every 24 hours
            val dailyResetRequest = PeriodicWorkRequestBuilder<DailyResetWorker>(
                24, TimeUnit.HOURS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                "ScrollGuardDailyReset",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyResetRequest
            )
        }
    }
}
