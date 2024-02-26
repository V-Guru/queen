package com.wozart.aura.ui.adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.wozart.aura.R
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.ui.auraSense.RemoteListModel
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.dialogue_edit_home.*
import kotlinx.android.synthetic.main.item_sense_list.view.*


/**
 * Created by Saif on 10/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class ButtonDeviceAdapter(var context: Context,var onClick : RecyclerItemClicked) : RecyclerView.Adapter<ButtonDeviceAdapter.ButtonViewHolder>() {

    var buttonDeviceList: MutableList<ButtonModel> = ArrayList()

    fun setData(list: MutableList<ButtonModel>) {
        this.buttonDeviceList.clear()
        this.buttonDeviceList.addAll(list)
    }

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(buttonModel: ButtonModel) {
            itemView.ivSelect.visibility = View.GONE
            itemView.deviceName.text = buttonModel.auraButtonName
            itemView.appliance_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_switch_off_new))
        }
        init {
            itemView.cardView.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition,buttonDeviceList[adapterPosition],Constant.SET_BUTTON_ACTION)
            }
            itemView.cardView.setOnLongClickListener {
                showDialog()
                true
            }
        }

        private fun showDialog() {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialogue_edit_home)
            dialog.tv_title.text = context.getString(R.string.delete_button)
            dialog.btn_edit.visibility = View.GONE
            dialog.btn_delete_home.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition,buttonDeviceList[adapterPosition], Constant.BUTTON_DELETE)
                buttonDeviceList.removeAt(adapterPosition)
                notifyItemChanged(adapterPosition)
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ButtonViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_sense_list, p0, false)
        return ButtonViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return buttonDeviceList.size
    }

    override fun onBindViewHolder(p0: ButtonViewHolder, p1: Int) {
        p0.bind(buttonDeviceList[p1])
    }
}