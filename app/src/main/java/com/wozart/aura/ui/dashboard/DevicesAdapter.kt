package com.wozart.aura.ui.dashboard

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.wozart.aura.R
import com.wozart.aura.aura.ui.dashboard.listener.OnDeviceOptionsListener
import com.wozart.aura.aura.utilities.Utils.getCurtainIcon
import com.wozart.aura.aura.utilities.Utils.getIconDrawable
import kotlinx.android.synthetic.main.item_device.view.*


/***
 * Created by Saif on 14-03-2018.
 */
class DevicesAdapter(val deviceList: ArrayList<Device>, val room: String, val listener: (Device, Boolean) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<DevicesAdapter.DeviceHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        holder.bind(deviceList[position], room, listener)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    class DeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(device: Device, room: String, listener: (Device, Boolean) -> Unit) = with(itemView) {

            val zoomin = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomin)
            val zoomout = AnimationUtils.loadAnimation(itemView.context, R.anim.click_anim)

            itemView.deviceName.text = device.name
            itemView.roomName.text = device.roomName

            if (device.status == "off") {
                itemView.deviceStatus.visibility = View.INVISIBLE
                itemView.errorIcon.visibility = View.INVISIBLE
                itemView.error.visibility = View.VISIBLE
                itemView.error.setTextColor(resources.getColor(R.color.Red))
                itemView.error.text = "No Response"
                itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
            } else if ((device.status == "update") and (device.devicePresent)) {
                itemView.deviceStatus.visibility = View.INVISIBLE
                itemView.errorIcon.visibility = View.INVISIBLE
                itemView.error.visibility = View.VISIBLE
                itemView.error.setTextColor(resources.getColor(R.color.black))
                itemView.error.text = "Updating.."
                itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
            } else if ((device.status == "update") and (!device.devicePresent)) {
                itemView.deviceStatus.visibility = View.INVISIBLE
                itemView.errorIcon.visibility = View.INVISIBLE
                itemView.error.visibility = View.VISIBLE
                itemView.error.setTextColor(resources.getColor(R.color.Red))
                itemView.error.text = "No Response"
                itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
            } else {
                if (device.status == "cloud") {
                    itemView.errorIcon.visibility = View.VISIBLE
                } else {
                    itemView.errorIcon.visibility = View.INVISIBLE
                }

                if (device.isTurnOn) {
                    if (!device.dimmable) {
                        if (device.checkType == "Curtain") {
                            itemView.deviceStatus.text = "Open"
                            itemView.error.visibility = View.INVISIBLE
                            itemView.deviceStatus.visibility = View.INVISIBLE
                            itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
                        } else {
                            if(device.power != 0){
                                itemView.plugPower.visibility = View.GONE
                                itemView.plugPower.text = device.power.toString()
                            }
                            itemView.deviceStatus.text = "ON"
                            itemView.error.visibility = View.INVISIBLE
                            itemView.deviceStatus.visibility = View.VISIBLE
                            itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
                        }
                    } else {
                        itemView.deviceStatus.text = device.dimVal.toString() + "%"
                        itemView.error.visibility = View.INVISIBLE
                        itemView.deviceStatus.visibility = View.VISIBLE
                        itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
                    }
                }

                if (!device.isTurnOn) {
                    if (device.checkType == "Curtain") {
                        itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
                        itemView.deviceStatus.text = "Close"
                        itemView.deviceStatus.visibility = View.INVISIBLE
                        itemView.error.visibility = View.INVISIBLE
                    } else {
                        itemView.plugPower.visibility = View.GONE
                        itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
                        itemView.deviceStatus.text = "Off"
                        itemView.deviceStatus.visibility = View.VISIBLE
                        itemView.error.visibility = View.INVISIBLE
                    }

                }

            }
            if (device.name == "Fan" && device.isTurnOn) {
                Glide.with(this).load(getIconDrawable(device.type, device.isTurnOn))
                        .into(itemView.deiceIcon)
            } else if (device.name == "Exhaust Fan" && device.isTurnOn) {
                Glide.with(this).load(getIconDrawable(device.type, device.isTurnOn))
                        .into(itemView.deiceIcon)
            } else {
                if (device.checkType == "Curtain") {
                    itemView.deiceIcon.setImageResource(getCurtainIcon(device.checkType!!, device.isTurnOn))
                } else {
                    itemView.deiceIcon.setImageResource(getIconDrawable(device.type, device.isTurnOn))
                }

            }

            itemView.setOnClickListener {
                itemView.deviceCard.startAnimation(zoomin)
                itemView.deviceCard.startAnimation(zoomout)
                listener(device, false)
            }
            itemView.setOnLongClickListener {
                listener(device, true)
                true
            }

        }
    }
}