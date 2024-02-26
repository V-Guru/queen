package com.wozart.aura.ui.createautomation

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.model.AutomationModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.model.SenseRoomSeperation
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.adapter.SenseRoomBasedAdapter
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.fragment_set_motion.*
import kotlinx.android.synthetic.main.layout_header.*


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-03-12
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/

class MotionSetFragment : androidx.fragment.app.Fragment(), RecyclerItemClicked {

    private val localSqlUtils = UtilsTable()
    private var mDbUtils: SQLiteDatabase? = null
    private var senseDeviceList: MutableList<RemoteModel> = ArrayList()
    var loads: MutableList<AuraSenseConfigure> = ArrayList()
    private var newLoadsList: MutableList<AuraSenseConfigure> = ArrayList()
    private var mListener: OnFragmentInteractionListener? = null
    private var senseRoomsAllocated: MutableList<SenseRoomSeperation> = arrayListOf()
    var senseAdapter: SenseRoomBasedAdapter? = null


    companion object {
        private var automationScheduleType: String? = null
        private var automationNameOld: String? = null
        var room: ArrayList<RoomModel> = ArrayList()
        var rooms: MutableList<RoomModel> = ArrayList()
        var scheduleDetails = AutomationModel("title", 0, room, false, " ", " ", " ", " ", "")
        fun newInstance(schedule: AutomationModel, automationSceneNameOld: String, automationSceneType: String): MotionSetFragment {
            this.automationNameOld = automationSceneNameOld
            this.automationScheduleType = automationSceneType
            this.scheduleDetails = schedule
            return MotionSetFragment()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_set_motion, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        init()
    }

    fun init() {
        senseAdapter = SenseRoomBasedAdapter(requireContext(), this)
        sense_list.adapter = senseAdapter
        senseRoomsAllocated.clear()
        newLoadsList.clear()
        loads.clear()
        senseDeviceList = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        for (sense_device in senseDeviceList.filter { it.home == Constant.HOME }) {
            val senseRomms = SenseRoomSeperation()
            for (loads_data in sense_device.sense_loads) {
                if (loads_data.auraSenseName != "IR Blaster") {
                    val loads_list = AuraSenseConfigure()
                    loads_list.auraSenseName = loads_data.auraSenseName
                    loads_list.auraSenseIcon = loads_data.auraSenseIcon
                    loads_list.auraSenseIndex = loads_data.auraSenseIndex
                    loads_list.auraSenseFavorite = loads_data.auraSenseFavorite
                    loads_data.senseDeviceName = sense_device.aura_sence_name
                    loads_data.senseMacId = sense_device.senseMacId
                    loads_data.senseUiud = sense_device.sense_uiud
                    loads.add(loads_data)
                }
            }
            var senseAvailable: Boolean = false
            for (sense in senseRoomsAllocated) {
                if (sense.roomName == sense_device.room) {
                    sense.senseLoadList.addAll(loads)
                    senseAvailable = true
                    break
                }
            }
            if (!senseAvailable) {
                senseRomms.roomName = sense_device.room
                senseRomms.senseLoadList.addAll(loads)
                senseRoomsAllocated.add(senseRomms)
                loads.clear()
            }
        }
        if (automationScheduleType == "edit") {
            var list: MutableList<AuraSenseConfigure> = arrayListOf()
            if (context is CreateAutomationActivity) {
                list = (context as CreateAutomationActivity).getSenseData()
            }
            for (senseData in senseRoomsAllocated) {
                for (senseSelected in list) {
                    for (dataSensor in senseData.senseLoadList) {
                        if (senseSelected.auraSenseName == dataSensor.auraSenseName && senseSelected.senseDeviceName == dataSensor.senseDeviceName) {
                            dataSensor.isSelected = true
                            newLoadsList.add(dataSensor)
                        }
                    }
                }
            }
        }
        setAdapter()
        tvNext.setOnClickListener {
            openNextScreen()
        }
        home.setOnClickListener {
            mListener?.onHomeBtnClicked()
        }
    }

    fun setAdapter() {
        (sense_list.adapter as SenseRoomBasedAdapter).senseDataList = senseRoomsAllocated
    }

    private fun openNextScreen() {
        if (newLoadsList.size > 0) {
            val motionData = Gson().toJson(newLoadsList)
            mListener?.navigateToFragment(MotionSelectionFragment.newInstance(scheduleDetails, automationNameOld!!, automationScheduleType!!, motionData))
        } else {
            Toast.makeText(requireContext(), getString(R.string.please_select_sensor), Toast.LENGTH_SHORT).show()
        }
    }

    fun initialize() {
        val dbUtils = UtilsDbHelper(requireContext())
        mDbUtils = dbUtils.writableDatabase
        tvTitle.text = requireContext().getString(R.string.set_motion_title)
        tvTitle.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        tvNext.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black_d_n))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else throw RuntimeException(context.toString() + " must implement OnAutomationListInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        val sense = data as AuraSenseConfigure
        var flag = false
        for (senses in newLoadsList) {
            flag = false
            if (senses.auraSenseName == sense.auraSenseName && sense.senseDeviceName == senses.senseDeviceName) {
                newLoadsList.remove(senses)
                flag = true
                break
            }
        }
        if (!flag) {
            newLoadsList.add(sense)
        }

    }
}