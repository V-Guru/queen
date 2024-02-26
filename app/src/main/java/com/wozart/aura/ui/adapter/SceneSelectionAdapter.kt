package com.wozart.aura.ui.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.transition.Scene
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.ui.dashboard.Scenes
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.item_scene_automation.view.*


class SceneSelectionAdapter(context: Context, var onClick: RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<SceneSelectionAdapter.SceneViewHolder>() {

    var sceneList: MutableList<Scenes> = ArrayList()

    fun dataSet(listScene: MutableList<Scenes>) {
        this.sceneList.clear()
        this.sceneList.addAll(listScene)
        notifyDataSetChanged()
    }

    inner class SceneViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(scene: Scenes) {
            val drawables = arrayListOf(R.drawable.ic_good_morning_off,
                    R.drawable.ic_good_night_off, R.drawable.ic_enter_off, R.drawable.ic_exit_off, R.drawable.ic_party_off, R.drawable.ic_reading_off, R.drawable.ic_movie_off)
            val zoomin = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomin)
            val zoomout = AnimationUtils.loadAnimation(itemView.context, R.anim.click_anim)
            itemView.scenesTitle.text = scene.title
            itemView.imgSelect?.visibility = View.VISIBLE
            itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(scene.isOn))
            itemView.setOnClickListener {
                itemView.card_scene.animation = zoomout
//                for(sense in 0 until sceneList.size){
//                    if(sceneList[sense].isOn){
//                        sceneList[sense].isOn = false
//                        notifyItemChanged(sense)
//                        break
//                    }
//                }
                sceneList[adapterPosition].isOn = !sceneList[adapterPosition].isOn
                //itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(sceneList[adapterPosition].isOn))
                if (sceneList[adapterPosition].isOn) {
                    itemView.card_scene.setBackgroundResource(R.drawable.card_shade_white)
                    itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scene.iconUrl, scene.isOn))
                } else {
                    itemView.card_scene.setBackgroundResource(R.drawable.card_shade_gray)
                    itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scene.iconUrl, scene.isOn))
                }
                onClick.onRecyclerItemClicked(adapterPosition, sceneList[adapterPosition], sceneList[adapterPosition].isOn)
                notifyItemChanged(adapterPosition)
                //var flag = false
//                for (s in 0 until sceneList.size) {
//                    flag = false
//                    if (sceneList[s].isOn) {
//                        for (sDevice in sceneList[s].rooms) {
//                            for (checkDevice in sDevice.deviceList) {
//                                for (clickedScene in scene.rooms) {
//                                    for (c in clickedScene.deviceList) {
//                                        if ((c.deviceName == checkDevice.deviceName) && (c.index == checkDevice.index)) {
//                                            flag = true
//                                            break
//                                        }
//                                    }
//
//                                }
//                            }
//                        }
//                        if (flag) {
//                            sceneList[s].isOn = !sceneList[s].isOn
//                            itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(drawables.indexOf(scene.iconUrl), scene.isOn))
//                            itemView.card_scene.setBackgroundResource(R.drawable.card_shade_gray)
//                            itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(sceneList[s].isOn))
//                            onClick.onRecyclerItemClicked(s, sceneList[s], sceneList[s].isOn)
//                            notifyItemChanged(s)
//                        }
//                    }
//                }
//                if(!flag){
//                    sceneList[adapterPosition].isOn = !sceneList[adapterPosition].isOn
//                    itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(drawables.indexOf(scene.iconUrl), scene.isOn))
//                    itemView.card_scene.setBackgroundResource(R.drawable.card_shade_gray)
//                    itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(sceneList[adapterPosition].isOn))
//                    onClick.onRecyclerItemClicked(adapterPosition, sceneList[adapterPosition], sceneList[adapterPosition].isOn)
//                    //notifyItemChanged(adapterPosition)
//                }
            }

            //for (i in 0 until drawables.size) {
               // val icon = drawables[i]
                //var icon = Utils.getSceneDrawable(i,scenes.isOn)
               // if (icon == scene.iconUrl) {
                    if (scene.isOn) {
                        itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scene.iconUrl, scene.isOn))
                        itemView.card_scene.setBackgroundResource(R.drawable.card_shade_white)
                        itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(sceneList[adapterPosition].isOn))
                    } else {
                        itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scene.iconUrl, scene.isOn))
                        itemView.card_scene.setBackgroundResource(R.drawable.card_shade_gray)
                        itemView.imgSelect?.background = ContextCompat.getDrawable(itemView.context, getDrawableId(sceneList[adapterPosition].isOn))
                    }
                //}
            //}
        }

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SceneViewHolder {
        return SceneViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_scene_automation, p0, false))
    }

    override fun getItemCount(): Int {
        return sceneList.size
    }

    override fun onBindViewHolder(p0: SceneViewHolder, p1: Int) {
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