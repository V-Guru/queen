package com.wozart.aura.ui.base.basesetactions

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wozart.aura.entity.network.Nsd
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.model.AutomationModel
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.OnFragmentInteractionListener
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.sceneController.SelectLoadFragment
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.layout_header.*


abstract class BaseSetActionsFragment : Fragment() {

    var activity: Activity? = null
    lateinit var text_scene: TextView
    lateinit var text_load: TextView
    private var mListener: OnFragmentInteractionListener? = null
    var buttonModel = ButtonModel()
    private var nsd: Nsd? = null
    var all_sense_device: MutableList<RemoteModel> = ArrayList()
    var abailableSenseHome: MutableList<RemoteModel> = ArrayList()
    private val localSqlUtils = UtilsTable()
    private var mDbUtils: SQLiteDatabase? = null
    var localData: String? = null
    var IP = IpHandler()


    companion object {
        var rooms: MutableList<RoomModel> = ArrayList()
        var sceneName: String? = null
        var sceneIcon: Int = 0
        var sceneNameOld: String? = null
        var sceneNameType: String? = null
        var automationNameOld: String? = null
        var automationScheduleType: String? = null
        var room: ArrayList<RoomModel> = ArrayList()
        var listAutomation: MutableList<AutomationScene> = ArrayList()
        var data = AutomationModel("title", 0, room, true, " ", " ", " ", " ", "")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_select_devices, container, false)
        text_load = layout.findViewById(R.id.tvSelectLoadIcon)
        text_scene = layout.findViewById(R.id.text_enter_scene)
        text_scene.visibility = View.GONE
        text_load.visibility = View.GONE
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    protected fun getSceneName(): String {
        return sceneName!!
    }

    protected fun getSceneNameOld(): String {
        return sceneNameOld!!
    }

    protected fun getSceneNameType(): String {
        return sceneNameType!!
    }

    protected fun getAutomationSceneName(): String? {
        return automationNameOld
    }

    protected fun getAutomationSceneType(): String? {
        return automationScheduleType
    }

    protected fun getSceneIcon(): Int {
        return sceneIcon
    }


    private fun init() {
        nsd = Nsd()
        nsd?.getInstance(requireActivity(), "HOME")
        val dbUtils = UtilsDbHelper(requireContext())
        mDbUtils = dbUtils.writableDatabase
        all_sense_device = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        abailableSenseHome.addAll(all_sense_device.filter { it.home == Constant.HOME  && it.sense_loads.size > 1})
        tvTitle.text = getString(R.string.text_set_actions)
        tvNext.text = "Done"
        tvNext.setOnClickListener { onFinish() }
        home.setColorFilter(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        home.setOnClickListener { mListener?.onHomeBtnClicked() }
        for (l in IP.getIpDevices()) {
            all_sense_device.find { it.aura_sence_name == l.name }?.let {
                it.sense_ip = l.ip ?: ""
            }
        }
        nsdDiscovery()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        if (activity?.intent != null && activity?.intent!!.hasExtra(Constant.BUTTON_DATA)) {
            if (SelectLoadFragment.scene != null) {
                buttonModel.sceneSelectedList.add(SelectLoadFragment.scene!!)
            }
        }

    }

    private fun nsdDiscovery() {
        nsd?.setBroadcastType("HOME")
        nsd?.initializeNsd()
        nsd?.discoverServices()
    }

    private var onNSDServiceResolved = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val name = p1!!.getStringExtra("name")
            val ip = p1.getStringExtra("ip")
            val device = name!!.substring(name.length - 6, name.length)
            if (all_sense_device.size > 0) {
                all_sense_device.find { it.aura_sence_name == device }?.let {
                    it.sense_ip = ip ?: ""
                }
            }

        }
    }

    abstract fun onFinish()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity

        if (activity?.intent != null && activity?.intent!!.hasExtra(Constant.BUTTON_DATA)) {
            buttonModel = Gson().fromJson(activity?.intent!!.getStringExtra(Constant.BUTTON_DATA), ButtonModel::class.java)
        }
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnAutomationListInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


}
