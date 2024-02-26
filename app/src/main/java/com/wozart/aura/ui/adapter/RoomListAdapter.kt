package com.wozart.aura.aura.ui.adapter

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.DialogRoomList
import kotlinx.android.synthetic.main.dialog_item_rooms.view.*

class RoomListAdapter(private val homeList: ArrayList<DialogRoomList>, val listener: (DialogRoomList, Int) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<RoomListAdapter.HomeHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        val inflatedView = LayoutInflater.from(parent?.context).inflate(R.layout.dialog_item_rooms, parent, false)
        return HomeHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        var room = homeList.get(position)
        holder?.itemView?.homeTitle?.text = homeList.get(position).title
        holder?.itemView?.setOnClickListener {
            resetRoomList(position)
            notifyDataSetChanged()

        }
        if (room.isInActive) {
            holder?.itemView?.homeTitle?.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorAccent))

        } else {
            holder?.itemView?.homeTitle?.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }
    }


    override fun getItemCount(): Int {
        return homeList.size
    }


    private fun resetRoomList(pos: Int) {
        for (i in homeList.indices) {
            if (i == pos)
                homeList[i].isInActive = true
            else
                homeList[i].isInActive = false
        }
    }

    class HomeHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}