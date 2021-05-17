package com.terrranullius.lockout.util

import kotlinx.coroutines.*

abstract class CountdownTimer(
    private val duration: Long,
    private val interval: Long = 1000L,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private lateinit var timerJob: Job
    private var _isRunning = false
    private var timeLeft = duration
    private var isFirstRun = true

    val isRunning: Boolean
        get() = _isRunning

    abstract fun onTick(millisUntilFinished: Long)

    abstract fun onFinish()

    fun start() {

        //Timer started for the first time/ after reset
        if (isFirstRun) {
            timeLeft = duration
            isFirstRun = false
            timerJob = Job()
        }

        _isRunning = true
        if (_isRunning) {
            CoroutineScope(dispatcher + timerJob).launch {
                while (_isRunning) {

                    //Interval passed
                    timeLeft -= interval

                    withContext(Dispatchers.Main) { onTick(timeLeft) }

                    //Timer Finished
                    if (timeLeft <= 0L) {
                        withContext(Dispatchers.Main) { onFinish() }
                        reset()
                    }
                    delay(interval)
                }
            }
        }
    }

    fun reset() {
        isFirstRun = true
        _isRunning = false
        timeLeft = 0L
        timerJob.cancel()
    }

    fun pause(){
        _isRunning = false
    }

}