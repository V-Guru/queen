package com.wozart.aura.aura.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.Wallpaper
import com.wozart.aura.aura.utilities.BounceInterpolator
import com.wozart.aura.aura.utilities.Utils
import kotlinx.android.synthetic.main.grid_item.view.*


/**
 * Created by Drona Sahoo on 3/15/2018.
 */

class WallpaperAdapter(private val mContext: Context,var listener:WallpaperListener, private var mWallpaperList: MutableList<Wallpaper>) : androidx.recyclerview.widget.RecyclerView.Adapter<WallpaperAdapter.ViewHolder>() {

    private var wallPaperID:Int=0
    private var drawablesSize = 5
    private var mListener:WallpaperListener=listener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent?.context).inflate(R.layout.grid_item, parent, false)
        var viewHolder = ViewHolder(view);
        this.mListener=listener
        return viewHolder;
    }

    override fun getItemCount(): Int {
        return mWallpaperList.size
    }
    fun update(wallpaperList: MutableList<Wallpaper>)
    {
        this.mWallpaperList=wallpaperList
        notifyDataSetChanged()
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder != null) {

            val zoomin = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.zoomin)
            val zoomout = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.zoomout)

            holder.itemView.grid_image.setImageResource(mWallpaperList[position].id)

           // Glide.with(mContext).load(mWallpaperList[position].id).into(holder.itemView.grid_image)


            if (mWallpaperList[position].isSelected )
                holder.itemView.image_layout.background = mContext.getDrawable(R.drawable.image_home_bg_border)
            else
                holder.itemView.image_layout.background = mContext.getDrawable(R.drawable.image_selected_home_bg_border)

            holder.itemView.wallpaper_card.setOnClickListener {
                holder.itemView.wallpaper_card.startAnimation(zoomin)
                holder.itemView.wallpaper_card.startAnimation(zoomout)
                resetWallPaperList(position)
                listener.onWallpaperSelect(wallPaperID)
                notifyDataSetChanged()
            }
        }

    }

    fun getWallPaperId():Int
    {
        return wallPaperID
    }

    private fun resetWallPaperList(pos: Int) {
        for (i in mWallpaperList.indices) {
            mWallpaperList[i].isSelected = i == pos
            wallPaperID=pos
        }
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface WallpaperListener{
        fun onWallpaperSelect(wallpaerId:Int)
    }

}
