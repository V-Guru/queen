package com.wozart.aura.entity.service

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-03-04
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class ZmoteCommunication(context: Context, onZmoteLearn: OnReceiveLearnZmote, address: String, btn_name: String,uiud:String,command:String) : AsyncTask<String, String, String>() {

    var result : String ?= null
    var signal_ir :String ?= null
    var listner : OnReceiveLearnZmote ?= null
    var ip_address : String ?= null
    var zmote_uiud :String ?= null
    var command_to_do :String ?= null
    private var remote_btn_name: String ?= null
    init{
        remote_btn_name = btn_name
        listner = onZmoteLearn
        ip_address = address
        zmote_uiud = uiud
        command_to_do = command
    }

    override fun doInBackground(vararg p0: String?): String {
        var connection : HttpURLConnection?= null
        try{
            //http://<IP>/v2/<UUID> -X POST -H 'Content-Type: text/plain' -d 'get_IRL'
            var get_learning = "get_IRL"

            val url = URL("http://$ip_address/v2/$zmote_uiud")
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod("POST")
            connection.setRequestProperty("Content-Type", "text/plain")
            connection.connect()

            val outputStream: DataOutputStream = DataOutputStream(connection.outputStream)
            outputStream.write(command_to_do!!.toByteArray())
            outputStream.flush()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                    val output: String = reader.readLine()
                    result = output
                    Thread.sleep(2000)
                    val send_ir: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                    val output_ir: String = send_ir.readLine()
                    signal_ir = output_ir
                    listner!!.onZmoteLearn(signal_ir!!,remote_btn_name)

                } catch (exception: Exception) {
                    Log.d("ERROR", "Error in HTTP_Connection")
                }
            } else {
                Log.d("error", "check connection")
            }
        }catch (e:java.lang.Exception){
            connection!!.disconnect()
            e.printStackTrace()
        }finally {
            if (connection != null) {
                connection.disconnect()
            }

        }
        return result.toString()
    }


    interface OnReceiveLearnZmote{
        fun onZmoteLearn(data: String, remoteBtnName: String?)
    }
}