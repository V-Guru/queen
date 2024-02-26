package com.wozart.aura.ui.createautomation

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.core.app.JobIntentService
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.gson.Gson
import com.wozart.aura.utilities.Constant
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class GeofenceService : JobIntentService() {

    private var JOB_ID = 573
    private var TAG = "GeoService"

    fun enqueueWork(context: Context, intent: Intent) {
        enqueueWork(context, GeofenceService::class.java, JOB_ID, intent)
    }

    override fun onHandleWork(intent: Intent) {

        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        val geofencetriggring: MutableList<Geofence>
        if (geofenceEvent.hasError()) {
            val geofenceError = GeofenceErrorMessages.getErrorString(this, geofenceEvent.errorCode)
            Log.i(TAG, "Found error$geofenceError")
        }
        val geofenceTransition = geofenceEvent.geofenceTransition
        if ((geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) || (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
            geofencetriggring = geofenceEvent.triggeringGeofences
            val geofenceDetails = getGeofenceDetails(this, geofenceTransition, geofencetriggring)
            Log.i(TAG, geofenceDetails)
        }
    }

    private fun getGeofenceDetails(context: Context, geofenceTransition: Int, triggring: MutableList<Geofence>): String {
        var geofenceTransitionString: String? = null
        triggring.map { geofence ->
            geofence.requestId
            getTransitionString(geofenceTransition, geofence.requestId)
        }
        geofenceTransitionString = geofenceTransition.toString()
        return geofenceTransitionString.toString()
    }

    private fun sendNotification(geoDetailsData: MutableList<GeoDetailsData>) {
        try {
            val gson = Gson()
            val url = URL("https://bpesxehfze.execute-api.us-east-1.amazonaws.com/testGeo/")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            val json = gson.toJson(geoDetailsData)
            connection.setRequestProperty("charset", "utf-8")
            connection.setRequestProperty("Content-lenght", json.toString())
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connect()

            val outputStream: DataOutputStream = DataOutputStream(connection.outputStream)
            outputStream.write(json.toByteArray())
            outputStream.flush()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                    val output: String = reader.readLine()
                    Toast.makeText(this, "There was error $output", Toast.LENGTH_SHORT).show()
                    System.exit(0)
                } catch (exception: Exception) {

                }
            }
        } catch (e: Exception) {

        }
    }


    private fun getTransitionString(geofenceTransition: Int, requestId: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val userId = prefs.getString("ID_", "NULL")
        val homeName = prefs.getString("HOME", "My Home")
        Log.d(TAG, "$geofenceTransition $requestId")

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                val geoDataDetail: MutableList<GeoDetailsData> = ArrayList()
                val geoData = GeoDetailsData()
                geoData.geohome = homeName
                geoData.geomessage = "Entered to Location"
                geoData.passage = "Arriving"
                geoData.geoUserId = userId
                geoData.scheduleName = requestId
                geoDataDetail.add(geoData)
                sendNotification(geoDataDetail)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                val geoDataDetail: MutableList<GeoDetailsData> = ArrayList()
                val geoData = GeoDetailsData()
                geoData.geohome = homeName
                geoData.geomessage = "Exited the Location"
                geoData.passage = "Leaving"
                geoData.geoUserId = userId
                geoData.scheduleName = requestId
                geoDataDetail.add(geoData)
                sendNotification(geoDataDetail)
            }
            else -> {
                Toast.makeText(this, "oh! cow eyes something bad happen!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}