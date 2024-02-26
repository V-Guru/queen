package com.wozart.aura.ui.automationlist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.App
import com.wozart.aura.R
import com.wozart.aura.aura.ui.automationlist.OnAutomationListInteractionListener
import com.wozart.aura.aura.ui.dashboard.listener.OnOptionsListener
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.ScheduleTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraComplete
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.DeviceThing
import com.wozart.aura.entity.network.ConnectTask
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.AutomationSceneAdapter
import com.wozart.aura.ui.createautomation.CreateAutomationActivity
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.fragment_automation_list.*
import kotlinx.android.synthetic.main.layout_refresh_home.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 12/09/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0.6
 * ____________________________________________________________________________
 *
 *****************************************************************************/


class AutomationListFragment : androidx.fragment.app.Fragment(), OnOptionsListener, ConnectTask.TcpMessageReceiver, EspHandler.OnEspHandlerMessage, RecyclerItemClicked {

    private val localSqlDatabase = DeviceTable()
    private val localSqlDatabaseSchedule = ScheduleTable()
    private var mDb: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    private var mDbUtils: SQLiteDatabase? = null
    private var mDBAutomation: SQLiteDatabase? = null
    private var mListener: OnAutomationListInteractionListener? = null
    var automationScene = ArrayList<AutomationScene>()
    var remoteDataList: MutableList<RemoteModel> = ArrayList()
    var automationSceneList = ArrayList<AutomationScene>()
    private lateinit var automationAdapter: AutomationSceneAdapter
    private lateinit var nsd: Nsd
    private var automationNameOld: String? = null
    val scheduleDynamoDb = RulesTableHandler()
    var userType = true
    var activity: Activity? = null
    var turnOff: String? = null
    var turnOffTiming: Int = 0
    var IP = IpHandler()
    var ipListDevice: MutableList<IpModel> = ArrayList()
    var localAutomationDelete: String? = null
    var automationToDelete: AutomationScene? = null
    var localData: String? = null
    val uiud_list: ArrayList<String> = ArrayList()
    val device_name: ArrayList<String> = arrayListOf()
    val mdlArray: ArrayList<Int> = arrayListOf()
    var sceneNameArray: ArrayList<String> = ArrayList()
    val deviceThingsList: ArrayList<DeviceThing> = ArrayList()
    var type: String? = null
    var abailableSenseHome: MutableList<RemoteModel> = ArrayList()
    var espHandler: EspHandler? = null

    companion object {
        fun newInstance(): AutomationListFragment {
            return AutomationListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_automation_list, container, false)
        return rootView
    }

    override fun onOptionsClicked(view: View) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dbAutomation = ScheduleDbHelper(requireContext())
        nsd = Nsd()
        nsd.getInstance(requireActivity(), "HOME")
        mDBAutomation = dbAutomation.writableDatabase
        val dbUtils = UtilsDbHelper(requireContext())
        mDbUtils = dbUtils.writableDatabase
        val dbHelper = DeviceDbHelper(requireContext())
        mDb = dbHelper.writableDatabase
        val btnCreateNew = view.findViewById<ImageView>(R.id.btnCreateNew)
        btnCreateNew.visibility = View.VISIBLE
        var listRoom: MutableList<RoomModelJson> = ArrayList()
        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        init()
        for (x in listRoom) {
            if (x.name == Constant.HOME) {
                if (x.sharedHome == "guest") {
                    userType = false
                    break
                }
            }
        }
        if (userType) {
            btnCreateNew.visibility = View.VISIBLE
            btnCreateNew.setOnClickListener {
                val intent = Intent(activity, CreateAutomationActivity::class.java)
                intent.putExtra("automationNameType", "create")
                intent.putExtra("input_name", "new")
                startActivity(intent)
            }
        } else {
            btnCreateNew.visibility = View.INVISIBLE
        }

        for (l in IP.getIpDevices()) {
            remoteDataList.find { it.aura_sence_name == l.name }?.let {
                it.sense_ip = l.ip ?: ""
            }
        }

        val listAutomations = view.findViewById(R.id.listAutomations) as androidx.recyclerview.widget.RecyclerView
        listAutomations.adapter = automationAdapter
        automationAdapter.userType = userType
        nsdDiscovery()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
    }


    private fun nsdDiscovery() {
        nsd.setBroadcastType("HOME")
        nsd.initializeNsd()
        nsd.discoverServices()

    }

    /**
     * Data from the TcpServer : Asynchronous
     */
    private val onNSDServiceResolved = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val name = intent.getStringExtra("name")
            val ip = intent.getStringExtra("ip")
            val device = name!!.substring(name.length - 6, name.length)
            if (remoteDataList.size > 0) {
                remoteDataList.find { it.aura_sence_name == device }?.let {
                    it.sense_ip = ip ?: ""
                }
            }
        }
    }

    private fun init() {
        ipListDevice = IP.getIpDevices()
        remoteDataList = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        automationScene = localSqlDatabaseSchedule.getAutomationScene(mDBAutomation!!, Constant.HOME!!)
        for (automation in automationScene) {
            if (automation.type == "geo") {
                automationSceneList.add(automation)
            } else {
                automationSceneList.add(automation)
            }
        }
        automationAdapter = AutomationSceneAdapter(automationSceneList, this)

        abailableSenseHome.addAll(remoteDataList.filter { it.home == Constant.HOME && it.sense_loads.size > 1 })

    }

    private fun enableDisableMotion(automation: AutomationScene) {
        if (abailableSenseHome.size > 0) {
            automationToDelete = automation
            type = "localData"
            localData = JsonHelper().sendAutomationSenseData(automation, automation.name!!, "motion", device_name, uiud_list, automation.property[0].turnOff!!, Common.getSenseData(automation.routine!!), sceneNameArray.size, sceneNameArray, "edit", mdlArray, automation.remote, abailableSenseHome)
            sendEspHandler(localData!!, abailableSenseHome[0].sense_ip, abailableSenseHome[0].aura_sence_name!!, "")
        } else {
            saveDataToLocal(automation, Constant.ONLINE_MODE)
        }
    }


    private fun deleteAutomationScene(automationScene: String) {
        val roomsList: MutableList<RoomModel> = ArrayList()
        val dbHelper = DeviceDbHelper(requireContext())
        mDb = dbHelper.writableDatabase
        val dummyRooms = localSqlDatabase.getRooms(mDb!!, Constant.HOME!!)
        for (x in dummyRooms) {
            val room = RoomModel()
            room.name = x.roomName
            roomsList.add(room)
        }
        if (!roomsList.isEmpty()) {
            for (x in roomsList) {
                val devices = localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, x.name!!)
            }

        }
    }

    override fun onTcpMessageReceived(message: String) {
        updateScheduleScene()
    }

    private fun updateScheduleScene() {
        for (scheduleScene in automationSceneList) {
            var sceneTurnedState = true
            val sceneDeviceList = ArrayList<AuraComplete>()
            for (sRoom in scheduleScene.room) {
                for (device in sceneDeviceList) {
                    if (sRoom.name == device.room) {
                        for (sDevice in sRoom.deviceList) {
                            if (sDevice.isTurnOn != device.loads[sDevice.index].status) {
                                sceneTurnedState = false
                                break
                            }
                        }
                        if (!sceneTurnedState) {
                            break
                        }
                    }
                }
                if (!sceneTurnedState) {
                    break
                }
            }
            scheduleScene.property[0].AutomationEnable = sceneTurnedState
        }
        automationAdapter.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
        if (context is OnAutomationListInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement OnAutomationListInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        AppExecutors().mainThread().execute{
            updateData(decryptedData)
        }

    }

    private fun updateData(message: String) {
        if (message.contains("ERROR")) {
            if (type == "delete") deleteAutomation(automationToDelete!!, Constant.OFFLINE_MODE) else saveDataToLocal(automationToDelete!!, Constant.OFFLINE_MODE)
        } else {
            requireActivity().findViewById<View>(R.id.layoutProgress).visibility = View.GONE
            val updatedDevice: AuraSwitch = JsonHelper().deserializeTcp(message)
            if (updatedDevice.error == 0) {
                if (type == "delete") {
                    deleteAutomation(automationToDelete!!, Constant.ONLINE_MODE)
                } else {
                    saveDataToLocal(automationToDelete!!, Constant.ONLINE_MODE)
                }

            } else {
                val message = "Device not responding."
                SingleBtnDialog.with(requireActivity()).setHeading(requireActivity().getString(R.string.alert)).setMessage(message).show()
            }
        }
    }


    @SuppressLint("CutPasteId")
    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is AutomationScene) {
            when (viewType) {
                Constant.AUTOMATION_DELETE -> {
                    automationToDelete = data
                    requireActivity().findViewById<View>(R.id.layoutProgress).pbText.text = getString(R.string.updating_data)
                    requireActivity().findViewById<View>(R.id.layoutProgress).visibility = View.VISIBLE
                    automationNameOld = data.name.toString()
                    if (abailableSenseHome.size > 0) {
                        type = "delete"
                        localAutomationDelete = JsonHelper().localDataDelete(automationNameOld!!, "delete", "Automation", abailableSenseHome[0].sense_uiud)
                        sendEspHandler(localAutomationDelete!!, abailableSenseHome[0].sense_ip, abailableSenseHome[0].aura_sence_name!!, "")
                    } else {
                        deleteAutomation(data as AutomationScene, Constant.ONLINE_MODE)
                    }
                }
                Constant.AUTOMATION_EDIT -> {
                    val intent = Intent(activity, CreateAutomationActivity::class.java)
                    intent.putExtra("automationNameType", "edit")
                    intent.putExtra("input_name", data.name)
                    intent.putExtra("automationIconUrl", data.icon)
                    startActivity(intent)
                }
                Constant.AUTOMATION_ENABLE -> {
                    if (userType) {
                        requireActivity().findViewById<View>(R.id.layoutProgress).pbText.text = getString(R.string.txt_please_wait)
                        requireActivity().findViewById<View>(R.id.layoutProgress).visibility = View.VISIBLE
                        if (data.type == "motion") {
                            createLocalData(data)
                            enableDisableMotion(data)  
                        } else if (data.type == "geo") {
                            turnOff = data.property[0].turnOff
                            if (turnOff != "Never") {
                                turnOffTiming = turnOff!!.toInt()
                            }
                            saveDataToLocal(data, Constant.ONLINE_MODE)

                        } else {
                            turnOff = data.property[0].turnOff
                            if (turnOff != "Never") {
                                turnOffTiming = turnOff!!.toInt()
                            }
                            if (abailableSenseHome.size > 0 && abailableSenseHome[0].home == Constant.HOME) {
                                createLocalData(data)
                                type = "localData"
                                automationToDelete = data
                                val lRoutine: HashMap<String, Boolean> = Gson().fromJson(automationToDelete!!.routine, object : TypeToken<HashMap<String, Boolean>>() {}.type)
                                val routines: ArrayList<Int> = arrayListOf()
                                routines.add(if (lRoutine["Sunday"]!!) 1 else 0)
                                routines.add(if (lRoutine["Monday"]!!) 1 else 0)
                                routines.add(if (lRoutine["Tuesday"]!!) 1 else 0)
                                routines.add(if (lRoutine["Wednesday"]!!) 1 else 0)
                                routines.add(if (lRoutine["Thursday"]!!) 1 else 0)
                                routines.add(if (lRoutine["Friday"]!!) 1 else 0)
                                routines.add(if (lRoutine["Saturday"]!!) 1 else 0)
                                localData = JsonHelper().sendAutomationData(data, data.name!!, "time", device_name, uiud_list, data.property[0].turnOff!!, sceneNameArray.size, sceneNameArray, "edit", mdlArray, routines, data.remote, abailableSenseHome)
                                sendEspHandler(localData!!,abailableSenseHome[0].sense_ip, abailableSenseHome[0].aura_sence_name!!, "")
                            } else {
                                saveDataToLocal(data, Constant.ONLINE_MODE)
                            }
                        }
                    } else {
                        SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_edit)).show()
                    }
                }

                Constant.ERROR_SHOW -> {
                    SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_edit)).show()
                }
            }
        }
    }

    private fun saveDataToLocal(data: AutomationScene, deviceState: String) {
        if (data.type == "time" || data.type == "geo") {
            if (localSqlDatabaseSchedule.updatechedule(mDBAutomation!!, data.name!!, data.icon, Gson().toJson(data.room), Gson().toJson(data.load), Gson().toJson(data.property), data.time!!, data.type!!, data.endTime!!, data.routine!!, Constant.HOME!!, data.name!!, Gson().toJson(data.remote))) {
                thread {
                    scheduleDynamoDb.updateUserSchedule(Constant.IDENTITY_ID!!, Constant.HOME!!, data, "edit", data.name!!, data.property[0].turnOff!!, if (data.property[0].turnOff != "Never") data.property[0].turnOff!!.toInt() else 0, data.routine!!, data.remote, deviceState, if (abailableSenseHome.size > 0) abailableSenseHome[0].senseMacId!! else "", if (abailableSenseHome.size > 0) abailableSenseHome[0].scense_thing!! else "")
                    runOnUiThread {
                        layoutProgress.visibility = View.GONE
                        automationAdapter.notifyDataSetChanged()
                        SingleBtnDialog.with(context).hideHeading().setMessage(getString(R.string.update_successful)).show()
                    }
                }

            } else {
                requireActivity().findViewById<View>(R.id.layoutProgress).visibility = View.GONE
                SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.error_auto)).show()
            }
        } else {
            if (localSqlDatabaseSchedule.updatechedule(mDBAutomation!!, data.name!!, data.icon, Gson().toJson(data.room), Gson().toJson(data.load), Gson().toJson(data.property), data.time!!, data.type!!, data.endTime!!, data.routine!!, Constant.HOME!!, data.name!!, Gson().toJson(data.remote))) {
                thread {
                    val senseData = Common.getSenseData(data.routine!!)
                    if (data.property[0].triggerWhen.isNullOrEmpty()) {
                        data.property[0].triggerWhen = "Any time"
                    }
                    scheduleDynamoDb.updateUserSense(Constant.IDENTITY_ID!!, Constant.HOME!!, data, "edit",
                            data.name!!, senseData, deviceThingsList, turnOffTiming,
                            data.property[0].triggerWhen!!, data.property[0].turnOff!!, deviceState, if (abailableSenseHome.size > 0) abailableSenseHome[0].senseMacId!! else "", if (abailableSenseHome.size > 0) abailableSenseHome[0].scense_thing!! else "", data.remote)

                    runOnUiThread {
                        requireActivity().findViewById<View>(R.id.layoutProgress).visibility = View.GONE
                        automationAdapter.notifyDataSetChanged()
                        SingleBtnDialog.with(context).hideHeading().setMessage(getString(R.string.update_successful)).show()
                    }
                }
            }
        }

    }

    private fun createLocalData(automation: AutomationScene) {
        deviceThingsList.clear()
        mdlArray.clear()
        device_name.clear()
        uiud_list.clear()
        if (turnOff != "Never") {
            if (turnOff != null) {
                turnOffTiming = turnOff!!.toInt()
            } else {
                turnOff = "Never"
            }
        }

        for (auto in automation.load) {
            for (device in auto.deviceList) {
                for (l in ipListDevice) {
                    var mdlFlag = false
                    if (l.name == device.name || l.name == device.deviceName) {
                        for (mdl in mdlArray) {
                            if (mdl == l.module) {
                                mdlFlag = true
                                break
                            }
                        }
                        if (!mdlFlag) {
                            mdlArray.add(l.module)
                            device_name.add(l.fullDeviceName.toString())
                            // automatonsScene.add(device.roomName)
                        }
                        break
                    }
                }
                if (device.deviceName != "Automation") {
                    val uiud = localSqlDatabase.getUiud(mDb!!, device.deviceName)
                    val deviceThings = localSqlDatabase.getThingForDevice(mDb!!, device.deviceName)
                    var flag = false
                    for (dev in deviceThingsList) {
                        if (dev.deviceName == device.deviceName) {
                            flag = true
                            break
                        }
                    }
                    if (!flag) {
                        val deviceThing = DeviceThing()
                        if (device.deviceName == "Scene") {
                            sceneNameArray.add(device.name)
                        } else {
                            deviceThing.deviceThing = deviceThings
                            deviceThing.deviceName = device.deviceName
                            deviceThingsList.add(deviceThing)
                            uiud_list.add(uiud!!)
                        }
                    }
                }

            }
        }
    }

    private fun deleteAutomation(automationScene: AutomationScene, senseMode: String) {
        localSqlDatabaseSchedule.deleteAutomationScene(mDBAutomation!!, automationNameOld!!, Constant.HOME!!)
        automationAdapter.notifyDataSetChanged()
        deleteAutomationScene(automationNameOld!!)
        if (automationScene.type != "motion") {
            thread {
                scheduleDynamoDb.deleteUserSchedule(Constant.IDENTITY_ID!!, Constant.HOME!!, automationNameOld!!, if (remoteDataList.size > 0) remoteDataList[0].senseMacId!! else "", if (remoteDataList.size > 0) remoteDataList[0].scense_thing!! else "", senseMode)
                runOnUiThread {
                    requireActivity().findViewById<View>(R.id.layoutProgress).visibility = View.GONE
                    SingleBtnDialog.with(activity).setHeading(getString(R.string.success)).setMessage(getString(R.string.automation_deleted)).show()
                }
            }

        } else {
            thread {
                scheduleDynamoDb.deleteUserSenseMotion(Constant.IDENTITY_ID!!, Constant.HOME!!, automationNameOld!!, if (remoteDataList.size > 0) remoteDataList[0].senseMacId!! else "", if (remoteDataList.size > 0) remoteDataList[0].scense_thing!! else "", senseMode)
                runOnUiThread {
                    requireActivity().findViewById<View>(R.id.layoutProgress).visibility = View.GONE
                    SingleBtnDialog.with(activity).setHeading(getString(R.string.success)).setMessage(getString(R.string.automation_deleted)).show()
                    automationAdapter.notifyDataSetChanged()
                }
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

}

