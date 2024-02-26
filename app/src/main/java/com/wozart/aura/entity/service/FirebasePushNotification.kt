package com.wozart.aura.entity.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wozart.aura.R


/**
 * Created by Saif on 07/10/21.
 * mds71964@gmail.com
 */
class FirebasePushNotification : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        sendNotification(p0.notification?.body)
        super.onMessageReceived(p0)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    private fun sendNotification(message: String?) {
        sendNotificationActivity(message ?: "")
        val intent = Intent(this, Class.forName("com.wozart.aura.ui.dashboard.DashboardActivity"))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.wozart_logo_about))
                .setSmallIcon(R.drawable.wozart_logo_about)
                .setContentTitle("Wozart")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())

    }

    private fun sendNotificationActivity(message: String) {
        val intent = Intent("firebase_message")
        intent.putExtra("message_data", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}