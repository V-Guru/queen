package com.wozart.aura.ui.dashboard.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.preference.PreferenceManager
import com.wozart.aura.R
import com.wozart.aura.ui.adapter.DeviceListAdapter
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.sceneController.SceneControllerSetUpActivity
import com.wozart.aura.ui.wifisettings.EspProvisition
import com.wozart.aura.ui.wifisettings.ZemoteActivty
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.fragment_device_add.*


/**
 * Created by Saif on 17/08/20.
 * mds71964@gmail.com
 */
class DeviceAddActivity : AppCompatActivity(), RecyclerItemClicked, View.OnClickListener {

    lateinit var adapter: DeviceListAdapter
    var deviceType: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_device_add)
        initialize()
    }

    fun initialize() {
        val deviceList = arrayListOf(getString(R.string.aura_switch_product), getString(R.string.aura_sense_product), getString(R.string.aura_plug_product), getString(R.string.aura_button_product), getString(R.string.aura_curtain_product), getString(R.string.wozart_led), getString(R.string.gate_controller),Constant.DEVICE_UNIVERSAL_IR,Constant.DEVICE_WOZART_FAN_CONTROLLER, getString(R.string.other_device))
        back_btn.setOnClickListener(this)
        adapter = DeviceListAdapter(this, this)
        rvDevices.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
        rvDevices.setHasFixedSize(true)
        rvDevices.adapter = adapter
        adapter.setData(deviceList)
    }


    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is String) {
            when (viewType) {
                getString(R.string.aura_switch_product) -> {
                    deviceType = getString(R.string.aura_switch_product)
                    val pref = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()

                    val intent = Intent(this, EspProvisition::class.java)
                    startActivity(intent)
                }
                getString(R.string.aura_sense_product) -> {
                    deviceType = getString(R.string.aura_sense_product)
                    val pref = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()
                    val intent = Intent(this, EspProvisition::class.java)
                    startActivity(intent)
                }
                getString(R.string.aura_button_product) -> {
                    deviceType = getString(R.string.aura_button_product)
                    val pref = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()
                    val intent = Intent(this, SceneControllerSetUpActivity::class.java)
                    startActivity(intent)
                }
                getString(R.string.aura_plug_product) -> {
                    deviceType = getString(R.string.aura_plug_product)
                    val pref = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()
                    val intent = Intent(this, EspProvisition::class.java)
                    startActivity(intent)
                }
                getString(R.string.aura_curtain_product) -> {
                    deviceType = getString(R.string.aura_curtain_product)
                    val pref = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()
                    val intent = Intent(this, EspProvisition::class.java)
                    startActivity(intent)
                }
                getString(R.string.wozart_led) -> {
                    deviceType = getString(R.string.wozart_led)
                    val pref = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()
                    val intent = Intent(this, EspProvisition::class.java)
                    startActivity(intent)
                }
                getString(R.string.gate_controller) -> {
                    deviceType = getString(R.string.gate_controller)
                    val pref = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()
                    val intent = Intent(this, EspProvisition::class.java)
                    startActivity(intent)
                }
                Constant.DEVICE_UNIVERSAL_IR ->{
                    deviceType = Constant.DEVICE_UNIVERSAL_IR
                    val pref = android.preference.PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()
                    val intent = Intent(this, EspProvisition::class.java)
                    startActivity(intent)
                }
                Constant.DEVICE_WOZART_FAN_CONTROLLER -> {
                    deviceType = Constant.DEVICE_WOZART_FAN_CONTROLLER
                    val pref = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()
                    val intent = Intent(this, EspProvisition::class.java)
                    startActivity(intent)
                }
                getString(R.string.other_device) -> {
                    deviceType = getString(R.string.other_device)
                    val pref = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    pref.putString(Constant.DEVICE_TITLE, deviceType)
                    pref.apply()
                    val intent = Intent(this, ZemoteActivty::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back_btn -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }


}