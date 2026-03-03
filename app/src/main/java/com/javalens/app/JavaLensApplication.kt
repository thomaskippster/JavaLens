package com.javalens.app

import android.app.Application
import com.javalens.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class JavaLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@JavaLensApplication)
            modules(appModule)
        }
    }
}
