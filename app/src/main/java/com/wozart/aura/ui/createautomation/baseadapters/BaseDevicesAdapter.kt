package com.wozart.aura.ui.createautomation.baseadapters

import android.database.sqlite.SQLiteDatabase
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.item_select_device.view.*

abstract class BaseDevicesAdapter(val deviceList: ArrayList<Device>) : RecyclerView.Adapter<BaseDevicesAdapter.DeviceHolder>() {
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
        fun bind(device: Device) = with(itemView) {
            if (itemView.deiceIcon != null) {
                if (device.name.equals("Fan") && device.isTurnOn) {
                    Glide.with(this).load(Utils.getIconDrawable(device.type, device.isTurnOn))
                            .into(itemView.deiceIcon)
                } else if (device.name.equals("Exhaust Fan") && device.isTurnOn) {
                    Glide.with(this).load(Utils.getIconDrawable(device.type, device.isTurnOn))
                            .into(itemView.deiceIcon)
                }
                if(device.checkType == "Curtain"){
                    itemView.deiceIcon.setImageResource(Utils.getCurtainIcon(device.checkType, device.isTurnOn))
                }else{
                    itemView.deiceIcon.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
                }

                itemView.deviceName.text = device.name
                itemView.roomName.text = device.roomName

                if (device.isTurnOn) {
                    itemView.deviceStatus.text = device.dimVal.toString() + "%"
                    itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
                } else {
                    itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
                    itemView.deviceStatus.text = "Off"
                }
            }
        }
    }


}