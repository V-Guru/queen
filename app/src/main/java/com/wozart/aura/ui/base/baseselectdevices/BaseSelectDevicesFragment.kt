package com.wozart.aura.ui.base.baseselectdevices

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.AuraSenseTable
import com.wozart.aura.ui.createautomation.OnFragmentInteractionListener
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.base.SceneIconAdapter
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.createscene.CreateSceneActivity
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.fragment_select_devices.*
import kotlinx.android.synthetic.main.layout_header.*
import kotlin.collections.ArrayList


abstract class BaseSelectDevicesFragment : androidx.fragment.app.Fragment(), RecyclerItemClicked {

    private val drawables = arrayListOf(R.drawable.ic_good_morning_off,
            R.drawable.ic_good_night_off, R.drawable.ic_enter_off, R.drawable.ic_exit_off, R.drawable.ic_party_off, R.drawable.ic_reading_off, R.drawable.ic_movie_off)
    private var sceneNameCheck: String? = null
    private var sceneIconurl: Int? = 0
    private var sceneIconPosition: Int? = 0
    private var sceneType: String? = null
    protected var mListener: OnFragmentInteractionListener? = null
    protected var adapter: SelectRoomAdapter = SelectRoomAdapter(this)
    var roomsList: MutableList<RoomModel> = ArrayList()
    private lateinit var sceneIconAdapter: SceneIconAdapter
    private val localSqlDatabase = DeviceTable()
    private val localSqlUtils = UtilsTable()
    private var mDbUtils: SQLiteDatabase? = null
    private var localSqlRemote = AuraSenseTable()
    private var mdbSense: SQLiteDatabase? = null
    private var mDb: SQLiteDatabase? = null
    private var sceneCreateType = true
    private val localSqlScene = SceneTable()
    var scene: Scene? = null
    var sceneList = ArrayList<Scene>()
    var sceneSelectedIconPosition: Int = 0
    var remoteFavButton: MutableList<RemoteIconModel> = ArrayList()
    var remoteDataList: MutableList<RemoteModel> = ArrayList()
    private var mDbScene: SQLiteDatabase? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context is CreateSceneActivity) {
            sceneNameCheck = (context as CreateSceneActivity).getSceneName()
            sceneType = (context as CreateSceneActivity).getSceneType()
            if (sceneNameCheck != "") {
                sceneCreateType = false
            }
        }
        if (context is CreateSceneActivity) {
            sceneIconurl = (context as CreateSceneActivity).getSceneIconUrl()
        }
        sceneIconPosition = 0

//        var pos = 0
//        for (icons in drawables) {
//            if (icons == sceneIconurl) {
//                sceneIconPosition = pos
//                selectedSceneIcon = true
//            } else {
//                pos += 1
//            }
//        }
        for (i in drawables.indices) {
            val scene = Scene()
            scene.icon = drawables[i]
            if (i == sceneIconurl) {
                sceneIconPosition = i
                scene.isSceneSelected = true
            }
            sceneList.add(scene)
        }

        val dbScene = SceneDbHelper(requireContext())
        mDbScene = dbScene.writableDatabase

        val dbUtils = UtilsDbHelper(requireContext())
        mDbUtils = dbUtils.writableDatabase

        val remoteDbHelper = AuraSenseDbHelper(requireContext())
        mdbSense = remoteDbHelper.writableDatabase

        init()
    }


    fun init() {
        remoteDataList.clear()
        remoteFavButton.clear()
        tvTitle.text = getTitle()
        tvNext.setOnClickListener { openNextScreen() }
        roomsList.clear()
        home.setOnClickListener { mListener?.onHomeBtnClicked() }
        home.setColorFilter(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        listRooms.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        listRooms.adapter = adapter
        adapter.init(roomsList)
        val visibility = if (showSceneInputs()) View.VISIBLE else View.INVISIBLE
        inputSceneName.visibility = visibility
        tvSelectSceneIcon.visibility = visibility
        listScenes.visibility = visibility
        switchAdvance.visibility = View.VISIBLE
        switchAdvance.setOnCheckedChangeListener { buttonView, isChecked ->
            cvEnableDisable.visibility = if (isChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }

        }
        if (visibility == View.VISIBLE) {
            sceneIconAdapter = SceneIconAdapter(sceneList) { pos: Int, _ ->
                if (pos == 0) {
                    sceneSelectedIconPosition = pos
                } else if (pos == 1) {
                    sceneSelectedIconPosition = pos
                } else if (pos == 2) {
                    sceneSelectedIconPosition = pos
                } else if (pos == 3) {
                    sceneSelectedIconPosition = pos
                } else if (pos == 4) {
                    sceneSelectedIconPosition = pos
                } else if (pos == 5) {
                    sceneSelectedIconPosition = pos
                } else if (pos == 6) {
                    sceneSelectedIconPosition = pos
                }
            }
            listScenes.adapter = sceneIconAdapter
        }
        populateData()
    }

    abstract fun showSceneInputs(): Boolean

    abstract fun getTitle(): String

    protected fun getSelectedRoomDeviceData(): MutableList<RoomModel> {
        val rooms: MutableList<RoomModel> = ArrayList()
        for (room in this.roomsList) {
            val deviceList = ArrayList(room.deviceList.filter { it.isSelected })  //for device isSelected.
            if (room.deviceList.size > 0) {
                val roomModel = room.copy()
                roomModel.deviceList = deviceList
                rooms.add(roomModel)
            }
        }
        return rooms
    }

    protected fun getIcon(): Int {
        return sceneSelectedIconPosition
        //return sceneIconAdapter.selectedPostion()
    }


    private fun populateData() {
        favouriteRemoteButton()
        deviceList()
        sceneList()
    }

    private fun sceneList() {
        sceneIconAdapter.init(drawables, sceneIconPosition!!)

    }

    private fun favouriteRemoteButton() {
        remoteDataList = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        remoteFavButton = localSqlRemote.getFavouriteRemote(mdbSense!!, Constant.HOME.toString())
    }

    private fun deviceList() {
        var autoCheckScene = false
        val dbHelper = DeviceDbHelper(requireContext())
        mDb = dbHelper.writableDatabase

        val dummyRooms = localSqlDatabase.getRooms(mDb!!, Constant.HOME.toString())
        for (x in dummyRooms) {
            val room = RoomModel()
            room.name = x.roomName
            roomsList.add(room)
        }

        scene = localSqlScene.getSceneByName(mDbScene!!, sceneNameCheck!!, Constant.HOME!!)
        if (!roomsList.isEmpty()) {
            for (x in roomsList) {
                val allDevicesList = ArrayList<Device>()
                val devices = localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, x.name!!)
                var scenRoom = RoomModel()

                for (s in scene!!.room) {
                    if (s.name == x.name) {
                        scenRoom = s
                    }
                    if (!autoCheckScene) {
                        for (d in s.deviceList) {
                            if (d.deviceName == "Automation") {
                                switchAdvance.isChecked = true
                                cvEnableDisable.visibility = View.VISIBLE
                                autoCheckScene = true
                                break
                            }
                        }
                    }

                }
                for (device in devices) {
                    for (i in 0 until device.loads.size) {
                        var flag = false
                        var check = false
                        for (d in scenRoom.deviceList) {
                            if (d.deviceName == device.name && d.index == i) {
                                //if (d.index == i) {
                                d.isSelected = true
                                device.loads[i].curtainState = d.curtainState
                                device.loads[i].brightness = d.dimVal
                                device.loads[i].saturation = d.saturationValue
                                device.loads[i].hue = d.hueValue
                                check = true
                                if (d.isTurnOn) {
                                    flag = true
                                }
                                break
                                //}
                            }
                        }
                        val loadsData = Device(device.loads[i].icon!!, flag, check, 100, device.loads[i].name!!, localSqlDatabase.getRoomForDevice(mDb!!, device.name), device.name, device.loads[i].index!!, device.loads[i].dimmable!!)
                        loadsData.checkType = device.loads[i].type.toString()
                        loadsData.dimVal = device.loads[i].brightness
                        loadsData.saturationValue = device.loads[i].saturation
                        loadsData.hueValue = device.loads[i].hue
                        loadsData.curtainState = device.loads[i].curtainState
                        loadsData.curtainState0 = device.loads[i].curtainState0
                        loadsData.curtainState1 = device.loads[i].curtainState1
                        allDevicesList.add(loadsData)
                    }
                    x.deviceList = allDevicesList
                }
            }
        }
    }

    abstract fun openNextScreen()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnAutomationListInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


}
