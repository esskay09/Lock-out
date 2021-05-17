package com.terrranullius.lockout.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.terrranullius.lockout.other.Constants.ACTION_SCREEN_ON
import com.terrranullius.lockout.services.MainService

class ScreenOnReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        context?.startService(Intent(context.applicationContext, MainService::class.java)
            .apply {
                action = ACTION_SCREEN_ON
            })
    }
}