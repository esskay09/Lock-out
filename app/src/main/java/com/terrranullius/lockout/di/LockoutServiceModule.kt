package com.terrranullius.lockout.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import com.terrranullius.lockout.R
import com.terrranullius.lockout.other.Constants.ACTION_CANCEL_LOCKOUT
import com.terrranullius.lockout.other.Constants.ACTION_LOCKNOW
import com.terrranullius.lockout.other.Constants.NOTIFICATION_CHANNEL_ID
import com.terrranullius.lockout.other.Constants.RC_CANCEL_LOCKOUT_PENDING_INTENT
import com.terrranullius.lockout.other.Constants.RC_LOCKOUT_PENDING_INTENT
import com.terrranullius.lockout.receivers.ScreenOnReceiver
import com.terrranullius.lockout.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object LockoutServiceModule {

    @Provides
    @ServiceScoped
    fun providesBaseNotificationBuilder(@ApplicationContext context: Context): NotificationCompat.Builder =
        NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Remaining")
            .setSmallIcon(R.drawable.ic_baseline_lock_24)
            .setPriority(PRIORITY_HIGH)
            .setContentIntent(addContentIntent(context))
            .addAction(
                R.drawable.ic_baseline_lock_24,
                "Easy Peasy",
                addLockoutPendingIntent(context)
            )
            .addAction(
                R.drawable.ic_baseline_stop_24,
                "I lose",
                addCancelPendingIntent(context)
            )

    private fun addContentIntent(context: Context) = PendingIntent.getActivity(
        context, 456,
        Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    private fun addLockoutPendingIntent(context: Context) =
        PendingIntent.getActivity(context, RC_LOCKOUT_PENDING_INTENT,
            Intent(context, MainActivity::class.java).apply {
                action = ACTION_LOCKNOW
            }
            , PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun addCancelPendingIntent(context: Context) =
        PendingIntent.getActivity(
            context, RC_CANCEL_LOCKOUT_PENDING_INTENT,
            Intent(context, MainActivity::class.java).apply {
                action = ACTION_CANCEL_LOCKOUT
            }, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    @Provides
    @ServiceScoped
    fun provideReceiver() = ScreenOnReceiver()
}