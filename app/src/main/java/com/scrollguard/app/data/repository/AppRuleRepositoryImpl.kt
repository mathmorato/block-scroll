package com.scrollguard.app.data.repository

import com.scrollguard.app.core.database.dao.AppRuleDao
import com.scrollguard.app.core.database.entity.AppRuleEntity
import com.scrollguard.app.domain.model.AppRule
import com.scrollguard.app.domain.repository.AppRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRuleRepositoryImpl @Inject constructor(
    private val appRuleDao: AppRuleDao
) : AppRuleRepository {

    override fun getAllRules(): Flow<List<AppRule>> {
        return appRuleDao.getAllRules().map { list -> list.map { it.toDomain() } }
    }

    override fun getMonitoredRules(): Flow<List<AppRule>> {
        return appRuleDao.getMonitoredRules().map { list -> list.map { it.toDomain() } }
    }

    override fun getRuleForPackage(packageName: String): Flow<AppRule?> {
        return appRuleDao.getRuleForPackage(packageName).map { it?.toDomain() }
    }

    override suspend fun getRuleForPackageSync(packageName: String): AppRule? {
        return appRuleDao.getRuleForPackageSync(packageName)?.toDomain()
    }

    override suspend fun upsertRule(rule: AppRule) {
        appRuleDao.insertOrUpdate(rule.toEntity())
    }

    override suspend fun insertDefaultRulesIfEmpty() {
        val existing = appRuleDao.getAllRules().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaults = listOf(
                AppRuleEntity(
                    packageName = "com.instagram.android",
                    appName = "Instagram",
                    isMonitored = true,
                    isReelsBlocked = true,
                    isScrollLimitBlocked = true,
                    customScrollLimitMinutes = 10
                ),
                AppRuleEntity(
                    packageName = "com.google.android.youtube",
                    appName = "YouTube",
                    isMonitored = true,
                    isReelsBlocked = true,
                    isScrollLimitBlocked = true,
                    customScrollLimitMinutes = 15
                ),
                AppRuleEntity(
                    packageName = "com.zhiliaoapp.musically",
                    appName = "TikTok",
                    isMonitored = true,
                    isReelsBlocked = true,
                    isScrollLimitBlocked = true,
                    customScrollLimitMinutes = 10
                ),
                AppRuleEntity(
                    packageName = "com.ss.android.ugc.trill",
                    appName = "TikTok (Global)",
                    isMonitored = true,
                    isReelsBlocked = true,
                    isScrollLimitBlocked = true,
                    customScrollLimitMinutes = 10
                ),
                AppRuleEntity(
                    packageName = "com.facebook.katana",
                    appName = "Facebook",
                    isMonitored = true,
                    isReelsBlocked = true,
                    isScrollLimitBlocked = true,
                    customScrollLimitMinutes = 10
                ),
                AppRuleEntity(
                    packageName = "com.twitter.android",
                    appName = "X (Twitter)",
                    isMonitored = true,
                    isReelsBlocked = false,
                    isScrollLimitBlocked = true,
                    customScrollLimitMinutes = 15
                ),
                AppRuleEntity(
                    packageName = "com.android.chrome",
                    appName = "Google Chrome",
                    isMonitored = true,
                    isReelsBlocked = false,
                    isScrollLimitBlocked = false,
                    customScrollLimitMinutes = 30
                )
            )
            appRuleDao.insertRules(defaults)
        }
    }

    override suspend fun deleteRule(packageName: String) {
        appRuleDao.deleteRule(packageName)
    }

    private fun AppRuleEntity.toDomain(): AppRule {
        return AppRule(
            packageName = packageName,
            appName = appName,
            isMonitored = isMonitored,
            isReelsBlocked = isReelsBlocked,
            isScrollLimitBlocked = isScrollLimitBlocked,
            customScrollLimitMinutes = customScrollLimitMinutes,
            iconUri = iconUri
        )
    }

    private fun AppRule.toEntity(): AppRuleEntity {
        return AppRuleEntity(
            packageName = packageName,
            appName = appName,
            isMonitored = isMonitored,
            isReelsBlocked = isReelsBlocked,
            isScrollLimitBlocked = isScrollLimitBlocked,
            customScrollLimitMinutes = customScrollLimitMinutes,
            iconUri = iconUri
        )
    }
}
