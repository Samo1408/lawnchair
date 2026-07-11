package com.homelauncher.prime.ui

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.homelauncher.prime.R
import com.homelauncher.prime.data.AppItem

object FolderPopup {
    fun show(ctx: Context, name: String, apps: List<AppItem>, onAppClick: (AppItem, View) -> Unit, onAppLongClick: (AppItem, View) -> Unit) {
        val dialog = Dialog(ctx, android.R.style.Theme_DeviceDefault_Dialog)
        dialog.setContentView(R.layout.dialog_folder)
        (dialog.findViewById<android.widget.TextView>(R.id.folderTitle)).text = name
        val rv = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.folderMini)
        rv.layoutManager = GridLayoutManager(ctx, 4)
        rv.adapter = AppGridAdapter(apps.toMutableList(), onAppClick, onAppLongClick)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }
}
