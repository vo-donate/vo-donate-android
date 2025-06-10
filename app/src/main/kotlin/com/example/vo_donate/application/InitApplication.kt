package com.example.vo_donate.application

import android.app.Application
import android.content.Intent
import com.example.vo_donate.di.AppModule
import com.example.vo_donate.di.AppModuleImpl
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class InitApplication : Application() {
    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(
            appContext = applicationContext,
            onTokenExpired = {
                // Broadcast to all listeners that the token has expired
                val intent = Intent("TOKEN_EXPIRED")
                sendBroadcast(intent)
            },
            ioDispatcher = Dispatchers.IO
        )
        Timber.plant(Timber.DebugTree())
    }
}