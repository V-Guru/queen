package com.wozart.aura.ui.dashboard.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.net.PlacesClient
import com.wozart.aura.R
import com.wozart.aura.ui.adapter.SearchLocationDisplayAdapter
import com.wozart.aura.ui.home.HomeDetailsActivity
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.layout_map_home.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-12-16
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 *****************************************************************************/

class HomeLocationActivity : FragmentActivity(), OnMapReadyCallback, LocationListener, OnCompleteListener<Void>, SearchLocationDisplayAdapter.PlaceAutoCompleteInterface {


    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private var mgoogleapiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var locationMarker: Marker? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    var map = SupportMapFragment()
    var googleMap: GoogleMap? = null
    private var GeofenceCircle: Circle? = null
    private var data: Location? = null
    private var latLong: LatLng? = null
    var geoModel = 200.0f
    var long: Double? = null
    var addressList: List<Address> = ArrayList()
    private val PLACE_PICKER_REQUEST = 3
    var circleOptions = CircleOptions()
    var locationSelect: String = ""
    var homeName: String? = null
    var homeType: String? = null
    var locationFlag = true
    var locationSetHome: String? = null
    lateinit var locationAdapter: SearchLocationDisplayAdapter
    var searchText = ""
    var handler = Handler()

    var runnable = Runnable {
        if (searchText.isNotEmpty()) {
            searchLocation(searchText)
        } else {
            recyclerView.visibility = View.GONE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_map_home)
        map = supportFragmentManager.findFragmentById(R.id.map_id) as SupportMapFragment
        map.getMapAsync(this)
        init()
        setListener()
    }

    private fun setListener() {
        home.setOnClickListener {
            this.finish()
        }

        save_location.setOnClickListener {
            openNextScreen()
        }

        enter_place.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, if (s.isNullOrEmpty()) 0 else 500)
                if (count > 0) {
                    cancel.visibility = View.VISIBLE
                } else {
                    cancel.visibility = View.GONE
                }
                searchText = s.toString()
            }
        })

        cancel.setOnClickListener {
            enter_place.setText("")
        }

        googleMap?.setOnCameraMoveListener {
            var camera = googleMap?.cameraPosition?.target
        }
    }

    fun setAdapter(addresses: MutableList<Address>) {
        recyclerView.visibility = View.VISIBLE
        (recyclerView.adapter as SearchLocationDisplayAdapter).setData(addresses)
    }

    private fun searchLocation(searchText: String) {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocationName(searchText, 5)
            if (null != addresses && !addresses.isEmpty()) {
                setAdapter(addresses)
            }
        } catch (e: IOException) {

        }
    }

    fun init() {
        locationAdapter = SearchLocationDisplayAdapter(this)
        recyclerView.adapter = locationAdapter
        val zoomControl = 0x1
        val zoomControls = map.requireView().findViewById(zoomControl) as View?
        zoomControls?.setPadding(20, 0, 0, 200)
        val intent = intent
        homeName = intent.getStringExtra("HOME_NAME")
        homeType = intent.getStringExtra(Constant.CREATE_HOME_ROOM)
        locationSetHome = intent.getStringExtra(Constant.SET_LOCATION)
        latitude = intent.getDoubleExtra(Constant.CURRENT_LATITUDE, 0.0)
        longitude = intent.getDoubleExtra(Constant.CURRENT_LONGITUDE, 0.0)

    }

    private fun openNextScreen() {
        val mintent = Intent(this, HomeDetailsActivity::class.java)
        mintent.putExtra("ADDRESS_LOCATION", locationSelect)
        mintent.putExtra(Constant.CREATE_HOME_ROOM, homeType)
        mintent.putExtra("HOME_NAME", homeName)
        mintent.putExtra(Constant.CURRENT_LATITUDE, latitude)
        mintent.putExtra(Constant.CURRENT_LONGITUDE, longitude)
        mintent.putExtra("LOCATION_FLAG", locationFlag)
        mintent.putExtra(Constant.SET_LOCATION, locationSetHome)
        mintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(mintent)
        //startActivityForResult(mintent,Activity.RESULT_OK)
        finish()
    }

    private fun initGoogleApiClient() {
        mgoogleapiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(ConnectionAddListner())
                .addOnConnectionFailedListener(ConnectionFailedListner())
                .build()
        mgoogleapiClient?.connect()
    }

    private fun ConnectionAddListner() = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(p0: Bundle?) {
            getLastLocation()
        }

        override fun onConnectionSuspended(p0: Int) {
        }
    }

    private fun ConnectionFailedListner() = GoogleApiClient.OnConnectionFailedListener {
    }

    fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        data = LocationServices.FusedLocationApi.getLastLocation(mgoogleapiClient!!)
        if (data == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleapiClient!!, mLocationRequest!!, this)
        } else {
            writeLocation(data!!)
        }

    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0
        initmap(googleMap!!)
    }

    private fun initmap(googleMap: GoogleMap) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                initGoogleApiClient()
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.setMinZoomPreference(15f)
                googleMap.isMyLocationEnabled = true
                val mCameraLocation = googleMap.cameraPosition.target
                getAddress(mCameraLocation.latitude, mCameraLocation.longitude)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
            }
        } else {
            initGoogleApiClient()
            googleMap.isMyLocationEnabled = true
        }
    }


    private fun writeLocation(data: Location) {
        if (latitude != 0.0 && longitude != 0.0) {
            latLong = LatLng(latitude!!, longitude!!)
            drawCustomGeofence(geoModel, latLong!!)
        } else {
            latitude = data.latitude
            longitude = data.longitude
            latLong = LatLng(latitude!!, longitude!!)
            drawCustomGeofence(geoModel, latLong!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initmap(googleMap!!)
            }
        }
    }

    override fun onLocationChanged(p0: Location?) {
        try {
            if (p0 == null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleapiClient!!, this)
            }
        } catch (e: Exception) {
        }
    }

    private fun drawCustomGeofence(customGeoRadius: Float, latLong: LatLng) {
        if (GeofenceCircle != null) {
            GeofenceCircle!!.remove()
        }
        if (locationMarker != null) {
            locationMarker!!.remove()
        }
        locationMarker = googleMap!!.addMarker(MarkerOptions()
                .title(getplaceAddress(latLong))
                .position(latLong))

        circleOptions = CircleOptions()
                .center(locationMarker!!.position)
                .strokeColor(Color.argb(191, 245, 245, 245))
                .strokeWidth(5f)
                .fillColor(Color.argb(105, 88, 147, 174))
                .radius(customGeoRadius.toDouble())
        locationMarker?.isDraggable = true
        GeofenceCircle = googleMap!!.addCircle(circleOptions)
        val zoom = 16f
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLong, zoom)
        googleMap!!.animateCamera(cameraUpdate)

    }


    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onComplete(p0: Task<Void>) {

    }

    private fun getplaceAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        var address: Address? = null
        var addressText = ""
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                addressText = address.subLocality + "," + address.locality
                locationSelect = address.getAddressLine(0)
                enter_place.setText(address.getAddressLine(0))
            }
        } catch (e: IOException) {
        }
        return addressText
    }

    @SuppressLint("SetTextI18n")
    fun getAddress(latitude: Double, longitude: Double) {
        latLong = LatLng(latitude, longitude)
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            addressList = geocoder.getFromLocation(latitude, longitude, 1)!!
            val present_address = addressList[0].getAddressLine(0).toString()
            locationSelect = present_address
            drawCustomGeofence(geoModel, latLong!!)
        } catch (e: Exception) {
        }
    }

    override fun onPlaceClick(address: Address) {
        recyclerView.visibility = View.GONE
        val presentAddress = address.getAddressLine(0).toString()
        latitude = address.latitude
        longitude = address.longitude
        latLong = LatLng(address.latitude, address.longitude)
        locationSelect = presentAddress
        drawCustomGeofence(geoModel, latLong!!)
    }


}