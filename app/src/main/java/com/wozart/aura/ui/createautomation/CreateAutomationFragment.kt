package com.wozart.aura.aura.ui.createautomation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.wozart.aura.R
import com.wozart.aura.data.model.AutomationModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.ScheduleTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.base.SceneIconAdapter
import com.wozart.aura.ui.createautomation.*
import com.wozart.aura.ui.dashboard.home.HomeLocationActivity
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.fragment_create_automation.*
import kotlinx.android.synthetic.main.layout_header.*
import org.jetbrains.anko.longToast

/**
 * Created by Saif on 11/11/2018.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */

class CreateAutomationFragment : androidx.fragment.app.Fragment() {
    private val drawables = arrayListOf(R.drawable.ic_enter_off, R.drawable.ic_exit_off, R.drawable.ic_good_morning_off,
            R.drawable.ic_good_night_off, R.drawable.ic_party_off, R.drawable.ic_reading_off, R.drawable.ic_movie_off)
    private var mListener: OnFragmentInteractionListener? = null
    private var sceneIconurl: Int? = 0
    var automationName: String? = null
    private var mDBAutomation: SQLiteDatabase? = null
    private var localSqlSchedule = ScheduleTable()
    var automationSceneNameOld: String? = null
    var automationSceneType: String? = null
    private var sceneIconPosition: Int? = 0
    private lateinit var sceneIconAdapter: SceneIconAdapter
    private var automationScene = AutomationScene()
    var roomList = arrayListOf<RoomModel>()
    var automationModal = AutomationModel("", 0, roomList, false, "", "", "", "", "")
    var scheduleType = "time"
    var automationEnable: Boolean = false
    var sceneList = ArrayList<Scene>()
    var selectedSceneIcon: Boolean = false
    var geo_radius: Float = 200f
    lateinit var tvNext: TextView
    private var aure_sense_device_list: MutableList<RemoteModel> = ArrayList()
    private var localSqlUtils = UtilsTable()
    private var mdbUtils: SQLiteDatabase? = null
    var sense_device_flag = false
    var senseDataList: MutableList<AuraSenseConfigure> = ArrayList()
    var listRoom:RoomModelJson?= null
    var locationCheck: Boolean = false


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_automation, container, false)
        val dbAutomation = ScheduleDbHelper(context!!)
        mDBAutomation = dbAutomation.writableDatabase
        val dbUtilsHelper = UtilsDbHelper(context!!)
        mdbUtils = dbUtilsHelper.writableDatabase
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val arrive = view.findViewById<androidx.cardview.widget.CardView>(R.id.arrive)
        val timing = view.findViewById<androidx.cardview.widget.CardView>(R.id.timing)
        val leave = view.findViewById<androidx.cardview.widget.CardView>(R.id.leave)
        val motion = view.findViewById<androidx.cardview.widget.CardView>(R.id.motion)
        val textDetails = view.findViewById<TextView>(R.id.text_details)
        val automationEnableLayout = view.findViewById<RelativeLayout>(R.id.enableCard)
        val switchenable = view!!.findViewById<SwitchCompat>(R.id.switchenable)
        tvNext = view.findViewById(R.id.tvNext)
        tvNext.visibility = View.INVISIBLE
        automationEnableLayout.visibility = View.INVISIBLE
        automationModal.Automationenable = true
        automationEnable = automationModal.Automationenable
        if (context is CreateAutomationActivity) {
            automationSceneNameOld = (context as CreateAutomationActivity).getAutomationSceneName()
            automationSceneType = (context as CreateAutomationActivity).getAutomationSceneType()
            automationScene = (context as CreateAutomationActivity).getAutomationScene()
            if ((automationSceneType == "edit") && (automationScene.type == "motion")) {
                senseDataList = (context as CreateAutomationActivity).getSenseData()
            }
        }
        if (automationSceneType == "edit") {
            val inputName = view.findViewById<TextInputEditText>(R.id.input_name)
            tvTitle.text = "Edit Automation"
            tvNext.visibility = View.VISIBLE
            inputName.setText(automationSceneNameOld)
            scheduleType = automationScene.type!!
            automationEnableLayout.visibility = View.VISIBLE
            for (automationEnableCheck in automationScene.property) {
                geo_radius = automationEnableCheck.newGeoRadius!!
                if (automationEnableCheck.AutomationEnable) {
                    automationEnable = automationEnableCheck.AutomationEnable
                    switchenable.isChecked = automationEnable
                } else {
                    automationEnable = automationEnableCheck.AutomationEnable
                    switchenable.isChecked = automationEnable
                }
            }
            switchenable?.setOnCheckedChangeListener { _, isChecked ->
                automationEnable = switchenable.isChecked
            }
            if (scheduleType == "geo") {
                for (occourCheck in automationScene.property) {
                    if (occourCheck.triggerType == "Arriving") {
                        textDetails.text = "When: People Arrive"
                        arrive.setBackgroundResource(R.drawable.card_shade_white)
                        leave.setBackgroundResource(R.drawable.card_shade_gray)
                    } else if (occourCheck.triggerType == "Leaving") {
                        textDetails.text = "When: People Leave"
                        leave.setBackgroundResource(R.drawable.card_shade_white)
                        arrive.setBackgroundResource(R.drawable.card_shade_gray)
                    }
                }
                motion.visibility = View.GONE
                timing.visibility = View.GONE
            } else if (scheduleType == "motion") {
                motion.visibility = View.VISIBLE
                motion.setBackgroundResource(R.drawable.card_shade_white)
                textDetails.text = "When Sense Trigger"
                timing.visibility = View.GONE
                arrive.visibility = View.GONE
                leave.visibility = View.GONE
            } else {
                if (scheduleType == "time") {
                    timing.setBackgroundResource(R.drawable.card_shade_white)
                    textDetails.text = "When ${automationScene.endTime}"
                    arrive.visibility = View.GONE
                    leave.visibility = View.GONE
                    motion.visibility = View.GONE

                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context is CreateAutomationActivity) {
            sceneIconurl = (context as CreateAutomationActivity).getSceneIconUrl()
        }
        sceneIconPosition = 0
        var pos = 0
        for (icons in drawables) {
            if (icons == automationScene.icon) {
                sceneIconPosition = pos
            } else {
                pos += 1
            }
        }

        for (i in drawables.indices) {
            val scene = Scene()
            scene.icon = drawables[i]
            if (i == sceneIconurl) {
                sceneIconPosition = pos
                selectedSceneIcon = true
            } else {
                selectedSceneIcon = false
            }
            sceneList.add(scene)
        }

        sceneIconAdapter = SceneIconAdapter(sceneList) { pos: Int, Boolean ->

        }

        init()
    }

    fun getIcon(): Int {
        return drawables[0]
    }


    @SuppressLint("ResourceType")
    private fun init() {
        var isCheckedButton = false
        var action: String? = null
        tvTitle.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        tvNext.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n))
        arrive.setBackgroundResource(R.drawable.card_shade_gray)
        leave.setBackgroundResource(R.drawable.card_shade_gray)
        timing.setBackgroundResource(R.drawable.card_shade_gray)
        motion.setBackgroundResource(R.drawable.card_shade_gray)
        home.setOnClickListener { mListener?.onHomeBtnClicked() }
        input_name.visibility = View.VISIBLE

        listRoom = localSqlUtils.getHomeData(mdbUtils!!, "home").find { it.type == "home" && it.name == Constant.HOME }
        if (listRoom?.homeLocation == "null" || listRoom?.homeLocation.isNullOrEmpty()) {
            locationCheck = true
        }

        aure_sense_device_list = localSqlUtils.getRemoteData(mdbUtils!!, "remote_device")

        if (aure_sense_device_list.filter { it.home == Constant.HOME }.isNotEmpty()) {
            sense_device_flag = true
        }

        arrive.setOnClickListener {
            isCheckedButton = true
            arrive.setBackgroundResource(R.drawable.card_shade_white)
            leave.setBackgroundResource(R.drawable.card_shade_gray)
            motion.setBackgroundResource(R.drawable.card_shade_gray)
            timing.setBackgroundResource(R.drawable.card_shade_gray)
            action = "Arriving"
            scheduleType = "geo"
            if (!Common.isLocationPermissionsGranted(requireContext())) {
                SingleBtnDialog.with(requireContext()).setHeading(getString(R.string.alert)).setMessage(getString(R.string.info_location))
                        .setCallback {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", requireActivity().packageName, null))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        .show()
            } else {
                if (locationCheck) {
                    val intent = Intent(activity, HomeLocationActivity::class.java)
                    intent.putExtra("HOME_NAME", Constant.HOME)
                    intent.putExtra(Constant.CREATE_HOME_ROOM, "edit")
                    intent.putExtra(Constant.SET_LOCATION, Constant.SET_LOCATION_HOME)
                    startActivity(intent)
                } else {
                    openSetGeoScreen(action!!)
                }
            }
        }
        leave.setOnClickListener {
            isCheckedButton = true
            leave.setBackgroundResource(R.drawable.card_shade_white)
            arrive.setBackgroundResource(R.drawable.card_shade_gray)
            motion.setBackgroundResource(R.drawable.card_shade_gray)
            timing.setBackgroundResource(R.drawable.card_shade_gray)
            action = "Leaving"
            scheduleType = "geo"
            if (!Common.isLocationPermissionsGranted(requireContext())) {
                SingleBtnDialog.with(requireContext()).setHeading(getString(R.string.alert)).setMessage(getString(R.string.info_location))
                        .setCallback {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", requireActivity().packageName, null))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        .show()
            } else {
                if (locationCheck) {
                    val intent = Intent(activity, HomeLocationActivity::class.java)
                    intent.putExtra("HOME_NAME", Constant.HOME)
                    intent.putExtra(Constant.CREATE_HOME_ROOM, "edit")
                    intent.putExtra(Constant.SET_LOCATION, Constant.SET_LOCATION_HOME)
                    startActivity(intent)
                } else {
                    openSetGeoScreen(action!!)
                }
            }
        }

        timing.setOnClickListener {
            isCheckedButton = true
            timing.setBackgroundResource(R.drawable.card_shade_white)
            leave.setBackgroundResource(R.drawable.card_shade_gray)
            arrive.setBackgroundResource(R.drawable.card_shade_gray)
            motion.setBackgroundResource(R.drawable.card_shade_gray)
            scheduleType = "time"
            openSetTimeScreen()
        }

        motion.setOnClickListener {
            if (sense_device_flag) {
                motion.setBackgroundResource(R.drawable.card_shade_white)
                timing.setBackgroundResource(R.drawable.card_shade_gray)
                leave.setBackgroundResource(R.drawable.card_shade_gray)
                arrive.setBackgroundResource(R.drawable.card_shade_gray)
                scheduleType = "motion"
                openSetAction()
            } else {
                activity?.longToast("You need Wozart Sense device to use this feature.")
            }

        }

        tvNext.setOnClickListener {
            if (scheduleType == "geo") {
                if (automationScene.property.size > 0) {
                    for (occourCheck in automationScene.property) {
                        if (occourCheck.triggerType == "Arriving") {
                            val amArriving = "Arriving"
                            scheduleType = "geo"
                            openSetGeoScreen(amArriving)

                        } else if (occourCheck.triggerType == "Leaving") {
                            val amLeaving = "Leaving"
                            scheduleType = "geo"
                            openSetGeoScreen(amLeaving)
                        }
                    }
                }
                timing.visibility = View.GONE
            } else if (scheduleType == "motion") {
                scheduleType = "motion"
                openSetAction()
            } else if (scheduleType == "time") {
                scheduleType = "time"
                openSetTimeScreen()
            }
        }
        populateData()
    }

    private fun populateData() {
        automationSceneList()
    }

    private fun automationSceneList() {
        sceneIconAdapter.init(drawables, sceneIconPosition!!)
    }

    fun openSetAction() {
        automationName = input_name.text.toString()
        if (automationName!!.isEmpty()) {
            activity?.longToast("Please Enter Automation Name")
        } else {
            val icon = getIcon()
            val schedule = AutomationModel(automationName, icon, roomList, automationEnable, "", scheduleType, "", "", "")
            mListener?.navigateToFragment(MotionSetFragment.newInstance(schedule, automationSceneNameOld!!, automationSceneType!!))

        }
    }

    private fun openSetGeoScreen(when_occur: String) {
        automationName = input_name.text.toString()
        if (automationName!!.isEmpty()) {
            activity?.longToast("Please Enter Automation Name")
        } else {
            val icon = getIcon()
            scheduleType = "geo"
            val intent = Intent(activity, SetGeoAutomationActivity::class.java)
            intent.putExtra("automationNameOld", automationSceneNameOld)
            intent.putExtra("automationSceneType", automationSceneType)
            intent.putExtra("automationName", automationName)
            intent.putExtra("scheduleBasedType", scheduleType)
            intent.putExtra("scheduleEnable", automationEnable)
            intent.putExtra("icon", icon)
            intent.putExtra("ACTION_WHEN", when_occur)
            intent.putExtra("GEO_RADIUS", geo_radius)
            startActivity(intent)
        }

    }

    private fun openSetTimeScreen() {
        automationName = input_name.text.toString()
        if (automationName!!.isEmpty()) {
            activity?.longToast("Please Enter Automation Name")
        } else {
            //if (localSqlSchedule.checkAutomationExist(mDBAutomation!!, automationName!!, Constant.HOME!!)) {
            scheduleType = "time"
            val icon = getIcon()
            mListener?.navigateToFragment(SetTimeAutomationFragment.newInstance(automationName!!, scheduleType, icon, automationSceneNameOld!!, automationSceneType!!, automationEnable))

        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
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
