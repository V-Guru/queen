package com.wozart.aura.aura.ui.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.More
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.item_more.view.*

/**
 * Created by Niranjan P on 3/14/2018.
 */
class MoreAdapter(val listener: OnAdapterInteractionListener) : androidx.recyclerview.widget.RecyclerView.Adapter<MoreAdapter.ViewHolder>() {
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        val more = moreList.get(p1)
        p0.itemView.tvMenu?.text = more.optionName
        if(more.url != null && !more.url.equals(""))
            p0.itemView.context?.let {
                p0.itemView.ivMenu.setImageDrawable(p0.itemView.context?.getDrawable(Utils.getImage(more.url,
                        p0.itemView.context)))
                //  Glide.with(holder.itemView.context).load(Utils.getImage(more.url,holder.itemView.context)).into(holder.itemView.ivMenu)
            }
        if(p1==2) {
            /*    holder?.itemView?.badge_notification?.visibility = View.VISIBLE*/
            if(Constant.NOTIFICATION_COUNT > 0) {
                p0.itemView.badge_notification?.visibility = View.GONE
                p0.itemView.badge_notification?.text = Constant.NOTIFICATION_COUNT.toString()
            }
        }
        else
            p0.itemView.badge_notification?.visibility = View.GONE

        p0.itemView.setOnClickListener {
            listener.onMenuOptionsSelected(p1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_more, parent, false)
        return ViewHolder(view)
    }

    var moreList: MutableList<More> = ArrayList()

    fun init(moreList: MutableList<More>) {
        this.moreList = moreList
    }

    override fun getItemCount(): Int = moreList.size

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface OnAdapterInteractionListener {
        fun onMenuOptionsSelected(position: Int)
    }

}