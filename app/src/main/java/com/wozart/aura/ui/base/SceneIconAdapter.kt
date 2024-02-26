package com.wozart.aura.ui.base

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.wozart.aura.R
import com.wozart.aura.entity.model.scene.Scene
import kotlinx.android.synthetic.main.item_icon.view.*
import com.wozart.aura.aura.utilities.Utils.getSceneTestDrawable

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 19/08/19
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class SceneIconAdapter(var sceneList: ArrayList<Scene>, var listner: (Int, Boolean) -> Unit) : androidx.recyclerview.widget.RecyclerView.Adapter<SceneIconAdapter.IconHolder>() {

    private var drawables: MutableList<Int> = ArrayList()
    private var selectedPos = 0


    fun init(drawables: MutableList<Int>, posNumber: Int) {
        this.drawables = drawables
        this.selectedPos = posNumber
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): IconHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_icon, p0, false)
        return SceneIconAdapter.IconHolder(view)
    }

    override fun getItemCount(): Int {
        return this.drawables.size
    }

    override fun onBindViewHolder(p0: IconHolder, p1: Int) {
        val drawable = drawables[p1]

        p0.itemView.imgThumbnail.setImageDrawable(ContextCompat.getDrawable(p0.itemView.context, drawable))
        if (sceneList[p1].isSceneSelected) {
            p0.itemView.scene_card.setCardBackgroundColor(ContextCompat.getColor(p0.itemView.context, R.color.white))
            p0.itemView.imgThumbnail.setImageResource(getSceneTestDrawable(p1, sceneList[p1].isSceneSelected))
            p0.itemView.background = p0.itemView.context.getDrawable(R.drawable.image_border_selected)
        } else {
            p0.itemView.scene_card.setCardBackgroundColor(ContextCompat.getColor(p0.itemView.context, R.color.list_item_inactive))
            p0.itemView.imgThumbnail.setImageResource(getSceneTestDrawable(p1, sceneList[p1].isSceneSelected))
            p0.itemView.background = p0.itemView.context.getDrawable(R.drawable.image_border)
        }


        p0.itemView.setOnClickListener {
            val zoomin = AnimationUtils.loadAnimation(p0.itemView.context, R.anim.zoomin)
            val zoomout = AnimationUtils.loadAnimation(p0.itemView.context, R.anim.zoomout)
            val prevPos = selectedPos
            selectedPos = p1
            selectedIcon(selectedPos)
            p0.itemView.scene_card.startAnimation(zoomin)
            p0.itemView.scene_card.startAnimation(zoomout)
            listner(selectedPos, false)
            if (prevPos != -1)
                notifyItemChanged(prevPos)
            notifyItemChanged(selectedPos)
        }
    }

    fun selectedIcon(position: Int) {
        for (i in sceneList.indices) {
            sceneList[i].isSceneSelected = i == position
        }
    }

    class IconHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

    }
}