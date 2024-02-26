package com.wozart.aura.ui.auraSense

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import kotlinx.android.synthetic.main.dummy_remote_layout.view.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-02-03
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class RemoteButtonIconAdapter(var context: Context, private var icon_list: MutableList<RemoteIconModel>, var listner: RemoteButtonSelected) : androidx.recyclerview.widget.RecyclerView.Adapter<RemoteButtonIconAdapter.RemoteIconViewHolder>() {

    var mListner: RemoteButtonSelected = listner
    var btn_list: MutableList<RemoteIconModel> = ArrayList()

    fun setData(icon_list: MutableList<RemoteIconModel>) {
        this.btn_list.clear()
        this.btn_list.addAll(icon_list)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RemoteIconViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.dummy_remote_layout, p0, false)
        return RemoteIconViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return btn_list.size
    }

    override fun onBindViewHolder(p0: RemoteIconViewHolder, p1: Int) {
        val remote_icon = btn_list[p1]
        p0.bind(remote_icon, p1)

    }

    inner class RemoteIconViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var zoomout = AnimationUtils.loadAnimation(context, R.anim.zoomout)
        val zoomin = AnimationUtils.loadAnimation(itemView.context, R.anim.click_anim)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(remote_icon: RemoteIconModel, position: Int) = with(itemView) {
            if (remote_icon.remoteIconButton == -1) {
                itemView.btn_control.visibility = View.GONE
                itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_no_theme)
                if (remote_icon.remoteButtonName == "KEY_POWER") {
                    itemView.btn_control.visibility = View.VISIBLE
                    itemView.ac_temp_text!!.visibility = View.GONE
                    itemView.btn_control.setImageResource(Utils.getRemoteIconByName(remote_icon.remoteButtonName!!))
                } else {
                    itemView.ac_temp_text!!.visibility = View.VISIBLE
                    itemView.ac_temp_text!!.setText(remote_icon.remoteButtonName)
                }
            } else {
                itemView.btn_ac_control.visibility = View.GONE
                if (remote_icon.remoteButtonName == "KEY_ENTER") {
                    itemView.ac_temp_text!!.visibility = View.VISIBLE
                    itemView.btn_control.visibility = View.GONE
                    itemView.ac_temp_text.text = context.getString(R.string.enter)
                    if (remote_icon.remoteIsselected) itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_icon_button_selected)
                    else itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_button_diselect)
                } else if (remote_icon.remoteButtonName == "ChanList") {
                    itemView.ac_temp_text!!.visibility = View.VISIBLE
                    itemView.btn_control.visibility = View.GONE
                    itemView.ac_temp_text.text = context.getString(R.string.channel_list)
                    if (remote_icon.remoteIsselected) itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_icon_button_selected) else itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_button_diselect)
                } else if (remote_icon.remoteButtonName == "KEY_1" || remote_icon.remoteButtonName == "KEY_2" || remote_icon.remoteButtonName == "KEY_3" || remote_icon.remoteButtonName == "KEY_4" || remote_icon.remoteButtonName == "KEY_5" || remote_icon.remoteButtonName == "KEY_6" || remote_icon.remoteButtonName == "KEY_7" || remote_icon.remoteButtonName == "KEY_8" || remote_icon.remoteButtonName == "KEY_9" || remote_icon.remoteButtonName == "KEY_0") {
                    itemView.ac_temp_text!!.visibility = View.VISIBLE
                    itemView.btn_control.visibility = View.GONE
                    itemView.ac_temp_text.text = Utils.getButtonText(context, remote_icon.remoteButtonName!!)
                    itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_no_theme)
                } else if (remote_icon.remoteButtonName == "Pre-Ch") {
                    itemView.ac_temp_text!!.visibility = View.VISIBLE
                    itemView.btn_control.visibility = View.GONE
                    itemView.ac_temp_text.text = context.getString(R.string.prev_channel)
                    if (remote_icon.remoteIsselected) itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_icon_button_selected) else itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_button_diselect)
                } else {
                    itemView.ac_temp_text!!.visibility = View.GONE
                    itemView.btn_control.visibility = View.VISIBLE
                    if (remote_icon.remoteIsselected) itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_out_bg) else itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_no_theme)
                    itemView.btn_control.setImageResource(Utils.getRemoteIconByName(remote_icon.remoteButtonName!!))
                }

            }

            itemView.btn_control.setOnClickListener {
                if (remote_icon.remoteIsselected) {
                    remote_icon.remoteIsselected = false
                    itemView.startAnimation(zoomout)
                    if (remote_icon.remoteButtonName == "KEY_ENTER" || remote_icon.remoteButtonName == "ChanList" || remote_icon.remoteButtonName == "Pre-Ch") {
                        itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_button_diselect)
                    } else {
                        itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_no_theme)
                    }
                    mListner.onButtonSelected(remote_icon, position)
                } else {
                    remote_icon.remoteIsselected = true
                    itemView.startAnimation(zoomin)
                    if (remote_icon.remoteButtonName == "KEY_ENTER" || remote_icon.remoteButtonName == "ChanList" || remote_icon.remoteButtonName == "Pre-Ch") {
                        itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_icon_button_selected)
                    } else {
                        itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_out_bg)
                    }
                    mListner.onButtonSelected(remote_icon, position)
                }
            }

            itemView.btn_ac_control.setOnClickListener {
                if (remote_icon.remoteIsselected) {
                    remote_icon.remoteIsselected = false
                    itemView.startAnimation(zoomout)
                    mListner.onButtonSelected(remote_icon, position)
                    itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_no_theme)
                } else {
                    remote_icon.remoteIsselected = true
                    itemView.startAnimation(zoomin)
                    mListner.onButtonSelected(remote_icon, position)
                    itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_out_bg)
                }
            }

            itemView.ac_temp_text!!.setOnClickListener {
                if (remote_icon.remoteIsselected) {
                    remote_icon.remoteIsselected = false
                    itemView.startAnimation(zoomout)
                    if (remote_icon.remoteButtonName == "KEY_ENTER" || remote_icon.remoteButtonName == "ChanList" || remote_icon.remoteButtonName == "Pre-Ch") {
                        itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_button_diselect)
                    } else {
                        itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_no_theme)
                    }
                    mListner.onButtonSelected(remote_icon, position)

                } else {
                    remote_icon.remoteIsselected = true
                    itemView.startAnimation(zoomin)
                    if (remote_icon.remoteButtonName == "KEY_ENTER" || remote_icon.remoteButtonName == "ChanList" || remote_icon.remoteButtonName == "Pre-Ch") {
                        itemView.rlImage.background = itemView.context.getDrawable(R.drawable.remote_icon_button_selected)
                    } else {
                        itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_out_bg)
                    }
                    mListner.onButtonSelected(remote_icon, position)

                }

            }

            setButtonView(remote_icon)

        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun setButtonView(remoteIcon: RemoteIconModel) {
            if (remoteIcon.remoteIsselected) {
                itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_out_bg)
            } else {
                itemView.rlImage.background = itemView.context.getDrawable(R.drawable.circle_no_theme)
            }
        }

    }

    interface RemoteButtonSelected {
        fun onButtonSelected(remote_icon: RemoteIconModel, position: Int)
    }
}