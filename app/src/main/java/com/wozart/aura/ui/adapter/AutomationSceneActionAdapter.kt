package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.ui.dashboard.Scenes
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_scene_automation.view.*
import kotlinx.android.synthetic.main.item_sense_load.view.card_scene
import kotlinx.android.synthetic.main.item_sense_load.view.scenesTitle

class AutomationSceneActionAdapter(context:Context,var onclick:RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<AutomationSceneActionAdapter.SceneActionViewHolder>() {

    var listScene : MutableList<Scenes> = ArrayList()

    fun dataSet(list: MutableList<Scenes>){
        this.listScene.clear()
        this.listScene.addAll(list)
        notifyDataSetChanged()
    }

    inner class SceneActionViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bind(scenes: Scenes){
            val zoomin = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomin)
            val zoomout = AnimationUtils.loadAnimation(itemView.context, R.anim.click_anim)
            itemView.scenesTitle.text = scenes.title
            if(scenes.isOn){
                itemView.card_scene.setBackgroundResource(R.drawable.card_shade_white)
            }else{
                itemView.card_scene.setBackgroundResource(R.drawable.card_shade_gray)
            }
            itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scenes.iconUrl, scenes.isOn))

            itemView.setOnClickListener {
                scenes.isOn = !scenes.isOn
                itemView.card_scene.animation = zoomout
                itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scenes.iconUrl, scenes.isOn))
                if (scenes.isOn){
                    itemView.card_scene.setBackgroundResource(R.drawable.card_shade_white)
                }else{
                    itemView.card_scene.setBackgroundResource(R.drawable.card_shade_gray)
                }
                onclick.onRecyclerItemClicked(adapterPosition,scenes,scenes.isOn)
                notifyItemChanged(adapterPosition)
            }

        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SceneActionViewHolder {
        return (SceneActionViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_scene_automation,p0,false)))
    }

    override fun getItemCount(): Int {
        return listScene.size
    }

    override fun onBindViewHolder(p0: SceneActionViewHolder, p1: Int) {
        p0.bind(listScene[p1])
    }
}