package com.scrollguard.app.core.di

import android.content.Context
import androidx.room.Room
import com.scrollguard.app.core.database.ScrollGuardDatabase
import com.scrollguard.app.core.database.dao.AppRuleDao
import com.scrollguard.app.core.database.dao.BlockLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideScrollGuardDatabase(
        @ApplicationContext context: Context
    ): ScrollGuardDatabase {
        return Room.databaseBuilder(
            context,
            ScrollGuardDatabase::class.java,
            ScrollGuardDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideBlockLogDao(database: ScrollGuardDatabase): BlockLogDao {
        return database.blockLogDao()
    }

    @Provides
    fun provideAppRuleDao(database: ScrollGuardDatabase): AppRuleDao {
        return database.appRuleDao()
    }
}
