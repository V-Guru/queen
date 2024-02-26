package com.wozart.aura.ui.auraSense

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.dummy_remote_layout.view.*
import kotlinx.android.synthetic.main.remote_list.view.*
import kotlinx.android.synthetic.main.remote_list.view.ac_temp_text

class FavoriteButtonAdapter(var context: Context, var onClick: RecyclerItemClicked) : androidx.recyclerview.widget.RecyclerView.Adapter<FavoriteButtonAdapter.FavoriteViewHolder>() {

    var btnList: MutableList<RemoteIconModel> = ArrayList()
    var showSelection : Boolean = false

    fun setData(list: MutableList<RemoteIconModel>) {
        this.btnList.clear()
        this.btnList.addAll(list)
        notifyDataSetChanged()

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    inner class FavoriteViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(remoteIconModel: RemoteIconModel) {
            if (remoteIconModel.remoteButtonName.equals("16") || remoteIconModel.remoteButtonName.equals("17") ||
                    remoteIconModel.remoteButtonName.equals("18") || remoteIconModel.remoteButtonName.equals("19") ||
                    remoteIconModel.remoteButtonName.equals("20") || remoteIconModel.remoteButtonName.equals("21") ||
                    remoteIconModel.remoteButtonName.equals("22") || remoteIconModel.remoteButtonName.equals("23") ||
                    remoteIconModel.remoteButtonName.equals("24") || remoteIconModel.remoteButtonName.equals("25") ||
                    remoteIconModel.remoteButtonName.equals("26") || remoteIconModel.remoteButtonName.equals("27") ||
                    remoteIconModel.remoteButtonName.equals("28") || remoteIconModel.remoteButtonName.equals("29") ||
                    remoteIconModel.remoteButtonName.equals("30")){

                itemView.ac_temp_text.visibility = View.VISIBLE
                itemView.ac_temp_text.text = remoteIconModel.remoteButtonName
                itemView.appliance_image.visibility = View.GONE
            } else if(remoteIconModel.remoteButtonName == "ON" || remoteIconModel.remoteButtonName == "KEY_1" ||
                    remoteIconModel.remoteButtonName == "KEY_2" || remoteIconModel.remoteButtonName == "KEY_3" || remoteIconModel.remoteButtonName == "KEY_4" ||
                    remoteIconModel.remoteButtonName == "KEY_5" || remoteIconModel.remoteButtonName == "KEY_6" || remoteIconModel.remoteButtonName == "KEY_7" ||
                    remoteIconModel.remoteButtonName == "KEY_8" || remoteIconModel.remoteButtonName == "KEY_9" || remoteIconModel.remoteButtonName == "KEY_0"){
                itemView.ac_temp_text.visibility = View.VISIBLE
                itemView.ac_temp_text.text = Utils.getButtonText(context, remoteIconModel.remoteButtonName!!)
                itemView.appliance_image.visibility = View.GONE
            } else if(!remoteIconModel.channelNumber.isEmpty()){
                itemView.ac_temp_text.visibility = View.VISIBLE
                itemView.ac_temp_text.text = remoteIconModel.channelNumber
                itemView.appliance_image.visibility = View.GONE
            } else {
                itemView.ac_temp_text.visibility = View.GONE
                itemView.appliance_image.visibility = View.VISIBLE
                itemView.appliance_image.setImageResource(Utils.getRemoteIconByName(remoteIconModel.remoteButtonName!!))
            }
            //itemView.brand_name.text = remoteIconModel.remoteButtonName

        }

        init {
            itemView.remote.setOnClickListener {
                btnList[adapterPosition].isSelected = !btnList[adapterPosition].isSelected
                if(!btnList[adapterPosition].isSelected && showSelection){
                    itemView.layout_remote.background = itemView.context.getDrawable(R.drawable.circle_out_bg)
                }else{
                    itemView.layout_remote.background = itemView.context.getDrawable(R.drawable.circle_no_theme)
                }
                onClick.onRecyclerItemClicked(adapterPosition, btnList[adapterPosition], Constant.REMOTE)
            }
            itemView.remote.setOnLongClickListener {
                onClick.onRecyclerItemClicked(adapterPosition, btnList[adapterPosition], Constant.REMOTE_DELETE)
                true
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): FavoriteViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.remote_list, p0, false)
        return FavoriteViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return btnList.size
    }

    override fun onBindViewHolder(p0: FavoriteViewHolder, p1: Int) {
        p0.bind(btnList[p1])
    }
}