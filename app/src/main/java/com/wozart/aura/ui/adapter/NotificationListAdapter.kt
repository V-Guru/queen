package com.wozart.aura.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.model.Messages
import com.wozart.aura.data.model.Notification
import com.wozart.aura.utilities.DialogListener
import kotlinx.android.synthetic.main.item_notification_list.view.*

class NotificationListAdapter(var context: Context) : RecyclerView.Adapter<NotificationListAdapter.NotifyViewHolder>() {

    var notificationList: MutableList<Messages> = ArrayList()
    fun displayData(list: MutableList<Messages>) {
        this.notificationList.clear()
        this.notificationList.addAll(list)
        notifyDataSetChanged()
    }

    inner class NotifyViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(notification: Messages) {
            itemView.tv_notificationTitle.text = """${notification.title}(${notification.notifyHome})"""
            itemView.tv_notificationDate.text = notification.notificationDate
            itemView.tvTimeNotify.text = notification.notificationTime
            itemView.tv_notificationMessage.text = itemView.context.getString(R.string.execution_notification_message,
                    notification.notificationType, notification.notificationTime, notification.mode)
            if(!notification.errorMessage.isNullOrEmpty()){
                itemView.ivErrorInfo.visibility = View.VISIBLE
            }else{
                itemView.ivErrorInfo.visibility = View.GONE
            }

            //itemView.tvNotification.text = notification.notificationMessage
        }
        init {
            itemView.ivErrorInfo.setOnClickListener {
                Utils.showCustomDialogOk(context,context.getString(R.string.error_info), notificationList[adapterPosition].errorMessage!!,R.layout.dialog_layout,object: DialogListener{
                    override fun onOkClicked() {

                    }

                    override fun onCancelClicked() {

                    }

                })
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NotifyViewHolder {
        val rootView = LayoutInflater.from(p0.context).inflate(R.layout.item_notification_list, p0, false)
        return NotifyViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(p0: NotifyViewHolder, p1: Int) {
        p0.bind(notificationList[p1])
    }
}