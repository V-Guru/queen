package com.wozart.aura.ui.base.baseselectdevices


import androidx.core.content.ContextCompat
import android.view.View
import android.view.animation.AnimationUtils
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils.getCurtainIcon
import com.wozart.aura.aura.utilities.Utils.getIconDrawable
import com.wozart.aura.ui.createautomation.baseadapters.BaseDevicesAdapter
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_select_device.view.*

class SelectDevicesAdapter(deviceList: ArrayList<Device>, onClick: RecyclerItemClicked) : BaseDevicesAdapter(deviceList) {

    var onClick : RecyclerItemClicked ?= null

    init {
        this.onClick = onClick
    }
    override fun customizeUI(itemView: View?, device: Device) {
        itemView?.let {
            // device.isSelected = device.isTurnOn
            val res = itemView.context.resources
            if (device.isSelected) {
                itemView.deviceStatus.visibility = View.GONE
                itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
                itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(device.isSelected))
            } else {
                itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
                itemView.deviceStatus.visibility = View.GONE
                itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(device.isSelected))
            }
        }
    }

    override fun getLayoutType(): Int {
        return R.layout.item_select_device
    }

    override fun registerListeners(holder: DeviceHolder?, position: Int) {
        val zoomin = AnimationUtils.loadAnimation(holder?.itemView?.context, R.anim.zoomin)
        val zoomout = AnimationUtils.loadAnimation(holder?.itemView?.context, R.anim.zoomout)
        val device = deviceList[position]
        if(device.checkType == "Curtain"){
            holder?.itemView?.deiceIcon?.setImageResource(R.drawable.ic_curtain_icons_curtain_close)
        }else{
            holder?.itemView?.deiceIcon?.setImageResource(getIconDrawable(device.type, device.isSelected))
        }
        holder?.itemView?.deviceCard?.setOnClickListener {
            holder.itemView.deviceCard.startAnimation(zoomin)
            holder.itemView.deviceCard.startAnimation(zoomout)
            device.isSelected = !device.isSelected
            holder.itemView.imgSelect?.background = ContextCompat.getDrawable(holder.itemView.context, getDrawableId(device.isSelected))
            if (device.isSelected){
                holder.itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
                onClick?.onRecyclerItemClicked(position,device,device.isSelected)
            }else{
                holder.itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
                onClick?.onRecyclerItemClicked(position,device,device.isSelected)
            }
            if(device.checkType == "Curtain"){
                holder.itemView.deiceIcon.setImageResource(getCurtainIcon(device.checkType, device.isSelected))
            }else{
                holder.itemView.deiceIcon.setImageResource(getIconDrawable(device.type, device.isSelected))
            }

        }
    }

    private fun getDrawableId(selected: Boolean): Int {
        if (selected) {
            return R.drawable.filled_circle
        } else {
            return R.drawable.ic_round_stroke
        }
    }

}