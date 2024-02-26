package com.wozart.aura.ui.dashboard.more

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.R
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.Notification
import com.wozart.aura.ui.adapter.NotificationAdapter
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.data.sqlLite.ScheduleTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraSwitchLoad
import com.wozart.aura.entity.model.aura.DeviceTableModel
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.createautomation.GeoModal
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_notification.*
import org.jetbrains.anko.design.longSnackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class NotificationActivity : AppCompatActivity(), NotificationAdapter.OnAcceptListener, NotificationAdapter.OnDeclineListener {


    private var notificationAdapter: NotificationAdapter? = null
    private var localSqlDevices = DeviceTable()
    private val localSqlSceneDatabase = SceneTable()
    var localSqlSchedule = ScheduleTable()
    val localSqlUtils = UtilsTable()
    private var mDb: SQLiteDatabase? = null
    private var mDbScene: SQLiteDatabase? = null
    private var mDbSchedule: SQLiteDatabase? = null
    var mDbUtils: SQLiteDatabase? = null
    var IpListDevices: MutableList<IpModel> = ArrayList()
    var IpListDevicesNew: MutableList<IpModel> = ArrayList()
    var allDeviceList = java.util.ArrayList<DeviceTableModel>()
    var rulesTableDo = RulesTableHandler()
    private var IP = IpHandler()
    var presentGuestHome: String? = null
    var guestPresnt: ArrayList<String> = ArrayList()
    var location: String = ""
    var latitude = 0.0
    var longitude = 0.0
    var sharedHomelocation: String = ""
    var shareHomelatitude = 0.0
    var sharedHomelongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        tvTitle.text = getString(R.string.invites_screen_title)
        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase
        val dbHelperScene = SceneDbHelper(this)
        mDbScene = dbHelperScene.writableDatabase
        val dbHelperScedule = ScheduleDbHelper(this)
        mDbSchedule = dbHelperScedule.writableDatabase
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        progress_bar.visibility = View.VISIBLE
        val listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        for (x in listRoom) {
            if (x.sharedHome == "guest") {
                presentGuestHome = x.name
                guestPresnt.add(presentGuestHome!!)
            }
        }
        thread {
            val user = rulesTableDo.getUser()
            val notifications: MutableList<Notification> = ArrayList()
            runOnUiThread {
                if (user != null) {
                    if (user.guest != null) {
                        for (invites in user.guest!!) {
                            var flagGuestExist = false
                            if (invites["Access"] == "invite") {
                                for (guestHome in guestPresnt) {
                                    flagGuestExist = false
                                    if (guestHome == invites["Home"]!!) {
                                        val notification = Notification()
                                        notification.status = 1
                                        notification.notificationTitle = "invite from ${invites["Name"]} "
                                        notifications.add(notification)
                                        flagGuestExist = true
                                        break
                                    }
                                }
                                if (!flagGuestExist) {
                                    val notification = Notification()
                                    notification.notificationTitle = "Invite from ${invites["Name"]} "
                                    val home = invites["Home"]!!.split("?")
                                    notification.notificationMessage = "Here are the keys of ${home[0]}"
                                    notification.sharedUserId = invites["Email"]
                                    notification.masterName = invites["Name"]!!.split(" ")[0]
                                    notification.sharedHome = invites["Home"]
                                    notification.access = invites["Access"]
                                    notification.ownerId = user.userId
                                    notification.status = 0
                                    notifications.add(notification)
                                }
                            }
                        }
                    } else if (user.guest == null) {
                        if (user.notifications != null) {
                            if (user.notifications!!.size == 0) {
                                Toast.makeText(this, "Sorry no notification found.", Toast.LENGTH_SHORT).show()
                            } else {
                                val notification = Notification()
                                notification.notificationMessage = user.notifications!!.get(0)
                                notification.status = 2
                                notifications.add(notification)
                            }
                        } else {
                            val notification = Notification()
                            notification.notificationMessage = "Sorry no notification found."
                            notification.status = 2
                            notifications.add(notification)
                        }
                    } else if (user.guest!!.size == 0) {
                        if (user.notifications != null) {
                            if (user.notifications!!.size == 0) {
                                Toast.makeText(this, "No notification..", Toast.LENGTH_SHORT).show()
                            } else {
                                val notification = Notification()
                                notification.notificationMessage = user.notifications!!.get(0)
                                notification.status = 2
                                notifications.add(notification)
                            }
                        }
                    }
                }else{
                    emptyNotificationTv.visibility = View.VISIBLE
                }

                progress_bar.visibility = View.GONE
                notificationAdapter = NotificationAdapter(this, notifications, this, this)
                list_notification.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
                list_notification.adapter = notificationAdapter

            }
        }

        back.setOnClickListener {
            finish()
        }
    }

    override fun onAcceptSelected(request: Notification) {
        progress_bar.visibility = View.VISIBLE
        val gson = Gson()
        thread {
            val rulesTable = rulesTableDo.getRulesTable()
            if (rulesTable!!.userId == request.ownerId) {
                //updating devices

                var masterDeviceflag = false
                if (rulesTable.masterDevices != null) {
                    if (rulesTable.masterDevices!!.size != 0) {
                        masterDeviceflag = true
                    }
                }
                if (masterDeviceflag) {
                    for (shareDevice in rulesTable.masterDevices!!) {
                        if (shareDevice["deviceType"] != "IR_device") {
                            val sharedHome = shareDevice["home"]
                            val sharedroom = shareDevice["room"]
                            val uiud = shareDevice["uiud"]
                            val sharedname = shareDevice["name"]
                            val sharedthing = shareDevice["thing"]
                            val shareLoads = rulesTable.masterLoads
                            val localLoads: MutableList<AuraSwitchLoad> = ArrayList()
                            for (sharedloads in shareLoads!!) {
                                var flag = false
                                for (load in sharedloads) {
                                    if ((load["type"] == "device" && load["device"] == shareDevice["id"] && load["type"] != "IR_device")) {
                                        flag = true
                                        break
                                    }
                                }
                                if (flag) {
                                    for (load in sharedloads) {
                                        if (load["type"] != "device") {
                                            val localLoad = AuraSwitchLoad()
                                            localLoad.index = load["index"]!!.toInt()
                                            localLoad.icon = load["icon"]!!.toInt()
                                            localLoad.name = load["name"]
                                            localLoad.type = load["type"]
                                            if (localLoad.type == "Curtain") {
                                                localLoad.curtainState = load["curtainSetState"].toString()
                                                localLoad.curtainState0 = load["curtainStatus0"]!!.toInt()
                                                localLoad.curtainState1 = load["curtainStatus1"]!!.toInt()
                                            }
                                            localLoad.favourite = load["favourite"]!!.toBoolean()
                                            localLoad.dimmable = load["dimmable"]!!.toBoolean()
                                            localLoad.module = load["mdl"]?.toInt() ?: -1
                                            localLoads.add(localLoad)
                                        }
                                    }
                                    break
                                }
                            }
                            localSqlDevices.insertDevice(mDb!!, sharedHome!!, sharedroom!!, uiud!!, sharedname!!, gson.toJson(localLoads), sharedthing!!)
                        }
                    }
                }
                val allsharedDevice = localSqlDevices.getDeviceTable(mDb!!)

                //update sharing Scene
                var sharingSceneFlag = false
                if (rulesTable.masterScenes != null) {
                    if (rulesTable.masterScenes!!.size != 0) {
                        sharingSceneFlag = true
                    }
                }
                if (sharingSceneFlag) {
                    val allShareScenes = rulesTable.masterScenes
                    for (sharescenes in allShareScenes!!) {
                        val rooms: ArrayList<RoomModel> = ArrayList()
                        val keys = sharescenes.keys
                        var pair = ""
                        for (k in keys) {
                            pair = k
                        }
                        val sharedscene = sharescenes[pair]
                        var sharedsceneName = ""
                        var icon = 0
                        var sharedhome = Constant.HOME

                        //extract all room list
                        val roomList: MutableList<String> = ArrayList()
                        val deviceList: MutableList<Device> = ArrayList()
                        val sceneRemote: MutableList<RemoteIconModel> = ArrayList()
                        for (s in sharedscene!!) {
                            if (s["type"] != "scene" && s["type"] != "remote") {
                                val device = Device(0, s["state"]!!.toBoolean(), false, s["level"]!!.toInt(), s["deviceName"]!!, "room", s["deviceName"]!!, s["index"]!!.toInt(), false)
                                for (d in allsharedDevice) {
                                    if (d.name == s["deviceName"]) {
                                        device.type = d.loads[device.index].icon!!
                                        device.name = d.loads[device.index].name!!
                                        device.roomName = d.room!!
                                        device.dimmable = d.loads[device.index].dimmable!!
                                        var flag = false
                                        for (r in roomList) {
                                            if (r == d.room) {
                                                flag = true
                                                break
                                            }
                                        }
                                        if (!flag) {
                                            roomList.add(d.room!!)
                                        }
                                        deviceList.add(device)
                                        break
                                    }
                                }
                                if (s["type"] == "Automation") {
                                    val auromationList = localSqlSchedule.getAutomationScene(mDbSchedule!!, Constant.HOME!!)
                                    for (schedule in auromationList) {
                                        if (s["deviceName"].toString() == schedule.name) {
                                            device.isTurnOn = s["state"]!!.toBoolean()
                                            device.deviceName = s["deviceName"].toString()
                                            device.name = s["type"].toString()
                                            device.roomName = schedule.room[0].name!!
                                            var flag = false
                                            for (r in roomList) {
                                                if (r == schedule.room[0].name) {
                                                    flag = true
                                                    break
                                                }
                                            }
                                            if (!flag) {
                                                roomList.add(schedule.room[0].name!!)
                                            }
                                            deviceList.add(device)
                                            break
                                        }
                                    }
                                }

                            } else if (s["type"] == "remote") {
                                val remote = RemoteIconModel()
                                remote.remoteButtonName = s["btnName"]
                                remote.remoteModel = s["model"]
                                remote.channelNumber = s["channel"].toString()
                                remote.btnFavourite = s["favourite"].toBoolean()
                                remote.name = s["senseName"]
                                sceneRemote.add(remote)
                            } else {
                                sharedsceneName = s["name"]!!
                                icon = s["icon"]!!.toInt()
                                sharedhome = s["home"]!!
                            }
                        }
                        for (r in roomList) {
                            val room = RoomModel()
                            room.name = r
                            for (d in deviceList) {
                                if (d.roomName == r) {
                                    room.deviceList.add(d)
                                }
                            }
                            rooms.add(room)
                        }
                        localSqlSceneDatabase.insertScene(mDbScene!!, sharedsceneName, gson.toJson(rooms), gson.toJson(roomList), sharedhome!!, icon, sharedsceneName, "create","")
                    }
                }

                //update sharing master schedule
                var sharingSchedulingFlag = false
                if (rulesTable.masterSchedules != null) {
                    if (rulesTable.masterSchedules!!.size != 0) {
                        sharingSchedulingFlag = true
                    }
                }
                if (sharingSchedulingFlag) {
                    val allshareSchedule = rulesTable.masterSchedules
                    for (sharSchedule in allshareSchedule!!) {
                        val rooms: ArrayList<RoomModel> = ArrayList()
                        val keys = sharSchedule.keys
                        var pair = ""
                        for (k in keys) {
                            pair = k
                        }
                        val schedule = sharSchedule[pair]
                        var sharedscheduleName = ""
                        var icon = 0
                        var sharedhome: String? = null
                        val sharedscheduleStatus: MutableList<GeoModal> = ArrayList()
                        val geoData = GeoModal()
                        var sharedscheduleStartTime: String? = null
                        var sharedscheduleType: String? = null
                        var sharedscheduleEndTime: String? = null
                        var sharedscheduleRoutine: String? = null
                        val roomList: MutableList<String> = ArrayList()
                        val deviceList: MutableList<Device> = ArrayList()
                        var scheduleRemote: MutableList<RemoteIconModel> = ArrayList()
                        for (sheduleshare in schedule!!) {
                            if (sheduleshare["type"] != "schedule" && sheduleshare["type"] != "remote") {
                                val device = Device(0, sheduleshare["state"]!!.toBoolean(), false, sheduleshare["level"]!!.toInt(), sheduleshare["deviceName"]!!, "room", sheduleshare["deviceName"]!!, sheduleshare["index"]!!.toInt(), false)
                                for (d in allsharedDevice) {
                                    if (d.name == sheduleshare["deviceName"]) {
                                        device.type = d.loads[device.index].icon!!
                                        device.name = d.loads[device.index].name!!
                                        device.roomName = d.room!!
                                        device.dimmable = d.loads[device.index].dimmable!!
                                        var flag = false
                                        for (r in roomList) {
                                            if (r == d.room) {
                                                flag = true
                                                break
                                            }
                                        }
                                        if (!flag) {
                                            roomList.add(d.room!!)
                                        }
                                        deviceList.add(device)
                                        break
                                    }
                                }
                                if (sheduleshare["type"] == "Scene") {
                                    val scene = localSqlSceneDatabase.getAllScenes(mDbScene!!, Constant.HOME!!)
                                    for (s_ in scene) {
                                        if (sheduleshare["deviceName"] == s_.name) {
                                            device.roomName = s_.roomName.toString()
                                            device.index = 0
                                            device.type = s_.icon
                                            device.isTurnOn = sheduleshare["state"]!!.toBoolean()
                                            device.deviceName = s_.name.toString()
                                            device.name = "Scene"
                                            device.dimmable = true
                                            var flag = false
                                            for (r in roomList) {
                                                if (r == s_.roomName) {
                                                    flag = true
                                                    break
                                                }
                                            }
                                            if (!flag) {
                                                if (s_.roomName != null) {
                                                    roomList.add(s_.roomName!!)
                                                }
                                            }
                                            deviceList.add(device)
                                            break
                                        }
                                    }
                                }
                            } else if (sheduleshare["type"] == "remote") {
                                val remote = RemoteIconModel()
                                remote.remoteButtonName = sheduleshare["btnName"]
                                remote.remoteModel = sheduleshare["model"]
                                remote.channelNumber = sheduleshare["channel"].toString()
                                remote.btnFavourite = sheduleshare["favourite"].toBoolean()
                                remote.name = sheduleshare["senseName"]
                                scheduleRemote.add(remote)
                            } else {
                                sharedscheduleType = sheduleshare["stype"]!!
                                if (sharedscheduleType == "geo") {
                                    geoData.newGeoRadius = sheduleshare["radius"]!!.toFloat()
                                    geoData.newGeoLat = sheduleshare["latitude"]!!.toDouble()
                                    geoData.newGeolong = sheduleshare["longitude"]!!.toDouble()
                                    geoData.triggerType = sheduleshare["triggeringType"]
                                    geoData.triggerWhen = sheduleshare["triggerwhen"]
                                    if (sheduleshare["turnOff"] != null) {
                                        geoData.turnOff = sheduleshare["turnOff"]
                                    } else {
                                        geoData.turnOff = "Never"
                                    }
                                    if (sheduleshare["automationEnable"] != null) {
                                        geoData.AutomationEnable = sheduleshare["automationEnable"]!!.toBoolean()
                                    } else {
                                        geoData.AutomationEnable = true
                                    }
                                    geoData.triggerSpecificStartTime = sheduleshare["specifictime"]
                                    geoData.triggerSpecificEndTime = sheduleshare["specificend"]
                                    sharedscheduleStatus.add(geoData)
                                    sharedscheduleName = sheduleshare["name"]!!
                                    icon = sheduleshare["icon"]!!.toInt()
                                    sharedhome = sheduleshare["home"]
                                    sharedscheduleType = sheduleshare["stype"]!!
                                    sharedscheduleStartTime = ""
                                    sharedscheduleEndTime = ""
                                    sharedscheduleRoutine = ""

                                } else {
                                    geoData.AutomationEnable = sheduleshare["automationEnable"]!!.toBoolean()
                                    if (sheduleshare["turnOff"] != null) {
                                        geoData.turnOff = sheduleshare["turnOff"]
                                    } else {
                                        geoData.turnOff = "Never"
                                    }
                                    sharedscheduleStatus.add(geoData)
                                    sharedscheduleName = sheduleshare["name"]!!
                                    icon = sheduleshare["icon"]!!.toInt()
                                    sharedhome = sheduleshare["home"]
                                    val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
                                    val currentLocalTime = calendar.getTime()
                                    val date = SimpleDateFormat("Z")
                                    val localTime = date.format(currentLocalTime)
                                    val offsetTimetype = localTime[0]
                                    val offsetTimeHours = localTime.substring(1, 3).toInt()
                                    val offsetTimeMinutes = localTime.substring(3).toInt()
                                    sharedscheduleStartTime = sheduleshare["time"]!!
                                    if ((sharedscheduleStartTime != "Sunset") || (sharedscheduleStartTime != "Sunrise")) {
                                        sharedscheduleStartTime.replace("\\s".toRegex(), "")
                                        if ((sharedscheduleStartTime.takeLast(2) == "PM") || (sharedscheduleStartTime.takeLast(2) == "AM")) {
                                            var timeHours24 = sharedscheduleStartTime.split(":")[0].replace("\\s".toRegex(), "").toInt()
                                            if (sharedscheduleStartTime.takeLast(2) == "PM") {
                                                timeHours24 += 11
                                            }
                                            val data = sharedscheduleStartTime.split(":")[1].replace(" ", "")
                                            val timeMinutes24 = data.replace("A", "").replace("M", "").replace("P", "").toInt()
                                            val totalTime = timeHours24 * 60 + timeMinutes24
                                            val totalOffset = offsetTimeHours * 60 + offsetTimeMinutes
                                            var gmtTime = 0
                                            if (offsetTimetype == '+') {
                                                if (totalTime > totalOffset) {
                                                    gmtTime = totalTime - totalOffset
                                                } else {
                                                    gmtTime = 1440 - totalOffset + totalTime
                                                }
                                            } else {
                                                gmtTime = totalTime + totalOffset
                                                if (gmtTime > 1440) {
                                                    gmtTime = gmtTime - 1440
                                                }
                                            }
                                            sharedscheduleStartTime = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
                                        }


//                                            val values = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                                            //var val1 = values[0].split("\\s".toRegex())
//                                            val timeHours24 = values[0].toInt()
//                                            val timeMinutes24 = values[1].toInt()
//                                            // val timeHours24 =sheduleshare["time"]!!.split(":")[0].toInt()
//                                            //val timeMinutes24 = sheduleshare["time"]!!.split(":")[1].toInt()
//                                            val totalTime = timeHours24 * 60 + timeMinutes24
//                                            val totalOffset = offsetTimeHours * 60 + offsetTimeMinutes
//                                            var timeSet = totalTime + totalOffset
//                                            if (timeSet >= 1440) {
//                                                timeSet = timeSet - 1440
//                                            }
//                                            var currentHour = timeSet / 60
//                                            val currentMinutes = timeSet % 60
//                                            if (currentHour > 11) {
//                                                currentHour = currentHour - 12
//                                                if (currentHour == 0) currentHour = 12
//                                                sharedscheduleStartTime = "$currentHour : $currentMinutes PM"
//                                            } else {
//                                                sharedscheduleStartTime = "$currentHour : $currentMinutes AM"
//                                            }
                                    }
                                    sharedscheduleType = sheduleshare["stype"]!!
                                    sharedscheduleEndTime = sheduleshare["endtime"]!!
                                    val routine = sheduleshare["routine"]!!
                                    val type = object : TypeToken<ArrayList<String>>() {}.type
                                    val gson = Gson()
                                    var routinedata: MutableList<String> = ArrayList()
                                    routinedata = gson.fromJson(routine, type)
                                    val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                                    val map = HashMap<String, String>()
                                    for ((i, name) in routinedata.withIndex()) {
                                        map[days[i]] = name
                                    }
                                    sharedscheduleRoutine = gson.toJson(map)
                                }
                            }
                        }
                        for (r in roomList) {
                            val room = RoomModel()
                            room.name = r
                            for (d in deviceList) {
                                if (d.roomName == r) {
                                    room.deviceList.add(d)
                                }
                            }
                            rooms.add(room)
                        }
                        localSqlSchedule.insertSchedule(mDbSchedule!!, sharedscheduleName, icon, gson.toJson(roomList), gson.toJson(rooms), gson.toJson(sharedscheduleStatus), sharedscheduleStartTime!!, sharedscheduleType!!, sharedscheduleEndTime!!, sharedscheduleRoutine!!, sharedhome!!, sharedscheduleName, "create","")
                    }
                }

                //update homes
                val newHomeList: MutableList<RoomModelJson> = ArrayList()
                var homesFlag = false
                if (rulesTable.homes != null) {
                    if (rulesTable.homes!!.size != 0) {
                        homesFlag = true
                    }
                }
                if (homesFlag) {
                    for (homes in rulesTable.homes!!) {
                        location = homes["location"].toString()
                        longitude = homes["longitude"]!!.toDouble()
                        latitude = homes["latitude"]!!.toDouble()
                        val home = RoomModelJson(homes["name"]!!, homes["type"]!!, homes["access"]!!, homes["bg"]!!, homes["icon"]!!.toInt(), latitude, longitude, location)
                        newHomeList.add(home)
                    }
                    //update shared home
                    var sharedHomeflag = false
                    if (rulesTable.masterHomes != null) {
                        if (rulesTable.masterHomes!!.size != 0) {
                            sharedHomeflag = true
                        }
                    }
                    if (sharedHomeflag) {
                        for (sharedHome in rulesTable.masterHomes!!) {
                            sharedHomelocation = sharedHome["location"].toString()
                            sharedHomelongitude = sharedHome["longitude"]!!.toDouble()
                            shareHomelatitude = sharedHome["latitude"]!!.toDouble()
                            val Sharedhome = RoomModelJson(sharedHome["name"]!!, sharedHome["type"]!!, sharedHome["access"]!!, sharedHome["bg"]!!, sharedHome["icon"]!!.toInt(), shareHomelatitude, sharedHomelongitude, sharedHomelocation)
                            newHomeList.add(Sharedhome)
                        }
                    }
                    localSqlUtils.replaceHome(mDbUtils!!, "home", newHomeList)

                } else {
                    newHomeList.add(RoomModelJson("My Home", "home", "master", "0", 0, 0.0, 0.0, ""))
                    newHomeList.add(RoomModelJson("Bedroom", "room", "default", "0", 1, 0.0, 0.0, ""))
                    newHomeList.add(RoomModelJson("Dining Room", "room", "default", "1", 2, 0.0, 0.0, ""))
                    newHomeList.add(RoomModelJson("Bathroom", "room", "default", "2", 3, 0.0, 0.0, ""))
                    newHomeList.add(RoomModelJson("Living Room", "room", "default", "0", 0, 0.0, 0.0, ""))

                    var sharedHomeflag = false
                    if (rulesTable.masterHomes != null) {
                        if (rulesTable.masterHomes!!.size != 0) {
                            sharedHomeflag = true
                        }
                    }
                    if (sharedHomeflag) {
                        for (sharedHome in rulesTable.masterHomes!!) {
                            sharedHomelocation = sharedHome["location"].toString()
                            sharedHomelongitude = sharedHome["longitude"]!!.toDouble()
                            shareHomelatitude = sharedHome["latitude"]!!.toDouble()
                            val Sharedhome = RoomModelJson(sharedHome["name"]!!, sharedHome["type"]!!, sharedHome["access"]!!, sharedHome["bg"]!!, sharedHome["icon"]!!.toInt(),shareHomelatitude, sharedHomelongitude, sharedHomelocation)
                            newHomeList.add(Sharedhome)
                        }
                    }
                    localSqlUtils.replaceHome(mDbUtils!!, "home", newHomeList)
                    val roomList: MutableList<MutableMap<String, String>> = ArrayList()
                    for (homes in newHomeList) {
                        val map = HashMap<String, String>()
                        map["bg"] = homes.bgUrl
                        map["name"] = homes.name
                        map["type"] = homes.type
                        map["access"] = homes.sharedHome
                        map["icon"] = homes.roomIcon.toString()
                        map["latitude"] = homes.homeLatitude.toString()
                        map["longitude"] = homes.homeLongitude.toString()
                        map["location"] = homes.homeLocation
                        roomList.add(map)
                    }
                    rulesTable.homes = roomList
                    rulesTableDo.saveRulesTable(rulesTable)
                }

            }

            allDeviceList = localSqlDevices.getDeviceTable(mDb!!)
            IpListDevices = localSqlUtils.getIpList(mDbUtils!!, "device")
            for (d in allDeviceList) {
                var ip = IpModel()
                var flag = false
                for (i in IpListDevices) {
                    if (i.name == d.name) {
                        ip = i
                        ip.name = d.name
                        ip.fullDeviceName = i.fullDeviceName
                        ip.thing = d.thing
                        ip.owned = 0
                        ip.failure = intArrayOf(0, 0, 0, 0)
                        ip.uiud = d.uiud
                        ip.aws = false
                        ip.local = true
                        ip.home = d.home
                        ip.room = d.room
                        IpListDevicesNew.add(ip)
                        flag = true
                    }
                }
                if (!flag) {
                    ip.name = d.name
                    ip.thing = d.thing
                    ip.owned = 0
                    ip.uiud = d.uiud
                    ip.aws = false
                    ip.local = false
                    ip.home = d.home
                    ip.room = d.room
                    IpListDevicesNew.add(ip)
                }

            }
            IpListDevices = IP.startDevices(IpListDevicesNew)
            localSqlUtils.replaceIpList(mDbUtils!!, "device", IpListDevices)
            // val sharingSuccess = userDynamoDb.updateSharedDevices(request, this)
            runOnUiThread {
                request.status = 1
                notificationAdapter!!.notifyDataSetChanged()
                progress_bar.visibility = View.GONE
                if (rulesTable.guest!!.size > 0) {
                    SingleBtnDialog.with(this).setHeading(getString(R.string.success_text)).setMessage("${request.sharedHome!!.split("?")[0]} successfully added").show()
                    if (Constant.NOTIFICATION_COUNT > 0) Constant.NOTIFICATION_COUNT = -1

                } else {
                    SingleBtnDialog.with(this).setHeading(getString(R.string.success_text)).setMessage(getString(R.string.user_profile_upload_error)).show()
                }
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("SharedHome", request.sharedHome)
                startActivity(intent)
            }
        }
    }


    override fun onDeclineSelected(request: Notification) {

        progress_bar.visibility = View.VISIBLE
    }

}
