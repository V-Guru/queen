package com.wozart.aura.ui.createautomation

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.R
import com.wozart.aura.aura.ui.createautomation.CreateAutomationFragment
import com.wozart.aura.data.sqlLite.ScheduleTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import org.json.JSONArray


/**
 * Created by Saif on 11/10/2018.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */

class CreateAutomationActivity : BaseAbstractActivity(), OnFragmentInteractionListener {

    private var automationSceneName: String? = null
    private var automstionSceneType: String? = null
    private var automationIconUrl: Int? = 0
    private val localSqlDatabaseSchedule = ScheduleTable()
    private var mDBAutomation: SQLiteDatabase? = null
    private var automationScene = AutomationScene()
    private var mDbUtils: SQLiteDatabase? = null


    override fun onRoomBtnClicked() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_automation)
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase
        val dbAutomation = ScheduleDbHelper(this)
        mDBAutomation = dbAutomation.writableDatabase
        val intent = intent
        automstionSceneType = intent.getStringExtra("automationNameType")
        automationSceneName = intent.getStringExtra("input_name")
        automationIconUrl = intent.getIntExtra("automationIconUrl", 0)

        if (automstionSceneType == "edit") {
            automationScene = localSqlDatabaseSchedule.getAutomationSceneByName(mDBAutomation!!, automationSceneName!!)
        }
        init()
    }

    private fun init() {
        navigateToFragment(CreateAutomationFragment(), getString(R.string.empty_tag), true, true)
    }

    override fun onHomeBtnClicked() {
        onBackPressed()
    }

    fun getSceneIconUrl(): Int? {
        return automationIconUrl
    }

    fun getAutomationSceneName(): String? {
        return automationSceneName
    }

    fun getAutomationSceneType(): String? {
        return automstionSceneType
    }

    fun getTurnOffTime(): String? {
        return automationScene.property[0].turnOff
    }

    fun getAutomationScene(): AutomationScene {
        return automationScene
    }

    fun getSenseData(): MutableList<AuraSenseConfigure> {
        val senseDataList: MutableList<AuraSenseConfigure> = ArrayList()
        if (automationScene.routine != null) {
            val jsonData = automationScene.routine
            val data = JSONArray(jsonData)
            for (d in 0 until data.length()) {
                val dataList = data.getJSONObject(d)
                val sense = AuraSenseConfigure()
                sense.auraSenseName = dataList.getString("auraSenseName")
                sense.above = dataList.getBoolean("above")
                sense.below = dataList.getBoolean("below")
                sense.auraSenseFavorite = dataList.getBoolean("auraSenseFavorite")
                sense.auraSenseIcon = dataList.getInt("auraSenseIcon")
                sense.auraSenseIndex = dataList.getInt("auraSenseIndex")
                sense.range = dataList.getInt("range")
                sense.isSelected = dataList.getBoolean("isSelected")
                sense.senseDeviceName = dataList.getString("senseDeviceName")
                sense.senseUiud = dataList.getString("senseUiud")
                sense.senseMacId = dataList.getString("senseMacId")
                sense.type = dataList.getString("type")
                senseDataList.add(sense)
            }

        }
        return senseDataList
    }
}
