# ============================================
# WAW LAUNDRY - PROGUARD RULES
# ============================================

# === REMOVE LOGS ===
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# === KOTLIN/COROUTINES ===
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# === COMPOSE ===
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# === ROOM ===
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# === HILT ===
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

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

# === WORKMANAGER ===
-keep class androidx.work.** { *; }

# === PRESERVE LINE NUMBERS ===
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
