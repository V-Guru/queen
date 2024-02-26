package com.wozart.aura.ui.createautomation.baseadapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.ui.dashboard.Device
import kotlinx.android.synthetic.main.item_select_device.view.*
import kotlinx.android.synthetic.main.item_select_device.view.deiceIcon
import kotlinx.android.synthetic.main.item_select_device.view.deviceCard
import kotlinx.android.synthetic.main.item_select_device.view.deviceName
import kotlinx.android.synthetic.main.item_select_device.view.deviceStatus
import kotlinx.android.synthetic.main.item_select_device.view.roomName
import kotlinx.android.synthetic.main.item_set_actions_device.view.*

abstract class BaseActionDeviceAdapter(val deviceList: ArrayList<Device>) : RecyclerView.Adapter<BaseActionDeviceAdapter.DeviceHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(getLayoutType(), parent, false)
        return DeviceHolder(inflatedView)
    }

    abstract fun getLayoutType(): Int

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        val device = deviceList[position]
        holder.bind(device)

        customizeUI(holder.itemView, device)
        registerListeners(holder, position)
    }

    abstract fun customizeUI(itemView: View?, device: Device)

    abstract fun registerListeners(holder: DeviceHolder?, position: Int)


    override fun getItemCount(): Int {
        return deviceList.size
    }

    class DeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(device: Device) = with(itemView) {
            if (itemView.deiceIcon != null) {
                if (device.name.equals("Fan") && device.isTurnOn) {
                    Glide.with(this).load(Utils.getIconDrawable(device.type, device.isTurnOn))
                            .into(itemView.deiceIcon)
                } else if (device.name.equals("Exhaust Fan") && device.isTurnOn) {
                    Glide.with(this).load(Utils.getIconDrawable(device.type, device.isTurnOn))
                            .into(itemView.deiceIcon)
                }
                itemView.deiceIcon.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
                itemView.deviceName.text = device.name
                itemView.roomName.text = device.roomName

                if (device.isTurnOn) {
                    if(device.dimmable) {
                        itemView.deviceStatus.text = device.dimVal.toString() + "%"
                    }else if(device.checkType == "Curtain"){
                        itemView.deviceStatus.text = "Open"
                    } else {
                        itemView.deviceStatus.text = "ON"
                    }
                    itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
                } else {
                    itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
                    if(device.checkType == "Curtain"){
                        itemView.deviceStatus.text = "Close"
                    } else {
                        itemView.deviceStatus.text = "Off"
                    }
                }
            }
        }
    }


}
