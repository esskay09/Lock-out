package com.terrranullius.lockout.other

object Constants {

    const val NOTIFICATION_CHANNEL_ID = "lockout_notif"
    const val NOTIFICATION_ID = 101

    const val ACTION_START_SERVICE = "start_lock_out"
    const val ACTION_SCREEN_ON = "screen_on"
    const val ACTION_STOP_SERVICE ="stop"

    const val EXTRA_LOCK_OUT_SECONDS = "millis_lock_out"

    const val RC_CANCEL_LOCKOUT_PENDING_INTENT = 132
    const val ACTION_CANCEL_LOCKOUT = "cancel_lockout"

    const val RC_LOCKOUT_PENDING_INTENT = 111
    const val ACTION_LOCKNOW = "lockout"

    const val ACTION_FRAGMENT_STARTED = "ress"

    const val PRIVACY_POLICY_URL = "https://ultimatecreations-production.up.railway.app/lockout-privacy.html"
}