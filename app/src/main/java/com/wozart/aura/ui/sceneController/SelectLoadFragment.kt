package com.wozart.aura.ui.sceneController

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.ui.adapter.ButtonSceneSelectionAdapter
import com.wozart.aura.ui.adapter.SceneSelectionAdapter
import com.wozart.aura.ui.base.baseselectdevices.BaseSelectAutomationDevices
import com.wozart.aura.ui.base.baseselectdevices.BaseSelectButtonLoad
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.GridAutoFitLayoutManager
import com.wozart.aura.ui.dashboard.GridListSpacingItemDecoration
import com.wozart.aura.ui.dashboard.Scenes
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.Constant.Companion.BUTTON_DATA
import com.wozart.aura.utilities.Constant.Companion.SCENE_LOADS
import kotlinx.android.synthetic.main.fragment_automation_select_device.*
import kotlinx.android.synthetic.main.layout_header.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.support.v4.longToast


/**
 * Created by Saif on 11/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */

class SelectLoadFragment : BaseSelectButtonLoad(),View.OnClickListener {

    private var sceneNameCheck: String? = null
    var roomDeviceList: MutableList<RoomModel> = ArrayList()
    var sceneSelectedList: MutableList<Scenes> = ArrayList()
    lateinit var sceneAdapter: ButtonSceneSelectionAdapter
    var scenesList = ArrayList<Scenes>()
    companion object{
        var scene : Scenes ?= null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity

    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is Scenes) {
            scene = data
            sceneSelectedList.clear()
            for(scene in data.rooms){
                for (r in roomsList) {
                    for (d in r.deviceList) {
                        for (dScene in scene.deviceList) {
                            if ((dScene.index == d.index) && (dScene.deviceName == d.deviceName)) {
                                d.isSelected = viewType as Boolean
                                break
                            }
                        }
                    }
                }
                sceneSelectedList.add(data)
                adapter.notifyDataSetChanged()
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_automation_select_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialise()
        setAdapter()
    }

    private fun setAdapter() {
        if (scenesList.size > 0) {
            listScenesdata.adapter = sceneAdapter
            listScenesdata.layoutManager = GridAutoFitLayoutManager(context, resources.getDimensionPixelSize(R.dimen.device_item_size))
            listScenesdata.setHasFixedSize(true)
            sceneAdapter.dataSet(scenesList)
            val spacing = resources.getDimensionPixelSize(R.dimen.uniform_half_spacing)
            listScenesdata.addItemDecoration(GridListSpacingItemDecoration(spacing))
        }
    }

    fun initialise() {
        rlSpinner.visibility = View.GONE
        tvTitle.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black))
        home.setColorFilter(ContextCompat.getColor(activity?.baseContext!!, R.color.black), PorterDuff.Mode.SRC_ATOP)
        tvNext.setTextColor(ContextCompat.getColor(activity?.baseContext!!, R.color.black))
        home.setOnClickListener(this)
        sceneSelectedList.clear()
        scenesList.clear()
        if (scenesSelectedList.size > 0) {
            scenesList = scenesSelectedList
            for (s in scenesSelectedList) {
                for (rScene in s.rooms) {
                    for (r in roomsList) {
                        for (d in r.deviceList) {
                            for (dScene in rScene.deviceList) {
                                if ((dScene.index == d.index) && (dScene.deviceName == d.deviceName)) {
                                    d.isSelected = true
                                    break
                                }
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
        sceneAdapter = ButtonSceneSelectionAdapter(requireContext(), this)
        for (scene in scenes) {
            var flag = false
            for (s in scenesList) {
                if (s.title == scene.name) {
                    sceneSelectedList.add(s)
                    flag = true
                    break
                }
            }
            if (!flag) {
                scenesList.add(Scenes(scene.name, scene.icon, scene.room, false))
            }
        }
        if (scenesList.size > 0) {
            tvSelectScene.visibility = View.VISIBLE
        }
    }

    override fun getTitle(): String {
        return getString(R.string.text_select_devices)
    }

    override fun openNextScreen() {
        var leng: Int = 0
        roomDeviceList = getSelectedRoomDeviceData()
        for (m in sceneSelectedList) {
            m.isOn = false
            for (r in roomDeviceList) {
                for (l in m.rooms) {
                    for (list in l.deviceList) {
                        for (device in 0 until r.deviceList.size) {
                            if (list.deviceName == r.deviceList[device].deviceName && list.index == r.deviceList[device].index) {
                                r.deviceList.removeAt(device)
                                break
                            }
                        }
                    }
                }
            }

        }
        for (item in roomDeviceList) {
            leng += item.deviceList.size
        }
        if (sceneSelectedList.size > 0) {
            leng += sceneSelectedList.size
        }
        if(roomDeviceList[0].deviceList.size >0){
            buttonModel.load = roomDeviceList
        }

       // buttonModel.sceneSelectedList = sceneSelectedList
        if (leng == 0) {
            activity?.longToast("Please Select Loads..")
        } else {
            val intent = Intent(activity,SceneButtonActivity::class.java)
            intent.putExtra(Constant.PARAM_FRAGMENT, Constant.SET_ACTION_FRAGMENT)
            intent.putExtra(BUTTON_DATA,Gson().toJson(buttonModel))
            startActivity(intent)
        }
    }

    private fun updateDeviceSelection(data: MutableList<RoomModel>, viewType: Any) {
        for (s_ in data) {
            for (r in roomsList) {
                for (d in r.deviceList) {
                    for (dScene in s_.deviceList) {
                        if ((dScene.index == d.index) && (dScene.deviceName == d.deviceName)) {
                            d.isSelected = viewType as Boolean
                            break
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.home -> {
                activity?.onBackPressed()
            }
        }
    }
}