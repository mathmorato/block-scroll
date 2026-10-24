package com.scrollguard.app.core.di

import android.content.Context
import com.scrollguard.app.core.util.DomainTrie
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDomainTrie(
        @ApplicationContext context: Context
    ): DomainTrie {
        return DomainTrie().apply {
            loadFromAssets(context)
        }
    }
}
