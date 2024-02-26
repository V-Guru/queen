package com.wozart.aura.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.ScenesAdapter
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.model.CameraModel
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.item_scenes.view.*


/**
 * Created by Saif on 25/02/21.
 * mds71964@gmail.com
 */
class CameraDisplayAdapter(var onClick:RecyclerItemClicked) : RecyclerView.Adapter<CameraDisplayAdapter.CameraViewHolder>() {

    var cameraList : MutableList<CameraModel> = ArrayList()
    fun setData(list : MutableList<CameraModel>){
        cameraList.clear()
        cameraList.addAll(list)
        notifyDataSetChanged()
    }

    inner class CameraViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        fun bind(cameraModel: CameraModel){
            itemView.scenesIcon.setImageResource(R.drawable.ic_menu_camera)
            itemView.scenesTitle.text = cameraModel.cameraName
        }

        init {
            itemView.rootView.setOnClickListener {
                onClick.onRecyclerItemClicked(adapterPosition,cameraList[adapterPosition],R.id.card_scene)
            }
            itemView.card_scene.setOnLongClickListener {
                onClick.onRecyclerItemClicked(adapterPosition,cameraList[adapterPosition],Constant.DELETE_CAMERA)
                true
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CameraViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_scenes, parent, false)
        return CameraViewHolder(inflatedView);
    }

    override fun getItemCount(): Int {
        return cameraList.size
    }

    override fun onBindViewHolder(holder: CameraViewHolder, position: Int) {
        holder.bind(cameraList[position])
    }
}