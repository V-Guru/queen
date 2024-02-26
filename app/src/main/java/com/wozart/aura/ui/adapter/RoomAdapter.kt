package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.wozart.aura.R
import com.wozart.aura.data.model.Room
import kotlinx.android.synthetic.main.grid_item_rooms.view.*

/**
 * Created by Drona Sahoo on 3/15/2018.
 */

class RoomAdapter(private val mContext: Context, private val listener: RoomListener, private var mRoomList: MutableList<Room>) : androidx.recyclerview.widget.RecyclerView.Adapter<RoomAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val zoomin = AnimationUtils.loadAnimation(holder?.itemView?.context, R.anim.zoomin)
        val zoomout = AnimationUtils.loadAnimation(holder?.itemView?.context, R.anim.zoomout)
        if (holder != null) {
            holder.itemView.room_name.text = mRoomList[position].roomName
            /* if (position == 0)
             holder.itemView.grid_image.scaleType=ImageView.ScaleType.CENTER_INSIDE*/
            holder.itemView.grid_image.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, mRoomList[position].url))
            if (mRoomList[position].isSelected && position != 0) {
                // DrawableCompat.setTint(holder.itemView.grid_image.getDrawable(), ContextCompat.getColor(holder.itemView.context, R.color.white));
                holder.itemView.image_layout.background = mContext.getDrawable(R.drawable.room_image_border)
                // holder.itemView.room_name.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.white))
            } else {
                //  DrawableCompat.setTint(holder.itemView.grid_image.getDrawable(), ContextCompat.getColor(holder.itemView.context, R.color.gray));
                holder.itemView.image_layout.setBackgroundResource(R.drawable.rounded_layout)
                // holder.itemView.room_name.setTextColor(ContextCompat.getColor(holder.itemView.context,R.color.grid_text))
            }
            holder.itemView.image_layout.setOnClickListener {
                holder.itemView.image_layout.startAnimation(zoomin)
                holder.itemView.image_layout.startAnimation(zoomout)
                if (position == 0) {
                    listener.onAddRoomClicked()

                } else {
                    resetRoomList(position)
                    listener.onRoomClicked()
                }
                notifyDataSetChanged()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.grid_item_rooms, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return mRoomList.size
    }


    fun update(roomList: MutableList<Room>) {
        mRoomList = roomList
        notifyDataSetChanged()
    }

    private fun resetRoomList(pos: Int) {
        for (i in mRoomList.indices) {
            mRoomList[i].isSelected = i == pos
        }
    }


    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface RoomListener {
        fun onAddRoomClicked()
        fun onRoomClicked()
    }


}
