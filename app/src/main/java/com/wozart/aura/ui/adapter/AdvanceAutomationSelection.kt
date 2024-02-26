package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_advance_automation_list.view.*

class AdvanceAutomationSelection(context: Context,var onClick: RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<AdvanceAutomationSelection.AdvanceViewHolder>() {

    var automationList: MutableList<AutomationScene> = ArrayList()
    var type : Int = -1
    fun setData(list : MutableList<AutomationScene>,type: Int){
        this.automationList.clear()
        this.automationList.addAll(list)
        this.type = type
        notifyDataSetChanged()
    }
   inner class AdvanceViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bind(automation : AutomationScene){
            itemView.checkedAutomation.text = automation.name
            itemView.switchToEnable.text = automation.name
            itemView.checkedAutomation.isChecked = automation.isSelected
            itemView.switchToEnable.isChecked = automation.property[0].AutomationEnable
        }

       init {
           if(type == 0){
               itemView.checkedAutomation.visibility = View.VISIBLE
               itemView.switchToEnable.visibility = View.GONE
           }else{
               itemView.switchToEnable.visibility = View.VISIBLE
               itemView.checkedAutomation.visibility = View.GONE
           }

           itemView.checkedAutomation.setOnClickListener {
               automationList[adapterPosition].isSelected = !automationList[adapterPosition].isSelected
               onClick.onRecyclerItemClicked(adapterPosition,automationList[adapterPosition],automationList[adapterPosition].isSelected)
           }

           itemView.switchToEnable.setOnClickListener {
               automationList[adapterPosition].property[0].AutomationEnable = !automationList[adapterPosition].property[0].AutomationEnable
               onClick.onRecyclerItemClicked(adapterPosition,automationList[adapterPosition],automationList[adapterPosition].property[0].AutomationEnable)
           }
       }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AdvanceViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_advance_automation_list,p0,false)
        return AdvanceViewHolder(rootView)
    }

    override fun getItemCount(): Int {
       return automationList.size
    }

    override fun onBindViewHolder(p0: AdvanceViewHolder, p1: Int) {
        p0.bind(automationList[p1])
    }

}