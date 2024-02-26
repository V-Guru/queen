package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.entity.model.aura.AuraSceneButton
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_sense_list.view.*


/**
 * Created by Saif on 10/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class DeviceButtonAdapter(var context: Context, var onClick: RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<DeviceButtonAdapter.DeviceHolder>() {

    var buttonList: MutableList<AuraSceneButton> = ArrayList()

    fun setData(list: MutableList<AuraSceneButton>) {
        this.buttonList.clear()
        this.buttonList.addAll(list)
        notifyDataSetChanged()
    }

    inner class DeviceHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(auraSceneButton: AuraSceneButton) {
            itemView.deviceName.text = auraSceneButton.buttonName
        }

        init {
            itemView.cardView.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition,buttonList[adapterPosition],R.id.cardView)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DeviceHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_sense_list, p0, false)
        return DeviceHolder(rootView)
    }

    override fun getItemCount(): Int {
        return buttonList.size
    }

    override fun onBindViewHolder(p0: DeviceHolder, p1: Int) {
        p0.bind(buttonList[p1])
    }
}