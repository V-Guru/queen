package com.wozart.aura.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_curtain_device.view.*


/**
 * Created by Saif on 28/01/21.
 * mds71964@gmail.com
 */

class CurtainDeviceAdapter(var context: Context, var onClick: RecyclerItemClicked) : RecyclerView.Adapter<CurtainDeviceAdapter.CurtainViewHolder>() {

    var curtainDeviceList: MutableList<Device> = ArrayList()

    fun setDataDevice(list: ArrayList<Device>) {
        this.curtainDeviceList.clear()
        this.curtainDeviceList.addAll(list)
        notifyDataSetChanged()
    }

    inner class CurtainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(device: Device) {

        }

        init {
            itemView.curtainOpen.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition, curtainDeviceList[adapterPosition], R.id.curtainOpen)
            }

            itemView.curtainClose.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition, curtainDeviceList[adapterPosition], R.id.curtainClose)
            }

            itemView.curtainStop.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition, curtainDeviceList[adapterPosition], R.id.curtainStop)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurtainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_curtain_device, parent, false)
        return CurtainViewHolder(view)
    }

    override fun getItemCount(): Int {
        return curtainDeviceList.size
    }

    override fun onBindViewHolder(holder: CurtainViewHolder, position: Int) {
        holder.bind(curtainDeviceList[position])
    }

}