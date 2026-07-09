package com.superlauncher.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

object NotificationBadges {
    private const val PREFS = "super_notification_badges"
    private const val KEY_COUNTS = "badge_counts"

    fun getBadgeCount(prefs: SharedPreferences, pkg: String): Int {
        val raw = prefs.getString(KEY_COUNTS, null) ?: return 0
        return try { JSONObject(raw).optInt(pkg, 0) } catch (_: Throwable) { 0 }
    }

    fun getAllBadges(prefs: SharedPreferences): Map<String, Int> {
        val raw = prefs.getString(KEY_COUNTS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, Int>()
            obj.keys().forEach { map[it] = obj.optInt(it, 0) }
            map
        } catch (_: Throwable) { emptyMap() }
    }
}