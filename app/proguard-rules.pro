# Proguard rules for ScrollGuard

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Glance
-keep class androidx.glance.** { *; }

# WorkManager
-keep class androidx.work.** { *; }

# Hilt
-keep class com.google.dagger.** { *; }
-keep class dagger.** { *; }

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }

# Models
-keep class com.scrollguard.app.core.database.entity.** { *; }
-keep class com.scrollguard.app.domain.model.** { *; }
