# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Retrofit
-keep class retrofit2.** { *; }
-keepattributes Exceptions

# Gson
-keep class com.google.gson.** { *; }
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep model classes
-keep class com.emishield.locker.model.** { *; }

# WorkManager
-keep class androidx.work.** { *; }

# Keep Device Admin related classes
-keep class com.emishield.locker.admin.** { *; }

# Prevent obfuscation
-keepnames class com.emishield.locker.** { *; }
