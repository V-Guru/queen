package com.wozart.aura.aura.ui.dashboard.rooms

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.data.model.Room
import kotlinx.android.synthetic.main.item_room_list.view.*

/***
 * Created by Kiran on 16-03-2018.
 */
class RoomsListAdapter(private val roomsList: ArrayList<Room>, val listener: (Room) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<RoomsListAdapter.RoomHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomHolder {
        val inflatedView = LayoutInflater.from(parent?.context).inflate(R.layout.item_room_list, parent, false)
        return RoomHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return roomsList.size
    }

    override fun onBindViewHolder(holder: RoomHolder, position: Int) {
        holder?.bind(roomsList.get(position), listener)
    }

    class RoomHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(room: Room, listener: (Room) -> Unit) = with(itemView) {
            itemView.roomName.text = room.roomName
            itemView.editRoom.setOnClickListener() {
                listener(room)
            }

        }
    }
}