package com.superlauncher.util

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object RootUtil {
    fun runAsRoot(vararg cmds: String): Boolean = try {
        val p = Runtime.getRuntime().exec("su")
        DataOutputStream(p.outputStream).use { os ->
            cmds.forEach { os.writeBytes("$it\n") }
            os.writeBytes("exit\n")
            os.flush()
        }
        p.waitFor() == 0
    } catch (t: Throwable) {
        Log.e("RootUtil", "su failed", t); false
    }

    fun runAsRootCapture(cmd: String): String? = try {
        val p = Runtime.getRuntime().exec("su")
        DataOutputStream(p.outputStream).use { os ->
            os.writeBytes("$cmd\n")
            os.writeBytes("exit\n")
            os.flush()
        }
        val out = BufferedReader(InputStreamReader(p.inputStream)).use { it.readText() }
        if (p.waitFor() == 0) out else null
    } catch (t: Throwable) {
        Log.e("RootUtil", "su capture failed", t); null
    }

    fun listUsers(): List<Pair<Int, String>> {
        val out = runAsRootCapture("pm list users") ?: return emptyList()
        val regex = Regex("""UserInfo\{(\\d+):([^:}]*):""")
        return regex.findAll(out).map { m ->
            m.groupValues[1].toInt() to m.groupValues[2].trim().ifEmpty { "User ${m.groupValues[1]}" }
        }.toList()
    }

    fun lockScreen(): Boolean = runAsRoot("input keyevent 26")

    fun isRootAvailable(): Boolean = runAsRootCapture("id")?.contains("uid=0") == true
}