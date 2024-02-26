package com.wozart.aura.aura.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.wozart.aura.R
import com.wozart.aura.data.model.Room
import kotlinx.android.synthetic.main.item_room_wallpaper.view.*
import org.jetbrains.anko.imageView
import com.wozart.aura.aura.utilities.Utils.getRoomDrawable


/**
 * Created by Drona Sahoo on 3/15/2018.
 */

class RoomWallpaperAdapter(private val mContext: Context, private val mRoomList: MutableList<Room>,var listner : (Int , Boolean) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<RoomWallpaperAdapter.ViewHolder>() {

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val zoomin = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.zoomin)
        val zoomout = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.zoomout)
        if(holder!=null)
        {
            holder.itemView.grid_image.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, mRoomList[position].url))
            if (mRoomList[position].isSelected == true) {
                //DrawableCompat.setTint(holder.itemView.grid_image.getDrawable(), ContextCompat.getColor(holder.itemView.context, R.color.white))
                holder.itemView.grid_image.setImageResource(getRoomDrawable(position,mRoomList[position].isSelected))
                holder.itemView.wallpaper_card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
                holder.itemView.image_layout.background = mContext.getDrawable(R.drawable.room_image_border)
            } else {
                 //DrawableCompat.setTint(holder.itemView.grid_image.getDrawable(), ContextCompat.getColor(holder.itemView.context, R.color.gray))
                holder.itemView.image_layout.setBackgroundResource(R.drawable.rounded_layout)
                holder.itemView.wallpaper_card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.list_item_inactive))
                holder.itemView.grid_image.setImageResource(getRoomDrawable(position,mRoomList[position].isSelected))
            }
            holder.itemView.image_layout.setOnClickListener {
                holder.itemView.wallpaper_card.startAnimation(zoomin)
                holder.itemView.wallpaper_card.startAnimation(zoomout)
                resetRoomList(position)
                listner(position,false)
                notifyDataSetChanged()
            }

        }
    }

    private var wallPaperID: Int = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent?.context).inflate(R.layout.item_room_wallpaper, parent, false)
        var viewHolder = ViewHolder(view);
        return viewHolder;
    }

    override fun getItemCount(): Int {
        return mRoomList.size
    }

    private fun resetRoomList(pos: Int) {
        for (i in mRoomList.indices) {
            if (i == pos)
                mRoomList[i].isSelected = true
            else
                mRoomList[i].isSelected = false
        }
        wallPaperID=pos
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

}