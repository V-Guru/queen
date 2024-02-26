package com.wozart.aura.entity.network

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.wozart.aura.utilities.Encryption
import com.wozart.aura.data.sqlLite.DeviceTable
import java.util.concurrent.*


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 24/05/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
 class ConnectTask(context: Context, onTcpMessageReceived: TcpMessageReceiver, message: String, address: String,name:String) : AsyncTask<String, String, TcpClientJava>() {

    private val LOG_TAG = ConnectTask::class.java.simpleName


    private var data: String? = null
    private var ip: String? = null
    private var deviceName:String?=null
    private var mTcpClient: TcpClientJava? = null
    private var listener: TcpMessageReceiver? = null
    private val TAG:String = "TCP_CLIENT"
    @SuppressLint("StaticFieldLeak")
    private var context : Context


    init {
        data = message
        ip = address
        deviceName = name
        this.context = context
        listener = onTcpMessageReceived
    }


    override fun doInBackground(vararg message: String): TcpClientJava? {

            mTcpClient = TcpClientJava(TcpClientJava.OnMessageReceived { message ->
                publishProgress(message)
            })
            val encryptedData = Encryption.encryptMessage(data?:"")
            mTcpClient!!.run(encryptedData,ip,deviceName)

            return null
        }

    override fun onProgressUpdate(vararg message: String) {
        if (message[0].contains("ERROR")) {
            Log.i("SERVER_DATA_ERROR", "Data Received : ${message[0]}")
            listener!!.onTcpMessageReceived(message[0])
        } else {
            val decryptedData = Encryption.decryptMessage(message[0])
            Log.i("SERVER_DATA", "Data Received : $decryptedData")
            listener!!.onTcpMessageReceived(decryptedData)
        }
    }

    interface TcpMessageReceiver {
        fun onTcpMessageReceived(message: String)
    }

}