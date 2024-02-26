package com.wozart.aura.ui.setactions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.R
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.AutomationModel
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.ScheduleTable
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.DeviceThing
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.ui.adapter.AutomationSceneActionAdapter
import com.wozart.aura.ui.auraSense.FavoriteButtonAdapter
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.base.basesetactions.BaseSetActionsFragment
import com.wozart.aura.ui.base.basesetactions.SetActionsRoomAdapter
import com.wozart.aura.ui.createautomation.*
import com.wozart.aura.ui.createscene.SetActionsForSceneFragment
import com.wozart.aura.ui.dashboard.*
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.selectdevices.SelectDevicesFragment
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.fragment_automation_select_device.*
import kotlinx.android.synthetic.main.fragment_automation_select_device.rvFavButton
import kotlinx.android.synthetic.main.fragment_automation_select_device.tvFavoriteButton
import kotlinx.android.synthetic.main.fragment_select_devices.tvSelectLoadIcon
import kotlinx.android.synthetic.main.layout_header.*
import org.jetbrains.anko.longToast
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


/**
 * Created by Saif on 11/20/2018.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */

class SetActionsFragment : BaseSetActionsFragment(), RecyclerItemClicked, EspHandler.OnEspHandlerMessage {

    private var localSqlSchedule = ScheduleTable()
    private val localSqlDatabase = DeviceTable()
    private var mDbSchedule: SQLiteDatabase? = null
    private var mDb: SQLiteDatabase? = null
    var roomsList: MutableList<RoomModel> = ArrayList()
    private var adapter = SetActionsRoomAdapter()
    lateinit var lati: String
    lateinit var longi: String
    var geoDetails: MutableList<GeoModal> = ArrayList()
    var geoData = GeoModal()
    var automationEnable: Boolean = false
    var geoTriggeringType: String? = null
    private var roomsSelected: MutableList<RoomModel> = ArrayList()
    var scheduleGeoTypeCheck: String? = null
    private var trigger_when: String? = null
    private var trigger_specific_start_time: String? = null
    private var trigger_specific_end_time: String? = null
    private var roomListData: MutableList<RoomModel> = ArrayList()
    var turnOffArray = arrayOf("Never", "After 1 minute", "After 2 minutes", "After 3 minutes", "After 4 minutes", "After 5 minutes", "After 10 minutes", "After 15 minutes", "After 20 minutes", "After 30 minutes", "After 45 minutes", "After 60 minutes")
    var turnOffTiming: Int = 0
    var turnOfNever = ""
    var timeSelected: Int? = null
    var senseDevicename: String? = null
    var senseUiud: String? = null
    var senseMacId: String? = null
    var senseDataList: MutableList<AuraSenseConfigure> = ArrayList()
    lateinit var sceneAdapter: AutomationSceneActionAdapter
    private var sceneToActive: MutableList<RoomModel> = ArrayList()
    var sceneSize: Int = 0
    var sceneNameArray: ArrayList<String> = ArrayList()
    var IpListDevices: MutableList<IpModel> = ArrayList()
    lateinit var btnAdapter: FavoriteButtonAdapter
    var espHandler: EspHandler? = null

    companion object {
        private var automationScheduleType: String? = null
        private var automationNameOld: String? = null
        private var automationName: String? = null
        private var selectedIcon: Int? = 0
        private var scheduleType: String? = null
        var senseData: String? = null
        var whenSenseTrigger: String = "Any time"
        private var Selectedrooms: MutableList<RoomModel> = ArrayList()
        var NewGoeRadius: Float? = 0f
        var roomList = arrayListOf<RoomModel>()
        var favRemoteButtonList: MutableList<RemoteIconModel> = ArrayList()
        var schedule = AutomationModel("title", 0, roomList, false, "", "", "", "", "")
        fun newInstance(rooms: MutableList<RoomModel>, scheduleDetails: AutomationModel, automationSceneNameOld: String, automationSceneType: String, motionData: String?, senseWhenTrigger: String?, remoteSelectedFavButton: MutableList<RemoteIconModel>): SetActionsFragment {
            BaseSetActionsFragment.rooms = rooms
            Selectedrooms = rooms
            senseData = motionData
            favRemoteButtonList.clear()
            whenSenseTrigger = senseWhenTrigger!!
            favRemoteButtonList = remoteSelectedFavButton
            BaseSetActionsFragment.data = scheduleDetails
            automationNameOld = automationSceneNameOld
            automationScheduleType = automationSceneType
            return SetActionsFragment()
        }

        fun newInstance(remoteSelectedFavButton: MutableList<RemoteIconModel>) : SetActionsFragment {
            favRemoteButtonList = remoteSelectedFavButton
            return SetActionsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_automation_select_device, container, false)
        val listRooms = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.listRooms)
        val dbSchedule = ScheduleDbHelper(requireContext())
        mDbSchedule = dbSchedule.writableDatabase
        val dbHelper = DeviceDbHelper(requireContext())
        mDb = dbHelper.writableDatabase

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        lati = pref.getString(Constant.GEOFENCE_POSITION_LATITUDE, "null")!!
        longi = pref.getString(Constant.GEOFENCE_POSITION_LONGITUDE, "null")!!
        roomsSelected = SelectDevicesFragment.rooms

        if (context is SetAutomationActivity) {
            scheduleGeoTypeCheck = (context as SetAutomationActivity).getScheduleType().toString()
        }

        if (scheduleGeoTypeCheck == "geo") {
            if (context is SetAutomationActivity) {
                automationNameOld = (context as SetAutomationActivity).getAutomationOldName()
                automationScheduleType = (context as SetAutomationActivity).getAutomationType()
                automationName = (context as SetAutomationActivity).getAutomationName()
                selectedIcon = (context as SetAutomationActivity).getIcon()
                scheduleType = (context as SetAutomationActivity).getScheduleType()
                NewGoeRadius = (context as SetAutomationActivity).getAddedGeoRadius()
                automationEnable = (context as SetAutomationActivity).getAutomationEnable()
                geoTriggeringType = (context as SetAutomationActivity).getTriggeringType()
                trigger_when = (context as SetAutomationActivity).getTriggerWhen()
                trigger_specific_start_time = (context as SetAutomationActivity).getGeoSpecificStartTime()
                trigger_specific_end_time = (context as SetAutomationActivity).getGeoSpecificEndTime()
                lati = (context as SetAutomationActivity).getLatitude().toString()
                longi = (context as SetAutomationActivity).getLongitude().toString()
                trigger_specific_end_time = (context as SetAutomationActivity).getGeoSpecificEndTime()
                schedule = AutomationModel(automationName, selectedIcon!!, roomList, automationEnable, "", scheduleType, "", "", geoTriggeringType!!)
            }
        }
        if (data.type == "motion") {
            converJsonMotionData()
        }
        if (scheduleType == "geo") {
            for (rooms in roomsSelected) {
                if (rooms.deviceList.size > 0) {
                    roomListData.add(rooms)
                }
            }
            listRooms.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            listRooms.adapter = adapter
            adapter.init(roomListData)
        } else {
            for (rooms in Selectedrooms) {
                if (rooms.deviceList.size > 0) {
                    roomListData.add(rooms)
                }
            }
            listRooms.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            listRooms.adapter = adapter
            adapter.init(roomListData)
        }

        return view
    }

    private fun converJsonMotionData() {
        senseDataList.clear()
        val data = JSONArray(senseData)
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
            sense.senseDeviceName = dataList.getString("senseDeviceName")
            senseDevicename = sense.senseDeviceName
            sense.senseUiud = dataList.getString("senseUiud")
            senseUiud = sense.senseUiud
            sense.senseMacId = dataList.getString("senseMacId")
            senseMacId = sense.senseMacId
            sense.type = dataList.getString("type")
            senseDataList.add(sense)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    var automatons: MutableList<RoomModel> = ArrayList()
    private val scheduleDynamoDb = RulesTableHandler()
    private val deviceThingsList: ArrayList<DeviceThing> = ArrayList()
    private val uiud_list: ArrayList<String> = ArrayList()
    val device_name: ArrayList<String> = arrayListOf()
    private val mdlArray: ArrayList<Int> = arrayListOf()
    private val automatonsScene: ArrayList<String> = ArrayList()
    val auto = AutomationScene()


    override fun onFinish() {
        progress_layout.visibility = View.VISIBLE
        if (scheduleGeoTypeCheck == "geo") {
            automatons = roomsSelected
        } else {
            automatons = Selectedrooms
        }
        for (automation in automatons) {
            for (device in automation.deviceList) {
                for (l in IpListDevices) {
                    var mdlFlag = false
                    if (l.name == device.name || l.name == device.deviceName) {
                        for (mdl in mdlArray) {
                            if (mdl == l.module) {
                                mdlFlag = true
                                break
                            }
                        }
                        if (!mdlFlag) {
                            device_name.add(l.fullDeviceName!!)
                            mdlArray.add(l.module)
                            automatonsScene.add(device.roomName)
                        }
                        break
                    }
                }
                var flag = false
                for (dev in deviceThingsList) {
                    if (dev.deviceName == device.deviceName) {
                        flag = true
                        break
                    }
                }
                if (!flag) {
                    val deviceThing = DeviceThing()
                    if (device.deviceName != "Scene") {
                        deviceThing.deviceName = device.deviceName
                        val uiud = localSqlDatabase.getUiud(mDb!!, device.deviceName)
                        deviceThing.deviceThing = localSqlDatabase.getThingForDevice(mDb!!, device.deviceName)
                        deviceThingsList.add(deviceThing)
                        uiud_list.add(uiud!!)
                    }
                }
                if (device.checkType == "Curtain") {
                    if (device.isTurnOn) {
                        if (device.curtainState != "Open") {
                            if (device.curtainState0 == 0) {
                                device.curtainState0 = 1
                                device.curtainState1 = 0
                            } else {
                                device.curtainState0 = 0
                                device.curtainState1 = 1
                            }
                        }
                    } else {
                        if (device.curtainState != "Close") {
                            if (device.curtainState0 == 0) {
                                device.curtainState0 = 1
                                device.curtainState1 = 0
                            } else {
                                device.curtainState0 = 0
                                device.curtainState1 = 1
                            }
                        }
                    }
                }

            }
            if (sceneToActive.size > 0) {
                updateDeviceList(automation)
            }
            if (sceneToActive.size == 0 && SelectDevicesFragment.sceneSelectedList.size > 0) {
                for (s in SelectDevicesFragment.sceneSelectedList) {
                    val model = RoomModel()
                    model.name = s.rooms[0].name
                    model.deviceList.add(Device(data.iconUrl, s.isOn, true, 100, s.title.toString(), model.name!!, "Scene", 0, false))
                    sceneToActive.add(model)
                }
                updateDeviceList(automation)
            }
        }

        if (scheduleGeoTypeCheck == "geo") {
            geoData.newGeoLat = lati.toDouble()
            geoData.newGeolong = longi.toDouble()
            geoData.newGeoRadius = NewGoeRadius
            geoData.AutomationEnable = automationEnable
            geoData.triggerType = geoTriggeringType
            geoData.triggerWhen = trigger_when
            if (turnOffTiming != 0) {
                geoData.turnOff = turnOffTiming.toString()
            } else {
                geoData.turnOff = turnOfNever
            }
            geoData.triggerSpecificEndTime = trigger_specific_end_time
            geoData.triggerSpecificStartTime = trigger_specific_start_time
            geoDetails.add(geoData)
            val scheduleName = automationName
            val scheduleType = scheduleType
            val scheduleIcon = selectedIcon
            val scheduleGMTTime = ""
            val scheduleLocalTime = ""
            val scheduleRoutine = ""
            val automationScheduleNameOld = automationNameOld
            val automationScheduleType = automationScheduleType
            val scheduleGeoDetails = geoDetails
            val gson = Gson()
            if (localSqlSchedule.insertSchedule(mDbSchedule!!, scheduleName.toString(), scheduleIcon!!, gson.toJson(automatonsScene), gson.toJson(automatons), gson.toJson(scheduleGeoDetails), scheduleGMTTime, scheduleType.toString(), scheduleLocalTime, scheduleRoutine, Constant.HOME!!, automationScheduleNameOld?:scheduleName.toString(), automationScheduleType?:"create", gson.toJson(favRemoteButtonList))) {
                progress_layout.visibility = View.GONE
                val intent = Intent(activity, DashboardActivity::class.java)
                intent.putExtra("TAB_SET", Constant.AUTOMATION_TAB)
                startActivity(intent)
                activity?.finish()
            } else {
                progress_layout.visibility = View.GONE
                activity?.longToast("Automation with same name present.")
            }

        } else {
            if (turnOffTiming != 0) {
                geoData.turnOff = turnOffTiming.toString()
            } else {
                geoData.turnOff = turnOfNever
            }
            geoData.AutomationEnable = data.Automationenable
            geoData.triggerWhen = whenSenseTrigger
            geoDetails.add(geoData)
            auto.name = data.title.toString()
            auto.icon = data.iconUrl
            auto.room = automatons as ArrayList<RoomModel>
            auto.load = automatons
            auto.property = geoDetails
            auto.type = scheduleType
            auto.time = data.time.toString()
            scheduleType = data.type
            auto.endTime = data.endTime
            if (abailableSenseHome.size > 0 && data.type != "motion" && abailableSenseHome[0].home == Constant.HOME) {
                val lRoutine: HashMap<String, Boolean> = Gson().fromJson(data.routine, object : TypeToken<HashMap<String, Boolean>>() {}.type)
                val routines: ArrayList<Int> = arrayListOf()
                routines.add(if (lRoutine["Sunday"]!!) 1 else 0)
                routines.add(if (lRoutine["Monday"]!!) 1 else 0)
                routines.add(if (lRoutine["Tuesday"]!!) 1 else 0)
                routines.add(if (lRoutine["Wednesday"]!!) 1 else 0)
                routines.add(if (lRoutine["Thursday"]!!) 1 else 0)
                routines.add(if (lRoutine["Friday"]!!) 1 else 0)
                routines.add(if (lRoutine["Saturday"]!!) 1 else 0)
                localData = JsonHelper().sendAutomationData(auto, automationNameOld!!, data.type!!, device_name, uiud_list, geoData.turnOff.toString(), sceneSize, sceneNameArray, automationScheduleType!!, mdlArray, routines, favRemoteButtonList, abailableSenseHome)
                sendEspHandler(localData!!, abailableSenseHome[0].sense_ip, abailableSenseHome[0].aura_sence_name!!, "")
            } else if (abailableSenseHome.size > 0 && abailableSenseHome[0].home == Constant.HOME) {
                auto.routine = ""
                if (senseDataList.size > 0) {
                    localData = JsonHelper().sendAutomationSenseData(auto, automationNameOld!!, data.type!!, device_name, uiud_list, geoData.turnOff.toString(), senseDataList, sceneSize, sceneNameArray, automationScheduleType!!, mdlArray, favRemoteButtonList, abailableSenseHome)
                    sendEspHandler(localData!!, abailableSenseHome[0].sense_ip, abailableSenseHome[0].aura_sence_name!!, "")
                }
            } else {
                saveAllData(Constant.ONLINE_MODE)
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

    fun initialize() {
        IpListDevices = IP.getIpDevices()
        btnAdapter = FavoriteButtonAdapter(requireActivity(), this)
        sceneAdapter = AutomationSceneActionAdapter(requireContext(), this)
        tvSelectLoadIcon.visibility = View.GONE
        tvTitle.setTextColor(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP);
        tvNext.setTextColor(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n))
        val turnOffSpinner = ArrayAdapter(requireContext(), R.layout.spinner_room_selection, turnOffArray)
        turnOffSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTurnOff.adapter = turnOffSpinner
        if (automationScheduleType == "edit") {
            if (context is CreateAutomationActivity) {
                val timeOff = (context as CreateAutomationActivity).getTurnOffTime()
                val turnOffTime = Common.getTurnoffData(timeOff!!)
                spinnerTurnOff.setSelection(turnOffArray.indexOf(turnOffTime))
                updateTiming(turnOffTime)
            }
        }
        spinnerTurnOff.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val time = spinnerTurnOff.selectedItem.toString()
                updateTiming(time)
            }

        }

        if (SelectDevicesFragment.sceneSelectedList.size > 0) {
            sceneSize = SelectDevicesFragment.sceneSelectedList.size
            tvSelectScene.visibility = View.VISIBLE
            listScenesdata.adapter = sceneAdapter
            listScenesdata.layoutManager = GridAutoFitLayoutManager(context, resources.getDimensionPixelSize(R.dimen.device_item_size))
            listScenesdata.setHasFixedSize(true)
            for (s in SelectDevicesFragment.sceneSelectedList) {
                sceneNameArray.add(s.title.toString())
                s.isOn = true
            }
            sceneAdapter.dataSet(SelectDevicesFragment.sceneSelectedList)
            val spacing = resources.getDimensionPixelSize(R.dimen.uniform_half_spacing)
            listScenesdata.addItemDecoration(GridListSpacingItemDecoration(spacing))
        }

        if (SetActionsForSceneFragment.favRemoteButtonList.size > 0) {
            tvFavoriteButton.visibility = View.VISIBLE
            rvFavButton.visibility = View.VISIBLE
            btnAdapter.setData(SetActionsForSceneFragment.favRemoteButtonList)
            rvFavButton.adapter = btnAdapter
        }

    }

    fun updateTiming(time: String) {
        if (time == "Never") {
            turnOfNever = time
            turnOffTiming = 0
        } else if (time.contains("minutes")) {
            val data = time.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            timeSelected = data[1].toInt() * 60
            turnOffTiming = timeSelected!!
        } else if (time.contains("minute")) {
            val data = time.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            timeSelected = data[1].toInt() * 60
            turnOffTiming = timeSelected!!
        }
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is Scenes) {
            var flag = false
            for (s in sceneToActive) {
                for (d in s.deviceList) {
                    if (d.name == data.title) {
                        d.isTurnOn = data.isOn
                        flag = true
                        break
                    }
                }
            }
            if (!flag) {
                val model = RoomModel()
                model.name = data.rooms[0].name
                model.deviceList.add(Device(data.iconUrl, data.isOn, true, 100, data.title.toString(), model.name!!, "Scene", 0, false))
                sceneToActive.add(model)
            }
        }
    }

    private fun updateDeviceList(automation: RoomModel) {
        for (edAutomation in sceneToActive) {
            if (automation.name == edAutomation.name) {
                for (dev in edAutomation.deviceList) {
                    if (automation.deviceList.size > 0) {
                        for (d in automation.deviceList) {
                            if (d.name != dev.name) {
                                automation.deviceList.add(dev)
                                break
                            }
                        }
                    } else {
                        automation.deviceList.add(dev)
                    }

                }
            }
        }
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        AppExecutors().mainThread().execute {
            updateData(decryptedData, name)
        }
    }

    private fun updateData(decryptedData: String, name: String) {
        if (decryptedData.contains("ERROR")) {
            saveAllData(Constant.OFFLINE_MODE)
        } else {
            val updatedDevice: AuraSwitch = JsonHelper().deserializeTcp(decryptedData)
            when (updatedDevice.type) {
                12 -> {
                    if (updatedDevice.error == 0) {
                        saveAllData(Constant.ONLINE_MODE)
                    } else {
                        val message = "Device-${name} not responding."
                        SingleBtnDialog.with(requireActivity()).setHeading(requireActivity().getString(R.string.alert)).setMessage(message).show()
                        val intent = Intent(activity, DashboardActivity::class.java)
                        intent.putExtra("TAB_SET", Constant.AUTOMATION_TAB)
                        startActivity(intent)
                        activity?.finish()
                    }
                }
            }
        }
    }

    private fun saveAllData(state: String) {
        val gson = Gson()
        if (scheduleType == "motion") {
            if (localSqlSchedule.insertSchedule(mDbSchedule!!, data.title.toString(), data.iconUrl, gson.toJson(automatonsScene), gson.toJson(automatons), gson.toJson(geoDetails), data.time.toString(), data.type.toString(), data.endTime.toString(), senseData!!, Constant.HOME!!, automationNameOld!!, automationScheduleType!!, gson.toJson(favRemoteButtonList))) {
                thread {
                    val home = Constant.HOME.toString()
                    val userId = Constant.IDENTITY_ID
                    scheduleDynamoDb.updateUserSense(userId!!, home, auto, automationScheduleType!!, automationNameOld!!, senseDataList, deviceThingsList, turnOffTiming, whenSenseTrigger, turnOfNever, state, if (abailableSenseHome.size > 0) abailableSenseHome[0].senseMacId!! else "", if (abailableSenseHome.size > 0) abailableSenseHome[0].scense_thing!! else "", favRemoteButtonList)
                    requireActivity().runOnUiThread {
                        progress_layout.visibility = View.GONE
                        val intent = Intent(activity, DashboardActivity::class.java)
                        intent.putExtra("TAB_SET", Constant.AUTOMATION_TAB)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }

            } else {
                progress_layout.visibility = View.GONE
                SingleBtnDialog.with(requireContext()).hideHeading().setMessage(getString(R.string.trigger_exist_error)).show()
            }
        } else {
            if (localSqlSchedule.insertSchedule(mDbSchedule!!, data.title?.trim().toString(), data.iconUrl, gson.toJson(automatonsScene), gson.toJson(automatons), gson.toJson(geoDetails), data.time.toString(), scheduleType.toString(), data.endTime.toString(), data.routine.toString(), Constant.HOME!!, automationNameOld!!, automationScheduleType!!, gson.toJson(favRemoteButtonList))) {
                thread {
                    if (state.contains(Constant.OFFLINE_MODE)) scheduleDynamoDb.storeConfigurationData(Constant.IDENTITY_ID!!, if (abailableSenseHome.size > 0) abailableSenseHome[0].senseMacId!! else "", auto.name!!, automationNameOld!!, if (abailableSenseHome.size > 0) all_sense_device[0].scense_thing!! else "", automationScheduleType!!, type = "automation")
                    requireActivity().runOnUiThread {
                        progress_layout.visibility = View.GONE
                        val intent = Intent(activity, DashboardActivity::class.java)
                        intent.putExtra("TAB_SET", Constant.AUTOMATION_TAB)
                        startActivity(intent)
                        activity?.finish()
                    }
                }

            } else {
                progress_layout.visibility = View.GONE
                SingleBtnDialog.with(requireContext()).hideHeading().setMessage(getString(R.string.automation_exist_error)).show()
            }
        }
    }

}
