package com.wozart.aura.ui.createautomation

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.model.AutomationModel
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.ui.adapter.DynamicMotionInputAdapter
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.selectdevices.SelectDevicesFragment
import kotlinx.android.synthetic.main.dialog_specific_time_layout.*
import kotlinx.android.synthetic.main.fragment_motion_selection.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MotionSelectionFragment : Fragment(), RecyclerItemClicked {


    private var triggerWhen: String? = null
    private var locationBased: String? = null
    private var mListener: OnFragmentInteractionListener? = null
    var peopleArray = arrayOf("Off", "Home", "Away")
    var timeArray = arrayListOf("Any time", "During the day", "At night", "Specific time")
    private var senseList: MutableList<AuraSenseConfigure> = ArrayList()
    private var starttimeformat: String? = null
    private var endTimeFormat: String? = null
    var motionInputAdapter: DynamicMotionInputAdapter? = null


    companion object {
        var automationScheduleType: String? = null
        private var automationNameOld: String? = null
        var room: ArrayList<RoomModel> = ArrayList()
        var rooms: MutableList<RoomModel> = ArrayList()
        var motionData: String? = null
        var scheduleDetails = AutomationModel("title", 0, room, false, " ", " ", " ", " ", "")
        fun newInstance(scheduleData: AutomationModel, automationSceneNameOld: String, automationSceneType: String, motionData: String): MotionSelectionFragment {
            val scheduleDetail = scheduleData
            this.scheduleDetails = scheduleDetail
            this.automationNameOld = automationSceneNameOld
            this.automationScheduleType = automationSceneType
            this.motionData = motionData
            return MotionSelectionFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_motion_selection, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTitle.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        btnNext.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n))
        initialize()
        setAdapter()
    }

    fun setAdapter() {
        (rvSenseView.adapter as DynamicMotionInputAdapter).recyclerItemClicked = this
        (rvSenseView.adapter as DynamicMotionInputAdapter).listInputSense = senseList
    }

    fun initialize() {
        motionInputAdapter = DynamicMotionInputAdapter()
        rvSenseView.adapter = motionInputAdapter
        val data = JSONArray(motionData)
        senseList.clear()
        for (d in 0 until data.length()) {
            val dataList = data.getJSONObject(d)
            val sense = AuraSenseConfigure()
            sense.auraSenseName = dataList.getString("auraSenseName")
            sense.isSelected = dataList.getBoolean("isSelected")
            sense.above = dataList.getBoolean("above")
            sense.below = dataList.getBoolean("below")
            sense.auraSenseIcon = dataList.getInt("auraSenseIcon")
            sense.auraSenseIndex = dataList.getInt("auraSenseIndex")
            sense.senseDeviceName = dataList.getString("senseDeviceName")
            sense.range = dataList.getInt("range")
            sense.senseMacId = dataList.getString("senseMacId")
            sense.senseUiud = dataList.getString("senseUiud")
            sense.auraSenseFavorite = dataList.getBoolean("auraSenseFavorite")
            sense.type = dataList.getString("type")
            senseList.add(sense)
        }

        btnNext.setOnClickListener {
            openNextScreen()
        }
        home.setOnClickListener {
            mListener?.onHomeBtnClicked()
        }

        val spinnerTypeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_list, peopleArray)
        spinnerTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPeople.adapter = spinnerTypeAdapter
        spinnerPeople.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                locationBased = spinnerPeople.selectedItem.toString()
            }
        }

        if (scheduleDetails.type == "motion") {
            if (timeArray.size > 3) timeArray.removeAt(3)
        }
        val spinnerTimeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_list, timeArray)
        spinnerTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTime.adapter = spinnerTimeAdapter
        spinnerTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                triggerWhen = spinnerTime.selectedItem.toString()
                if (scheduleDetails.type != "motion") {
                    if (triggerWhen == timeArray[3]) {
                        showTimeDialog()
                    }
                }
            }
        }


    }

    @SuppressLint("SimpleDateFormat")
    fun showTimeDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_specific_time_layout)
        dialog.setCanceledOnTouchOutside(true)
        val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        val currentLocalTime = calendar.getTime()
        val date = SimpleDateFormat("Z")
        val localTime = date.format(currentLocalTime)
        val offsetTimetype = localTime[0]
        val offsetTimeHours = localTime.substring(1, 3).toInt()
        val offsetTimeMinutes = localTime.substring(3).toInt()
        dialog.start_time_picker.setOnTimeChangedListener { timePicker, hour, minute ->
            val StrMin = String.format("%02d", minute)
            val timeHours24 = hour
            val timeMinutes24 = minute
            val totalTime = timeHours24 * 60 + timeMinutes24
            val totalOffset = offsetTimeHours * 60 + offsetTimeMinutes
            var gmtTime = 0

            if (offsetTimetype == '+') {
                if (totalTime > totalOffset) {
                    gmtTime = totalTime - totalOffset
                } else {
                    gmtTime = 1440 - totalOffset + totalTime
                }
            } else {
                gmtTime = totalTime + totalOffset
                if (gmtTime > 1440) {
                    gmtTime = gmtTime - 1440
                }
            }
            starttimeformat = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
            scheduleDetails.time = starttimeformat
        }

        dialog.end_time_picker.setOnTimeChangedListener { timePicker, hrs, minutes ->

            val StrMin = String.format("%02d", minutes)
            val timeHours24 = hrs
            val timeMinutes24 = minutes
            val totalTime = timeHours24 * 60 + timeMinutes24
            val totalOffset = offsetTimeHours * 60 + offsetTimeMinutes
            var gmtTime = 0

            if (offsetTimetype == '+') {
                if (totalTime > totalOffset) {
                    gmtTime = totalTime - totalOffset
                } else {
                    gmtTime = 1440 - totalOffset + totalTime
                }
            } else {
                gmtTime = totalTime + totalOffset
                if (gmtTime > 1440) {
                    gmtTime = gmtTime - 1440
                }
            }

            endTimeFormat = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
            scheduleDetails.endTime = endTimeFormat
        }

        dialog.done.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }


    fun openNextScreen() {
        val gson = Gson()
        val motionData_ = gson.toJson(motionInputAdapter?.listInputSense)
        mListener?.navigateToFragment(SelectDevicesFragment.newInstance(scheduleDetails, automationNameOld!!, automationScheduleType!!, motionData_!!, triggerWhen))
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

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
    }
}