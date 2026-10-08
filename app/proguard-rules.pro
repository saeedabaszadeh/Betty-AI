# Betty AI ProGuard/R8 rules

# Keep Kotlin serialization metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.bettyai.assistant.**$$serializer { *; }
-keepclassmembers class com.bettyai.assistant.** {
    *** Companion;
}
-keepclasseswithmembers class com.bettyai.assistant.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep model classes used by reflection-free serialization
-keep class com.bettyai.assistant.data.remote.dto.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Tink (used by androidx.security-crypto) references compile-only errorprone
# annotations that are not present on the runtime classpath.
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.crypto.tink.**

# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# Hilt / Dagger generated code is handled by its own consumer rules.
