package com.scrollguard.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.scrollguard.app.core.database.entity.BlockLogEntity
import kotlinx.coroutines.flow.Flow

data class AppBlockStat(
    val packageName: String,
    val appName: String,
    val count: Int
)

data class DailyBlockStat(
    val dateKey: String,
    val count: Int
)

@Dao
interface BlockLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: BlockLogEntity): Long

    @Query("SELECT * FROM block_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 30): Flow<List<BlockLogEntity>>

    @Query("SELECT COUNT(*) FROM block_logs WHERE timestamp >= :startOfDay")
    fun getTodayBlockCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(timeSavedSeconds), 0) FROM block_logs WHERE timestamp >= :startOfDay")
    fun getTodayTimeSavedSeconds(startOfDay: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(timeSavedSeconds), 0) FROM block_logs")
    fun getTotalTimeSavedSeconds(): Flow<Long>

    @Query("SELECT COUNT(*) FROM block_logs")
    fun getTotalBlockCount(): Flow<Int>

    @Query("""
        SELECT packageName, appName, COUNT(*) as count 
        FROM block_logs 
        WHERE timestamp >= :sinceTimestamp 
        GROUP BY packageName 
        ORDER BY count DESC 
        LIMIT :limit
    """)
    fun getTopBlockedApps(sinceTimestamp: Long, limit: Int = 5): Flow<List<AppBlockStat>>

    @Query("""
        SELECT strftime('%Y-%m-%d', datetime(timestamp / 1000, 'unixepoch', 'localtime')) as dateKey, 
               COUNT(*) as count 
        FROM block_logs 
        WHERE timestamp >= :sinceTimestamp 
        GROUP BY dateKey 
        ORDER BY dateKey ASC
    """)
    fun getDailyStats(sinceTimestamp: Long): Flow<List<DailyBlockStat>>

    @Query("DELETE FROM block_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldLogs(cutoffTimestamp: Long): Int
}
