package com.wozart.aura.ui.dashboard.more

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import com.wozart.aura.R
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.DeviceMessage
import com.wozart.aura.data.model.Messages
import com.wozart.aura.data.model.Notification
import com.wozart.aura.ui.adapter.NotificationListAdapter
import com.wozart.aura.ui.customview.LinearLayoutManager
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import kotlinx.android.synthetic.main.activity_notification.*
import kotlin.concurrent.thread

class MessagesActivity : AppCompatActivity(), RecyclerItemClicked {
    var rulesTableDo = RulesTableHandler()
    lateinit var adapter: NotificationListAdapter
    val notifications: MutableList<Messages> = ArrayList()

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        initialise()
    }

    fun initialise() {
        tvTitle.text = getString(R.string.notification_title)
        back.setOnClickListener {
            finish()
        }
        adapter = NotificationListAdapter(this)
        list_notification.adapter = adapter
        list_notification.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        getListNotification()
    }

    private fun getListNotification() {
        progress_bar.visibility = View.VISIBLE
        notifications.clear()
        thread {
            val user = rulesTableDo.getUser()
            runOnUiThread {
                if (user != null) {
                    if (user._messages != null) {
                        for (notify in user._messages!!) {
                            val message = Messages()
                            message.mode = notify["mode"].toString()
                            message.notifyHome = notify["home"].toString()
                            message.title = notify["name"].toString()
                            message.errorMessage = notify["error"]
                            message.notificationDate = notify["date"].toString()
                            message.notificationTime = notify["time"].toString()
                            message.notificationType = notify["type"].toString()
                            notifications.add(message)
                        }
                        progress_bar.visibility = View.GONE
                        adapter.displayData(notifications)
                        adapter.notifyDataSetChanged()
                    } else {
                        progress_bar.visibility = View.GONE
                        emptyNotificationTv.visibility = View.VISIBLE
                    }
                } else {
                    progress_bar.visibility = View.GONE
                    emptyNotificationTv.visibility = View.VISIBLE
                }
            }
        }
    }
}