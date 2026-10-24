package com.scrollguard.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.scrollguard.app.core.database.dao.AppRuleDao
import com.scrollguard.app.core.database.dao.BlockLogDao
import com.scrollguard.app.core.database.entity.AppRuleEntity
import com.scrollguard.app.core.database.entity.BlockLogEntity

@Database(
    entities = [
        BlockLogEntity::class,
        AppRuleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ScrollGuardDatabase : RoomDatabase() {
    abstract fun blockLogDao(): BlockLogDao
    abstract fun appRuleDao(): AppRuleDao

    companion object {
        const val DATABASE_NAME = "scrollguard_db"
    }
}
