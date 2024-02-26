package com.wozart.aura.ui.selectdevices

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wozart.aura.R
import com.wozart.aura.ui.createautomation.CreateAutomationActivity
import com.wozart.aura.ui.createautomation.SetAutomationActivity
import com.wozart.aura.ui.setactions.SetActionsFragment
import com.wozart.aura.data.model.AutomationModel
import com.wozart.aura.ui.adapter.SceneSelectionAdapter
import com.wozart.aura.ui.auraSense.FavoriteButtonAdapter
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.base.baseselectdevices.BaseSelectAutomationDevices
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.*
import kotlinx.android.synthetic.main.fragment_automation_select_device.*
import kotlinx.android.synthetic.main.layout_header.*
import org.jetbrains.anko.longToast
import kotlin.collections.ArrayList

/**
 * Created by Saif on 11/20/2018.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */

class SelectDevicesFragment : BaseSelectAutomationDevices() {


    var scheduleTypeCheck: String? = null
    lateinit var sceneAdapter: SceneSelectionAdapter
    var scenesList = ArrayList<Scenes>()
    var remoteSelectedFavButton: MutableList<RemoteIconModel> = ArrayList()
    lateinit var btnAdapter: FavoriteButtonAdapter
    var hasProceed = false

    companion object {
        private var automationScheduleType: String? = null
        private var automationNameOld: String? = null
        var room: ArrayList<RoomModel> = ArrayList()
        var sceneSelectedList: MutableList<Scenes> = ArrayList()
        var motionData: String? = null
        var senseWhenTrigger: String? = null
        var rooms: MutableList<RoomModel> = ArrayList()
        var scheduleDetails = AutomationModel("title", 0, room, false, " ", " ", " ", " ", "")
        fun newInstance(scheduleData: AutomationModel, automationSceneNameOld: String, automationSceneType: String, motionData: String, triggerWhen: String?): SelectDevicesFragment {
            val scheduleDetail = scheduleData
            scheduleDetails = scheduleDetail
            automationNameOld = automationSceneNameOld
            automationScheduleType = automationSceneType
            this.motionData = motionData
            this.senseWhenTrigger = triggerWhen
            return SelectDevicesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_automation_select_device, container, false)
        if (context is CreateAutomationActivity) {
            automationScheduleType = (context as CreateAutomationActivity).getAutomationSceneType()
            automationNameOld = (context as CreateAutomationActivity).getAutomationSceneName()
        }
        if (context is SetAutomationActivity) {
            automationNameOld = (context as SetAutomationActivity).getAutomationOldName()
            automationScheduleType = (context as SetAutomationActivity).getAutomationType()
            scheduleTypeCheck = (context as SetAutomationActivity).getScheduleType()

        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setAdapter()
    }

    fun setAdapter() {
        if (scenesList.size > 0) {
            listScenesdata.adapter = sceneAdapter
            listScenesdata.layoutManager = GridAutoFitLayoutManager(context, resources.getDimensionPixelSize(R.dimen.device_item_size))
            listScenesdata.setHasFixedSize(true)
            sceneAdapter.dataSet(scenesList)
            val spacing = resources.getDimensionPixelSize(R.dimen.uniform_half_spacing)
            listScenesdata.addItemDecoration(GridListSpacingItemDecoration(spacing))
        }
    }

    override fun openNextScreen() {
        var leng: Int = 0
        rooms = getSelectedRoomDeviceData()
        // sceneSelectedList = scenesSelectedList
        for (m in sceneSelectedList) {
            m.isOn = false
            for (r in rooms) {
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

        hasProceed = sceneSelectedList.size > 0 || remoteSelectedFavButton.size > 0 || (rooms.any { it.deviceList.size > 0 })

        if (!hasProceed) {
            activity?.longToast("Please Select Loads..")
        } else {
            if (scheduleTypeCheck == "geo") {
                mListener?.navigateToFragment(SetActionsFragment())
            } else {
                mListener?.navigateToFragment(SetActionsFragment.newInstance(rooms, scheduleDetails, automationNameOld?:"new", automationScheduleType!!, motionData, senseWhenTrigger,remoteSelectedFavButton)) //pass selected devices with rooms data
            }
        }
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is Scenes) {
            var flag = false
            for (s in 0 until sceneSelectedList.size) {
                flag = false
                if (sceneSelectedList[s].isOn && data.isOn) {
                    for (sDevice in sceneSelectedList[s].rooms) {
                        for (checkDevice in sDevice.deviceList) {
                            for (clickedScene in data.rooms) {
                                for (c in clickedScene.deviceList) {
                                    if ((c.deviceName == checkDevice.deviceName) && (c.index == checkDevice.index)) {
                                        flag = true
                                        break
                                    }
                                }

                            }
                        }
                    }
                    if (flag) {
                        updateDeviceSelection(data.rooms, viewType)
                    }
                }
            }
            if (!flag) {
                updateDeviceSelection(data.rooms, viewType)
            }

            var scenFlag = false
            for (s in 0 until sceneSelectedList.size) {
                scenFlag = false
                if (sceneSelectedList[s] == data) {
                    if (!data.isOn) {
                        sceneSelectedList.removeAt(s)
                        scenFlag = true
                        break
                    }
                }
            }
            if (!scenFlag) {
                sceneSelectedList.add(data)
            }
        }

        if(data is RemoteIconModel){
            var flag = false
            for (remote in remoteSelectedFavButton) {
                flag = false
                if (remote.remoteButtonName == data.remoteButtonName) {
                    remoteSelectedFavButton.remove(remote)
                    flag = true
                    break
                }
            }
            if (!flag) {
                remoteSelectedFavButton.add(data)
            }
        }

        if (data is Device) {
            var flag = false
            for (s in 0 until sceneSelectedList.size) {
                flag = false
                if (sceneSelectedList[s].isOn && !data.isSelected) {
                    for (sDevice in sceneSelectedList[s].rooms) {
                        for (checkDevice in sDevice.deviceList) {
                            if ((data.deviceName == checkDevice.deviceName) && (data.index == checkDevice.index)) {
                                flag = true
                                break
                            }
                        }
                    }
                    if (flag) {
                        updateSceneAppearance(sceneSelectedList[s])
                    }
                }
            }
        }
    }

    private fun updateSceneAppearance(scenes: Scenes) {
        for (s in 0 until sceneSelectedList.size) {
            if (sceneSelectedList[s] == scenes) {
                sceneSelectedList[s].isOn = !sceneSelectedList[s].isOn
                break
            }
            sceneAdapter.notifyDataSetChanged()
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

    override fun showSceneInputs(): Boolean {
        return false
    }

    override fun getTitle(): String {
        return getString(R.string.text_select_devices)
    }

    fun initialize() {
        btnAdapter = FavoriteButtonAdapter(requireActivity(), this)
        btnAdapter.showSelection = true
        rlSpinner.visibility = View.GONE
        tvTitle.setTextColor(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        tvNext.setTextColor(ContextCompat.getColor(requireActivity().baseContext, R.color.black_d_n))
        sceneSelectedList.clear()
        scenesList.clear()
        if (remoteFavButton.size > 0) {
            tvFavoriteButton.visibility = View.VISIBLE
            rvFavButton.visibility = View.VISIBLE
            btnAdapter.setData(remoteFavButton)
            rvFavButton.adapter = btnAdapter
        }
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
        sceneAdapter = SceneSelectionAdapter(requireContext(), this)
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
}
