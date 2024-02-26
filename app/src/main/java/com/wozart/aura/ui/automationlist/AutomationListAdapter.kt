package com.wozart.aura.aura.ui.automationlist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.data.model.AutomationModel

import kotlinx.android.synthetic.main.item_automation.view.*

/**
 * Created by Niranjan P on 3/14/2018.
 */
class AutomationListAdapter(var listener: OnAutomationListInteractionListener?) : androidx.recyclerview.widget.RecyclerView.Adapter<AutomationListAdapter.ViewHolder>() {

    var automationList: MutableList<AutomationModel> = ArrayList()
    private var selectedPosition = -1
    fun init(automationList: MutableList<AutomationModel>) {
        this.automationList = automationList
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val automation = automationList.get(position)
        holder.itemView.layoutExtended?.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
        registerListeners(holder, position, automation)
    }

    private fun registerListeners(holder: ViewHolder, position: Int, automation: AutomationModel) {

        holder.itemView.setOnClickListener {
            onItemLongClick(position)
        }
        holder.itemView.layoutDetails?.setOnClickListener { listener?.onDetailsBtnClicked(automation) }
        holder.itemView.layoutDelete?.setOnClickListener { listener?.onDeleteBtnClicked(automation) }
    }


    private fun onItemLongClick(position: Int) {
        if (position == selectedPosition) {
            selectedPosition = -1
        } else {
            val oldSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldSelectedPosition)
        }
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int = automationList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_automation, parent, false)
        val viewHolder = ViewHolder(view);
        return viewHolder;
    }


    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}