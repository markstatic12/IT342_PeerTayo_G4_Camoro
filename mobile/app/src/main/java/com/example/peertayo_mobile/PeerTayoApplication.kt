package com.example.peertayo_mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for PeerTayo mobile app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class PeerTayoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize app-level components here if needed
    }
}
