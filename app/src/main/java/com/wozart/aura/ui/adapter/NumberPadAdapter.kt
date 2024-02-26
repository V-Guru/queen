package com.wozart.aura.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wozart.aura.R
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.dialog_number_item.view.*


/**
 * Created by Saif on 21/04/21.
 * mds71964@gmail.com
 */
class NumberPadAdapter(var onClick : RecyclerItemClicked) : RecyclerView.Adapter<NumberPadAdapter.NumberViewHolder>() {

    var remoteNumberList : MutableList<RemoteIconModel> = arrayListOf()
    fun setData(listRemoteData: MutableList<RemoteIconModel>){
        remoteNumberList.clear()
        remoteNumberList.addAll(listRemoteData)
        notifyDataSetChanged()
    }

    inner class NumberViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        fun bind(remoteIconModel: RemoteIconModel){
            val nameArrray =  remoteIconModel.remoteButtonName!!.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            itemView.tvNumpad.text=nameArrray[1]
        }
        init {
            itemView.cvCard.setOnClickListener {
                if(adapterPosition != -1 && adapterPosition >= 0){
                    onClick.onRecyclerItemClicked(adapterPosition,remoteNumberList[adapterPosition],"KEY_PAD_CONTROL")
                }
            }

            itemView.cvCard.setOnLongClickListener {
                if(adapterPosition != -1 && adapterPosition >= 0){
                    onClick.onRecyclerItemClicked(adapterPosition,remoteNumberList[adapterPosition],"KEY_PAD_LEARN")
                }
                true
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberViewHolder {
        return NumberViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.dialog_number_item,parent,false))
    }

    override fun getItemCount(): Int {
       return remoteNumberList.size
    }

    override fun onBindViewHolder(holder: NumberViewHolder, position: Int) {
        holder.bind(remoteNumberList[position])
    }
}