package com.wozart.aura.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.internal.and
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Method
import java.math.BigInteger
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.nio.ByteOrder
import java.util.*


/**
 * Created by Saif on 14/09/21.
 * mds71964@gmail.com
 */
class ConnectionUtils {

    fun copyFile(inputStream: InputStream, out: OutputStream): Boolean {
        val buf = ByteArray(1024)
        var len: Int
        try {
            while (inputStream.read(buf).also { len = it } != -1) {
                out.write(buf, 0, len)
            }
            out.close()
            inputStream.close()
        } catch (e: IOException) {
            Log.d("DDDDX", e.toString())
            return false
        }
        return true
    }

    fun getInputStreamByteArray(input: InputStream): ByteArray? {
        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        try {
            while (input.read(buffer).also { len = it } > -1) {
                baos.write(buffer, 0, len)
            }
            baos.flush()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        return baos.toByteArray()
    }

//    fun getMyMacAddress(): String? {
//        try {
//            val all: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
//            for (nintf in all) {
//                if (!nintf.getName().equalsIgnoreCase("wlan0")) continue
//                val macBytes: ByteArray = nintf.getHardwareAddress() ?: return ""
//                val sb1 = StringBuilder()
//                for (b in macBytes) {
//                    var hex = Integer.toHexString(b and 0xFF)
//                    if (hex.length == 1) {
//                        hex = "0$hex"
//                    }
//                    sb1.append("$hex:")
//                }
//                if (sb1.length > 0) {
//                    sb1.deleteCharAt(sb1.length - 1)
//                }
//                return sb1.toString()
//            }
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//        }
//        return "02:00:00:00:00:00"
//    }

    fun getMyIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {

        }
        return null
    }

    fun getWiFiIPAddress(context: Context): String {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return getDottedDecimalIP(wm.connectionInfo.ipAddress)
    }

    fun getDottedDecimalIP(ipAddr: Int): String {
        var ipAddr = ipAddr
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddr = Integer.reverseBytes(ipAddr)
        }
        val ipByteArray: ByteArray = BigInteger.valueOf(ipAddr.toLong()).toByteArray()

        //convert to dotted decimal notation:
        return getDottedDecimalIP(ipByteArray)
    }

    fun getDottedDecimalIP(ipAddr: ByteArray): String {
        //convert to dotted decimal notation:
        var ipAddrStr = ""
        for (i in ipAddr.indices) {
            if (i > 0) {
                ipAddrStr += "."
            }
            ipAddrStr += ipAddr[i] and 0xFF
        }
        return ipAddrStr
    }

    fun isWifiConnected(context: Context): Boolean {
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return if (wifiMgr.isWifiEnabled) { // WiFi adapter is ON
            val wifiInfo = wifiMgr.connectionInfo
            if (wifiInfo != null && wifiInfo.networkId == -1) {
                false // Not connected to an access-Point
            } else true
            // Connected to an Access Point
        } else {
            false // WiFi adapter is OFF
        }
    }

    fun isWiFiEnabled(context: Context): Boolean {
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiMgr.isWifiEnabled
    }

    fun requestPermission(strPermission: String, perCode: Int, activity: Activity?) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, strPermission)) {
            Toast.makeText(activity, "GPS permission allows us to access location data." +
                    " Please allow in App Settings for additional functionality.",
                    Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(strPermission), perCode)
        }
    }

    fun checkPermission(strPermission: String?, _c: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result = _c?.let { ContextCompat.checkSelfPermission(it, strPermission!!) }
            result == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun deletePersistentGroups(wifiP2pManager: WifiP2pManager?, channel: WifiP2pManager.Channel?) {
        try {
            val methods: Array<Method> = WifiP2pManager::class.java.methods
            for (i in methods.indices) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (netid in 0..31) {
                        methods[i].invoke(wifiP2pManager, channel, netid, null)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ApplySharedPref")
    fun clearKey(cxt: Context, key: String?) {
        val prefsEditor: SharedPreferences.Editor = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit()
        prefsEditor.remove(key)
        prefsEditor.commit()
    }

    fun saveString(cxt: Context, key: String?, value: String?) {
        val prefsEditor: SharedPreferences.Editor = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit()
        prefsEditor.putString(key, value)
        prefsEditor.commit()
    }

    fun getString(cxt: Context, key: String?): String? {
        val prefs: SharedPreferences = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }

    fun saveInt(cxt: Context, key: String?, value: Int) {
        val prefsEditor: SharedPreferences.Editor = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit()
        prefsEditor.putInt(key, value)
        prefsEditor.commit()
    }

    fun getInt(cxt: Context, key: String?): Int {
        val prefs: SharedPreferences = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE)
        return prefs.getInt(key, -1)
    }

    fun saveBool(cxt: Context, key: String?, value: Boolean) {
        val prefsEditor: SharedPreferences.Editor = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit()
        prefsEditor.putBoolean(key, value)
        prefsEditor.commit()
    }

    fun getBool(cxt: Context, key: String?): Boolean {
        val prefs: SharedPreferences = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE)
        return prefs.getBoolean(key, false)
    }

    fun clearPreferences(cxt: Context) {
        val prefsEditor: SharedPreferences.Editor = cxt.getSharedPreferences("kkd", Context.MODE_PRIVATE).edit()
        prefsEditor.clear().commit()
    }
}