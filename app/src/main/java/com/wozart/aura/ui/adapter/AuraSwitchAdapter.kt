package com.wozart.aura.aura.ui.adapter

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.ui.home.ConfigureLoadActivity
import com.wozart.aura.data.model.AuraSwitch
import kotlinx.android.synthetic.main.item_aura_switch.view.*

/**
 * Created by Niranjan P on 3/14/2018.
 */
class AuraSwitchAdapter() : androidx.recyclerview.widget.RecyclerView.Adapter<AuraSwitchAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val switch = auraSwitches.get(position)
        holder?.itemView?.tvName?.text = switch.name
        holder?.itemView?.setOnClickListener {
            val intent = Intent(holder?.itemView?.context, ConfigureLoadActivity::class.java)
            holder?.itemView?.context?.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent?.context).inflate(R.layout.item_aura_switch, parent, false)
        var viewHolder = ViewHolder(view);
        return viewHolder;
    }

    var auraSwitches: MutableList<AuraSwitch> = ArrayList()

    fun init(auraSwitches: MutableList<AuraSwitch>) {
        this.auraSwitches = auraSwitches
    }


    override fun getItemCount(): Int = auraSwitches.size


    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}