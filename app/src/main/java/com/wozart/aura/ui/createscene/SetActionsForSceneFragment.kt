package com.wozart.aura.ui.createscene

import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.data.sqlLite.ScheduleTable
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.ui.adapter.AdvanceAutomationSelection
import com.wozart.aura.ui.auraSense.FavoriteButtonAdapter
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.base.basesetactions.BaseSetActionsFragment
import com.wozart.aura.ui.base.basesetactions.SetActionsRoomAdapter
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.fragment_select_devices.*
import kotlinx.android.synthetic.main.layout_header.*
import org.jetbrains.anko.startActivity
import kotlin.concurrent.thread


class SetActionsForSceneFragment : BaseSetActionsFragment(), RecyclerItemClicked, EspHandler.OnEspHandlerMessage {

    private var mDbScene: SQLiteDatabase? = null
    private val localSqlDatabase = DeviceTable()
    private var mDbDevice: SQLiteDatabase? = null
    var automationScene = ArrayList<AutomationScene>()
    private val localSqlDatabaseSchedule = ScheduleTable()
    private var mDBAutomation: SQLiteDatabase? = null
    private val roomList_: MutableList<String> = ArrayList()
    var roomsList: MutableList<RoomModel> = ArrayList()
    private val scheduleDynamoDb = RulesTableHandler()
    private var adapter = SetActionsRoomAdapter()
    private var roomList: MutableList<RoomModel> = ArrayList()
    lateinit var automationSelectionAdapter: AdvanceAutomationSelection
    var automationToEnableDisable: MutableList<RoomModel> = ArrayList()
    var jsonHelper = JsonHelper()
    var IpListDevices: MutableList<IpModel> = ArrayList()
    lateinit var btnAdapter: FavoriteButtonAdapter
    var espHandler: EspHandler? = null


    companion object {
        private var selectedRoom: MutableList<RoomModel> = ArrayList()

        var favRemoteButtonList: MutableList<RemoteIconModel> = ArrayList()
        fun newInstance(room: MutableList<RoomModel>, sceneName: String, sceneNameOld: String, sceneIcon: Int, sceneNameType: String, disableEnableAutomation: MutableList<AutomationScene>, remoteSelectedFavButton: MutableList<RemoteIconModel>): SetActionsForSceneFragment {
            selectedRoom = room
            this.favRemoteButtonList.clear()
            BaseSetActionsFragment.listAutomation = disableEnableAutomation
            this.favRemoteButtonList = remoteSelectedFavButton
            BaseSetActionsFragment.rooms = room
            BaseSetActionsFragment.sceneName = sceneName
            BaseSetActionsFragment.sceneIcon = sceneIcon
            BaseSetActionsFragment.sceneNameOld = sceneNameOld
            BaseSetActionsFragment.sceneNameType = sceneNameType
            return SetActionsForSceneFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_select_devices, container, false)

        val dbAutomation = ScheduleDbHelper(requireContext())
        mDBAutomation = dbAutomation.writableDatabase

        val dbDevice = DeviceDbHelper(requireContext())
        mDbDevice = dbDevice.writableDatabase

        val listRooms = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.listRooms)
        val textScene = view.findViewById<TextView>(R.id.text_enter_scene)
        textScene.visibility = View.GONE

        val textelectLoad = view.findViewById<TextView>(R.id.tvSelectLoadIcon)
        textelectLoad.visibility = View.GONE

        if (sceneNameType == "edit") {
            automationScene = localSqlDatabaseSchedule.getAutomationScene(mDBAutomation!!, Constant.HOME!!)
            var updateAutomationFlag = false
            for (schedule in automationScene) {
                for (s in schedule.load) {
                    for (sDevice in s.deviceList) {
                        if (sDevice.deviceName == "Scene" && sDevice.name == sceneNameOld && sDevice.name != sceneName) {
                            sDevice.name = getSceneName()
                            updateAutomationFlag = true
                            break
                        }
                    }
                    var flag = false
                    for (r in roomList_) {
                        if (r == s.name) {
                            flag = true
                            break
                        }
                    }
                    if (!flag) {
                        roomList_.add(s.name!!)
                    }
                }
            }
        }

        for (room in selectedRoom) {
            if (room.deviceList.size > 0) {
                roomList.add(room)

            }
        }
        listRooms.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        listRooms.adapter = adapter
        adapter.init(roomList)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    val sceneTable = SceneTable()
    val rooms = selectedRoom
    val roomsForScenes: ArrayList<String> = ArrayList()
    val uiud_list: ArrayList<String> = ArrayList()
    val device_name: ArrayList<String> = arrayListOf()
    val roomModelForScenes: MutableList<RoomModel> = ArrayList()
    val mdlArray: ArrayList<Int> = arrayListOf()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onFinish() {
        progress_layout.visibility = View.VISIBLE
        for (room in rooms) {
            if (room.deviceList.size > 0) {
                roomModelForScenes.add(room)
                for (device in room.deviceList) {
                    for (l in IpListDevices) {
                        var mdlFlag = false
                        if (l.name == device.deviceName) {
                            for (mdl in mdlArray) {
                                if (mdl == l.module) {
                                    mdlFlag = true
                                    break
                                }
                            }
                            if (!mdlFlag) {
                                roomsForScenes.add(device.roomName)
                                device_name.add(l.fullDeviceName.toString())
                                mdlArray.add(l.module)
                            }
                            break
                        }
                    }
                    val uiud = localSqlDatabase.getUiud(mDbDevice!!, device.deviceName)
                    var flag = false
                    for (ud in uiud_list) {
                        if (ud == uiud) {
                            flag = true
                            break
                        }
                    }
                    if (!flag) {
                        uiud_list.add(uiud!!)
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
            }
        }
        for (room in rooms) {
            for (edAutomation in automationToEnableDisable) {
                for (dev in edAutomation.deviceList) {
                    for (d in room.deviceList) {
                        if (d.name != dev.name) {
                            room.deviceList.add(dev)
                            break
                        }
                    }
                }
            }
        }
        val name = getSceneName()
        val sceneIcon = getSceneIcon()
        val sceneNameOld = getSceneNameOld()
        val sceneNameType = getSceneNameType()
        if (abailableSenseHome.size > 0 && abailableSenseHome[0].home == Constant.HOME) {
            localData = jsonHelper.sendSceneData(name, roomModelForScenes, sceneNameOld, sceneNameType, uiud_list, device_name, mdlArray, favRemoteButtonList, abailableSenseHome)
            sendEspHandler(localData!!,abailableSenseHome[0].sense_ip, abailableSenseHome[0].aura_sence_name!!, "")
        } else {
            saveData(Constant.ONLINE_MODE)
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
        btnAdapter = FavoriteButtonAdapter(requireActivity(), this)
        IpListDevices = IP.getIpDevices()
        val dbScene = SceneDbHelper(requireContext())
        mDbScene = dbScene.writableDatabase
        tvTitle.setTextColor(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        tvNext.setTextColor(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n))
        if (listAutomation.size > 0) {
            listAutomations.visibility = View.VISIBLE
            tvAdvanceSetting.visibility = View.VISIBLE
            automationToEnableDisable.clear()
            for (lAuto in listAutomation) {
                val model = RoomModel()
                model.name = lAuto.load[0].name
                model.deviceList.add(Device(lAuto.icon, lAuto.property[0].AutomationEnable, true, 100, lAuto.name.toString(), model.name!!, "Automation", 0, false))
                automationToEnableDisable.add(model)

            }
        }
        automationSelectionAdapter = AdvanceAutomationSelection(requireContext(), this)
        automationSelectionAdapter.setData(listAutomation, type = 1)
        listAutomations.adapter = automationSelectionAdapter

        if (favRemoteButtonList.size > 0) {
            tvFavoriteButton.visibility = View.VISIBLE
            rvFavButton.visibility = View.VISIBLE
            btnAdapter.setData(favRemoteButtonList)
            rvFavButton.adapter = btnAdapter
        }
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is AutomationScene) {
            var flag = false
            for (auto in automationToEnableDisable) {
                for (d in auto.deviceList) {
                    if (d.name == data.name) {
                        auto.deviceList[0].isTurnOn = data.property[0].AutomationEnable
                        flag = true
                        break
                    }
                }
            }
            if (!flag) {
                val model = RoomModel()
                model.name = data.load[0].name
                model.deviceList.add(Device(data.icon, data.property[0].AutomationEnable, true, 100, data.name.toString(), model.name!!, "Automation", 0, false))
                automationToEnableDisable.add(model)
            }
        }
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
       AppExecutors().mainThread().execute {
            updateData(decryptedData, name)
        }
    }

    private fun updateData(message: String, name: String) {
        if (message.contains("ERROR")) {
            saveData(Constant.OFFLINE_MODE)
        } else {
            val updatedDevice: AuraSwitch = JsonHelper().deserializeTcp(message)
            when (updatedDevice.type) {
                11 -> {
                    if (updatedDevice.error == 0) {
                        saveData(Constant.ONLINE_MODE)
                    } else {
                        val message_error = "Device-${name} not responding."
                        SingleBtnDialog.with(requireActivity()).setHeading(requireActivity().getString(R.string.alert)).setMessage(message_error).show()
                    }
                }
            }
        }
    }

    private fun saveData(stateMode: String) {
        val name = getSceneName()
        val sceneIcon = getSceneIcon()
        val sceneNameOld = getSceneNameOld()
        val sceneNameType = getSceneNameType()
        val gson = Gson()
        if (sceneTable.insertScene(mDbScene!!, name.trim().toString(), gson.toJson(roomModelForScenes), gson.toJson(roomsForScenes), Constant.HOME!!, sceneIcon, sceneNameOld, sceneNameType, gson.toJson(favRemoteButtonList))) {
            thread {
                val home = Constant.HOME
                val userId = Constant.IDENTITY_ID
                scheduleDynamoDb.updateUserScene(userId!!, home!!, name, roomModelForScenes, sceneIcon, sceneNameType, sceneNameOld, favRemoteButtonList, if (abailableSenseHome.size > 0) abailableSenseHome[0].senseMacId!! else "", if (abailableSenseHome.size > 0) abailableSenseHome[0].scense_thing!! else "", stateMode)
                requireActivity().runOnUiThread {
                    progress_layout.visibility = View.GONE
                    activity?.startActivity<DashboardActivity>()
                }
            }
        } else {
            progress_layout.visibility = View.GONE
            SingleBtnDialog.with(requireContext()).hideHeading().setMessage(getString(R.string.scene_exist_error)).show()
        }
    }
}
