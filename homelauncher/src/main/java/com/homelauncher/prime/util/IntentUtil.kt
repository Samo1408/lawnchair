package com.homelauncher.prime.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.homelauncher.prime.admin.LockAdminReceiver

object IntentUtil {
    fun adminComponent(ctx: Context) = ComponentName(ctx, LockAdminReceiver::class.java)
    fun enableAdminIntent(ctx: Context): Intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply { putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent(ctx)); putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable screen lock from launcher") }
}
