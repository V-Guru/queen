package com.wozart.aura.entity.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager


/**
 * Created by Saif on 18/11/21.
 * mds71964@gmail.com
 */
class NetworkChangeBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            sendBroadCast(isOnline(context!!),context)
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            netInfo != null && netInfo.isConnected
        } catch (e: NullPointerException) {
            e.printStackTrace()
            false
        }
    }

    fun sendBroadCast(boolean: Boolean,context: Context){
        val intent = Intent("networkChange")
        intent.putExtra("isOnline", boolean)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

    }
}