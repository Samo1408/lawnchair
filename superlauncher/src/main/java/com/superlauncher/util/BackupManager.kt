package com.superlauncher.util

import android.content.Context
import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupManager {
    private const val KEY_DESKTOP_PAGES = "desktop_pages_json"
    private const val KEY_SHORTCUTS = "shortcut_ids"
    private const val KEY_FOLDERS = "folders_json"
    private const val KEY_WIDGETS = "widgets_csv"
    private const val PREFS_FILE = "super_desktop_layout"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    fun exportAll(ctx: Context, file: File) {
        val prefs = prefs(ctx)
        val root = JSONObject()
        val pagesRaw = prefs.getString(KEY_DESKTOP_PAGES, null)
        if (pagesRaw != null) root.put(KEY_DESKTOP_PAGES, JSONArray(pagesRaw))
        val shortcuts = prefs.getStringSet(KEY_SHORTCUTS, emptySet()) ?: emptySet()
        root.put(KEY_SHORTCUTS, JSONArray(shortcuts.toList()))
        val foldersRaw = prefs.getString(KEY_FOLDERS, null)
        if (foldersRaw != null) root.put(KEY_FOLDERS, JSONObject(foldersRaw))
        val widgets = prefs.getString(KEY_WIDGETS, "") ?: ""
        root.put(KEY_WIDGETS, widgets)
        file.writeText(root.toString(2))
    }

    fun importAll(ctx: Context, file: File) {
        val root = JSONObject(file.readText())
        val editor = prefs(ctx).edit()
        if (root.has(KEY_DESKTOP_PAGES)) {
            editor.putString(KEY_DESKTOP_PAGES, root.getJSONArray(KEY_DESKTOP_PAGES).toString())
        }
        if (root.has(KEY_SHORTCUTS)) {
            val arr = root.getJSONArray(KEY_SHORTCUTS)
            val ids = mutableSetOf<String>()
            for (i in 0 until arr.length()) ids += arr.getString(i)
            editor.putStringSet(KEY_SHORTCUTS, ids)
        }
        if (root.has(KEY_FOLDERS)) {
            editor.putString(KEY_FOLDERS, root.getJSONObject(KEY_FOLDERS).toString())
        }
        if (root.has(KEY_WIDGETS)) {
            editor.putString(KEY_WIDGETS, root.getString(KEY_WIDGETS))
        }
        editor.apply()
    }

    fun getBackupDir(): File {
        val dir = File(Environment.getExternalStorageDirectory(), "SuperLauncher")
        dir.mkdirs()
        return dir
    }

    fun resetAll(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }
}