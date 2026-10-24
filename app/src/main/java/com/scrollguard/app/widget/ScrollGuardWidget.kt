package com.scrollguard.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.scrollguard.app.MainActivity
import com.scrollguard.app.R
import com.scrollguard.app.core.database.ScrollGuardDatabase
import com.scrollguard.app.core.util.PermissionHelper
import com.scrollguard.app.service.accessibility.ScrollGuardAccessibilityService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class ScrollGuardWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun database(): ScrollGuardDatabase
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isServiceActive = ScrollGuardAccessibilityService.isServiceRunning &&
                PermissionHelper.isAccessibilityServiceEnabled(context)

        var todayCount = 0
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            todayCount = entryPoint.database().blockLogDao().getTodayBlockCount(startOfDay).firstOrNull() ?: 0
        } catch (_: Exception) {
            // fallback
        }

        provideContent {
            GlanceTheme {
                WidgetContent(
                    isServiceActive = isServiceActive,
                    todayBlockedCount = todayCount
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        isServiceActive: Boolean,
        todayBlockedCount: Int
    ) {
        val backgroundColor = ColorProvider(android.graphics.Color.parseColor("#0F172A"))
        val cardColor = ColorProvider(android.graphics.Color.parseColor("#1E293B"))
        val statusColor = if (isServiceActive) {
            ColorProvider(android.graphics.Color.parseColor("#10B981")) // Green
        } else {
            ColorProvider(android.graphics.Color.parseColor("#EF4444")) // Red
        }
        val textPrimary = ColorProvider(android.graphics.Color.parseColor("#F8FAFC"))
        val textSecondary = ColorProvider(android.graphics.Color.parseColor("#94A3B8"))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_shield),
                    contentDescription = "Escudo ScrollGuard",
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = if (isServiceActive) "Protegido" else "Desativado",
                    style = TextStyle(
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(cardColor)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = todayBlockedCount.toString(),
                    style = TextStyle(
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "Distrações Bloqueadas Hoje",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Button(
                text = "Abrir ScrollGuard",
                onClick = actionStartActivity<MainActivity>(),
                modifier = GlanceModifier.fillMaxWidth().height(36.dp)
            )
        }
    }
}

class ScrollGuardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScrollGuardWidget()
}
