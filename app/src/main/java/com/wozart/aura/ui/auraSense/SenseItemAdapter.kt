package com.wozart.aura.ui.auraSense

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.item_sense_load.view.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-02-12
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/

class SenseItemAdapter(context: Context, var sense_load: MutableList<AuraSenseConfigure>, var senseDeviceList: MutableList<RemoteModel>) : androidx.recyclerview.widget.RecyclerView.Adapter<SenseItemAdapter.SenseViewHolder>() {

    var remoteData = RemoteListModel()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SenseViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_room_sense_loads, p0, false)
        return SenseViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return sense_load.size
    }


    override fun onBindViewHolder(p0: SenseViewHolder, p1: Int) {
        p0.bind(sense_load[p1], p1)
    }


    inner class SenseViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var clickable = false
        var gson = Gson()
        var startActivity = false

        fun bind(senseLoad: AuraSenseConfigure, position: Int) {

            if (senseLoad.auraSenseName == "Humidity") {
                itemView.text_display.text = if (senseLoad.intensity != null) {
                    senseLoad.intensity
                } else {
                    Constant.HUMIDITY
                }
                itemView.scenesTitle.text = senseLoad.auraSenseName
                itemView.sense_icon.setImageResource(Utils.getSenseLoadsImages(senseLoad.auraSenseIndex))

            } else if (senseLoad.auraSenseName == "Temperature") {
                itemView.text_display.text = if (senseLoad.intensity != null) {
                    senseLoad.intensity
                } else {
                    Constant.TEMP
                }
                itemView.scenesTitle.text = senseLoad.auraSenseName
                itemView.sense_icon.setImageResource(Utils.getSenseLoadsImages(senseLoad.auraSenseIndex))
            } else if (senseLoad.auraSenseName == "Light Intensity") {
                itemView.text_display.text = if (senseLoad.intensity != null) {
                    senseLoad.intensity
                } else {
                    Constant.LUX
                }
                itemView.scenesTitle.text = senseLoad.auraSenseName
                itemView.sense_icon.setImageResource(Utils.getSenseLoadsImages(senseLoad.auraSenseIndex))
            } else if (senseLoad.auraSenseName == "Motion") {
                itemView.scenesTitle.text = senseLoad.auraSenseName
                if (senseLoad.intensity == "1") {
                    itemView.sense_icon.setImageResource(R.drawable.ic_motion_triggered)
                } else {
                    itemView.sense_icon.setImageResource(R.drawable.ic_motion_idle)
                }
                itemView.text_display.visibility = View.INVISIBLE

            } else if (senseLoad.auraSenseName == "IR Blaster") {
                itemView.scenesTitle.text = itemView.context.getString(R.string.add_remote)
                itemView.sense_icon.setImageResource(R.drawable.svg_blue_plus)
                itemView.text_display.visibility = View.INVISIBLE
                clickable = true

            }

            itemView.setOnClickListener {
                if (senseLoad.auraSenseName == "IR Blaster") {
                    for (s in senseDeviceList) {
                        if (s.aura_sence_name == senseLoad.senseDeviceName) {
                            remoteData.auraSenseName = senseLoad.senseDeviceName
                            remoteData.senseIp = s.sense_ip
                            remoteData.senseUiud = s.sense_uiud
                            remoteData.remoteLocation = s.room
                            remoteData.home = s.home
                            remoteData.senseThing = s.scense_thing
                            startActivity = true
                            break
                        }
                    }
                    if (startActivity) {
                        val intent = Intent(itemView.context, DownloadRemoteActivity::class.java)
                        intent.putExtra("REMOTE_DATA", Gson().toJson(remoteData))
                        itemView.context.startActivity(intent)
                    }
                }
            }

        }

    }

}