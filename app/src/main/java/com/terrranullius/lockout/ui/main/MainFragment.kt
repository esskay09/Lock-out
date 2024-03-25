package com.terrranullius.lockout.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.terrranullius.lockout.R
import com.terrranullius.lockout.other.Constants.ACTION_FRAGMENT_STARTED
import com.terrranullius.lockout.other.Constants.ACTION_LOCKNOW
import com.terrranullius.lockout.other.Constants.ACTION_START_SERVICE
import com.terrranullius.lockout.other.Constants.ACTION_STOP_SERVICE
import com.terrranullius.lockout.other.Constants.EXTRA_LOCK_OUT_SECONDS
import com.terrranullius.lockout.other.EventObserver
import com.terrranullius.lockout.services.MainService
import com.terrranullius.lockout.ui.util.PopupItem
import com.terrranullius.lockout.ui.util.showPopup
import com.terrranullius.lockout.util.FinishStatus
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment) {

    private lateinit var timerDialogLayout: View
    private lateinit var timerDialog: AlertDialog

    private lateinit var finishDialogLayout: View
    private lateinit var finishDialog: AlertDialog

    private lateinit var lockImageView: ImageView

    private lateinit var vibrator: Vibrator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi(view)
        setUpDialogs()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    }

    override fun onStart() {
        super.onStart()
        sendCommandToService(ACTION_FRAGMENT_STARTED)
        setUpObservers()

    }

    override fun onStop() {
        super.onStop()
        dismissAllTimerDialogs()
    }


    private fun sendCommandToService(action: String, num_minutes_string: String = "") {
        Intent(context, MainService::class.java).apply {
            this.action = action
            if (action == ACTION_START_SERVICE && num_minutes_string.isNotBlank()) {
                putExtra(
                    EXTRA_LOCK_OUT_SECONDS,
                    TimeUnit.MINUTES.toSeconds(num_minutes_string.toLong())
                )
                lockImageView.setImageResource(R.drawable.ic_lock)
            }
            requireActivity().startService(this)
        }
    }

    private fun setUpObservers() {
        MainService.isDialogShown.observe(viewLifecycleOwner, EventObserver { showDialog ->
            if (showDialog) {
                dismissAllTimerDialogs()
                timerDialog.show()
            } else {
                dismissAllTimerDialogs()
            }
        })

        MainService.timeLeftMainFragDialog.observe(
            viewLifecycleOwner,
            Observer { dialogTimeleft ->
                if (timerDialog.isShowing) {
                    timerDialogLayout.findViewById<TextView>(R.id.time_left_tv)
                        .text = dialogTimeleft.toString()
                }
            })

        MainService.isLockOutOver.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                FinishStatus.SUCCESS -> lockOutSuccess()
                FinishStatus.FAILED -> lockOutFailed()
                else -> Unit
            }
        })
    }

    private fun lockOutSuccess() {
        dismissAllTimerDialogs()
        showFinishDialog("YAY")
        lockImageView.setImageResource(R.drawable.ic_unlock)
        vibrate(3000L)
    }

    private fun lockOutFailed() {
        dismissAllTimerDialogs()
        showFinishDialog("Loser")
        lockImageView.setImageResource(R.drawable.ic_unlock)
    }

    private fun showFinishDialog(title: String) {
        finishDialogLayout.findViewById<TextView>(R.id.tv_finish_dialog).text = title
        finishDialog.show()
    }

    private fun dismissAllTimerDialogs() {
        while (timerDialog.isShowing) {
            timerDialog.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    private fun setUpDialogs() {
        timerDialogLayout = LayoutInflater.from(context).inflate(
            R.layout.main_timer_dialog, null
        )

        timerDialog = MaterialAlertDialogBuilder(requireContext()).setView(timerDialogLayout)
            .setPositiveButton("Loser") { _: DialogInterface, _: Int ->
                sendCommandToService(ACTION_STOP_SERVICE)
            }.setNegativeButton("Not a loser") { _: DialogInterface, _: Int ->
                sendCommandToService(ACTION_LOCKNOW)
            }.create()

        finishDialogLayout = LayoutInflater.from(context).inflate(
            R.layout.main_finish_dialog, null
        )

        finishDialog = MaterialAlertDialogBuilder(requireContext()).setView(finishDialogLayout)
            .setPositiveButton("Okay") { _: DialogInterface, _: Int ->

            }
            .create()
    }

    private fun setUpUi(view: View) {
        view.findViewById<Button>(R.id.btn_start_service).setOnClickListener {
            val minutesString = view.findViewById<EditText>(R.id.ti_num_minutes).text.toString()
            if (minutesString.isNotEmpty()) sendCommandToService(
                ACTION_START_SERVICE, minutesString
            )
        }
        view.findViewById<ImageView>(R.id.ivMore).setOnClickListener {
            it.showPopup(
                listOf(PopupItem("Privacy Policy") {
                    findNavController().navigate(R.id.action_mainFragment_to_privacyPolicyFragment)
                })
            )
        }
        lockImageView = view.findViewById(R.id.iv_lock_main)
    }

    private fun vibrate(duration: Long = 2000L) {

        vibrator.vibrate(
            VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

}