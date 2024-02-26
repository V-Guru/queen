package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.internal.Utils
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_wozart_device.view.*


/**
 * Created by Saif on 17/08/20.
 * EZJobs
 * mdsaif@onata.com
 */
class DeviceListAdapter(var context: Context,var onClick : RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    var listDeviceName :ArrayList<String> = ArrayList()

    fun setData(list : ArrayList<String>){
        this.listDeviceName.clear()
        this.listDeviceName.addAll(list)
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bind(device: String){
            itemView.deviceName.text = device
            itemView.ivDeviceIcon.setImageResource(com.wozart.aura.aura.utilities.Utils.getDeviceIcon(context,device))
        }
        init {
            itemView.cardDevice.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition,listDeviceName[adapterPosition],listDeviceName[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DeviceViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_wozart_device,p0,false)
        return DeviceViewHolder(rootView)
    }

    override fun getItemCount(): Int {
       return listDeviceName.size
    }

    override fun onBindViewHolder(p0: DeviceViewHolder, p1: Int) {
       p0.bind(listDeviceName[p1])
    }
}