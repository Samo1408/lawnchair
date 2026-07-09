package com.superlauncher.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONObject

class SuperNotificationListener : NotificationListenerService() {

    companion object {
        const val PREFS = "super_notification_badges"
        const val KEY_COUNTS = "badge_counts"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        updateBadge(sbn.packageName, +1)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        updateBadge(sbn.packageName, -1)
    }

    private fun updateBadge(pkg: String, delta: Int) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val raw = prefs.getString(KEY_COUNTS, null)
        val obj = try { JSONObject(raw ?: "{}") } catch (_: Throwable) { JSONObject() }
        val cur = obj.optInt(pkg, 0)
        val next = (cur + delta).coerceAtLeast(0)
        if (next <= 0) obj.remove(pkg) else obj.put(pkg, next)
        prefs.edit().putString(KEY_COUNTS, obj.toString()).apply()
        sendBroadcast(Intent("com.superlauncher.NOTIFICATION_BADGE_CHANGED")
            .setPackage(packageName)
            .putExtra("pkg", pkg).putExtra("count", next))
    }

    override fun onListenerConnected() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE).edit()
        prefs.putString(KEY_COUNTS, JSONObject().toString()).apply()
        for (sbn in activeNotifications) {
            updateBadge(sbn.packageName, +1)
        }
    }
}
