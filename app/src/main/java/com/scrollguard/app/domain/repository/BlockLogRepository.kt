package com.scrollguard.app.domain.repository

import com.scrollguard.app.core.database.dao.AppBlockStat
import com.scrollguard.app.core.database.dao.DailyBlockStat
import com.scrollguard.app.domain.model.BlockLog
import kotlinx.coroutines.flow.Flow

interface BlockLogRepository {
    suspend fun insertLog(log: BlockLog): Long
    fun getRecentLogs(limit: Int = 30): Flow<List<BlockLog>>
    fun getTodayBlockCount(): Flow<Int>
    fun getTodayTimeSavedSeconds(): Flow<Long>
    fun getTotalBlockCount(): Flow<Int>
    fun getTotalTimeSavedSeconds(): Flow<Long>
    fun getTopBlockedApps(sinceTimestamp: Long, limit: Int = 5): Flow<List<AppBlockStat>>
    fun getDailyStats(sinceTimestamp: Long): Flow<List<DailyBlockStat>>
    suspend fun deleteOldLogs(cutoffTimestamp: Long): Int
}
