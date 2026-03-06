package com.javalens.app.di

import androidx.room.Room
import com.javalens.app.data.AppDatabase
import com.javalens.app.domain.ai.LocalAiService
import com.javalens.app.domain.repository.SnippetRepository
import com.javalens.app.viewmodel.ScannerViewModel
import com.javalens.app.viewmodel.VideoImportViewModel
import com.javalens.app.viewmodel.ProjectChatViewModel
import com.javalens.app.viewmodel.VaultViewModel
import com.javalens.app.domain.video.VideoCodeExtractor
import com.javalens.app.domain.export.GitHubApi
import com.javalens.app.domain.export.GitHubExporter
import com.javalens.app.domain.export.GitHubSyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    
    // GitHub Logic Setup
    single {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApi::class.java)
    }
    single { GitHubExporter(androidContext(), get()) }
    
    // Repository
    single { SnippetRepository(get(), get()) }
    
    // ViewModels
    viewModel { ScannerViewModel(get(), get()) }
    viewModel { VideoImportViewModel(get()) }
    viewModel { ProjectChatViewModel(get()) }
    viewModel { VaultViewModel(get()) }
    
    // Workers
    worker { GitHubSyncWorker(get(), get(), get(), get()) }
}
