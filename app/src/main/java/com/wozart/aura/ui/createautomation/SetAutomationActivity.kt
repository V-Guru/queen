package com.wozart.aura.ui.createautomation

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.base.baseselectdevices.BaseSelectAutomationDevices
import com.wozart.aura.ui.selectdevices.SelectDevicesFragment
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.activity_create_automation.*


class SetAutomationActivity : BaseAbstractActivity(), OnFragmentInteractionListener {

    private var automationNameOld: String? = null
    private var automstionSceneType: String? = null
    private var automationName: String? = null
    private var scheduletype: String? = null
    private var selectedIcon: Int? = 0
    private var customGeoRadius: Float? = 0f
    private var automationEnable: Boolean = false
    var geoTriggeringType: String? = null
    private var mDbUtils: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    private var geo_trigger_when: String? = null
    private var geo_specific_start_time: String? = null
    private var geo_specific_end_time: String? = null
    private var geo_latitude: Double = 0.0
    private var geo_longitude: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_automation)
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        val intent = intent
        automstionSceneType = intent.getStringExtra("automationSceneType")
        automationNameOld = intent.getStringExtra("automationNameOld")
        automationName = intent.getStringExtra("automationName")
        scheduletype = intent.getStringExtra("scheduleBasedType")
        selectedIcon = intent.getIntExtra("icon", 0)
        customGeoRadius = intent.getFloatExtra("customRadius", 0f)
        automationEnable = intent.getBooleanExtra("scheduleEnable", false)
        geoTriggeringType = intent.getStringExtra("triggerType")
        geo_trigger_when = intent.getStringExtra("trigger_when")
        geo_specific_start_time = intent.getStringExtra("specific_start_time")
        geo_specific_end_time = intent.getStringExtra("specific_end_time")
        geo_latitude = intent.getDoubleExtra("geo_latitude", 0.0)
        geo_longitude = intent.getDoubleExtra("geo_longitude", 0.0)

        navigateToFragment(SelectDevicesFragment(), "", true, true)

    }

    override fun onRoomBtnClicked() {

    }

    override fun onHomeBtnClicked() {
        onBackPressed()
    }

    fun getAutomationOldName(): String? {
        return automationNameOld
    }

    fun getAutomationType(): String? {
        return automstionSceneType
    }

    fun getAutomationName(): String? {
        return automationName
    }

    fun getScheduleType(): String? {
        return scheduletype
    }

    fun getIcon(): Int? {
        return selectedIcon
    }

    fun getAddedGeoRadius(): Float? {
        return customGeoRadius
    }

    fun getAutomationEnable(): Boolean {
        return automationEnable
    }

    fun getTriggeringType(): String? {
        return geoTriggeringType
    }

    fun getTriggerWhen(): String? {
        return geo_trigger_when
    }

    fun getGeoSpecificStartTime(): String? {
        return geo_specific_start_time
    }

    fun getGeoSpecificEndTime(): String? {
        return geo_specific_end_time
    }

    fun getLatitude(): Double {
        return geo_latitude
    }

    fun getLongitude(): Double? {
        return geo_longitude
    }

}
