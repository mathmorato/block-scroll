package com.scrollguard.app.domain.repository

import com.scrollguard.app.domain.model.AppRule
import kotlinx.coroutines.flow.Flow

interface AppRuleRepository {
    fun getAllRules(): Flow<List<AppRule>>
    fun getMonitoredRules(): Flow<List<AppRule>>
    fun getRuleForPackage(packageName: String): Flow<AppRule?>
    suspend fun getRuleForPackageSync(packageName: String): AppRule?
    suspend fun upsertRule(rule: AppRule)
    suspend fun insertDefaultRulesIfEmpty()
    suspend fun deleteRule(packageName: String)
}
