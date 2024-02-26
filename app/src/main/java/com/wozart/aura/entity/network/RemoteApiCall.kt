package com.wozart.aura.entity.network

import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by Saif on 21/03/22.
 * mds71964@gmail.com
 */

object RemoteApiCall {

    const val DOWNLOAD_URL = "https://536oh8ac6e.execute-api.ap-south-1.amazonaws.com/test"
    const val GET_ALL_DATA = "https://536oh8ac6e.execute-api.ap-south-1.amazonaws.com/test/get-method"
    var appliancesTypeSelected: String? = null
    var brandSelected: String? = null
    var modelSelected: String? = null
    var remoteData: String? = null

    fun setBrands(brandName: String): String {
        return brandSelected.toString()
    }

    fun setModel(model: String): String {
        return modelSelected.toString()
    }

    fun setAppliancesType(type: String): String {
        return appliancesTypeSelected.toString()
    }

    fun getRemoteData(actionToDo: String) {
        AppExecutors().networkIO().execute {
            if (actionToDo == Constant.DOWNLOAD) {
                getDataFromS3(DOWNLOAD_URL,actionToDo)
            } else {
                getDataFromS3(GET_ALL_DATA,actionToDo)
            }
        }
    }

    private fun getDataFromS3(url: String, state: String) {
        var result: String? = null
        var reader: BufferedReader? = null
        var connection: HttpURLConnection? = null
        try {
            connection = getConnection(url, state)
            val inputStream = connection.inputStream
            val buffer = StringBuffer()
            reader = BufferedReader(InputStreamReader(inputStream!!))
            var line: String? = ""
            while (line != null) {
                line = reader.readLine()
                buffer.append(line + "\n")
                result += line
            }
            result = buffer.toString()
            remoteData = result
        } catch (e: Exception) {

        } finally {
            connection?.disconnect()
            if (reader != null) {
                try {
                    reader.close();
                } catch (e: Exception) {

                }
            }

        }

    }

    private fun getConnection(url: String, state: String): HttpURLConnection {
        val urlToGet = URL(url)
        val connection = urlToGet.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        if (state == Constant.DOWNLOAD) {
            connection.setRequestProperty("type", appliancesTypeSelected)
            connection.setRequestProperty("brand", brandSelected)
            connection.setRequestProperty("model", modelSelected)
        }
        connection.connectTimeout = 5000
        return connection
    }
}