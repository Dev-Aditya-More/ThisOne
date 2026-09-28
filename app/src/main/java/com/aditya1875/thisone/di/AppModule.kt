// ─────────────────────────────────────────────
// di/AppModule.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.di

import androidx.room.Room
import com.aditya1875.thisone.BuildConfig
import com.aditya1875.thisone.data.local.MemeDatabase
import com.aditya1875.thisone.data.remote.GeminiApi
import com.aditya1875.thisone.data.remote.ImgflipApi
import com.aditya1875.thisone.data.repository.MemeRepository
import com.aditya1875.thisone.data.repository.SelectedMemeStore
import com.aditya1875.thisone.ui.detail.MemeDetailViewModel
import com.aditya1875.thisone.ui.home.HomeViewModel
import com.aditya1875.thisone.ui.saved.SavedViewModel
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    // ── Gson ───────────────────────────────────────────────────────────────
    single { Gson() }

    // ── OkHttp ─────────────────────────────────────────────────────────────
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // OkHttp for Imgflip (no auth)
    single(qualifier = org.koin.core.qualifier.named("imgflip")) {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    // OkHttp for Gemini (adds API key header)
    single(qualifier = org.koin.core.qualifier.named("gemini")) {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
                .build()
            chain.proceed(request)
        }
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    // ── Retrofit ───────────────────────────────────────────────────────────
    single<ImgflipApi> {
        Retrofit.Builder()
            .baseUrl("https://api.imgflip.com/")
            .client(get(qualifier = org.koin.core.qualifier.named("imgflip")))
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
            .create(ImgflipApi::class.java)
    }

    single<GeminiApi> {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(get(qualifier = org.koin.core.qualifier.named("gemini")))
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
            .create(GeminiApi::class.java)
    }

    // ── Room ───────────────────────────────────────────────────────────────
    single {
        Room.databaseBuilder(
            androidContext(),
            MemeDatabase::class.java,
            "thisone_db",
        )
            // MVP: no migrations written yet, safe to wipe local cache on schema bumps.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single { get<MemeDatabase>().savedMemeDao() }

    // ── Repository ─────────────────────────────────────────────────────────
    single {
        MemeRepository(
            imgflipApi = get(),
            geminiApi = get(),
            savedMemeDao = get(),
            gson = get(),
        )
    }

    // ── Cross-screen state ────────────────────────────────────────────────
    single { SelectedMemeStore() }

    // ── ViewModels ─────────────────────────────────────────────────────────
    viewModel { HomeViewModel(repository = get(), selectedMemeStore = get()) }
    viewModel { SavedViewModel(repository = get(), selectedMemeStore = get()) }
    viewModel { MemeDetailViewModel(repository = get(), selectedMemeStore = get()) }
}