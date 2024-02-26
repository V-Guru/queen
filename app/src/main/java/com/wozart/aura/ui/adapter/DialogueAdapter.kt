package com.wozart.aura.ui.adapter

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.model.RoomUpdateModel
import kotlinx.android.synthetic.main.item_room_list.view.*

class DialogueAdapter(context: Context, var clicked: RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<DialogueAdapter.ItemViewHolder>() {

    var roomList: MutableList<RoomUpdateModel> = ArrayList()
    fun setData(list: MutableList<RoomUpdateModel>) {
        this.roomList.clear()
        this.roomList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(room: RoomUpdateModel) {
            itemView.roomName.text = room.roomName
            itemView.roomName.isChecked = room.isSelected
            itemView.roomName.setOnClickListener {
                for (r in 0 until roomList.size) {
                    if (roomList[r].isSelected) {
                        roomList[r].isSelected = false
                        itemView.roomName.isChecked = false
                        notifyItemChanged(r)
                        break
                    }
                }
                roomList[adapterPosition].isSelected = true
                itemView.roomName.isChecked = roomList[adapterPosition].isSelected
                clicked.onRecyclerItemClicked(adapterPosition, roomList[adapterPosition], roomList[adapterPosition].isSelected)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ItemViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_room_list, p0, false)
        return ItemViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    override fun onBindViewHolder(p0: ItemViewHolder, p1: Int) {
        p0.bind(roomList[p1])
    }
}