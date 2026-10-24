package com.scrollguard.app.domain.usecase

import com.scrollguard.app.domain.model.BlockLog
import com.scrollguard.app.domain.model.BlockType
import com.scrollguard.app.domain.repository.BlockLogRepository
import javax.inject.Inject

class LogBlockEventUseCase @Inject constructor(
    private val blockLogRepository: BlockLogRepository
) {
    suspend operator fun invoke(
        packageName: String,
        appName: String,
        blockType: BlockType,
        details: String,
        timeSavedSeconds: Long = when (blockType) {
            BlockType.REELS -> 180L // ~3 minutes per short video rabbit hole prevented
            BlockType.SCROLL_LIMIT -> 300L // ~5 minutes saved by enforcing break
            BlockType.ADULT_CONTENT -> 600L // ~10 minutes saved
        }
    ): Long {
        val log = BlockLog(
            packageName = packageName,
            appName = appName,
            blockType = blockType,
            details = details,
            timestamp = System.currentTimeMillis(),
            timeSavedSeconds = timeSavedSeconds
        )
        return blockLogRepository.insertLog(log)
    }
}
