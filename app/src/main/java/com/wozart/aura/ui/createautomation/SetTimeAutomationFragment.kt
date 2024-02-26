package com.wozart.aura.aura.ui.createautomation

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.R
import com.wozart.aura.ui.selectdevices.SelectDevicesFragment
import com.wozart.aura.data.model.AutomationModel
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.CreateAutomationActivity
import com.wozart.aura.ui.createautomation.OnFragmentInteractionListener
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.fragment_set_time.*
import kotlinx.android.synthetic.main.layout_header.*
import org.jetbrains.anko.longToast
import java.util.*
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat


/**
 * Created by Saif on 11/18/2018.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */

class SetTimeAutomationFragment : androidx.fragment.app.Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private var selectedWeekDays = HashMap<String, Boolean>()
    private var automationScene = AutomationScene()
    private var automationSceneType: String? = null
    private var globalStartTime: String? = null
    private var timeformat: String? = null
    private var sunsetTime: String? = null
    private lateinit var time_picker: TimePicker
    private var time1: String? = null

    companion object {
        private var scheduleName: String? = null
        private var scheduleType: String? = null
        private var automationSceneIcon: Int = 0
        private var automationNameOld: String? = null
        private var automationScheduleType: String? = null
        private var automationEnable: Boolean = false

        fun newInstance(automationName: String, type: String, icon: Int, automationSceneNameOld: String, automationSceneType: String, automationenable: Boolean): SetTimeAutomationFragment {
            scheduleName = automationName
            scheduleType = type
            automationSceneIcon = icon
            automationNameOld = automationSceneNameOld
            automationScheduleType = automationSceneType
            automationEnable = automationenable
            return SetTimeAutomationFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_set_time, container, false)
        val inputStart = view.findViewById<TextView>(R.id.inputStart)
        var inputEnd = view.findViewById<EditText>(R.id.inputEnd)

        if (context is CreateAutomationActivity) {
            automationScene = (context as CreateAutomationActivity).getAutomationScene()
            automationSceneType = (context as CreateAutomationActivity).getAutomationSceneType()

        }
        if (automationSceneType == "edit") {

            inputStart.setText(automationScene.endTime)

        }

        return view
    }


    @TargetApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        time_picker = view.findViewById(R.id.time_picker)
        init()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun init() {
        if(automationSceneType == "create"){
            showTimePicker()
        }else{
            if(automationSceneType == "edit"){
                if(automationScene.endTime == "Sunrise"){
                    time1 = "Sunrise"
                    selected_automation.visibility = View.VISIBLE
                    selected_sunset.visibility = View.GONE
                    time_picker.visibility = View.GONE
                    selected_time.visibility = View.GONE
                    sunsetTime = "5:45"
                    getSunsetSunrise(sunsetTime!!)

                } else if(automationScene.endTime == "Sunset"){
                    time1 = "Sunset"
                    selected_sunset.visibility = View.VISIBLE
                    selected_time.visibility = View.GONE
                    time_picker.visibility = View.GONE
                    selected_automation.visibility = View.GONE
                    sunsetTime = "18:10"
                    getSunsetSunrise(sunsetTime!!)
                }else{
                    selected_time.visibility = View.VISIBLE
                    selected_automation.visibility = View.GONE
                    time_picker.visibility = View.VISIBLE
                    selected_sunset.visibility = View.GONE
                    showTimePicker()
                }
            }
        }

        card_sunrise.setOnClickListener {
            time1 = "Sunrise"
            selected_automation.visibility = View.VISIBLE
            selected_sunset.visibility = View.GONE
            time_picker.visibility = View.GONE
            selected_time.visibility = View.GONE
            sunsetTime = "5:45"
            getSunsetSunrise(sunsetTime!!)

        }
        card_sunset.setOnClickListener {
            time1 = "Sunset"
            selected_sunset.visibility = View.VISIBLE
            selected_time.visibility = View.GONE
            time_picker.visibility = View.GONE
            selected_automation.visibility = View.GONE
            sunsetTime = "18:10"
            getSunsetSunrise(sunsetTime!!)


        }
        card_time.setOnClickListener {
            selected_time.visibility = View.VISIBLE
            selected_automation.visibility = View.GONE
            time_picker.visibility = View.VISIBLE
            selected_sunset.visibility = View.GONE
            showTimePicker()
        }

        tvTitle.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n))
        tvNext.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        tvTitle.text = getString(R.string.title_set_time)
        home.setOnClickListener { mListener?.onHomeBtnClicked() }
        tvNext.setOnClickListener {

            openNextScreen()
        }
        tvSunday.setOnClickListener {
            changeTvBg(tvSunday, Constant.SUNDAY)
        }
        tvMonday.setOnClickListener {
            changeTvBg(tvMonday, Constant.MONDAY)
        }
        tvTuesday.setOnClickListener {
            changeTvBg(tvTuesday, Constant.TUESDAY)
        }
        tvWednesday.setOnClickListener {
            changeTvBg(tvWednesday, Constant.WEDNESDAY)
        }
        tvThursday.setOnClickListener {
            changeTvBg(tvThursday, Constant.THURSDAY)
        }
        tvFriday.setOnClickListener {
            changeTvBg(tvFriday, Constant.FRIDAY)
        }
        tvSaturday.setOnClickListener {
            changeTvBg(tvSaturday, Constant.SATURDAY)
        }


        initMap()
    }

    private fun getAMPM(hour: Int): String {
        return if (hour > 11) "PM" else "AM"
    }

    private fun getSunsetSunrise(timeSunsetSunrise: String){
        val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        val currentLocalTime = calendar.getTime()
        val date = SimpleDateFormat("Z")
        val localTime = date.format(currentLocalTime)
        val offsetTimetype = localTime[0]
        val offsetTimeHours = localTime.substring(1, 3).toInt()
        val offsetTimeMinutes = localTime.substring(3).toInt()

        val timeSunset = timeSunsetSunrise!!.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sunsetHour = timeSunset[0].toInt()
        val sunsetMinutes = timeSunset[1].toInt()
        val totalTime = sunsetHour * 60 + sunsetMinutes
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
                gmtTime -= 1440
            }
        }

        timeformat = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
    }

    @SuppressLint("NewApi", "SetTextI18n")
    private fun showTimePicker() {

        val linear_time_layout = requireView().findViewById<RelativeLayout>(R.id.linear_time_layout)
        linear_time_layout.visibility = View.GONE

        time_picker = requireView().findViewById<TimePicker>(R.id.time_picker)

        if (automationSceneType == "edit") {
            val times = automationScene.endTime!!.replace("A", "").replace("P", "").replace("M", "").replace(" ", "").split(":")
            if(times[0] == "null"){
                activity?.longToast("Please select time.")

            }else{
                val editHour = times[0].toInt()
                val editMinute = times[1].toInt()
                time_picker.minute = editMinute.toInt()
                if (automationScene.endTime!!.contains("P")) {
                    time_picker.hour = editHour + 12
                }else{
                    time_picker.hour = editHour
                }

            }

            time1 =  automationScene.endTime!!
            timeformat = automationScene.time!!

        } else {
            var hour = time_picker.hour
            var minute = time_picker.minute

            if (hour > 12) {
                hour -= 12
                inputStart.text = "${hour} " + ": $minute PM"
            } else {
                inputStart.text = "${hour} " + ": $minute AM"
            }
        }

        time_picker.setOnTimeChangedListener { timePicker, hour, minutes ->
            val StrMin = String.format("%02d", minutes)
            val timeHours24 = hour
            val timeMinutes24 = minutes
            val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
            val currentLocalTime = calendar.getTime()
            val date = SimpleDateFormat("Z")
            val localTime = date.format(currentLocalTime)
            val offsetTimetype = localTime[0]
            val offsetTimeHours = localTime.substring(1, 3).toInt()
            val offsetTimeMinutes = localTime.substring(3).toInt()

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

            timeformat = String.format("%02d", (gmtTime / 60)) + ":" + String.format("%02d", (gmtTime % 60))
            val StrHr = String.format("%02d", getHourAMPM(hour))
            globalStartTime = "$StrHr:$StrMin:${getAMPM(hour)}"
            inputStart.text = "${getHourAMPM(hour)} " + ": $StrMin ${getAMPM(hour)}"
            time1 = inputStart.text.toString()

        }
    }

    private fun getHourAMPM(hour: Int): Int {
        var modifiedHour = if (hour > 11) hour - 12 else hour
        if (modifiedHour == 0) {
            modifiedHour = 12
        }
        return modifiedHour

    }


    private fun initMap() {
        if (automationSceneType == "edit") {
            val type = object : TypeToken<HashMap<String, Boolean>>() {}.type
            val gson = Gson()
            val routine: HashMap<String, Boolean>
            routine = gson.fromJson(automationScene.routine, type)
            selectedWeekDays[Constant.SUNDAY] = routine["Sunday"]!!
            selectedWeekDays[Constant.MONDAY] = routine["Monday"]!!
            selectedWeekDays[Constant.TUESDAY] = routine["Tuesday"]!!
            selectedWeekDays[Constant.WEDNESDAY] = routine["Wednesday"]!!
            selectedWeekDays[Constant.THURSDAY] = routine["Thursday"]!!
            selectedWeekDays[Constant.FRIDAY] = routine["Friday"]!!
            selectedWeekDays[Constant.SATURDAY] = routine["Saturday"]!!
            updateAll()
        } else {
            selectedWeekDays[Constant.SUNDAY] = true
            selectedWeekDays[Constant.MONDAY] = true
            selectedWeekDays[Constant.TUESDAY] = true
            selectedWeekDays[Constant.WEDNESDAY] = true
            selectedWeekDays[Constant.THURSDAY] = true
            selectedWeekDays[Constant.FRIDAY] = true
            selectedWeekDays[Constant.SATURDAY] = true
            updateAll()
        }

    }

    private fun updateAll() {
        updateView(tvSunday, Constant.SUNDAY)
        updateView(tvMonday, Constant.MONDAY)
        updateView(tvTuesday, Constant.TUESDAY)
        updateView(tvWednesday, Constant.WEDNESDAY)
        updateView(tvThursday, Constant.THURSDAY)
        updateView(tvFriday, Constant.FRIDAY)
        updateView(tvSaturday, Constant.SATURDAY)
    }


    private fun changeTvBg(textView: TextView, dayKey: String) {
        //This is a way to change bg but based on business logic check logic {selectWeekDays -> Hashmap) can change}
        activity?.let {
            val status = selectedWeekDays[dayKey]
            if (status!!) {
                selectedWeekDays[dayKey] = false
                if ((selectedWeekDays["Sunday"] == false) and (selectedWeekDays["Saturday"] == false)) {
                    if ((selectedWeekDays["Monday"] == false) || (selectedWeekDays["Tuesday"] == false) || (selectedWeekDays["Wednesday"] == false) || (selectedWeekDays["Thursday"] == false) || (selectedWeekDays["Friday"] == false)) {
                        text_set_day.text = "On selected days"
                    } else {
                        text_set_day.text = "Weekdays"
                    }
                } else {
                    text_set_day.text = "On selected days"
                }
                textView.background = ContextCompat.getDrawable(requireActivity(), R.drawable.circle_out_bg)
                textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            } else {
                selectedWeekDays[dayKey] = true
                if ((selectedWeekDays["Sunday"] == true) and (selectedWeekDays["Monday"] == true) and (selectedWeekDays["Tuesday"] == true) and (selectedWeekDays["Wednesday"] == true) and (selectedWeekDays["Thursday"] == true) and (selectedWeekDays["Friday"] == true) and (selectedWeekDays["Saturday"] == true)) {
                    text_set_day.text = "Everyday"
                } else if ((selectedWeekDays["Sunday"] == false) and (selectedWeekDays["Saturday"] == false)) {
                    if ((selectedWeekDays["Monday"] == false) || (selectedWeekDays["Tuesday"] == false) || (selectedWeekDays["Wednesday"] == false) || (selectedWeekDays["Thursday"] == false) || (selectedWeekDays["Friday"] == false)) {
                        text_set_day.text = "On selected days"
                    } else {
                        text_set_day.text = "Weekdays"
                    }
                } else {
                    text_set_day.text = "On selected days"
                }
                textView.background = ContextCompat.getDrawable(requireActivity(), R.drawable.circle_in_bg)
                textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            }
        }
    }

    private fun updateView(textView: TextView, dayKey: String) {
        //This is a way to change bg but based on business logic check logic {selectWeekDays -> Hashmap) can change}
        activity?.let {
            val status = selectedWeekDays[dayKey]
            if (status!!) {
                if ((selectedWeekDays["Sunday"] == true) and (selectedWeekDays["Monday"] == true) and (selectedWeekDays["Tuesday"] == true) and (selectedWeekDays["Wednesday"] == true) and (selectedWeekDays["Thursday"] == true) and (selectedWeekDays["Friday"] == true) and (selectedWeekDays["Saturday"] == true)) {
                    text_set_day.text = "Everyday"
                } else {
                    text_set_day.text = "On selected days"
                }
                textView.background = ContextCompat.getDrawable(requireActivity(), R.drawable.circle_in_bg)
                textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            } else {
                if ((selectedWeekDays["Sunday"] == false) and (selectedWeekDays["Saturday"] == false)) {
                    text_set_day.text = "Weekdays"
                } else {
                    text_set_day.text = "On selected days"
                }
                textView.background = ContextCompat.getDrawable(requireActivity(), R.drawable.circle_out_bg)
                textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            }
        }
    }

    private fun openNextScreen() {
        var roomList = arrayListOf<RoomModel>()

        if (timeformat == null) {
            activity?.toast("Please select time")
        } else {
            var schedule = AutomationModel(scheduleName, automationSceneIcon, roomList, automationEnable, timeformat, scheduleType, time1, selectedWeekDays.toString(), "")

            mListener?.navigateToFragment(SelectDevicesFragment.newInstance(schedule, automationNameOld!!, automationScheduleType!!,"",""))
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
