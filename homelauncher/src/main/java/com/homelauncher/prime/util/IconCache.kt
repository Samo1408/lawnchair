package com.homelauncher.prime.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache
import android.widget.ImageView
import com.homelauncher.prime.data.AppItem
import java.util.concurrent.ConcurrentHashMap

object IconCache {
    private val cache = LruCache<String, Bitmap>(200)
    val badgeCounts = ConcurrentHashMap<String, Int>()
    fun load(ctx: Context, item: AppItem, target: ImageView) {
        val key = item.id; val cached = cache.get(key)
        if (cached != null) { target.setImageBitmap(cached); return }
        try {
            val icon: Drawable? = item.launcherInfo?.getIcon(0)
            val bmp = if (icon != null) drawableToBitmap(icon, 56f, ctx) else drawableToBitmap(ctx.packageManager.getApplicationIcon(item.packageName), 56f, ctx)
            cache.put(key, bmp); target.setImageBitmap(bmp)
        } catch (_: Throwable) {}
    }
    fun clear() { cache.evictAll() }
    private fun drawableToBitmap(d: Drawable, dp: Float, ctx: Context): Bitmap {
        val px = (dp * ctx.resources.displayMetrics.density).toInt()
        val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp); d.setBounds(0, 0, px, px); d.draw(canvas)
        return bmp
    }
}
