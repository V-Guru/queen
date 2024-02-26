package com.wozart.aura.ui.curtain

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraComplete
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.AuraSwitchLoad
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.ui.home.ConfigureLoadActivity
import com.wozart.aura.ui.home.ConfigureLoadActivity.Companion.SENSE_MODE
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.DoubleBtnDialog
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_set_up_curtain.*
import kotlinx.android.synthetic.main.layout_header.*
import kotlin.concurrent.thread


/**
 * Created by Saif on 26/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class CurtainSetUpActivity : AppCompatActivity(), View.OnClickListener, EspHandler.OnEspHandlerMessage {

    private var mDbUtils: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    private val localSqlDatabase = DeviceTable()
    private var mDb: SQLiteDatabase? = null
    var roomSelected: String? = null
    private var rulesTableDo = RulesTableHandler()
    private var roomList: MutableList<String> = ArrayList()
    private var auraSwitch: AuraSwitch = AuraSwitch()
    var jsonhelper: JsonHelper = JsonHelper()
    private var roomIconSelected: Int = 0
    private lateinit var UIUD: String
    private var IP = IpHandler()
    var IpListDevices: MutableList<IpModel> = ArrayList()
    private var aura_sense_device: MutableList<RemoteModel> = ArrayList()
    private var listRoom: MutableList<RoomModelJson> = ArrayList()
    val auraLoads = AuraSwitchLoad()
    var loads: MutableList<AuraSwitchLoad> = ArrayList()
    var checkFirst: Int = 0
    var type: String? = null
    var device: Device? = null
    var notifyData = ""
    var allDeviceList = ArrayList<AuraComplete>()
    var espHandler: EspHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up_curtain)
        init()
        setListner()
    }

    fun init() {
        tvTitle.setTextColor(ContextCompat.getColor(this.baseContext, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(this.baseContext, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        tvNext.visibility = View.INVISIBLE
        home.visibility = View.INVISIBLE
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase

        type = intent.getStringExtra("TYPE")
        val deviceString: String = intent!!.getStringExtra("DEVICE")!!
        if (type == "edit") {
            device = Gson().fromJson(deviceString, Device::class.java)
        } else {
            auraSwitch = Gson().fromJson(deviceString, AuraSwitch::class.java)
        }
        loads = auraLoads.auraPlugLoad(3)
        switchFav.isChecked = loads[0].favourite!!
        UIUD = intent!!.getStringExtra("UIUD")!!

        if (Constant.HOME == null) {
            Constant.HOME = PreferenceManager.getDefaultSharedPreferences(this).getString("HOME", "My Home")
        }
        if (type == "edit") {
            auraSwitch = localSqlDatabase.getDevice(mDb!!, device!!.deviceName)
            input_device_name.setText(auraSwitch.loads[0].name)
            btnTest.visibility = View.GONE
            tvMessage.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
            tvTitle.text = getString(R.string.edit_curtain)
        } else {
            tvTitle.text = getString(R.string.set_up_curtain)
        }
        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        allDeviceList = localSqlDatabase.getAllDevicesScenes(mDb!!, Constant.HOME!!)
        aura_sense_device = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        IpListDevices = IP.getIpDevices()
        for (senseDevice in aura_sense_device) {
            if (senseDevice.home == Constant.HOME) {
                ConfigureLoadActivity.SENSE_NAME = senseDevice.aura_sense_full_name
                ConfigureLoadActivity.SENSE_UIUD = senseDevice.sense_uiud
                break
            }
        }

        if (type != "edit") {
            if (aura_sense_device.size == 1 && auraSwitch.esp_device!!.contains("Sense")) {
                for (device in allDeviceList) {
                    for (l in IpListDevices) {
                        if (l.name == device.name && l.owned == 0) {
                            notifyData = JsonHelper().notifyDevice(ConfigureLoadActivity.SENSE_NAME.toString(), ConfigureLoadActivity.SENSE_UIUD.toString(), l.uiud!!, Constant.SLAVE)
                            sendEspHandler(notifyData,l.ip!!,l.name!!,"")
                        }
                    }
                }
            } else if (aura_sense_device.size > 0 && !auraSwitch.esp_device!!.contains("Sense")) {
                for (l in IpListDevices) {
                    if (l.name == auraSwitch.name) {
                        notifyData = JsonHelper().notifyDevice(ConfigureLoadActivity.SENSE_NAME.toString(), ConfigureLoadActivity.SENSE_UIUD.toString(), UIUD, Constant.SLAVE)
                        sendEspHandler(notifyData,l.ip!!,l.name!!,"")
                    }
                }
            } else if (aura_sense_device.size > 1 && auraSwitch.esp_device!!.contains("Sense")) {
                for (l in IpListDevices) {
                    if (l.name == auraSwitch.name) {
                        notifyData = JsonHelper().notifyDevice(ConfigureLoadActivity.SENSE_NAME.toString(), ConfigureLoadActivity.SENSE_UIUD.toString(), UIUD, Constant.SLAVE)
                        sendEspHandler(notifyData,l.ip!!,l.name!!,"")
                    }
                }
            }
        }
        roomList.add(auraSwitch.room)
        for (dummy in listRoom) {
            if (dummy.type == "room") {
                if ((dummy.sharedHome == Constant.HOME) or (dummy.sharedHome == "default")) {
                    if (dummy.name != auraSwitch.room) {
                        roomList.add(dummy.name)
                    }
                }

            }
        }

    }

    private fun setListner() {
        switchFav?.setOnCheckedChangeListener { _, isChecked ->
            loads[0].favourite = isChecked
        }

        home.setOnClickListener(this)

        spinnerRoom.adapter = ArrayAdapter(this, R.layout.spinner_item_list, roomList)
        spinnerRoom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                roomSelected = spinnerRoom.selectedItem.toString()
                roomIconSelected = position
            }

        }

        btnTest.setOnClickListener(this)
        btnSave.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnTest -> {
                if (input_device_name.text.isNullOrEmpty()) {
                    input_device_name.error = getString(R.string.please_give_name)
                    input_device_name.requestFocus()
                } else {
                    if (checkFirst == 0) {
                        val data = jsonhelper.testCurtain(UIUD)
                        sendEspHandler(data, auraSwitch.ip!!, auraSwitch.name, "")
                    } else {
                        val data = jsonhelper.testAgainCurtain(UIUD)
                        sendEspHandler(data, auraSwitch.ip!!, auraSwitch.name, "")
                    }
                }

            }
            R.id.btnSave -> {
                Utils.hideSoftKeyboard(this)
                saveData(auraSwitch, getString(R.string.close_curtaing))
            }
            R.id.home -> {
                onBackPressed()
            }
        }
    }

    private fun sendEspHandler(data: String, ip: String, name: String, type: String) {
        try {
            if (espHandler == null) {
                espHandler = EspHandler(this)
            }
            Log.d("DATA_REQUEST", data)
                espHandler?.getResponseData(data, ip, name, type)

        } catch (e: Exception) {
            Log.d("error", "Error in ESP_Handler_Connection")
        }
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        AppExecutors().mainThread().execute {
            updateData(decryptedData)
        }
    }

    private fun updateData(decryptedData: String) {
        if (decryptedData.contains("ERROR")) {
            val message = "Some error occurred, please enable and disable Wi-Fi"
            SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(message).show()
        } else {
            val updatedDevice: AuraSwitch = jsonhelper.deserializeTcp(decryptedData)
            Log.d("RECEIVED_DATA", "$updatedDevice")
            when (updatedDevice.type) {
                4 -> {
                    if (checkFirst == 0) {
                        DoubleBtnDialog.with(this).setHeading(getString(R.string.alert))
                                .setMessage(getString(R.string.message_dialog))
                                .setCallback(object : DoubleBtnDialog.OnActionPerformed {
                                    override fun negative() {
                                        checkFirst = 1
                                        btnTest.text = getString(R.string.test_close)
                                    }

                                    override fun positive() {
                                        showFinalDialog(updatedDevice)
                                    }

                                }).show()
                    } else {
                        showFinalDialog(updatedDevice)
                    }
                }
            }
        }
    }

    fun showFinalDialog(updatedDevice: AuraSwitch) {
        DoubleBtnDialog.with(this).setHeading(getString(R.string.alert))
                .setMessage(getString(R.string.message_dialog))
                .setOptionNegative(getString(R.string.close_curtaing))
                .setOptionPositive(getString(R.string.open_curtain))
                .setCallback(object : DoubleBtnDialog.OnActionPerformed {
                    override fun negative() {
                        saveData(updatedDevice, getString(R.string.close_curtaing))
                    }

                    override fun positive() {
                        saveData(updatedDevice, getString(R.string.open_curtain))
                    }

                }).show()
    }

    fun saveData(updatedDevice: AuraSwitch, states: String) {
        progressBar.visibility = View.VISIBLE
        var flag = true
        for (ip in IpListDevices) {
            if (ip.name == updatedDevice.name) {
                ip.uiud = UIUD
                ip.owned = 0
                ip.module = 3
                flag = false
                break
            }
        }
        if (flag) {
            val ip = IpModel()
            ip.name = updatedDevice.name
            ip.fullDeviceName = auraSwitch.esp_device
            ip.owned = 0
            ip.uiud = UIUD
            ip.curtainStates = states
            ip.ip = auraSwitch.ip
            ip.curtainState[0] =   if (checkFirst == 0) 0 else 1
            ip.curtainState[1] =  if (checkFirst == 0) 1 else 0
            ip.aws = false
            ip.module = 3
            ip.local = false
            ip.room = roomSelected
            ip.home = Constant.HOME!!.trim()
            IpListDevices.add(ip)
            IP.registerIpDevice(ip)
        }
        loads[0].module = 3
        loads[0].name = input_device_name.text.toString()
        loads[0].type = "Curtain"
        loads[0].loadType = "Curtain"
        loads[0].curtainState = states
        if(device != null){
            loads[0].curtainState0 = device!!.curtainState0
            loads[0].curtainState1 = device!!.curtainState1
        }else{
            loads[0].curtainState0 = if (checkFirst == 0) 0 else 1
            loads[0].curtainState1 = if (checkFirst == 0) 1 else 0
        }

        auraSwitch.loads = loads
        auraSwitch.uiud = UIUD
        if (auraSwitch.thing != null) {
            localSqlDatabase.insertDevice(mDb!!, Constant.HOME!!, roomSelected!!, UIUD, updatedDevice.name, Gson().toJson(loads), auraSwitch.thing!!)
        } else {
            localSqlDatabase.insertDevice(mDb!!, Constant.HOME!!, roomSelected!!, UIUD, updatedDevice.name, Gson().toJson(loads), "null")
        }
        localSqlUtils.replaceIpList(mDbUtils!!, "device", IpListDevices)
        thread {
            rulesTableDo.insertDeviceLoads(auraSwitch)
            runOnUiThread {
                progressBar.visibility = View.GONE
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
        }
    }

}
