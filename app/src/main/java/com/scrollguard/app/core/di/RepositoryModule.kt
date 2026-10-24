package com.scrollguard.app.core.di

import com.scrollguard.app.data.repository.AppRuleRepositoryImpl
import com.scrollguard.app.data.repository.BlockLogRepositoryImpl
import com.scrollguard.app.domain.repository.AppRuleRepository
import com.scrollguard.app.domain.repository.BlockLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBlockLogRepository(
        impl: BlockLogRepositoryImpl
    ): BlockLogRepository

    @Binds
    @Singleton
    abstract fun bindAppRuleRepository(
        impl: AppRuleRepositoryImpl
    ): AppRuleRepository
}
