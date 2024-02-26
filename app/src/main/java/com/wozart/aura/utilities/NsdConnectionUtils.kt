package com.wozart.aura.utilities

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.IOException

import java.net.ServerSocket


/**
 * Created by Saif on 14/09/21.
 * mds71964@gmail.com
 */
class NsdConnectionUtils {

    fun getPort(context: Context?): Int {
        var localPort: Int = ConnectionUtils().getInt(context!!, "localport")
        if (localPort < 0) {
            localPort = getNextFreePort()
            ConnectionUtils().saveInt(context, "localport", localPort)
        }
        return localPort
    }

    fun getNextFreePort(): Int {
        var localPort = -1
        try {
            val s = ServerSocket(0)
            localPort = s.getLocalPort()

            //closing the port
            if (s != null && !s.isClosed()) {
                s.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.v("DXDXD", Build.MANUFACTURER + ": free port requested: " + localPort)
        return localPort
    }

    fun clearPort(context: Context?) {
        ConnectionUtils().clearKey(context!!, "localport")
    }
}