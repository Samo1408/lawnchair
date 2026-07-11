package com.homelauncher.prime.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.homelauncher.prime.R
import com.homelauncher.prime.data.AppItem
import com.homelauncher.prime.util.IconCache

class DesktopAdapter(private val items: List<Entry>, private val onClick: (AppItem, View) -> Unit, private val onLongClick: (AppItem, View) -> Unit) : RecyclerView.Adapter<DesktopAdapter.VH>() {
    sealed class Entry { data class Shortcut(val app: AppItem) : Entry() }
    class VH(v: View) : RecyclerView.ViewHolder(v) { val icon: ImageView = v.findViewById(R.id.icon); val label: TextView = v.findViewById(R.id.label) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val entry = items[pos]
        if (entry is Entry.Shortcut) { h.label.text = entry.app.label; IconCache.load(h.itemView.context, entry.app, h.icon); h.itemView.setOnClickListener { onClick(entry.app, h.itemView) }; h.itemView.setOnLongClickListener { onLongClick(entry.app, h.itemView); true } }
    }
}
