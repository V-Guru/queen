package com.wozart.aura.ui.thirdParty

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.ui.auraSense.DownloadRemoteActivity
import com.wozart.aura.ui.auraSense.RemoteListModel
import kotlinx.android.synthetic.main.zmote_device_list.view.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-03-04
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class ZmoteDeviceAdapter(context:Context,var list_zmote : MutableList<IRDeviceModal>) : androidx.recyclerview.widget.RecyclerView.Adapter<ZmoteDeviceAdapter.ZmoteViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ZmoteViewHolder {
      var rootView = LayoutInflater.from(p0.context).inflate(R.layout.zmote_device_list,p0,false)
        return ZmoteViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return list_zmote.size
    }

    override fun onBindViewHolder(p0: ZmoteViewHolder, p1: Int) {
       p0.bind(list_zmote[p1])
    }

    inner class ZmoteViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){

        fun bind(ir : IRDeviceModal){
            itemView.zmote_name.text= ir.IrName
            itemView.device_id.text = ir.zchipId
            itemView.deviceStatus.text = ir.zstate

            itemView.setOnClickListener {
                val remote = RemoteListModel()
                remote.auraSenseName = ir.zchipId
                remote.zmote_ip = ir.zipCoonnect

                val intent = Intent(itemView.context, DownloadRemoteActivity::class.java)
                intent.putExtra("REMOTE_DATA",Gson().toJson(remote))
                itemView.context.startActivity(intent)
            }
        }

    }
}