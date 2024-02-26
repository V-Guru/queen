package com.wozart.aura.ui.base.baseselectdevices

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.sqlLite.AuraButtonTable
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.entity.sql.buttonDevice.ButtonDbHelper
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.ui.createautomation.OnFragmentInteractionListener
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.Scenes
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.fragment_select_devices.*
import kotlinx.android.synthetic.main.layout_header.*


/**
 * Created by Saif on 12/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
abstract class BaseSelectButtonLoad : androidx.fragment.app.Fragment(), RecyclerItemClicked {

    protected var mListener: OnFragmentInteractionListener? = null
    protected var adapter: SelectRoomAdapter = SelectRoomAdapter(this)
    var roomsList: MutableList<RoomModel> = ArrayList()
    private val localSQLButton  = AuraButtonTable()
    private var mDbButton : SQLiteDatabase ?= null
    private val localSqlDatabase = DeviceTable()
    private var mDb: SQLiteDatabase? = null
    var localSqlScene = SceneTable()
    var mDbScene: SQLiteDatabase? = null
    var buttonModel = ButtonModel()
    var scenes = ArrayList<Scene>()
    var devicelist : MutableList<Device> = ArrayList()
    val scenesSelectedList = ArrayList<Scenes>()
    var activity : Activity?= null
    var selectedButtonId : String ?= null
    var buttonTapId : String ?= null
    var generatedId : String ?= null

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
        selectedButtonId = activity?.intent!!.getIntExtra(Constant.BUTTON_SELECTED_ID,0).toString()
        buttonTapId = activity?.intent!!.getIntExtra(Constant.BUTTON_TAP_ID,0).toString()
        buttonModel = Gson().fromJson(activity?.intent!!.getStringExtra(Constant.BUTTON_DATA),ButtonModel::class.java)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnAutomationListInteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_automation_select_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun initialize(){
        val dbHelper = DeviceDbHelper(context!!)
        mDb = dbHelper.writableDatabase

        val dbScene = SceneDbHelper(context!!)
        mDbScene = dbScene.writableDatabase

        val dbButton = ButtonDbHelper(context!!)
        mDbButton = dbButton.writableDatabase

        generatedId = buttonModel.start.toString()+selectedButtonId+buttonTapId+buttonModel.end
        buttonModel.buttonId = generatedId

        val tvNext = requireView().findViewById<TextView>(R.id.tvNext)
        tvNext.text = getString(R.string.text_next)
        tvTitle.text = getTitle()
        tvNext.setOnClickListener { openNextScreen() }
        tvNext.setTextColor(Color.WHITE)
        roomsList.clear()
        scenesSelectedList.clear()
        home.setOnClickListener { mListener?.onHomeBtnClicked() }
        listRooms.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        listRooms.adapter = adapter
        adapter.init(roomsList)
        populateData()
    }

    abstract fun getTitle(): String

    private fun populateData() {
        deviceList()
    }

    protected fun getSelectedRoomDeviceData(): MutableList<RoomModel> {
        devicelist.clear()
        val rooms: MutableList<RoomModel> = ArrayList()
        for (room in this.roomsList) {
            devicelist = ArrayList(room.deviceList.filter { it.isSelected })
            if (room.deviceList.size > 0) {
                val roomModel = room.copy()
                roomModel.deviceList = devicelist as ArrayList<Device>
                rooms.add(roomModel)
            }
        }
        return rooms
    }

    fun deviceList(){
        scenes.clear()
        scenes = localSqlScene.getAllScenes(mDbScene!!, Constant.HOME!!)
        val dummyRooms = localSqlDatabase.getRooms(mDb!!, Constant.HOME!!)
        for (x in dummyRooms) {
            val room = RoomModel()
            room.name = x.roomName
            roomsList.add(room)
        }

        val buttonData = localSQLButton.getSceneControllerData(mDbButton!!, buttonModel.auraButtonName.toString(), generatedId.toString())
        if (!roomsList.isEmpty()) {
            for (x in roomsList) {
                val allDevicesList = ArrayList<Device>()
                val devices = localSqlDatabase.getDevicesForRoom(mDb!!,  Constant.HOME!!, x.name!!)
                for (device in devices) {
                    for (i in 0 until device.loads.size) {
                        var flag = false
                        var selected_loads = false
                        for(room in buttonData.load){
                            for(r in room.deviceList){
                                if(r.deviceName == "Scene"){
                                    var scenePresent = false
                                    for(scene in scenes){
                                        scenePresent = false
                                        for(s in scenesSelectedList){
                                            if(s.title == r.name){
                                                scenePresent = true
                                                break
                                            }
                                        }
                                        if((r.name == scene.name) && (!scenePresent)){
                                            scenesSelectedList.add(Scenes(scene.name, scene.icon, scene.room, r.isTurnOn))
                                            break
                                        }
                                    }
                                    break
                                }
                                if((r.deviceName == device.name) and (r.index == i)){
                                    r.isSelected = true
                                    selected_loads = true
                                    if(r.isTurnOn){
                                        flag = true
                                    }
                                    break
                                }
                            }
                        }
                        allDevicesList.add(Device(device.loads[i].icon!!, flag,selected_loads,100, device.loads[i].name!!, localSqlDatabase.getRoomForDevice(mDb!!, device.name), device.name, device.loads[i].index!!,device.loads[i].dimmable!!))
                    }
                }
                x.deviceList = allDevicesList
            }
        }
    }

    abstract fun openNextScreen()

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
}