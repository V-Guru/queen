package com.wozart.aura.ui.wifisettings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.wozart.aura.R
import com.wozart.aura.espProvision.Provision
import com.wozart.aura.espProvision.transport.BLETransport
import com.wozart.aura.ui.auraswitchlist.AuraListActivity
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Common.Companion.isLocationEnabled
import com.wozart.aura.utilities.Constant
import java.util.*


/**
 * Created by Saif on 21/04/21.
 * mds71964@gmail.com
 */
class EspProvisition : AppCompatActivity() {


    private var transportVersion: String? = null
    private var securityVersion: String? = null
    private var POP: String? = null
    private var BASE_URL: String? = null
    private var NETWORK_NAME_PREFIX: String? = null
    private var SERVICE_UUID: String? = null
    private var SESSION_UUID: String? = null
    private var CONFIG_UUID: String? = null
    private var DEVICE_NAME_PREFIX: String? = null
    private var AURA_SENSE_NETWORK: String? = null
    private var AURA_PLUG_NETWORK: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esp_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        val textView = findViewById<View>(R.id.tvSkip) as TextView
        val titleText = findViewById<View>(R.id.textTitle) as TextView
        val back = findViewById<View>(R.id.back) as ImageView
        titleText.text = "Configure Device"
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)?.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        val thisActivity: Activity = this
        val provision = findViewById<Button>(R.id.provision_button)

        BASE_URL = resources.getString(R.string.wifi_base_url)
        NETWORK_NAME_PREFIX = resources.getString(R.string.wifi_network_name_prefix)
        AURA_SENSE_NETWORK = resources.getString(R.string.sense_wifi_network_name_prefix)
        POP = resources.getString(R.string.proof_of_possesion)
        SERVICE_UUID = resources.getString(R.string.ble_service_uuid)
        SESSION_UUID = resources.getString(R.string.ble_session_uuid)
        CONFIG_UUID = resources.getString(R.string.ble_config_uuid)
        DEVICE_NAME_PREFIX = resources.getString(R.string.ble_device_name_prefix)
        AURA_PLUG_NETWORK = resources.getString(R.string.plug_network_name)

        securityVersion = Provision.CONFIG_SECURITY_SECURITY0
        transportVersion = Provision.CONFIG_TRANSPORT_WIFI

        textView.setOnClickListener {
            val intent = Intent(applicationContext, AuraListActivity::class.java)
            startActivity(intent)
            finish()
        }

        back.setOnClickListener { finish() }

        provision.setOnClickListener { checkPermission(transportVersion!!, securityVersion!!) }
    }

    private fun checkPermission(securityVersion: String, transportVersion: String) {
        if (isLocationEnabled(this)) {
            if (Common.isLocationPermissions(this)) {
                startProvisioning(securityVersion, transportVersion)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Provision.REQUEST_PERMISSIONS_CODE)
            }
        } else {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, Constant.REQUEST_ENABLE_GPS)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Provision.REQUEST_PERMISSIONS_CODE -> {
                setResult(resultCode)
            }
        }
    }

    private fun startProvisioning(securityVersion: String, transportVersion: String) {
        val config = HashMap<String, String>()
        config[Provision.CONFIG_TRANSPORT_KEY] = transportVersion
        config[Provision.CONFIG_SECURITY_KEY] = securityVersion
        config[Provision.CONFIG_PROOF_OF_POSSESSION_KEY] = POP!!
        config[Provision.CONFIG_BASE_URL_KEY] = BASE_URL!!
        config[Provision.CONFIG_WIFI_AP_KEY] = NETWORK_NAME_PREFIX!!
        config[Provision.CONFIG_AURA_SENSE_WIFI_KEY] = AURA_SENSE_NETWORK!!
        config[Provision.CONFIG_AURA_PLUG_WIFI_KEY] = AURA_PLUG_NETWORK!!
        config[BLETransport.SERVICE_UUID_KEY] = SERVICE_UUID!!
        config[BLETransport.SESSION_UUID_KEY] = SESSION_UUID!!
        config[BLETransport.CONFIG_UUID_KEY] = CONFIG_UUID!!
        config[BLETransport.DEVICE_NAME_PREFIX_KEY] = DEVICE_NAME_PREFIX!!
        Provision.showProvisioningUI(this, config)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkPermission(securityVersion!!, transportVersion!!)
        }
    }

    private fun goToSuccessPage(statusText: String) {
        val goToSuccessPage = Intent(applicationContext, ProvisionSuccessActivity::class.java)
        goToSuccessPage.putExtra("status", statusText)
        startActivity(goToSuccessPage)
        this.setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}