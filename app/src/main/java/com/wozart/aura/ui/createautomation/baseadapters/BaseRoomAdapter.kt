package com.wozart.aura.ui.createautomation.baseadapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.GridAutoFitLayoutManager
import kotlinx.android.synthetic.main.item_select_room.view.*

/**
 * Created by Saif on 3/22/2018.
 */

abstract class BaseRoomAdapter : RecyclerView.Adapter<BaseRoomAdapter.ViewHolder>() {

    var rooms: MutableList<RoomModel> = ArrayList()

    fun init(rooms: MutableList<RoomModel>) {
        this.rooms = rooms
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = rooms[position]

        holder.itemView.tvRoomName?.text = room.name
        holder.itemView.let {
            holder.itemView.listDevices?.layoutManager = GridAutoFitLayoutManager(holder.itemView.context,
                    holder.itemView.resources.getDimensionPixelSize(R.dimen.device_item_size))
        }
        holder.itemView.listDevices?.isNestedScrollingEnabled = false
        bindAdapter(holder, room)
    }

    abstract fun bindAdapter(holder: ViewHolder?, room: RoomModel)

    override fun getItemCount(): Int = rooms.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_room, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}
