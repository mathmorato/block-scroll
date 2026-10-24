package com.scrollguard.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val GUARDIAN_ACTIVE = booleanPreferencesKey("is_guardian_active")
        val ANTI_REELS_ACTIVE = booleanPreferencesKey("is_anti_reels_active")
        val ANTI_SCROLL_ACTIVE = booleanPreferencesKey("is_anti_scroll_active")
        val ADULT_FILTER_ACTIVE = booleanPreferencesKey("is_adult_filter_active")
        val STRICT_MODE_ACTIVE = booleanPreferencesKey("is_strict_mode_active")
        val SCROLL_LIMIT_MINUTES = intPreferencesKey("scroll_limit_minutes")
        val SCROLL_DELAY_SECONDS = intPreferencesKey("scroll_delay_seconds")
        val STRICT_MODE_CHALLENGE_TYPE = stringPreferencesKey("strict_mode_challenge_type")
        val STRICT_MODE_PIN = stringPreferencesKey("strict_mode_pin")
        val STRICT_MODE_UNTIL_TIMESTAMP = longPreferencesKey("strict_mode_until_timestamp")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                isGuardianActive = preferences[PreferencesKeys.GUARDIAN_ACTIVE] ?: true,
                isAntiReelsActive = preferences[PreferencesKeys.ANTI_REELS_ACTIVE] ?: true,
                isAntiScrollActive = preferences[PreferencesKeys.ANTI_SCROLL_ACTIVE] ?: true,
                isAdultFilterActive = preferences[PreferencesKeys.ADULT_FILTER_ACTIVE] ?: true,
                isStrictModeActive = preferences[PreferencesKeys.STRICT_MODE_ACTIVE] ?: false,
                scrollSessionLimitMinutes = preferences[PreferencesKeys.SCROLL_LIMIT_MINUTES] ?: 10,
                scrollDelaySeconds = preferences[PreferencesKeys.SCROLL_DELAY_SECONDS] ?: 5,
                strictModeChallengeType = preferences[PreferencesKeys.STRICT_MODE_CHALLENGE_TYPE] ?: "TIMER",
                strictModePin = preferences[PreferencesKeys.STRICT_MODE_PIN] ?: "",
                strictModeUntilTimestamp = preferences[PreferencesKeys.STRICT_MODE_UNTIL_TIMESTAMP] ?: 0L,
                isOnboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
                streakDays = preferences[PreferencesKeys.STREAK_DAYS] ?: 1,
                lastActiveDate = preferences[PreferencesKeys.LAST_ACTIVE_DATE] ?: ""
            )
        }

    suspend fun setGuardianActive(active: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GUARDIAN_ACTIVE] = active
        }
    }

    suspend fun setAntiReelsActive(active: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANTI_REELS_ACTIVE] = active
        }
    }

    suspend fun setAntiScrollActive(active: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANTI_SCROLL_ACTIVE] = active
        }
    }

    suspend fun setAdultFilterActive(active: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ADULT_FILTER_ACTIVE] = active
        }
    }

    suspend fun setStrictModeActive(active: Boolean, untilTimestamp: Long = 0L) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STRICT_MODE_ACTIVE] = active
            preferences[PreferencesKeys.STRICT_MODE_UNTIL_TIMESTAMP] = untilTimestamp
        }
    }

    suspend fun setScrollLimitMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCROLL_LIMIT_MINUTES] = minutes
        }
    }

    suspend fun setScrollDelaySeconds(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCROLL_DELAY_SECONDS] = seconds
        }
    }

    suspend fun setStrictModeChallenge(challengeType: String, pin: String = "") {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STRICT_MODE_CHALLENGE_TYPE] = challengeType
            preferences[PreferencesKeys.STRICT_MODE_PIN] = pin
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateStreak(streakDays: Int, lastActiveDate: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STREAK_DAYS] = streakDays
            preferences[PreferencesKeys.LAST_ACTIVE_DATE] = lastActiveDate
        }
    }
}
