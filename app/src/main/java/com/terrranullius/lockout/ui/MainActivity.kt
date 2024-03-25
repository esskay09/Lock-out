package com.terrranullius.lockout.ui

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.terrranullius.lockout.R
import com.terrranullius.lockout.other.Constants
import com.terrranullius.lockout.receivers.AdminAccessReceiver
import com.terrranullius.lockout.services.MainService
import com.terrranullius.lockout.ui.main.MainFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var adminName: ComponentName
    private lateinit var devicePolicyManager: DevicePolicyManager
    private val permissionRequester =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        checkNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        requestAdminAccess()
    }

    private fun requestAdminAccess() {
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminName = ComponentName(this, AdminAccessReceiver::class.java)
        if (!devicePolicyManager.isAdminActive(adminName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName)
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.also {
            when (it.action) {
                Constants.ACTION_CANCEL_LOCKOUT -> startService(
                    Intent(this, MainService::class.java)
                        .apply { this.action = Constants.ACTION_STOP_SERVICE }
                )

                Constants.ACTION_LOCKNOW -> startService(
                    Intent(this, MainService::class.java)
                        .apply {
                            this.action = Constants.ACTION_LOCKNOW
                        }
                )
            }
        }
    }

    private fun checkNotificationPermission() {
        val isSDK33OrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        if (!isSDK33OrHigher) return

        val hasPermission =
            checkCallingOrSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) return

        // Request permission
        permissionRequester.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}