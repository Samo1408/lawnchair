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

class DrawerCellAdapter(private val items: List<AppItem>) : RecyclerView.Adapter<DrawerCellAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) { val icon: ImageView = v.findViewById(R.id.icon); val label: TextView = v.findViewById(R.id.label) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]; h.label.text = item.label; IconCache.load(h.itemView.context, item, h.icon)
        h.itemView.setOnClickListener { AppGridAdapter.launch(h.itemView.context, item, h.itemView) }
    }
}
