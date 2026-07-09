package com.superlauncher.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.os.UserHandle
import android.provider.Settings
import android.widget.Toast

object MultiUserUtil {

    fun userIdOf(user: UserHandle?): Int {
        val uh = user ?: Process.myUserHandle()
        return try {
            UserHandle::class.java.getMethod("getIdentifier").invoke(uh) as Int
        } catch (_: Throwable) {
            val s = uh.toString()
            Regex("""\{(\d+)\}""").find(s)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        }
    }

    private fun myUserId(): Int = userIdOf(Process.myUserHandle())

    fun openAppInfo(ctx: Context, pkg: String, user: UserHandle? = null) {
        val uid = userIdOf(user)
        if (uid != myUserId()) {
            val ok = RootUtil.runAsRoot(
                "am start --user $uid -a android.settings.APPLICATION_DETAILS_SETTINGS -d package:$pkg"
            )
            if (ok) return
            Toast.makeText(ctx, "تعذر فتح معلومات التطبيق للمستخدم $uid (يتطلب root)", Toast.LENGTH_LONG).show()
            return
        }
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$pkg")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(i)
    }

    /** Install an existing app to another user, marking it as installed by Play Store */
    fun installToOtherUser(ctx: Context, pkg: String, userId: Int): Boolean {
        val pathOut = RootUtil.runAsRootCapture("pm path $pkg")
        val apkLine = pathOut?.lineSequence()
            ?.map { it.trim() }
            ?.firstOrNull { it.startsWith("package:") }
        val apkPath = apkLine?.removePrefix("package:")?.trim()
        return if (!apkPath.isNullOrEmpty()) {
            RootUtil.runAsRoot("pm install -r --user $userId -i com.android.vending \"$apkPath\"")
        } else {
            RootUtil.runAsRoot("pm install-existing --user $userId $pkg")
        }
    }

    fun uninstallForUser(ctx: Context, pkg: String, user: UserHandle? = null) {
        val uid = userIdOf(user)
        if (uid != myUserId()) {
            val ok = RootUtil.runAsRoot("pm uninstall --user $uid $pkg")
            val msg = if (ok) "تم إلغاء تثبيت $pkg للمستخدم $uid"
                      else "فشل إلغاء التثبيت للمستخدم $uid (يتطلب root)"
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
            return
        }
        val i = Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    }

    fun showInstallToUserDialog(ctx: Context, packages: List<String>) {
        if (packages.isEmpty()) return
        val users = RootUtil.listUsers()
        if (users.isEmpty()) {
            android.app.AlertDialog.Builder(ctx)
                .setTitle("تثبيت لمستخدم آخر")
                .setMessage("تعذر قراءة قائمة المستخدمين. تأكد من وجود صلاحية الروت (su).")
                .setPositiveButton(android.R.string.ok, null).show()
            return
        }
        android.app.AlertDialog.Builder(ctx)
            .setTitle("تثبيت لمستخدم آخر")
            .setItems(users.map { "${it.second}  (id=${it.first})" }.toTypedArray()) { _, which ->
                val userId = users[which].first
                val results = packages.map { pkg -> installToOtherUser(ctx, pkg, userId) }
                val ok = results.all { it }
                android.app.AlertDialog.Builder(ctx)
                    .setTitle("تثبيت لمستخدم آخر")
                    .setMessage(if (ok) "تم تثبيت ${packages.size} تطبيق للمستخدم ${users[which].second}"
                                else "فشل تثبيت بعض التطبيقات (يتطلب root)")
                    .setPositiveButton(android.R.string.ok, null).show()
            }
            .setNegativeButton(android.R.string.cancel, null).show()
    }
}
