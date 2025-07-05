package com.saubh.strategysquares

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StrategySquaresApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase initialization is handled by FirebaseModule
    }
}