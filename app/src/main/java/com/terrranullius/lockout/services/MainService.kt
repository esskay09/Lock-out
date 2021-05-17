package com.terrranullius.lockout.services

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.terrranullius.lockout.other.Constants.ACTION_FRAGMENT_STARTED
import com.terrranullius.lockout.other.Constants.ACTION_LOCKNOW
import com.terrranullius.lockout.other.Constants.ACTION_SCREEN_ON
import com.terrranullius.lockout.other.Constants.ACTION_START_SERVICE
import com.terrranullius.lockout.other.Constants.ACTION_STOP_SERVICE
import com.terrranullius.lockout.other.Constants.EXTRA_LOCK_OUT_SECONDS
import com.terrranullius.lockout.other.Constants.NOTIFICATION_ID
import com.terrranullius.lockout.other.Event
import com.terrranullius.lockout.other.sendNotification
import com.terrranullius.lockout.other.sendNotificationCompleted
import com.terrranullius.lockout.receivers.ScreenOnReceiver
import com.terrranullius.lockout.ui.MainActivity
import com.terrranullius.lockout.util.CountdownTimer
import com.terrranullius.lockout.util.FinishStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainService : Service() {

    companion object {
        val timeLeftInSeconds = MutableLiveData<Long>(0)
        val isTimerRunning = MutableLiveData<Boolean>(false)
        val isDialogShown = MutableLiveData<Event<Boolean>>(Event(false))
        val timeLeftMainFragDialog = MutableLiveData<Long>(0)
        var isTimerMainFragRunning = false
        val isLockOutOver = MutableLiveData<Event<FinishStatus>>(Event(FinishStatus.NONE))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var receiver: ScreenOnReceiver

    private lateinit var timer: CountdownTimer
    private lateinit var timerMainFrag: CountdownTimer

    private lateinit var devicePolicyManager: DevicePolicyManager

    private lateinit var lockJob: CompletableJob

    var lockoutTimeInSeconds = 0L

    override fun onCreate() {
        super.onCreate()
        setUpBroadcastReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_START_SERVICE -> startLockOut(intent)
            ACTION_SCREEN_ON -> onScreenOn()
            ACTION_STOP_SERVICE -> resetService(failed = true)
            ACTION_FRAGMENT_STARTED -> onFragmentStarted()
            ACTION_LOCKNOW -> lockNow()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun lockNow() {
        timerMainFrag.reset()
        resetTimerMainFrag()
        lockDevice(500L)
    }

    private fun onFragmentStarted() {
        if (isTimerRunning.value!!) {

            if (!isTimerMainFragRunning) {
                isTimerMainFragRunning = true
                startTimerMainFrag()
            }
            isDialogShown.postValue(Event(true))
        }
    }

    private fun startTimerMainFrag() {
        timerMainFrag = object : CountdownTimer(duration = 11000L, interval = 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMainFragDialog.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                resetTimerMainFrag()
                lockDevice(100L)
            }
        }

        timerMainFrag.start()
    }

    private fun resetTimerMainFrag() {
        isDialogShown.postValue(Event(false))
        timeLeftMainFragDialog.value = 0L
        isTimerMainFragRunning = false
    }

    private fun startLockOut(intent: Intent) {
        if (isTimerRunning.value == false) {
            lockJob = Job()
            startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
            lockoutTimeInSeconds = intent.getLongExtra(EXTRA_LOCK_OUT_SECONDS, -1)
            isTimerRunning.value = true
            lockDevice(10000L)
            startTimer()
            onFragmentStarted()
        }
    }

    private fun lockDevice(after: Long) {
        CoroutineScope(Dispatchers.Default + lockJob).launch {
            delay(after)
            withContext(Dispatchers.Main) {
                devicePolicyManager =
                    getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                devicePolicyManager.lockNow()
            }
        }
    }

    private fun onScreenOn() {
        if (isTimerRunning.value!! && !isTimerMainFragRunning) lockDevice(10000L)
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun startTimer() {

        timer = object : CountdownTimer(
            duration = TimeUnit.SECONDS.toMillis(lockoutTimeInSeconds),
            interval = 1000L
        ) {
            override fun onFinish() {
                sendNotificationCompleted("Yay you did it!")
                isLockOutOver.postValue(Event(FinishStatus.SUCCESS))
                resetService()
            }

            override fun onTick(millisUntilFinished: Long) {
                var millis = millisUntilFinished
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
                millis -= TimeUnit.MINUTES.toMillis(minutes)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)

                val notifContentTitle = "Remaining: ${if (minutes < 10) "0" else ""}$minutes:" +
                        "${if (seconds < 10) "0" else ""}$seconds"

                sendNotification(notifContentTitle)
                timeLeftInSeconds.postValue(millisUntilFinished / 1000)

            }
        }
        timer.start()
    }

    private fun resetService(failed: Boolean = false) {
        if (isTimerRunning.value!!) {

            if (failed) {
                isLockOutOver.postValue(Event(FinishStatus.FAILED))
            }

            timeLeftInSeconds.value = 0
            isTimerRunning.value = false
            isDialogShown.value = Event(false)
            timeLeftMainFragDialog.value = 0L
            isTimerMainFragRunning = false
            timerMainFrag.reset()
            timer.reset()
            lockJob.cancel()
            stopForeground(true)
            stopSelf()
        }
    }

    private fun setUpBroadcastReceiver() {
        val intentFilters = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(receiver, intentFilters)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}