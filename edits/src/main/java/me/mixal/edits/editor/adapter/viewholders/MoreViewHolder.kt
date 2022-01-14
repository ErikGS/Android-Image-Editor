package me.mixal.edits.editor.adapter.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import me.mixal.edits.R

class MoreViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField var moreButton: View = itemView.findViewById(R.id.color_panel_more)
}