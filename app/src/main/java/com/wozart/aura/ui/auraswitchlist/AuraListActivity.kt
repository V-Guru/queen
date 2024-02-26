package com.wozart.aura.ui.auraswitchlist

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import aura.wozart.com.aura.entity.amazonaws.models.nosql.ThingTableDO
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.aura.ui.auraswitchlist.OnAdapterInteractionListener
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.DeviceTableHandler
import com.wozart.aura.data.dynamoDb.ThingTableHandler
import com.wozart.aura.data.model.DeviceUiud
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.model.ThingError
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.AuraSwitchLoad
import com.wozart.aura.entity.network.ConnectTask
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.curtain.CurtainSetUpActivity
import com.wozart.aura.ui.dashboard.GridAutoFitLayoutManager
import com.wozart.aura.ui.home.ConfigureLoadActivity
import com.wozart.aura.ui.home.ConfigureLoadActivity.Companion.SENSE_MODE
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.Encryption
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_aura_list.*
import kotlinx.android.synthetic.main.dialog_aura_device_code.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import kotlin.concurrent.thread


class AuraListActivity : BaseAbstractActivity(), OnAdapterInteractionListener, AnkoLogger, ConnectTask.TcpMessageReceiver, EspHandler.OnEspHandlerMessage {


    lateinit var adapter: AuraListAdapter
    private var auraSwitches: MutableList<AuraSwitch> = ArrayList()
    var keysSetFlags = ArrayList<Int>()
    private var Nsd: Nsd? = null
    private var jsonHelper: JsonHelper = JsonHelper()
    private val deviceDynamoDb = DeviceTableHandler()
    private val thingDynamoDb = ThingTableHandler()
    private val localSqlDatabase = DeviceTable()
    private var mDb: SQLiteDatabase? = null
    private var UIUD: String? = null
    private var CLOUD_CONNECT = false
    private var resetAllKeysFlag: Boolean = false
    var thing: ThingTableDO? = null
    var allAwsKeys: MutableList<String> = ArrayList()
    lateinit var configuredIpAddress: String
    var reconnectTries: Int = 0
    var UiudList: MutableList<DeviceUiud> = ArrayList()
    private var mDbUtils: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    private var IP = IpHandler()
    var ERROR_TAG = "ERRORCONFIGURE"
    var deviceType: String? = null
    var nsdHandler: Handler? = null
    var espHandler: EspHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aura_list)
        init()
        setAdapter()

    }

    fun setAdapter() {
        adapter.init(auraSwitches)
        listSwitches.layoutManager = GridAutoFitLayoutManager(this, resources.getDimensionPixelSize(R.dimen.wifi_device_item_size))
        listSwitches.adapter = adapter
    }

    fun init() {
        Nsd = Nsd()
        Nsd?.getInstance(this, "WIFI")
        nsdDiscover()
        LocalBroadcastManager.getInstance(this).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        deviceType = PreferenceManager.getDefaultSharedPreferences(this).getString(Constant.DEVICE_TITLE, "")
        if (deviceType != null) {
            tv_title.text = getString(R.string.available, deviceType)
        }
        if (Constant.HOME == null) {
            Constant.HOME = PreferenceManager.getDefaultSharedPreferences(this).getString("HOME", "My Home")
        }
        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase

        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase
        adapter = AuraListAdapter(this, this)
        back.setOnClickListener {
            this.finish()
        }
        imgScan.setOnClickListener {
            UiudList.clear()
            scan()
        }
        imgScan.visibility = View.INVISIBLE
        nsdHandler = Handler()
        onCompletionOfScan()
    }

    private fun nsdDiscover() {
        Nsd?.setBroadcastType("WIFI")
        Nsd?.initializeNsd()
        Nsd?.discoverServices()
    }

    private fun sendEspHandler(data: String, ip: String, name: String) {
        try {
            if (espHandler == null) {
                espHandler = EspHandler(this)
            }
            Log.d("DATA_REQUEST", data)
            espHandler?.getResponseData(data, ip, name, "")

        } catch (e: Exception) {
            Log.d("error", "Error in ESP_Handler_Connection")
        }
    }


    /**
     * Data from the TcpServer : Asynchronous
     */
    var checkDeviceFound: DeviceUiud? = null
    private val onNSDServiceResolved = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val name = intent.getStringExtra("name")
            val ip = intent.getStringExtra("ip")
            if (UiudList.size > 0) {
                checkDeviceFound = UiudList.find { it.name == name }
                if (checkDeviceFound != null) {
                    UiudList.remove(checkDeviceFound)
                    checkDeviceFound = null
                }
            }
            val deviceuiud = DeviceUiud()
            deviceuiud.name = name
            deviceuiud.ip = ip
            deviceuiud.uiud = Constant.UNPAIRED
            UiudList.add(deviceuiud)

            if (checkDeviceFound == null) {
                try {
                    if (name!!.startsWith("Aura Switch") && !name.contains("Pro")) {
                        sendTcpConnect(Constant.UNPAIRED, ip!!, name)
                    } else {
                        sendEspHandler(jsonHelper.initialData(Constant.UNPAIRED), ip!!, name)
                    }
                } catch (e: Exception) {

                }
            }

        }
    }

    private fun sendTcpConnect(uiud: String, ip: String, name: String) {
        ConnectTask(this, this, jsonHelper.initialData(uiud), ip, name).execute("")
    }

    private fun sendTcpData(data: String, ip: String, name: String) {
        ConnectTask(this, this, data, ip, name).execute("")
    }


    private fun sendEspHandlerPin(data: String, ip: String, name: String) {
        sendEspHandler(data, ip, name)
    }

    private fun scan() {
        auraSwitches.clear()
        onStartingScan()
        Nsd?.initializeNsd()
        Nsd?.stopDiscovery()
        Nsd?.setBroadcastType("WIFI")
        Nsd?.discoverServices()
        nsdHandler?.removeCallbacksAndMessages(null)
        onCompletionOfScan()

    }

    private fun onCompletionOfScan() {
        Thread(Runnable {
            nsdHandler?.postDelayed({
                progress_bar.visibility = View.INVISIBLE
                imgScan.visibility = View.VISIBLE
            }, 10000)

        }).start()

    }

    private fun onStartingScan() {
        progress_bar.visibility = View.VISIBLE
        imgScan.visibility = View.INVISIBLE
        tvRestartDevice.visibility = View.GONE
        tvScanSwitch.visibility = View.GONE
    }

    private fun placeScanUi() {
        val params = imgScan.layoutParams as RelativeLayout.LayoutParams
        if (auraSwitches.size > 0) {
            tvRestartDevice.visibility = View.VISIBLE
            tvScanSwitch.visibility = View.GONE
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params.setMargins(0, 0, 0, 100)

        } else {
            tvScanSwitch.visibility = View.VISIBLE
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
        }
        imgScan.layoutParams = params
    }

    override fun onSelectAuraDevice(switch: AuraSwitch) {
        openDialog(switch)
    }

    private fun openDialog(device: AuraSwitch) {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_aura_device_code)
        dialog.tvDialogTitle.text = device.name

        if (device.aws == 1) {
            dialog.switchConnect.visibility = View.INVISIBLE
            dialog.tvConnectAws.visibility = View.INVISIBLE
            dialog.tvMore.visibility = View.INVISIBLE
            CLOUD_CONNECT = false
        } else {
            dialog.switchConnect.visibility = View.VISIBLE
            dialog.tvConnectAws.visibility = View.VISIBLE
            dialog.tvMore.visibility = View.VISIBLE
        }

        dialog.btnSubmt.setOnClickListener {
            Utils.hideSoftKeyboard(this)
            val encryptedPin = Encryption.SHA256(dialog.inputDeviceCode.value.toString())
            UIUD = Encryption.generateUIUD(device.id!!)
            val data = jsonHelper.pairingData(UIUD!!, encryptedPin)
            for (y in UiudList) {
                if (y.name!!.contains(device.deviceEndName)) {
                    if (y.name!!.startsWith("Aura Switch") && !y.name!!.contains("Pro")) {
                        sendTcpData(data, device.ip!!, device.name)
                    } else {
                        sendEspHandlerPin(data, y.ip!!, device.name)
                    }
                }
            }
            if (device.aws == 0) {
                this.CLOUD_CONNECT = dialog.switchConnect.isChecked
            }

            dialog.cancel()
        }
        dialog.setOnCancelListener {
            hideSoftKeyBoard()
        }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (Nsd == null) {
            Nsd = Nsd()
            Nsd?.getInstance(this, "WIFI")
        }
        nsdDiscover()
        LocalBroadcastManager.getInstance(this).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
    }


    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNSDServiceResolved)
        // Nsd?.stopDiscovery()
        Nsd = null
    }

    override fun onDestroy() {
        super.onDestroy()
        nsdHandler?.removeCallbacksAndMessages(null)
    }

    /**
     * Transferring Keys to Device
     */

    private fun getKeys(device: AuraSwitch) {
        resetAllKeysFlag = true
        keysSetFlags.add(0)
        keysSetFlags.add(1)
        keysSetFlags.add(2)
        configuredIpAddress = device.ip!!
        reconnectTries = 0
        ConnectTask(this, this, "{\"type\":8, \"set\":1}", device.ip!!, device.name).execute("")

        thread {
            try {
                val thingAlreadyExists = deviceDynamoDb.checkThing(UIUD!!.substring(0, Math.min(UIUD!!.length, 12)))
                if (thingAlreadyExists!!.error == "error") {
                    if (thingAlreadyExists.version == "error") {
                        progress_bar.visibility = View.INVISIBLE
                        longToast("No internet Please check!!")
                        val gson = Gson()
                        startActivity<ConfigureLoadActivity>("DEVICE" to gson.toJson(device), "UIUD" to UIUD)
                        this.finish()
                    } else {
                        if (thingAlreadyExists.thing == null) {
                            thing = thingDynamoDb.searchAvailableDevices()!!

                        } else {
                            thing = thingDynamoDb.thingDetails(thingAlreadyExists.thing!!)
                        }
                        allAwsKeys.clear()
                        val data = jsonHelper.awsRegionThing(thing!!.region!!, thing!!.thing!!)
                        allAwsKeys.add(data)
                        val certs = jsonHelper.certificates(thing!!.certificate!!)
                        allAwsKeys.addAll(certs)
                        val keys = jsonHelper.privateKeys(thing!!.privateKey!!)
                        allAwsKeys.addAll(keys)
                        allAwsKeys.add(data)
                        //longSnackbar(findViewById(android.R.id.content), "Exchanging data.")
                        sendDataAllKeys(allAwsKeys as ArrayList<String>, device)
                        val localThing = ThingError()
                        localThing.region = thing!!.region
                        localThing.privateKey = thing!!.privateKey
                        localThing.certificate = thing!!.certificate
                        localThing.thing = thing!!.thing
                        localThing.version = "Version1"
                        localThing.error = "success"
                        localSqlUtils.replaceThing(mDbUtils!!, device.name, localThing)
                    }
                } else {
                    allAwsKeys.clear()
                    val data = jsonHelper.awsRegionThing(thingAlreadyExists.region!!, thingAlreadyExists.thing!!)
                    allAwsKeys.add(data)
                    val certs = jsonHelper.certificates(thingAlreadyExists.certificate!!)
                    allAwsKeys.addAll(certs)
                    val keys = jsonHelper.privateKeys(thingAlreadyExists.privateKey!!)
                    allAwsKeys.addAll(keys)
                    allAwsKeys.add(data)
                    sendDataAllKeys(allAwsKeys as java.util.ArrayList<String>, device)
                    //store to local sql
                    val localThing = ThingError()
                    localThing.region = thingAlreadyExists.region
                    localThing.privateKey = thingAlreadyExists.privateKey
                    localThing.certificate = thingAlreadyExists.certificate
                    localThing.thing = thingAlreadyExists.thing
                    localThing.version = "Version2"
                    localThing.error = "success"
                    localSqlUtils.replaceThing(mDbUtils!!, thingAlreadyExists.thing!!, localThing)
                }
            } catch (e: Exception) {
                progress_bar.visibility = View.INVISIBLE
                //longSnackbar(findViewById(android.R.id.content), "Thing not matched $thing,Please Consult the developer $thing")
                Log.d(ERROR_TAG, "getKeys : " + e)
            }
        }
    }

    private fun getKeysMissing(reg: Int, cert: Int, keys: Int) {
        val runnable = Runnable {
            try {

                val data = ArrayList<String>()
                if (reg == 0) {
                    data.add(allAwsKeys[0])
                }
                for (i in 0..19) {
                    val d = (cert shr i) and 1
                    if (d % 2 == 0) {
                        data.add(allAwsKeys[i + 1])
                    }
                }
                for (i in 0..26) {
                    val d = (keys shr i) and 1
                    if (d % 2 == 0) {
                        data.add(allAwsKeys[i + 21])
                    }
                }
                sendDataAllKeysMissing(data)
            } catch (e: Exception) {
                progress_bar.visibility = View.INVISIBLE
                Log.d(ERROR_TAG, "getKeysMissing : " + e)
            }
        }
        val getAvailableDevices = Thread(runnable)
        getAvailableDevices.start()
    }


    private fun sendDataAllKeys(data: ArrayList<String>, device: AuraSwitch) {

        for (key in data) {
            try {
                ConnectTask(this, this, key, device.ip!!, device.name).execute("")
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                Log.d(ERROR_TAG, "sendDataAllKeys : " + e)
            }

        }
        resetAllKeysFlag = false
        ConnectTask(this, this, "{\"type\":8, \"set\":0}", device.ip!!, device.name).execute("")

    }

    private fun sendDataAllKeysMissing(data: java.util.ArrayList<String>) {

        for (key in data) {
            try {
                ConnectTask(this, this, key, configuredIpAddress, "XXXXXX").execute("")
                Thread.sleep(20)
            } catch (e: InterruptedException) {
                Log.d(ERROR_TAG, "sendDataAllKeysMissing : " + e)
            }
        }
        resetAllKeysFlag = false
        ConnectTask(this, this, "{\"type\":8, \"set\":0}", configuredIpAddress, "XXXXXX").execute("")
    }

    override fun onTcpMessageReceived(message: String) {

        updateMessage(message)

    }

    private fun makeDeviceList(dummyDevice: AuraSwitch) {
        if (auraSwitches.size > 0) {
            var flag = true
            for (stc in auraSwitches) {
                flag = true
                if (stc.name == dummyDevice.name) {
                    flag = false
                    break
                }
            }
            if (flag) {
                auraSwitches.add(dummyDevice)
            }
        } else {
            auraSwitches.add(dummyDevice)
        }
        setAdapter()
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        AppExecutors().mainThread().execute {
            updateMessage(decryptedData)
        }
    }

    private fun updateMessage(message: String) {
        if (message.contains("ERROR")) {
            Log.d("ERROR_MESSAGE", "ERROR : $message")
        } else {
            val dummyDevice = jsonHelper.deserializeTcp(message)
            val gson = Gson()
            Log.d("DEBUG_IN", "DATA : ${dummyDevice.type}")
            when (dummyDevice.type) {
                1 -> {
                    if (dummyDevice.dsy == 0) {
                        dummyDevice.deviceEndName = dummyDevice.name
                        if(dummyDevice.mdl == 1){

                        }else if(dummyDevice.mdl == 2){

                        }else if(dummyDevice.mdl == 8){
                            if(dummyDevice.thing != null){
                                dummyDevice.name = "WLO-"+dummyDevice.thing!!.takeLast(4)
                            }else{
                                dummyDevice.name = "WLO-"+dummyDevice.name
                            }
                            makeDeviceList(dummyDevice)
                        }
                        placeScanUi()
                    }
                }

                2 -> {
                    if (dummyDevice.error == 0) {
                        if (dummyDevice.mdl == 3) {
                            for (y in UiudList) {
                                if (y.name!!.contains(dummyDevice.name)) {
                                    dummyDevice.ip = y.ip
                                    dummyDevice.esp_device = y.name
                                    break
                                }
                            }

                        } else {
                            for (y in UiudList) {
                                if (y.name!!.contains(dummyDevice.name)) {
                                    if (y.name!!.contains(Constant.DEVICE_UNIVERSAL_IR) || y.name!!.contains("Sense")) {
                                        val remoteDataList = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
                                        val existSense = remoteDataList.filter { Constant.HOME == it.home }
                                        var flag = true
                                        for (remote in remoteDataList) {
                                            if (remote.aura_sence_name == dummyDevice.name) {
                                                remote.sense_uiud = UIUD
                                                remote.sense_owned = 0
                                                flag = false
                                                break
                                            }
                                        }
                                        if (flag) {
                                            val remoteModel = RemoteModel()
                                            val ip = IpModel()
                                            ip.name = dummyDevice.name
                                            ip.fullDeviceName = y.name
                                            ip.owned = 0
                                            ip.uiud = UIUD
                                            ip.ip = y.ip
                                            ip.home = Constant.HOME
                                            ip.room = "Living Room"
                                            ip.aws = false
                                            ip.local = false

                                            remoteModel.aura_sence_name = dummyDevice.name
                                            remoteModel.scense_thing = dummyDevice.thing
                                            remoteModel.sense_owned = 0
                                            remoteModel.aura_sense_full_name = y.name
                                            remoteModel.sense_uiud = UIUD
                                            remoteModel.sense_ip = y.ip.toString()
                                            remoteModel.senseMacId = dummyDevice.id
                                            remoteModel.home = Constant.HOME
                                            remoteModel.room = "Living Room"
                                            remoteModel.sense_aws = false
                                            remoteModel.sense_local = false
                                            val auraSense = AuraSenseConfigure()
                                            val auraSenseConfigParameter = if (dummyDevice.mdl == 15) auraSense.universalSenseRemoteIr() else auraSense.auraSenseDefault()
                                            remoteModel.sense_loads = auraSenseConfigParameter
                                            if (existSense.isNotEmpty()) remoteModel.role = Constant.SLAVE else remoteModel.role = Constant.MASTER
                                            SENSE_MODE = remoteModel.role
                                            remoteDataList.add(remoteModel)
                                            IP.registerIpDevice(ip)
                                        }
                                        localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", remoteDataList)

                                    } else {
                                        val IpListDevices = localSqlUtils.getIpList(mDbUtils!!, "device")
                                        var flag = true
                                        for (ip in IpListDevices) {
                                            if (ip.name == dummyDevice.name) {
                                                ip.uiud = UIUD
                                                ip.owned = 0
                                                ip.home = Constant.HOME
                                                flag = false
                                                break
                                            }
                                        }
                                        if (flag) {
                                            val ip = IpModel()
                                            ip.name = dummyDevice.name
                                            ip.fullDeviceName = y.name
                                            ip.owned = 0
                                            ip.uiud = UIUD
                                            ip.ip = y.ip
                                            ip.home = Constant.HOME
                                            ip.aws = false
                                            ip.module = dummyDevice.mdl
                                            ip.local = false
                                            IpListDevices.add(ip)
                                            IP.registerIpDevice(ip)
                                        }
                                        val auraLoads = AuraSwitchLoad()
                                        var loads: MutableList<AuraSwitchLoad> = ArrayList()
                                        if (dummyDevice.mdl == 4) {
                                            loads = auraLoads.defaultLoadList(dummyDevice.mdl)
                                        } else if (dummyDevice.mdl == 6 || dummyDevice.mdl == 14) {
                                            loads = auraLoads.defaultSwitchProLoadList(dummyDevice.mdl)
                                        } else if (dummyDevice.mdl == 2 || dummyDevice.mdl == 12) {
                                            loads = auraLoads.twoModuleLoad(dummyDevice.mdl)
                                        } else if (dummyDevice.mdl == 7) {
                                            loads = auraLoads.rgbLoads(dummyDevice.mdl)
                                        } else if (dummyDevice.mdl == 8) {
                                            loads = auraLoads.rgbTunableLoads(dummyDevice.mdl)
                                        } else if (dummyDevice.mdl == 1) {
                                            loads = auraLoads.auraPlugLoad(dummyDevice.mdl)
                                        } else if (dummyDevice.mdl == 5) {
                                            loads = auraLoads.fiveLoadList(dummyDevice.mdl)
                                        } else if (dummyDevice.mdl == 11) {
                                            loads = auraLoads.twoModuleDimmerLoad(dummyDevice.mdl)
                                        }
                                        try {
                                            if (dummyDevice.thing != null) {
                                                localSqlDatabase.insertDevice(mDb!!, Constant.HOME!!, "Living Room", UIUD!!, dummyDevice.name, gson.toJson(loads), dummyDevice.thing!!)
                                            } else {
                                                localSqlDatabase.insertDevice(mDb!!, Constant.HOME!!, "Living Room", UIUD!!, dummyDevice.name, gson.toJson(loads), "null")
                                            }
                                            localSqlUtils.replaceIpList(mDbUtils!!, "device", IpListDevices)
                                        } catch (e: Exception) {
                                            Log.d("NULLPOINTER", e.toString())
                                        }
                                    }
                                    dummyDevice.ip = y.ip
                                    dummyDevice.esp_device = y.name
                                }
                            }
                        }

                        if (CLOUD_CONNECT) {
                            //longSnackbar(findViewById(android.R.id.content), "Enabling Remote Access..")
                            getKeys(dummyDevice)
                            onStartingScan()
                        } else {
                            if (dummyDevice.mdl != 3) {
                                //longSnackbar(findViewById(android.R.id.content), "Fetching thing from database..")
                                SingleBtnDialog.with(this).setHeading(getString(R.string.success_text)).setMessage(getString(R.string.device_paired)).show()
                                //longSnackbar(findViewById(android.R.id.content), "Device Paired Successfully")
                                startActivity<ConfigureLoadActivity>("DEVICE" to gson.toJson(dummyDevice), "UIUD" to UIUD)
                                this.finish()
                            } else {
                                startActivity<CurtainSetUpActivity>("DEVICE" to gson.toJson(dummyDevice), "UIUD" to UIUD, "TYPE" to "create")
                                this.finish()
                            }

                        }
                    }
                    if (dummyDevice.error == 1) {
                        SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.wrong_pin)).show()
                        progress_bar.visibility = View.INVISIBLE
                        imgScan.visibility = View.VISIBLE
                    }
                }

                5 -> {
                    keysSetFlags[0] = dummyDevice.r
                    keysSetFlags[1] = dummyDevice.c
                    keysSetFlags[2] = dummyDevice.k

                }

                6 -> {
                    keysSetFlags[0] = dummyDevice.r
                    keysSetFlags[1] = dummyDevice.c
                    keysSetFlags[2] = dummyDevice.k

                }
                7 -> {
                    keysSetFlags[0] = dummyDevice.r
                    keysSetFlags[1] = dummyDevice.c
                    keysSetFlags[2] = dummyDevice.k
                }

                8 -> {
                    if (!resetAllKeysFlag) {
                        if (dummyDevice.error == 0) {
                            for (y in UiudList) {
                                if (y.name!!.contains(dummyDevice.name)) {
                                    dummyDevice.ip = y.ip
                                    dummyDevice.esp_device = y.name
                                    break
                                }
                            }
                            keysSetFlags.clear()
                            progress_bar.visibility = View.INVISIBLE
                            longToast("Device Paired Successfully")
                            startActivity<ConfigureLoadActivity>("DEVICE" to gson.toJson(dummyDevice), "UIUD" to UIUD)
                            this.finish()
                        } else {
                            val a = keysSetFlags[0]
                            val b = keysSetFlags[1]
                            val c = keysSetFlags[2]
                            if ((a == 1) and (b == 134217727) and (c == 1048575)) {
                                progress_bar.visibility = View.INVISIBLE
                                longToast("Wrong keys added Report to WOZART!!")
                                startActivity<ConfigureLoadActivity>("DEVICE" to gson.toJson(dummyDevice), "UIUD" to UIUD)
                                this.finish()
                            } else {
                                if (reconnectTries > 2) {
                                    progress_bar.visibility = View.INVISIBLE
                                    longToast("Wrong keys added Report to WOZART!!")
                                    startActivity<ConfigureLoadActivity>("DEVICE" to gson.toJson(dummyDevice), "UIUD" to UIUD)
                                    this.finish()
                                } else {
                                    longToast("Getting Missing Keys")
                                    getKeysMissing(keysSetFlags[0], keysSetFlags[1], keysSetFlags[2])
                                    reconnectTries++
                                }
                            }
                        }
                    }

                }

                else -> {
                    onCompletionOfScan()
                }
            }

        }
    }

}

