package com.javalens.app.di

import androidx.room.Room
import com.javalens.app.data.AppDatabase
import com.javalens.app.domain.ai.LocalAiService
import com.javalens.app.domain.repository.SnippetRepository
import com.javalens.app.viewmodel.ScannerViewModel
import com.javalens.app.viewmodel.VideoImportViewModel
import com.javalens.app.domain.video.VideoCodeExtractor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java, "javalens-db"
        ).build()
    }
    
    // DAO
    single { get<AppDatabase>().snippetDao() }
    
    // Services
    single { LocalAiService(androidContext()) }
    single { VideoCodeExtractor(androidContext()) }
    
    // Repository
    single { SnippetRepository(get(), get()) }
    
    // ViewModels
    viewModel { ScannerViewModel(get()) }
    viewModel { VideoImportViewModel(get()) }
}
