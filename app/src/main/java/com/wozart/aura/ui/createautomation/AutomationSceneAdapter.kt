package com.wozart.aura.ui.createautomation


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.dialogue_edit_home.*
import kotlinx.android.synthetic.main.item_automation_scene.view.*
import java.util.HashMap

class AutomationSceneAdapter(var automationScene: ArrayList<AutomationScene>, var onClick: RecyclerItemClicked) : RecyclerView.Adapter<AutomationSceneAdapter.AutomationHolder>() {

    var userType: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutomationHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_automation_scene, parent, false)
        return AutomationHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return automationScene.size

    }

    override fun onBindViewHolder(holder: AutomationHolder, position: Int) {

        holder.bind(position, automationScene[position])
        if (automationScene[position].property[0].AutomationEnable) {
            holder.itemView.card_scene_auto.setCardBackgroundColor(holder.itemView.resources.getColor(R.color.white))
        } else {
            if ((automationScene[position].type == "geo") || (automationScene[position].type == "motion")) {
                holder.itemView.scheduleRoutines.visibility = View.GONE
                holder.itemView.card_scene_auto.setCardBackgroundColor(holder.itemView.resources.getColor(R.color.list_item_inactive))
            } else {
                holder.itemView.card_scene_auto.setCardBackgroundColor(holder.itemView.resources.getColor(R.color.list_item_inactive))
            }
        }
    }

    inner class AutomationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int, automatScene: AutomationScene) = with(itemView) {
            val zoomin = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomin)
            val zoomout = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomout)
            val scheduleTypeBased = automatScene.type
            if (automatScene.name!!.length > 15) {
                val ttl = automatScene.name!!.substring(0, 15)
                itemView.text_trigger.text = ttl + ".."
            } else {
                itemView.text_trigger.text = automatScene.name
            }
            if (scheduleTypeBased == "geo") {
                checkEnable(automatScene)
                itemView.scenesIconAuto.setImageResource(R.drawable.ic_address_icon_01)
                itemView.text_trigger.visibility = View.VISIBLE
                itemView.scheduleTimes.text = automatScene.property[0].triggerType
                itemView.scheduleRoutines.visibility = View.GONE

            } else if (scheduleTypeBased == "motion") {
                checkEnable(automatScene)
                val data = Common.getSenseData(automatScene.routine!!)
                itemView.scenesIconAuto.setImageResource(Utils.getMotionImages(data[0].auraSenseName!!))
//                itemView.text_trigger.visibility = View.VISIBLE
//                itemView.text_trigger.text = automatScene.name
                itemView.scheduleRoutines.visibility = View.GONE
                itemView.scheduleTimes.visibility = View.GONE
            } else {
                checkEnable(automatScene)
//                itemView.text_trigger.visibility = View.GONE
                if ((automatScene.time == "Sunrise") || (automatScene.time == "Sunset")) {
                    itemView.scheduleTimes.text = automatScene.endTime.toString()
                } else {
                    itemView.scheduleTimes.text = automatScene.endTime
                }
                itemView.scheduleRoutines.visibility = View.VISIBLE
                itemView.scenesIconAuto.setImageResource(R.drawable.ic_clock_icon_new)
                val type = object : TypeToken<HashMap<String, Boolean>>() {}.type
                val gson = Gson()
                val routine: HashMap<String, Boolean>
                routine = gson.fromJson(automatScene.routine, type)
                changeTvBg(tvSunday, routine["Sunday"]!!)
                changeTvBg(tvMonday, routine["Monday"]!!)
                changeTvBg(tvTuesday, routine["Tuesday"]!!)
                changeTvBg(tvWednesday, routine["Wednesday"]!!)
                changeTvBg(tvThursday, routine["Thursday"]!!)
                changeTvBg(tvFriday, routine["Friday"]!!)
                changeTvBg(tvSaturday, routine["Saturday"]!!)
            }

            itemView.setOnClickListener {
                if (userType) {
                    itemView.card_scene_auto.startAnimation(zoomin)
                    itemView.card_scene_auto.startAnimation(zoomout)
                    automationScene[adapterPosition].property[0].AutomationEnable = !automationScene[adapterPosition].property[0].AutomationEnable
                    onClick.onRecyclerItemClicked(adapterPosition, automationScene[adapterPosition], Constant.AUTOMATION_ENABLE)
                    enableEffect(automatScene)
                    notifyItemChanged(adapterPosition)
                } else {
                    onClick.onRecyclerItemClicked(adapterPosition, automationScene[adapterPosition], Constant.ERROR_SHOW)
                }

            }
            itemView.setOnLongClickListener {
                if (userType) {
                    dialogEditSchedule(automatScene)
                } else {
                    onClick.onRecyclerItemClicked(adapterPosition, automationScene[adapterPosition], Constant.ERROR_SHOW)
                }
                true
            }

        }

        private fun checkEnable(automatScene: AutomationScene) {
            if (automatScene.property[0].AutomationEnable) {
                itemView.card_scene_auto.setBackgroundResource(R.drawable.card_shade_white)
            } else {
                itemView.card_scene_auto.setBackgroundResource(R.drawable.card_shade_gray)
            }
        }

        private fun enableEffect(automatScene: AutomationScene) {
            if (automatScene.property[0].AutomationEnable) {
                Toast.makeText(itemView.context, "Automation Enabled", Toast.LENGTH_SHORT).show()
                itemView.card_scene_auto.setBackgroundResource(R.drawable.card_shade_white)
            } else {
                Toast.makeText(itemView.context, "Automation Disabled", Toast.LENGTH_SHORT).show()
                itemView.card_scene_auto.setBackgroundResource(R.drawable.card_shade_gray)
            }
        }

        private fun changeTvBg(textView: TextView, selected: Boolean) {
            itemView.let {
                if (selected) {
                    textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorAccent))
                } else {
                    textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                }
            }
        }

        private fun dialogEditSchedule(automationScenes: AutomationScene) {
            val dialog = Dialog(itemView.context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialogue_edit_home)
            dialog.tv_title.text = itemView.context.getString(R.string.edit_automation_title)

            dialog.btn_edit.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition, automationScenes, Constant.AUTOMATION_EDIT)
                notifyItemChanged(adapterPosition)
                dialog.dismiss()
            }

            dialog.btn_delete_home.setOnClickListener {
                if (adapterPosition != -1 && adapterPosition >= 0) {
                    onClick.onRecyclerItemClicked(adapterPosition, automationScenes, Constant.AUTOMATION_DELETE)
                    automationScene.remove(automationScene.find { scene -> scene.name == automationScenes.name })
                }
                dialog.dismiss()
            }
            dialog.show()
        }
    }
}

