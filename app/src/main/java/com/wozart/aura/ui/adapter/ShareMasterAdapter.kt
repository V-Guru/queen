package com.wozart.aura.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.share_guest_layout.view.*


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 14/09/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/


class ShareMasterAdapter(var context: Context, var onClick: RecyclerItemClicked, val shareList: MutableList<MutableMap<String, String>>) : RecyclerView.Adapter<ShareMasterAdapter.ShareViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareMasterAdapter.ShareViewHolder {

        val rootView = LayoutInflater.from(parent.context)
                .inflate(R.layout.share_guest_layout, parent, false)

        return ShareViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return shareList.size
    }

    override fun onBindViewHolder(holder: ShareMasterAdapter.ShareViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.bind(position, shareList)
    }

    inner class ShareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int, shareList: MutableList<MutableMap<String, String>>) {
            if (shareList[position]["Home"]!!.contains(Constant.HOME!!)) {
                itemView.tv_contact_name.text = shareList[position]["Name"]
            } else {
                itemView.tv_contact_name.visibility = View.GONE
            }
            itemView.layout_guest_access?.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition, shareList[adapterPosition], Constant.REVOKE_ACCESS)
            }
        }
    }
}