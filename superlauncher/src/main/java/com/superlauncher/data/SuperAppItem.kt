package com.superlauncher.data

import android.content.pm.LauncherActivityInfo
import android.os.UserHandle

data class SuperAppItem(
    val packageName: String,
    val componentName: String,
    val label: String,
    var user: UserHandle? = null,
    val userSerial: Long = 0,
    val isWork: Boolean = false,
    val userLabel: String = "Personal",
    var launcherInfo: LauncherActivityInfo? = null
) {
    val id: String get() = "$packageName/$componentName@$userSerial"
}