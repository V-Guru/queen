package com.wozart.aura.entity.network


import android.util.Log
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Encryption
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-11-11
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class EspHandler(onEspHandler: OnEspHandlerMessage) {

    var result: String? = null
    private var listner: OnEspHandlerMessage? = null

    init {
        listner = onEspHandler
    }

    fun getResponseData(data: String, ipaddress: String, deviceName: String, btn_name: String) {

        AppExecutors().networkIO().execute {
            val encryptedData = Encryption.encryptMessage(data)
            var connection: HttpURLConnection? = null
            try {

                val url = URL("http://$ipaddress/android")
                Log.d("DEVICE_REQUESTING_URL", "$url")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.doOutput = true
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("authorization", "Bearer teNa5F6AGgIKwxg54fOzJRjNoWyGZaCv")
                connection.connectTimeout = 1000

                val outputStream: DataOutputStream = DataOutputStream(connection.outputStream)
                outputStream.write(encryptedData.toByteArray())
                Log.d("ESP_HANDLER_SEND", "DATA: $encryptedData")
                outputStream.flush()
                outputStream.close()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    try {
                        val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                        val output: String = reader.readLine()
                        val decryptedData = Encryption.decryptMessage(output)
                        Log.i("SERVER_ESP_DATA", "Data Received : $decryptedData")
                        result = decryptedData
                        listner!!.onEspHandlerMessage(decryptedData, btn_name!!)
                    } catch (exception: Exception) {
                        Log.d("ERROR", "Error in HTTP_Connection $deviceName")
                    }
                }

            } catch (e: java.lang.Exception) {
                connection?.disconnect()
                Log.d("DEVICE_REQUESTING_URL", "ERROR:$deviceName")
                listner!!.onEspHandlerMessage("ERROR:$deviceName", btn_name.toString())
                Log.d("ERROR", "Error in HTTP_Connection $deviceName")

            } finally {
                connection?.disconnect()
            }
        }

    }

    interface OnEspHandlerMessage {
        fun onEspHandlerMessage(decryptedData: String, name: String)
    }
}
