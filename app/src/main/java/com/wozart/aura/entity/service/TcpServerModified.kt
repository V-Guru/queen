package com.wozart.aura.entity.service

import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wozart.aura.utilities.Encryption
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket


/**
 * Created by Saif on 03/08/21.
 * mds71964@gmail.com
 */

class TcpServerModified : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val LOG_TAG = "TcpServer"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val socketServerThread = Thread(SocketServerThread())
        socketServerThread.start()
        Log.d(LOG_TAG, "Server started")
        return START_NOT_STICKY
    }

    private class SocketServerThread : Thread() {
        override fun run() {
            try {
                val soServer = ServerSocket(2345)
                var socClient: Socket? = null
                while (!currentThread().isInterrupted) {
                    socClient = soServer.accept()
                    val serverAsyncTask = ServerAsyncTask()
                    serverAsyncTask.execute(socClient)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    class ServerAsyncTask : AsyncTask<Socket?, Void?, String?>() {

        override fun onPostExecute(s: String?) {}

        override fun doInBackground(vararg params: Socket?): String? {
            var result: String? = null
            val mySocket = params[0]
            try {
                val `is` = mySocket?.getInputStream()
                val out = PrintWriter(mySocket!!.getOutputStream(),
                        true)
                val br = BufferedReader(
                        InputStreamReader(`is`))
                result = br.readLine()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            TcpServerModified().sendMessageToActivity(result)
            return result
        }

    }

     fun sendMessageToActivity(data: String?) {
        var decryptedData = ""
        if (data != null) {
            decryptedData = Encryption.decryptMessage(data)
        }
        val intent = Intent("intentKey")
        intent.putExtra("key", decryptedData)
        LocalBroadcastManager.getInstance(this@TcpServerModified).sendBroadcast(intent)
    }
}