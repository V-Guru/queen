package com.wozart.aura.ui.createscene

import android.app.Dialog
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.cardview.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.wozart.aura.R
import com.wozart.aura.ui.base.baseselectdevices.BaseSelectDevicesFragment
import kotlinx.android.synthetic.main.fragment_select_devices.*
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.data.sqlLite.ScheduleTable
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.ui.adapter.AdvanceAutomationSelection
import com.wozart.aura.ui.auraSense.FavoriteButtonAdapter
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.selectdevices.SelectDevicesFragment
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.dialog_automation_selection.*
import kotlinx.android.synthetic.main.fragment_select_devices.rvFavButton
import kotlinx.android.synthetic.main.fragment_select_devices.tvFavoriteButton
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.toast
import kotlin.concurrent.thread


class SelectDeviceForSceneFragment : BaseSelectDevicesFragment(), RecyclerItemClicked {
    private val localSqlScene = SceneTable()
    private var mDbScene: SQLiteDatabase? = null
    var automationScene = ArrayList<AutomationScene>()
    private val localSqlDatabaseSchedule = ScheduleTable()
    private var mDBAutomation: SQLiteDatabase? = null
    private val localSqlDatabase = DeviceTable()
    private var mDb: SQLiteDatabase? = null
    lateinit var automationSelectionAdapter: AdvanceAutomationSelection
    var sceneNameOld: String? = null
    var sceneNameType: String? = null
    lateinit var tvTitle: TextView
    lateinit var tvNext: TextView
    var disableEnableAutomation: MutableList<AutomationScene> = ArrayList()
    lateinit var btnAdapter: FavoriteButtonAdapter
    var remoteSelectedFavButton: MutableList<RemoteIconModel> = ArrayList()
    var hasProceed = false


    override fun showSceneInputs(): Boolean {
        return true
    }

    override fun getTitle(): String {
        if ((context as CreateSceneActivity).getSceneType() == "create")
            return getString(R.string.title_create_scene)
        else
            return getString(R.string.title_edit_scene)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_devices, container, false)
    }

    fun deleteSceneFromLoads(scene: String) {
        val dbHelper = DeviceDbHelper(requireContext())
        mDb = dbHelper.writableDatabase
        val dummyRooms = localSqlDatabase.getRooms(mDb!!, Constant.HOME!!)
        for (x in dummyRooms) {
            val room = RoomModel()
            room.name = x.roomName
            roomsList.add(room)
        }
        if (!roomsList.isEmpty()) {
            for (x in roomsList) {
                val devices = localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, x.name!!)
                //NEWSCENES delete all scenes
            }
        }
    }

    override fun openNextScreen() {
        var len: Int = 0
        val selectedRooms = getSelectedRoomDeviceData()
        val sceneName = inputSceneName.text.toString()
        val sceneIcon = getIcon()
        hasProceed = remoteSelectedFavButton.size > 0 || (selectedRooms.any { it.deviceList.size > 0 })

        if (!hasProceed) {
            SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.please_select_loads)).show()
        } else {
            if (sceneName.isEmpty()) {
                inputSceneName.error = getString(R.string.please_enter_scene_name)
                inputSceneName.requestFocus()
            } else {
                if (selectedRooms.size == 0) {
                    SingleBtnDialog.with(context).setHeading(getString(R.string.alert)).setMessage(getString(R.string.please_add_device)).show()
                } else {
                    mListener?.navigateToFragment(SetActionsForSceneFragment.newInstance(selectedRooms, sceneName, sceneNameOld!!, sceneIcon, sceneNameType!!, disableEnableAutomation, remoteSelectedFavButton))
                }

            }
        }
    }

    private fun showDialog() {
        val dialog = Dialog(requireActivity(), R.style.full_screen_dialog)
        dialog.setContentView(R.layout.dialog_automation_selection)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        disableEnableAutomation.clear()
        if (automationScene.size > 0) {
            for (s in scene!!.room) {
                for (sDev in s.deviceList) {
                    for (auto in automationScene) {
                        if (sDev.name == auto.name) {
                            auto.isSelected = true
                            disableEnableAutomation.add(auto)
                            break
                        }
                    }
                }
            }
            automationSelectionAdapter.setData(automationScene, type = 0)
            dialog.recyclerview.adapter = automationSelectionAdapter
        }

        dialog.ivClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.tvSave.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize(view)
        setAdapter()
    }

    fun initialize(view: View) {
        val dbAutomation = ScheduleDbHelper(requireContext())
        mDBAutomation = dbAutomation.writableDatabase
        val dbScene = SceneDbHelper(requireContext())
        mDbScene = dbScene.writableDatabase
        btnAdapter = FavoriteButtonAdapter(requireActivity(), this)
        btnAdapter.showSelection = true
        automationSelectionAdapter = AdvanceAutomationSelection(requireContext(), this)
        automationScene = localSqlDatabaseSchedule.getAutomationScene(mDBAutomation!!, Constant.HOME!!)

        if (context is CreateSceneActivity) {
            sceneNameOld = (context as CreateSceneActivity).getSceneName()
            sceneNameType = (context as CreateSceneActivity).getSceneType()
        }
        if (sceneNameType == "edit") {
            val inputSceneName = view.findViewById<EditText>(R.id.inputSceneName)
            showSceneInputs()
            btnSceneDelete.visibility = View.GONE
            inputSceneName.setText(sceneNameOld)
        }

        cvEnableDisable.setOnClickListener {
            showDialog()
        }

        btnSceneDelete.setOnClickListener {
            localSqlScene.deleteScene(mDbScene!!, sceneNameOld!!, Constant.HOME!!)
            deleteSceneFromLoads(sceneNameOld!!)
            thread {
              //  sceneDynamoDb.deleteUserScene(Constant.IDENTITY_ID!!, Constant.HOME!!, sceneNameOld!!)
            }
            startActivity(activity?.intentFor<DashboardActivity>()!!.newTask())
            activity?.toast("Scene deleted")
        }

    }

    fun setAdapter() {
        if (remoteFavButton.size > 0) {
            tvFavoriteButton.visibility = View.VISIBLE
            rvFavButton.visibility = View.VISIBLE
            btnAdapter.setData(remoteFavButton)
            rvFavButton.adapter = btnAdapter
        }
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is RemoteIconModel) {
            var flag = false
            for (remote in remoteSelectedFavButton) {
                flag = false
                if (remote.remoteButtonName == data.remoteButtonName && remote.remoteModel == data.remoteModel) {
                    remoteSelectedFavButton.remove(remote)
                    flag = true
                    break
                }
            }
            if (!flag) {
                remoteSelectedFavButton.add(data)
            }
        } else if (data is AutomationScene) {
            var flag = false
            for (selected in disableEnableAutomation) {
                if ((data.name == selected.name) && (!data.isSelected)) {
                    disableEnableAutomation.remove(selected)
                    flag = true
                    break
                }
            }
            if (!flag && data.isSelected) {
                disableEnableAutomation.add(data)
            }
        }
    }
}
