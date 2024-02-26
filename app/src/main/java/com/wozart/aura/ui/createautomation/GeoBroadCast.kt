package com.wozart.aura.ui.createautomation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class GeoBroadCast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        GeofenceService().enqueueWork(context!!,intent!!)
    }
}