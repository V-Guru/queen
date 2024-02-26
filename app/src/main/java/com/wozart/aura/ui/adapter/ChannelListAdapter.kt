package com.wozart.aura.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.item_channel.view.*


/**
 * Created by Saif on 01/09/20.
 * EZJobs
 * mdsaif@onata.com
 */
class ChannelListAdapter(var context: Context, var onClick: RecyclerItemClicked) : RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder>() {

    var remoteList: MutableList<RemoteIconModel> = ArrayList()
    var viewType: String = Constant.INTERNET_CHANNEL

    fun setData(list: MutableList<RemoteIconModel>) {
        this.remoteList.clear()
        this.remoteList.addAll(list)

        notifyDataSetChanged()
    }


    var channelShortcutList: MutableList<RemoteIconModel>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    open inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        inner class InternetChannelViewHolder : ChannelViewHolder(itemView) {
            fun bind(remoteIconModel: RemoteIconModel) {
                //itemView.channelName.text = remoteIconModel.remoteButtonName
                itemView.channelImage.setImageResource(Utils.getRemoteIconByName(remoteIconModel.remoteButtonName?:""))
            }

            init {
                itemView.cardView.setOnLongClickListener {
                    if (adapterPosition != -1 && remoteList.size > 0) {
                        onClick.onRecyclerItemClicked(adapterPosition, remoteList[adapterPosition], Constant.INTERNET_CHANNEL_LEARN)
                    }
                    return@setOnLongClickListener true
                }
                itemView.cardView.setOnClickListener {
                    if (adapterPosition != -1 && remoteList.size > 0) {
                        onClick.onRecyclerItemClicked(adapterPosition, remoteList[adapterPosition], Constant.INTERNET_CHANNEL)
                    }
                }
            }

        }

        inner class ChannelShortcutViewHolder : ChannelViewHolder(itemView) {
            fun bind(remoteIconModel: RemoteIconModel) {
                itemView.channelName.text = remoteIconModel.remoteButtonName
                itemView.tvChannelNumber.text = remoteIconModel.channelNumber
                //itemView.channelImage.setImageResource(Utils.getRemoteIconByName(remoteIconModel.remoteButtonName!!))
            }

            init {
                itemView.cardView.setOnClickListener {
                    if (adapterPosition != -1 && channelShortcutList!!.size > 0) {
                        onClick.onRecyclerItemClicked(adapterPosition, channelShortcutList!![adapterPosition], Constant.CHANEL_CREATED)
                    }
                }
                itemView.cardView.setOnLongClickListener {
                    if (adapterPosition != -1 && channelShortcutList!!.size > 0) {
                        onClick.onRecyclerItemClicked(adapterPosition, channelShortcutList!![adapterPosition], Constant.CHANNEL_ADDED)
                    }
                    return@setOnLongClickListener true
                }
            }

        }

    }


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ChannelViewHolder {
        return when (viewType) {
            Constant.INTERNET_CHANNEL -> {
                val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_channel, p0, false)
                ChannelViewHolder(rootView).InternetChannelViewHolder()
            }
            Constant.CHANNEL_SHORTCUT -> {
                val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_channel, p0, false)
                ChannelViewHolder(rootView).ChannelShortcutViewHolder()
            }
            else -> {
                val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_channel, p0, false)
                ChannelViewHolder(rootView).InternetChannelViewHolder()
            }
        }

    }

    override fun getItemCount(): Int {
        return (if (viewType == Constant.INTERNET_CHANNEL) {
            remoteList.size ?: 0
        } else {
            channelShortcutList!!.size ?: 0
        })
    }


    override fun onBindViewHolder(p0: ChannelViewHolder, p1: Int) {
        when (viewType) {
            Constant.INTERNET_CHANNEL -> {
                (p0 as ChannelViewHolder.InternetChannelViewHolder).bind(remoteList[p1])
            }
            Constant.CHANNEL_SHORTCUT -> {
                (p0 as ChannelViewHolder.ChannelShortcutViewHolder).bind(channelShortcutList?.get(p1)!!)
            }
        }
    }

}