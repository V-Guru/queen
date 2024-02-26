package com.wozart.aura.utilities

import android.util.Log
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.jvm.Throws

object Encryption {

    private const val value = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.,';:[]{}!@#$%^&*()_=+\"/- "
    private val random = intArrayOf(82, 11, 31, 63, 52, 13, 74, 3, 42, 46, 10, 64, 12, 9, 17, 15, 75, 16, 66, 55, 25, 36, 57, 61, 50, 83, 72, 20, 33, 69, 18, 77, 1, 14, 6, 0, 4, 65, 24, 37, 39, 70, 59, 32, 51, 2, 35, 68, 67, 84, 27, 78, 45, 48, 41, 87, 80, 60, 73, 40, 53, 76, 81, 43, 38, 30, 23, 44, 19, 86, 56, 58, 62, 7, 29, 47, 8, 28, 22, 5, 71, 49, 85, 21, 34, 79, 54, 26)

    @Throws(NoSuchAlgorithmException::class)
    fun SHA256(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        try {
            md.update(pin.toByteArray(charset("UTF-8"))) // Change this to "UTF-16" if needed
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        val digest = md.digest()
        return String.format("%064x", java.math.BigInteger(1, digest))
    }

    fun generateUIUD(deviceId: String): String {
        val rand = Random()
        val uiudNumbers = rand.nextInt(100000)
        var uiudString = uiudNumbers.toString()
        if (uiudString.length == 1) uiudString += "0000"
        if (uiudString.length == 2) uiudString += "000"
        if (uiudString.length == 3) uiudString += "00"
        if (uiudString.length == 4) uiudString += "0"
        return deviceId + uiudString
    }

    fun encryptMessage(data: String): String {
        var encryptedData = ""
        for (element in data) {
            val index = value.indexOf(element)
            val modIndex = random[index]
            val modLEtter = value[modIndex]
            encryptedData += modLEtter
        }

        return encryptedData
    }

    fun decryptMessage(data: String): String {
        var dencryptedData = ""
        for (element in data) {
            val index = value.indexOf(element)
            for (j in random.indices) {
                if (random[j] == index) {
                    dencryptedData += value[j]
                }
            }
        }

        return dencryptedData
    }

    fun isInternetWorking(): Boolean {
        var success = false
        try {
            val url = URL("https://google.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.connect()
            success = connection.responseCode == 200
        } catch (e: IOException) {
           Log.d("IS_INTERNET","INTERNET_CHECK")
        }
        return success
    }
}