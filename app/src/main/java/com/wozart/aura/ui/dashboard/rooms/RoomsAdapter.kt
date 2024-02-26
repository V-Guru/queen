package com.wozart.aura.ui.dashboard.rooms

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.executor.GlideExecutor.UncaughtThrowableStrategy.LOG
import com.google.gson.Gson
import com.warkiz.widget.IndicatorSeekBar
import com.wozart.aura.R
import com.wozart.aura.aura.ui.dashboard.rooms.AddRoomActivity
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.model.Room
import com.wozart.aura.data.sqlLite.*
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aws.AwsState
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.ui.adapter.ButtonDeviceAdapter
import com.wozart.aura.ui.auraSense.RemoteListAdapter
import com.wozart.aura.ui.auraSense.RemoteListModel
import com.wozart.aura.ui.auraSense.SenseItemAdapter
import com.wozart.aura.ui.createscene.CreateSceneActivity
import com.wozart.aura.ui.dashboard.*
import com.wozart.aura.ui.dashboard.home.DeviceAddActivity
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.room.RoomActivity
import com.wozart.aura.ui.sceneController.SceneButtonActivity
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.dialog_configure_edit_dimming.*
import kotlinx.android.synthetic.main.dialogue_edit_home.*
import kotlinx.android.synthetic.main.item_room.view.*


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 09/08/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0.2
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class RoomsAdapter(private var context: Context, var onClick: RecyclerItemClicked, onLoadPressed: OnLoadPressedListiner,
                   onScenePressed: OnScenePressedListener, private val roomsList: ArrayList<Room>) : androidx.recyclerview.widget.RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>(),
        RemoteListAdapter.OnRemoteDelete, RecyclerItemClicked {

    private var localSqlRemote = AuraSenseTable()
    private var mdbSense: SQLiteDatabase? = null
    private var loadListener: OnLoadPressedListiner = onLoadPressed
    private var sceneListener: OnScenePressedListener = onScenePressed
    private var jsonHelper: JsonHelper = JsonHelper()
    private var allDevicesList = ArrayList<Device>()
    private var deviceAdapter: DevicesAdapter? = null
    private var sceneAdapter: RoomScenesAdapter? = null
    var usertype = true
    var deviceError: Int = -1
    var checkInternetFlag = false
    var sceneList = ArrayList<Scenes>()
    var sense_device_list: MutableList<RemoteModel> = ArrayList()
    var sense_load_adapter: SenseItemAdapter? = null
    var loads: MutableList<AuraSenseConfigure> = ArrayList()
    var remoteListData: MutableList<RemoteListModel> = ArrayList()
    var presentRemote: MutableList<RemoteListModel> = ArrayList()
    lateinit var remoteListAdapter: RemoteListAdapter
    var flagExist = false
    var buttonDeviceList: MutableList<ButtonModel> = ArrayList()
    lateinit var buttonAdapter: ButtonDeviceAdapter
    private var IP = IpHandler()


    fun setData(deviceList: ArrayList<Device>, sceneList: MutableList<Scenes>, buttonDeviceList: MutableList<ButtonModel>, types: Boolean) {
        this.sceneList.clear()
        this.buttonDeviceList.clear()
        this.allDevicesList.clear()
        this.allDevicesList.addAll(deviceList)
        this.sceneList.addAll(sceneList)
        this.buttonDeviceList.addAll(buttonDeviceList)
        this.usertype = types
        notifyDataSetChanged()
    }


    fun updateScene(scene: Scenes) {
        for (scenes in sceneList) {
            if (scenes.title == scene.title)
                scenes.isOn = scene.isOn
        }
        sceneAdapter!!.notifyDataSetChanged()
    }

    fun updateAppearance(IpListDevices: MutableList<IpModel>) {
        for (ip in IpListDevices) {
            if (ip.room == roomsList[0].roomName) {
                for (device in allDevicesList) {
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
                        if (ip.module == 2 || ip.module == 12) {
                            device.isTurnOn = ip.twoModuleState[device.index]
                            device.dimmable = false
                        } else if (ip.module == 11) {
                            device.isTurnOn = ip.twoModuleState[device.index]
                            device.dimVal = ip.twoModuleDim[device.index]
                        } else if (ip.module == 1) {
                            device.isTurnOn = ip.auraPlugState[0]
                            device.dimmable = false
                        } else if (ip.module == 3) {
                            device.isTurnOn = false
                            device.dimmable = false
                        } else if (ip.module == 7 || ip.module == 8) {
                            device.isTurnOn = ip.auraPlugState[0]
                            device.dimVal = ip.brightness
                            device.hueValue = ip.hue
                            device.saturationValue = ip.saturation
                            device.tempValue = ip.tempLight
                            device.dimmable = true
                        } else {
                            device.isTurnOn = ip.state[device.index]
                            device.dimVal = ip.dim[device.index]
                        }
                    }
                }
            }
            if (deviceAdapter != null) {
                deviceAdapter?.notifyDataSetChanged()
            }

        }

        for (scene in sceneList) {
            var sceneTurnedState = true
            for (sRoom in scene.rooms) {
                for (sDevice in sRoom.deviceList) {
                    for (ip in IpListDevices) {
                        if (sDevice.deviceName == ip.name) {
                            if (ip.module == 2 || ip.module == 12 || ip.module == 11) {
                                if (sDevice.isTurnOn != ip.twoModuleState[sDevice.index]) {
                                    sceneTurnedState = false
                                    break
                                }
                            } else if (ip.module == 1) {
                                if (sDevice.isTurnOn != ip.auraPlugState[0]) {
                                    sceneTurnedState = false
                                    break
                                }
                            } else if (ip.module == 7 || ip.module == 8) {
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
                                if (sDevice.curtainState0 != ip.curtainState[0] || sDevice.curtainState1 != ip.curtainState[1]) {
                                    sceneTurnedState = false
                                    break
                                }
                            } else if (ip.module == 6 || ip.module == 14) {
                                if (sDevice.isTurnOn != ip.state[sDevice.index] || sDevice.dimVal != ip.dim[sDevice.index]) {
                                    sceneTurnedState = false
                                    break
                                }
                            } else {
                                if (sDevice.isTurnOn != ip.state[sDevice.index]) {
                                    sceneTurnedState = false
                                    break
                                }
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
        sceneAdapter?.notifyDataSetChanged()
    }

    fun updateState(message: String, IpListDevices: MutableList<IpModel>): MutableList<IpModel> {
        if (message.contains("ERROR")) {
            var deviceErrorNameCheck: String = ""
            deviceError = -1
            val data = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            deviceErrorNameCheck = data[1]
            if (deviceError != 0) {
                for (l in IpListDevices) {
                    if (l.name == deviceErrorNameCheck) {
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
                                if (context is RoomActivity) {
                                    (context as RoomActivity).pusblishDataToShadow(l.thing!!, jsonHelper.serializeLEDData())
                                }
                            }
                        }
                        IP.registerIpDevice(l)
                        break
                    }
                }
            }
        } else {
            val updatedDevice: AuraSwitch = jsonHelper.deserializeTcp(message)
            deviceError = updatedDevice.error
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
                                l.local = true
                                l.aws = false
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
                                break
                            }
                        }
                    } else {
                        for (l in IpListDevices) {
                            if (l.name == updatedDevice.name) {
                                if (updatedDevice.state.size == 2 || updatedDevice.mdl == 2 || updatedDevice.mdl == 12 || updatedDevice.mdl == 11 || updatedDevice.mdl == 20) {
                                    l.local = true
                                    l.twoModuleState[0] = updatedDevice.state[0] == 1
                                    l.twoModuleState[1] = updatedDevice.state[1] == 1
                                    l.twoModuleDim[0] = updatedDevice.dim[0]
                                    l.twoModuleDim[1] = updatedDevice.dim[1]
                                    for (i in 0..3) {
                                        l.condition[i] = "ready"
                                    }
                                    l.aws = false
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
                                    l.local = true
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
                                    for (i in 0..4) {
                                        l.condition[i] = "ready"
                                    }
                                    l.aws = false
                                } else {
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
                                break
                            }
                        }
                    }
                }
                5 -> {
                    for (motion in sense_device_list) {
                        if (motion.aura_sence_name == updatedDevice.name) {
                            for (loads in motion.sense_loads) {
                                if (loads.auraSenseName == "Humidity") {
                                    loads.intensity = updatedDevice.humid
                                    Constant.HUMIDITY = updatedDevice.humid
                                } else if (loads.auraSenseName == "Temperature") {
                                    loads.intensity = updatedDevice.temp
                                    Constant.TEMP = updatedDevice.temp
                                } else if (loads.auraSenseName == "Light Intensity") {
                                    loads.intensity = updatedDevice.ldr
                                    Constant.LUX = updatedDevice.ldr
                                } else if (loads.auraSenseName == "Motion") {
                                    loads.intensity = updatedDevice.motion
                                }
                            }
                            sense_load_adapter?.notifyDataSetChanged()
                            break
                        }
                    }
                }
                6 -> {
                    for (motion in sense_device_list) {
                        if (motion.aura_sence_name == updatedDevice.name) {
                            for (loads in motion.sense_loads) {
                                if (loads.auraSenseName == "Motion") {
                                    loads.intensity = updatedDevice.motion
                                } else if (loads.auraSenseName == "Humidity") {
                                    loads.intensity = updatedDevice.humid
                                    Constant.HUMIDITY = updatedDevice.humid
                                } else if (loads.auraSenseName == "Temperature") {
                                    loads.intensity = updatedDevice.temp
                                    Constant.TEMP = updatedDevice.temp
                                } else if (loads.auraSenseName == "Light Intensity") {
                                    loads.intensity = updatedDevice.ldr
                                    Constant.LUX = updatedDevice.ldr
                                }
                            }
                            sense_load_adapter?.notifyDataSetChanged()
                            break
                        }
                    }
                }
                13, 14 -> {
                    if (updatedDevice.error == 0) {

                    }
                }
                else -> {
                    for (l in IpListDevices) {
                        if (l.name == updatedDevice.name) {
                            if (updatedDevice.state.size == 2 || updatedDevice.mdl == 2 || updatedDevice.mdl == 12 || updatedDevice.mdl == 11 || updatedDevice.mdl == 20) {
                                l.twoModuleState[0] = updatedDevice.state[0] == 1
                                l.twoModuleState[1] = updatedDevice.state[1] == 1
                                l.twoModuleDim[0] = updatedDevice.dim[0]
                                l.twoModuleDim[1] = updatedDevice.dim[1]
                                l.local = true
                                l.aws = false
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
                                l.local = true
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
                                for (i in 0..3) {
                                    l.condition[i] = "ready"
                                }
                                l.aws = false
                            } else {
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
                            break
                        }
                    }
                }

            }

        }
        if (deviceAdapter != null) {
            deviceAdapter!!.notifyDataSetChanged()
        }
        return IpListDevices
    }

    fun updateSenseAws(data: AwsState, device: String) {
        if (data.led != 0) {
            val convertState = jsonHelper.convertMotionState(Gson().toJson(data.state))
            for (s in sense_device_list) {
                if (s.sense_uiud == data.uiud) {
                    s.sense_loads[1].intensity = convertState.motion!!.get(0)
                    s.sense_loads[2].intensity = convertState.temp!!.get(1)
                    Constant.TEMP = convertState.temp!!.get(1)
                    s.sense_loads[3].intensity = convertState.humid!!.get(1)
                    Constant.HUMIDITY = convertState.humid!!.get(1)
                    s.sense_loads[4].intensity = convertState.lux!!.get(0)
                    Constant.LUX = convertState.lux!!.get(0)
                    sense_load_adapter?.notifyDataSetChanged()
                    break
                }
            }
        }
    }


    fun updateStateFromAws(device: String, data: AwsState, IpListDevices: MutableList<IpModel>): MutableList<IpModel> {
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
                            } else if (l.module == 1) {
                                if (!l.local) {
                                    l.aws = true
                                    l.condition[0] = "ready"
                                    l.auraPlugState[0] = data.state["s0"] == 1.0
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
                                    l.curtainState[0] = if (data.state["s0"] == 1.0) 1 else 0
                                    l.curtainState[1] = if (data.state["s1"] == 1.0) 1 else 0
                                }
                            } else if (l.module == 7 || l.module == 8 || l.module == 13) {
                                if (data.uiud == l.uiud) {
                                    if (!l.local) {
                                        l.aws = true
                                        for (i in 0..3) {
                                            l.failure[i] = 0
                                            l.condition[i] = "ready"
                                        }
                                        l.auraPlugState[0] = data.state["s0"] == 1.0
                                        l.saturation = data.saturation
                                        l.hue = data.hue
                                        l.tempLight = data.temperature
                                        l.brightness = data.dim["d0"] ?: data.brightness
                                        l.failure[0] = 0
                                        l.curnLoad[0] = false
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

                                }
                            } else {
                                if (!l.local) {
                                    l.aws = true
                                    for (i in 0..3) {
                                        l.condition[i] = "ready"
                                    }
                                    if (data.dim["d0"] != null) {
                                        l.dim[0] = data.dim["d0"]!!
                                        l.dim[1] = data.dim["d1"]!!
                                        l.dim[2] = data.dim["d2"]!!
                                        l.dim[3] = data.dim["d3"]!!
                                    }
                                    for (i in 0..3) {
                                        l.state[i] = data.state["s$i"] == 1.0
                                        l.failure[i] = 0
                                        l.curnLoad[i] = false
                                    }
                                }
                            }
                        }
                    }
                    break
                }
            }
        } else {
            for (l in IpListDevices) {
                if (l.name == device) {
                    if (!l.local) {
                        if (!l.aws) {
                            if (!checkInternetFlag) {
                                for (i in 0..3) {
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
                                for (i in 0..3) {
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
                                    } else {
                                        l.aws = false
                                        l.failure[i] = 0
                                        l.condition[i] = "update"
                                    }

                                }
                            }
                        }
                    }
                    break
                }
            }
        }
        return IpListDevices
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        val remoteDbHelper = AuraSenseDbHelper(parent.context)
        mdbSense = remoteDbHelper.writableDatabase
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(roomsList[position])

    }

    override fun getItemCount(): Int {
        return roomsList.size
    }

    inner class RoomViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(room: Room) = with(itemView) {
            roomTitleTv.text = room.roomName

            if (buttonDeviceList.size > 0) itemView.rlButton.visibility = View.VISIBLE else View.GONE

            var noDeviceFlag = false
            if (allDevicesList.isEmpty() || allDevicesList.size == 0) {
                noDeviceFlag = true
                textView2.visibility = View.GONE
                btn_add_scene.visibility = View.GONE
            }

            btn_add_accessory.setOnClickListener {
                if (usertype) {
                    val intent = Intent(context, DeviceAddActivity::class.java)
                    context.startActivity(intent)
                } else {
                    SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(context.getString(R.string.not_access_to_add)).show()
                }
            }

            if (sceneList.size == 0 && !noDeviceFlag) {
                btn_add_scene.visibility = View.VISIBLE
            }

            btn_add_scene.setOnClickListener {
                if (usertype) {
                    val intent = Intent(context, CreateSceneActivity::class.java)
                    intent.putExtra("inputSceneType", "create")
                    intent.putExtra("inputSceneName", "")
                    intent.putExtra("inputSceneIconUrl", 0)
                    intent.putExtra(Constant.ROOM_NAME, room.roomName)
                    intent.putExtra(Constant.PARAM_FRAGMENT, Constant.ROOMS_FRAGMENT)
                    context.startActivity(intent)
                } else {
                    SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(context.getString(R.string.not_access_to_add)).show()
                }
            }

            edit_room_option.setOnClickListener {
                showPopup(edit_room_option, room.roomName!!)
            }
            Log.d("ERROR","method being executed")
            for (sense_device in sense_device_list.filter { it.room == room.roomName && it.home == Constant.HOME }) {
                itemView.sense_layout.visibility = View.VISIBLE
                remoteListData = localSqlRemote.getRemoteList(mdbSense!!, sense_device.aura_sence_name!!)
                for (loads_data in sense_device.sense_loads) {
                    val loads_list = AuraSenseConfigure()
                    loads_list.auraSenseName = loads_data.auraSenseName
                    loads_list.auraSenseIcon = loads_data.auraSenseIcon
                    loads_list.auraSenseIndex = loads_data.auraSenseIndex
                    loads_list.auraSenseFavorite = loads_data.auraSenseFavorite
                    loads_data.senseDeviceName = sense_device.aura_sence_name
                    loads.add(loads_data)

                }
                if (remoteListData.size > 0) {
                    for (remote in remoteListData) {
                        flagExist = true
                        val remoteModel = RemoteListModel()
                        remoteModel.auraSenseName = remote.auraSenseName
                        remoteModel.modelNumber = remote.modelNumber
                        remoteModel.brandName = remote.brandName
                        remoteModel.remoteName = remote.remoteName
                        remoteModel.senseUiud = sense_device.sense_uiud
                        remoteModel.senseIp = sense_device.sense_ip
                        remoteModel.typeAppliances = remote.typeAppliances
                        remoteModel.dynamicRemoteIconList = remote.dynamicRemoteIconList
                        remoteModel.remoteFavourite = remote.remoteFavourite
                        remoteModel.remoteLocation = remote.remoteLocation
                        remoteModel.senseThing = sense_device.scense_thing
                        presentRemote.add(remoteModel)

                    }
                }
            }

            if (presentRemote.size > 0) {
                itemView.rlRemote.visibility = View.VISIBLE
                remoteListAdapter = RemoteListAdapter(context, presentRemote, flagExist, this@RoomsAdapter)
                itemView.remote_list.adapter = remoteListAdapter
            }

            sense_load_adapter = SenseItemAdapter(context!!, loads, sense_device_list)
            val spacing_sense_load = resources.getDimensionPixelSize(R.dimen.uniform_half_spacing)
            sense_load.layoutManager = GridAutoFitLayoutManager(context, resources.getDimensionPixelSize(R.dimen.tiles_motion_hight))
            sense_load.setHasFixedSize(true)
            sense_load.addItemDecoration(GridListSpacingItemDecoration(spacing_sense_load))
            sense_load.adapter = sense_load_adapter

            buttonAdapter = ButtonDeviceAdapter(context, this@RoomsAdapter)
            rvButtonDevice.layoutManager = GridAutoFitLayoutManager(context, resources.getDimensionPixelSize(R.dimen.device_item_size))
            rvButtonDevice.setHasFixedSize(true)
            rvButtonDevice.addItemDecoration(GridListSpacingItemDecoration(spacing_sense_load))
            rvButtonDevice.adapter = buttonAdapter
            buttonAdapter.setData(buttonDeviceList)

            sceneAdapter = RoomScenesAdapter(scenesList = sceneList) { scenes: Scenes, isLongPressed: Boolean ->
                if (isLongPressed) {
                    if (usertype) {
                        dialogueEditRoomScene(scenes, room.roomName!!)
                    } else {
                        SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(context.getString(R.string.can_not_edit)).show()
                    }
                } else {
                    sceneListener.onScenePressed(scenes)
                }
            }
            scenesRv.adapter = sceneAdapter

            if (noDeviceFlag) {
                viewAllDevicesTv.visibility = View.INVISIBLE
                btn_add_accessory.visibility = View.VISIBLE
            } else {
                viewAllDevicesTv.visibility = View.INVISIBLE
                btn_add_accessory.visibility = View.INVISIBLE

                deviceAdapter = DevicesAdapter(allDevicesList, room.roomName!!) { device: Device, isLongPressed: Boolean ->
                    if (isLongPressed) {
                        if (device.fullDeviceName!!.contains(context.getString(R.string.aura_curtain_product)) || device.checkType == "Curtain") {
                            onClick.onRecyclerItemClicked(adapterPosition, device, Constant.CURTAIN)
                        } else if (device.checkType == "rgbDevice") {
                            onClick.onRecyclerItemClicked(adapterPosition, device, Constant.RGB)
                        } else if (device.checkType == "tunableDevice") {
                            onClick.onRecyclerItemClicked(adapterPosition, device, Constant.RGB_TUNNABLE)
                        } else {
                            if (!device.dimmable) {
                                if (usertype) {
                                    val gson = Gson()
                                    val intent = Intent(context, EditLoadActivity::class.java)
                                    intent.putExtra("DEVICE", gson.toJson(device))
                                    intent.putExtra("START_ROOM", "RoomDevice")
                                    intent.putExtra(Constant.ROOM_NAME, room.roomName!!)
                                    intent.putExtra(Constant.PARAM_FRAGMENT, Constant.ROOMS_FRAGMENT)
                                    context.startActivity(intent)
                                } else {
                                    SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(context.getString(R.string.can_not_edit)).show()
                                }

                            } else if ((device.fullDeviceName!!.contains("AuraSwitch Pro") || device.fullDeviceName!!.contains("Wozart Switch Controller Pro") || device.fullDeviceName!!.contains("Fan")) && device.index == 2) {
                                onClick.onRecyclerItemClicked(adapterPosition, device, Constant.FAN_DIM)
                            } else {
                                openDimmingDialog(context, device, room.roomName!!)
                            }
                        }

                    } else {
                        if (device.checkType == "rgbDevice") {
                            onClick.onRecyclerItemClicked(adapterPosition, device, Constant.RGB_LOADS)
                        } else if (device.checkType == "tunableDevice") {
                            onClick.onRecyclerItemClicked(adapterPosition, device, Constant.TUNBLE_LOADS)
                        } else {
                            loadListener.onLoadPressed(device, false)
                        }
                    }

                }

            }
            val spacing = resources.getDimensionPixelSize(R.dimen.uniform_half_spacing)
            fudRv.adapter = deviceAdapter
            fudRv.layoutManager = GridAutoFitLayoutManager(context, resources.getDimensionPixelSize(R.dimen.device_item_size))
            fudRv.setHasFixedSize(true)
            fudRv.addItemDecoration(GridListSpacingItemDecoration(spacing))
        }
    }


    interface OnLoadPressedListiner {
        fun onLoadPressed(auraDevice: Device, longPressed: Boolean)
    }

    fun showPopup(editRoomOption: ImageView, roomName: String) {
        val popup = context.let { PopupMenu(it, editRoomOption) }
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_rooms, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_favourite -> {
                    if (usertype) {
                        val intent = Intent(context, AddRoomActivity::class.java)
                        intent.putExtra("NAME_ROOM", roomName)
                        intent.putExtra("ROOM_EDIT_TYPE", "edit")
                        intent.putExtra(Constant.PARAM_FRAGMENT, Constant.ROOMS_FRAGMENT)
                        context.startActivity(intent)
                    } else {
                        SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(context.getString(R.string.can_not_edit)).show()
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun dialogueEditRoomScene(scenes: Scenes, roomName: String) {
        val dialogue = Dialog(context)
        dialogue.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogue.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogue.setContentView(R.layout.dialogue_edit_home)
        dialogue.tv_title.text = context.getString(R.string.edit_scene_delete)
        dialogue.btn_edit.setOnClickListener {
            val intent = Intent(context, CreateSceneActivity::class.java)
            intent.putExtra("inputSceneType", "edit")
            intent.putExtra("inputSceneName", scenes.title)
            intent.putExtra("inputSceneIconUrl", scenes.iconUrl)
            intent.putExtra(Constant.ROOM_NAME, roomName)
            intent.putExtra(Constant.PARAM_FRAGMENT, Constant.ROOMS_FRAGMENT)
            context.startActivity(intent)
        }
        dialogue.btn_delete_home.setOnClickListener {
            onClick.onRecyclerItemClicked(scenes.iconUrl, scenes, Constant.SCENE_DELETE)
            dialogue.dismiss()
        }
        dialogue.show()
    }


    interface OnScenePressedListener {
        fun onScenePressed(scene: Scenes)
    }

    private fun openDimmingDialog(context: Context, auraDevice: Device, roomName: String) {
        val dialog = Dialog(context)
        var dimVal = auraDevice.dimVal
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_configure_edit_dimming)
        if (auraDevice.name == "Fan" && auraDevice.isTurnOn) {
            Glide.with(context).load(Utils.getIconDrawable(auraDevice.type, auraDevice.isTurnOn))
                    .into(dialog.iconDevice)
        } else {
            dialog.iconDevice.setImageResource(Utils.getIconDrawable(auraDevice.type, auraDevice.isTurnOn))
        }
        dialog.tvDialogTitle.text = String.format(context.getString(R.string.text_configure_device_dim), auraDevice.name)
        dialog.rangeBarDim.setOnRangeBarChangeListener { _, _, _, _, rightPinValue ->
            dimVal = Integer.parseInt(rightPinValue)
        }
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
                auraDevice.dimVal = dimVal
                loadListener.onLoadPressed(auraDevice, true)
            }
        })

        dialog.btnEdit.setOnClickListener {
            if (usertype) {
                val gson = Gson()
                val intent = Intent(context, EditLoadActivity::class.java)
                intent.putExtra("DEVICE", gson.toJson(auraDevice))
                intent.putExtra("START_ROOM", "RoomDevice")
                intent.putExtra(Constant.ROOM_NAME, roomName)
                intent.putExtra(Constant.PARAM_FRAGMENT, Constant.ROOMS_FRAGMENT)
                context.startActivity(intent)
            } else {
                SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(context.getString(R.string.can_not_edit)).show()
            }
        }
        dialog.btnDone.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }


    override fun onRemoteDelete(remoteListModel: RemoteListModel, position: Int) {
        onClick.onRecyclerItemClicked(position, remoteListModel, Constant.REMOTE_DELETE)
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is ButtonModel) {
            when (viewType) {
                Constant.SET_BUTTON_ACTION -> {
                    val intent = Intent(context, SceneButtonActivity::class.java)
                    intent.putExtra(Constant.PARAM_FRAGMENT, Constant.SCENE_BUTTON_FRAGMENT)
                    intent.putExtra(Constant.BUTTON_DEVICE_DATA, Gson().toJson(data))
                    context.startActivity(intent)
                }
                Constant.BUTTON_DELETE -> {
                    onClick.onRecyclerItemClicked(position, data, Constant.BUTTON_DELETE)
                }
            }

        }
    }

}

