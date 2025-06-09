package dev.kevin.core.network.di

import android.content.Context
import dev.kevin.core.network.api.NetworkApi
import dev.kevin.core.network.jwt.JwtInterceptor
import dev.kevin.core.preferences.TokenManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

class NetworkModule(context: Context, private val onTokenExpired: () -> Unit) {
    private val baseUrl = "https://api.jetpack.io"

    private val tokenManager = TokenManager(context)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(JwtInterceptor(tokenManager, onTokenExpired))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Singleton
    fun provideNetworkApi(): NetworkApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(Json.Default.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NetworkApi::class.java)
    }

    fun provideTokenManager(): TokenManager = tokenManager
}