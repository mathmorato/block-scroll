package com.scrollguard.app.data.repository

import com.scrollguard.app.core.database.dao.AppBlockStat
import com.scrollguard.app.core.database.dao.BlockLogDao
import com.scrollguard.app.core.database.dao.DailyBlockStat
import com.scrollguard.app.core.database.entity.BlockLogEntity
import com.scrollguard.app.domain.model.BlockLog
import com.scrollguard.app.domain.model.BlockType
import com.scrollguard.app.domain.repository.BlockLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockLogRepositoryImpl @Inject constructor(
    private val blockLogDao: BlockLogDao
) : BlockLogRepository {

    override suspend fun insertLog(log: BlockLog): Long {
        return blockLogDao.insert(
            BlockLogEntity(
                id = log.id,
                packageName = log.packageName,
                appName = log.appName,
                blockType = log.blockType.name,
                details = log.details,
                timestamp = log.timestamp,
                timeSavedSeconds = log.timeSavedSeconds
            )
        )
    }

    override fun getRecentLogs(limit: Int): Flow<List<BlockLog>> {
        return blockLogDao.getRecentLogs(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTodayBlockCount(): Flow<Int> {
        return blockLogDao.getTodayBlockCount(getStartOfTodayTimestamp())
    }

    override fun getTodayTimeSavedSeconds(): Flow<Long> {
        return blockLogDao.getTodayTimeSavedSeconds(getStartOfTodayTimestamp())
    }

    override fun getTotalBlockCount(): Flow<Int> {
        return blockLogDao.getTotalBlockCount()
    }

    override fun getTotalTimeSavedSeconds(): Flow<Long> {
        return blockLogDao.getTotalTimeSavedSeconds()
    }

    override fun getTopBlockedApps(sinceTimestamp: Long, limit: Int): Flow<List<AppBlockStat>> {
        return blockLogDao.getTopBlockedApps(sinceTimestamp, limit)
    }

    override fun getDailyStats(sinceTimestamp: Long): Flow<List<DailyBlockStat>> {
        return blockLogDao.getDailyStats(sinceTimestamp)
    }

    override suspend fun deleteOldLogs(cutoffTimestamp: Long): Int {
        return blockLogDao.deleteOldLogs(cutoffTimestamp)
    }

    private fun getStartOfTodayTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun BlockLogEntity.toDomain(): BlockLog {
        val type = try {
            BlockType.valueOf(blockType)
        } catch (_: Exception) {
            BlockType.REELS
        }
        return BlockLog(
            id = id,
            packageName = packageName,
            appName = appName,
            blockType = type,
            details = details,
            timestamp = timestamp,
            timeSavedSeconds = timeSavedSeconds
        )
    }
}
