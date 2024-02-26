package com.wozart.aura.aura.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.AwsSwitch
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.utilities.DialogListener
import kotlinx.android.synthetic.main.item_aws_connect_switch_list.view.*

class AwsConnectAdapter() : androidx.recyclerview.widget.RecyclerView.Adapter<AwsConnectAdapter.ViewHolder>() {

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        var switch = auraSwitches.get(p1)
        p0?.itemView?.tvName?.text = switch.switchName
        if (auraSwitches.get(p1).isEnabled) {
            p0?.itemView?.device_status?.visibility = View.VISIBLE
            p0?.itemView?.awsSwitch?.visibility = View.GONE
        } else {
            p0?.itemView?.device_status?.visibility = View.GONE
            p0?.itemView?.awsSwitch?.visibility = View.VISIBLE
        }
        p0?.itemView?.awsSwitch?.setOnClickListener {
            Utils.showCustomDialog(p0.itemView.context, p0.itemView.context.getString(R.string.title_aws_connect), p0.itemView.context.getString(R.string.aws_connect_dialog_message), R.layout.dialog_layout_aws_connect, object : DialogListener {
                override fun onOkClicked() {
                    auraSwitches.get(p1).isEnabled = true
                    p0?.itemView?.device_status?.visibility = View.VISIBLE
                    p0?.itemView?.awsSwitch?.visibility = View.GONE
                }

                override fun onCancelClicked() {
                    auraSwitches.get(p1).isEnabled = false
                    p0?.itemView?.awsSwitch?.visibility = View.VISIBLE


                }

            })
        }

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        var view = LayoutInflater.from(p0?.context).inflate(R.layout.item_aws_connect_switch_list, p0, false)
        var viewHolder = ViewHolder(view);
        return viewHolder;
    }

    var auraSwitches: MutableList<AwsSwitch> = ArrayList()

    fun init(auraSwitches: MutableList<AwsSwitch>) {
        this.auraSwitches = auraSwitches
    }

    fun update(auraSwitches: MutableList<AwsSwitch>) {
        this.auraSwitches = auraSwitches
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = auraSwitches.size


    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

}