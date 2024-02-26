package com.wozart.aura.ui.adapter

import android.location.Address
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.wozart.aura.R
import kotlinx.android.synthetic.main.item_locations.view.*


/**
 * Created by Saif on 17/08/21.
 * mds71964@gmail.com
 */
class SearchLocationDisplayAdapter(var mListener: PlaceAutoCompleteInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var locationList: MutableList<Address> = arrayListOf()
    fun setData(addresses: MutableList<Address>) {
        this.locationList.clear()
        this.locationList.addAll(addresses)
        notifyDataSetChanged()
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(location: Address) {
            itemView.address.text = location.getAddressLine(0)
        }

        init {
            itemView.relativeLayout.setOnClickListener {
                if (adapterPosition != -1 && !locationList.isNullOrEmpty()) {
                    mListener.onPlaceClick(locationList[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_locations, parent, false)
        return PlaceViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PlaceViewHolder).bind(locationList[position])
    }

    override fun getItemCount(): Int {
        return locationList.size
    }


    interface PlaceAutoCompleteInterface {
        fun onPlaceClick(address: Address)
    }

}