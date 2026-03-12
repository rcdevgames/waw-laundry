# ============================================
# WAW LAUNDRY - AGGRESSIVE PROGUARD RULES
# ============================================

# === AGGRESSIVE OPTIMIZATION ===
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-mergeinterfacesaggressively

# === REMOVE DEBUG LOGS ===
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkParameterIsNotNull(...);
    public static void checkNotNull(...);
    public static void checkNotNullParameter(...);
    public static void checkExpressionValueIsNotNull(...);
}

# === KOTLIN COROUTINES ===
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# === COMPOSE ===
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# === ROOM DATABASE ===
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# === HILT ===
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }
-dontwarn dagger.**

# === SQLCIPHER ===
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# === SERIALIZATION ===
-keepattributes Signature
-keepclassmembers,allowobfuscation class * {
    @kotlinx.serialization.SerialName <fields>;
}

# === BLUETOOTH ===
-keep class android.bluetooth.** { *; }

# === FILE PROVIDER ===
-keep class androidx.core.content.FileProvider { *; }

# === WORKMANAGER ===
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# === DATASTORE ===
-keep class androidx.datastore.** { *; }

# === SECURITY CRYPTO ===
-keep class androidx.security.** { *; }

# === AGGRESSIVE UNUSED CODE REMOVAL ===
# Remove unused Supabase/Ktor code aggressively
-dontwarn io.github.jan.supabase.**
-dontwarn io.ktor.**
-dontwarn io.ktor.client.**
-dontwarn kotlin.io.**
-dontwarn kotlinx.io.**

# Keep only used Supabase classes
-keep class io.github.jan.supabase.** { *; }

# === REMOVE UNUSED RESOURCES ===
# (handled by isShrinkResources = true)

# === PRESERVE LINE NUMBERS ===
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
