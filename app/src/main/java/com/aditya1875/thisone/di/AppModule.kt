// ─────────────────────────────────────────────
// di/AppModule.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.di

import androidx.room.Room
import com.aditya1875.thisone.BuildConfig
import com.aditya1875.thisone.data.local.MemeDatabase
import com.aditya1875.thisone.data.remote.AnthropicApi
import com.aditya1875.thisone.data.remote.ImgflipApi
import com.aditya1875.thisone.data.repository.MemeRepository
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

    // OkHttp for Anthropic (adds API key header)
    single(qualifier = org.koin.core.qualifier.named("anthropic")) {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
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

    single<AnthropicApi> {
        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(get(qualifier = org.koin.core.qualifier.named("anthropic")))
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
            .create(AnthropicApi::class.java)
    }

    // ── Room ───────────────────────────────────────────────────────────────
    single {
        Room.databaseBuilder(
            androidContext(),
            MemeDatabase::class.java,
            "thisone_db",
        ).build()
    }

    single { get<MemeDatabase>().savedMemeDao() }

    // ── Repository ─────────────────────────────────────────────────────────
    single {
        MemeRepository(
            imgflipApi = get(),
            anthropicApi = get(),
            savedMemeDao = get(),
            gson = get(),
        )
    }

    // ── ViewModels ─────────────────────────────────────────────────────────
    viewModel { HomeViewModel(get())}
    viewModel { SavedViewModel(repository = get()) }
}