package com.wozart.aura.ui.auraswitchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.ui.auraswitchlist.OnAdapterInteractionListener
import com.wozart.aura.entity.model.aura.AuraSwitch
import kotlinx.android.synthetic.main.item_aura_switch.view.*

/**
 * Created by Niranjan P on 3/14/2018.
 */
class AuraListAdapter(context: Context, val listener: OnAdapterInteractionListener) : androidx.recyclerview.widget.RecyclerView.Adapter<AuraListAdapter.ViewHolder>() {

    var auraSwitches: MutableList<AuraSwitch> = ArrayList()

    fun init(auraSwitches: MutableList<AuraSwitch>) {
        this.auraSwitches.clear()
        this.auraSwitches.addAll(auraSwitches)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val switch = auraSwitches[position]
        holder.bind(switch, listener)
    }

    override fun getItemCount(): Int = auraSwitches.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aura_switch, parent, false)
        return ViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(switch: AuraSwitch, listener: OnAdapterInteractionListener) = with(itemView) {
            val switchNames = switch.name.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            itemView.tvName?.text = switchNames[0]
            itemView.roomName?.text = switchNames[1]
            upgrade(itemView,switch.name)
            itemView.setOnClickListener {
                listener.onSelectAuraDevice(switch)
            }
        }

        fun upgrade(holder: View, espDevice: String?) {
            if (espDevice!!.contains("Switch") && (!espDevice.contains("Wozart Switch Pro"))|| espDevice.contains("WSC")) {
                holder.wifiIcon.setImageResource(R.drawable.ic_wozart_switch_controller)
            } else if (espDevice.contains("Sense") && !espDevice.contains("Sense Pro")|| espDevice.contains("WIR")) {
                holder.wifiIcon.setImageResource(R.drawable.ic_wozart_sense)
            } else if (espDevice.contains("Wozart Switch Pro")|| espDevice.contains("WFC")) {
                holder.wifiIcon.setImageResource(R.drawable.ic_wozart_switch_controller)
            } else if (espDevice.contains("Wozart Switch Controller Mini")|| espDevice.contains("WSM")) {
                holder.wifiIcon.setImageResource(R.drawable.ic_wozart_switch_controller_mini)
            } else if (espDevice.contains("Curtain")|| espDevice.contains("WCC")) {
                holder.wifiIcon.setImageResource(R.drawable.ic_wozart_switch_controller_mini)
            } else if (espDevice.contains("LED") || espDevice.contains("WLO")) {
                holder.wifiIcon.setImageResource(R.drawable.ic_wozart_switch_controller_mini)
            } else if (espDevice.contains("Plug")|| espDevice.contains("WPL")) {
                holder.wifiIcon.setImageResource(R.drawable.ic_wozart_smart_plug)
            } else if (espDevice.contains("Sense Pro")|| espDevice.contains("WFC")) {
                holder.wifiIcon.setImageResource(R.drawable.ic_wozart_sense)
            } else {
                holder.wifiIcon.setImageResource(R.drawable.ic_wozart_switch_controller)
            }
        }
    }



}