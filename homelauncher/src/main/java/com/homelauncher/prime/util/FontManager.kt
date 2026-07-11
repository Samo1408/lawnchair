package com.homelauncher.prime.util

import android.content.Context
import android.graphics.Typeface

object FontManager {
    var currentTypeface: Typeface = Typeface.DEFAULT
    fun getCurrentTypeface(ctx: Context): Typeface = currentTypeface
    fun setTypeface(tf: Typeface) { currentTypeface = tf }
}
