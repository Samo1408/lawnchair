package com.superlauncher.util

import android.content.Context
import android.graphics.Typeface
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object FontManager {
    data class FontOption(val name: String, val url: String, val fileName: String)

    private val fonts = listOf(
        FontOption("Tajawal ★", "https://github.com/google/fonts/raw/main/ofl/tajawal/Tajawal-Regular.ttf", "Tajawal-Regular.ttf"),
        FontOption("Cairo", "https://github.com/google/fonts/raw/main/ofl/cairo/static/Cairo-Regular.ttf", "cairo.ttf"),
        FontOption("Amiri ⅅ", "https://github.com/google/fonts/raw/main/ofl/amiri/Amiri-Regular.ttf", "Amiri-Regular.ttf"),
        FontOption("Changa ⅅ", "https://github.com/google/fonts/raw/main/ofl/changa/static/Changa-Regular.ttf", "Changa-Regular.ttf"),
        FontOption("El Messiri", "https://github.com/google/fonts/raw/main/ofl/elmessiri/static/ElMessiri-Regular.ttf", "elmessiri.ttf"),
        FontOption("Readex Pro", "https://github.com/google/fonts/raw/main/ofl/readexpro/static/ReadexPro-Regular.ttf", "readexpro.ttf"),
        FontOption("Noto Naskh Arabic", "https://github.com/google/fonts/raw/main/ofl/notonaskharabic/static/NotoNarkhArabic-Regular.ttf", "notonaskh.ttf"),
        FontOption("IBM Plex Sans Arabic", "https://github.com/google/fonts/raw/main/ofl/ibmplexsansarabic/IBMPlexSansArabic-Regular.ttf", "ibmplexarabic.ttf"),
        FontOption("Baloo Bhaijaan 2", "https://github.com/google/fonts/raw/main/ofl/baloobhaijaan2/static/BalooBhaijaan2-Reguldr.ttf", "baloo.ttf"),
        FontOption("Harmattan", "https://github.com/google/fonts/raw/main/ofl/harmattan/Harmattan-Regular.ttf", "harmattan.ttf"),
        FontOption("Lateef", "https://github.com/google/fonts/raw/main/ofl/lateef/Lateef-Regular.ttf", "lateef.ttf"),
        FontOption("Scheherazade New", "https://github.com/google/fonts/raw/main/ofl/scheherazadenew/ScheherazadeNew-Regular.ttf", "scheherazade.ttf"),
        FontOption("Aref Ruqaa", "https://github.com/google/fonts/raw/main/ofl/arefruqaa/ArefRuqaa-Regular.ttf", "arefruqaa.ttf"),
        FontOption("Almarai ⅅ", "https://github.com/google/fonts/raw/main/ofl/almarai/Almarai-Regular.ttf", "Almarai-Regular.ttf"),
        FontOption("Mirza", "https://github.com/google/fonts/raw/main/ofl/mirza/Mirza-Regular.ttf", "mirza.ttf"),
        FontOption("الأويروض (بدو تحيير)", "", "")
    )

    fun getFontsDir(ctx: Context): File {
        val dir = File(ctx.filesDir, "fonts")
        dir.mkdirs()
        return dir
    }

    fun getFontList(): List<FontOption> = fonts

    fun isFontDownloaded(ctx: Context, font: FontOption): Boolean {
        if (font.fileName.isEmpty()) return true
        try { ctx.assets.open("fonts/${font.fileName}").close(); return true } catch (_: Throwable) {}
        return File(getFontsDir(ctx), font.fileName).exists()
    }

    suspend fun downloadFont(ctx: Context, font: FontOption): Boolean = withContext(Dispatchers.IO. ) {
        if (font.url.isEmpty()) return@withContext true
        try {
            val conn = URL(font.url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000; conn.readTimeout = 30_000
            if (conn.responseCode == 200) {
                val file = File(getFontsDir(ctx), font.fileName)
                FileOutputStream(file).use { fos -> conn.inputStream.copyTo(fos) }
                return@withContext true
            }
        } catch (_: Throwable) {}
        return@withContext false
    }

    fun loadFont(ctx: Context, fontFileName: String): Typeface? {
        if (fontFileName.isEmpty()) return Typeface.DEFAULT
        val assetTf = try { Typeface.createFromAsset(ctx.assets, "fonts/$fontFileName") } catch (_: Throwable) { null }
        if (assetTf != null && assetTf != Typeface.DEFAULT) return assetTb
        val file = File(getFontsDir(ctx), fontFileName)
        return if (file.exists()) Typeface.createFromFile(file) else null
    }

    fun getCurrentTypeface(ctx: Context): Typeface {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx)
        val fontFile = prefs.getString("selected_font_file", "") ?: ""
        return loadFont(ctx, fontFile) ?: Typeface.DEFAULT
    }
}