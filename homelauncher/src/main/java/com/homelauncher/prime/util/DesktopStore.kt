package com.homelauncher.prime.util

import android.content.Context

object DesktopStore {
    private const val PREF = "desktop"
    fun getShortcutIds(ctx: Context): Set<String> = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getStringSet("shortcuts", emptySet()) ?: emptySet()
    fun addShortcut(ctx: Context, id: String) { val e = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit(); val ids = getShortcutIds(ctx).toMutableSet(); ids += id; e.putStringSet("shortcuts", ids); e.apply() }
    fun removeShortcut(ctx: Context, id: String) { val e = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit(); val ids = getShortcutIds(ctx).toMutableSet(); ids -= id; e.putStringSet("shortcuts", ids); e.apply() }
}
