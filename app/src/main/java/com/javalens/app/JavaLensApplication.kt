package com.javalens.app

import android.app.Application
import com.javalens.app.di.appModule
import io.sentry.android.core.SentryAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import timber.log.Timber

class JavaLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Koin FIRST to ensure WorkManager is initialized by Koin
        startKoin {
            androidLogger()
            androidContext(this@JavaLensApplication)
            workManagerFactory()
            modules(appModule)
        }
        
        // Initialize Sentry for error tracking
        SentryAndroid.init(this) { options ->
            options.isEnableAutoSessionTracking = true
            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true
        }
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
