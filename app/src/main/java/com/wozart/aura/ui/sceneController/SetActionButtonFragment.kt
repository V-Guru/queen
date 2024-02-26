package com.wozart.aura.ui.sceneController

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.sqlLite.AuraButtonTable
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.entity.sql.buttonDevice.ButtonDbHelper
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.ui.adapter.AutomationSceneActionAdapter
import com.wozart.aura.ui.base.basesetactions.BaseSetActionsFragment
import com.wozart.aura.ui.base.basesetactions.SetActionsRoomAdapter
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.GridAutoFitLayoutManager
import com.wozart.aura.ui.dashboard.GridListSpacingItemDecoration
import com.wozart.aura.ui.dashboard.Scenes
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.fragment_automation_select_device.*
import kotlin.concurrent.thread


/**
 * Created by Saif on 12/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class SetActionButtonFragment : BaseSetActionsFragment(), RecyclerItemClicked {


    private val localSqlDatabase = DeviceTable()
    private var localSqlButton = AuraButtonTable()
    private var mDbButton : SQLiteDatabase ?= null
    private var mDb: SQLiteDatabase? = null
    private var adapter = SetActionsRoomAdapter()
    lateinit var sceneAdapter: AutomationSceneActionAdapter
    private var sceneToActive: MutableList<RoomModel> = ArrayList()
    var rulesTableHandler = RulesTableHandler()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_automation_select_device, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setAdapter()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun initialize(){
        rlSpinner.visibility = View.GONE
        val dbHelper = DeviceDbHelper(context!!)
        mDb = dbHelper.writableDatabase

        val dbButton = ButtonDbHelper(context!!)
        mDbButton = dbButton.writableDatabase

        sceneAdapter = AutomationSceneActionAdapter(activity!!,this)
        listRooms.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)

        if(buttonModel.sceneSelectedList.size > 0){
            tvSelectScene.visibility = View.VISIBLE
            listScenesdata.layoutManager = GridAutoFitLayoutManager(context, resources.getDimensionPixelSize(R.dimen.device_item_size))
            listScenesdata.setHasFixedSize(true)
            val spacing = resources.getDimensionPixelSize(R.dimen.uniform_half_spacing)
            listScenesdata.addItemDecoration(GridListSpacingItemDecoration(spacing))
        }
    }

    private fun setAdapter(){
        listRooms.adapter = adapter
        adapter.init(buttonModel.load)
        listScenesdata.adapter = sceneAdapter
        sceneAdapter.dataSet(buttonModel.sceneSelectedList)
    }

    override fun onFinish() {
        progress_layout.visibility = View.VISIBLE
        if(buttonModel.load.size == 0){
            if (sceneToActive.size > 0) {
                buttonModel.load = sceneToActive
            }else{
                for (s in buttonModel.sceneSelectedList) {
                    val model = RoomModel()
                    model.name = s.rooms[0].name
                    model.deviceList.add(Device(data.iconUrl, s.isOn, true, 100, s.title.toString(), model.name!!, "Scene", 0, false))
                    sceneToActive.add(model)
                }
                buttonModel.load = sceneToActive
            }
        }
//        for (buttonDevice in buttonModel.load) {
//            for (device in buttonDevice.deviceList) {
//                var flag = false
//                for (r in roomNameList) {
//                    if (r == device.roomName) {
//                        flag = true
//                        break
//                    }
//                }
//                if (!flag) {
//                    roomNameList.add(device.roomName)
//                }
//            }
//            if (sceneToActive.size > 0) {
//                updateDeviceList(buttonDevice)
//            }
//            if (sceneToActive.size == 0 && buttonModel.sceneSelectedList.size > 0) {
//                for (s in buttonModel.sceneSelectedList) {
//                    val model = RoomModel()
//                    model.name = s.rooms[0].name
//                    model.deviceList.add(Device(data.iconUrl, s.isOn, true, 100, s.title.toString(), model.name!!, "Scene", 0, false))
//                    sceneToActive.add(model)
//                }
//                updateDeviceList(buttonDevice)
//            }
//        }

        localSqlButton.insertButtonDevice(mDbButton!!, buttonModel.auraButtonName!!,buttonModel.buttonId!!,buttonModel.buttonTapName!!,buttonModel.unicastAddress!!,Gson().toJson(buttonModel.load),Constant.HOME!!.trim(),buttonModel.room!!,buttonModel.senseUiud!!,buttonModel.thing.toString(), buttonModel.senseName!!)
            thread{
               rulesTableHandler.storeSceneControllerLoad(buttonModel)
                requireActivity().runOnUiThread {
                    sceneToActive.clear()
                    buttonModel.load.clear()
                    buttonModel.sceneSelectedList.clear()
                    buttonModel.buttonId = ""
                    progress_layout.visibility = View.GONE
                    val intent = Intent(activity, SceneButtonActivity::class.java)
                    intent.putExtra(Constant.PARAM_FRAGMENT,Constant.SCENE_BUTTON_FRAGMENT)
                    intent.putExtra(Constant.BUTTON_DEVICE_DATA,Gson().toJson(buttonModel))
                    startActivity(intent)
                    activity?.finishAffinity()
                }
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

    private fun updateDeviceList(automation: RoomModel){
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

}