package com.wozart.aura.ui.dashboard.rooms

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.warkiz.widget.IndicatorSeekBar
import com.wozart.aura.R
import com.wozart.aura.aura.ui.dashboard.rooms.AddRoomActivity
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.device.SceneHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.model.Room
import com.wozart.aura.data.sqlLite.*
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.AuraSwitchLoad
import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.entity.network.ConnectTask
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.auraSense.RemoteListModel
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.createscene.CreateSceneActivity
import com.wozart.aura.ui.curtain.CurtainSetUpActivity
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.EditLoadActivity
import com.wozart.aura.ui.dashboard.Scenes
import com.wozart.aura.ui.dashboard.listener.OnAdaptiveSelected
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.room.RoomActivity
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.dialog_configure_edit_dimming.iconDevice
import kotlinx.android.synthetic.main.dialog_curtain_control_edit.*
import kotlinx.android.synthetic.main.dialog_curtain_control_edit.btnEdit
import kotlinx.android.synthetic.main.dialogue_tunnable_set.*
import kotlinx.android.synthetic.main.fan_dimming_layout.*
import kotlinx.android.synthetic.main.fragment_rooms.*
import org.jetbrains.anko.longToast


/***
 * Created by saif on 14-03-2018.
 */

class RoomsFragment : Fragment(),
        RoomsAdapter.OnLoadPressedListiner, RoomsAdapter.OnScenePressedListener, ConnectTask.TcpMessageReceiver,
        EspHandler.OnEspHandlerMessage, RecyclerItemClicked, OnAdaptiveSelected {


    private val LOG_TAG = RoomsFragment::class.java.simpleName
    private val localSqlDatabase = DeviceTable()
    private val localSqlUtils = UtilsTable()
    private val localSqlScene = SceneTable()
    private var localSqlRemote = AuraSenseTable()
    private var localSQLButton = AuraButtonTable()
    private var mdbButton: SQLiteDatabase? = null
    private var mdbSense: SQLiteDatabase? = null
    private var mDb: SQLiteDatabase? = null
    private var mDbUtils: SQLiteDatabase? = null
    private var mDbScene: SQLiteDatabase? = null
    private var sceneHandler: SceneHandler = SceneHandler()
    private var nsd: Nsd? = null
    private var jsonHelper: JsonHelper = JsonHelper()
    private lateinit var roomsAdapter: RoomsAdapter
    private var roomDeviceList = ArrayList<Device>()
    var roomNameOld: String? = null
    var usertype = true
    var IpListDevices: MutableList<IpModel> = ArrayList()
    private var IP = IpHandler()
    var noDevicePresentFlag = false
    private var totalSenseAvailableLocal: MutableList<RemoteModel> = ArrayList()
    var buttonDeviceListRoom: MutableList<ButtonModel> = ArrayList()
    var activity: Activity? = null
    val rulesTableHandler = RulesTableHandler()
    var remoteDeleteData: RemoteListModel? = null
    var sceneDelete: Scenes? = null
    private val roomsScenes = ArrayList<Scenes>()
    var buttonDeviceList: MutableList<ButtonModel> = arrayListOf()
    var sceneList: MutableList<Scene> = arrayListOf()
    var remoteCloudControlData: RemoteIconModel? = null
    var espHandler: EspHandler? = null
    var senseAvailableForHome: MutableList<RemoteModel> = arrayListOf()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rooms, container, false)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nsd = Nsd()
        nsd?.getInstance(requireActivity(), "ROOM")
        nsdDiscovery()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))

        val dbHelper = DeviceDbHelper(requireContext())
        mDb = dbHelper.writableDatabase
        val dbUtils = UtilsDbHelper(requireContext())
        mDbUtils = dbUtils.writableDatabase
        val senseDbHelper = AuraSenseDbHelper(requireContext())
        mdbSense = senseDbHelper.writableDatabase
        val dbScene = SceneDbHelper(requireContext())
        mDbScene = dbScene.writableDatabase
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onTcpServerMessageReceived, IntentFilter("intentKey"))
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onAwsMessageReceived, IntentFilter("AwsShadow"))

        val addRoomBtn = view.findViewById<ImageView>(R.id.addRoomBtn)
        addRoomBtn.visibility = View.VISIBLE
        initialize()
        listener()
        checkDevice()
        setDataToAdapter()
    }

    private fun setDataToAdapter() {
        val dummyRoom = Room()
        dummyRoom.roomName = roomNameOld
        val roomsList: ArrayList<Room> = ArrayList()
        roomsList.add(dummyRoom)
        roomsAdapter = RoomsAdapter(requireContext(), this, this, this, roomsList)
        roomsAdapter.sense_device_list = totalSenseAvailableLocal
        roomsRv.adapter = roomsAdapter
        roomsAdapter.setData(sceneList = roomsScenes, deviceList = roomDeviceList, buttonDeviceList = buttonDeviceListRoom, types = usertype)
    }


    private fun checkDevice() {
        val listOfDevices: ArrayList<AuraSwitch> = localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, roomNameOld!!)
        if (listOfDevices.isEmpty()) {
            noDevicePresentFlag = true
        }
        if (!noDevicePresentFlag) {
            for (device in listOfDevices) {
                for (l in IpListDevices) {
                    if (device.name == l.name) {
                        for (i in 0 until device.loads.size) {
                            if (device.loads.size == 1) {
                                l.state[i] = l.auraPlugState[0]
                            } else if (device.loads.size == 2) {
                                l.state[i] = l.twoModuleState[i]
                            }
                            val load = Device(device.loads[i].icon!!, l.state[i], false, l.dim[i], device.loads[i].name!!, localSqlDatabase.getRoomForDevice(mDb!!, device.name), device.name, device.loads[i].index!!, device.loads[i].dimmable!!)
                            if (l.condition[i] == "ready") {
                                if (l.local) {
                                    load.status = "on"
                                } else {
                                    load.status = "cloud"
                                }
                            } else {
                                load.status = "update"
                            }
                            load.power = l.auraPlugPower
                            load.fullDeviceName = l.fullDeviceName.toString()
                            if (device.loads[i].type == "Curtain") {
                                load.checkType = device.loads[i].type.toString()
                                load.curtainState = device.loads[0].curtainState
                                load.curtainState0 = device.loads[0].curtainState0
                                load.curtainState1 = device.loads[0].curtainState1
                            } else if (device.loads[i].type == "rgbDevice") {
                                load.checkType = device.loads[i].type.toString()
                            } else if (device.loads[i].type == "tunableDevice") {
                                load.checkType = device.loads[i].type.toString()
                            }
                            roomDeviceList.add(load)
                        }
                    }

                }
            }
        }

        buttonDeviceList = localSqlUtils.getButtonData(mDbUtils!!, "button_device")

        totalSenseAvailableLocal = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        senseAvailableForHome.addAll(totalSenseAvailableLocal.filter { it.home == Constant.HOME && it.sense_loads.size > 1 })
        sceneList = localSqlScene.getAllScenes(mDbScene!!, Constant.HOME!!)
        if (buttonDeviceList.size > 0) {
            buttonDeviceListRoom.clear()
            for (button in buttonDeviceList) {
                if (button.room == roomNameOld && button.home == Constant.HOME!!) {
                    buttonDeviceListRoom.add(button)
                }
            }
        }

        for (scene in sceneList.filter { it.room.find { it.name == roomNameOld }?.name == roomNameOld }) {
            roomsScenes.add(Scenes(scene.name, scene.icon, scene.room, false))
        }

        for (sense in senseAvailableForHome.filter { it.home == Constant.HOME && it.room == roomNameOld }) {
            IpListDevices.find { it.name == sense.aura_sence_name }?.let {
                try {
                    sendEspHandler(jsonHelper.motionData(sense.sense_uiud!!), it.ip!!, sense.aura_sence_name!!, "SENSE_TYPE_5")
                } catch (e: Exception) {
                    sense.sense_local = false
                    sense.sense_aws = true
                    if (context is RoomActivity) {
                        (context as RoomActivity).pusblishDataToShadow(sense.scense_thing!!, jsonHelper.serializeLEDData())
                    }
                }
            }
        }

        for (l in IpListDevices) {
            try {
                roomDeviceList.find { it.deviceName == l.name }?.let {
                    if (l.fullDeviceName!!.startsWith("Aura Switch")) {
                        sendTcpConnect(l.uiud!!, l.ip!!, l.name!!)
                    } else {
                        sendEspHandler(jsonHelper.initialData(l.uiud!!), l.ip!!, l.name!!, "")
                    }
                }
            } catch (e: Exception) {
                for (i in 0..4) {
                    l.condition[i] = "update"
                    l.failure[i] = 0
                }
                l.local = false
                l.aws = true
                if (context is RoomActivity) {
                    (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeLEDData())
                }
            }
        }
    }

    fun initialize() {
        if (Constant.HOME == null) {
            Constant.HOME = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("HOME", "My Home")
        }
        if (context is RoomActivity) {
            roomNameOld = (context as RoomActivity).getRoomName()
        }
        IpListDevices = IP.getIpDevices()

        val listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        for (x in listRoom) {
            if ((x.type == "room") and (x.sharedHome == Constant.HOME)) {
                if (x.name == roomNameOld) {
                    Utils.setRoomDrawable(requireContext(), requireActivity().findViewById(R.id.rooms_main_activity), x.bgUrl.toInt())
                }
            } else if ((x.type == "room") and (x.sharedHome == "default")) {
                if (x.name == roomNameOld) {
                    Utils.setRoomDrawable(requireContext(), requireActivity().findViewById(R.id.rooms_main_activity), x.bgUrl.toInt())
                }
            } else if (x.name == Constant.HOME) {
                if (x.sharedHome == "guest") {
                    addRoomBtn.visibility = View.INVISIBLE
                    Utils.setDrawable(requireContext(), requireActivity().findViewById(R.id.rooms_main_activity), x.bgUrl.toInt())
                    usertype = false
                }
            }
        }
    }

    fun listener() {
        addRoomBtn.setOnClickListener {
            showPopup(addRoomBtn)
        }

        back_btn.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onLoadPressed(auraDevice: Device, longPressed: Boolean) {
        if (auraDevice.checkType == "Curtain") {
            showDialogCurtain(auraDevice, "control")
        } else {
            controlDevice(auraDevice, longPressed)
        }
    }

    private fun controlDevice(auraDevice: Device, longPressed: Boolean) {
        if (roomNameOld == auraDevice.roomName) {
            var cFlag = false
            if (longPressed) {
                cFlag = true
            }
            for (l in IpListDevices) {
                if (l.name == auraDevice.deviceName) {
                    val data = jsonHelper.serialize(auraDevice, l.uiud!!, l.module, cFlag)
                    l.condition[auraDevice.index] = "update"
                    l.curnLoad[auraDevice.index] = true
                    if (l.ip == null) {
                        if (l.aws) {
                            if (l.thing != null) {
                                if (context is RoomActivity) {
                                    (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeDataToAws(auraDevice, l.uiud!!, l.module, cFlag))
                                }
                            }
                        } else {
                            for (i in 0..3) {
                                l.condition[i] = "fail"
                            }
                        }

                    } else if (l.local) {
                        if (l.fullDeviceName!!.startsWith("Aura Switch")) {
                            ConnectTask(requireContext(), this, data, l.ip!!, auraDevice.deviceName).execute("")
                        } else {
                            sendEspHandler(data, l.ip!!, auraDevice.deviceName, "")
                        }
                    } else if (l.aws) {
                        if (l.thing != null) {
                            if (context is RoomActivity) {
                                val flag = (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeDataToAws(auraDevice, l.uiud!!, l.module, cFlag))
                            }
                        }
                    } else {
                        if ((!l.local) and (!l.aws)) {
                            for (i in 0..3) {
                                l.condition[i] = "fail"
                            }
                        }
                    }
                    IP.registerIpDevice(l)
                    break
                }
            }
        }
    }


    override fun onScenePressed(scene: Scenes) {
        for (s in sceneList) {
            if (s.name == scene.title) {
                if (s.remoteData.size > 0) {
                    for (sense in totalSenseAvailableLocal) {
                        for (r in s.remoteData) {
                            if (r.name == sense.aura_sence_name) {
                                remoteCloudControlData = r
                                val data: String = "{\"type\":14,\"model\":\"${r.remoteModel}\",\"btnName\":\"${r.remoteButtonName}\",\"uiud\":\"${sense.sense_uiud}\",\"cNumber\":\"${r.channelNumber}\"}"
                                sendEspHandler(data, sense.sense_ip, "RemoteData:${sense.sense_uiud}:${sense.scense_thing}", sense.aura_sence_name!!)
                            }
                        }
                    }
                }
                break
            }
        }
        val sceneList = sceneHandler.convertToSceneModel(scene.rooms)
        val isSceneOn = !scene.isOn
        for (sceneFromList in sceneList) {
            for (l in IpListDevices) {
                if (l.name == sceneFromList.device) {
                    val data = jsonHelper.serializeSceneData(sceneFromList, l.uiud!!, l.module, isSceneOn)
                    if (l.ip == null) {
                        l.aws = true
                        if (l.thing != null) {
                            if (context is RoomActivity) {
                                (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeSceneDataForAws(sceneFromList, isSceneOn, l.module))
                            }
                        }
                    } else if (!l.aws) {
                        if (l.fullDeviceName == null) {
                            ConnectTask(requireContext(), this, data, l.ip!!, sceneFromList.device!!).execute()
                        } else {
                            if (l.fullDeviceName!!.startsWith("Aura Switch")) {
                                ConnectTask(requireContext(), this, data, l.ip!!, sceneFromList.device!!).execute("")
                            } else {
                                sendEspHandler(data, l.ip!!, sceneFromList.device!!, "")
                            }
                        }
                    } else {
                        if (l.thing != null) {
                            if (context is RoomActivity) {
                                (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeSceneDataForAws(sceneFromList, isSceneOn, l.module))
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * Data from the TcpClient
     */
    override fun onTcpMessageReceived(message: String) {
        AppExecutors().mainThread().execute {
            IpListDevices = roomsAdapter.updateState(message, IpListDevices)
            roomsAdapter.updateAppearance(IpListDevices)
        }

    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        AppExecutors().mainThread().execute {
            if (name == Constant.REMOTE_DELETE || name == "scene_delete" || name.contains("RemoteData")) {
                updateMessageForDelete(decryptedData, name)
            } else if (name == "SENSE_TYPE_5") {
                roomsAdapter.updateState(decryptedData, IpListDevices)
            } else {
                IpListDevices = roomsAdapter.updateState(decryptedData, IpListDevices)
                roomsAdapter.updateAppearance(IpListDevices)
            }
        }
    }

    private fun updateMessageForDelete(message: String, type: String) {
        if (message.contains("ERROR")) {
            val cNumber = ArrayList<String>(arrayListOf())
            val modeL = ArrayList<String>(arrayListOf())
            val btnNameArr = ArrayList<String>(arrayListOf())
            val deviceErrorName: String
            val data = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            deviceErrorName = data[1]
            if (type != Constant.REMOTE_DELETE) {
                deleteScene(sceneDelete!!, Constant.OFFLINE_MODE)
            } else if (type == Constant.REMOTE_DELETE) {
                deleteRemote(remoteDeleteData!!)
            } else if (deviceErrorName == "RemoteData") {
                cNumber.add(remoteCloudControlData?.channelNumber!!)
                modeL.add(remoteCloudControlData?.remoteModel.toString())
                btnNameArr.add(remoteCloudControlData?.remoteButtonName.toString())
                (context as RoomActivity).pusblishDataToShadow(data[3], jsonHelper.serializeSceneRemoteControl(data[2], modeL, cNumber, btnNameArr))
            } else {
                progress_layout.visibility = View.GONE
                SingleBtnDialog.with(requireActivity()).setHeading(getString(R.string.alert))
                        .setMessage(getString(R.string.error_learning)).show()
            }
        } else {
            val updatedDevice: AuraSwitch = jsonHelper.deserializeTcp(message)
            if (updatedDevice.error == 0) {
                if (type == Constant.REMOTE_DELETE) {
                    deleteRemote(remoteDeleteData!!)
                } else {
                    sceneDelete?.let { deleteScene(it, Constant.ONLINE_MODE) }
                }
            }
        }

    }

    /**
     * Data from the TcpServer
     */
    private val onTcpServerMessageReceived = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = intent.getStringExtra("key")
            for (ip in IpListDevices) {
                if (ip.room == roomNameOld) {
                    IpListDevices = roomsAdapter.updateState(data ?: "", IpListDevices)
                    roomsAdapter.updateAppearance(IpListDevices)
                }
            }
        }
    }

    /**
     * Data from the AwsShadowUpdate
     */
    private val onAwsMessageReceived = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val shadow = intent.getStringExtra("data")
            val segments = shadow!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (shadow.contains("accepted")) {
                val device = localSqlDatabase.getDeviceForThing(mDb!!, segments[1])
                val data = jsonHelper.deserializeAwsData(segments[0])
                var flagMotionExist = false
                for (d in data.state) {
                    if (d.key == "motion" || d.key == "oe" || d.key == "ze") {
                        flagMotionExist = true
                        break
                    }
                }
                if (!flagMotionExist) {
                    for (ip in IpListDevices) {
                        if (ip.room == roomNameOld) {
                            IpListDevices = roomsAdapter.updateStateFromAws(device, data, IpListDevices)
                            roomsAdapter.updateAppearance(IpListDevices)
                            break
                        }
                    }
                } else {
                    roomsAdapter.updateSenseAws(data, device)
                }
            }
        }
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
                    sendEspHandler(jsonHelper.initialData(ipDevice.uiud!!), ipDevice.ip!!, ipDevice.name!!, "")
                }
            }
        }
    }

    private fun sendTcpConnect(uiud: String, ip: String, name: String) {
        ConnectTask(requireContext(), this, jsonHelper.initialData(uiud), ip, name).execute("")

    }


    private fun nsdDiscovery() {
        nsd?.setBroadcastType("ROOM")
        nsd?.initializeNsd()
        nsd?.discoverServices()
    }

    private fun showPopup(v: View) {
        val popup = context?.let { PopupMenu(it, v) }
        val inflater = popup?.menuInflater
        inflater?.inflate(R.menu.room_options_menu, popup.menu)
        popup?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.addScene -> {
                    if (usertype) {
                        if (noDevicePresentFlag) {
                            SingleBtnDialog.with(context).hideHeading().setMessage(getString(R.string.scene_adding_error)).show()
                        } else {
                            val intent = Intent(context, CreateSceneActivity::class.java)
                            intent.putExtra("inputSceneType", "create")
                            intent.putExtra("inputSceneName", "")
                            intent.putExtra("inputSceneIconUrl", 0)
                            startActivity(intent)
                        }

                    } else {
                        activity?.longToast("Guest cannot edit Shared Room")
                    }
                    true
                }

                R.id.editRoom -> {
                    if (usertype) {
                        val intent = Intent(activity, AddRoomActivity::class.java)
                        intent.putExtra("NAME_ROOM", roomNameOld)
                        intent.putExtra("ROOM_EDIT_TYPE", "edit")
                        startActivity(intent)
                    } else {
                        activity?.longToast("Guest cannot edit Shared Room")
                    }
                    true
                }
                else -> false
            }
        }
        popup?.show()
    }


    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is Device) {
            when (viewType) {
                Constant.CURTAIN -> {
                    showDialogCurtain(data, "edit")
                }
                Constant.FAN_DIM -> {
                    openFanDimmingDialog(data)
                }
                Constant.RGB -> {
                    dialog(data)
                }
                Constant.RGB_TUNNABLE -> {
                    tunnableDialog(data)

                }
                Constant.RGB_LOADS -> {
                    controlRGBTunnable(data, false, "RGB")
                }
                Constant.TUNBLE_LOADS -> {
                    controlRGBTunnable(data, false, "Tunable")
                }
            }
        } else if (data is RemoteListModel) {
            remoteDeleteData = data
            progress_layout.visibility = View.VISIBLE
            if (senseAvailableForHome.size > 0) {
                val learingData = "{\"type\":13,\"dType\":\"delete\",\"uiud\":\"${data.senseUiud}\",\"model\":\"${data.modelNumber}\"}"
                sendEspHandler(learingData, data.senseIp, data.auraSenseName!!, Constant.REMOTE_DELETE)
            } else {
                deleteRemote(data)
            }

        } else if (data is Scenes) {
            sceneDelete = data
            progress_layout.visibility = View.VISIBLE
            if (senseAvailableForHome.size > 0) {
                val localSceneDelete = JsonHelper().localDataDelete(data.title.toString(), "delete", "Scene", senseAvailableForHome[0].sense_uiud)
                sendEspHandler(localSceneDelete, senseAvailableForHome[0].sense_ip, senseAvailableForHome[0].aura_sence_name!!, "scene_delete")
            } else {
                deleteScene(data, Constant.ONLINE_MODE)
            }
        } else if (data is ButtonModel) {
            for (buttonAvailable in buttonDeviceList) {
                if (buttonAvailable.auraButtonName == data.auraButtonName) {
                    buttonDeviceList.remove(buttonAvailable)
                    break
                }
            }
            localSQLButton.deleteButtonDevice(mdbButton!!, data.auraButtonName!!)
            localSqlUtils.replaceButtonData(mDbUtils!!, "button_device", buttonDeviceList)
            AppExecutors().diskIO().execute {
                rulesTableHandler.deleteButtonDevice(data)
            }
        }
    }


    private fun deleteSceneFromLoads(scene: String) {
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
                //NEWSCENES delete all scenes
            }
        }
    }

    private fun deleteScene(data: Scenes, stateMode: String) {
        val sceneNameOld = data.title
        localSqlScene.deleteScene(mDbScene!!, sceneNameOld.toString(), Constant.HOME!!)
        deleteSceneFromLoads(sceneNameOld.toString())
        roomsScenes.remove(roomsScenes.find { it.title == data.title })
        setDataToAdapter()
        AppExecutors().diskIO().execute {
            rulesTableHandler.deleteUserScene(Constant.IDENTITY_ID!!, Constant.HOME!!, sceneNameOld.toString(), if (senseAvailableForHome.size > 0) senseAvailableForHome[0].senseMacId!! else "", if (senseAvailableForHome.size > 0) senseAvailableForHome[0].scense_thing!! else "", stateMode)
        }
        progress_layout.visibility = View.GONE

    }

    private fun openFanDimmingDialog(device: Device) {
        val dialog = Dialog(requireContext())
        val dimVal = device.dimVal
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.fan_dimming_layout)
        if (device.name == "Fan" && device.isTurnOn) {
            Glide.with(this).load(Utils.getIconDrawable(device.type, device.isTurnOn))
                    .into(dialog.iconDevice)
        } else {
            dialog.iconDevice.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
        }
        dialog.tvDialogTitle_.text = String.format(getString(R.string.fan_dimming), device.name)

        dialog.fanDim.setProgress(dimVal.toFloat())

        dialog.fanDim.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {
            }

            override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                device.dimVal = progress
                controlDevice(device, true)
            }
        })

        dialog.btnEdit_.setOnClickListener {
            if (usertype) {
                val intent = Intent(context, EditLoadActivity::class.java)
                intent.putExtra("DEVICE", Gson().toJson(device))
                intent.putExtra(Constant.ROOM_NAME, Constant.CREATE_ROOM_NAME)
                startActivity(intent)
            } else {
                SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_edit)).show()
            }

        }
        dialog.btnDoneFan.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }

    private fun showDialogCurtain(device: Device, s: String) {
        val dialog = Dialog(requireContext())
        var uiud: String? = null
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_curtain_control_edit)
        dialog.btnEdit.visibility = if (s == "edit") View.VISIBLE else View.GONE
        dialog.ivCloseCurtain.setOnClickListener {
            controlCurtainAction(device, getString(R.string.close_curtaing))
            dialog.dismiss()
        }
        dialog.ivOpenCurtain.setOnClickListener {
            controlCurtainAction(device, getString(R.string.open_curtain))
            dialog.dismiss()
        }
        dialog.ivStop.setOnClickListener {
            controlCurtainAction(device, getString(R.string.stop))
            dialog.dismiss()
        }
        dialog.btnEdit.setOnClickListener {
            if (usertype) {
                for (l in IpListDevices) {
                    if (l.name == device.deviceName) {
                        uiud = l.uiud
                        break
                    }
                }
                startActivity(Intent(activity, CurtainSetUpActivity::class.java)
                        .putExtra("DEVICE", Gson().toJson(device)).putExtra("UIUD", uiud).putExtra("TYPE", "edit"))
            } else {
                SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_edit)).show()
            }

        }
        dialog.show()
    }

    private fun controlCurtainAction(auraDevice: Device, action: String) {
        for (l in IpListDevices) {
            if (l.name == auraDevice.deviceName) {
                val data = jsonHelper.setActionCurtain(requireContext(), auraDevice, l.uiud!!, action, stateSet = true)
                l.condition[auraDevice.index] = "update"
                l.curnLoad[auraDevice.index] = true
                if (l.ip == null) {
                    if (l.aws) {
                        if (l.thing != null) {
                            if (context is RoomActivity) {
                                (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.curtainCloudControl(requireActivity(), l.uiud!!, action, auraDevice))
                            }
                        }
                    } else {
                        for (i in 0..3) {
                            l.condition[i] = "fail"
                        }
                    }
                } else if (l.local) {
                    sendEspHandler(data, l.ip!!, auraDevice.deviceName, "")
                } else if (l.aws) {
                    if (l.thing != null) {
                        if (context is RoomActivity) {
                            (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.curtainCloudControl(requireActivity(), l.uiud!!, action, auraDevice))
                        }
                    }
                } else {
                    if ((!l.local) and (!l.aws)) {
                        for (i in 0..3) {
                            l.condition[i] = "fail"
                        }
                        val message = "Device-${l.name} not available."
                        SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(message).show()
                    }
                }
                IP.registerIpDevice(l)
                // roomsAdapter.updateAppearance(IpListDevices)
                break
            }
        }
    }

    private fun dialog(device: Device) {
        val builder: ColorPickerDialog.Builder = ColorPickerDialog.Builder(context)
                .setTitle("Set colour")
                .setPreferenceName("Test")
                .setNeutralButton(getString(R.string.edit)) { dialog, which ->
                    if (usertype) {
                        val intent = Intent(context, EditLoadActivity::class.java)
                        intent.putExtra("DEVICE", Gson().toJson(device))
                        intent.putExtra(Constant.ROOM_NAME, Constant.CREATE_ROOM_NAME)
                        intent.putExtra(Constant.HOME, false)
                        startActivity(intent)
                    } else {
                        SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_edit)).show()
                    }

                }
                .setPositiveButton(getString(R.string.done), object : ColorEnvelopeListener {

                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        setLayoutColor(envelope!!, device)
                    }

                })
                .setNegativeButton(
                        getString(R.string.txt_cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
        builder.colorPickerView.brightnessSlider!!.visibility = View.VISIBLE
        builder.colorPickerView.alphaSlideBar!!.visibility = View.GONE
        builder.show()
    }


    @SuppressLint("SetTextI18n")
    private fun setLayoutColor(envelope: com.skydoves.colorpickerview.ColorEnvelope, device: Device) {
        val hsv = FloatArray(3)
        Color.colorToHSV(envelope.color, hsv)
        val saturation = hsv[1] * 100
        val lightness = hsv[2] * 100
        device.hueValue = hsv[0].toInt()
        device.saturationValue = saturation.toInt()
        device.dimVal = lightness.toInt()
        controlRGBTunnable(device, true, "RGB")

    }

    private fun controlRGBTunnable(auraDevice: Device, longPressed: Boolean, type: String) {
        var cFlag = false
        if (longPressed) {
            cFlag = true
        }
        for (l in IpListDevices) {
            if (l.name == auraDevice.deviceName) {
                if (!cFlag) {
                    auraDevice.dimVal = l.brightness
                    auraDevice.hueValue = l.hue
                    auraDevice.saturationValue = l.saturation
                    auraDevice.tempValue = l.tempLight
                }
                val data = jsonHelper.serializeDatargb(auraDevice, l.uiud!!, cFlag, type)
                l.condition[auraDevice.index] = "update"
                l.curnLoad[auraDevice.index] = true
                if (l.ip == null) {
                    if (l.aws) {
                        if (l.thing != null) {
                            if (context is RoomActivity) {
                                (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeRGBTdata(auraDevice, l.uiud!!, l.module, cFlag))
                            }
                        }
                    } else {
                        for (i in 0..3) {
                            l.condition[i] = "fail"
                        }
                    }
                } else if (l.local) {
                    sendEspHandler(data, l.ip!!, auraDevice.deviceName, "")
                } else if (l.aws) {
                    if (l.thing != null) {
                        if (context is RoomActivity) {
                            (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeRGBTdata(auraDevice, l.uiud!!, l.module, cFlag))
                        }
                    }
                } else {
                    if ((!l.local) and (!l.aws)) {
                        for (i in 0..3) {
                            l.condition[i] = "fail"
                        }
                        val message = "Device-${l.name} not available."
                        SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(message).show()
                    }
                }
                IP.registerIpDevice(l)
                break
            }
        }
    }

    private fun tunnableDialog(device: Device) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialogue_tunnable_set)
        dialog.setBrightness.setProgress(device.dimVal.toFloat())
        dialog.tempBar.setProgress(device.tempValue.toFloat())
        dialog.btnEditTunnable.visibility = View.VISIBLE
        dialog.btnEditTunnable.setOnClickListener {
            val intent = Intent(context, EditLoadActivity::class.java)
            intent.putExtra("DEVICE", Gson().toJson(device))
            intent.putExtra(Constant.ROOM_NAME, Constant.CREATE_ROOM_NAME)
            startActivity(intent)
        }

        dialog.tempBar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                device.tempValue = progress
            }

            override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

            }

        })

        dialog.setBrightness.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                device.dimVal = progress
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

            }

            override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

            }
        })
        dialog.btnOk.setOnClickListener {
            controlRGBTunnable(device, true, "Tunable")
            dialog.cancel()
        }
        dialog.show()

    }


    private fun deleteRemote(data: RemoteListModel) {
        localSqlRemote.deleteRemote(mdbSense!!, data.remoteName!!, data.auraSenseName!!)
        AppExecutors().diskIO().execute {
            rulesTableHandler.deleteRemote(Constant.IDENTITY_ID!!, data.brandName!!, data.modelNumber!!, Constant.HOME.toString())
            AppExecutors().mainThread().execute {
                progress_layout.visibility = View.GONE
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (nsd == null) {
            nsd = Nsd()
            nsd?.getInstance(requireActivity(), "ROOM")
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                onAwsMessageReceived, IntentFilter("AwsShadow"))
        nsdDiscovery()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(onNSDServiceResolved)
        nsd?.stopDiscovery()
        nsd = null
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(onNSDServiceResolved)
        nsd = null

    }

    override fun onAdaptiveChecked(data: Any, type: Any) {
        var adaptiveLightIp: String = ""
        val jsonData = JsonHelper().setAdaptiveLight((type as String), (data as AuraSwitchLoad).isAdaptive)
        for (i in IP.getIpDevices()) {
            if (i.uiud == (type as String)) {
                adaptiveLightIp = i.ip ?: ""
                break
            }
        }
        sendEspCommandAdaptive(jsonData, (data as AuraSwitchLoad).name ?: "", adaptiveLightIp)
    }

    private fun sendEspCommandAdaptive(data: String, name: String, ip: String) {
        try {
            sendEspHandler(data, ip, name, "")
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error in ESP_Handler_Connection")
        }
    }

    private fun sendEspHandler(data: String, ip: String, name: String, type: String) {
        try {
            if (espHandler == null) {
                espHandler = EspHandler(this)
            }
            espHandler?.getResponseData(data, ip, name, type)
            Log.d("CONNECT_REQUEST", data)

        } catch (e: Exception) {
            Log.d("error", "Error in ESP_Handler_Connection")
        }
    }
}


