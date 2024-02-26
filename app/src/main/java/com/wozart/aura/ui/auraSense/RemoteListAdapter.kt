package com.wozart.aura.ui.auraSense

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.dialogue_edit_home.*
import kotlinx.android.synthetic.main.remote_list_display.view.*


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-01-31
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 *****************************************************************************/

class RemoteListAdapter(private var context: Context, var remote_list: MutableList<RemoteListModel>, var flagRemoteExist: Boolean, var onDelete: OnRemoteDelete) : androidx.recyclerview.widget.RecyclerView.Adapter<RemoteListAdapter.RemoteListHolder>() {

    var gson = Gson()
    val listner: OnRemoteDelete = onDelete

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RemoteListHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.remote_list_display, p0, false)
        return RemoteListHolder(rootView)
    }

    override fun getItemCount(): Int {
        return remote_list.size
    }

    override fun onBindViewHolder(p0: RemoteListHolder, p1: Int) {
        p0.bind(remote_list[p1], p1)
    }

    inner class RemoteListHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(remoteListModel: RemoteListModel, position: Int) {
            if (flagRemoteExist) {
                if (remoteListModel.typeAppliances == null) {
                    remoteListModel.typeAppliances = "TV"
                }
                itemView.appliance_image.setImageResource(Utils.getAppliancesImage(position, remoteListModel.typeAppliances!!))
                itemView.brand_name.text = remoteListModel.modelNumber
                itemView.remoteName.text = remoteListModel.remoteName?:""
            } else {
                itemView.remote.visibility = View.GONE
            }

            itemView.remote.setOnClickListener {
                if (remoteListModel.typeAppliances != "AC") {
                    val intent = Intent(context, RemoteCreateActivity::class.java)
                    remoteListModel.dataType = "rOld"
                    intent.putExtra("remote", gson.toJson(remoteListModel))
                    intent.putExtra(Constant.REMOTE, remoteListModel.typeAppliances + " " + "Remote")
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, SenseRemoteActivity::class.java)
                    remoteListModel.dataType = "rOld"
                    intent.putExtra("remote", gson.toJson(remoteListModel))
                    intent.putExtra(Constant.REMOTE, remoteListModel.typeAppliances + " " + "Remote")
                    context.startActivity(intent)
                }

            }

            itemView.setOnLongClickListener {
                showDialog(remoteListModel, position)
                true
            }

        }

    }

    fun showDialog(remoteListModel: RemoteListModel, position: Int) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialogue_edit_home)
        dialog.tv_title.text = context.getString(R.string.delte_remote)
        dialog.btn_edit.visibility = View.GONE
        dialog.btn_delete_home.setOnClickListener {
            listner.onRemoteDelete(remoteListModel, position)
            remote_list.removeAt(position)
            notifyItemChanged(position)
            dialog.dismiss()
        }
        dialog.show()
    }

    interface OnRemoteDelete {
        fun onRemoteDelete(remoteListModel: RemoteListModel, position: Int)
    }
}