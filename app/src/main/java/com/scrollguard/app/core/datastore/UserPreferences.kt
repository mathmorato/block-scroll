package com.scrollguard.app.core.datastore

data class UserPreferences(
    val isGuardianActive: Boolean = true,
    val isAntiReelsActive: Boolean = true,
    val isAntiScrollActive: Boolean = true,
    val isAdultFilterActive: Boolean = true,
    val isStrictModeActive: Boolean = false,
    val scrollSessionLimitMinutes: Int = 10,
    val scrollDelaySeconds: Int = 5,
    val strictModeChallengeType: String = "TIMER", // TIMER, TYPING, PIN
    val strictModePin: String = "",
    val strictModeUntilTimestamp: Long = 0L,
    val isOnboardingCompleted: Boolean = false,
    val streakDays: Int = 1,
    val lastActiveDate: String = ""
)
