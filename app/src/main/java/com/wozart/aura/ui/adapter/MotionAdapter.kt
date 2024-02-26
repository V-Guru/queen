package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.item_sense_load.view.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-03-19
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class MotionAdapter(var context: Context) : RecyclerView.Adapter<MotionAdapter.MotionViewHolder>() {

    var loadList: MutableList<AuraSenseConfigure> = arrayListOf()
    var onClick : RecyclerItemClicked ?= null

    fun setData(sense_load: MutableList<AuraSenseConfigure>) {
        this.loadList.clear()
        this.loadList.addAll(sense_load)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MotionViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_sense_load, p0, false)
        return MotionViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return loadList.size

    }

    override fun onBindViewHolder(p0: MotionViewHolder, p1: Int) {
        p0.bind(loadList[p1])

    }

    inner class MotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val zoomin = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomin)
        val zoomout = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomout)
        fun bind(senseLoad: AuraSenseConfigure) = with(itemView) {
            itemView.card_scene.visibility = View.VISIBLE
            if (senseLoad.auraSenseName == "Temperature") {
                itemView.text_display.text = Constant.TEMP
            }
            if (senseLoad.auraSenseName == "Humidity") {
                itemView.text_display.text = Constant.HUMIDITY
            }
            if (senseLoad.auraSenseName == "Lux") {
                itemView.text_display.text = Constant.LUX
            }
            if (senseLoad.auraSenseName == "Motion") {
                itemView.text_display.text = senseLoad.intensity
            }

            itemView.scenesTitle.text = senseLoad.auraSenseName
            itemView.ivSelect.background = ContextCompat.getDrawable(itemView.context, getDrawableId(senseLoad.isSelected))
            if (senseLoad.isSelected) {
                itemView.sense_icon.setImageResource(Utils.getSenseLoadsImages(senseLoad.auraSenseIndex))
                itemView.card_scene.setBackgroundResource(R.drawable.card_shade_white)
            } else {
                itemView.sense_icon.setImageResource(Utils.getSenseLoadsImages(senseLoad.auraSenseIndex))
                itemView.card_scene.setBackgroundResource(R.drawable.card_shade_gray)
            }

            itemView.setOnClickListener {
                itemView.card_scene.startAnimation(zoomout)
                itemView.card_scene.startAnimation(zoomin)
                if (senseLoad.isSelected) {
                    if (senseLoad.auraSenseName == "Motion") {
                        itemView.sense_icon.setImageResource(R.drawable.ic_motion_idle)
                        senseLoad.range = 0
                    }
                    senseLoad.isSelected = false
                    itemView.ivSelect.background = ContextCompat.getDrawable(itemView.context, getDrawableId(senseLoad.isSelected))
                    itemView.card_scene.setBackgroundResource(R.drawable.card_shade_gray)
                } else {
                    if (senseLoad.auraSenseName == "Motion") {
                        itemView.sense_icon.setImageResource(R.drawable.ic_motion_triggered)
                        senseLoad.range = 1
                    }
                    senseLoad.isSelected = true
                    itemView.ivSelect.background = ContextCompat.getDrawable(itemView.context, getDrawableId(senseLoad.isSelected))
                    itemView.card_scene.setBackgroundResource(R.drawable.card_shade_white)
                }
                onClick?.onRecyclerItemClicked(adapterPosition,senseLoad,R.id.card_scene)
            }
        }

        fun getDrawableId(selected: Boolean): Int {
            if (selected) {
                return R.drawable.filled_circle
            } else {
                return R.drawable.ic_round_stroke
            }
        }
    }
}