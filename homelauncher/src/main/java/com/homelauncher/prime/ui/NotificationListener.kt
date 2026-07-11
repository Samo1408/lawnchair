package com.homelauncher.prime.ui

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.util.Log
import com.homelauncher.prime.util.IconCache

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn != null) { val c = activeNotifications.count { it.packageName == sbn.packageName }; broadcastBadge(sbn.packageName, c); IconCache.badgeCounts[sbn.packageName] = c }
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn != null) { val c = activeNotifications.count { it.packageName == sbn.packageName }; broadcastBadge(sbn.packageName, c); IconCache.badgeCounts[sbn.packageName] = c }
    }
    private fun broadcastBadge(pkg: String, count: Int) {
        try { val i = Intent("com.homelauncher.UPDATE_BADGE"); i.putExtra("packageName", pkg); i.putExtra("count", count); androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(i) } catch (_: Throwable) { Log.e("NotifListener", "broadcast failed", _) }
    }
}
