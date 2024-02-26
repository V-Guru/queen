package com.wozart.aura.ui.base.baseselectdevices

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.wozart.aura.R
import com.wozart.aura.ui.createautomation.CreateAutomationActivity
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.*
import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.OnFragmentInteractionListener
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.createautomation.SetAutomationActivity
import com.wozart.aura.ui.dashboard.*
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.fragment_select_devices.listRooms
import kotlinx.android.synthetic.main.layout_header.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 06/09/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Saif - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0.5
 * ____________________________________________________________________________
 *
 *****************************************************************************/
abstract class BaseSelectAutomationDevices : androidx.fragment.app.Fragment(), RecyclerItemClicked {

    private var sceneNameCheck: String? = null
    protected var mListener: OnFragmentInteractionListener? = null
    protected var adapter: SelectRoomAdapter = SelectRoomAdapter(this)
    var roomsList: MutableList<RoomModel> = ArrayList()
    private val localSqlDatabaseSchedule = ScheduleTable()
    var localSqlScene = SceneTable()
    var mDbScene: SQLiteDatabase? = null
    private var mDbSchedule: SQLiteDatabase? = null
    private val localSqlDatabase = DeviceTable()
    private var mDb: SQLiteDatabase? = null
    private var automationScene = AutomationScene()
    private val localSqlUtils = UtilsTable()
    private var mDbUtils: SQLiteDatabase? = null
    private var localSqlRemote = AuraSenseTable()
    private var mdbSense: SQLiteDatabase? = null
    var automationSceneType: String? = null
    var scenes = ArrayList<Scene>()
    var devicelist: MutableList<Device> = ArrayList()
    val scenesSelectedList = ArrayList<Scenes>()
    var activity: Activity? = null
    var remoteFavButton: MutableList<RemoteIconModel> = ArrayList()
    var remoteDataList: MutableList<RemoteModel> = ArrayList()

    companion object {
        var automationSceneNameOld: String? = null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_automation_select_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context is CreateAutomationActivity) {
            automationScene = (context as CreateAutomationActivity).getAutomationScene()
            automationSceneNameOld = (context as CreateAutomationActivity).getAutomationSceneName()
            automationSceneType = (context as CreateAutomationActivity).getAutomationSceneType()
        }
        if (automationSceneType == "edit") {
            sceneNameCheck = automationScene.name
        }
        if (context is SetAutomationActivity) {
            automationSceneNameOld = (context as SetAutomationActivity).getAutomationOldName()

        }
        init()
    }

    fun init() {

        val dbUtils = UtilsDbHelper(requireContext())
        mDbUtils = dbUtils.writableDatabase
        val remoteDbHelper = AuraSenseDbHelper(requireContext())
        mdbSense = remoteDbHelper.writableDatabase
        remoteDataList.clear()
        remoteFavButton.clear()
        if (Constant.HOME == null) {
            val preference = PreferenceManager.getDefaultSharedPreferences(activity)
            Constant.HOME = preference.getString("HOME", "NO HOME")
        }
        val tvNext = requireView().findViewById<TextView>(R.id.tvNext)
        tvNext.text = getString(R.string.text_next)
        tvTitle.text = getTitle()
        tvNext.setOnClickListener { openNextScreen() }
        // tvNext.setTextColor(Color.WHITE)
        roomsList.clear()
        scenesSelectedList.clear()
        home.setOnClickListener { mListener?.onHomeBtnClicked() }
        listRooms.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        listRooms.adapter = adapter
        adapter.init(roomsList)
        populateData()
    }

    abstract fun showSceneInputs(): Boolean

    abstract fun getTitle(): String

    protected fun getSelectedRoomDeviceData(): MutableList<RoomModel> {
        devicelist.clear()
        val rooms: MutableList<RoomModel> = ArrayList()
        for (room in this.roomsList) {
            devicelist = ArrayList(room.deviceList.filter { it.isSelected })  //for device isSelected.
            if (room.deviceList.size > 0) {
                val roomModel = room.copy()
                roomModel.deviceList = devicelist as ArrayList<Device>
                rooms.add(roomModel)
            }
        }
        return rooms
    }

    private fun populateData() {
        deviceList()
        favouriteRemoteButton()
    }

    private fun favouriteRemoteButton() {
        remoteDataList = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        remoteFavButton = localSqlRemote.getFavouriteRemote(mdbSense!!, Constant.HOME!!)
    }

    private fun deviceList() {
        val dbSchedule = ScheduleDbHelper(requireContext())
        mDbSchedule = dbSchedule.writableDatabase
        val dbHelper = DeviceDbHelper(requireContext())
        mDb = dbHelper.writableDatabase
        val dbScene = SceneDbHelper(requireContext())
        mDbScene = dbScene.writableDatabase
        scenes.clear()
        scenes = localSqlScene.getAllScenes(mDbScene!!, Constant.HOME!!)
        val dummyRooms = localSqlDatabase.getRooms(mDb!!, Constant.HOME!!)
        for (x in dummyRooms) {
            val room = RoomModel()
            room.name = x.roomName
            roomsList.add(room)
        }
        val scheduleSelected = localSqlDatabaseSchedule.getAutomationScheduleScene(mDbSchedule!!, automationSceneNameOld?:"new", Constant.HOME!!)
        if (roomsList.isNotEmpty()) {
            for (x in roomsList) {
                val allDevicesList = ArrayList<Device>()
                val devices = localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, x.name!!)
                for (device in devices) {
                    for (i in 0 until device.loads.size) {
                        var flag = false
                        var selected_loads = false
                        for (room in scheduleSelected.load) {
                            for (r in room.deviceList) {
                                if (r.deviceName == "Scene") {
                                    var scenePresent = false
                                    for (scene in scenes) {
                                        scenePresent = false
                                        for (s in scenesSelectedList) {
                                            if (s.title == r.name) {
                                                scenePresent = true
                                                break
                                            }
                                        }
                                        if ((r.name == scene.name) && (!scenePresent)) {
                                            scenesSelectedList.add(Scenes(scene.name, scene.icon, scene.room, r.isTurnOn))
                                            break
                                        }
                                    }
                                }
                                if ((r.deviceName == device.name) and (r.index == i)) {
                                    r.isSelected = true
                                    selected_loads = true
                                    if (r.isTurnOn) {
                                        flag = true
                                    }
                                    break
                                }
                            }
                        }
                        val loadsData = Device(device.loads[i].icon!!, flag, selected_loads, 100, device.loads[i].name!!, localSqlDatabase.getRoomForDevice(mDb!!, device.name), device.name, device.loads[i].index!!, device.loads[i].dimmable!!)
                        loadsData.checkType = device.loads[i].type.toString()
                        loadsData.dimVal = device.loads[i].brightness
                        loadsData.saturationValue = device.loads[i].saturation
                        loadsData.hueValue = device.loads[i].hue
                        loadsData.curtainState = device.loads[i].curtainState
                        loadsData.curtainState0 = device.loads[i].curtainState0
                        loadsData.curtainState1 = device.loads[i].curtainState1
                        allDevicesList.add(loadsData)
                    }
                }
                x.deviceList = allDevicesList
            }
        }
    }

    abstract fun openNextScreen()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnAutomationListInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

}