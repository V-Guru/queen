package com.wozart.aura.ui.dashboard.more

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.Customization
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.model.CustomizationList
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.DeviceTableModel
import com.wozart.aura.entity.network.ConnectTask
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.adapter.CustomizationAdapter
import com.wozart.aura.ui.dashboard.listener.UpgradeFirmwareListener
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import kotlinx.android.synthetic.main.activity_customizations.*
import kotlinx.coroutines.launch

class CustomizationActivity : AppCompatActivity(), CustomizationAdapter.onDeviceDeletedListener, ConnectTask.TcpMessageReceiver, EspHandler.OnEspHandlerMessage, CustomizationAdapter.onActivtyStart, View.OnClickListener, UpgradeFirmwareListener {


    private val localSqlDatabase = DeviceTable()
    private var mDb: SQLiteDatabase? = null
    var userType: Boolean = true
    val localSqlUtils = UtilsTable()
    var mDbUtils: SQLiteDatabase? = null
    private var IP = IpHandler()
    var IpListDevices: MutableList<IpModel> = java.util.ArrayList()
    private var auraSwitches: MutableList<AuraSwitch> = java.util.ArrayList()
    var allDeviceList = java.util.ArrayList<DeviceTableModel>()
    private var mDbDevice: SQLiteDatabase? = null
    private var Nsd: Nsd? = null
    var adapter: CustomizationAdapter? = null
    var senseDevice: MutableList<RemoteModel> = arrayListOf()
    var espHandler: EspHandler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customizations)
        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase
        Nsd = Nsd()
        Nsd?.getInstance(this, "WIFI")
        nsdDiscover()
        LocalBroadcastManager.getInstance(this).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        progress_bar_set.visibility = View.VISIBLE
        val listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        for (x in listRoom) {
            if (x.name == Constant.HOME) {
                if (x.sharedHome == "guest") {
                    userType = false
                }
            }
        }
        senseDevice = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        btnOpenWebsite.setOnClickListener(this)
        back.setOnClickListener(this)
        setUpRecyclerView()
        populateRecyclerView()
        init()
    }

    private fun nsdDiscover() {
        Nsd?.setBroadcastType("WIFI")
        Nsd?.initializeNsd()
        Nsd?.discoverServices()

    }

    private val onNSDServiceResolved = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val name = intent.getStringExtra("name")
            val ip = intent.getStringExtra("ip")
            val device = name!!.substring(name.length - 6, name.length)
            var ipDevice = IpModel()
            ipDevice.ip = ip
            ipDevice.name = device
            ipDevice.fullDeviceName = name
            var isDeviceExists = false
            for (l in IP.getIpDevices()) {
                if (l.name == ipDevice.name) {
                    l.ip = ip
                    l.fullDeviceName = name
                    ipDevice = l
                    isDeviceExists = true
                    IP.registerIpDevice(ipDevice)
                    break
                }
            }
            if (isDeviceExists) {
                if (name.contains("Aura Switch")) {
                    sendTcpConnect(ipDevice.uiud!!, ipDevice.ip!!, ipDevice.name!!)
                } else {
                    sendEspHandler(JsonHelper().initialData(ipDevice.uiud!!), ipDevice.ip!!, ipDevice.name!!, "")
                }
            }

        }
    }

    override fun onDeviceDeleted(name: String, ip: String, uiud: String, esp_device_name: String) {
        lifecycleScope.launch {
            if (esp_device_name.contains("Aura Switch")) {
                ConnectTask(this@CustomizationActivity, this@CustomizationActivity, "{\"type\":8,\"name\":\"$name\",\"set\":1,\"uiud\":\"$uiud\",\"pair\":1}", ip, name).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            } else {
                val deleteDataCommand = "{\"type\":8,\"name\":\"$name\",\"set\":1,\"uiud\":\"$uiud\",\"pair\":1}"
                sendEspHandler(deleteDataCommand, ip, name, "")
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

    override fun onTcpMessageReceived(message: String) {
        val data = message
        Log.d("DATATCP", data)
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        AppExecutors().mainThread().execute {
            if (decryptedData.contains("ERROR")) {

            } else {
                val auraSwitch: AuraSwitch = JsonHelper().deserializeTcp(decryptedData)
                when (auraSwitch.type) {
                    9 -> {

                    }
                    else -> {
                        if (auraSwitch.aws == 1 && auraSwitch.error == 0) {
                            updateAwsDevice(auraSwitch)
                        }
                    }
                }

            }
        }
    }

    private fun updateAwsDevice(auraSwitch: AuraSwitch): MutableList<AuraSwitch> {
        if (auraSwitch.aws == 1) {
            for (awsDeviceConnect in auraSwitches) {
                if (auraSwitch.name == awsDeviceConnect.name) {
                    awsDeviceConnect.aws = auraSwitch.aws
                    awsDeviceConnect.error = auraSwitch.error
                    awsDeviceConnect.CloudPresence = true
                }
            }
        }
        if (adapter != null) {
            adapter?.notifyDataSetChanged()
        }
        return auraSwitches
    }

    private fun setUpRecyclerView() {
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        customization_recycler_view.layoutManager = linearLayoutManager
    }

    private fun populateRecyclerView() {
        val rooms = localSqlDatabase.getRooms(mDb!!, Constant.HOME!!)
        val sectionModelArrayList: ArrayList<CustomizationList> = ArrayList()

        for (room in rooms) {
            val itemArrayList: MutableList<Customization> = ArrayList()
            for (device in localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, room.roomName!!)) {
                val customization = Customization()
                customization.deviceName = device.name
                customization.isConnected = device.thing != null
                itemArrayList.add(customization)
            }
            sectionModelArrayList.add(CustomizationList(room.roomName!!, itemArrayList))
        }
        for (device in senseDevice.filter { it.home == Constant.HOME }) {
            var flag = false
            val itemArrayList: MutableList<Customization> = ArrayList()
            for (l in sectionModelArrayList) {
                if (l.customizationLabel == device.room) {
                    val customization = Customization()
                    customization.deviceName = device.aura_sence_name
                    customization.isConnected = device.scense_thing != null
                    l.itemArrayList.add(customization)
                    flag = true
                    break
                }
            }
            if (!flag) {
                val customization = Customization()
                customization.deviceName = device.aura_sence_name
                customization.isConnected = device.scense_thing != null
                itemArrayList.add(customization)
                sectionModelArrayList.add(CustomizationList(device.room!!, itemArrayList))
            }

        }
        if (sectionModelArrayList.isEmpty()) noDevicetv.visibility = View.VISIBLE
        progress_bar_set.visibility = View.GONE
        adapter = CustomizationAdapter(this, sectionModelArrayList, userType, this, auraSwitches, this)
        adapter?.upgradeListener = this
        customization_recycler_view.adapter = adapter
    }

    fun init() {
        val dbHelper = DeviceDbHelper(this)
        mDbDevice = dbHelper.writableDatabase
        allDeviceList = localSqlDatabase.getDeviceTable(mDbDevice!!)
        IpListDevices = IP.getIpDevices()
        val rooms = localSqlDatabase.getRooms(mDb!!, Constant.HOME!!)
        for (l in IpListDevices) {
            for (room in rooms) {
                for (device in localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, room.roomName!!)) {
                    val switch = AuraSwitch()
                    if (device.name == l.name) {
                        switch.room = device.room.toString()
                        if (l.aws || l.local) {
                            switch.CloudPresence = true
                        }
                        switch.esp_device = l.fullDeviceName
                        switch.uiud = l.uiud!!
                        switch.ip = l.ip
                        switch.mdl = l.module
                        switch.loads = device.loads
                        switch.name = device.name
                        auraSwitches.add(switch)
                        break
                    }
                }
            }
        }

        for (device in senseDevice.filter { it.home == Constant.HOME }) {
             for (l in IpListDevices) {
                if (l.name == device.aura_sence_name) {
                    val switch = AuraSwitch()
                    switch.room = device.room.toString()
                    switch.CloudPresence = l.aws || l.local || device.sense_aws
                    switch.esp_device = device.aura_sense_full_name
                    switch.uiud = device.sense_uiud.toString()
                    switch.ip = l.ip
                    switch.mdl = l.module
                    switch.aura_sense_confi = device.sense_loads
                    switch.name = device.aura_sence_name.toString()
                    auraSwitches.add(switch)
                    break
                }
            }

        }

    }


    fun sendTcpConnect(uiud: String, ip: String, name: String) {
        ConnectTask(this, this, JsonHelper().initialData(uiud), ip, name).execute("")
    }

    override fun onFinish() {
        val intent = Intent(this, CustomizationActivity::class.java)
        startActivity(intent)
        this.finish()
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

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNSDServiceResolved)
        Nsd?.stopDiscovery()
        Nsd = null
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNSDServiceResolved)
        Nsd = null
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnOpenWebsite -> {
                val uri = Uri.parse("https://www.wozart.com/shop")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            R.id.back -> {
                this.finish()
            }
        }
    }


    override fun onUpgradeDevice(position: Int, data: Any, deviceIp: String?, deviceMdl: Any, viewType: Any) {
        when (viewType) {
            Constant.UPGRADE_DEVICE -> {
                try {
                    if (deviceIp.isNullOrEmpty()) {
                        Toast.makeText(this@CustomizationActivity, "remotely cannot upgrade the device ", Toast.LENGTH_SHORT).show()
                    } else {
                        sendEspHandler(data as String, deviceIp, "", "device_upgrade")
                    }
                } catch (e: Exception) {

                }
            }
        }

    }
}
