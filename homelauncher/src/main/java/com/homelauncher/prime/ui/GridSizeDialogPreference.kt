package com.homelauncher.prime.ui

import android.content.Context
import android.util.AttributeSet
import android.content.SharedPreferences

class GridSizeDialogPreference(ctx: Context, attrs: AttributeSet) : androidx.preference.DialogPreference(ctx, attrs) {
    var cols: Int = 4
    var rows: Int = 5
    fun load(prefs: SharedPreferences) { cols = prefs.getInt("desktop_cols", 4); rows = prefs.getInt("desktop_rows", 5) }
    fun save(prefs: SharedPreferences.Editor) { prefs.putInt("desktop_cols", cols); prefs.putInt("desktop_rows", rows) }
    override fun getSummary(): CharSequence = "$cols x $rows"
}
