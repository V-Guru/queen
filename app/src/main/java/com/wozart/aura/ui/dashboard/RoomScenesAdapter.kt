package com.wozart.aura.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.wozart.aura.R
import com.wozart.aura.aura.ui.dashboard.listener.OnOptionsListener
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.utilities.DialogListener
import kotlinx.android.synthetic.main.item_room_scenes.view.*
import kotlinx.android.synthetic.main.item_room_scenes.view.card_scene
import kotlinx.android.synthetic.main.item_room_scenes.view.rlScenes
import kotlinx.android.synthetic.main.item_room_scenes.view.scenesIcon
import kotlinx.android.synthetic.main.item_room_scenes.view.scenesTitle
import kotlinx.android.synthetic.main.item_scenes.view.*

/***
 * Created by Saif on 14-03-2018.
 */
class RoomScenesAdapter(val scenesList: ArrayList<Scenes>, val listener: (Scenes, Boolean) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<RoomScenesAdapter.ScenesHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScenesHolder {
        val inflatedView = LayoutInflater.from(parent?.context).inflate(R.layout.item_room_scenes, parent, false)
        return ScenesHolder(inflatedView);
    }

    override fun onBindViewHolder(holder: ScenesHolder, position: Int) {
        holder?.bind(position, scenesList[position], listener);
    }


    override fun getItemCount(): Int {
        return scenesList.size
    }

    class ScenesHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int, scenes: Scenes, listener: (Scenes, Boolean) -> Unit) = with(itemView) {
            val zoomin = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomin)
            val zoomout = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomout)
            itemView.scenesTitle.text = scenes.title
            // itemView.scenesIconRoom.setImageResource(scenes.iconUrl)
            itemView.setOnClickListener {
                itemView.card_scene.startAnimation(zoomin)
                itemView.card_scene.startAnimation(zoomout)
                listener(scenes, false)
            }
            itemView.setOnLongClickListener {
                listener(scenes, true)
                true
            }

            if (scenes.isOn) {
                itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scenes.iconUrl, scenes.isOn))
                itemView.rlScenes.setBackgroundResource(R.drawable.image_border_selected)
            } else {
                itemView.scenesIcon.setImageResource(Utils.getSceneDrawable(scenes.iconUrl, scenes.isOn))
                itemView.rlScenes.setBackgroundResource(R.drawable.card_shade_gray)
            }
        }
    }


}