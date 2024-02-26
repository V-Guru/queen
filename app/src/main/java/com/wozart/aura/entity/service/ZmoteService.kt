package com.wozart.aura.entity.service

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.wozart.aura.utilities.Encryption
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
class ZmoteService(context: Context,onZemoteFetch:OnZemoteFetchData) :  AsyncTask<String, String, String>() {
    private var result : String ?= null
    private var listner : OnZemoteFetchData ?= null

    init{
        listner = onZemoteFetch
    }

    override fun doInBackground(vararg p0: String?): String {
        var connection : HttpURLConnection?= null
        var reader: BufferedReader? = null
        try{
            val url = URL("http://api.zmote.io/discover")
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod("GET")
            connection.setRequestProperty("Content-Type", "text/plain")
            connection.connect()

            val inputStream = connection.inputStream
            val buffer = StringBuffer()
            if (inputStream == null) {

            }
            reader = BufferedReader(InputStreamReader(inputStream!!))
            var line : String? = ""
            while (line != null) {
                line = reader.readLine()
                buffer.append(line + "\n")
                result += line
            }

            if (buffer.length == 0){

            }
            result = buffer.toString()
            listner!!.onZemoteFetch(buffer.toString())

        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            if (connection != null) {
                connection.disconnect()
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (e : Exception) {
                    Log.e("ERROR", "Error closing stream", e);
                }
            }

        }
        return result.toString()
    }

    interface OnZemoteFetchData{
        fun onZemoteFetch(message:String)
    }

}