package com.wozart.aura.ui.dashboard.room

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
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wozart.aura.R
import com.wozart.aura.aura.ui.dashboard.rooms.AddRoomActivity
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.device.SceneHandler
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraComplete
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aws.AwsState
import com.wozart.aura.entity.network.ConnectTask
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import org.jetbrains.anko.toast


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 09/08/18
 * Description :
 *****************************************************************************/
class RoomFragment : Fragment(), RoomAdapter.OnAllDevice, RoomAdapter.OffAllDevice, ConnectTask.TcpMessageReceiver, EspHandler.OnEspHandlerMessage {


    private val localSqlDatabase = DeviceTable()
    private var mDb: SQLiteDatabase? = null
    var roomsList: MutableList<RoomModel> = ArrayList()
    val rooms = ArrayList<RoomModel>()
    private val localSqlUtils = UtilsTable()
    var IpListDevices: MutableList<IpModel> = ArrayList()
    var allDeviceList = ArrayList<AuraComplete>()
    var devicesList: ArrayList<Device> = ArrayList()
    private var IP = IpHandler()
    private var nsd: Nsd? = null
    private var mDbUtils: SQLiteDatabase? = null
    var listRoom: MutableList<RoomModelJson> = ArrayList()
    var roomsListDetails: MutableList<RoomModel> = ArrayList()
    private var jsonHelper: JsonHelper = JsonHelper()
    private var sceneHandler: SceneHandler = SceneHandler()
    private var InternetStatusCheck = false
    var position_room = 0
    var senseDeviceList: MutableList<RemoteModel> = ArrayList()
    var espHandler: EspHandler? = null

    companion object {
        fun newInstance(): RoomFragment {
            return RoomFragment()
        }
    }

    var roomIcon = arrayOf(R.drawable.ic_living_room_off)
    var userType = true
    private lateinit var adapter: RoomAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_room, container, false)
        return rootView
    }

    private fun nsdDiscovery() {
        nsd?.setBroadcastType("ROOM")
        nsd?.initializeNsd()
        nsd?.discoverServices()


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nsd = Nsd()
        nsd?.getInstance(requireActivity(), "ROOM")
        nsdDiscovery()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        val roomNameList = view.findViewById(R.id.roomNameList) as androidx.recyclerview.widget.RecyclerView
        val addRoomButtonplus = view.findViewById<ImageView>(R.id.addRoomButtonplus)
        val dbUtils = UtilsDbHelper(requireContext())
        mDbUtils = dbUtils.writableDatabase
        val dbHelper = DeviceDbHelper(requireContext())
        mDb = dbHelper.writableDatabase
        IpListDevices = IP.getIpDevices()
        allDeviceList = localSqlDatabase.getAllDevicesScenes(mDb!!, Constant.HOME!!)
        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        senseDeviceList = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                onAwsMessageReceived, IntentFilter("AwsShadow"))

        adapter = RoomAdapter(requireContext(), this, this, roomsListDetails as ArrayList<RoomModel>)
        init()
        updateRoomList()
        roomNameList.adapter = adapter

        for (x in listRoom) {
            if (x.name == Constant.HOME) {
                if (x.sharedHome == "guest") {
                    userType = false
                    break
                }
            }
        }
        if (userType) {
            addRoomButtonplus.visibility = View.VISIBLE
            addRoomButtonplus.setOnClickListener {
                showpopup(addRoomButtonplus)
            }
        } else {
            addRoomButtonplus.visibility = View.INVISIBLE
        }
    }

    /**
     * Data from the TcpServer : Asynchronous
     */
    private val onNSDServiceResolved = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val name = intent.getStringExtra("name")
            val ip = intent.getStringExtra("ip")
            val device = name?.substring(name.length - 6, name.length)
            //check model and insert data
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
                if (name?.contains("Aura Switch")!!) {
                    sendTcpConnect(ipDevice.uiud!!, ipDevice.ip!!, ipDevice.name!!)
                } else {
                    sendEspHandler(jsonHelper.initialData(ipDevice.uiud!!), ipDevice.ip!!, ipDevice.name!!, "")
                }
            }
        }
    }

    private fun updateRoomList() {
        for (listrooms in roomsList) {
            val room = RoomModel()
            var flagtest = false
            for (checkroom in roomsListDetails) {
                if (checkroom.name_room == listrooms.name_room) {
                    flagtest = true
                    break
                }
            }
            if (!flagtest) {
                val totalDevice = 0
                val onDevices = 0
                val offDevices = 0
                room.name_room = listrooms.name_room
                room.roomIcon = 0
                room.roombg = listrooms.roombg
                room.room_total_device_count = totalDevice.toString()
                room.room_deviceCount_off = offDevices.toString()
                room.room_deviceCount_on = onDevices.toString()
                roomsListDetails.add(room)
            }
        }
        adapter.notifyItemChanged(position_room)
    }


    fun init() {
        val roomBg = localSqlDatabase.getRoomNews(mDb!!, Constant.HOME!!)
        for (x in roomBg) {
            val room = RoomModel()
            var isRoomExist = false
            for (y in roomsList) {
                if (y.name_room == x.name_room) {
                    isRoomExist = true
                    break
                }
            }
            if (!isRoomExist) {
                room.name_room = x.name_room
                var roomFound = false
                for (dummy in listRoom) {
                    if (dummy.name == x.name_room) {
                        room.roomIcon = roomIcon[0]
                        roomsList.add(room)
                        roomFound = true
                        break
                    }
                }
                if (!roomFound) {
                    room.roombg = 0
                    room.roomIcon = roomIcon[0]
                    roomsList.add(room)
                }

            }
        }

        for (l in listRoom) {
            val room = RoomModel()
            if ((l.type == "room") and (l.sharedHome == Constant.HOME)) {
                var flag = false
                for (r in roomsList) {
                    if (r.name_room == l.name) {
                        flag = true
                        break
                    }
                }
                if (!flag) {
                    room.name_room = l.name
                    room.roomIcon = roomIcon[l.roomIcon]
                    room.roombg = l.bgUrl.toInt()
                    roomsList.add(room)
                }
            }
        }
        for (senseRoom in senseDeviceList.filter { it.home == Constant.HOME }) {
            var flag = false
            val room = RoomModel()
            for (r in roomsList) {
                if (r.name_room == senseRoom.room) {
                    flag = true
                    break
                }
            }
            if (!flag) {
                room.name_room = senseRoom.room
                room.roombg = 0
                roomsList.add(room)
            }
        }
        updateDeviceCount(roomsList, false)
    }

    fun updateSwitchAws(data: AwsState, device: String) {
        if (data.led != 0) {
            for (l in IpListDevices) {
                if (l.name == device) {
                    if (data.uiud != null) {
                        if (data.uiud == l.uiud) {
                            if (l.module == 2 || l.module == 12 || l.module == 11 || l.module == 20) {
                                if (!l.local) {
                                    l.aws = true
                                    l.condition[0] = "ready"
                                    l.condition[1] = "ready"
                                    if (l.module == 11) {
                                        l.twoModuleDim[0] = data.dim["d0"] ?: 100
                                        l.twoModuleDim[1] = data.dim["d1"] ?: 100
                                    }
                                    l.twoModuleState[0] = data.state["s0"] == 1.0
                                    l.twoModuleState[1] = data.state["s1"] == 1.0
                                    l.failure[0] = 0
                                    l.curnLoad[0] = false
                                }
                            } else if (l.module == 1 || l.module == 7 || l.module == 8 || l.module == 13) {
                                if (data.uiud == l.uiud) {
                                    if (!l.local) {
                                        l.aws = true
                                        l.failure[0] = 0
                                        l.condition[0] = "ready"
                                        l.auraPlugState[0] = data.state["s0"] == 1.0
                                        if (l.module == 7) {
                                            l.hue = data.hue
                                            l.brightness = data.dim["d0"] ?: 100
                                            l.saturation = data.saturation
                                        } else if (l.module == 8) {
                                            l.brightness = data.dim["d0"] ?: 100
                                            l.tempLight = data.temperature
                                        }
                                        l.failure[0] = 0
                                        l.curnLoad[0] = false
                                    }

                                } else {
                                    l.condition[0] = "fail"
                                }
                            } else if (l.module == 3) {
                                if (data.uiud == l.uiud) {
                                    if (!l.local) {
                                        l.aws = true
                                        for (i in 0..3) {
                                            l.failure[i] = 0
                                            l.condition[i] = "ready"
                                        }
                                    }

                                }
                            } else if (l.module == 5) {
                                if (data.uiud == l.uiud) {
                                    if (!l.local) {
                                        l.aws = true
                                        for (i in 0..4) {
                                            l.failure[i] = 0
                                            l.condition[i] = "ready"
                                        }
                                        l.dim[0] = data.dim["d0"] ?: 100
                                        l.dim[1] = data.dim["d1"] ?: 100
                                        l.dim[2] = data.dim["d2"] ?: 100
                                        l.dim[3] = data.dim["d3"] ?: 100
                                        l.dim[4] = data.dim["d4"] ?: 100
                                        for (i in 0..4) {
                                            l.state[i] = data.state["s$i"] == 1.0
                                            l.failure[i] = 0
                                            l.curnLoad[i] = false
                                        }
                                    }

                                } else {
                                    for (i in 0..3) {
                                        l.condition[i] = "fail"
                                    }
                                }
                            } else {
                                if (!l.local) {
                                    l.aws = true
                                    for (i in 0..3) {
                                        l.condition[i] = "ready"
                                    }
                                    l.dim[0] = data.dim["d0"] ?: 100
                                    l.dim[1] = data.dim["d1"] ?: 100
                                    l.dim[2] = data.dim["d2"] ?: 100
                                    l.dim[3] = data.dim["d3"] ?: 100
                                    for (i in 0..3) {
                                        l.state[i] = data.state["s$i"] == 1.0
                                        l.failure[i] = 0
                                        l.curnLoad[i] = false
                                    }
                                }
                            }
                        }
                    }
                    IP.registerIpDevice(l)
                    updateDeviceCount(roomsList, true)
                    break
                }
            }
        } else {
            for (l in IpListDevices) {
                if (l.name == device) {
                    if (l.thing != null) {
                        if (!l.local) {
                            if (!l.aws) {
                                if (!InternetStatusCheck) {
                                    for (i in 0..4) {
                                        l.failure[i] = l.failure[i] + 1
                                        if (l.failure[i] > 2) {
                                            l.aws = false
                                            if (l.curnLoad[i]) {
                                                l.curnLoad[i] = false
                                                l.condition[i] = "fail"
                                            } else {
                                                if (!l.curnLoad[i]) {
                                                    l.condition[i] = "fail"
                                                }
                                            }
                                            l.failure[i] = 0
                                        }

                                    }
                                } else {
                                    for (i in 0..4) {
                                        l.failure[i] = l.failure[i] + 1
                                        if (l.failure[i] > 18) {
                                            l.aws = false
                                            if (l.curnLoad[i]) {
                                                l.curnLoad[i] = false
                                                l.condition[i] = "fail"
                                            } else {
                                                if (!l.curnLoad[i]) {
                                                    l.condition[i] = "fail"
                                                }
                                            }
                                            l.failure[i] = 0
                                        }

                                    }
                                }
                            }
                        }
                    }
                    break
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
            val segments = shadow?.split("/".toRegex())!!.dropLastWhile { it.isEmpty() }.toTypedArray()
            if (shadow!!.contains("accepted")) {
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
                    updateSwitchAws(data, device)
                }
            }

        }
    }

    private fun updateDeviceCount(roomsList: MutableList<RoomModel>, b: Boolean) {
        if (!b) {
            for (IplistDevice in IpListDevices) {
                for (device in allDeviceList) {
                    if ((IplistDevice.name == device.name) and (IplistDevice.owned == 0)) {
                        if (IplistDevice.home == Constant.HOME) {
                            for (room in roomsList) {
                                // val rooms = RoomModel()
                                if (device.room == room.name_room) {
                                    var flag = false
                                    for (presentRoom in roomsListDetails) {
                                        if (presentRoom.name_room == room.name_room) {
                                            flag = true
                                            break
                                        }
                                    }
                                    if (!flag) {
                                        var totalDevice = 0
                                        var onDevices = 0
                                        var offDevices = 0
                                        if (device.loads.size > 2 || IplistDevice.module == 6 || IplistDevice.module == 4 || IplistDevice.module == 23) {
                                            for (i in IplistDevice.state.indices) {
                                                if (i <= device.loads.size - 1) {
                                                    totalDevice++
                                                    if (IplistDevice.state[i]) {
                                                        onDevices++
                                                    } else {
                                                        offDevices++
                                                    }
                                                }
                                            }
                                        } else if (device.loads.size == 2 || IplistDevice.module == 2 || IplistDevice.module == 12 || IplistDevice.module == 11 || IplistDevice.module == 20) {
                                            for (i in IplistDevice.twoModuleState) {
                                                totalDevice++
                                                if (i) {
                                                    onDevices++

                                                } else {
                                                    offDevices++
                                                }
                                            }
                                        } else if (device.loads.size == 1 || IplistDevice.module == 1 || IplistDevice.module == 3) {
                                            for (i in IplistDevice.auraPlugState) {
                                                totalDevice++
                                                if (i) {
                                                    onDevices++

                                                } else {
                                                    offDevices++
                                                }
                                            }
                                        }
                                        room.room_deviceCount_on = onDevices.toString()
                                        room.room_deviceCount_off = (offDevices).toString()
                                        room.room_total_device_count = (totalDevice).toString()
                                        room.name_room = room.name_room
                                        room.roombg = room.roombg
                                        room.roomIcon = room.roomIcon
                                        roomsListDetails.add(room)
                                    }
                                    if (flag) {
                                        var totalDevice = 0
                                        var onDevices = 0
                                        var offDevices = 0
                                        if (device.loads.size > 2 || IplistDevice.module == 6 || IplistDevice.module == 4) {
                                            for (i in 0 until IplistDevice.state.size) {
                                                if (i <= device.loads.size - 1) {
                                                    totalDevice++
                                                    if (IplistDevice.state[i]) {
                                                        onDevices++
                                                    } else {
                                                        offDevices++
                                                    }
                                                }
                                            }
                                        } else if (device.loads.size == 2 || IplistDevice.module == 2 || IplistDevice.module == 20 || IplistDevice.module == 12 || IplistDevice.module == 11) {
                                            for (i in IplistDevice.twoModuleState) {
                                                totalDevice++
                                                if (i) {
                                                    onDevices++

                                                } else {
                                                    offDevices++
                                                }
                                            }
                                        } else if (device.loads.size == 1 || IplistDevice.module == 1 || IplistDevice.module == 3) {
                                            for (i in IplistDevice.auraPlugState) {
                                                totalDevice++
                                                if (i) {
                                                    onDevices++

                                                } else {
                                                    offDevices++
                                                }
                                            }
                                        }

                                        val deviceTotalCount = room.room_total_device_count!!.toInt()
                                        val deviceOnCount = room.room_deviceCount_on!!.toInt()
                                        val deviceOffCount = room.room_deviceCount_off!!.toInt()
                                        val totalCount = deviceTotalCount + totalDevice
                                        val totalOn = deviceOnCount + onDevices
                                        val totalOff = deviceOffCount + offDevices
                                        room.room_deviceCount_on = totalOn.toString()
                                        room.room_deviceCount_off = totalOff.toString()
                                        room.room_total_device_count = totalCount.toString()
                                    }

                                }

                            }

                        }
                    }
                }
            }
        } else {
            roomsListDetails.clear()
            for (IplistDevice in IpListDevices) {
                for (device in allDeviceList) {
                    if ((IplistDevice.name == device.name) and (IplistDevice.owned == 0)) {
                        if (IplistDevice.home == Constant.HOME) {
                            for (room in roomsList) {
                                // val rooms = RoomModel()
                                if (device.room == room.name_room) {
                                    var flag = false
                                    for (presentRoom in roomsListDetails) {
                                        if (presentRoom.name_room == room.name_room) {
                                            flag = true
                                            break
                                        }
                                    }
                                    if (!flag) {
                                        var totalDevice = 0
                                        var onDevices = 0
                                        var offDevices = 0
                                        if (device.loads.size > 2 || IplistDevice.module == 6 || IplistDevice.module == 4 || IplistDevice.module == 23) {
                                            for (i in 0 until IplistDevice.state.size) {
                                                if (i <= device.loads.size - 1) {
                                                    totalDevice++
                                                    if (IplistDevice.state[i]) {
                                                        onDevices++
                                                    } else {
                                                        offDevices++
                                                    }
                                                }
                                            }
                                        } else if (device.loads.size == 2 || IplistDevice.module == 2 || IplistDevice.module == 12 || IplistDevice.module == 11 || IplistDevice.module == 20) {
                                            for (i in IplistDevice.twoModuleState) {
                                                totalDevice++
                                                if (i) {
                                                    onDevices++

                                                } else {
                                                    offDevices++
                                                }
                                            }
                                        } else if (device.loads.size == 1 || IplistDevice.module == 1 || IplistDevice.module == 3) {
                                            for (i in IplistDevice.auraPlugState) {
                                                totalDevice++
                                                if (i) {
                                                    onDevices++

                                                } else {
                                                    offDevices++
                                                }
                                            }
                                        }
                                        room.room_deviceCount_on = onDevices.toString()
                                        room.room_deviceCount_off = offDevices.toString()
                                        room.room_total_device_count = totalDevice.toString()
                                        room.name_room = room.name_room
                                        room.roombg = room.roombg
                                        room.roomIcon = room.roomIcon

                                        roomsListDetails.add(room)
                                    }
                                    if (flag) {
                                        var totalDevice = 0
                                        var onDevices = 0
                                        var offDevices = 0
                                        if (device.loads.size > 2) {
                                            for (i in IplistDevice.state) {
                                                totalDevice++
                                                if (i) {
                                                    onDevices++

                                                } else {
                                                    offDevices++
                                                }
                                            }
                                        } else if (device.loads.size == 2 || IplistDevice.module == 2 || IplistDevice.module == 12 || IplistDevice.module == 11 || IplistDevice.module == 20) {
                                            for (i in IplistDevice.twoModuleState) {
                                                totalDevice++
                                                if (i) {
                                                    onDevices++

                                                } else {
                                                    offDevices++
                                                }
                                            }
                                        } else if (device.loads.size == 1 || IplistDevice.module == 1 || IplistDevice.module == 3) {
                                            for (i in IplistDevice.auraPlugState) {
                                                totalDevice++
                                                if (i) {
                                                    onDevices++

                                                } else {
                                                    offDevices++
                                                }
                                            }
                                        }
                                        val deviceTotalCount = room.room_total_device_count!!.toInt()
                                        val deviceOnCount = room.room_deviceCount_on!!.toInt()
                                        val deviceOffCount = room.room_deviceCount_off!!.toInt()
                                        val totalCount = deviceTotalCount + totalDevice
                                        val totalOn = deviceOnCount + onDevices
                                        val totalOff = deviceOffCount + offDevices
                                        room.room_deviceCount_on = totalOn.toString()
                                        room.room_deviceCount_off = totalOff.toString()
                                        room.room_total_device_count = totalCount.toString()
                                    }

                                }

                            }

                        }
                    }
                }
            }
            updateRoomList()
        }

    }

    override fun onAlldevice(name_room: String?, b: Boolean, position: Int) {
        position_room = position
        val listOfDevices: ArrayList<AuraSwitch> = localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, name_room!!)
        devicesList.clear()
        var curtainPersent = false
        val ipList = IP.getIpDevices()
        for (device in listOfDevices) {
            for (l in ipList) {
                if (device.name == l.name) {
                    curtainPersent = false
                    for (i in 0 until device.loads.size) {
                        l.state[i] = b
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
                        if (device.loads[i].type == "Curtain") {
                            curtainPersent = true
                        } else if (device.loads[i].type == "rgbDevice") {
                            load.checkType = device.loads[i].type.toString()
                            load.hueValue = l.hue
                            load.saturationValue = l.saturation
                            load.dimVal = l.brightness
                        } else if (device.loads[i].type == "tunableDevice") {
                            load.checkType = device.loads[i].type.toString()
                            load.tempValue = l.tempLight
                            load.dimVal = l.brightness
                        }
                        if (!curtainPersent) {
                            devicesList.add(load)
                        }
                    }
                    break
                }

            }
        }

        val deviceLoad = sceneHandler.convertToAllLoad(devicesList)
        for (device in deviceLoad) {
            for (l in ipList) {
                if (device.device == l.name) {
                    val data = jsonHelper.serializeOnAllDevice(device, l.uiud!!, true, device.device!!, l.module)
                    if (l.ip == null) {
                        if (l.aws) {
                            if (l.thing != null) {
                                if (context is DashboardActivity) {
                                    var flag = (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeOnAllDeviceAws(device, b))

                                }
                            }
                        }

                    } else {
                        if (l.local) {
                            if (l.fullDeviceName!!.startsWith("Aura Switch")) {
                                ConnectTask(requireContext(), this, data, l.ip!!, device.device!!).execute("")
                            } else {
                                sendEspHandler(data, l.ip!!, device.device!!, "")
                            }
                        } else {
                            if (l.aws) {
                                if (l.thing != null) {
                                    if (context is RoomActivity) {
                                        var flag = (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeOnAllDeviceAws(device, b))

                                    }
                                }
                            }
                        }
                    }
                    break
                }
            }
        }
    }

    override fun offAllDevice(name_room: String?, b: Boolean, position: Int) {
        position_room = position
        val listOfDevices: ArrayList<AuraSwitch> = localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, name_room!!)
        val ipList = IP.getIpDevices()
        devicesList.clear()
        var curtainPersent = false
        for (device in listOfDevices) {
            for (l in ipList) {
                if (device.name == l.name) {
                    curtainPersent = false
                    for (i in 0 until device.loads.size) {
                        l.state[i] = b
                        val load = Device(device.loads[i].icon!!, l.state[i]!!, false, l.dim[i], device.loads[i].name!!, localSqlDatabase.getRoomForDevice(mDb!!, device.name), device.name, device.loads[i].index!!, device.loads[i].dimmable!!)
                        if (l.condition[i] == "ready") {
                            if (l.local) {
                                load.status = "on"
                            } else {
                                load.status = "cloud"
                            }
                        } else {
                            load.status = "update"
                        }
                        if (device.loads[i].type == "Curtain") {
                            curtainPersent = true
                        } else if (device.loads[i].type == "rgbDevice") {
                            load.checkType = device.loads[i].type.toString()
                            load.hueValue = l.hue
                            load.saturationValue = l.saturation
                            load.dimVal = l.brightness
                        } else if (device.loads[i].type == "tunableDevice") {
                            load.checkType = device.loads[i].type.toString()
                            load.tempValue = l.tempLight
                            load.dimVal = l.brightness
                        }
                        if (!curtainPersent) {
                            devicesList.add(load)
                        }
                    }
                    break
                }

            }
        }

        val deviceLoad = sceneHandler.convertToAllLoad(devicesList)
        for (device in deviceLoad) {
            for (l in ipList) {
                if (device.device == l.name) {
                    val data = jsonHelper.serializeOnAllDevice(device, l.uiud!!, false, device.device!!, l.module)
                    if (l.ip == null) {
                        if (l.aws) {
                            if (l.thing != null) {
                                if (context is DashboardActivity) {
                                    (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeOnAllDeviceAws(device, b))
                                }
                            }
                        }
                    } else {
                        if (l.local) {
                            if (l.fullDeviceName!!.startsWith("Aura Switch")) {
                                ConnectTask(requireContext(), this, data, l.ip!!, device.device!!).execute("")
                            } else {
                                sendEspHandler(data, l.ip!!, device.device!!, "")
                            }
                        } else {
                            if (l.aws) {
                                if (l.thing != null) {
                                    if (context is DashboardActivity) {
                                        (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeOnAllDeviceAws(device, b))
                                    }
                                }
                            }

                        }
                    }
                    break
                }
            }
        }
    }

    override fun onTcpMessageReceived(message: String) {
        AppExecutors().mainThread().execute {
            updateStates(message)
        }
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        AppExecutors().mainThread().execute {
            updateStates(decryptedData)
        }
    }


    private fun showpopup(v: View) {
        val popup = context?.let { PopupMenu(it, v) }
        val inflater = popup?.menuInflater
        inflater?.inflate(R.menu.room_add_option, popup.menu)
        popup?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.addRoom -> {
                    val intent = Intent(context, AddRoomActivity::class.java)
                    intent.putExtra("ROOM_NAME", "Room Name")
                    intent.putExtra("ROOM_EDIT_TYPE", "create")
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popup?.show()
    }

    private fun sendTcpConnect(uiud: String, ip: String, name: String) {
        try {
            ConnectTask(requireContext(), this, jsonHelper.initialData(uiud), ip, name).execute("")
        } catch (e: Exception) {

        }
    }


    private fun sendEspHandler(data: String, ip: String, name: String, type: String) {

        try {
            if (espHandler == null) {
                espHandler = EspHandler(this)
            }
            espHandler?.getResponseData(data, ip, name, type)
        } catch (e: Exception) {
            Log.d("error", "Error in ESP_Handler_Connection")
        }
    }

    private fun updateStates(message: String) {
        if (message.contains("ERROR")) {
            val device_error_name: String
            var deviceError: Int = -1
            deviceError = -1
            val data = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            device_error_name = data[1]
            if (deviceError != 0) {
                for (x in data) {
                    for (l in IpListDevices) {
                        if (l.name == device_error_name) {
                            if (l.owned == 0) {
                                if (l.thing != null) {
                                    if (l.aws) {
                                        for (i in 0..3) {
                                            l.condition[i] = "ready"
                                        }
                                    } else {
                                        InternetStatusCheck = true
                                        for (i in 0..3) {
                                            l.condition[i] = "update"
                                            l.failure[i] = 0
                                        }
                                        l.local = false
                                        l.aws = true
                                        if (context is DashboardActivity)
                                            (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeLEDData())
                                    }
                                }

                            }
                            IP.registerIpDevice(l)
                            break
                        }
                    }
                }
            }

        } else {
            val updatedDevice: AuraSwitch = jsonHelper.deserializeTcp(message)
            when (updatedDevice.type) {
                1 -> {
                    for (l in IpListDevices) {
                        if (l.name == updatedDevice.name) {
                            if (updatedDevice.state.size == 2 || updatedDevice.mdl == 2 || updatedDevice.mdl == 12 || updatedDevice.mdl == 11 || updatedDevice.mdl == 20) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.twoModuleState[0] = updatedDevice.state[0] == 1
                                l.twoModuleState[1] = updatedDevice.state[1] == 1
                                l.twoModuleDim[0] = updatedDevice.dim[0]
                                l.twoModuleDim[1] = updatedDevice.dim[1]

                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 1) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.auraPlugState[0] = updatedDevice.state[0] == 1
                                l.auraPlugPower = updatedDevice.p0
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 3) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.curtainState[0] = updatedDevice.state[0]
                                l.curtainState[1] = updatedDevice.state[1]
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 7 || updatedDevice.mdl == 13) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.brightness = updatedDevice.brightness
                                l.saturation = updatedDevice.saturation
                                l.hue = updatedDevice.hue
                                l.auraPlugState[0] = updatedDevice.state[0] == 1
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 8) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.brightness = updatedDevice.brightness
                                l.saturation = updatedDevice.saturation
                                l.hue = updatedDevice.hue
                                l.tempLight = updatedDevice.temperature
                                l.auraPlugState[0] = updatedDevice.state[0] == 1
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 5) {
                                l.module = updatedDevice.mdl
                                l.state[0] = updatedDevice.state[0] == 1
                                l.state[1] = updatedDevice.state[1] == 1
                                l.state[2] = updatedDevice.state[2] == 1
                                l.state[3] = updatedDevice.state[3] == 1
                                l.state[4] = updatedDevice.state[4] == 1
                                l.dim[0] = updatedDevice.dim[0]
                                l.dim[1] = updatedDevice.dim[1]
                                l.dim[2] = updatedDevice.dim[2]
                                l.dim[3] = updatedDevice.dim[3]
                                l.dim[4] = updatedDevice.dim[4]
                                l.local = true
                                l.aws = false
                                for (i in 0..4) {
                                    l.condition[i] = "ready"
                                }
                            } else {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.state[0] = updatedDevice.state[0] == 1
                                l.state[1] = updatedDevice.state[1] == 1
                                l.state[2] = updatedDevice.state[2] == 1
                                l.state[3] = updatedDevice.state[3] == 1
                                l.dim[0] = updatedDevice.dim[0]
                                l.dim[1] = updatedDevice.dim[1]
                                l.dim[2] = updatedDevice.dim[2]
                                l.dim[3] = updatedDevice.dim[3]
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            }

                            IP.registerIpDevice(l)
                            updateDeviceCount(roomsList, true)
                            break
                        }

                    }

                }

                4, 3 -> {
                    if (updatedDevice.error == 1) {
                        for (l in IpListDevices) {
                            if (l.name == updatedDevice.name) {
                                l.local = false
                                l.aws = false
                                for (i in 0..3) {
                                    l.condition[i] = "fail"
                                }
                                IP.registerIpDevice(l)
                                break
                            }
                        }
                        requireActivity().toast("Device used by someone,Unauthorized Access")
                    } else {
                        for (l in IpListDevices) {
                            if (l.name == updatedDevice.name) {
                                if (updatedDevice.mdl == 2 || updatedDevice.mdl == 12 || updatedDevice.mdl == 11 || updatedDevice.mdl == 20) {
                                    l.local = true
                                    l.aws = false
                                    l.module = updatedDevice.mdl
                                    l.twoModuleState[0] = updatedDevice.state[0] == 1
                                    l.twoModuleState[1] = updatedDevice.state[1] == 1
                                    l.twoModuleDim[0] = updatedDevice.dim[0]
                                    l.twoModuleDim[1] = updatedDevice.dim[1]

                                    for (i in 0..3) {
                                        l.condition[i] = "ready"
                                    }
                                } else if (updatedDevice.mdl == 1) {
                                    l.local = true
                                    l.aws = false
                                    l.module = updatedDevice.mdl
                                    l.auraPlugState[0] = updatedDevice.state[0] == 1
                                    l.auraPlugPower = updatedDevice.p0
                                    for (i in 0..3) {
                                        l.condition[i] = "ready"
                                    }
                                } else if (updatedDevice.mdl == 3) {
                                    l.local = true
                                    l.aws = false
                                    l.module = updatedDevice.mdl
                                    l.curtainState[0] = updatedDevice.state[0]
                                    l.curtainState[1] = updatedDevice.state[1]
                                    for (i in 0..3) {
                                        l.condition[i] = "ready"
                                    }
                                } else if (updatedDevice.mdl == 7 || updatedDevice.mdl == 13) {
                                    l.local = true
                                    l.aws = false
                                    l.module = updatedDevice.mdl
                                    l.brightness = updatedDevice.brightness
                                    l.saturation = updatedDevice.saturation
                                    l.hue = updatedDevice.hue
                                    l.auraPlugState[0] = updatedDevice.state[0] == 1
                                    for (i in 0..3) {
                                        l.condition[i] = "ready"
                                    }
                                } else if (updatedDevice.mdl == 8) {
                                    l.local = true
                                    l.aws = false
                                    l.module = updatedDevice.mdl
                                    l.brightness = updatedDevice.brightness
                                    l.saturation = updatedDevice.saturation
                                    l.hue = updatedDevice.hue
                                    l.tempLight = updatedDevice.temperature
                                    l.auraPlugState[0] = updatedDevice.state[0] == 1
                                    for (i in 0..3) {
                                        l.condition[i] = "ready"
                                    }
                                } else if (updatedDevice.mdl == 5) {
                                    l.module = updatedDevice.mdl
                                    l.state[0] = updatedDevice.state[0] == 1
                                    l.state[1] = updatedDevice.state[1] == 1
                                    l.state[2] = updatedDevice.state[2] == 1
                                    l.state[3] = updatedDevice.state[3] == 1
                                    l.state[4] = updatedDevice.state[4] == 1
                                    l.dim[0] = updatedDevice.dim[0]
                                    l.dim[1] = updatedDevice.dim[1]
                                    l.dim[2] = updatedDevice.dim[2]
                                    l.dim[3] = updatedDevice.dim[3]
                                    l.dim[4] = updatedDevice.dim[4]
                                    l.local = true
                                    l.aws = false
                                    for (i in 0..4) {
                                        l.condition[i] = "ready"
                                    }
                                }  else {
                                    l.local = true
                                    l.module = updatedDevice.mdl
                                    l.state[0] = updatedDevice.state[0] == 1
                                    l.state[1] = updatedDevice.state[1] == 1
                                    l.state[2] = updatedDevice.state[2] == 1
                                    l.state[3] = updatedDevice.state[3] == 1
                                    l.dim[0] = updatedDevice.dim[0]
                                    l.dim[1] = updatedDevice.dim[1]
                                    l.dim[2] = updatedDevice.dim[2]
                                    l.dim[3] = updatedDevice.dim[3]
                                    for (i in 0..3) {
                                        l.condition[i] = "ready"
                                    }
                                    l.aws = false
                                }
                                IP.registerIpDevice(l)
                                updateDeviceCount(roomsList, true)
                                break
                            }
                        }
                    }
                }
                else -> {
                    for (l in IpListDevices) {
                        if (l.name == updatedDevice.name) {
                            if (updatedDevice.mdl == 2 || updatedDevice.mdl == 12 || updatedDevice.mdl == 11 || updatedDevice.mdl == 20) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.twoModuleState[0] = updatedDevice.state[0] == 1
                                l.twoModuleState[1] = updatedDevice.state[1] == 1
                                l.twoModuleDim[0] = updatedDevice.dim[0]
                                l.twoModuleDim[1] = updatedDevice.dim[1]

                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 1) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.auraPlugState[0] = updatedDevice.state[0] == 1
                                l.auraPlugPower = updatedDevice.p0
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 3) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.curtainState[0] = updatedDevice.state[0]
                                l.curtainState[1] = updatedDevice.state[1]
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 7 || updatedDevice.mdl == 13) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.brightness = updatedDevice.brightness
                                l.saturation = updatedDevice.saturation
                                l.hue = updatedDevice.hue
                                l.auraPlugState[0] = updatedDevice.state[0] == 1
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 8) {
                                l.local = true
                                l.aws = false
                                l.module = updatedDevice.mdl
                                l.brightness = updatedDevice.brightness
                                l.saturation = updatedDevice.saturation
                                l.hue = updatedDevice.hue
                                l.tempLight = updatedDevice.temperature
                                l.auraPlugState[0] = updatedDevice.state[0] == 1
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            } else if (updatedDevice.mdl == 5) {
                                l.module = updatedDevice.mdl
                                l.state[0] = updatedDevice.state[0] == 1
                                l.state[1] = updatedDevice.state[1] == 1
                                l.state[2] = updatedDevice.state[2] == 1
                                l.state[3] = updatedDevice.state[3] == 1
                                l.state[4] = updatedDevice.state[4] == 1
                                l.dim[0] = updatedDevice.dim[0]
                                l.dim[1] = updatedDevice.dim[1]
                                l.dim[2] = updatedDevice.dim[2]
                                l.dim[3] = updatedDevice.dim[3]
                                l.dim[4] = updatedDevice.dim[4]
                                l.local = true
                                l.aws = false
                                for (i in 0..4) {
                                    l.condition[i] = "ready"
                                }
                            } else {
                                l.module = updatedDevice.mdl
                                l.state[0] = updatedDevice.state[0] == 1
                                l.state[1] = updatedDevice.state[1] == 1
                                l.state[2] = updatedDevice.state[2] == 1
                                l.state[3] = updatedDevice.state[3] == 1
                                l.dim[0] = updatedDevice.dim[0]
                                l.dim[1] = updatedDevice.dim[1]
                                l.dim[2] = updatedDevice.dim[2]
                                l.dim[3] = updatedDevice.dim[3]
                                l.local = true
                                l.aws = false
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                            }

                            IP.registerIpDevice(l)
                            break
                        }
                    }
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (nsd == null) {
            nsd = Nsd()
            nsd?.getInstance(requireActivity(), "HOME")
        }
        nsdDiscovery()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                onAwsMessageReceived, IntentFilter("AwsShadow"))
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

    override fun onDetach() {
        super.onDetach()
        if (context is RoomActivity) {
            (context as RoomActivity).unbindService((context as RoomActivity).mConnection)
            (context as RoomActivity).mBounded = false
        }
    }

}
