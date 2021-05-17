package com.terrranullius.lockout

import android.app.Application
import com.terrranullius.lockout.other.setNotificationChannel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        setNotificationChannel(this)
    }
}