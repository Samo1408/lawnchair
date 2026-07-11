/*
 * Home-Launcher Multi-User Integration for Lawnchair
 * 
 * Enhances Lawnchair's existing multi-user support with:
 * - JSON cache for fast app loading
 * - Quick refresh on package changes
 * - User-labeled app grouping
 */

package app.lawnchair.homelauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.annotation.WorkerThread
import com.android.launcher3.pm.UserCache
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Enhanced multi-user app helper that provides fast cached access to apps
 * from ALL user profiles, with JSON persistence for instant loading.
 * 
 * Usage: Call MultiUserAppsHelper.getInstance(context).getApps() from a coroutine
 * to get a cached list of all apps across profiles.
 */
class MultiUserAppsHelper private constructor(private val context: Context) {

    companion object {
        private const val CACHE_FILE = "hl_apps_cache.json"
        private const val CACHE_META = "hl_apps_cache_meta.json"
        private val instances = ConcurrentHashMap<String, MultiUserAppsHelper>()

        fun getInstance(context: Context): MultiUserAppsHelper {
            return instances.getOrPut(context.packageName) { MultiUserAppsHelper(context.applicationContext) }
        }
    }

    data class AppEntry(
        val packageName: String,
        val componentName: String,
        val label: String,
        val userSerial: Long,
        val isWork: Boolean,
        val userLabel: String,
    ) {
        val id: String get() = "$packageName/$componentName@$userSerial"
    }

    @Volatile private var cache: List<AppEntry>? = null
    @Volatile var isDirty: Boolean = true
        private set

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var receiverRegistered = false

    fun getCached(): List<AppEntry> = cache ?: emptyList()

    fun getCachedOrLoad(): List<AppEntry> {
        if (cache == null) {
            val file = File(context.filesDir, CACHE_FILE)
            if (file.exists()) {
                cache = deserialize(file)
                isDirty = false
            }
        }
        if (cache == null || isDirty) {
            scope.launch { loadAll() }
        }
        return cache ?: emptyList()
    }

    @WorkerThread
    fun loadAll(): List<AppEntry> {
        val launcher = context.getSystemService(LauncherApps::class.java) ?: return emptyList()
        val um = context.getSystemService(UserManager::class.java) ?: return emptyList()
        val myUser = Process.myUserHandle()
        val out = ArrayList<AppEntry>(256)

        for (user in um.userProfiles) {
            val isWork = user != myUser
            val serial = um.getSerialNumberForUser(user)
            val userLabel = if (isWork) "Work" else "Personal"

            for (info in launcher.getActivityList(null, user)) {
                out += AppEntry(
                    packageName = info.applicationInfo.packageName,
                    componentName = info.componentName.className,
                    label = info.label?.toString() ?: info.applicationInfo.packageName,
                    userSerial = serial,
                    isWork = isWork,
                    userLabel = userLabel,
                )
            }
        }
        out.sortBy { it.label.lowercase() }
        cache = out
        isDirty = false
        scope.launch { serialize(out) }
        return out
    }

    fun registerPackageListener(context: Context) {
        if (receiverRegistered) return
        receiverRegistered = true
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                invalidate()
            }
        }, filter)
    }

    fun invalidate() { isDirty = true; cache = null }

    private fun serialize(list: List<AppEntry>) {
        try {
            val arr = JSONArray()
            for (a in list) arr.put(JSONObject().apply {
                put("pkg", a.packageName); put("cls", a.componentName)
                put("label", a.label); put("serial", a.userSerial)
                put("work", a.isWork); put("ulabel", a.userLabel)
            })
            File(context.filesDir, CACHE_FILE).writeText(arr.toString())
            File(context.filesDir, CACHE_META).writeText(JSONObject().apply {
                put("count", list.size); put("time", System.currentTimeMillis())
            }.toString())
        } catch (_: Throwable) {}
    }

    private fun deserialize(file: File): List<AppEntry>? = try {
        val arr = JSONArray(file.readText())
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            AppEntry(
                packageName = obj.getString("pkg"), componentName = obj.getString("cls"),
                label = obj.getString("label"), userSerial = obj.getLong("serial"),
                isWork = obj.getBoolean("work"), userLabel = obj.getString("ulabel"),
            )
        }
    } catch (_: Throwable) { null }
}
