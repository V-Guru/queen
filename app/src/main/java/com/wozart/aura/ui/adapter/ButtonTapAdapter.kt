package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.entity.model.aura.AuraLongSingleDouble
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.item_button_press.view.*


/**
 * Created by Saif on 10/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class ButtonTapAdapter(var context: Context, var onClick: RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<ButtonTapAdapter.ButtonHolder>() {

    var buttonTapList: MutableList<AuraLongSingleDouble> = ArrayList()
    var buttonModel: MutableList<ButtonModel> = ArrayList()

    fun setData(list: MutableList<AuraLongSingleDouble>, databasebuttonModel: MutableList<ButtonModel>) {
        this.buttonTapList.clear()
        this.buttonTapList.addAll(list)
        this.buttonModel = databasebuttonModel
        notifyDataSetChanged()
    }

    inner class ButtonHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(buttonTap: AuraLongSingleDouble) {
            itemView.deviceName.text = buttonTap.btnName
            if (buttonModel.size > 0) {
                for (button in buttonModel) {
                    if (button.buttonTapName == buttonTap.btnName) {
                        if (button.buttonId == Constant.BTN1_SINGL_TAP || button.buttonId == Constant.BTN1_DOUBL_TAP || button.buttonId == Constant.BTN1_LONG_TAP) {
                            for (scenes in button.load) {
                                for (l in scenes.deviceList) {
                                    itemView.device_assigned.setTextColor(context.resources.getColor(R.color.colorAccent))
                                    itemView.device_assigned.text = l.name
                                }
                            }
                        }
                        break
                    }
                }
            }
        }

        init {
            itemView.cardView.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition, buttonTapList[adapterPosition], R.id.cardView)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ButtonHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_button_press, p0, false)
        return ButtonHolder(rootView)
    }

    override fun getItemCount(): Int {
        return buttonTapList.size
    }

    override fun onBindViewHolder(p0: ButtonHolder, p1: Int) {
        p0.bind(buttonTapList[p1])
    }
}