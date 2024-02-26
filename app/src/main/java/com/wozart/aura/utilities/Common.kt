package com.wozart.aura.utilities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.ui.auraSense.RemoteIconModel
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Saif on 03/09/20.
 * EZJobs
 * mdsaif@onata.com
 */
class Common {


    companion object {
        val channelList: MutableList<RemoteIconModel> = arrayListOf()
        var numberChannelList: MutableList<RemoteIconModel> = ArrayList()
        var initiatenumberList: MutableList<RemoteIconModel> = ArrayList()

        var numberList = arrayOf("KEY_1",
                "KEY_2",
                "KEY_3",
                "KEY_4",
                "KEY_5",
                "KEY_6",
                "KEY_7",
                "KEY_8",
                "KEY_9",
                "KEY_0")

        var sharingUserName: String? = null

        fun getSenseData(motionData: String): MutableList<AuraSenseConfigure> {
            val jsonData = motionData
            val senseDataList: MutableList<AuraSenseConfigure> = ArrayList()
            val data = JSONArray(jsonData)
            for (d in 0 until data.length()) {
                val dataList = data.getJSONObject(d)
                val sense = AuraSenseConfigure()
                sense.auraSenseName = dataList.getString("auraSenseName")
                sense.above = dataList.getBoolean("above")
                sense.below = dataList.getBoolean("below")
                sense.auraSenseFavorite = dataList.getBoolean("auraSenseFavorite")
                sense.auraSenseIcon = dataList.getInt("auraSenseIcon")
                sense.auraSenseIndex = dataList.getInt("auraSenseIndex")
                sense.range = dataList.getInt("range")
                sense.isSelected = dataList.getBoolean("isSelected")
                sense.senseDeviceName = dataList.getString("senseDeviceName")
                sense.senseUiud = dataList.getString("senseUiud")
                sense.senseMacId = dataList.getString("senseMacId")
                sense.type = dataList.getString("type")
                senseDataList.add(sense)
            }
            return senseDataList
        }


        fun getArrayNumber(): MutableList<RemoteIconModel> {
            initiatenumberList.clear()
            for (num in numberList) {
                val numberData = RemoteIconModel()
                numberData.remoteButtonName = num
                initiatenumberList.add(numberData)
            }
            return initiatenumberList
        }

        fun openPlayStore(context: Context) {
            val appPackageName = context.packageName
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (e: ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }

        fun checkChannelPresence(channelName: String, channelNumber: String): Boolean {
            var flag = false
            for (channel in channelList) {
                if (channel.channelNumber == channelNumber || channel.remoteButtonName == channelName) {
                    flag = true
                    break
                }
            }
            return flag
        }

        fun addChangelist(remoteIconModel: RemoteIconModel): MutableList<RemoteIconModel> {
            channelList.add(remoteIconModel)
            return channelList
        }

        fun addNumberChannelList(remoteIconModel: RemoteIconModel) {
            numberChannelList.add(remoteIconModel)
        }

        var turnOffArray = arrayOf("Never", "After 1 minute", "After 2 minutes", "After 3 minutes", "After 4 minutes", "After 5 minutes", "After 10 minutes", "After 15 minutes", "After 20 minutes", "After 30 minutes", "After 45 minutes", "After 60 minutes")
        fun getTurnoffData(timeOff: String): String {
            var turnOffTime = timeOff
            if (turnOffTime == "60") {
                turnOffTime = turnOffArray[1]
            } else if (turnOffTime == "120") {
                turnOffTime = turnOffArray[2]
            } else if (turnOffTime == "180") {
                turnOffTime = turnOffArray[3]
            } else if (turnOffTime == "240") {
                turnOffTime = turnOffArray[4]
            } else if (turnOffTime == "300") {
                turnOffTime = turnOffArray[5]
            } else if (turnOffTime == "600") {
                turnOffTime = turnOffArray[6]
            } else if (turnOffTime == "900") {
                turnOffTime = turnOffArray[7]
            } else if (turnOffTime == "1200") {
                turnOffTime = turnOffArray[8]
            } else if (turnOffTime == "1800") {
                turnOffTime = turnOffArray[9]
            } else if (turnOffTime == "2700") {
                turnOffTime = turnOffArray[10]
            } else if (turnOffTime == "3600") {
                turnOffTime = turnOffArray[11]
            }
            return turnOffTime
        }


        var buttonList = arrayOf(
                "KEY_POWER",
                "KEY_VOLUMEUP",
                "KEY_VOLUMEDOWN",
                "KEY_CHANNELUP",
                "KEY_CHANNELDOWN",
                "KEY_MUTE",
                "KEY_TV",
                "KEY_MENU",
                "KEY_FAVORITES",
                "KEY_1",
                "KEY_2",
                "KEY_3",
                "KEY_4",
                "KEY_5",
                "KEY_6",
                "KEY_7",
                "KEY_8",
                "KEY_9",
                "KEY_0",
                "Pre-Ch",
                "ChanList",
                "KEY_ENTER",
                "KEY_UP",
                "KEY_RIGHT",
                "KEY_LEFT",
                "KEY_DOWN",
                "KEY_OK",
                "KEY_INFO",
                "KEY_INTERNET",
                "KEY_EXIT",
                "KEY_RED",
                "KEY_GREEN",
                "KEY_BLUE",
                "KEY_REWIND",
                "KEY_PAUSE",
                "KEY_FORWARD",
                "KEY_RECORD",
                "KEY_PLAY",
                "KEY_STOP",
                "Tools",
                "KEY_RETURN"

        )

        fun <T> getPojoFromMap(linkedTree: Any, className: Class<T>): T {
            val gson = Gson()
            return gson.fromJson(gson.toJsonTree(linkedTree).asJsonObject, className)
        }

        fun isLocationPermissionsGranted(context: Context?): Boolean {
            if (isVersionAbove())
                return (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
            else return isLocationPermissions(context)
        }

        fun isLocationPermissions(context: Context?): Boolean {
            return (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        }

        fun isLocationEnabled(context: Context): Boolean {
            if (isMarshmallowOrAbove()) {
                var locationMode = Settings.Secure.LOCATION_MODE_OFF
                try {
                    locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
                } catch (e: Settings.SettingNotFoundException) {
                    // do nothing
                }
                return locationMode != Settings.Secure.LOCATION_MODE_OFF
            }
            return true
        }

        private fun isMarshmallowOrAbove(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }

        fun isVersionAbove():Boolean{
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }

        fun isLocationRequired(context: Context?): Boolean {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getBoolean(Constant.PREFS_LOCATION_NOT_REQUIRED, isMarshmallowOrAbove())
        }


        fun markLocationPermissionRequested(context: Context?) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.edit().putBoolean(Constant.PREFS_PERMISSION_REQUESTED, true).apply()
        }


        fun markLocationNotRequired(context: Context?) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.edit().putBoolean(Constant.PREFS_LOCATION_NOT_REQUIRED, false).apply()
        }

        fun isBleEnabled(): Boolean {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            return adapter != null && adapter.isEnabled
        }

        fun getStateDayStatus(context: Context?): String {
            val c = Calendar.getInstance()
            val timeOfDay = c[Calendar.HOUR_OF_DAY]
            var dayState: String? = null
            if (timeOfDay in 0..11) {
                dayState = context?.getString(R.string.good_morning)
            } else if (timeOfDay in 12..15) {
                dayState = context?.getString(R.string.good_noon)
            } else if (timeOfDay in 16..20) {
                dayState = context?.getString(R.string.good_evening)
            } else if (timeOfDay in 21..23) {
                dayState = context?.getString(R.string.good_night)
            }
            return dayState!!
        }

        fun getSunsetSunrise(timeSunsetSunrise: String): String {
            val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
            val currentLocalTime = calendar.getTime()
            val date = SimpleDateFormat("Z")
            val localTime = date.format(currentLocalTime)
            val offsetTimetype = localTime[0]
            val offsetTimeHours = localTime.substring(1, 3).toInt()
            val offsetTimeMinutes = localTime.substring(3).toInt()

            val timeSunset = timeSunsetSunrise!!.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val sunsetHour = timeSunset[0].toInt()
            val sunsetMinutes = timeSunset[1].toInt()
            val totalTime = sunsetHour * 60 + sunsetMinutes
            val totalOffset = offsetTimeHours * 60 + offsetTimeMinutes
            var gmtTime = 0

            if (offsetTimetype == '+') {
                if (totalTime > totalOffset) {
                    gmtTime = totalTime - totalOffset
                } else {
                    gmtTime = 1440 - totalOffset + totalTime
                }
            } else {
                gmtTime = totalTime + totalOffset
                if (gmtTime > 1440) {
                    gmtTime = gmtTime - 1440
                }
            }

            return String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
        }
    }
}