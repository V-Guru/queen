package com.wozart.aura.ui.adapter

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pedro.vlc.VlcListener
import com.pedro.vlc.VlcVideoLibrary
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.model.CameraModel
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.item_camera_view.view.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Saif on 09/03/21.
 * mds71964@gmail.com
 */
class CameraViewAdapter(var context: Context,var onClick:RecyclerItemClicked) : RecyclerView.Adapter<CameraViewAdapter.CameraViewHolder>() {

    var listView: MutableList<CameraModel> = ArrayList()
    var vlcVideoLibrary: VlcVideoLibrary? = null
    fun setData(viewList: MutableList<CameraModel>) {
        this.listView.clear()
        this.listView.addAll(viewList)
        notifyDataSetChanged()
    }

    inner class CameraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), VlcListener,MediaPlayer.OnErrorListener{
        fun bind(view: CameraModel) {
            if(adapterPosition>-1 && listView.size >0)
                turnOnCamera(view)
        }

        init {
            itemView.closeCamera.setOnClickListener {
                if(adapterPosition>-1 && listView.size >0)
                    onClick.onRecyclerItemClicked(adapterPosition,listView[adapterPosition],Constant.VIDEO_STOPED)
            }

        }

        private fun turnOnCamera(cameraModel: CameraModel) {
            vlcVideoLibrary = VlcVideoLibrary(context, this, itemView.surfaceView)
            vlcVideoLibrary?.let { vlcVideoLibrary ->
                if (!vlcVideoLibrary.isPlaying) {
                    vlcVideoLibrary.play(cameraModel.url)
                } else {
                    vlcVideoLibrary.stop()
                }
            }

        }


        override fun onComplete() {

        }

        override fun onError() {
            vlcVideoLibrary?.stop()
        }

        override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CameraViewHolder {
        return CameraViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_camera_view, parent, false))
    }

    override fun getItemCount(): Int {
        return listView.size
    }

    override fun onBindViewHolder(holder: CameraViewHolder, position: Int) {
        holder.bind(listView[position])
    }
}
