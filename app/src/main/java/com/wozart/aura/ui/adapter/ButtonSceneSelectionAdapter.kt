package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.item_scenes.view.*
import kotlinx.android.synthetic.main.item_scenes.view.card_scene
import kotlinx.android.synthetic.main.item_scenes.view.scenesIcon
import kotlinx.android.synthetic.main.item_scenes.view.scenesTitle


/**
 * Created by Saif on 20/08/20.
 * EZJobs
 * mdsaif@onata.com
 */
class ButtonSceneSelectionAdapter(var context: Context,var onClick:RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<ButtonSceneSelectionAdapter.ButtonSelectionHolder>() {


    var sceneList: MutableList<Scenes> = ArrayList()

    fun dataSet(listScene: MutableList<Scenes>) {
        this.sceneList.clear()
        this.sceneList.addAll(listScene)
        notifyDataSetChanged()
    }

    inner class ButtonSelectionHolder(itemView:View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){

        fun bind(scene: Scenes) {
            val zoomin = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomin)
            val zoomout = AnimationUtils.loadAnimation(itemView.context, R.anim.click_anim)
            itemView.scenesTitle.text = scene.title
            itemView.imgSelect?.visibility = View.VISIBLE
            //itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(scene.isOn))
            itemView.setOnClickListener {
                itemView.card_scene.animation = zoomout
                for(sense in 0 until sceneList.size){
                    if(sceneList[sense].isOn){
                        sceneList[sense].isOn = false
                        notifyItemChanged(sense)
                        break
                    }
                }
                sceneList[adapterPosition].isOn = !sceneList[adapterPosition].isOn
                itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(sceneList[adapterPosition].isOn))

                onClick.onRecyclerItemClicked(adapterPosition, sceneList[adapterPosition], sceneList[adapterPosition].isOn)
                notifyItemChanged(adapterPosition)
            }

            if (scene.isOn) {
                itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scene.iconUrl, scene.isOn))
                itemView.card_scene.setBackgroundResource(R.drawable.card_shade_white)
                itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(sceneList[adapterPosition].isOn))
            } else {
                itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scene.iconUrl, scene.isOn))
                itemView.card_scene.setBackgroundResource(R.drawable.card_shade_gray)
                itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(sceneList[adapterPosition].isOn))
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ButtonSelectionHolder {
        return ButtonSelectionHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_scene_automation, p0, false))
    }

    override fun getItemCount(): Int {
        return sceneList.size
    }

    override fun onBindViewHolder(p0: ButtonSelectionHolder, p1: Int) {
        p0.bind(sceneList[p1])
    }

    private fun getDrawableId(selected: Boolean): Int {
        if (selected) {
            return R.drawable.filled_circle
        } else {
            return R.drawable.ic_round_stroke
        }
    }
}