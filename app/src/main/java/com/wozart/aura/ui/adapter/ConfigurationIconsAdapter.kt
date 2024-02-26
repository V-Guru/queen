package com.wozart.aura.ui.adapter

import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.data.model.Load
import kotlinx.android.synthetic.main.item_configure_loads.view.*

class ConfigurationIconsAdapter(onLoadCallback: OnLoadSelected) : androidx.recyclerview.widget.RecyclerView.Adapter<ConfigurationIconsAdapter.ViewHolder>() {

    private var loadList: MutableList<Load> = ArrayList()
    private var selectedPos = 0
    fun init(mLoadList: MutableList<Load>) {
        this.loadList = mLoadList
    }

    private var listener: OnLoadSelected? = null

    init {
        listener = onLoadCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_configure_loads, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drawable = loadList[position]
        holder.itemView.img_load.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, drawable.loadImage))
        holder.itemView.tv_load.text = drawable.loadName
        if (selectedPos != position) {
            holder.itemView.load_layout?.background = holder.itemView.context.getDrawable(R.drawable.image_border)
            holder.itemView.load_arrow.visibility = View.GONE
        } else {
            holder.itemView.load_layout?.background = holder.itemView.context.getDrawable(R.drawable.image_border_selected)
            holder.itemView.load_arrow.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            var prevPos = selectedPos
            selectedPos = position
            if (prevPos != -1)
                notifyItemChanged(prevPos)
            notifyItemChanged(selectedPos)
        }
        listener?.onLoadCallback(selectedPos)

    }

    override fun getItemCount(): Int {
        return this.loadList.size
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface OnLoadSelected {
        fun onLoadCallback(load: Int)
    }

}