package com.superlauncher.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ApkSharer {

    private fun getOutputDir(ctx: Context): File {
        val dir = File(android.os.Environment.getExternalStorageDirectory(), "SuperLauncher/APKs")
        dir.mkdirs()
        return dir
    }

    fun shareApp(ctx: Context, pkg: String, label: String) {
        Toast.makeText(ctx, "جاري تجهيز المشاركة...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ai = ctx.packageManager.getApplicationInfo(pkg, 0)
                val base: String = ai.sourceDir
                val splits: Array<String>? = ai.splitSourceDirs
                val outDir = getOutputDir(ctx)

                val safeName = label.replace(Regex("[^A-Za-z0-9._\\-]"), "_").ifEmpty { pkg }
                val (file, mime) = if (splits.isNullOrEmpty()) {
                    val f = File(outDir, "$safeName.apk")
                    FileInputStream(File(base)).use { ins ->
                        FileOutputStream(f).use { outs -> ins.copyTo(outs) }
                    }
                    f to "application/vnd.android.package-archive"
                } else {
                    val f = File(outDir, "$safeName.apks")
                    ZipOutputStream(FileOutputStream(f)).use { zip ->
                        addToZip(zip, File(base), "base.apk")
                        splits.forEach { sp -> addToZip(zip, File(sp), File(sp).name) }
                    }
                    f to "application/zip"
                }

                val uri: Uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.superlauncher.fileprovider", file)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, label)
                    putExtra(Intent.EXTRA_TEXT, "$label ($pkg)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(send, "مشاركة $label")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                withContext(Dispatchers.Main) { ctx.startActivity(chooser) }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(ctx, "فشلت المشاركة: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addToZip(zip: ZipOutputStream, src: File, name: String) {
        zip.putNextEntry(ZipEntry(name))
        FileInputStream(src).use { it.copyTo(zip) }
        zip.closeEntry()
    }
}
