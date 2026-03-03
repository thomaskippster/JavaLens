# JavaLens - Proguard / R8 Rules

# Project Specific
-keep class com.javalens.app.data.** { *; }
-keep class com.javalens.app.domain.model.** { *; }
-keep class com.javalens.app.domain.ai.SnippetMetadata { *; }

# Room
-keep class androidx.room.RoomMasterTable
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations, AnnotationDefault
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    val factory;
}

# Koin
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}

# WorkManager
-keep class androidx.work.** { *; }

# Timber
-keep class timber.log.Timber { *; }

# AICore
-keep class com.google.ai.edge.aicore.** { *; }

# AndroidX Lifecycle
-keep class androidx.lifecycle.ViewModel { *; }
