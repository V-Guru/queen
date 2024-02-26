package com.wozart.aura.ui.createautomation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.API
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.wozart.aura.R
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.MyPreferences
import kotlinx.android.synthetic.main.activity_set_geo_automation.*
import kotlinx.android.synthetic.main.dialog_specific_time_layout.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SetGeoAutomationActivity : androidx.fragment.app.FragmentActivity(), OnCompleteListener<Void>, LocationListener, OnMapReadyCallback,
        ResultCallback<Status> {


    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    var TAG: String = "LOCATION_CHECK"
    private var mGeofencePendingIntent: PendingIntent? = null
    private var mgoogleapiClient: GoogleApiClient? = null
    private var locationMarker: Marker? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    var map = SupportMapFragment()
    var googleMap: GoogleMap? = null
    private var GeofenceCircle: Circle? = null
    var geofencingClient: GeofencingClient? = null
    private var data: Location? = null
    private var latLong: LatLng? = null
    var geoModel = 200.0f
    var updateRadius: Float = 0.0f
    var Updated = 0f
    var addressList: List<Address> = ArrayList()
    private val PLACE_PICKER_REQUEST = 3
    private var automationNameOld: String? = null
    private var automstionSceneType: String? = null
    private var automationName: String? = null
    private var scheduletype: String? = null
    private var selectedIcon: Int? = 0
    private var automationEnable: Boolean = false
    var geofenceTa: Geofence? = null
    var circleOptions = CircleOptions()
    var triggerType: String? = null
    private var starttimeformat: String? = null
    private var endTimeFormat: String? = null
    private var trigger_when: String? = null
    private val localSqlUtils = UtilsTable()
    private var mDbUtils: SQLiteDatabase? = null
    var listRoom: RoomModelJson? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_geo_automation)
        checkTheme()
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase
        if (Constant.HOME == null) {
            val preference = PreferenceManager.getDefaultSharedPreferences(this)
            Constant.HOME = preference.getString("HOME", "NO HOME")
        }

        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home").find { it.type == "home" && it.name == Constant.HOME }
        if (listRoom != null) {
            latitude = listRoom?.homeLatitude
            longitude = listRoom?.homeLongitude
        }
        automationNameOld = intent.getStringExtra("automationNameOld")
        automstionSceneType = intent.getStringExtra("automationSceneType")
        automationName = intent.getStringExtra("automationName")
        scheduletype = intent.getStringExtra("scheduleBasedType")
        selectedIcon = intent.getIntExtra("icon", 0)
        automationEnable = intent.getBooleanExtra("scheduleEnable", false)
        triggerType = intent.getStringExtra("ACTION_WHEN")
        geoModel = intent.getFloatExtra("GEO_RADIUS", 200.0f)
        map = supportFragmentManager.findFragmentById(R.id.map_id) as SupportMapFragment
        map.getMapAsync(this)

        geofencingClient = LocationServices.getGeofencingClient(this)

        init()
    }

    private fun checkTheme() {
        when (MyPreferences(this).darkMode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun init() {
        home.setOnClickListener {
            onBackPressed()
        }
        btnNext.setOnClickListener { openNextScreen() }

        var timeselectClicked = false

        trigger_when = "Any time"

        if (triggerType == "Arriving") {
            text_header.text = getText(R.string.arrive_in)
        } else {
            text_header.text = getString(R.string.leaving_out)
        }

        time_select_layout.setOnClickListener {
            if (!timeselectClicked) {
                timeselectClicked = true
                layout_selection.visibility = View.VISIBLE
            } else {
                if (timeselectClicked) {
                    timeselectClicked = false
                    layout_selection.visibility = View.INVISIBLE
                }
            }
        }

        action_select_layout.setOnClickListener {
            selected_time.visibility = View.VISIBLE
            day_time_selected.visibility = View.INVISIBLE
            night_selected.visibility = View.INVISIBLE
            specific_time_selected.visibility = View.INVISIBLE
            text_selected_action.text = "Any time"
            trigger_when = "Any time"
        }
        action_during_day.setOnClickListener {
            day_time_selected.visibility = View.VISIBLE
            night_selected.visibility = View.INVISIBLE
            selected_time.visibility = View.INVISIBLE
            specific_time_selected.visibility = View.INVISIBLE
            text_selected_action.text = "During the day"
            trigger_when = "During the day"
        }
        night_view.setOnClickListener {
            night_selected.visibility = View.VISIBLE
            specific_time_selected.visibility = View.INVISIBLE
            day_time_selected.visibility = View.INVISIBLE
            selected_time.visibility = View.INVISIBLE
            text_selected_action.text = "At night"
            trigger_when = "At night"
        }
        specific_time_card.setOnClickListener {
            specific_time_selected.visibility = View.VISIBLE
            day_time_selected.visibility = View.INVISIBLE
            night_selected.visibility = View.INVISIBLE
            selected_time.visibility = View.INVISIBLE
            text_selected_action.text = "Specific time"
            trigger_when = "Specific time"
            showTimeDialog()
        }

        val seek_bar = findViewById<SeekBar>(R.id.seek_bar)
        val tv_meter = findViewById<TextView>(R.id.tv_meters)
        tv_meter.text = geoModel.toString()
        seek_bar.progress = geoModel.toInt()
        seek_bar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tv_meter.text = progress.toString()
                updateRadius = progress.toFloat()
                geoModel = updateRadius
                updatePopulateGeofence(latLong!!, updateRadius)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Toast.makeText(this@SetGeoAutomationActivity, "Selected radius is " + seekBar.progress + "m", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun showTimeDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_specific_time_layout)
        dialog.setCanceledOnTouchOutside(true)
        val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        val currentLocalTime = calendar.getTime()
        val date = SimpleDateFormat("Z")
        val localTime = date.format(currentLocalTime)
        val offsetTimetype = localTime[0]
        val offsetTimeHours = localTime.substring(1, 3).toInt()
        val offsetTimeMinutes = localTime.substring(3).toInt()
        dialog.start_time_picker.setOnTimeChangedListener { timePicker, hour, minute ->
            val StrMin = String.format("%02d", minute)
            val timeHours24 = hour
            val timeMinutes24 = minute
            val totalTime = timeHours24 * 60 + timeMinutes24
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
            starttimeformat = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
        }

        dialog.end_time_picker.setOnTimeChangedListener { timePicker, hrs, minutes ->

            val StrMin = String.format("%02d", minutes)
            val timeHours24 = hrs
            val timeMinutes24 = minutes
            val totalTime = timeHours24 * 60 + timeMinutes24
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
                    gmtTime -= 1440
                }
            }

            endTimeFormat = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
        }

        dialog.done.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }


    override fun onMapReady(map: GoogleMap?) {
        googleMap = map
        initmap(googleMap!!)

    }

    private fun initmap(googleMap: GoogleMap) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                initGoogleApiClient()
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.setMinZoomPreference(15f)
                googleMap.isMyLocationEnabled = true
                getAddress(latitude!!, longitude!!)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
            }
        } else {
            initGoogleApiClient()
            googleMap.isMyLocationEnabled = true
        }
    }

    private fun initGoogleApiClient() {
        mgoogleapiClient = GoogleApiClient.Builder(this)
                .addApi(API)
                .build()
        mgoogleapiClient?.connect()
    }


    private fun drawCustomGeofence(customGeoRadius: Float, latLong: LatLng) {

        if (GeofenceCircle != null) {
            GeofenceCircle!!.remove()
        }

        if (locationMarker != null) {
            locationMarker!!.remove()
        }
        locationMarker = googleMap!!.addMarker(MarkerOptions()
                .position(latLong))

        circleOptions = CircleOptions()
                .center(locationMarker!!.position)
                .strokeColor(Color.argb(191, 245, 245, 245))
                .strokeWidth(5f)
                .fillColor(Color.argb(105, 88, 147, 174))
                .radius(customGeoRadius.toDouble())


        GeofenceCircle = googleMap!!.addCircle(circleOptions)

        if (customGeoRadius > 200.0f) {
            val zoom = 18.0f
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLong, zoom)
            googleMap!!.animateCamera(cameraUpdate)
        } else {
            val zoom = 16.0f
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLong, zoom)
            googleMap!!.animateCamera(cameraUpdate)
        }

    }


    private fun updatePopulateGeofence(latLong: LatLng, geoModel: Float) {
        drawCustomGeofence(geoModel, latLong)
        addGeofences()
    }


    private fun populateGeofenceList(selectedLatLng: LatLng, customGeoRadius: Float): Geofence? {
        val circleRadius = customGeoRadius
        return Geofence.Builder()
                .setRequestId(automationName.toString())
                .setCircularRegion(selectedLatLng.latitude, selectedLatLng.longitude, circleRadius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
    }


    private fun addGeofences() {
        geofenceTa = populateGeofenceList(locationMarker!!.position, geoModel)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        geofencingClient!!.addGeofences(getGeofencingRequest(geofenceTa), getGeofencePendingIntent()!!)
                .addOnCompleteListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initmap(googleMap!!)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onResult(p0: Status) {
    }


    override fun onLocationChanged(p0: Location?) {
        try {
            if (p0 == null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleapiClient!!, this)
            }
        } catch (e: Exception) {
        }
    }

    override fun onComplete(p0: Task<Void>) {

    }


    @SuppressLint("SetTextI18n")
    fun getAddress(latitude: Double, longitude: Double) {
        latLong = LatLng(latitude, longitude)
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            addressList = geocoder.getFromLocation(latitude, longitude, 1)!!
            drawCustomGeofence(geoModel, latLong!!)
            addGeofences()
        } catch (e: Exception) {
        }
    }


    private fun removeGeofences() {

        geofencingClient!!.removeGeofences(getGeofencePendingIntent()!!).addOnCompleteListener(this)

    }

    private fun openNextScreen() {
        if (googleMap != null) {
            val intent = Intent(this, SetAutomationActivity::class.java)
            intent.putExtra("automationSceneType", automstionSceneType)
            intent.putExtra("automationNameOld", automationNameOld)
            intent.putExtra("scheduleBasedType", scheduletype)
            intent.putExtra("automationName", automationName)
            intent.putExtra("icon", selectedIcon)
            intent.putExtra("customRadius", geoModel)
            intent.putExtra("scheduleEnable", automationEnable)
            intent.putExtra("triggerType", triggerType)
            intent.putExtra("trigger_when", trigger_when)
            intent.putExtra("specific_start_time", starttimeformat)
            intent.putExtra("specific_end_time", endTimeFormat)
            intent.putExtra("geo_latitude", latitude)
            intent.putExtra("geo_longitude", longitude)
            startActivity(intent)
            this.finish()
        }
    }


    private fun getGeofencingRequest(geofence: Geofence?): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence!!)
        }.build()
    }

    private fun getGeofencePendingIntent(): PendingIntent? {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent
        }
        val intent = Intent(this, GeoBroadCast::class.java)
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return mGeofencePendingIntent
    }
}
