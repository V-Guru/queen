package com.wozart.aura.ui.dashboard.home

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
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.warkiz.widget.IndicatorSeekBar
import com.wozart.aura.R
import com.wozart.aura.aura.ui.dashboard.home.Home
import com.wozart.aura.aura.ui.dashboard.listener.OnDeviceOptionsListener
import com.wozart.aura.aura.ui.dashboard.listener.OnOptionsListener
import com.wozart.aura.aura.ui.dashboard.rooms.AddRoomActivity
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.aura.utilities.Utils.getIconDrawable
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.device.SceneHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.*
import com.wozart.aura.entity.model.aura.AuraComplete
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.AuraSwitchLoad
import com.wozart.aura.entity.model.aws.AwsState
import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.entity.network.ConnectTask
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.entity.sql.camera.CameraDbHelper
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.users.UserDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.adapter.CameraDisplayAdapter
import com.wozart.aura.ui.adapter.CameraViewAdapter
import com.wozart.aura.ui.adapter.HomeFavButtonAdapter
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.auraSense.RemoteListAdapter
import com.wozart.aura.ui.auraSense.RemoteListModel
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.createscene.CreateSceneActivity
import com.wozart.aura.ui.curtain.CurtainSetUpActivity
import com.wozart.aura.ui.dashboard.*
import com.wozart.aura.ui.dashboard.listener.OnAdaptiveSelected
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.model.CameraModel
import com.wozart.aura.ui.dashboard.more.ShareAuraActivity
import com.wozart.aura.ui.home.HomeDetailsActivity
import com.wozart.aura.ui.thirdParty.IRDeviceModal
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.Constant.Companion.CREATE_HOME_ROOM
import com.wozart.aura.utilities.Constant.Companion.EDIT_HOME
import com.wozart.aura.utilities.Constant.Companion.SELECTED_HOME
import com.wozart.aura.utilities.Constant.Companion.SHARE_HOME
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.DoubleBtnDialog
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.dialog_configure_edit_dimming.*
import kotlinx.android.synthetic.main.dialog_configure_edit_dimming.btnEdit
import kotlinx.android.synthetic.main.dialog_configure_edit_dimming.iconDevice
import kotlinx.android.synthetic.main.dialog_configure_edit_dimming.tvDialogTitle
import kotlinx.android.synthetic.main.dialog_curtain_control_edit.*
import kotlinx.android.synthetic.main.dialogue_edit_home.*
import kotlinx.android.synthetic.main.dialogue_tunnable_set.*
import kotlinx.android.synthetic.main.fan_dimming_layout.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_refresh_home.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.toast
import kotlin.math.roundToInt


/***
 * Created by Saif on 14-07-2018.
 */

class HomeFragment : Fragment(), OnOptionsListener, OnDeviceOptionsListener,
        ConnectTask.TcpMessageReceiver, EspHandler.OnEspHandlerMessage, View.OnClickListener,
        RemoteListAdapter.OnRemoteDelete, RecyclerItemClicked, OnAdaptiveSelected {

    private val LOG_TAG = HomeFragment::class.java.simpleName
    private var optionType: Int = 0
    private val localSqlDatabase = DeviceTable()
    private val localSqlScene = SceneTable()
    private val localSqlUtils = UtilsTable()
    private var localSQLUser = UserTable()
    private var localSQLCamera = CameraTable()
    private var mdbCamera: SQLiteDatabase? = null
    private var mdbUser: SQLiteDatabase? = null
    private var localSqlRemote = AuraSenseTable()
    private var mdbSense: SQLiteDatabase? = null
    private var mDbScene: SQLiteDatabase? = null
    private var mDb: SQLiteDatabase? = null
    private var mDbUtils: SQLiteDatabase? = null
    private var nsd: Nsd? = null
    private var sceneHandler: SceneHandler = SceneHandler()
    private var jsonHelper: JsonHelper = JsonHelper()
    private lateinit var deviceAdapter: DevicesAdapter
    private lateinit var sceneAdapter: ScenesAdapter
    var cameraAdapter: CameraDisplayAdapter? = null
    private var deviceLoadList: ArrayList<Device>? = null
    private var userHomeType = true
    val scenesList = ArrayList<Scenes>()
    var scenesData = ArrayList<Scene>()
    var allDeviceList = ArrayList<AuraComplete>()
    var IpListDevices: MutableList<IpModel> = ArrayList()
    private var IP = IpHandler()
    lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    var deviceError: Int = -1
    var totalSenseAvailableLocal: MutableList<RemoteModel> = ArrayList()
    var activity: Activity? = null
    var zmoteDeviceList: MutableList<IRDeviceModal> = ArrayList()
    lateinit var remoteListAdapter: RemoteListAdapter
    var remoteFavButton: MutableList<RemoteIconModel> = ArrayList()
    lateinit var btnAdapter: HomeFavButtonAdapter
    var loadPositionClicked: Int = -1
    var loadClicked = false
    var clickedDevice: Device? = null
    var remoteCloudControlData: RemoteIconModel? = null
    var sceneToDelete: Scenes? = null
    var cameraList: MutableList<CameraModel> = ArrayList()
    var cameraViewList: MutableList<CameraModel> = ArrayList()
    var camerViewadapter: CameraViewAdapter? = null
    var espHandler: EspHandler? = null
    var senseAvailableForHome : MutableList<RemoteModel> = arrayListOf()

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }


    override fun onDeviceOptionsClicked(view: View) {
        optionType = 1
        showPopup(view)
    }

    override fun onOptionsClicked(view: View) {
        optionType = 0
        showPopup(view)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favDevicesRv.isNestedScrollingEnabled = false
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        startBrodcastNetwork()
        init()
        swipeRefreshLayout.setOnRefreshListener {
            val ft = parentFragmentManager.beginTransaction()
            ft.detach(this).attach(this).commit()
        }

    }

    private fun startBrodcastNetwork() {
        nsd = Nsd()
        nsd?.getInstance(requireActivity(), "HOME")
        nsdDiscovery()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onTcpServerMessageReceived, IntentFilter("intentKey"))
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onAwsMessageReceived, IntentFilter("AwsShadow"))
        //LocalBroadcastManager.getInstance(requireContext()).registerReceiver(onNetworkChanged, IntentFilter("networkChange"));
    }

//    var connectivityUpdated: Boolean = false
//    private var onNetworkChanged = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val data = intent?.getBooleanExtra("isOnline", false)
//            if (data!!) {
//                Log.d("CONNECTION_CHANGE", connectivityUpdated.toString())
//                connectivityUpdated = true
//            }
//        }
//
//    }

    fun init() {
        remoteFavButton.clear()
        totalSenseAvailableLocal.clear()
        cameraList.clear()
        IpListDevices = IP.getIpDevices()
        val dbScene = SceneDbHelper(requireActivity())
        mDbScene = dbScene.writableDatabase

        val dbHelper = DeviceDbHelper(requireActivity())
        mDb = dbHelper.writableDatabase

        val dbUtils = UtilsDbHelper(requireActivity())
        mDbUtils = dbUtils.writableDatabase

        val remoteDbHelper = AuraSenseDbHelper(requireActivity())
        mdbSense = remoteDbHelper.writableDatabase

        val dbUser = UserDbHelper(requireActivity())
        mdbUser = dbUser.writableDatabase

        val dbCamera = CameraDbHelper(requireActivity())
        mdbCamera = dbCamera.writableDatabase

        weather.text = Common.getStateDayStatus(requireContext())
        homeTitleTv.setOnClickListener(this)
        addAuraSwitchBtn.setOnClickListener(this)
        btnAddDevice.setOnClickListener(this)
        spinnerHomes.setOnClickListener(this)
        home_wozart_logo.setOnClickListener(this)
        btnAdapter = HomeFavButtonAdapter(requireActivity(), this)
        cameraAdapter = CameraDisplayAdapter(this)
        camerViewadapter = CameraViewAdapter(requireActivity(), this)
        rvCameraView.adapter = camerViewadapter
        totalSenseAvailableLocal = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        senseAvailableForHome.addAll(totalSenseAvailableLocal.filter { it.home == Constant.HOME && it.sense_loads.size > 1 })
        remoteFavButton = localSqlRemote.getFavouriteRemote(mdbSense!!, Constant.HOME!!)
        cameraList = localSQLCamera.getAllCameras(mdbCamera!!, Constant.HOME!!)
        navigationView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.delay_up_side))
        if (remoteFavButton.size > 0) {
            tvFavoriteButton.visibility = View.VISIBLE
            rvFavButton.visibility = View.VISIBLE
        } else {
            tvFavoriteButton.visibility = View.GONE
            rvFavButton.visibility = View.GONE
        }
        btnAdapter.setData(remoteFavButton)
        rvFavButton.adapter = btnAdapter
        if (cameraList.size > 0) {
            tvCameraTitle.visibility = View.VISIBLE
            rvCamera.visibility = View.VISIBLE
            rvCamera.adapter = cameraAdapter
            setCamDisplayAdapter()
        }

        if (Constant.HOME!!.length > 10) {
            val ttl = Constant.HOME!!.substring(0, 10)
            homeTitleTv.text = "$ttl.."
        } else {
            homeTitleTv.text = Constant.HOME
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val userId = prefs.getString("ID_", "NO USER")
        val userData = localSQLUser.getUsers(mdbUser!!, userId!!)
        welcome.text = String.format(getString(R.string.welcome), userData.get(0).firstName)
        val listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        userHomeType = true
        addAuraSwitchBtn.visibility = View.VISIBLE
        for (x in listRoom) {
            if (x.type == "home") {
                if (x.name == Constant.HOME) {
                    if (x.sharedHome == "guest") {
                        addAuraSwitchBtn.visibility = View.INVISIBLE
                        userHomeType = false
                    }
                }
            }
        }

        allDeviceList = localSqlDatabase.getAllDevicesScenes(mDb!!, Constant.HOME!!)
        deviceLoadList = ArrayList<Device>()
        for (device in allDeviceList) {
            for (l in IpListDevices) {
                if (device.name == l.name) {
                    for (i in 0 until device.loads.size) {
                        if (device.loads[i].favourite!!) {
                            if (device.loads.size == 1) {
                                l.state[i] = l.auraPlugState[i]
                            } else if (device.loads.size == 2) {
                                l.state[i] = l.twoModuleState[i]
                            }
                            val load = Device(device.loads[i].icon!!, l.state[i], false, l.dim[i], device.loads[i].name!!, device.room!!, device.name, device.loads[i].index!!, device.loads[i].dimmable!!)
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
                                load.curtainState = device.loads[i].curtainState
                                load.curtainState0 = device.loads[i].curtainState0
                                load.curtainState1 = device.loads[i].curtainState1
                                load.checkType = device.loads[i].type.toString()
                            } else if (device.loads[i].type == "rgbDevice") {
                                load.checkType = device.loads[i].type.toString()
                            } else if (device.loads[i].type == "tunableDevice") {
                                load.checkType = device.loads[i].type.toString()
                            }
                            deviceLoadList!!.add(load)
                        }
                    }
                }
            }
        }

        deviceAdapter = DevicesAdapter(deviceLoadList!!, "Any") { device: Device, isLongPressed: Boolean ->
            if (isLongPressed) {
                if (device.fullDeviceName!!.contains(getString(R.string.aura_curtain_product)) || device.checkType == "Curtain") {
                    showDialogCurtain(device, "edit")
                } else if (device.checkType == "rgbDevice") {
                    dialog(device)
                } else if (device.checkType == "tunableDevice") {
                    tunnableDialog(device)
                } else {
                    if (!device.dimmable) {
                        if (userHomeType) {
                            val intent = Intent(context, EditLoadActivity::class.java)
                            intent.putExtra("DEVICE", Gson().toJson(device))
                            intent.putExtra(Constant.ROOM_NAME, Constant.CREATE_ROOM_NAME)
                            startActivity(intent)
                        } else {
                            SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_edit)).show()
                        }
                    } else {
                        if ((device.fullDeviceName!!.contains("AuraSwitch Pro") || device.fullDeviceName!!.contains("Wozart Switch Controller Pro") || device.fullDeviceName!!.contains("Fan")) && device.index == 2) {
                            openFanDimmingDialog(device)
                        } else {
                            openDimmingDialog(device)
                        }
                    }
                }

            } else {
                if (device.checkType == "rgbDevice") {
                    controlRGBTunnable(device, false, "RGB")
                } else if (device.checkType == "tunableDevice") {
                    controlRGBTunnable(device, false, "Tunable")
                } else if (device.fullDeviceName!!.contains(getString(R.string.aura_curtain_product)) || device.checkType == "Curtain") {
                    showDialogCurtain(device, "control")
                } else {
                    controlDevice(device, false)
                }

            }
        }
        favDevicesRv.adapter = deviceAdapter
        favDevicesRv.layoutManager = GridAutoFitLayoutManager(context, resources.getDimensionPixelSize(R.dimen.device_item_size))
        favDevicesRv.setHasFixedSize(true)
        val spacing = resources.getDimensionPixelSize(R.dimen.uniform_half_spacing)
        favDevicesRv.addItemDecoration(GridListSpacingItemDecoration(spacing))
        scenesList.clear()
        scenesData = localSqlScene.getAllScenes(mDbScene!!, Constant.HOME!!)
        for (scene in scenesData) {
            scenesList.add(Scenes(scene.name, scene.icon, scene.room, false))
        }
        sceneAdapter = ScenesAdapter(scenesList, this) { scenes: Scenes, isLongPressed: Boolean ->
            if (isLongPressed) {
                if (userHomeType) {
                    dialogueEditHome(scenes)
                } else {
                    SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_edit)).show()
                }
            } else {
                for (s in scenesData) {
                    if (s.name == scenes.title) {
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
                val sceneList = sceneHandler.convertToSceneModel(scenes.rooms)
                val isSceneOn = !scenes.isOn
                for (scene in sceneList) {
                    for (l in IpListDevices) {
                        if (l.name == scene.device) {
                            val data = jsonHelper.serializeSceneData(scene, l.uiud!!, l.module, isSceneOn)
                            if (l.ip == null) {
                                if (l.thing != null) {
                                    if (context is DashboardActivity) {
                                        (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeSceneDataForAws(scene, isSceneOn, l.module))
                                    }
                                }
                            } else if (l.local) {
                                if (l.fullDeviceName!!.startsWith("Aura Switch")) {
                                    ConnectTask(requireContext(), this, data, l.ip!!, scene.device!!).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                                } else {
                                    sendEspHandler(data, l.ip!!, scene.device!!, "")
                                }
                            } else {
                                if (l.aws) {
                                    if (l.thing != null) {
                                        if (context is DashboardActivity) {
                                            (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeSceneDataForAws(scene, isSceneOn, l.module))
//
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        favScenesRv.adapter = sceneAdapter

        getAllAvailableDeviceLoads(deviceLoadList ?: arrayListOf())

        if (deviceLoadList!!.size == 0 && scenesList.size == 0 && cameraList.size == 0 && remoteFavButton.size == 0) {
            rlEmptyScreen.visibility = View.VISIBLE
            tvFavoriteScenesTitle.visibility = View.GONE
            tvFavoriteDevicesTitle.visibility = View.GONE
        } else {
            rlEmptyScreen.visibility = View.GONE
            tvFavoriteScenesTitle.visibility = View.VISIBLE
            tvFavoriteDevicesTitle.visibility = View.VISIBLE
        }
        navigationView.scrollTo(0, 0)
        updateAppearance()
    }

    private fun getAllAvailableDeviceLoads(deviceLoadList: ArrayList<Device>) {
        for (l in IpListDevices) {
            try {
                deviceLoadList?.find { it.deviceName == l.name }?.let {
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
                if (context is DashboardActivity) {
                    (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeLEDData())
                }
            }
        }
    }

    private fun setCamDisplayAdapter() {
        (rvCamera.adapter as CameraDisplayAdapter).setData(cameraList)
    }

    private fun showDialogCurtain(device: Device, s: String) {
        val dialog = Dialog(requireContext())
        var uiud: String? = null
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_curtain_control_edit)
        dialog.btnEdit.visibility = if (s == "edit") View.VISIBLE else View.GONE
        dialog.setCanceledOnTouchOutside(true)
        dialog.ivCloseCurtain.setOnClickListener {
            controlCurtainAction(device, getString(R.string.close_curtaing))
           // dialog.dismiss()
        }
        dialog.ivOpenCurtain.setOnClickListener {
            controlCurtainAction(device, getString(R.string.open_curtain))
            //dialog.dismiss()
        }
        dialog.ivStop.setOnClickListener {
            controlCurtainAction(device, getString(R.string.stop))
            //dialog.dismiss()
        }
        dialog.btnEdit.setOnClickListener {
            if (userHomeType) {
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
        if (roomsList.isNotEmpty()) {
            for (x in roomsList) {
                val devices = localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, x.name!!)
            }
        }
    }

    private fun dialogueEditHome(scenes: Scenes) {
        val dialogue = Dialog(requireContext())
        dialogue.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogue.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogue.setContentView(R.layout.dialogue_edit_home)
        dialogue.tv_title.text = getString(R.string.edit_scene_delete)
        dialogue.btn_edit.setOnClickListener {
            val intent = Intent(activity, CreateSceneActivity::class.java)
            intent.putExtra("inputSceneType", "edit")
            intent.putExtra("inputSceneName", scenes.title)
            intent.putExtra("inputSceneIconUrl", scenes.iconUrl)
            startActivity(intent)
        }
        dialogue.btn_delete_home.setOnClickListener {
            pbText.text = getString(R.string.updating_data)
            progress_layout.visibility = View.VISIBLE
            val sceneNameOld = scenes.title
            sceneToDelete = scenes
            if (senseAvailableForHome.size > 0) {
                val localSceneDelete = JsonHelper().localDataDelete(sceneNameOld.toString(), "delete", "Scene", senseAvailableForHome[0].sense_uiud)
                for (ip in IpListDevices) {
                    if (ip.name == senseAvailableForHome[0].aura_sence_name) {
                        senseAvailableForHome[0].sense_ip = ip.ip ?: ""
                        break
                    }
                }
                sendEspHandler(localSceneDelete, senseAvailableForHome[0].sense_ip, senseAvailableForHome[0].aura_sence_name!!, "scene_delete")
            } else {
                deleteScene(scenes, Constant.ONLINE_MODE)
            }
            dialogue.dismiss()
        }
        dialogue.show()
    }

    private fun deleteScene(scenes: Scenes, mode: String) {
        val sceneDynamoDb = RulesTableHandler()
        val sceneNameOld = scenes.title
        localSqlScene.deleteScene(mDbScene!!, sceneNameOld.toString(), Constant.HOME!!)
        deleteSceneFromLoads(sceneNameOld.toString())
        AppExecutors().diskIO().execute {
            sceneDynamoDb.deleteUserScene(Constant.IDENTITY_ID!!, Constant.HOME!!, sceneNameOld.toString(), if (senseAvailableForHome.size > 0) senseAvailableForHome[0].senseMacId!! else "", if (senseAvailableForHome.size > 0) senseAvailableForHome[0].scense_thing!! else "", mode)
            activity?.runOnUiThread {
                progress_layout.visibility = View.GONE
                startActivity(activity?.intentFor<DashboardActivity>()?.newTask())
                activity?.toast("Scene Deleted")
            }
        }

    }

    /**
     * Data from the TCP : Synchronous
     */
    override fun onTcpMessageReceived(message: String) {
        AppExecutors().mainThread().execute {
            updateStates(message, "")
            updateAppearance()
        }
    }

    //
    override fun onResume() {
        super.onResume()
        if (nsd == null) {
            nsd = Nsd()
            nsd?.getInstance(requireActivity(), "HOME")
        }
        nsdDiscovery()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onAwsMessageReceived, IntentFilter("AwsShadow"))
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        AppExecutors().mainThread().execute {
            updateStates(decryptedData, name)
            updateAppearance()
        }
    }

    fun updateAppearance() {
        IpListDevices = IP.getIpDevices()
        for (device in deviceLoadList!!) {
            for (ip in IpListDevices) {
                if (device.deviceName == ip.name) {
                    if (ip.condition[device.index] == "fail") {
                        device.status = "off"
                    } else if ((ip.condition[device.index] == "update") and (deviceError == 0)) {
                        device.status = "update"
                        device.devicePresent = true
                    } else if ((ip.condition[device.index] == "update") and (deviceError != 0)) {
                        if (ip.aws) {
                            device.status = "update"
                            device.devicePresent = true
                        } else {
                            device.status = "update"
                            device.devicePresent = true
                        }
                    } else if (ip.condition[device.index] == "ready") {
                        if (ip.local) {
                            device.status = "on"
                        } else {
                            device.status = "cloud"
                        }
                    }
                    if (ip.module == 2 || ip.module == 12 || ip.module == 20) {
                        device.isTurnOn = ip.twoModuleState[device.index]
                        device.dimmable = false
                    } else if (ip.module == 11) {
                        device.isTurnOn = ip.twoModuleState[device.index]
                        device.dimVal = ip.twoModuleDim[device.index]
                    } else if (ip.module == 1) {
                        device.power = ip.auraPlugPower
                        device.isTurnOn = ip.auraPlugState[0]
                        device.dimmable = false
                    } else if (ip.module == 7 || ip.module == 8 || ip.module == 13) {
                        device.isTurnOn = ip.auraPlugState[0]
                        device.dimVal = ip.brightness
                        device.hueValue = ip.hue
                        device.saturationValue = ip.saturation
                        device.tempValue = ip.tempLight
                        device.dimmable = true
                    } else if (ip.module == 3) {
                        device.isTurnOn = false
                        device.dimmable = false
                    } else {
                        device.isTurnOn = ip.state[device.index]
                        device.dimVal = ip.dim[device.index]
                    }
                    break
                }
            }
        }
        deviceAdapter.notifyDataSetChanged()

        for (scene in scenesList) {
            var sceneTurnedState = true
            for (sRoom in scene.rooms) {
                for (sDevice in sRoom.deviceList) {
                    if (sDevice.deviceName != "Automation") {
                        for (ip in IpListDevices) {
                            if (sDevice.deviceName == ip.name) {
                                if (ip.module == 2 || ip.module == 12 || ip.module == 11 || ip.module == 20) {
                                    if (sDevice.isTurnOn != ip.twoModuleState[sDevice.index]) {
                                        sceneTurnedState = false
                                        break
                                    }
                                } else if (ip.module == 1) {
                                    if (sDevice.isTurnOn != ip.auraPlugState[0]) {
                                        sceneTurnedState = false
                                        break
                                    }
                                } else if (ip.module == 7 || ip.module == 8 || ip.module == 13) {
                                    if (ip.auraPlugState[0]) {
                                        if (sDevice.isTurnOn != ip.auraPlugState[0] || sDevice.dimVal != ip.brightness || sDevice.saturationValue != ip.saturation || sDevice.hueValue != ip.hue) {
                                            sceneTurnedState = false
                                            break
                                        }
                                    } else {
                                        if (sDevice.isTurnOn != ip.auraPlugState[0]) {
                                            sceneTurnedState = false
                                            break
                                        }
                                    }
                                } else if (ip.module == 3) {
                                    if ((sDevice.curtainState0 != ip.curtainState[0] || sDevice.curtainState1 != ip.curtainState[1]) || (ip.curtainState[0] == 0 && ip.curtainState[1] == 0)) {
                                        sceneTurnedState = false
                                        break
                                    }
                                } else if (ip.module == 6 || ip.module == 23 || ip.module == 14) {
                                    if ((sDevice.isTurnOn != ip.state[sDevice.index] || sDevice.dimVal != ip.dim[sDevice.index])) {
                                        sceneTurnedState = false
                                        break
                                    } else {
                                        if (sDevice.isTurnOn != ip.state[sDevice.index]) {
                                            sceneTurnedState = false
                                            break
                                        }
                                    }
                                } else {
                                    if (sDevice.isTurnOn != ip.state[sDevice.index]) {
                                        sceneTurnedState = false
                                        break
                                    }
                                }
                                break
                            }
                        }
                    }

                }
                if (!sceneTurnedState) {
                    break
                }
            }
            scene.isOn = sceneTurnedState
        }
        sceneAdapter.notifyDataSetChanged()
    }

    /**
     * Data from the TcpServer : Asynchronous
     */
    private val onTcpServerMessageReceived = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = intent.getStringExtra("key")
            if (data != null) {
                updateStates(data, "")
                updateAppearance()
            }
            Log.d("TCP_SERVER_DATA", data!!)

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
                    updateSwitchAws(data, device)
                } else {
                    updateSenseAws(data, device)
                }
            }
           /* if (shadow == "Connected") {
                fetchLoadsShadow()
            }*/
        }
    }
  /*  var thing_list: MutableList<String> = ArrayList()

    fun fetchLoadsShadow() {
        if (thing_list.size == 0) {
            thing_list = localSqlDatabase.getThing(mDb!!)
            val aura_sense_list = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
            for (things_name in aura_sense_list) {
                val thing = things_name.scense_thing
                thing_list.add(thing!!)
            }
        }
        for (x in thing_list) {
            Constant.IS_FIRST = false
            if (context is DashboardActivity) {
                (context as DashboardActivity).pusblishDataToShadow(x, jsonHelper.serializeLEDData())
            }
            // Thread.sleep(500)
        }
    }*/

    fun updateSwitchAws(data: AwsState, device: String) {
        if (data.led != 0) {
            for (l in IP.getIpDevices()) {
                if (l.name == device) {
                    if (l.module == 2 || l.module == 12 || l.module == 11 || l.module == 20) {
                        if (data.uiud == l.uiud) {
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

                        } else {
                            for (i in 0..1) {
                                l.condition[i] = "fail"
                            }

                        }
                    } else if (l.module == 1) {
                        if (data.uiud == l.uiud) {
                            if (!l.local) {
                                l.aws = true
                                l.condition[0] = "ready"
                                l.auraPlugState[0] = data.state["s0"] == 1.0
                                l.failure[0] = 0
                                l.curnLoad[0] = false
                            }
                        } else {
                            if (!l.local) l.condition[0] = "fail"
                        }
                    } else if (l.module == 3) {
                        if (data.uiud == l.uiud) {
                            if (!l.local) {
                                l.aws = true
                                for (i in 0..3) {
                                    l.failure[i] = 0
                                    l.condition[i] = "ready"
                                }
                                l.failure[0] = 0
                                l.curnLoad[0] = false
                            }
                            l.curtainState[0] = if (data.state["s0"] == 1.0) 1 else 0
                            l.curtainState[1] = if (data.state["s1"] == 1.0) 1 else 0
                        } else {
                            if (!l.local) {
                                l.condition[0] = "fail"
                            }
                        }

                    } else if (l.module == 7 || l.module == 8 || l.module == 13) {
                        if (data.uiud == l.uiud) {
                            if (!l.local) {
                                l.aws = true
                                l.condition[0] = "ready"
                                l.auraPlugState[0] = data.state["s0"] == 1.0
                                l.saturation = data.saturation
                                l.hue = data.hue
                                l.tempLight = data.temperature
                                l.brightness = data.dim["d0"] ?: data.brightness
                                l.failure[0] = 0
                                l.curnLoad[0] = false
                            }

                        } else {
                            if (!l.local) l.condition[0] = "fail"
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
                            if (!l.local) {
                                for (i in 0..4) {
                                    l.condition[i] = "fail"
                                }
                            }

                        }
                    } else {
                        if (data.uiud == l.uiud) {
                            if (!l.local) {
                                l.aws = true
                                for (i in 0..3) {
                                    l.failure[i] = 0
                                    l.condition[i] = "ready"
                                }
                                if (data.dim["d0"] != null) {
                                    l.dim[0] = data.dim["d0"] ?: 100
                                    l.dim[1] = data.dim["d1"] ?: 100
                                    l.dim[2] = data.dim["d2"] ?: 100
                                    l.dim[3] = data.dim["d3"] ?: 100
                                }
                                for (i in 0..3) {
                                    l.state[i] = data.state["s$i"] == 1.0
                                    l.failure[i] = 0
                                    l.curnLoad[i] = false
                                }
                            }

                        } else {
                            if (!l.local) {
                                for (i in 0..3) {
                                    l.condition[i] = "fail"
                                }
                            }
                        }
                    }
                    IP.registerIpDevice(l)
                    updateAppearance()
                    break
                }
            }
        } else {
            for (l in IP.getIpDevices()) {
                if (data.uiud == null) {
                    if (l.name == device) {
                        if (l.thing != null) {
                            if (!l.local && !l.aws) {
                                for (i in 0..3) {
                                    l.failure[i] = l.failure[i] + 1
                                    if (l.failure[i] > 3) {
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
                        IP.registerIpDevice(l)
                        updateAppearance()
                        break
                    }
                }
            }
        }
    }

    fun updateSenseAws(data: AwsState, device: String) {
        if (data.led != 0) {
            val convertState = jsonHelper.convertMotionState(Gson().toJson(data.state))
            for (s in totalSenseAvailableLocal) {
                if (s.sense_uiud == data.uiud) {
                    s.sense_loads[1].intensity = convertState.motion!![0]
                    s.sense_loads[2].intensity = convertState.temp!![1]
                    Constant.TEMP = convertState.temp!![1]
                    s.sense_loads[3].intensity = convertState.humid!![1]
                    Constant.HUMIDITY = convertState.humid!!.get(1)
                    s.sense_loads[4].intensity = convertState.lux!![0]
                    Constant.LUX = convertState.lux!![0]
                }
            }
        }
    }

    private fun nsdDiscovery() {
        nsd?.setBroadcastType("HOME")
        nsd?.initializeNsd()
        nsd?.discoverServices()
    }

    /**
     * Data from the TcpServer : Asynchronous
     */

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
            totalSenseAvailableLocal.find { it.aura_sence_name == device }?.let {
                it.sense_ip = ip!!
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
        try {
            ConnectTask(requireContext(), this, jsonHelper.initialData(uiud), ip, name).execute("")
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Before error connect task")
        }
    }

    private fun sendEspHandler(data: String, ip: String, name: String, type: String) {
        try {
            if (espHandler == null) {
                espHandler = EspHandler(this)
            }
            espHandler?.getResponseData(data, ip, name, type)

        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error in ESP_Handler_Connection")
        }

    }


    private fun sendEspCommandAdaptive(data: String, name: String, ip: String) {
        try {
            sendEspHandler(data, ip, name, "")
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error in ESP_Handler_Connection")
        }
    }

    private fun controlDevice(auraDevice: Device, longPressed: Boolean) {
        clickedDevice = auraDevice
        var cFlag = false
        if (longPressed) {
            cFlag = true
        }
        loadClicked = true
        loadPositionClicked = auraDevice.index
        for (l in IpListDevices) {
            if (l.name == auraDevice.deviceName) {
                val data = jsonHelper.serialize(auraDevice, l.uiud!!, l.module, cFlag)
                l.condition[auraDevice.index] = "update"
                l.curnLoad[auraDevice.index] = true
                if (l.ip == null) {
                    if (l.aws) {
                        if (l.thing != null) {
                            if (context is DashboardActivity) {
                                val flag = (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeDataToAws(auraDevice, l.uiud!!, l.module, cFlag))
                            }
                        }
                    } else {
                        for (i in 0..3) {
                            l.condition[i] = "fail"
                        }
                    }
                } else if (l.local) {
                    if (l.fullDeviceName == null) {
                        ConnectTask(requireContext(), this, data, l.ip!!, auraDevice.deviceName).execute("")
                    } else {
                        if (l.fullDeviceName!!.startsWith("Aura Switch")) {
                            ConnectTask(requireContext(), this, data, l.ip!!, auraDevice.deviceName).execute("")
                        } else {
                            sendEspHandler(data, l.ip!!, auraDevice.deviceName, "")
                        }
                    }
                } else if (l.aws) {
                    if (l.thing != null) {
                        if (context is DashboardActivity) {
                            val flag = (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeDataToAws(auraDevice, l.uiud!!, l.module, cFlag))
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
        updateAppearance()
    }


    private fun updateStates(message: String, name: String) {
        if (message.contains("ERROR")) {
            val cNumber = ArrayList<String>(arrayListOf())
            val modeL = ArrayList<String>(arrayListOf())
            val btnNameArr = ArrayList<String>(arrayListOf())
            val deviceErrorName: String
            deviceError = -1
            cNumber.clear()
            modeL.clear()
            btnNameArr.clear()
            val data = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            deviceErrorName = data[1]
            if (deviceErrorName == "RemoteData") {
                cNumber.add(remoteCloudControlData?.channelNumber.toString())
                modeL.add(remoteCloudControlData?.remoteModel.toString())
                btnNameArr.add(remoteCloudControlData?.remoteButtonName.toString())
                (context as DashboardActivity).pusblishDataToShadow(data[3], jsonHelper.serializeSceneRemoteControl(data[2], modeL, cNumber, btnNameArr))
            } else if (name == "scene_delete") {
                deleteScene(sceneToDelete!!, Constant.OFFLINE_MODE)
            } else {
                if (deviceError != 0) {
                    for (l in IpListDevices) {
                        if (l.name == deviceErrorName) {
                            if (l.owned == 0) {
                                if (l.thing != null) {
                                    if (l.aws) {
                                        l.local = false
                                        for (i in 0..3) {
                                            l.failure[i] = 0
                                            l.condition[i] = "ready"
                                        }
                                    } else {
                                        for (i in 0..3) {
                                            l.condition[i] = "update"
                                            l.failure[i] = 0
                                        }
                                        l.local = false
                                        l.aws = true
                                    }
                                    if (context is DashboardActivity) {
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
            Log.d("SERVER_ESP_DATA", "Data Received : $updatedDevice")
            deviceError = updatedDevice.error
            when (updatedDevice.type) {
                1 -> {
                    for (l in IP.getIpDevices()) {
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
                            }  else {
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
                            break
                        }
                    }

                }

                4, 3 -> {
                    if (updatedDevice.error == 1) {
                        for (l in IP.getIpDevices()) {
                            if (l.name == updatedDevice.name) {
                                l.local = false
                                l.aws = false
                                if (updatedDevice.state.size == 2 || updatedDevice.mdl == 2 || updatedDevice.mdl == 12 || updatedDevice.mdl == 11 || updatedDevice.mdl == 20) {
                                    for (i in 0..1) {
                                        l.condition[i] = "fail"
                                    }
                                } else {
                                    for (i in 0..3) {
                                        l.condition[i] = "fail"
                                    }
                                }
                                IP.registerIpDevice(l)
                                val message = "Device-${l.name} not available."
                                SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(message).show()
                                break
                            }
                        }
                    } else {
                        for (l in IP.getIpDevices()) {
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
                                    l.module = updatedDevice.mdl
                                    l.local = true
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
                                break
                            }
                        }
                    }
                }
                11 -> {
                    if (updatedDevice.error == 0) {
                        progress_layout.visibility = View.VISIBLE
                        deleteScene(sceneToDelete!!, Constant.ONLINE_MODE)
                    } else {
                        val message = "Device-${name} not authorized."
                        SingleBtnDialog.with(requireActivity()).setHeading(requireActivity().getString(R.string.alert)).setMessage(message).show()
                    }
                }
                14 -> {
                    if (updatedDevice.error == 0) {
                        Toast.makeText(requireActivity(), getString(R.string.message_send), Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    for (l in IP.getIpDevices()) {
                        if (l.name == updatedDevice.name) {
                            if (updatedDevice.state.size == 2 || updatedDevice.mdl == 2 || updatedDevice.mdl == 12 || updatedDevice.mdl == 11 || updatedDevice.mdl
                                    == 20) {
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
        // updateAppearance()
    }

    private fun openDimmingDialog(device: Device) {
        val dialog = Dialog(requireContext())
        var dimVal = device.dimVal
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_configure_edit_dimming)
        if (device.name == "Fan" && device.isTurnOn) {
            Glide.with(this).load(getIconDrawable(device.type, device.isTurnOn))
                    .into(dialog.iconDevice)
        } else {
            dialog.iconDevice.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
        }
        dialog.tvDialogTitle.text = String.format(getString(R.string.text_configure_device_dim), device.name)
        dialog.sickbar.setProgress(dimVal.toFloat())
        dialog.sickbar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {
            }

            override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                dimVal = progress
                device.dimVal = dimVal
                controlDevice(device, true)
            }
        })

        dialog.btnEdit.setOnClickListener {
            if (userHomeType) {
                val intent = Intent(context, EditLoadActivity::class.java)
                intent.putExtra("DEVICE", Gson().toJson(device))
                intent.putExtra(Constant.ROOM_NAME, Constant.CREATE_ROOM_NAME)
                startActivity(intent)
            } else {
                SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_edit)).show()
            }

        }
        dialog.btnDone.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }

    private fun tunnableDialog(device: Device) {
        val dialog = Dialog(requireContext())
        var dimVal = 0
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

    private fun openFanDimmingDialog(device: Device) {
        val dialog = Dialog(requireContext())
        val dimVal = device.dimVal
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.fan_dimming_layout)
        if (device.name == "Fan" && device.isTurnOn) {
            Glide.with(this).load(getIconDrawable(device.type, device.isTurnOn))
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
            if (userHomeType) {
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

    private fun showHomesDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_homes)
        dialog.setCanceledOnTouchOutside(true)
        val addNewHome = dialog.findViewById<TextView>(R.id.addHome)
        val addHomePlus = dialog.findViewById<ImageView>(R.id.image_add)

        addNewHome.setOnClickListener {
            val intent = Intent(activity, HomeDetailsActivity::class.java)
            intent.putExtra(CREATE_HOME_ROOM, "create")
            intent.putExtra("HOME_NAME", "Home Name")
            startActivity(intent)
        }
        addHomePlus.setOnClickListener {
            val intent = Intent(activity, HomeDetailsActivity::class.java)
            intent.putExtra(CREATE_HOME_ROOM, "create")
            intent.putExtra("HOME_NAME", "Home Name")
            startActivity(intent)
        }

        val homesList = ArrayList<Home>()
        val listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        for (home in listRoom) {
            if (home.type == "home") {
                if (home.sharedHome == "master") {
                    if (home.name == Constant.HOME) {
                        homesList.add(Home(home.name, isInActive = true, sharedFlag = false, bgUrl = home.bgUrl.toInt()))
                    } else {
                        homesList.add(Home(home.name, isInActive = false, sharedFlag = false, bgUrl = home.bgUrl.toInt()))
                    }
                } else {
                    if (home.sharedHome == "guest") {
                        if (home.name == Constant.HOME) {
                            homesList.add(Home(home.name, isInActive = true, sharedFlag = true, bgUrl = home.bgUrl.toInt()))
                        } else {
                            homesList.add(Home(home.name, isInActive = false, sharedFlag = true, bgUrl = home.bgUrl.toInt()))
                        }
                    }
                }
            }
        }

        if (homesList.isEmpty()) homesList.add(Home("My Home", isInActive = true, sharedFlag = false, bgUrl = 0))
        val homesRv = dialog.findViewById<RecyclerView>(R.id.homesRv)
        homesRv.adapter = HomeListAdapter(homesList) { home: Home, action: Int ->
            when (action) {
                EDIT_HOME -> {
                    if (userHomeType) {
                        val homeSelected = home.title.toString()
                        val intent = Intent(activity, HomeDetailsActivity::class.java)
                        intent.putExtra(CREATE_HOME_ROOM, "edit")
                        intent.putExtra("HOME_NAME", homeSelected)
                        startActivity(intent)
                    } else {
                        SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_edit)).show()
                        //longSnackbar(requireActivity().findViewById(android.R.id.content), "Guest cannot edit.")
                    }

                }
                SELECTED_HOME -> {
                    val homeSelected = home.title.toString()
                    if (Constant.HOME != homeSelected) {
                        Constant.HOME = homeSelected
                        val prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                        prefEditor.putString("HOME", homeSelected)
                        prefEditor.apply()
                        init()
                    }
                }
                SHARE_HOME -> {
                    if (userHomeType) {
                        val intent = Intent(activity, ShareAuraActivity::class.java)
                        startActivity(intent)
                    } else {
                        SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_share)).show()
                        //longSnackbar(requireActivity().findViewById(android.R.id.content), "Guest cannot share.")
                    }
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showPopup(v: View) {
        val popup = context?.let { PopupMenu(it, v) }
        val inflater = popup?.menuInflater
        inflater?.inflate(R.menu.options_menu, popup.menu)
        popup?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.addAuraSwitch -> {
                    val intent = Intent(activity, DeviceAddActivity::class.java)
                    startActivity(intent)
                    //displayPopUp()
                    true
                }
                R.id.createScenes -> {
                    val intent = Intent(activity, CreateSceneActivity::class.java)
                    intent.putExtra("inputSceneType", "create")
                    intent.putExtra("inputSceneName", "")
                    intent.putExtra("inputSceneIconUrl", 0)
                    startActivity(intent)
                    true
                }
                R.id.addRoom -> {
                    val intent = Intent(activity, AddRoomActivity::class.java)
                    intent.putExtra(Constant.ROOM_NAME, "Room Name")
                    intent.putExtra("ROOM_EDIT_TYPE", "create")
                    startActivity(intent)
                    true
                }
                R.id.addRemote -> {
                    if (zmoteDeviceList.size > 0) {
                        //showDialogIrDevice()
                    } else {
                        activity?.toast("Please add Zmote device to use this feature.")
                    }
                    true
                }
                R.id.addCamera -> {
                    val intent = Intent(activity, AddCameraActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        popup?.show()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnAddDevice -> {
                val intent = Intent(activity, DeviceAddActivity::class.java)
                startActivity(intent)
            }
            R.id.addAuraSwitchBtn -> {
                showPopup(addAuraSwitchBtn)
            }
            R.id.homeTitleTv, R.id.home_wozart_logo, R.id.spinnerHomes -> {
                showHomesDialog()
            }
        }
    }


    override fun onRemoteDelete(remoteListModel: RemoteListModel, position: Int) {
        localSqlRemote.deleteRemote(mdbSense!!, remoteListModel.remoteName!!, remoteListModel.auraSenseName!!)
        remoteListAdapter.notifyDataSetChanged()
    }

    private fun setCameraAdapter() {
        (rvCameraView.adapter as CameraViewAdapter).setData(cameraViewList)
    }


    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        when (viewType) {
            Constant.REMOTE_DELETE -> {
                if (data is RemoteIconModel) {
                    data.btnFavourite = false
                    btnAdapter.notifyDataSetChanged()
                }
            }
            Constant.REMOTE -> {
                if (data is RemoteIconModel) {
                    remoteTriggerAction(data)
                }
            }
            R.id.card_scene -> {
                if (!cameraViewList.contains((data as CameraModel))) {
                    cameraViewList.add(data as CameraModel)
                } else {
                    cameraViewList.remove(data as CameraModel)
                }

                setCameraAdapter()
            }
            Constant.DELETE_CAMERA -> {
                DoubleBtnDialog.with(activity).setHeading(getString(R.string.alert)).setMessage(getString(R.string.delete_camera))
                        .setOptionNegative(getString(R.string.txt_cancel))
                        .setOptionPositive(getString(R.string.text_delete))
                        .setCallback(object : DoubleBtnDialog.OnActionPerformed {
                            override fun negative() {

                            }

                            override fun positive() {
                                localSQLCamera.delete(mdbCamera!!, (data as CameraModel).cameraName!!, Constant.HOME!!)
                                cameraList.removeAt(position)
                                for (cam in cameraViewList) {
                                    if (cam.cameraName == data.cameraName) {
                                        cameraViewList.remove(cam)
                                        break
                                    }
                                }
                                setCamDisplayAdapter()
                                setCameraAdapter()
                            }

                        }).show()

            }
            Constant.VIDEO_STOPED -> {
                cameraViewList.removeAt(position)
                setCameraAdapter()
                if (cameraViewList.size == 0) {
                    camerViewadapter?.vlcVideoLibrary?.stop()
                }
            }
        }
    }

    private fun remoteTriggerAction(remoteIconModel: RemoteIconModel) {
        for (remote in totalSenseAvailableLocal) {
            if (remote.aura_sence_name == remoteIconModel.name) {
                remoteCloudControlData = remoteIconModel
                val dataTrigger = jsonHelper.triggerRemoteButton(remoteIconModel, remoteIconModel.remoteModel, remote.sense_uiud!!)
                sendCommandToHandler(dataTrigger, remoteIconModel.remoteButtonName, remote.sense_ip, remote.aura_sence_name, remote.sense_uiud!!, remote.scense_thing)
            }
        }

    }

    private fun sendCommandToHandler(dataSend: String, remoteButtonName: String?, senseIp: String, auraSenceName: String?, senseUiud: String, scenseThing: String?) {
        sendEspHandler(dataSend, senseIp, "RemoteData:$senseUiud:$scenseThing", auraSenceName!!)
    }

    override fun onDetach() {
        super.onDetach()
        if (camerViewadapter?.vlcVideoLibrary != null) {
            if (camerViewadapter?.vlcVideoLibrary?.isPlaying!!) {
                camerViewadapter?.vlcVideoLibrary?.stop()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(onNSDServiceResolved)
        //nsd?.stopDiscovery()
        nsd = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (camerViewadapter?.vlcVideoLibrary != null) {
            if (camerViewadapter?.vlcVideoLibrary?.isPlaying!!) {
                camerViewadapter?.vlcVideoLibrary?.stop()
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(onNSDServiceResolved)
        nsd?.stopDiscovery()
        nsd = null
    }


    @SuppressLint("UseRequireInsteadOfGet")
    private fun controlCurtainAction(auraDevice: Device, action: String) {
        for (l in IpListDevices) {
            if (l.name == auraDevice.deviceName) {
                val data = jsonHelper.setActionCurtain(context!!, auraDevice, l.uiud!!, action, stateSet = true)
                l.condition[auraDevice.index] = "update"
                l.curnLoad[auraDevice.index] = true
                if (l.ip == null) {
                    if (l.aws) {
                        if (l.thing != null) {
                            if (context is DashboardActivity) {
                                val flag = (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.curtainCloudControl(activity!!, l.uiud!!, action, auraDevice))
                            }
                        }
                    } else {
                        for (i in 0..3) {
                            l.condition[i] = "fail"
                        }
                    }
                } else {
                    if (l.local) {
                        sendEspHandler(data, l.ip!!, auraDevice.deviceName, "")

                    } else {
                        if (l.aws) {
                            if (l.thing != null) {
                                if (context is DashboardActivity) {
                                    val flag = (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.curtainCloudControl(activity!!, l.uiud!!, action, auraDevice))
                                }
                            }
                        } else {
                            if ((!l.local) and (!l.aws)) {
                                for (i in 0..3) {
                                    l.condition[i] = "fail"
                                }
                                val message = "Device-${l.name} not available."
                                //longSnackbar(requireView().findViewById(android.R.id.content), "Device-${l.name} not available.")
                                SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(message).show()
                            }
                        }
                    }
                }
                IP.registerIpDevice(l)
                //updateAppearance()
                break
            }
        }
    }

    private fun dialog(device: Device) {
        val builder: ColorPickerDialog.Builder = ColorPickerDialog.Builder(context)
                .setTitle("Set colour")
                .setPreferenceName("Test")
                .setNeutralButton(getString(R.string.edit)) { dialog, which ->
                    if (userHomeType) {
                        val intent = Intent(context, EditLoadActivity::class.java)
                        intent.putExtra("DEVICE", Gson().toJson(device))
                        intent.putExtra(Constant.ROOM_NAME, Constant.CREATE_ROOM_NAME)
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
                .setBottomSpace(8)
        builder.colorPickerView.alphaSlideBar!!.visibility = View.GONE
        builder.show()
    }


    @SuppressLint("SetTextI18n")
    private fun setLayoutColor(envelope: ColorEnvelope, device: Device) {
        val hsv = FloatArray(3)
        Color.colorToHSV(envelope.color, hsv)
        val saturation = hsv[1] * 100
        val lightness = hsv[2] * 100
        device.hueValue = hsv[0].toInt()
        device.saturationValue = saturation.toInt()
        device.dimVal = lightness.toInt()
        Log.d("COLOR_H_S_L", "$saturation , $lightness ${device.hueValue}")
        controlRGBTunnable(device, true, "RGB")

    }

    private fun setTunableTempOnColor(envelope: ColorEnvelope, device: Device) {
        val hsv = FloatArray(3)
        val xyzOut = DoubleArray(3)
        Color.colorToHSV(envelope.color, hsv)
        ColorUtils.RGBToLAB(hsv[0].roundToInt(), hsv[1].roundToInt(), hsv[2].roundToInt(), xyzOut)
        Log.d("TEMP", "$xyzOut")
        val saturation = hsv[1] * 100
        device.hueValue = hsv[0].toInt()
        device.saturationValue = saturation.toInt()
        controlRGBTunnable(device, true, "Tunable")
    }

    private fun controlRGBTunnable(auraDevice: Device, longPressed: Boolean, type: String) {
        clickedDevice = auraDevice
        var cFlag = false
        if (longPressed) {
            cFlag = true
        }
        loadClicked = true
        loadPositionClicked = auraDevice.index
        for (l in IpListDevices) {
            if (l.name == auraDevice.deviceName) {
                if (!cFlag) {
                    auraDevice.dimVal = l.brightness
                    auraDevice.hueValue = l.hue
                    auraDevice.saturationValue = l.saturation
                    auraDevice.tempValue = l.tempLight
                }
                val data = jsonHelper.serializeDatargb(auraDevice, l.uiud!!, cFlag, type)
                Log.d("ESP_DATA_SEND", data)
                l.condition[auraDevice.index] = "update"
                l.curnLoad[auraDevice.index] = true
                if (l.ip == null) {
                    if (l.aws) {
                        if (l.thing != null) {
                            if (context is DashboardActivity) {
                                val flag = (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeRGBTdata(auraDevice, l.uiud!!, l.module, cFlag))
                            }
                        }
                    } else {
                        for (i in 0..3) {
                            l.condition[i] = "fail"
                        }
                    }
                } else {
                    if (l.local) {
                        sendEspHandler(data, l.ip!!, auraDevice.deviceName, "")
                    } else {
                        if (l.aws) {
                            if (l.thing != null) {
                                if (context is DashboardActivity) {
                                    val flag = (context as DashboardActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeRGBTdata(auraDevice, l.uiud!!, l.module, cFlag))
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
                    }
                }
                IP.registerIpDevice(l)
                break
            }
        }

    }

}

