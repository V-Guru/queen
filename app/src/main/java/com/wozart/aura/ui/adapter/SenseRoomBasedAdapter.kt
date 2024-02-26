package com.wozart.aura.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wozart.aura.R
import com.wozart.aura.data.model.SenseRoomSeperation
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_motion_sensors.view.*


/**
 * Created by Saif on 12/08/21.
 * mds71964@gmail.com
 */
class SenseRoomBasedAdapter(var context: Context, var onClick: RecyclerItemClicked) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var senseDataList: MutableList<SenseRoomSeperation>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var adapter: MotionAdapter? = null

    inner class MotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView), RecyclerItemClicked {
        fun bind(sense: SenseRoomSeperation) {
            adapter = MotionAdapter(context)
            adapter?.onClick = onClick
            itemView.roomTitleTv.text = sense.roomName
            itemView.sense_load.layoutManager = GridLayoutManager(context, 3)
            itemView.sense_load.adapter = adapter
            (itemView.sense_load.adapter as MotionAdapter).setData(sense.senseLoadList)
        }

        override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
            onClick.onRecyclerItemClicked(position, data, viewType)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_motion_sensors, parent, false)
        return MotionViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return senseDataList!!.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MotionViewHolder).bind(senseDataList?.get(position)!!)
    }
}