package com.wozart.aura.ui.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.R
import com.wozart.aura.aura.ui.automationlist.OnAutomationListInteractionListener
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.AutomationModel
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.model.tutorial.TutorialModel
import com.wozart.aura.data.sqlLite.*
import com.wozart.aura.entity.model.aura.AuraSceneButton
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.model.aura.AuraSwitchLoad
import com.wozart.aura.entity.model.aura.DeviceTableModel
import com.wozart.aura.entity.service.AwsPubSub
import com.wozart.aura.entity.sql.buttonDevice.ButtonDbHelper
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.auraSense.RemoteListModel
import com.wozart.aura.ui.automationlist.AutomationListFragment
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.createautomation.CreateAutomationActivity
import com.wozart.aura.ui.createautomation.GeoModal
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.customview.spotlight.OnSpotlightStateChangedListener
import com.wozart.aura.ui.customview.spotlight.Spotlight
import com.wozart.aura.ui.customview.spotlight.shape.Circle
import com.wozart.aura.ui.customview.spotlight.shape.RoundedRectangle
import com.wozart.aura.ui.customview.spotlight.target.SimpleTarget
import com.wozart.aura.ui.dashboard.home.HomeFragment
import com.wozart.aura.ui.dashboard.more.MoreFragment
import com.wozart.aura.ui.dashboard.more.MoreFragment.Companion.THEME_SELECTED
import com.wozart.aura.ui.dashboard.room.RoomFragment
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.ui.helper.BottomNavigationViewHelper
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.Constant.Companion.AUTOMATION_TAB
import com.wozart.aura.utilities.Constant.Companion.HOME_TAB
import com.wozart.aura.utilities.Constant.Companion.MORE_TAB
import com.wozart.aura.utilities.Constant.Companion.ROOMS_TAB
import com.wozart.aura.utilities.MyPreferences
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.Dispatcher
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/***
 * Created by Saif on 14-07-2018.
 */

class DashboardActivity : BaseAbstractActivity(), OnAutomationListInteractionListener {


    private var mDbDevice: SQLiteDatabase? = null
    private var mSelectedTab = HOME_TAB
    private var mCurrentTab = -1
    private var lastBackPressTime: Long = 0
    var currentFragment: androidx.fragment.app.Fragment? = null
    private var awsPubSub: AwsPubSub? = null
    private val localSqlDatabase = DeviceTable()
    var IpListDevices: MutableList<IpModel> = ArrayList()
    var IpListDevicesNew: MutableList<IpModel> = ArrayList()
    val localSqlUtils = UtilsTable()
    var mDbUtils: SQLiteDatabase? = null
    private var localSqlButton = AuraButtonTable()
    private var mDbButton: SQLiteDatabase? = null
    var selectedFragment: androidx.fragment.app.Fragment? = null
    private var IP = IpHandler()
    var allDeviceList = java.util.ArrayList<DeviceTableModel>()
    private var mDbScene: SQLiteDatabase? = null
    private var mDbSchedule: SQLiteDatabase? = null
    private val localSqlScene = SceneTable()
    var localSqlSchedule = ScheduleTable()
    var localSQLRemote = AuraSenseTable()
    private var mdbRemote: SQLiteDatabase? = null
    var sharedDataList: MutableList<RoomModelJson> = ArrayList()
    var tab_selected: Int = 0
    var shareHomeGuest: String? = null
    var aura_sense_list: MutableList<RemoteModel> = ArrayList()
    var sceneControllerDeviceList: MutableList<ButtonModel> = ArrayList()
    var location: String = ""
    var latitude = 0.0
    var longitude = 0.0
    var sharedHomelocation: String = ""
    var shareHomelatitude = 0.0
    var sharedHomelongitude = 0.0
    val rulesTableDo: RulesTableHandler by lazy { RulesTableHandler() }
    val gson = Gson()
    var thing_list: MutableList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        checkTheme()
        val bottomNavigationMenuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val v = bottomNavigationMenuView.getChildAt(3)
        val itemView = v as BottomNavigationItemView
        val badge = LayoutInflater.from(this)
                .inflate(R.layout.notification_badge, bottomNavigationMenuView, false)

        val intent = intent
        tab_selected = intent.getIntExtra("TAB_SET", 0)
        shareHomeGuest = intent.getStringExtra("SharedHome")

        if (tab_selected == 0 && !THEME_SELECTED) {
            mSelectedTab = HOME_TAB
        } else if (THEME_SELECTED) {
            mSelectedTab = Constant.MORE_TAB
        } else {
            mSelectedTab = tab_selected
        }
        val dbHelper = DeviceDbHelper(this)
        mDbDevice = dbHelper.writableDatabase
        val dbHelperScedule = ScheduleDbHelper(this)
        mDbSchedule = dbHelperScedule.writableDatabase
        val dbHelperScene = SceneDbHelper(this)
        mDbScene = dbHelperScene.writableDatabase
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase
        val dbButton = ButtonDbHelper(this)
        mDbButton = dbButton.writableDatabase
        val dbRemote = AuraSenseDbHelper(this)
        mdbRemote = dbRemote.writableDatabase
        getPrefdata()
        syncDevices()
        itemView.addView(badge)
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.action_item1 -> {
                    mSelectedTab = HOME_TAB
                    selectedFragment = HomeFragment.newInstance()

                }
                R.id.action_item2 -> {
                    mSelectedTab = ROOMS_TAB
                    selectedFragment = RoomFragment.newInstance()
                }
                R.id.action_item3 -> {
                    selectedFragment = AutomationListFragment.newInstance()
                    mSelectedTab = AUTOMATION_TAB
                }
                R.id.action_item4 -> {
                    selectedFragment = MoreFragment.newInstance()
                    mSelectedTab = MORE_TAB
                }
            }
            if (selectedFragment != null && mCurrentTab != mSelectedTab) {
                mCurrentTab = mSelectedTab
                currentFragment = selectedFragment
                IpListDevices = IP.getIpDevices()
                localSqlUtils.replaceIpList(mDbUtils!!, "device", IpListDevices)
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, selectedFragment!!)
                transaction.commit()
            }
            true
        }
        performTabClick(mSelectedTab)

    }


    private fun checkTheme() {
        when (MyPreferences(this).darkMode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun fetchLoadsShadow(getStatus: Boolean) {
        thing_list.clear()
        thing_list = localSqlDatabase.getThing(mDbDevice!!)
        for (things_name in aura_sense_list) {
            val thing = things_name.scense_thing
            thing_list.add(thing!!)
        }
        if (Constant.IS_FIRST || getStatus) {
            for (x in thing_list) {
                Constant.IS_FIRST = false
                awsPubSub?.subscribeToAws(x)
                val led = (System.currentTimeMillis() / 1000)
                val msg = "{\"state\":{\"desired\": {\"led\": $led}}}"
                awsPubSub?.publishToAws(x, msg)
                awsPubSub?.getShadowData(x)
            }
        }
    }

    private fun initialize() {
        aura_sense_list = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        allDeviceList = localSqlDatabase.getDeviceTable(mDbDevice!!)
        IpListDevices = localSqlUtils.getIpList(mDbUtils!!, "device")
        for (d in allDeviceList) {
            var ip = IpModel()
            if (d.loads.size != 0) {
                if (d.loads.size == 1) {
                    d.loads.let {
                        if (it[0].type == "rgbDevice") {
                            if (d.loads[0].module != -1) ip.module = d.loads[0].module else {
                                ip.module = 7
                                d.loads[0].module = ip.module
                            }
                            ip.fullDeviceName = Constant.DEVICE_LED + "-" + d.name
                        } else if (it[0].type == "tunableDevice") {
                            if (d.loads[0].module != -1) ip.module = d.loads[0].module else {
                                ip.module = 8
                                d.loads[0].module = ip.module
                            }
                            ip.fullDeviceName = Constant.DEVICE_LED + "-" + d.name
                        } else if (it[0].type == "Curtain") {
                            ip.curtainStates = it[0].curtainState
                            ip.curtainState[0] = it[0].curtainState0
                            ip.curtainState[1] = it[0].curtainState1
                            ip.module = 3
                            d.loads[0].module = ip.module
                            ip.fullDeviceName = Constant.DEVICE_WOZART_CURTAIN + "-" + d.name
                        } else {
                            ip.module = 1
                            d.loads[0].module = ip.module
                            ip.fullDeviceName = Constant.DEVICE_WOZART_PLUG + "-" + d.name
                        }
                    }
                } else if (d.loads.size == 2) {
                    if (d.loads[0].module != -1) {
                        ip.module = d.loads[0].module
                        ip.fullDeviceName = if (ip.module == 2 || ip.module == 20) {
                            Constant.DEVICE_SWITCH_MINI + "-" + d.name
                        } else if (ip.module == 12) {
                            Constant.DEVICE_WOZART_GATE + "-" + d.name
                        } else {
                            Constant.DEVICE_WOZART_DIMMER + "-" + d.name
                        }
                    } else {
                        if (d.loads[0].dimmable == true) {
                            ip.module = 11
                            ip.fullDeviceName = Constant.DEVICE_WOZART_DIMMER + "-" + d.name
                        } else {
                            ip.module = 2
                            ip.fullDeviceName = Constant.DEVICE_SWITCH_MINI + "-" + d.name
                        }
                        d.loads[0].module = ip.module
                    }
                } else if (d.loads[0].module == 14) {
                    ip.module = d.loads[0].module
                    ip.fullDeviceName = Constant.DEVICE_WOZART_FAN_CONTROLLER + "-" + d.name
                } else {
                    if (d.loads[2].dimmable!! && d.loads[3].dimmable!!) {
                        if (d.loads[0].module != -1) ip.module = d.loads[0].module else {
                            ip.module = 6
                            d.loads[0].module = ip.module
                        }
                        ip.fullDeviceName = Constant.DEVICE_WOZART_SWITCH_PRO + "-" + d.name
                    } else {
                        ip.module = d.loads.size
                        d.loads[0].module = ip.module
                        ip.fullDeviceName = Constant.DEVICE_WOZART_SWITCH + "-" + d.name
                    }
                }
            }

            var flag = false
            for (i in IpListDevices) {
                if (i.name == d.name && d.loads.size > 0) {
                    ip.aws = i.aws
                    ip.name = d.name
                    i.fullDeviceName = i.fullDeviceName
                    ip = i
                    ip.module = d.loads[0].module
                    ip.thing = d.thing
                    ip.owned = 0
                    ip.failure = intArrayOf(0, 0, 0, 0, 0)
                    ip.uiud = d.uiud
                    ip.local = i.local
                    ip.home = d.home
                    ip.room = d.room
                    IpListDevicesNew.add(ip)
                    flag = true
                    break
                }
            }

            if (!flag) {
                ip.name = d.name
                ip.thing = d.thing
                ip.owned = 0
                ip.module = ip.module
                ip.failure = intArrayOf(0, 0, 0, 0, 0)
                ip.uiud = d.uiud
                ip.aws = false
                ip.local = false
                ip.home = d.home
                ip.room = d.room
                IpListDevicesNew.add(ip)
            }
        }

        for (sense in aura_sense_list) {
            var ip = IpModel()
            var flag = false
            for (i in IpListDevices) {
                if (i.name == sense.aura_sence_name) {
                    ip.aws = i.aws
                    ip.name = sense.aura_sence_name
                    ip.module = if (sense.sense_loads.size > 1) 9 else 15
                    ip = i
                    ip.fullDeviceName = if (sense.sense_loads.size > 1) "Wozart Sense" + "-" + sense.aura_sence_name else Constant.DEVICE_UNIVERSAL_IR + "-" + sense.aura_sence_name
                    ip.thing = sense.scense_thing
                    ip.owned = 0
                    ip.uiud = sense.sense_uiud
                    ip.local = i.local
                    ip.home = sense.home
                    ip.room = sense.room
                    IpListDevicesNew.add(ip)
                    flag = true
                    break
                }
            }

            if (!flag) {
                ip.name = sense.aura_sence_name
                ip.thing = sense.scense_thing
                ip.owned = 0
                ip.module = if (sense.sense_loads.size > 1) 9 else 15
                ip.fullDeviceName = if (sense.sense_loads.size > 1) "Wozart Sense" + "-" + sense.aura_sence_name else Constant.DEVICE_UNIVERSAL_IR + "-" + sense.aura_sence_name
                ip.uiud = sense.sense_uiud
                ip.aws = false
                ip.local = false
                ip.home = sense.home
                ip.room = sense.room
                IpListDevicesNew.add(ip)
            }
        }
        IpListDevices = IP.startDevices(IpListDevicesNew)
        localSqlUtils.replaceIpList(mDbUtils!!, "device", IpListDevices)
    }

    private fun getPrefdata() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val userName = prefs.getString("USERNAME", "NO USER")
        val email = prefs.getString("EMAIL", "NO USER")
        val userId = prefs.getString("ID_", "NO USER")
        val home = prefs.getString("HOME", "NO HOME")
        val verified = prefs.getString("VERIFIED_EMAIL", "NULL")
        prefs.getString("TOKEN", "")?.let { Log.d("Token_data", it) }
        Constant.EMAIL = email
        Constant.IDENTITY_ID = userId
        Constant.USERNAME = userName
        Constant.ACCOUNT_VERIFIED = verified
        if (shareHomeGuest != null) {
            Constant.HOME = shareHomeGuest
        } else {
            Constant.HOME = home
        }
    }

    var mConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
            AwsPubSub.mBounded = false
            awsPubSub = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            AwsPubSub.mBounded = true
            val mLocalBinder = service as AwsPubSub.LocalAwsBinder
            awsPubSub = mLocalBinder.getServerInstance()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!AwsPubSub.mBounded) {
            val mIntent = Intent(applicationContext, AwsPubSub::class.java)
            bindService(mIntent, mConnection, Context.BIND_WAIVE_PRIORITY)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
                onAwsConnectionEstablished, IntentFilter("aws_connected"))
    }

    private val onAwsConnectionEstablished = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val shadow = intent?.getStringExtra("data")
            when (shadow) {
                "Connected" -> {
                    //showToast("Connected now free to go")
                    fetchLoadsShadow(getStatus = true)
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        if (!AwsPubSub.mBounded) {
            val mIntent = Intent(applicationContext, AwsPubSub::class.java)
            bindService(mIntent, mConnection, Context.BIND_WAIVE_PRIORITY)
        }else{
            //showToast(getString(R.string.please_success_connecting_aws))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        IpListDevices = IP.getIpDevices()
        localSqlUtils.replaceIpList(mDbUtils!!, "device", IpListDevices)
        if (AwsPubSub.mBounded) {
            unbindService(mConnection)
            AwsPubSub.mBounded = false
        }
    }


    fun pusblishDataToShadow(thing: String, data: String) {
        if (!AwsPubSub.isAwsConnected) {
            //showToast(getString(R.string.please_wait_connecting_aws))
            return
        }
        if (awsPubSub == null) {
            awsPubSub = AwsPubSub().LocalAwsBinder().getServerInstance()
        }
        awsPubSub?.apply {
            publishToAws(thing, data)
            getShadowData(thing)
        }
    }


    private fun syncDevices() {
        GlobalScope.launch {
            val homeData = localSqlUtils.getHomeData(mDbUtils!!, "home")
            if ((false) or (homeData.size == 0)) {
                aura_sense_list.clear()
                syncCloud()
            } else {
                initialize()
                syncLocal()
            }
        }
    }


    private fun getTargets(tutorialList: ArrayList<TutorialModel>, activity: Activity): ArrayList<SimpleTarget> {
        val list = arrayListOf<SimpleTarget>()
        for (data in tutorialList) {
            val target = SimpleTarget.Builder(activity)
                    .setPoint(data.x!!, data.y!!)
                    .setDuration(500L)
                    .setShape(if (data.isCircle) Circle(data.radius) else RoundedRectangle(data.height, data.width, data.radius))
                    .setTitle(data.title)
                    .setDescription(data.message)
                    .setOverlayPoint(100f, data.overLayY)
                    .build()
            list.add(target)
        }

        return list
    }

    private fun startTutorials() {

        val views = arrayListOf<View>()
        views.add(findViewById(R.id.action_item1))
        views.add(findViewById(R.id.action_item2))
        views.add(findViewById(R.id.action_item3))
        views.add(findViewById(R.id.action_item4))

        Spotlight.with(this)
                .setAnimation(OvershootInterpolator())
                .setOverlayColor(R.color.transparent_black)
                .setTargets(getTargets(Utils.getDashboardTutorial(views), this))
                .showSkipButton(true)
                .setSkipText(getString(R.string.skip))
                .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                    override fun onStarted() {

                    }

                    override fun onEnded() {

                    }
                })
                .setClosedOnTouchedOutside(true)
                .start()
    }

    @SuppressLint("SimpleDateFormat")
    private fun syncCloud() {
        val newHomeList: MutableList<RoomModelJson> = ArrayList()//update shared home//extract all room list//update shared home//extract all room list
       // withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            CoroutineScope(Dispatchers.IO).launch {
            val rulesTable = rulesTableDo.getRulesTable()
            if (rulesTable.userId != "ERROR") {
                //updating devices
                var deviceFlag = false
                if (rulesTable.devices != null) {
                    if (rulesTable.devices!!.size != 0) {
                        deviceFlag = true
                    }
                }
                if (deviceFlag) {
                    for (d in rulesTable.devices!!) {
                        var senseDeviceExist = false
                        val allLoads = rulesTable.loads
                        val home = d["home"]
                        val room = d["room"]
                        val uiud = d["uiud"]
                        val name = d["name"]
                        val thing = d["thing"]
                        val mdl: Int = d["mdl"]?.toInt() ?: -1
                        if (d["deviceType"] == "IR_device") {
                            senseDeviceExist = true
                            val senseModel = RemoteModel()
                            senseModel.home = home
                            senseModel.room = room
                            senseModel.sense_uiud = uiud
                            senseModel.aura_sence_name = name
                            senseModel.senseMacId = d["id"]
                            senseModel.scense_thing = thing
                            senseModel.sense_loads = AuraSenseConfigure().universalSenseRemoteIr()
                            senseModel.aura_sense_full_name = Constant.DEVICE_UNIVERSAL_IR + "-" + senseModel.aura_sence_name
                            aura_sense_list.add(senseModel)

                            localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", aura_sense_list)
                        }
                        if (!senseDeviceExist) {
                            val localLoads: MutableList<AuraSwitchLoad> = ArrayList()
                            for (loads in allLoads!!) {
                                var flag = false
                                for (load in loads) {
                                    if ((load["type"] == "device" && load["device"] == d["id"] && load["type"] != "IR_device")) {
                                        flag = true
                                        break
                                    }
                                }
                                if (flag) {
                                    for (load in loads) {
                                        if (load["type"] != "device") {
                                            val localLoad = AuraSwitchLoad()
                                            localLoad.index = load["index"]!!.toInt()
                                            localLoad.icon = load["icon"]!!.toInt()
                                            localLoad.name = load["name"]
                                            localLoad.type = load["type"]
                                            localLoad.loadType = localLoad.getLoadType(mdl, localLoad.index!!)
                                            if (localLoad.type == "Curtain") {
                                                localLoad.curtainState = load["curtainSetState"].toString()
                                                localLoad.curtainState0 = load["curtainStatus0"]!!.toInt()
                                                localLoad.curtainState1 = load["curtainStatus1"]!!.toInt()
                                            }
                                            localLoad.favourite = load["favourite"].toBoolean()
                                            localLoad.dimmable = load["dimmable"].toBoolean()
                                            localLoad.module = load["mdl"]?.toInt() ?: mdl
                                            localLoads.add(localLoad)
                                        }
                                    }
                                    break
                                }
                            }
                            localSqlDatabase.insertDevice(mDbDevice!!, home!!, room!!, uiud!!, name!!, gson.toJson(localLoads), thing!!)
                        }
                    }
                }
                val allDevices = localSqlDatabase.getDeviceTable(mDbDevice!!)
                //updating Sharing devices
                var masterDeviceflag = false
                if (rulesTable.masterDevices != null) {
                    if (rulesTable.masterDevices!!.size != 0) {
                        masterDeviceflag = true
                    }
                }
                if (masterDeviceflag) {
                    for (shareDevice in rulesTable.masterDevices!!) {
                        var senseDeviceExist = false
                        val shareLoads = rulesTable.masterLoads
                        val sharedHome = shareDevice["home"]
                        val sharedroom = shareDevice["room"]
                        val uiud = shareDevice["uiud"]
                        val sharedname = shareDevice["name"]
                        val sharedthing = shareDevice["thing"]
                        val mdl: Int = shareDevice["mdl"]?.toInt() ?: -1
                        if (shareDevice["deviceType"] == "IR_device") {
                            senseDeviceExist = true
                            val senseModel = RemoteModel()
                            senseModel.home = sharedHome
                            senseModel.room = sharedroom
                            senseModel.sense_uiud = uiud
                            senseModel.aura_sence_name = sharedname
                            senseModel.senseMacId = shareDevice["id"]
                            senseModel.scense_thing = sharedthing
                            senseModel.sense_loads = AuraSenseConfigure().universalSenseRemoteIr()
                            senseModel.aura_sense_full_name = Constant.DEVICE_UNIVERSAL_IR + "-" + senseModel.aura_sence_name
                            aura_sense_list.add(senseModel)

                            localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", aura_sense_list)
                        }
                        if (!senseDeviceExist) {
                            val localLoads: MutableList<AuraSwitchLoad> = ArrayList()
                            for (sharedloads in shareLoads!!) {
                                var flag = false
                                for (load in sharedloads) {
                                    if (load["type"] == "device" && load["device"] == shareDevice["id"] && load["type"] != "IR_device") {
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
                                            localLoad.loadType = localLoad.getLoadType(mdl, localLoad.index!!)
                                            if (localLoad.type == "Curtain") {
                                                localLoad.curtainState = load["curtainSetState"].toString()
                                                localLoad.curtainState0 = load["curtainStatus0"]!!.toInt()
                                                localLoad.curtainState1 = load["curtainStatus1"]!!.toInt()
                                            }
                                            localLoad.favourite = load["favourite"].toBoolean()
                                            localLoad.dimmable = load["dimmable"].toBoolean()
                                            localLoad.module = shareDevice["mdl"]?.toInt()
                                                    ?: mdl
                                            localLoads.add(localLoad)
                                        }
                                    }
                                    break
                                }
                            }
                            localSqlDatabase.insertDevice(mDbDevice!!, sharedHome!!, sharedroom!!, uiud!!, sharedname!!, gson.toJson(localLoads), sharedthing!!)
                        }
                    }
                }
                val allsharedDevice = localSqlDatabase.getDeviceTable(mDbDevice!!)

                //updating scenes
                var sceneFlag = false
                if (rulesTable.scenes != null) {
                    if (rulesTable.scenes!!.size != 0) {
                        sceneFlag = true
                    }
                }
                if (sceneFlag) {
                    val allScenes = rulesTable.scenes
                    for (scenes in allScenes!!) {
                        val rooms: ArrayList<RoomModel> = ArrayList()
                        val keys = scenes.keys
                        var pair = ""
                        for (k in keys) {
                            pair = k
                        }
                        val scene = scenes[pair]
                        var sceneName = ""
                        var icon = 0
                        var home: String? = null

                        //extract all room list
                        val roomList: MutableList<String> = ArrayList()
                        val deviceList: MutableList<Device> = ArrayList()
                        val sceneRemote: MutableList<RemoteIconModel> = ArrayList()
                        for (s in scene!!) {
                            if (s["type"] != "scene" && s["type"] != "remote") {
                                val device = Device(0, s["state"]!!.toBoolean(), false, s["level"]!!.toInt(), s["deviceName"]!!, "room", s["deviceName"]!!, s["index"]!!.toInt(), false)
                                for (d in allDevices) {
                                    if (d.loads.size != 0) {
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

                                            if (s["deviceType"] == "rgbDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.hueValue = s["hue"]?.toInt() ?: 100
                                                device.saturationValue = s["saturation"]?.toInt()
                                                        ?: 100
                                                device.checkType = s["deviceType"].toString()
                                            } else if (s["deviceType"] == "tunableDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.tempValue = d.loads[device.index].tempLight
                                                device.checkType = s["deviceType"].toString()
                                            } else if (s["deviceType"] == "Curtain") {
                                                device.curtainState = s["curtainStatus"].toString()
                                                device.curtainState0 = s["curtainStatus0"]!!.toInt()
                                                device.curtainState1 = s["curtainStatus1"]!!.toInt()
                                                device.checkType = s["deviceType"].toString()
                                            }
                                            deviceList.add(device)
                                            break
                                        }
                                    }

                                }
                                if (s["type"] == "Automation") {
                                    val auromationList = localSqlSchedule.getAutomationScene(mDbSchedule!!, Constant.HOME!!)
                                    for (schedule in auromationList) {
                                        if (s["deviceName"].toString() == schedule.name) {
                                            device.isTurnOn = s["state"].toBoolean()
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
                                sceneName = s["name"]!!
                                icon = s["icon"]!!.toInt()
                                home = s["home"]!!
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

                        localSqlScene.insertScene(mDbScene!!, sceneName, gson.toJson(rooms), gson.toJson(roomList), home!!, icon, sceneName, "create", "")
                    }
                }

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
                        val roomList: MutableList<String> = ArrayList()
                        val deviceList: MutableList<Device> = ArrayList()
                        val sceneRemote: MutableList<RemoteIconModel> = ArrayList()
                        for (s in sharedscene!!) {
                            if (s["type"] != "scene" && s["type"] != "remote") {
                                val device = Device(0, s["state"]!!.toBoolean(), false, s["level"]!!.toInt(), s["deviceName"]!!, "room", s["deviceName"]!!, s["index"]!!.toInt(), false)
                                for (d in allsharedDevice) {
                                    if (d.loads.size != 0) {
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
                                            if (s["deviceType"] == "rgbDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.hueValue = s["hue"]?.toInt() ?: 100
                                                device.saturationValue = s["saturation"]?.toInt()
                                                        ?: 100
                                            } else if (s["deviceType"] == "tunableDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.tempValue = d.loads[device.index].tempLight
                                            } else if (s["deviceType"] == "Curtain") {
                                                device.curtainState = s["curtainStatus"].toString()
                                                device.curtainState0 = s["curtainStatus0"]!!.toInt()
                                                device.curtainState1 = s["curtainStatus1"]!!.toInt()
                                            }
                                            device.checkType = s["deviceType"] ?: ""
                                            deviceList.add(device)
                                            break
                                        }
                                    }

                                }
                                if (s["type"] == "Automation") {
                                    val auromationList = localSqlSchedule.getAutomationScene(mDbSchedule!!, Constant.HOME!!)
                                    for (schedule in auromationList) {
                                        if (s["deviceName"].toString() == schedule.name) {
                                            device.isTurnOn = s["state"].toBoolean()
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
                        localSqlScene.insertScene(mDbScene!!, sharedsceneName, gson.toJson(rooms), gson.toJson(roomList), sharedhome!!, icon, sharedsceneName, "create", "")
                    }
                }

                //update schedules
                var scheduleFlag = false
                if (rulesTable.schedules != null) {
                    if (rulesTable.schedules!!.size != 0) {
                        scheduleFlag = true
                    }
                }
                if (scheduleFlag) {
                    val allSchedules = rulesTable.schedules
                    for (schedules in allSchedules!!) {
                        val rooms: ArrayList<RoomModel> = ArrayList()
                        val keys = schedules.keys
                        var pair = ""
                        for (k in keys) {
                            pair = k
                        }
                        val schedule = schedules[pair]
                        var scheduleName = ""
                        var icon = 0
                        var home: String? = null
                        val scheduleStatus: MutableList<GeoModal> = ArrayList()
                        val geoData = GeoModal()
                        var scheduleStartTime = ""
                        var scheduleType = ""
                        var scheduleEndTime = ""
                        var scheduleRoutine = ""
                        val roomList: MutableList<String> = ArrayList()
                        val deviceList: MutableList<Device> = ArrayList()
                        val scheduleRemote: MutableList<RemoteIconModel> = ArrayList()
                        for (s in schedule!!) {
                            if (s["type"] != "schedule" && s["type"] != "remote") {
                                val device = Device(0, s["state"]!!.toBoolean(), false, s["level"]!!.toInt(), s["deviceName"]!!, "room", s["deviceName"]!!, s["index"]!!.toInt(), false)
                                for (d in allDevices) {
                                    if (d.loads.size != 0) {
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
                                            if (s["deviceType"] == "rgbDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.hueValue = s["hue"]?.toInt() ?: 100
                                                device.saturationValue = s["saturation"]?.toInt()
                                                        ?: 100
                                            } else if (s["deviceType"] == "tunableDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.tempValue = d.loads[device.index].tempLight
                                            } else if (s["deviceType"] == "Curtain") {
                                                device.curtainState = s["curtainStatus"].toString()
                                                device.curtainState0 = s["curtainStatus0"]!!.toInt()
                                                device.curtainState1 = s["curtainStatus1"]!!.toInt()
                                            }
                                            device.checkType = s["deviceType"] ?: ""
                                            deviceList.add(device)
                                            break
                                        }
                                    }

                                }
                                if (s["type"] == "Scene") {
                                    val scene = localSqlScene.getAllScenes(mDbScene!!, Constant.HOME!!)
                                    for (s_ in scene) {
                                        if (s["deviceName"] == s_.name) {
                                            device.roomName = s_.roomName.toString()
                                            device.index = 0
                                            device.type = s_.icon
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
                            } else if (s["type"] == "remote") {
                                val remote = RemoteIconModel()
                                remote.remoteButtonName = s["btnName"]
                                remote.remoteModel = s["model"]
                                remote.channelNumber = s["channel"].toString()
                                remote.btnFavourite = s["favourite"].toBoolean()
                                remote.name = s["senseName"]
                                scheduleRemote.add(remote)
                            } else {
                                scheduleType = s["stype"].toString()
                                if (scheduleType == "geo") {
                                    geoData.newGeoRadius = s["radius"]!!.toFloat()
                                    geoData.newGeoLat = s["latitude"]!!.toDouble()
                                    geoData.newGeolong = s["longitude"]!!.toDouble()
                                    if (s["automationEnable"] != null) {
                                        geoData.AutomationEnable = s["automationEnable"].toBoolean()
                                    } else {
                                        geoData.AutomationEnable = true
                                    }
                                    if (s["turnOff"] != null) {
                                        geoData.turnOff = s["turnOff"]
                                    } else {
                                        geoData.turnOff = "Never"
                                    }
                                    geoData.triggerType = s["triggeringType"]
                                    geoData.triggerWhen = s["triggerwhen"]
                                    geoData.triggerSpecificStartTime = s["specifictime"]
                                    geoData.triggerSpecificEndTime = s["specificend"]
                                    scheduleStatus.add(geoData)
                                    scheduleName = s["name"].toString()
                                    icon = s["icon"]?.toInt() ?: 0
                                    home = s["home"]
                                    scheduleType = s["stype"].toString()
                                } else {
                                    geoData.AutomationEnable = s["automationEnable"].toBoolean()
                                    if (s["turnOff"] != null) {
                                        geoData.turnOff = s["turnOff"]
                                    } else {
                                        geoData.turnOff = "Never"
                                    }
                                    scheduleStatus.add(geoData)
                                    scheduleName = s["name"]!!
                                    icon = s["icon"]?.toInt() ?: 0
                                    home = s["home"]
                                    scheduleType = s["stype"].toString()
                                    val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
                                    val currentLocalTime = calendar.getTime()
                                    val date = SimpleDateFormat("Z")
                                    val localTime = date.format(currentLocalTime)
                                    val offsetTimetype = localTime[0]
                                    val offsetTimeHours = localTime.substring(1, 3).toInt()
                                    val offsetTimeMinutes = localTime.substring(3).toInt()
                                    val time = s["time"].toString()
                                    if ((time != "Sunset") || (time != "Sunrise")) {
                                        time.replace("\\s".toRegex(), "")
                                        if ((time.takeLast(2) == "PM") || (time.takeLast(2) == "AM")) {
                                            var timeHours24 = time.split(":")[0].replace("\\s".toRegex(), "").toInt()
                                            if (time.takeLast(2) == "PM") {
                                                timeHours24 += 11
                                            }
                                            val data = time.split(":")[1].replace(" ", "")
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
                                                if (gmtTime > 1440) gmtTime -= 1440
                                            }
                                            scheduleStartTime = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
                                        } else {
                                            scheduleStartTime = time
                                        }
                                    } else {
                                        scheduleStartTime = time
                                    }
                                    scheduleEndTime = s["endtime"].toString()
                                    val routine = s["routine"].toString()
                                    val type = object : TypeToken<ArrayList<String>>() {}.type
                                    var routinedata: MutableList<String> = ArrayList()
                                    routinedata = Gson().fromJson(routine, type)
                                    val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

                                    val map = HashMap<String, String>()
                                    for ((i, name) in routinedata.withIndex()) {
                                        map[days[i]] = name
                                    }
                                    scheduleRoutine = gson.toJson(map)
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
                        localSqlSchedule.insertSchedule(mDbSchedule!!, scheduleName, icon, gson.toJson(roomList), gson.toJson(rooms), gson.toJson(scheduleStatus), scheduleStartTime!!, scheduleType!!, scheduleEndTime!!, scheduleRoutine!!, home!!, scheduleName, "create", gson.toJson(scheduleRemote))
                    }
                }

                /*
                *Update Sense
                 */
                var senseFlag = false
                if (rulesTable.sense_motion != null) {
                    if (rulesTable.sense_motion!!.size != 0) {
                        senseFlag = true
                    }
                }
                if (senseFlag) {
                    val allSense = rulesTable.sense_motion
                    for (sense in allSense!!) {
                        val rooms: ArrayList<RoomModel> = ArrayList()
                        val keys = sense.keys
                        var pair = ""
                        for (k in keys) {
                            pair = k
                        }
                        val senses = sense[pair]
                        var home = ""
                        val scheduleStatus: MutableList<GeoModal> = ArrayList()
                        val senseData: MutableList<AuraSenseConfigure> = ArrayList()
                        val geoData = GeoModal()
                        var senseStartTime = ""
                        var senseEndTime = ""
                        var triggerWhen: String? = null
                        var device: Device? = null
                        val roomList: MutableList<String> = ArrayList()
                        val deviceList: MutableList<Device> = ArrayList()
                        for (s in senses!!) {
                            if (s["type"] == "device") {
                                device = Device(0, s["state"]!!.toBoolean(), false, s["level"]!!.toInt(), s["deviceName"]!!, "room", s["deviceName"]!!, s["index"]!!.toInt(), false)
                                for (d in allDevices) {
                                    if (d.loads.size != 0) {
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
                                            if (s["deviceType"] == "rgbDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.hueValue = s["hue"]?.toInt() ?: 100
                                                device.saturationValue = s["saturation"]?.toInt()
                                                        ?: 100
                                            } else if (s["deviceType"] == "tunableDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.tempValue = d.loads[device.index].tempLight
                                            } else if (s["deviceType"] == "Curtain") {
                                                device.curtainState = s["curtainStatus"].toString()
                                                device.curtainState0 = s["curtainStatus0"]!!.toInt()
                                                device.curtainState1 = s["curtainStatus1"]!!.toInt()
                                            }
                                            device.checkType = s["deviceType"] ?: ""
                                            deviceList.add(device)
                                            break
                                        }
                                    }

                                }
                            }
                            if (s["type"] == "Scene") {
                                device = Device(0, s["state"]!!.toBoolean(), false, s["level"]!!.toInt(), s["deviceName"]!!, "room", s["deviceName"]!!, s["index"]!!.toInt(), false)
                                val scene = localSqlScene.getAllScenes(mDbScene!!, Constant.HOME!!)
                                for (s_ in scene) {
                                    if (s["deviceName"] == s_.name) {
                                        device.roomName = s_.roomName.toString()
                                        device.index = 0
                                        device.type = s_.icon
                                        device.isTurnOn = s["state"].toBoolean()
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
                            if (s["type"] == "sensor") {
                                val senses_ = AuraSenseConfigure()
                                senses_.auraSenseName = s["sensorType"]
                                if (senses_.auraSenseName == "Motion") {
                                    senses_.auraSenseIndex = 1
                                } else if (senses_.auraSenseName == "Temperature") {
                                    senses_.auraSenseIndex = 2
                                } else if (senses_.auraSenseName == "Humidity") {
                                    senses_.auraSenseIndex = 3
                                } else if (senses_.auraSenseName == "Light Intensity") {
                                    senses_.auraSenseIndex = 4
                                }
                                senses_.senseDeviceName = s["senseDeviceName"]
                                senses_.senseMacId = s["senseMac"]
                                senses_.senseUiud = s["senseUiud"]
                                senses_.above = s["above"].toBoolean()
                                senses_.below = s["below"].toBoolean()
                                senses_.auraSenseFavorite = s["favourite"].toBoolean()
                                senses_.type = s["type"].toString()
                                senses_.range = s["value"]!!.toInt()
                                senseStartTime = s["startTime"].toString()
                                senseEndTime = s["endTime"].toString()
                                senseData.add(senses_)
                            } else if (s["type"] == "senseDevice") {
                                home = s["home"].toString()
                                geoData.AutomationEnable = s["automationEnable"].toBoolean()
                                geoData.turnOff = s["turnOff"]
                                geoData.triggerWhen = s["triggerWhen"]
                                triggerWhen = s["triggerWhen"]
                                scheduleStatus.add(geoData)
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
                        localSqlSchedule.insertSchedule(mDbSchedule!!, pair, 0, gson.toJson(roomList), gson.toJson(rooms), gson.toJson(scheduleStatus), senseStartTime!!, "motion", senseEndTime!!, gson.toJson(senseData)!!, home!!, pair, "create", "")
                    }
                }

                /*
                Synced shared Universal Sense Device
                 */
                var sharedSenseDeviceFlag = false
                if (rulesTable.masterSenseDevice != null) {
                    if (rulesTable.masterSenseDevice!!.size != 0) {
                        sharedSenseDeviceFlag = true
                    }
                }
                if (sharedSenseDeviceFlag) {
                    for (senseDevice in rulesTable.masterSenseDevice!!) {
                        val senseModel = RemoteModel()
                        senseModel.home = senseDevice["home"]
                        senseModel.room = senseDevice["room"]
                        senseModel.sense_uiud = senseDevice["uiud"]
                        senseModel.aura_sence_name = senseDevice["name"]
                        senseModel.senseMacId = senseDevice["id"]
                        senseModel.scense_thing = senseDevice["thing"]
                        senseModel.sense_loads = AuraSenseConfigure().auraSenseDefault()
                        senseModel.aura_sense_full_name = getString(R.string.aura_sense_title) + "-" + senseModel.aura_sence_name
                        aura_sense_list.add(senseModel)
                    }
                    localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", aura_sense_list)

                }

                /*
                Synced Shared Sense Remote
                 */
                var sharedRemoteExit = false
                if (rulesTable.masterSenseRemote != null) {
                    if (rulesTable.masterSenseRemote!!.size != 0) {
                        sharedRemoteExit = true
                    }
                }
                if (sharedRemoteExit) {
                    for (remote in rulesTable.masterSenseRemote!!) {
                        val keys = remote.keys
                        var pair = ""
                        for (k in keys) {
                            pair = k
                        }
                        val remoteDataList = remote[pair]
                        val remoteData: MutableList<RemoteIconModel> = ArrayList()
                        val favChannelList: MutableList<RemoteIconModel> = ArrayList()
                        val listRemote = RemoteListModel()
                        for (r in remoteDataList!!) {
                            if (r["type"] == "senseDevice") {
                                listRemote.typeAppliances = r["appliances"]
                                listRemote.senseUiud = r["deviceId"]
                                listRemote.auraSenseName = r["deviceName"]
                                listRemote.home = r["home"]
                                listRemote.remoteLocation = r["location"]
                                listRemote.remoteFavourite = r["remoteFav"].toBoolean()
                                listRemote.brandName = r["remoteMake"]
                                listRemote.remoteName = r["remoteName"]
                                listRemote.modelNumber = r["remoteModel"]
                            }
                            if (r["type"] == "remoteButton") {
                                val remoteModel = RemoteIconModel()
                                remoteModel.btnFavourite = r["btnFav"].toBoolean()
                                remoteModel.remoteButtonName = r["btnName"]
                                remoteModel.name = r["deviceName"]
                                remoteModel.brandName = r["remoteMake"]
                                remoteModel.remoteName = r["remoteName"]
                                remoteModel.remoteModel = r["remoteModel"]
                                remoteModel.dType = r["dType"] ?: "PRV"
                                remoteModel.isSelected = true
                                if (!r["cNumber"].isNullOrEmpty()) {
                                    remoteModel.channelNumber = r["cNumber"].toString()
                                    remoteModel.channelShortCut = true
                                }
                                remoteModel.internetChannel = r["internetChannel"].toBoolean()
                                remoteData.add(remoteModel)
                            }
                        }
                        localSQLRemote.insertRemote(mdbRemote!!, listRemote.auraSenseName!!, listRemote.remoteName
                                ?: "", listRemote.brandName!!, listRemote.modelNumber!!, gson.toJson(remoteData), gson.toJson(favChannelList), listRemote.typeAppliances
                                ?: "TV", listRemote.remoteLocation
                                ?: "Living Room", (Constant.HOME
                                ?: PreferenceManager.getDefaultSharedPreferences(this@DashboardActivity).getString("HOME", ""))!!)
                    }

                }

                /*
                Update sense Device
                 */
                var senseDeviceFlag = false
                if (rulesTable.senseDevice != null) {
                    if (rulesTable.senseDevice!!.size != 0) {
                        senseDeviceFlag = true
                    }
                }
                if (senseDeviceFlag) {
                    for (d in rulesTable.senseDevice!!) {
                        val senseModel = RemoteModel()
                        senseModel.home = d["home"]
                        senseModel.room = d["room"]
                        senseModel.sense_uiud = d["uiud"]
                        senseModel.aura_sence_name = d["name"]
                        senseModel.senseMacId = d["id"]
                        senseModel.scense_thing = d["thing"]
                        senseModel.sense_loads = AuraSenseConfigure().auraSenseDefault()
                        senseModel.aura_sense_full_name = getString(R.string.aura_sense_title) + "-" + senseModel.aura_sence_name
                        aura_sense_list.add(senseModel)
                    }
                    localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", aura_sense_list)
                }

                var remoteExit = false
                if (rulesTable.sense_remote != null) {
                    if (rulesTable.sense_remote!!.size != 0) {
                        remoteExit = true
                    }
                }
                if (remoteExit) {
                    for (remote in rulesTable.sense_remote!!) {
                        val keys = remote.keys
                        var pair = ""
                        for (k in keys) {
                            pair = k
                        }
                        val remoteDataList = remote[pair]
                        val remoteData: MutableList<RemoteIconModel> = ArrayList()
                        val favChannelList: MutableList<RemoteIconModel> = ArrayList()
                        val listRemote = RemoteListModel()
                        for (r in remoteDataList!!) {
                            if (r["type"] == "senseDevice") {
                                listRemote.typeAppliances = r["appliances"]
                                listRemote.senseUiud = r["deviceId"]
                                listRemote.auraSenseName = r["deviceName"]
                                listRemote.home = r["home"]
                                listRemote.remoteLocation = r["location"]
                                listRemote.remoteFavourite = r["remoteFav"].toBoolean()
                                listRemote.brandName = r["remoteMake"]
                                listRemote.remoteName = r["remoteName"]
                                listRemote.modelNumber = r["remoteModel"]
                            }
                            if (r["type"] == "remoteButton") {
                                val remoteModel = RemoteIconModel()
                                remoteModel.btnFavourite = r["btnFav"].toBoolean()
                                remoteModel.remoteButtonName = r["btnName"]
                                remoteModel.name = r["deviceName"]
                                remoteModel.brandName = r["remoteMake"]
                                remoteModel.remoteName = r["remoteName"]
                                remoteModel.remoteModel = r["remoteModel"]
                                remoteModel.dType = r["dType"] ?: "PRV"
                                remoteModel.isSelected = true
                                if (!r["cNumber"].isNullOrEmpty()) {
                                    remoteModel.channelNumber = r["cNumber"].toString()
                                    remoteModel.channelShortCut = true
                                }
                                remoteModel.internetChannel = r["internetChannel"].toBoolean()
                                remoteData.add(remoteModel)
                            }
                        }
                        localSQLRemote.insertRemote(mdbRemote!!, listRemote.auraSenseName!!, listRemote.remoteName
                                ?: "", listRemote.brandName!!, listRemote.modelNumber!!, gson.toJson(remoteData), gson.toJson(favChannelList), listRemote.typeAppliances
                                ?: "TV", listRemote.remoteLocation
                                ?: "Living Room", (Constant.HOME
                                ?: PreferenceManager.getDefaultSharedPreferences(this@DashboardActivity).getString("HOME", ""))!!)
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
                        val scheduleRemote: MutableList<RemoteIconModel> = ArrayList()
                        for (sheduleshare in schedule!!) {
                            if (sheduleshare["type"] != "schedule" && sheduleshare["type"] != "remote") {
                                val device = Device(0, sheduleshare["state"].toBoolean(), false, sheduleshare["level"]!!.toInt(), sheduleshare["deviceName"]!!, "room", sheduleshare["deviceName"]!!, sheduleshare["index"]!!.toInt(), false)
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
                                        if (sheduleshare["deviceType"] == "rgbDevice") {
                                            device.dimVal = sheduleshare["level"]?.toInt()
                                                    ?: 100
                                            device.hueValue = sheduleshare["hue"]?.toInt()
                                                    ?: 100
                                            device.saturationValue = sheduleshare["saturation"]?.toInt()
                                                    ?: 100
                                        } else if (sheduleshare["deviceType"] == "tunableDevice") {
                                            device.dimVal = sheduleshare["level"]?.toInt()
                                                    ?: 100
                                            device.tempValue = d.loads[device.index].tempLight
                                        } else if (sheduleshare["deviceType"] == "Curtain") {
                                            device.curtainState = sheduleshare["curtainStatus"].toString()
                                            device.curtainState0 = sheduleshare["curtainStatus0"]!!.toInt()
                                            device.curtainState1 = sheduleshare["curtainStatus1"]!!.toInt()
                                        }
                                        device.checkType = sheduleshare["deviceType"] ?: ""
                                        deviceList.add(device)
                                        break
                                    }
                                }
                                if (sheduleshare["type"] == "Scene") {
                                    val scene = localSqlScene.getAllScenes(mDbScene!!, Constant.HOME!!)
                                    for (s_ in scene) {
                                        if (sheduleshare["deviceName"] == s_.name) {
                                            device.roomName = s_.roomName.toString()
                                            device.index = 0
                                            device.type = s_.icon
                                            device.isTurnOn = sheduleshare["state"].toBoolean()
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
                                        geoData.AutomationEnable = sheduleshare["automationEnable"].toBoolean()
                                    } else {
                                        geoData.AutomationEnable = true
                                    }
                                    geoData.triggerSpecificStartTime = sheduleshare["specifictime"]
                                    geoData.triggerSpecificEndTime = sheduleshare["specificend"]
                                    sharedscheduleStatus.add(geoData)
                                    sharedscheduleName = sheduleshare["name"]!!
                                    icon = sheduleshare["icon"]?.toInt() ?: 0
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
                                    icon = sheduleshare["icon"]?.toInt() ?: 0
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
                                                if (gmtTime > 1440) gmtTime -= 1440
                                            }
                                            sharedscheduleStartTime = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
                                        }
                                    }
                                    sharedscheduleType = sheduleshare["stype"]!!
                                    sharedscheduleEndTime = sheduleshare["endtime"]!!
                                    val routine = sheduleshare["routine"]!!
                                    val type = object : TypeToken<ArrayList<String>>() {}.type
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
                        localSqlSchedule.insertSchedule(mDbSchedule!!, sharedscheduleName, icon, gson.toJson(roomList), gson.toJson(rooms), gson.toJson(sharedscheduleStatus), sharedscheduleStartTime!!, sharedscheduleType!!, sharedscheduleEndTime!!, sharedscheduleRoutine!!, sharedhome!!, sharedscheduleName, "create", "")

                    }
                }

                /*
                Update Scene Controller Device
                 */
                var sceneControlerDevice = false
                if (rulesTable.buttonDevice != null) {
                    if (rulesTable.buttonDevice!!.size != 0) {
                        sceneControlerDevice = true
                    }
                }
                if (sceneControlerDevice) {
                    sceneControllerDeviceList.clear()
                    for (d in rulesTable.buttonDevice!!) {
                        val button = ButtonModel()
                        val buttonModel = AuraSceneButton()
                        button.buttonList = buttonModel.buttonData()
                        button.home = d["home"]
                        button.room = d["room"]
                        button.unicastAddress = d["id"]
                        button.auraButtonName = d["name"]
                        button.senseMacId = d["id"]
                        button.thing = d["thing"]
                        sceneControllerDeviceList.add(button)
                    }
                    localSqlUtils.replaceButtonData(mDbUtils!!, "button_device", sceneControllerDeviceList)
                }

                /*
                Update scene controler data
                 */
                var sceneControlerData = false
                if (rulesTable.buttonControl != null) {
                    if (rulesTable.buttonControl!!.size != 0) {
                        sceneControlerData = true
                    }
                }
                if (sceneControlerData) {
                    val allButtonControl = rulesTable.buttonControl
                    for (butonScene in allButtonControl!!) {
                        val rooms: ArrayList<RoomModel> = ArrayList()
                        val keys = butonScene.keys
                        var pair = ""
                        for (k in keys) {
                            pair = k
                        }
                        var device: Device? = null
                        val roomList: MutableList<String> = ArrayList()
                        val deviceList: MutableList<Device> = ArrayList()
                        val buttonModel = ButtonModel()
                        val roomModel = RoomModel()
                        val buttonLoad: MutableList<RoomModel> = arrayListOf()
                        val buttonId = butonScene[pair]
                        for (control in buttonId!!) {
                            if (control["type"] != "buttonControl") {
                                device = Device(0, control["state"]!!.toBoolean(), false, control["level"]!!.toInt(), control["deviceName"]!!, "room", control["deviceName"]!!, control["index"]!!.toInt(), false)
                                for (d in allDevices) {
                                    if (d.name == control["deviceName"]) {
                                        buttonLoad.clear()
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
                                        roomModel.name = device.roomName
                                        roomModel.deviceList = deviceList as ArrayList<Device>
                                        buttonLoad.add(roomModel)
                                        break
                                    }
                                }
                                if (control["type"] == "Scene") {
                                    device = Device(0, control["state"]!!.toBoolean(), false, control["level"]!!.toInt(), control["deviceName"]!!, "room", control["deviceName"]!!, control["index"]!!.toInt(), false)
                                    val scene = localSqlScene.getAllScenes(mDbScene!!, Constant.HOME!!)
                                    for (s_ in scene) {
                                        if (control["deviceName"] == s_.name) {
                                            buttonLoad.clear()
                                            device.roomName = s_.roomName.toString()
                                            device.index = 0
                                            device.type = s_.icon
                                            device.isTurnOn = control["state"].toBoolean()
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
                                            roomModel.name = device.roomName
                                            roomModel.deviceList = deviceList as ArrayList<Device>
                                            buttonLoad.add(roomModel)
                                            break
                                        }
                                    }
                                }
                            } else {
                                buttonModel.buttonId = buttonId.toString()
                                buttonModel.auraButtonName = control["buttonName"]
                                buttonModel.buttonTapName = control["buttonTap"]
                                buttonModel.home = control["home"]
                                buttonModel.room = control["room"]
                                buttonModel.senseName = control["senseDevice"]
                                buttonModel.thing = control["senseThing"]
                                buttonModel.senseUiud = control["senseUiud"]
                                buttonModel.type = control["type"].toString()
                                buttonModel.unicastAddress = control["unicastAddress"]
                            }
                        }
                        localSqlButton.insertButtonDevice(mDbButton!!, buttonModel.auraButtonName!!, buttonModel.buttonId!!, buttonModel.buttonTapName!!, buttonModel.unicastAddress!!, Gson().toJson(buttonLoad), Constant.HOME.toString(), buttonModel.room!!, buttonModel.senseUiud!!, buttonModel.thing.toString(), buttonModel.senseName!!)

                    }
                }

                //update homes
                var homesFlag = false
                if (rulesTable.homes != null) {
                    if (rulesTable.homes!!.size != 0) {
                        homesFlag = true
                    }
                }
                if (homesFlag) {
                    for (homes in rulesTable.homes!!) {
                        location = if (homes["location"].isNullOrEmpty()) "null" else homes["location"].toString()
                        longitude = if (homes["longitude"].isNullOrEmpty()) 0.0 else homes["longitude"]!!.toDouble()
                        latitude = if (homes["latitude"].isNullOrEmpty()) 0.0 else homes["latitude"]!!.toDouble()
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
                            sharedHomelocation = if (sharedHome["location"].isNullOrEmpty()) "null" else sharedHome["location"].toString()
                            sharedHomelongitude = if (sharedHome["longitude"].isNullOrEmpty()) 0.0 else sharedHome["longitude"]!!.toDouble()
                            shareHomelatitude = if (sharedHome["latitude"].isNullOrEmpty()) 0.0 else sharedHome["latitude"]!!.toDouble()
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
                            val Sharedhome = RoomModelJson(sharedHome["name"]!!, sharedHome["type"]!!, sharedHome["access"]!!, sharedHome["bg"]!!, sharedHome["icon"]!!.toInt(), shareHomelatitude, sharedHomelongitude, sharedHomelocation)
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
            } else {
                newHomeList.add(RoomModelJson("My Home", "home", "master", "0", 0, 0.0, 0.0, ""))
                newHomeList.add(RoomModelJson("Bedroom", "room", "default", "0", 1, 0.0, 0.0, ""))
                newHomeList.add(RoomModelJson("Dining Room", "room", "default", "1", 2, 0.0, 0.0, ""))
                newHomeList.add(RoomModelJson("Bathroom", "room", "default", "2", 3, 0.0, 0.0, ""))
                newHomeList.add(RoomModelJson("Living Room", "room", "default", "0", 0, 0.0, 0.0, ""))
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
                if (Constant.ACCOUNT_VERIFIED == "NULL") {
                    rulesTable.verified = "true"
                } else {
                    rulesTable.verified = Constant.ACCOUNT_VERIFIED
                }
                rulesTable.userId = Constant.IDENTITY_ID
                rulesTable.name = Constant.USERNAME
                rulesTable.email = Constant.EMAIL
                rulesTableDo.saveRulesTable(rulesTable)
            }
            CoroutineScope(Dispatchers.Main).launch {
                initialize()
                if (Constant.IS_LOGOUT) {
                    fetchLoadsShadow(true)
                    Constant.IS_LOGOUT = false
                    this@DashboardActivity.recreate()
                }
            }

        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun syncLocal() {
        val homeData = localSqlUtils.getHomeData(mDbUtils!!, "home")
        CoroutineScope(Dispatchers.IO).launch {
            val rulesTable = rulesTableDo.getRulesTable()
            if (rulesTable.userId != "ERROR") {
                val roomList: MutableList<MutableMap<String, String>> = ArrayList()
                val guestHomes: MutableList<String> = ArrayList()
                for (homes in homeData.filter { it.type == "home" && it.sharedHome == "guest" }) {
                    guestHomes.add(homes.name)
                    sharedDataList.add(RoomModelJson(homes.name, "home", "false", "0", 0, homes.homeLatitude, homes.homeLongitude, homes.homeLocation))
                }
                for (homes in homeData.filter { it.type == "room" && it.sharedHome != "default" }) {
                    for (s in sharedDataList) {
                        if (s.sharedHome == homes.sharedHome) {
                            sharedDataList.add(RoomModelJson(homes.name, "room", "false", "0", 0, 0.0, 0.0, ""))
                        }
                    }
                }
                for (homes in homeData) {
                    if (homes.type == "home") {
                        if (homes.sharedHome != "guest") {
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
                    } else {
                        var flag = false
                        for (item in guestHomes) {
                            if (item == homes.sharedHome) {
                                flag = true
                                break
                            }
                        }
                        if (!flag) {
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
                    }
                }
                rulesTable.homes = roomList

                val allScenes = localSqlScene.getSceneTable(mDbScene!!)
                for (d in allScenes) {
                    for (s in sharedDataList) {
                        if ((s.name == d.home) and (s.type == "home")) {
                            sharedDataList.add(RoomModelJson(d.name!!, "scene", "false", d.home!!, 0, 0.0, 0.0, ""))
                            break
                        }
                    }
                }
                val SceneList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
                for (scene in allScenes) {
                    val Scene = HashMap<String, MutableList<MutableMap<String, String>>>()
                    var flag = false
                    for (item in guestHomes) {
                        if (item == scene.home!!) {
                            flag = true
                            break
                        }
                    }
                    if (!flag) {
                        val deviceList: MutableList<MutableMap<String, String>> = ArrayList()
                        val map = HashMap<String, String>()
                        map["type"] = "scene"
                        map["name"] = scene.name!!
                        map["icon"] = scene.icon.toString()
                        map["home"] = scene.home!!
                        map["status"] = "modified"
                        deviceList.add(map)
                        for (room in scene.room) {
                            for (device in room.deviceList) {
                                val mapLoad = HashMap<String, String>()
                                if (device.deviceName == "Automation") {
                                    mapLoad["type"] = device.deviceName
                                    mapLoad["deviceName"] = device.name
                                } else {
                                    mapLoad["type"] = "device"
                                    mapLoad["deviceName"] = device.deviceName
                                }
                                if (device.checkType == "Curtain") {
                                    mapLoad["deviceType"] = device.checkType
                                    mapLoad["curtainSetState"] = device.curtainState
                                    mapLoad["curtainStatus0"] = device.curtainState0.toString()
                                    mapLoad["curtainStatus1"] = device.curtainState1.toString()
                                }
                                if (device.checkType == "rgbDevice") {
                                    mapLoad["deviceType"] = "rgbDevice"
                                    mapLoad["hue"] = device.hueValue.toString()
                                    mapLoad["saturation"] = device.saturationValue.toString()
                                }
                                if (device.checkType == "tunableDevice") {
                                    mapLoad["deviceType"] = "tunableDevice"
                                    mapLoad["temperature"] = device.tempValue.toString()
                                }
                                mapLoad["level"] = device.dimVal.toString()
                                mapLoad["index"] = device.index.toString()
                                mapLoad["state"] = device.isTurnOn.toString()
                                deviceList.add(mapLoad)
                            }
                        }
                        for (remote in scene.remoteData) {
                            val map_remote = HashMap<String, String>()
                            map_remote["type"] = "remote"
                            map_remote["btnName"] = remote.remoteButtonName.toString()
                            map_remote["model"] = remote.remoteModel.toString()
                            map_remote["channel"] = remote.channelNumber.toString()
                            map_remote["favourite"] = remote.btnFavourite.toString()
                            map_remote["senseName"] = remote.name.toString()
                            deviceList.add(map_remote)
                        }
                        Scene[scene.name!!] = deviceList
                        SceneList.add(Scene)
                    }

                }
                rulesTable.scenes = SceneList

                //Update local Schedule to cloud
                val allSchedules = localSqlSchedule.getScheduleTable(mDbSchedule!!)
                for (d in allSchedules) {
                    for (s in sharedDataList) {
                        if ((s.name == d.home) and (s.type == "home")) {
                            sharedDataList.add(RoomModelJson(d.name!!, "schedule", "false", d.home!!, 0, 0.0, 0.0, ""))
                            break
                        }
                    }
                }
                val ScheduleList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
                for (schedule in allSchedules) {
                    val Schedule = HashMap<String, MutableList<MutableMap<String, String>>>()
                    val deviceList: MutableList<MutableMap<String, String>> = ArrayList()
                    var flag = false
                    var motionExistFlag = false
                    if (schedule.type == "motion") {
                        motionExistFlag = true
                    }
                    for (item in guestHomes) {
                        if (item == schedule.home!!) {
                            flag = true
                        }
                    }
                    if ((!flag) && (!motionExistFlag)) {
                        val map = HashMap<String, String>()
                        map["type"] = "schedule"
                        map["stype"] = schedule.type!!
                        map["status"] = "modified"
                        if (map["stype"] == "geo") {
                            for (geoDetail in schedule.property) {
                                map["radius"] = geoDetail.newGeoRadius.toString()
                                map["latitude"] = geoDetail.newGeoLat.toString()
                                map["longitude"] = geoDetail.newGeolong.toString()
                                if (geoDetail.turnOff != null) {
                                    map["turnOff"] = geoDetail.turnOff.toString()
                                } else {
                                    geoDetail.turnOff = "Never"
                                    map["turnOff"] = geoDetail.turnOff.toString()
                                }
                                map["automationEnable"] = geoDetail.AutomationEnable.toString()
                                map["triggeringType"] = geoDetail.triggerType.toString()
                                map["triggerwhen"] = geoDetail.triggerWhen.toString()
                                map["specifictime"] = geoDetail.triggerSpecificStartTime.toString()
                                map["specificend"] = geoDetail.triggerSpecificEndTime.toString()
                            }
                            map["stype"] = schedule.type!!
                            map["name"] = schedule.name!!
                            map["icon"] = schedule.icon.toString()
                            map["home"] = schedule.home!!

                        } else {
                            map["stype"] = schedule.type!!
                            map["name"] = schedule.name!!
                            map["icon"] = schedule.icon.toString()
                            map["endtime"] = schedule.endTime!!
                            map["home"] = schedule.home!!
                            val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
                            val currentLocalTime = calendar.getTime()
                            val date = SimpleDateFormat("Z")
                            val localTime = date.format(currentLocalTime)
                            val offsetTimetype = localTime[0]
                            val offsetTimeHours = localTime.substring(1, 3).toInt()
                            val offsetTimeMinutes = localTime.substring(3).toInt()
                            if ((schedule.time == "Sunset") || (schedule.time == "Sunrise")) {
                                map["time"] = schedule.time.toString()
                            } else {
                                val time = schedule.time!!.replace("\\s".toRegex(), "")
                                if ((time.takeLast(2) == "PM") || (time.takeLast(2) == "AM")) {
                                    var timeHours24 = schedule.time!!.split(":")[0].replace("\\s".toRegex(), "").toInt()
                                    if (time.takeLast(2) == "PM") {
                                        timeHours24 += 11
                                    }
                                    val data = schedule.time!!.split(":")[1].replace(" ", "")
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
                                            gmtTime -= 1440
                                        }
                                    }
                                    map["time"] = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
                                } else {
                                    map["time"] = time
                                }
                            }

                            for (geoDetails in schedule.property) {
                                map["automationEnable"] = geoDetails.AutomationEnable.toString()
                                if (geoDetails.turnOff != null) {
                                    map["turnOff"] = geoDetails.turnOff.toString()
                                } else {
                                    geoDetails.turnOff = "Never"
                                    map["turnOff"] = geoDetails.turnOff.toString()
                                }
                            }
                            val type = object : TypeToken<HashMap<String, Boolean>>() {}.type
                            val gson_ = Gson()
                            val routine: HashMap<String, Boolean> = gson_.fromJson(schedule.routine, type)
                            map["routine"] = "[${routine["Sunday"]},${routine["Monday"]},${routine["Tuesday"]},${routine["Wednesday"]},${routine["Thursday"]},${routine["Friday"]},${routine["Saturday"]}]"
                        }
                        deviceList.add(map)
                        for (room in schedule.load) {
                            for (device in room.deviceList) {
                                val map_ = HashMap<String, String>()
                                if (device.deviceName != "Scene") {
                                    map_["type"] = "device"
                                    map_["deviceName"] = device.deviceName
                                } else {
                                    map_["type"] = "Scene"
                                    map_["deviceName"] = device.name
                                }
                                if (device.checkType == "Curtain") {
                                    map_["deviceType"] = device.checkType
                                    map_["curtainSetState"] = device.curtainState
                                    map_["curtainStatus0"] = device.curtainState0.toString()
                                    map_["curtainStatus1"] = device.curtainState1.toString()
                                }
                                if (device.checkType == "rgbDevice") {
                                    map_["deviceType"] = "rgbDevice"
                                    map_["hue"] = device.hueValue.toString()
                                    map_["saturation"] = device.saturationValue.toString()
                                }
                                if (device.checkType == "tunableDevice") {
                                    map_["deviceType"] = "tunableDevice"
                                    map_["temperature"] = device.tempValue.toString()
                                }
                                map_["level"] = device.dimVal.toString()
                                map_["index"] = device.index.toString()
                                map_["state"] = device.isTurnOn.toString()
                                map["status"] = "modified"
                                deviceList.add(map_)
                            }
                        }
                        for (remote in schedule.remote) {
                            val map_remote = HashMap<String, String>()
                            map_remote["type"] = "remote"
                            map_remote["btnName"] = remote.remoteButtonName.toString()
                            map_remote["model"] = remote.remoteModel.toString()
                            map_remote["channel"] = remote.channelNumber.toString()
                            map_remote["favourite"] = remote.btnFavourite.toString()
                            map_remote["senseName"] = remote.name.toString()
                            map["status"] = "modified"
                            deviceList.add(map_remote)
                        }
                        Schedule[schedule.name!!] = deviceList
                        ScheduleList.add(Schedule)
                    }
                }
                rulesTable.schedules = ScheduleList

                var masterDeviceflag = false
                if (rulesTable.masterDevices != null) {
                    if (rulesTable.masterDevices!!.size != 0) {
                        masterDeviceflag = true
                    }
                }
                if (masterDeviceflag) {
                    for (shareDevice in rulesTable.masterDevices!!) {
                        var senseDeviceExist = false
                        val shareLoads = rulesTable.masterLoads
                        val sharedHome = shareDevice["home"]
                        val sharedroom = shareDevice["room"]
                        val uiud = shareDevice["uiud"]
                        val sharedname = shareDevice["name"]
                        val sharedthing = shareDevice["thing"]
                        val mdl: Int = shareDevice["mdl"]?.toInt() ?: -1
                        if (shareDevice["deviceType"] == "IR_device") {
                            var newDeviceFlag = true
                            val iterate = aura_sense_list.listIterator()
                            while (iterate.hasNext()) {
                                   val oldValue = iterate.next()
                                   if(oldValue.sense_uiud == uiud){
                                       newDeviceFlag = false
                                       break
                                   }
                            }
                            if(newDeviceFlag == true){
                                senseDeviceExist = true
                                val senseModel = RemoteModel()
                                senseModel.home = sharedHome
                                senseModel.room = sharedroom
                                senseModel.sense_uiud = uiud
                                senseModel.aura_sence_name = sharedname
                                senseModel.senseMacId = shareDevice["id"]
                                senseModel.scense_thing = sharedthing
                                senseModel.sense_loads = AuraSenseConfigure().universalSenseRemoteIr()
                                senseModel.aura_sense_full_name = Constant.DEVICE_UNIVERSAL_IR + "-" + senseModel.aura_sence_name
                                aura_sense_list.add(senseModel)

                                localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", aura_sense_list)
                            }


                        }
                        if (!senseDeviceExist) {
                            val localLoads: MutableList<AuraSwitchLoad> = ArrayList()
                            for (sharedloads in shareLoads!!) {
                                var flag = false
                                for (load in sharedloads) {
                                    if (load["type"] == "device" && load["device"] == shareDevice["id"] && load["type"] != "IR_device") {
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
                                            localLoad.loadType = localLoad.getLoadType(mdl, localLoad.index!!)
                                            if (localLoad.type == "Curtain") {
                                                localLoad.curtainState = load["curtainSetState"].toString()
                                                localLoad.curtainState0 = load["curtainStatus0"]!!.toInt()
                                                localLoad.curtainState1 = load["curtainStatus1"]!!.toInt()
                                            }
                                            localLoad.favourite = load["favourite"].toBoolean()
                                            localLoad.dimmable = load["dimmable"].toBoolean()
                                            localLoad.module = shareDevice["mdl"]?.toInt()
                                                    ?: mdl
                                            localLoads.add(localLoad)
                                        }
                                    }
                                    break
                                }
                            }
                            localSqlDatabase.insertDevice(mDbDevice!!, sharedHome!!, sharedroom!!, uiud!!, sharedname!!, gson.toJson(localLoads), sharedthing!!)
                        }
                    }
                }
                val allsharedDevice = localSqlDatabase.getDeviceTable(mDbDevice!!)

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
                        var sharedhome: String? = null

                        //extract all room list
                        val roomList: MutableList<String> = ArrayList()
                        var roomName: String? = null
                        val deviceList: MutableList<Device> = ArrayList()
                        val sceneRemote: MutableList<RemoteIconModel> = ArrayList()
                        for (s in sharedscene!!) {
                            if (s["type"] != "scene" && s["type"] != "remote") {
                                val device = Device(0, s["state"]!!.toBoolean(), false, s["level"]!!.toInt(), s["deviceName"]!!, "room", s["deviceName"]!!, s["index"]!!.toInt(), false)
                                for (d in allsharedDevice) {
                                    if (d.loads.size != 0) {
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
                                                roomList.add(d.room.toString())
                                            }
                                            if (s["deviceType"] == "rgbDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.hueValue = s["hue"]?.toInt() ?: 100
                                                device.saturationValue = s["saturation"]?.toInt()
                                                        ?: 100
                                            } else if (s["deviceType"] == "tunableDevice") {
                                                device.dimVal = s["level"]?.toInt() ?: 100
                                                device.tempValue = d.loads[device.index].tempLight
                                            } else if (s["deviceType"] == "Curtain") {
                                                device.curtainState = s["curtainStatus"].toString()
                                                device.curtainState0 = s["curtainStatus0"]!!.toInt()
                                                device.curtainState1 = s["curtainStatus1"]!!.toInt()
                                            }
                                            device.checkType = s["deviceType"] ?: ""
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
                                remote.btnFavourite = s["favourite"]!!.toBoolean()
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
                            roomName = r
                            for (d in deviceList) {
                                if (d.roomName == r) {
                                    room.deviceList.add(d)
                                }
                            }
                            rooms.add(room)
                        }
                        if (roomName == null) {
                            roomName = ""
                        }
                        localSqlScene.updateScene(mDbScene!!, sharedsceneName, gson.toJson(rooms), roomName!!, sharedhome!!, icon, sharedsceneName)
                        for (s in sharedDataList) {
                            if ((s.name == sharedsceneName) and (s.type == "scene")) {
                                s.sharedHome = "true"
                                break
                            }
                        }
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
                        val scheduleRemote: MutableList<RemoteIconModel> = ArrayList()
                        for (sheduleshare in schedule!!) {
                            if (sheduleshare["type"] != "schedule" && sheduleshare["type"] != "remote") {
                                val device = Device(0, sheduleshare["state"]!!.toBoolean(), false, sheduleshare["level"]!!.toInt(), sheduleshare["deviceName"]!!, "room", sheduleshare["deviceName"]!!, sheduleshare["index"]!!.toInt(), false)
                                for (d in allsharedDevice) {
                                    if (d.loads.size != 0) {
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
                                            if (sheduleshare["deviceType"] == "rgbDevice") {
                                                device.dimVal = sheduleshare["level"]?.toInt()
                                                        ?: 100
                                                device.hueValue = sheduleshare["hue"]?.toInt()
                                                        ?: 100
                                                device.saturationValue = sheduleshare["saturation"]?.toInt()
                                                        ?: 100
                                            } else if (sheduleshare["deviceType"] == "tunableDevice") {
                                                device.dimVal = sheduleshare["level"]?.toInt()
                                                        ?: 100
                                                device.tempValue = d.loads[device.index].tempLight
                                            } else if (sheduleshare["deviceType"] == "Curtain") {
                                                device.curtainState = sheduleshare["curtainStatus"].toString()
                                                device.curtainState0 = sheduleshare["curtainStatus0"]!!.toInt()
                                                device.curtainState1 = sheduleshare["curtainStatus1"]!!.toInt()
                                            }
                                            device.checkType = sheduleshare["deviceType"] ?: ""
                                            deviceList.add(device)
                                            break
                                        }
                                    }

                                }
                                if (sheduleshare["type"] == "Scene") {
                                    val scene = localSqlScene.getAllScenes(mDbScene!!, Constant.HOME!!)
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
                                                roomList.add(s_.roomName.toString())
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
                                remote.btnFavourite = sheduleshare["favourite"]!!.toBoolean()
                                remote.name = sheduleshare["senseName"]
                                scheduleRemote.add(remote)
                            } else {
                                sharedscheduleType = sheduleshare["stype"]!!
                                if (sharedscheduleType == "geo") {
                                    geoData.newGeoRadius = sheduleshare["radius"]!!.toFloat()
                                    geoData.newGeoLat = sheduleshare["latitude"]!!.toDouble()
                                    geoData.newGeolong = sheduleshare["longitude"]!!.toDouble()
                                    if (sheduleshare["automationEnable"] != null) {
                                        geoData.AutomationEnable = sheduleshare["automationEnable"]!!.toBoolean()
                                    } else {
                                        geoData.AutomationEnable = true
                                    }
                                    geoData.triggerType = sheduleshare["triggeringType"]
                                    geoData.triggerWhen = sheduleshare["triggerwhen"]
                                    geoData.triggerSpecificStartTime = sheduleshare["specifictime"]
                                    geoData.triggerSpecificEndTime = sheduleshare["specificend"]
                                    if (sheduleshare["turnOff"] != null) {
                                        geoData.turnOff = sheduleshare["turnOff"]
                                    } else {
                                        geoData.turnOff = "Never"
                                    }
                                    sharedscheduleStatus.add(geoData)
                                    sharedscheduleName = sheduleshare["name"]!!
                                    icon = sheduleshare["icon"]?.toInt() ?: 0
                                    sharedhome = sheduleshare["home"]
                                    sharedscheduleStartTime = ""
                                    sharedscheduleEndTime = ""
                                    sharedscheduleRoutine = ""

                                } else {
                                    sharedscheduleName = sheduleshare["name"]!!
                                    icon = sheduleshare["icon"]?.toInt() ?: 0
                                    geoData.AutomationEnable = sheduleshare["automationEnable"]!!.toBoolean()
                                    if (sheduleshare["turnOff"] != null) {
                                        geoData.turnOff = sheduleshare["turnOff"]
                                    } else {
                                        geoData.turnOff = "Never"
                                    }
                                    sharedscheduleStatus.add(geoData)
                                    sharedhome = sheduleshare["home"]
                                    sharedscheduleStartTime = sheduleshare["time"]
                                    if ((sharedscheduleStartTime != "Sunset") || (sharedscheduleStartTime != "Sunrise")) {
                                        val time = sharedscheduleStartTime!!.replace("\\s".toRegex(), "")
                                        if ((time.takeLast(2) == "PM") || (time.takeLast(2) == "AM")) {
                                            val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
                                            val currentLocalTime = calendar.getTime()
                                            val date = SimpleDateFormat("Z")
                                            val localTime = date.format(currentLocalTime)
                                            val offsetTimetype = localTime[0]
                                            val offsetTimeHours = localTime.substring(1, 3).toInt()
                                            val offsetTimeMinutes = localTime.substring(3).toInt()
                                            var timeHours24 = time.split(":")[0].replace("\\s".toRegex(), "").toInt()
                                            if (time.takeLast(2) == "PM") {
                                                timeHours24 += 11
                                            }
                                            val data = time.split(":")[1].replace(" ", "")
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
                                    }
                                    sharedscheduleEndTime = sheduleshare["endtime"]
                                    val routine = sheduleshare["routine"]!!
                                    val type = object : TypeToken<ArrayList<String>>() {}.type
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
                        localSqlSchedule.updatechedule(mDbSchedule!!, sharedscheduleName, icon, gson.toJson(roomList), gson.toJson(rooms), gson.toJson(sharedscheduleStatus), sharedscheduleStartTime!!, sharedscheduleType!!, sharedscheduleEndTime!!, sharedscheduleRoutine!!, sharedhome!!, sharedscheduleName, gson.toJson(scheduleRemote))

                        for (s in sharedDataList) {
                            if ((s.name == sharedscheduleName) and (s.type == "schedule")) {
                                s.sharedHome = "true"
                                break
                            }
                        }
                    }
                }
                val newHomeList: MutableList<RoomModelJson> = ArrayList()

                for (homes in homeData) {
                    if ((homes.type == "home" || homes.type == "room") && homes.sharedHome != "guest") {
                        newHomeList.add(homes)
                    }
                }

                //newHomeList.addAll(homeData.filter { (it.type == "home" || it.type == "room") && (it.sharedHome != "guest") })

                var sharedHomeflag = false
                if (rulesTable.masterHomes != null) {
                    if (rulesTable.masterHomes!!.size != 0) {
                        sharedHomeflag = true
                    }
                }
                if (sharedHomeflag) {
                    for (sharedHome in rulesTable.masterHomes!!) {
                        sharedHomelocation = if (sharedHome["location"].toString().isEmpty()) "null" else sharedHome["location"].toString()
                        sharedHomelongitude = if (sharedHome["longitude"].isNullOrEmpty()) 0.0 else sharedHome["longitude"]!!.toDouble()
                        shareHomelatitude = if (sharedHome["latitude"].isNullOrEmpty()) 0.0 else sharedHome["latitude"]!!.toDouble()
                        val Sharedhome = RoomModelJson(sharedHome["name"]!!, sharedHome["type"]!!, sharedHome["access"]!!, sharedHome["bg"]!!, sharedHome["icon"]!!.toInt(), shareHomelatitude, sharedHomelongitude, sharedHomelocation)
                        newHomeList.add(Sharedhome)
                    }
                }
                for (s in sharedDataList) {
                    if (s.sharedHome == "false") {
                        if (s.type == "device") {
                            localSqlDatabase.deleteDevice(mDbDevice!!, s.name)
                        } else if (s.type == "scene") {
                            localSqlScene.deleteScene(mDbScene!!, s.name, s.bgUrl)
                        } else if (s.type == "schedule") {
                            localSqlSchedule.deleteAutomationScene(mDbSchedule!!, s.name, s.bgUrl)
                        }
                    }
                }
                var flage = true
                for (n in newHomeList) {
                    if ((n.type == "home") && (n.name == Constant.HOME)) {
                        flage = false
                        break
                    }
                }
                if (flage) {
                    val prefEditor = PreferenceManager.getDefaultSharedPreferences(this@DashboardActivity).edit()
                    prefEditor.putString("HOME", "My Home")
                    prefEditor.apply()
                    Constant.HOME = "My Home"
                }
                localSqlUtils.replaceHome(mDbUtils!!, "home", newHomeList)
                rulesTableDo.saveRulesTable(rulesTable)
            }
        }

    }

    private fun performTabClick(tab: Int) {
        val view: View? = when (tab) {
            HOME_TAB -> bottomNavigationView.findViewById(R.id.action_item1)
            ROOMS_TAB -> bottomNavigationView.findViewById(R.id.action_item2)
            AUTOMATION_TAB -> bottomNavigationView.findViewById(R.id.action_item3)
            MORE_TAB -> bottomNavigationView.findViewById(R.id.action_item4)
            else -> bottomNavigationView.findViewById(R.id.action_item1)
        }
        view?.performClick()
    }

    override fun onBackPressed() {

        if (lastBackPressTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            this.finishAffinity()
        } else {
            Toast.makeText(baseContext, getString(R.string.app_close_toast_text), Toast.LENGTH_SHORT).show()
            lastBackPressTime = System.currentTimeMillis()

        }
    }

    override fun onCreateAutomationBtnClicked() {
        startActivity(Intent(this, CreateAutomationActivity::class.java))
    }

    override fun onDetailsBtnClicked(automation: AutomationModel) {
        val intent = Intent(this, CreateAutomationActivity::class.java)
        startActivity(intent)
    }

    override fun onDeleteBtnClicked(automation: AutomationModel) {
        showToast("Delete clicked ")
    }

}
