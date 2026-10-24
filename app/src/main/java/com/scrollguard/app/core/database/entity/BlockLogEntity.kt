package com.scrollguard.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "block_logs",
    indices = [
        Index("timestamp"),
        Index("packageName"),
        Index("blockType")
    ]
)
data class BlockLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val blockType: String, // REELS, SCROLL_LIMIT, ADULT_CONTENT
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timeSavedSeconds: Long = 180 // Default estimation: 3 minutes saved per block
)
