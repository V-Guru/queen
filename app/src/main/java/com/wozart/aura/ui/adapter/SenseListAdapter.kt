package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_sense_list.view.*


/**
 * Created by Saif on 05/08/20.
 * EZJobs
 * mdsaif@onata.com
 */
class SenseListAdapter(context: Context,var onClick : RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<SenseListAdapter.SenseViewHolder>() {

    var senseList : MutableList<RemoteModel> = ArrayList()
    fun setData(list:MutableList<RemoteModel>){
        this.senseList.clear()
        this.senseList.addAll(list)
        notifyDataSetChanged()
    }

    inner class SenseViewHolder(itemView:View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bind(senseDevice:RemoteModel){
            itemView.device_name.text = senseDevice.aura_sence_name
        }
        init {
            itemView.cardView.setOnClickListener {
                for(sense in 0 until senseList.size){
                    if(senseList[sense].isSelected){
                        senseList[sense].isSelected = false
                        notifyItemChanged(sense)
                        break
                    }
                }
                senseList[adapterPosition].isSelected = !senseList[adapterPosition].isSelected
                itemView.ivSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(senseList[adapterPosition].isSelected))
                onClick.onRecyclerItemClicked(adapterPosition,senseList[adapterPosition],R.id.cardView)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SenseViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_sense_list,p0,false)
        return SenseViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return senseList.size
    }

    override fun onBindViewHolder(p0: SenseViewHolder, p1: Int) {
        p0.bind(senseList[p1])
    }

    private fun getDrawableId(selected: Boolean): Int {
        if (selected) {
            return R.drawable.filled_circle
        } else {
            return R.drawable.ic_round_stroke
        }
    }
}