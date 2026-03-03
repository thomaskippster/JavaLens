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
        
        // Initialize Sentry for error tracking
        SentryAndroid.init(this) { options ->
            // Der DSN wird normalerweise in der AndroidManifest.xml oder sentry.properties konfiguriert.
            // Er kann aber auch hier explizit gesetzt werden:
            // options.dsn = "DEIN_SENTRY_DSN_HIER"
            
            options.isEnableAutoSessionTracking = true
            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true
        }
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        startKoin {
            androidLogger()
            androidContext(this@JavaLensApplication)
            workManagerFactory()
            modules(appModule)
        }
    }
}
