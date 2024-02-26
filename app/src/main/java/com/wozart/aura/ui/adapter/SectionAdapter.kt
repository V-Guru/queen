package com.wozart.aura.aura.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.data.model.SectionModel
import kotlinx.android.synthetic.main.item_sectioned_list.view.*
import java.util.*

/**
 * Created by Drona Sahoo on 3/19/2018.
 */

class SectionAdapter(private val context: Context, private val sectionModelArrayList: ArrayList<SectionModel>) :RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {


   class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sectioned_list, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val sectionModel = sectionModelArrayList[position]
        holder.itemView.section_label?.text = sectionModel.sectionLabel

        //recycler view for items
        holder.itemView.item_recycler_view?.setHasFixedSize(true)
        holder.itemView.item_recycler_view?.isNestedScrollingEnabled = false

        /* set layout manager on basis of recyclerview enum type */

        val linearLayoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false)
        holder.itemView.item_recycler_view?.layoutManager = linearLayoutManager

        val adapter = StatisticsItemAdapter(context, sectionModel.sectionLabel, sectionModel.itemArrayList)
        holder.itemView.item_recycler_view?.adapter = adapter


    }

    override fun getItemCount(): Int {
        return sectionModelArrayList.size
    }


}
