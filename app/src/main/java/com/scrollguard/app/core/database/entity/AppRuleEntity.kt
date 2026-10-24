package com.scrollguard.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isMonitored: Boolean = true,
    val isReelsBlocked: Boolean = true,
    val isScrollLimitBlocked: Boolean = true,
    val customScrollLimitMinutes: Int = 10,
    val iconUri: String? = null
)
