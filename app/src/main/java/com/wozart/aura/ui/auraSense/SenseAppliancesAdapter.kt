package com.wozart.aura.ui.auraSense

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.appliances_list.view.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-01-31
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 *****************************************************************************/

class SenseAppliancesAdapter(var list: MutableList<RemoteIconModel>,var onClick : RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<SenseAppliancesAdapter.SenseViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SenseViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.appliances_list, p0, false)
        return SenseViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: SenseViewHolder, p1: Int) {
       p0.bind(list[p1])
    }

    inner class SenseViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bind(appliances: RemoteIconModel) = with(itemView){
            itemView.appliances_image.setImageResource(Utils.getAppliancesImage(appliances.remoteIconButton,appliances.remoteButtonName!!))
            itemView.text_name.text = appliances.remoteButtonName
            itemView.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition,list[adapterPosition],list[adapterPosition])
            }
        }
    }
}