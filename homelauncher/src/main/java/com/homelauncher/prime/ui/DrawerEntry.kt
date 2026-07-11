package com.homelauncher.prime.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.homelauncher.prime.R
import android.widget.TextView

class DrawerEntry(private val text: String) : View {
    constructor(ctx: Context) : this(ctx.apply { LayoutInflater.from(it).inflate(R.layout.page_drawer, null) })
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
}
