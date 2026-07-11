/*
 * QuickLockHelper - Fast screen lock integration for Lawnchair
 * 
 * Wraps the existing SleepGestureHandler lock methods (Root, DeviceAdmin, Accessibility)
 * into a convenient helper that can be used from anywhere (shortcuts, app drawer, etc.).
 */

package app.lawnchair.homelauncher

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import app.lawnchair.LawnchairLauncher
import app.lawnchair.lawnchairApp
import app.lawnchair.util.requireSystemService
import com.android.launcher3.R
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * One-call screen lock helper that tries the best available method.
 * Priority: Root > Accessibility > DeviceAdmin
 */
object QuickLockHelper {

    suspend fun tryLockSilent(context: Context): Boolean = withContext(Dispatchers.IO) {
        if (Shell.getShell().isRoot) {
            val result = Shell.su("input keyevent 26").exec()
            if (result.isSuccess) return@withContext true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val app = context.lawnchairApp
            if (app.isAccessibilityServiceBound()) {
                return@withContext app.performGlobalAction(
                    AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
                )
            }
        }
        val dpm: DevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, QuickLockDeviceAdmin::class.java)
        if (dpm.isAdminActive(adminComponent)) {
            dpm.lockNow()
            return@withContext true
        }
        false
    }

    suspend fun lockWithFeedback(context: Context) {
        if (!tryLockSilent(context)) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lock failed - enable Device Admin or root", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class QuickLockDeviceAdmin : android.app.admin.DeviceAdminReceiver()
