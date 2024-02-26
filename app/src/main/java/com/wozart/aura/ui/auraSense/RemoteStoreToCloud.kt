package com.wozart.aura.ui.auraSense

import android.util.Log
import com.wozart.aura.utilities.AppExecutors
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-01-31
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 *****************************************************************************/

object RemoteStoreToCloud {

    fun storeRemoteToCloud(data: String, dType: String) {
        AppExecutors().diskIO().execute {
            val json = data
            var connection: HttpURLConnection? = null
            try {
                val url = URL("https://536oh8ac6e.execute-api.ap-south-1.amazonaws.com/test/")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = if (dType == "rNew") {
                    "POST"
                } else {
                    "PUT"
                }
                connection.connectTimeout = 5000
                connection.doOutput = true

                val outputStream: DataOutputStream = DataOutputStream(connection.outputStream)
                outputStream.write(json?.toByteArray())
                outputStream.flush()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    try {
                        val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                        val output: String = reader.readLine()
                        Log.d("Remote_data", output)

                    } catch (exception: Exception) {
                        Log.d("Error", "Error getting data")
                    }

                }
            } catch (e: Exception) {
                connection!!.disconnect()
                e.printStackTrace()
            } finally {
                connection?.disconnect()
            }
        }
    }

}