package com.wozart.aura.aura.ui.adapter

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide

import com.wozart.aura.R
import com.wozart.aura.aura.data.model.Statistics
import com.wozart.aura.aura.ui.dashboard.more.StatisticsGraphActivity
import com.wozart.aura.aura.ui.dashboard.more.StatsticsActivity
import com.wozart.aura.aura.utilities.Utils
import kotlinx.android.synthetic.main.grid_item.view.*

import java.util.ArrayList
import kotlinx.android.synthetic.main.item_statistics.view.*

/**
 * Created by Drona Sahoo on 3/19/2018.
 */

class StatisticsItemAdapter(private val context: Context, private val label: String, private val arrayList: MutableList<Statistics>) : androidx.recyclerview.widget.RecyclerView.Adapter<StatisticsItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_statistics, parent, false)

        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder?.itemView?.load_name?.text = arrayList.get(position).loadName
        //Glide.with(context).load(Utils.getImage(arrayList[position].loadImage,context)).into(holder.itemView.IVload)
        holder.itemView.IVload.setImageDrawable(holder?.itemView?.context?.getDrawable(Utils.getImage(arrayList[position].loadImage,
                holder?.itemView?.context!!)))
        holder?.itemView?.item_label?.text=arrayList.get(position).powerConsumed
        holder?.itemView?.setOnClickListener {
            val intent = Intent(context.applicationContext, StatisticsGraphActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }


}
