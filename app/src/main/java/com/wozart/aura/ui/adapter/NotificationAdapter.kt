package com.wozart.aura.ui.adapter


import android.app.Activity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.data.model.Notification
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.utilities.DialogListener
import kotlinx.android.synthetic.main.item_notification.view.*

class NotificationAdapter(private val activity: Activity, var notifications: MutableList<Notification>, onAcceptSelected: OnAcceptListener, onDeclineSelected: OnDeclineListener) : androidx.recyclerview.widget.RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {


    var listener: OnAcceptListener = onAcceptSelected
    var declineListener: OnDeclineListener = onDeclineSelected

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification: Notification = notifications[position]
        holder.bind(notification, declineListener, listener)
    }


    override fun getItemCount(): Int {
        return notifications.size
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(notification: Notification, listener: OnDeclineListener, mListner: OnAcceptListener) {

            if (notification.status == 0) {
                itemView.tv_notificationTitle.visibility = View.VISIBLE
                itemView.tv_message.visibility = View.VISIBLE
                itemView.tv_notificationTitle?.text = notification.notificationTitle
                itemView.tv_message?.text = notification.notificationMessage
                itemView.btn_accept.visibility = View.VISIBLE
                itemView.btn_reject.visibility = View.GONE
                itemView.tv_status.visibility = View.GONE
                itemView.notification_text.visibility = View.GONE
                itemView.card_notification.visibility = View.GONE
            } else {
                itemView.btn_accept.visibility = View.GONE
                itemView.btn_reject.visibility = View.GONE
                itemView.tv_notificationTitle.visibility = View.GONE
                itemView.tv_message.visibility = View.GONE
                if (notification.status == 1) {
                    itemView.tv_status.visibility = View.VISIBLE
                    itemView.card_notification.visibility = View.GONE
                    itemView.tv_status.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                    itemView.tv_status.text = "Accepted " + notification.notificationTitle
                } else {
                    itemView.layoutAccept.visibility = View.GONE
                    itemView.tv_status.visibility = View.INVISIBLE
                    itemView.card_notification.visibility = View.VISIBLE
                    itemView.notification_text.visibility = View.VISIBLE
                    itemView.notification_text.text = notification.notificationMessage
                }
            }

            itemView.btn_accept.setOnClickListener {
                Utils.showCustomDialog(itemView.context, itemView.context.getString(R.string.txt_accept), itemView.context.getString(R.string.dialog_accept_notification), R.layout.dialog_layout, object : DialogListener {
                    override fun onOkClicked() {
                        //notifyItemChanged(position)
                        mListner.onAcceptSelected(notification)
                    }

                    override fun onCancelClicked() {
                    }
                })

            }
            itemView.btn_reject.setOnClickListener {
                Utils.showCustomDialog(itemView.context, itemView.context.getString(R.string.txt_reject), itemView.context.getString(R.string.dialog_reject_notification), R.layout.dialog_layout, object : DialogListener {
                    override fun onOkClicked() {
                        //notifications[position].status = 2
                        listener.onDeclineSelected(notification)
                    }

                    override fun onCancelClicked() {

                    }
                })
            }
        }


    }

    interface OnAcceptListener {
        fun onAcceptSelected(request: Notification)
    }

    interface OnDeclineListener {
        fun onDeclineSelected(request: Notification)
    }


}
