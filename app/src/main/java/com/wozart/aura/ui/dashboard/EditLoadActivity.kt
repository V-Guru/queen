package com.wozart.aura.ui.dashboard

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.AuraSwitchLoad
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.base.IconsAdapter
import com.wozart.aura.ui.dashboard.home.HomeFragment
import com.wozart.aura.ui.dashboard.room.RoomActivity
import com.wozart.aura.ui.dashboard.rooms.RoomsFragment
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.activity_edit_load.*
import kotlinx.android.synthetic.main.layout_header.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class EditLoadActivity : AppCompatActivity() {

    private var load: AuraSwitchLoad = AuraSwitchLoad()
    var auraDevice = AuraSwitch()
    private val localSqlDatabase = DeviceTable()
    private var mDb: SQLiteDatabase? = null
    private var mDbScene: SQLiteDatabase? = null
    private var mDbUtils: SQLiteDatabase? = null
    lateinit var loadAdapter: IconsAdapter
    var roomDeviceCheck: String? = null
    var roomName: String? = null
    val switch_icons = arrayListOf(R.drawable.ic_bulb_off_new, R.drawable.ic_ceiling_lamp_off_new, R.drawable.ic_bed_lamp_off_new,
            R.drawable.ic_fan_off_, R.drawable.ic_switch_off_new_1, R.drawable.ic_ac_off, R.drawable.ic_exhaust_fan_off)
    var rgbLightIcons = arrayListOf<Int>(R.drawable.ic_bulb_off_new, R.drawable.ic_ceiling_lamp_off_new, R.drawable.ic_bed_lamp_off_new)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_load)
        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase
        val scenDbHelper = SceneDbHelper(this)
        mDbScene = scenDbHelper.writableDatabase
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase
        initialize()
    }

    fun initialize() {
        val deviceString: String = intent.getStringExtra("DEVICE") ?: ""
        val gson = Gson()
        val device = gson.fromJson(deviceString, Device::class.java)
        roomDeviceCheck = intent.getStringExtra("START_ROOM")
        roomName = intent.getStringExtra(Constant.ROOM_NAME)
        auraDevice = localSqlDatabase.getDevice(mDb!!, device.deviceName)
        auraDevice.mdl = auraDevice.loads[0].module
        load = auraDevice.loads[device.index]
        if ((device.fullDeviceName!!.contains("AuraSwitch Pro") || load.module == 6) && device.index == 3) {
            dimmableSwitch.visibility = View.VISIBLE
        } else {
            dimmableSwitch.visibility = View.GONE
        }
        if (load.module == 8) {
            swAdaptive.isChecked = load.isAdaptive
            swAdaptive.visibility = View.VISIBLE
        } else {
            swAdaptive.visibility = View.GONE
        }

        init(device.deviceName)
    }

    private fun init(device: String) {

        loadAdapter = IconsAdapter { pos: Int, Boolean ->

        }
        loadAdapter.viewTypeIcon = Constant.ICON_VIEW
        enableFields(true)
        tvNext.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            load.icon = loadAdapter.selectedPostion()
            //localSqlDatabase.updateLoad(mDb!!, device, load)
            MainScope().launch {
                Utils.hideSoftKeyboard(this@EditLoadActivity)
                localSqlDatabase.updateLoad(mDb!!, device, load)
                AppExecutors().diskIO().execute {
                    auraDevice.loads[load.index!!] = load
                    RulesTableHandler().insertDeviceLoads(auraDevice)
                }
                delay(1000)
                if (roomDeviceCheck == "RoomDevice") {
                    val intent = Intent(this@EditLoadActivity, RoomActivity::class.java)
                    intent.putExtra(Constant.ROOM_NAME, roomName)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@EditLoadActivity, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            }
//            if (roomDeviceCheck == "RoomDevice") {
//                val intent = Intent(this, RoomActivity::class.java)
//                intent.putExtra(Constant.ROOM_NAME, roomName)
//                setResult(Activity.RESULT_OK,intent)
//                finish()
//            } else {
//                val intent = Intent(this, DashboardActivity::class.java)
//                setResult(Activity.RESULT_OK,intent)
//                finish()
//            }
        }

        favouriteSwitch.setOnClickListener {
            load.favourite = favouriteSwitch.isChecked
        }

        dimmableSwitch.setOnClickListener {
            load.dimmable = dimmableSwitch.isChecked
        }
        swAdaptive.setOnClickListener {
            load.isAdaptive = swAdaptive.isChecked
            if (intent.getBooleanExtra(Constant.HOME, false)) {
                HomeFragment().onAdaptiveChecked(load, auraDevice.uiud)
            } else {
                RoomsFragment().onAdaptiveChecked(load, auraDevice.uiud)
            }
        }

        tvTitle.text = getString(R.string.manage_loads)
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.black_d_n))
        tvNext.text = getString(R.string.text_finish)
        tvNext.setTextColor(ContextCompat.getColor(this, R.color.black_d_n))
        DrawableCompat.setTint(home.drawable, ContextCompat.getColor(this, R.color.black_d_n))
        home.setOnClickListener {
            this.onBackPressed()
        }
        sickbar_layout.visibility = GONE
        dimmableSwitch.isChecked = load.dimmable!!
        favouriteSwitch.isChecked = load.favourite!!

        input_load.setText(load.name)

        input_load.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                load.name = input_load.text.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        val reverseLayout = false
        loadList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, reverseLayout)
        loadList.adapter = loadAdapter
        if (load.module == 7 || load.module == 8) {
            loadAdapter.init(rgbLightIcons, load.icon!!)
        } else {
            loadAdapter.init(switch_icons, load.icon!!)
        }

    }

    private fun enableFields(enable: Boolean) {
        if (enable) {
            input_load.isEnabled = true
            favouriteSwitch.isEnabled = true
            dimmableSwitch.isEnabled = true
            loadList.isClickable = true
            loadList.visibility = View.VISIBLE
            //sickbar.isEnabled=true
        } else {
            input_load.isEnabled = false
            favouriteSwitch.isEnabled = false
            dimmableSwitch.isEnabled = false
            loadList.isClickable = false
            loadList.visibility = View.INVISIBLE
            // sickbar.isEnabled=false
        }

    }

}
