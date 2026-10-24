package com.scrollguard.app.domain.usecase

import com.scrollguard.app.core.datastore.UserPreferencesRepository
import com.scrollguard.app.core.util.DomainTrie
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckUrlSafetyUseCase @Inject constructor(
    private val domainTrie: DomainTrie,
    private val preferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(urlOrText: String): Boolean {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        if (!prefs.isGuardianActive || !prefs.isAdultFilterActive) {
            return false // Safe/not blocked because filter is disabled
        }
        return domainTrie.isRestricted(urlOrText)
    }
}
