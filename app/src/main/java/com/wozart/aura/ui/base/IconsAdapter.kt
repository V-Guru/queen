package com.wozart.aura.ui.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.item_channel_image.view.*
import kotlinx.android.synthetic.main.item_icon.view.imgThumbnail


class IconsAdapter(var listner: (Int, Boolean) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var drawables: MutableList<Int> = ArrayList()
    private var selectedPos = 0
    var viewTypeIcon = Constant.ICON_VIEW
    var recyclerItemClicked : RecyclerItemClicked ?= null


    fun init(drawables: MutableList<Int>, posNumber: Int) {
        this.drawables = drawables
        this.selectedPos = posNumber
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewTypeIcon) {
            Constant.ICON_VIEW -> {
                return IconViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_icon, parent, false))
            }
            Constant.CHANNEL_ICON_VIEW -> {
                return ChannelIconViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_channel_image, parent, false))
            }
            else -> {
                return IconViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_icon, parent, false))
            }
        }
    }

    inner class ChannelIconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView) {
        fun bind(drawable: Int) {
            itemView.channelImage.setImageDrawable(ContextCompat.getDrawable(itemView.context, drawable))
        }

        init {
            if(drawables.size - 1 == adapterPosition){
                recyclerItemClicked?.onRecyclerItemClicked(adapterPosition,drawables[adapterPosition],Constant.CAMERA)
            }
        }

    }

    inner class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(drawable: Int) {
            itemView.imgThumbnail.setImageDrawable(ContextCompat.getDrawable(itemView.context, drawable))

            if (selectedPos != adapterPosition) {
                itemView.background = itemView.context.getDrawable(R.drawable.image_border)
            } else {
                itemView.background = itemView.context.getDrawable(R.drawable.image_border_selected)
            }

            itemView.setOnClickListener {
                val prevPos = selectedPos
                selectedPos = adapterPosition
                listner(selectedPos, false)
                if (prevPos != -1)
                    notifyItemChanged(prevPos)
                notifyItemChanged(selectedPos)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (viewTypeIcon) {
            Constant.ICON_VIEW -> {
                (holder as IconViewHolder).bind(drawables[position])
            }
            Constant.CHANNEL_ICON_VIEW -> {
                (holder as ChannelIconViewHolder).bind(drawables[position])
            }
        }
    }

    fun selectedPostion(): Int {
        return selectedPos
    }

    fun selectIconPosition(pos: Int) {
        val prevPos = selectedPos
        selectedPos = pos
        if (prevPos != -1)
            notifyItemChanged(prevPos)
        notifyItemChanged(selectedPos)
    }

    override fun getItemCount(): Int {
        return this.drawables.size
    }

//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}
}