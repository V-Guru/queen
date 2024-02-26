package com.wozart.aura.ui.home

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraComplete
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.AuraSwitchLoad
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.adapter.ConfigurationIconsAdapter
import com.wozart.aura.ui.base.IconsAdapter
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import kotlinx.android.synthetic.main.activity_configure_load.*
import kotlinx.coroutines.*

class ConfigureLoadActivity : AppCompatActivity(), ConfigurationIconsAdapter.OnLoadSelected, EspHandler.OnEspHandlerMessage {

    val switch_icons = arrayListOf(R.drawable.ic_bulb_off_new, R.drawable.ic_ceiling_lamp_off_new, R.drawable.ic_bed_lamp_off_new,
            R.drawable.ic_fan_off_, R.drawable.ic_switch_off_new_1, R.drawable.ic_ac_off, R.drawable.ic_exhaust_fan_off, R.drawable.ic_smartplug_off)
    var rgbLightIcons = arrayListOf(R.drawable.ic_bulb_off_new, R.drawable.ic_ceiling_lamp_off_new, R.drawable.ic_bed_lamp_off_new)

    var aura_sense_icon = arrayListOf(R.drawable.ic_ir_blaster, R.drawable.ic_motion_idle, R.drawable.ic_temperature, R.drawable.ic_humidity, R.drawable.ic_light_sensor)

    private lateinit var iconsAdapter: IconsAdapter
    private var roomList: MutableList<String> = ArrayList()
    private var Device: AuraSwitch = AuraSwitch()
    private val localSqlDatabase = DeviceTable()
    private var mDb: SQLiteDatabase? = null
    private lateinit var gson: Gson
    private var LOAD_POS: Int = 0
    private lateinit var UIUD: String
    private var editLoadType: Boolean = false
    private var roomIconSelected: Int = 0
    private var listRoom: MutableList<RoomModelJson> = ArrayList()
    private var aura_sense_device: MutableList<RemoteModel> = ArrayList()
    private var mDbScene: SQLiteDatabase? = null
    private var mDbUtils: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    var roomSelected: String? = null
    private var rulesTableDo = RulesTableHandler()
    private var IP = IpHandler()
    var senseDevice = RemoteModel()
    var allDeviceList = ArrayList<AuraComplete>()
    var IpListDevices: MutableList<IpModel> = ArrayList()
    var notifyData = ""
    var espHandler: EspHandler? = null

    companion object {
        var SENSE_NAME: String? = null
        var SENSE_UIUD: String? = null
        var SENSE_MODE: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_load)
        val deviceString: String = intent.getStringExtra("DEVICE")!!
        gson = Gson()
        Device = gson.fromJson(deviceString, AuraSwitch::class.java)
        UIUD = intent!!.getStringExtra("UIUD") ?: ""
        if (Constant.HOME == null) {
            Constant.HOME = PreferenceManager.getDefaultSharedPreferences(this).getString("HOME", "My Home")
        }
        switchDim.visibility = View.GONE
        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase

        val dbScene = SceneDbHelper(this)
        mDbScene = dbScene.writableDatabase

        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        iconsAdapter = IconsAdapter { pos: Int, Boolean ->

        }
        iconsAdapter.viewTypeIcon = Constant.ICON_VIEW
        loadList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        loadList.adapter = iconsAdapter
        init()
    }

    private fun sendEspHandler(data: String, ip: String, name: String, type: String) {
        try {
            if (espHandler == null) {
                espHandler = EspHandler(this)
            }
            Log.i("_type", "$data")
            espHandler?.getResponseData(data, ip, name, type)
        } catch (e: Exception) {
            Log.d("error", "Error in ESP_Handler_Connection")
        }
    }

    private fun init() {
        allDeviceList = localSqlDatabase.getAllDevicesScenes(mDb!!, Constant.HOME!!)
        aura_sense_device = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        IpListDevices = IP.getIpDevices()
        val senseAvailable = aura_sense_device.filter { it.home == Constant.HOME && it.sense_loads.size > 1 }
        if (senseAvailable.size == 1 && Device.esp_device!!.contains("Sense")) {
            for (device in allDeviceList) {
                for (l in IpListDevices) {
                    if (l.name == device.name && l.owned == 0 && l.home == Constant.HOME) {
                        try {
                            notifyData = JsonHelper().notifyDevice(senseAvailable[0].aura_sence_name.toString(), senseAvailable[0].sense_uiud.toString(), l.uiud!!, Constant.SLAVE)
                            sendEspHandler(notifyData, l.ip!!, l.name!!, "")
                        } catch (e: Exception) {

                        }
                    }
                }
            }
        } else if (senseAvailable.isNotEmpty() && !Device.esp_device!!.contains("Sense")) {
            for (l in this.IpListDevices) {
                if (l.name == Device.name && l.home == Constant.HOME) {
                    try {
                        notifyData = JsonHelper().notifyDevice(senseAvailable[0].aura_sence_name.toString(), senseAvailable[0].sense_uiud.toString(), UIUD, Constant.SLAVE)
                        sendEspHandler(notifyData, l.ip!!, l.name!!, "")
                    } catch (e: Exception) {

                    }
                }
            }
        } else if (senseAvailable.size > 1 && Device.esp_device!!.contains("Sense")) {
            for (l in IpListDevices) {
                if (l.name == Device.name && l.home == Constant.HOME) {
                    try {
                        notifyData = JsonHelper().notifyDevice(senseAvailable[0].aura_sence_name.toString(), senseAvailable[0].sense_uiud.toString(), l.uiud!!, Constant.SLAVE)
                        sendEspHandler(notifyData, l.ip!!, l.name!!, "")
                    } catch (e: Exception) {

                    }

                }
            }
        }

        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        roomList.add(Device.room)
        for (dummy in listRoom) {
            if (dummy.type == "room") {
                if ((dummy.sharedHome == Constant.HOME) or (dummy.sharedHome == "default")) {
                    if (dummy.name != Device.room) {
                        roomList.add(dummy.name)
                    }
                }
            }
        }

        val auraSenseConfigure = AuraSenseConfigure()
        val loads = AuraSwitchLoad()
        roomSelected = Device.room
        val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item_list, roomList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roomlistSpinner!!.adapter = spinnerAdapter

        roomlistSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                roomSelected = Device.room
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                roomSelected = roomlistSpinner.selectedItem.toString()
                roomIconSelected = position
            }
        }

        if (UIUD == "FARGIUIUD") {
            editLoadType = true
        } else {
            if (Device.esp_device!!.contains(Constant.DEVICE_UNIVERSAL_IR)) {
                Device.aura_sense_confi = auraSenseConfigure.universalSenseRemoteIr()
                Device.uiud = UIUD
                selected_load.text = Device.aura_sense_confi[0].displayName
                text_title.text = getString(R.string.configure_universal_ir)
                input_load.visibility = View.GONE
                switchFav.visibility = View.GONE
            } else if (Device.esp_device!!.contains("Sense")) {
                Device.aura_sense_confi = auraSenseConfigure.auraSenseDefault()
                Device.uiud = UIUD
                selected_load.text = Device.aura_sense_confi[0].displayName
                text_title.text = getString(R.string.configure_sense)
                input_load.visibility = View.GONE
                switchFav.visibility = View.GONE
            } else {
                Device.uiud = UIUD
                if (Device.mdl == 2 || Device.mdl == 12 || Device.mdl == 20) {
                    Device.loads = loads.twoModuleLoad(Device.mdl)
                    Device.loads[0].dimmable = false
                } else if (Device.mdl == 11) {
                    Device.loads = loads.twoModuleDimmerLoad(Device.mdl)
                    switchDim.visibility = View.VISIBLE
                    Device.loads[0].name = "Light"
                    input_load.setText(Device.loads[0].name)
                    Device.loads[0].dimmable = true
                } else if (Device.mdl == 1) {
                    Device.loads = loads.auraPlugLoad(Device.mdl)
                    back.visibility = View.INVISIBLE
                    btn_submit.text = getString(R.string.text_submit)
                    Device.loads[0].dimmable = false
                } else if (Device.mdl == 6 || Device.mdl == 23 || Device.mdl == 14) {
                    Device.loads = loads.defaultSwitchProLoadList(Device.mdl)
                    Device.loads[0].dimmable = false
                } else if (Device.mdl == 7 || Device.mdl == 13) {
                    switchDim.visibility = View.VISIBLE
                    back.visibility = View.INVISIBLE
                    btn_submit.text = getString(R.string.text_submit)
                    Device.loads = loads.rgbLoads(Device.mdl)
                } else if (Device.mdl == 8) {
                    switchDim.visibility = View.VISIBLE
                    back.visibility = View.INVISIBLE
                    btn_submit.text = getString(R.string.text_submit)
                    Device.loads = loads.rgbTunableLoads(Device.mdl)
                } else if (Device.mdl == 5) {
                    Device.loads = loads.fiveLoadList(Device.mdl)
                    Device.loads[0].dimmable = false
                } else {
                    Device.loads = loads.defaultLoadList(Device.mdl)
                    Device.loads[0].dimmable = false
                }
                input_load.setText(Device.loads[0].name)
                switchDim.isChecked = Device.loads[0].dimmable!!
                switchFav.isChecked = Device.loads[0].favourite!!
            }
        }

        /*   if (Device.esp_device!!.contains("Sense")) {
               selected_load.text = Device.aura_sense_confi[0].displayName
               text_title.text = getString(R.string.configure_sense)
               input_load.visibility = View.GONE
               switchDim.visibility = View.GONE
               switchFav.visibility = View.GONE
           } else {
               if (Device.mdl == 11) {
                   switchDim.visibility = View.VISIBLE
                   Device.loads[0].name = "Light"
                   input_load.setText(Device.loads[0].name)
                   Device.loads[0].dimmable = true
               } else if (Device.mdl == 8 || Device.mdl == 7 || Device.mdl == 13) {
                   input_load.setText(Device.loads[0].name)
               } else {
                   switchDim.visibility = View.GONE
                   Device.loads[0].name = "Switch 1"
                   input_load.setText(Device.loads[0].name)
                   Device.loads[0].dimmable = false
               }
               switchDim.isChecked = Device.loads[0].dimmable!!
               switchFav.isChecked = Device.loads[0].favourite!!

           }*/

        back.setOnClickListener {
            val finale = false
            val selectedPosition = iconsAdapter.selectedPostion()
            if (Device.esp_device!!.contains(Constant.DEVICE_UNIVERSAL_IR)) {
                Device.aura_sense_confi[LOAD_POS].auraSenseIcon = selectedPosition
                if (LOAD_POS == 0) {
                    back.visibility = View.INVISIBLE
                    btn_submit.text = getString(R.string.text_next)
                } else if (LOAD_POS == 1) {
                    back.visibility = View.INVISIBLE
                    btn_submit.text = getString(R.string.text_next)
                    LOAD_POS--
                }
            } else if (Device.esp_device!!.contains("Sense")) {
                Device.aura_sense_confi[LOAD_POS].auraSenseIcon = selectedPosition
                if (LOAD_POS == 0) {
                    back.visibility = View.INVISIBLE
                    btn_submit.text = getString(R.string.text_next)
                } else if (LOAD_POS == 1) {
                    back.visibility = View.VISIBLE
                    btn_submit.text = getString(R.string.text_next)
                    LOAD_POS--
                } else if (LOAD_POS == 2) {
                    back.visibility = View.VISIBLE
                    btn_submit.text = getString(R.string.text_next)
                    LOAD_POS--
                } else if (LOAD_POS == 3) {
                    back.visibility = View.VISIBLE
                    btn_submit.text = getString(R.string.text_next)
                    LOAD_POS--
                } else if (LOAD_POS == 4) {
                    back.visibility = View.VISIBLE
                    btn_submit.text = getString(R.string.text_next)
                    LOAD_POS--
                }
            } else {
                Device.loads[LOAD_POS].icon = selectedPosition
                if (Device.mdl == 2 || Device.mdl == 12 || Device.mdl == 20) {
                    if (LOAD_POS == 0) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.INVISIBLE
                        btn_submit.text = getString(R.string.text_next)
                    } else if (LOAD_POS == 1) {
                        input_load.setText(Device.loads[LOAD_POS].name)
                        Device.loads[LOAD_POS].dimmable = false
                        switchDim.visibility = View.GONE
                        back.visibility = View.INVISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS--
                    }
                } else if (Device.mdl == 11) {
                    if (LOAD_POS == 0) {
                        input_load.setText(Device.loads[LOAD_POS].name)
                        Device.loads[LOAD_POS].dimmable = true
                        switchDim.visibility = View.VISIBLE
                        back.visibility = View.INVISIBLE
                        btn_submit.text = getString(R.string.text_next)
                    } else if (LOAD_POS == 1) {
                        input_load.setText(Device.loads[LOAD_POS].name)
                        Device.loads[LOAD_POS].dimmable = false
                        switchDim.visibility = View.GONE
                        back.visibility = View.INVISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS--
                    }
                } else if (Device.mdl == 5) {
                    if (LOAD_POS == 0) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.INVISIBLE
                        btn_submit.text = getString(R.string.text_next)
                    } else if (LOAD_POS == 1) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.INVISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS--
                    } else if (LOAD_POS == 2) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS--
                    } else if (LOAD_POS == 3) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS--
                    } else if (LOAD_POS == 4) {
                        input_load.setText(Device.loads[LOAD_POS].name)
                        Device.loads[LOAD_POS].dimmable = false
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS--
                    }
                } else {
                    if (LOAD_POS == 0) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.INVISIBLE
                        btn_submit.text = getString(R.string.text_next)
                    } else if (LOAD_POS == 1) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.INVISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS--
                    } else if (LOAD_POS == 2) {
                        if (Device.mdl != 6) {
                            Device.loads[LOAD_POS].dimmable = false
                            input_load.setText(Device.loads[LOAD_POS].name)
                            switchDim.visibility = View.GONE
                        } else {
                            Device.loads[LOAD_POS].dimmable = true
                            input_load.setText(Device.loads[LOAD_POS].name)
                            switchDim.visibility = View.VISIBLE
                        }
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS--
                    } else if (LOAD_POS == 3) {
                        if (Device.mdl != 6) {
                            input_load.setText(Device.loads[LOAD_POS].name)
                            Device.loads[LOAD_POS].dimmable = false
                            switchDim.visibility = View.GONE
                        } else {
                            Device.loads[LOAD_POS].dimmable = true
                            input_load.setText(Device.loads[LOAD_POS].name)
                            switchDim.visibility = View.VISIBLE
                        }
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS--
                    }
                }
            }
            configureScreenDetails(LOAD_POS, finale)
        }

        btn_submit.setOnClickListener {
            var finale = false
            val selectedPosition = iconsAdapter.selectedPostion()
            if (Device.esp_device!!.contains(Constant.DEVICE_UNIVERSAL_IR)) {
                Device.aura_sense_confi[LOAD_POS].auraSenseIcon = selectedPosition
                finale = true
            } else if (Device.esp_device!!.contains("Sense")) {
                Device.aura_sense_confi[LOAD_POS].auraSenseIcon = selectedPosition
                if (LOAD_POS == 0) {
                    back.visibility = View.VISIBLE
                    btn_submit.text = getString(R.string.text_next)
                    LOAD_POS++
                } else if (LOAD_POS == 1) {
                    back.visibility = View.VISIBLE
                    btn_submit.text = getString(R.string.text_next)
                    LOAD_POS++
                } else if (LOAD_POS == 2) {
                    back.visibility = View.VISIBLE
                    btn_submit.text = getString(R.string.text_next)
                    LOAD_POS++
                } else if (LOAD_POS == 3) {
                    back.visibility = View.VISIBLE
                    btn_submit.text = getString(R.string.text_finish)
                    LOAD_POS++
                } else {
                    finale = true
                }
            } else {
                Device.loads[LOAD_POS].icon = selectedPosition
                if (Device.mdl == 2 || Device.mdl == 12 || Device.mdl == 20) {
                    if (LOAD_POS == 0) {
                        input_load.setText(Device.loads[LOAD_POS].name)
                        Device.loads[LOAD_POS].dimmable = false
                        switchDim.visibility = View.GONE
                        btn_submit.text = getString(R.string.text_finish)
                        LOAD_POS++
                    } else {
                        finale = true
                    }
                } else if (Device.mdl == 11) {
                    if (LOAD_POS == 0) {
                        Device.loads[LOAD_POS].dimmable = true
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_finish)
                        LOAD_POS++
                    } else {
                        finale = true
                    }
                } else if (Device.mdl == 1 || Device.mdl == 7 || Device.mdl == 8 || Device.mdl == 13) {
                    finale = true
                } else if (Device.mdl == 5) {
                    if (LOAD_POS == 0) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS++
                    } else if (LOAD_POS == 1) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS++
                    } else if (LOAD_POS == 2) {
                        input_load.setText(Device.loads[LOAD_POS].name)
                        Device.loads[LOAD_POS].dimmable = false
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS++
                    } else if (LOAD_POS == 3) {
                        input_load.setText(Device.loads[LOAD_POS].name)
                        Device.loads[LOAD_POS].dimmable = false
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_finish)
                        LOAD_POS++
                    } else {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        finale = true
                    }
                }/* else if (Device.mdl == 14) {
                    if (LOAD_POS == 0) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS++
                    } else if (LOAD_POS == 1) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS++
                    } else if (LOAD_POS == 2) {
                        Device.loads[LOAD_POS].dimmable = true
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.VISIBLE
                        finale = true
                    }
                }*/ else {
                    if (LOAD_POS == 0) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS++
                    } else if (LOAD_POS == 1) {
                        Device.loads[LOAD_POS].dimmable = false
                        input_load.setText(Device.loads[LOAD_POS].name)
                        switchDim.visibility = View.GONE
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_next)
                        LOAD_POS++
                    } else if (LOAD_POS == 2) {
                        if (Device.mdl == 6 || Device.mdl == 23 || Device.mdl == 14) {
                            Device.loads[LOAD_POS].dimmable = true
                            input_load.setText(Device.loads[LOAD_POS].name)
                            switchDim.visibility = View.VISIBLE
                        } else {
                            input_load.setText(Device.loads[LOAD_POS].name)
                            Device.loads[LOAD_POS].dimmable = false
                            switchDim.visibility = View.GONE
                        }
                        back.visibility = View.VISIBLE
                        btn_submit.text = getString(R.string.text_finish)
                        LOAD_POS++
                    } else {
                        if (Device.mdl != 6) {
                            Device.loads[LOAD_POS].dimmable = false
                            switchDim.visibility = View.GONE
                        } else {
                            switchDim.visibility = View.VISIBLE
                            Device.loads[LOAD_POS].dimmable = true
                            input_load.setText(Device.loads[LOAD_POS].name)
                        }
                        finale = true
                    }
                }
            }
            configureScreenDetails(LOAD_POS, finale)
        }

        switchFav?.setOnCheckedChangeListener { _, isChecked ->
            Device.loads[LOAD_POS].favourite = isChecked
        }

        switchDim?.setOnCheckedChangeListener { _, isChecked ->
            Device.loads[LOAD_POS].dimmable = isChecked
        }

        input_load.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!p0.isNullOrEmpty()) {
                    Device.loads[LOAD_POS].name = p0.trim().toString()
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        if (Device.mdl == 7 || Device.mdl == 8 || Device.mdl == 11 || Device.mdl == 9 || Device.mdl == 13) {
            generateIcons(0)
        } else if (Device.mdl == 1) {
            generateIcons(7)
        } else {
            generateIcons(4)
        }
        if (Device.mdl == 9 || Device.mdl == 15) {
            selected_load.text = Device.aura_sense_confi[0].auraSenseName
        } else {
            selected_load.text = "Load 1"
        }
    }

    private fun generateIcons(pos: Int) {
        if (Device.esp_device!!.contains("Sense")) {
            iconsAdapter.init(aura_sense_icon, pos)
        } else if (Device.esp_device!!.contains(Constant.DEVICE_UNIVERSAL_IR)) {
            iconsAdapter.init(aura_sense_icon.filter { it == R.drawable.ic_ir_blaster } as MutableList<Int>, pos)
        } else if (Device.mdl == 7 || Device.mdl == 8 || Device.mdl == 13) {
            iconsAdapter.init(rgbLightIcons, pos)
        } else {
            iconsAdapter.init(switch_icons, pos)
        }
    }

    override fun onLoadCallback(load: Int) {
        LOAD_POS = load

    }

    private fun configureScreenDetails(position: Int, save: Boolean) {
        if (Device.esp_device!!.contains(Constant.DEVICE_UNIVERSAL_IR) || Device.esp_device!!.contains("Sense")) {
            iconsAdapter.selectIconPosition(Device.aura_sense_confi[position].auraSenseIcon)
            selected_load.text = Device.aura_sense_confi[position].displayName
            if (save) {
                progress_layout.visibility = View.VISIBLE
                for (senseDevice in aura_sense_device) {
                    if (senseDevice.aura_sence_name == Device.name) {
                        senseDevice.room = roomSelected.toString()
                        this.senseDevice = senseDevice
                        break
                    }
                }

                val homeSenseList = aura_sense_device.filter { it.home == Constant.HOME }
                if (homeSenseList.size == 1) {
                    senseDevice.role = Constant.MASTER
                    aura_sense_device[0].role = Constant.MASTER
                }
                localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", aura_sense_device)
                updateToCloud()
            }
        } else {
            input_load.setText(Device.loads[position].name)
            switchDim.isChecked = Device.loads[position].dimmable!!
            switchFav.isChecked = Device.loads[position].favourite!!
            iconsAdapter.selectIconPosition(Device.loads[position].icon!!)
            selected_load.text = "Load " + (position + 1)

            if (save) {
                progress_layout.visibility = View.VISIBLE
                if (Device.thing == null) {
                    localSqlDatabase.insertDevice(mDb!!, Constant.HOME!!, roomSelected!!, Device.uiud, Device.name, gson.toJson(Device.loads), "null")
                } else {
                    localSqlDatabase.insertDevice(mDb!!, Constant.HOME!!, roomSelected!!, Device.uiud, Device.name, gson.toJson(Device.loads), Device.thing!!)
                }
                updateToCloud()
            }
        }
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        runOnUiThread {

        }
    }

    private fun updateToCloud() {
        MainScope().launch {
            Device.room = roomSelected.toString()
            CoroutineScope(Dispatchers.IO).launch {
                if (Device.esp_device!!.contains("Sense")) {
                    rulesTableDo.insertSenseDevice(senseDevice)
                } else {
                    if(Device.mdl == 15) rulesTableDo.insertUniversalDevice(Device) else rulesTableDo.insertDeviceLoads(Device)
                }
                withContext(Dispatchers.Main) {
                    progress_layout.visibility = View.GONE
                    val intent = Intent(applicationContext, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

    }

}
