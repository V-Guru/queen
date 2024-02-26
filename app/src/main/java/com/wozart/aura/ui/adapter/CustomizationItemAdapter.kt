package com.wozart.aura.ui.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import aura.wozart.com.aura.entity.amazonaws.models.nosql.ThingTableDO
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.Customization
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.aura.ui.dashboard.rooms.AddRoomActivity
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.AuraSenseTable
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.Scenes
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.listener.UpgradeFirmwareListener
import com.wozart.aura.ui.dashboard.listener.onStartActivity
import com.wozart.aura.ui.dashboard.model.RoomUpdateModel
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.*
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.dialogue_room.*
import kotlinx.android.synthetic.main.item_customization.view.*

class CustomizationItemAdapter(var context: Context, val pos: Int, val adapter: CustomizationAdapter, private val label: String, val arrayList: MutableList<Customization>, val userType: Boolean, onItemDelete: onItemDeletedListener, private val auraSwitches: MutableList<AuraSwitch>, var onStartActivity: onStartActivity) : androidx.recyclerview.widget.RecyclerView.Adapter<CustomizationItemAdapter.ItemViewHolder>(), RecyclerItemClicked {

    private var selectedPosition = -1
    private val localSqlDatabase = DeviceTable()
    private val localSqlScene = SceneTable()
    private var localSqlRemote = AuraSenseTable()
    private var mdbSense: SQLiteDatabase? = null
    private var mDb: SQLiteDatabase? = null
    private var mDbScene: SQLiteDatabase? = null
    var mDbUtils: SQLiteDatabase? = null
    var IpListDevices: MutableList<IpModel> = ArrayList()
    private var jsonHelper: JsonHelper = JsonHelper()
    val localSqlUtils = UtilsTable()
    var scenes = ArrayList<Scene>()
    var thing: ThingTableDO? = null
    private var rulesTableDo = RulesTableHandler()
    private var itemListener: onItemDeletedListener = onItemDelete
    var builder = AlertDialog.Builder(context)
    lateinit var dialogueAdapter: DialogueAdapter
    private var listRoom: MutableList<RoomModelJson> = ArrayList()
    private var roomList: MutableList<RoomUpdateModel> = ArrayList()
    var selectedRoomName: String? = null
    var senseDeviceList: MutableList<RemoteModel> = ArrayList()
    var sceneList: MutableList<Scene> = ArrayList()
    var upgradeListener: UpgradeFirmwareListener? = null
    private var IP = IpHandler()

    interface onItemDeletedListener {
        fun onItemDelete(name: String, ip: String, uiud: String, esp_name: String)
    }

    class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customization, parent, false)
        val dbHelper = DeviceDbHelper(parent.context)
        mDb = dbHelper.writableDatabase
        val scenDbHelper = SceneDbHelper(parent.context)
        mDbScene = scenDbHelper.writableDatabase
        val dbUtils = UtilsDbHelper(parent.context)
        mDbUtils = dbUtils.writableDatabase
        val senseDbHelper = AuraSenseDbHelper(parent.context)
        mdbSense = senseDbHelper.writableDatabase
        initialize()
        return ItemViewHolder(view)
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        val roomUpdated = data as RoomUpdateModel
        if (roomUpdated.isSelected) {
            selectedRoomName = roomUpdated.roomName
        }
        adapter.notifyDataSetChanged()
    }

    fun initialize() {
        dialogueAdapter = DialogueAdapter(context, this)
        roomList.clear()
        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        IpListDevices = localSqlUtils.getIpList(mDbUtils!!, "device")
        for (dummy in listRoom) {
            if (dummy.type == "room") {
                if ((dummy.sharedHome == Constant.HOME) or (dummy.sharedHome == "default")) {
                    val roomUpdate = RoomUpdateModel()
                    roomUpdate.roomName = dummy.name
                    roomList.add(roomUpdate)
                }
            }
        }
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemView.layoutExtended?.visibility = if (position == selectedPosition && userType) View.VISIBLE else View.GONE
        holder.itemView.layout_upgrade?.visibility = if (position == selectedPosition && userType) View.VISIBLE else View.GONE
        for (connect in auraSwitches) {
            if (arrayList[position].deviceName == connect.name) {
                upgrade(holder, connect.esp_device.toString(), arrayList[position].deviceName!!, connect.ip, connect.uiud, true, connect.mdl)
                if (connect.CloudPresence || (connect.aws == 1)) {
                    holder.itemView.device_status?.visibility = View.VISIBLE
                    holder.itemView.device_status?.text = context.getString(R.string.connected)
                    holder.itemView.layoutConnect?.visibility = View.GONE
                } else {
                    holder.itemView.device_status?.visibility = View.VISIBLE
                    holder.itemView.device_status?.text = context.getString(R.string.not_connected)
                    holder.itemView.layoutConnect?.visibility = View.GONE
                }
                break
            }
        }

        holder.itemView.layoutDelete?.setOnClickListener {
            Utils.showCustomDialog(holder.itemView.context, holder.itemView.context.getString(R.string.title_delete_appliance), holder.itemView.context.getString(R.string.dialog_delete_appliance), R.layout.dialog_layout, object : DialogListener {
                override fun onOkClicked() {
                    holder.itemView.loader.visibility = View.VISIBLE
                    deleteDevice(holder, position)
                }

                override fun onCancelClicked() {
                }
            })
        }

        holder.itemView.layout_upgrade?.setOnClickListener {
            Utils.showCustomDialog(holder.itemView.context, holder.itemView.context.getString(R.string.upgrade_firmware), holder.itemView.context.getString(R.string.details_upgradation), R.layout.dialog_layout, object : DialogListener {
                override fun onOkClicked() {
                    holder.itemView.loader.visibility = View.VISIBLE
                    var ip: String? = null
                    var name: String? = null
                    var uiud: String? = null
                    var deviceFulName: String? = null
                    var device_mdl: Int? = null
                    var upgradeCheck = false
                    for (deviceUiud in auraSwitches) {
                        upgradeCheck = false
                        if ((arrayList[position].deviceName == deviceUiud.name) && (deviceUiud.CloudPresence || deviceUiud.aws == 1 || deviceUiud.error == 0)) {
                            upgradeCheck = true
                            name = deviceUiud.name
                            ip = deviceUiud.ip
                            uiud = deviceUiud.uiud
                            deviceFulName = deviceUiud.esp_device
                            device_mdl = deviceUiud.mdl
                            break
                        }
                    }
                    if (upgradeCheck) {
                        upgrade(holder, deviceFulName.toString(), name!!, ip, uiud.toString(), false, device_mdl!!)
                    } else {
                        holder.itemView.loader.visibility = View.GONE
                        SingleBtnDialog.with(context).setMessage(context.getString(R.string.issue_upgrading)).show()
                    }
                }

                override fun onCancelClicked() {

                }
            })
        }

        holder.itemView.layoutDetails?.setOnClickListener {
            var device = localSqlDatabase.getDevice(mDb!!, arrayList[position].deviceName!!)
            for (switch in auraSwitches) {
                if (switch.name == arrayList[position].deviceName!!) {
                    if (device.aura_sense_confi.size == 0) {
                        switch.room = device.room
                        device = switch
                    }
                    device.esp_device = switch.esp_device
                    break
                }
            }
            showDialogue(device)
        }

        holder.itemView.setOnClickListener {
            onItemLongClick(position)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(position)
            false
        }
    }

    private fun deleteDevice(holder: ItemViewHolder, position: Int) {
        holder.itemView.loader.visibility = View.VISIBLE
        var uiud = localSqlDatabase.getUiud(mDb!!, arrayList[position].deviceName!!)
        val device = arrayList[position].deviceName
        var senseDelete = false
        var sense = RemoteModel()
        if (uiud != null) {
            AppExecutors().diskIO().execute {
                if (Encryption.isInternetWorking()) {
                    val auraDevice = localSqlDatabase.getDevice(mDb!!, device!!)
                    rulesTableDo.deleteDevice(auraDevice, Constant.DEVICE_TYPE)
                    AppExecutors().mainThread().execute {
                        localSqlDatabase.deleteDevice(mDb!!, device)
                        var rmv = IpModel()
                        for (l in IpListDevices) {
                            if (l.name == arrayList[position].deviceName) {
                                if (!l.ip.isNullOrEmpty()) {
                                    itemListener.onItemDelete(arrayList[position].deviceName!!, l.ip.toString(), uiud!!, l.fullDeviceName.toString())
                                }
                                rmv = l
                                break
                            }
                        }
                        IpListDevices.remove(rmv)
                        IpHandler().removeIpDevice(rmv)
                        localSqlUtils.replaceIpList(mDbUtils!!, "device", IpListDevices)
                        holder.itemView.loader.visibility = View.GONE
                        arrayList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        adapter.notifyItemChanged(position)
                        notifyDataSetChanged()
                    }
                } else {
                    AppExecutors().mainThread().execute {
                        holder.itemView.loader.visibility = View.GONE
                        val message = "You cannot delete device from remote or without internet"
                        SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(message).show()
                    }
                }
            }
        } else {
            val senseDeviceData = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
            for (d in 0 until senseDeviceData.size) {
                if (senseDeviceData[d].aura_sence_name == device) {
                    for (s in auraSwitches) {
                        if (s.name == senseDeviceData[d].aura_sence_name) {
                            senseDelete = true
                            // senseDeviceData[d].module = s.mdl
                            senseDeviceData[d].sense_ip = s.ip.toString()
                            sense = senseDeviceData[d]
                            senseDeviceData.removeAt(d)
                            break
                        }
                    }
                    break
                }
            }
            if (senseDelete) {
                AppExecutors().diskIO().execute {
                    if (Encryption.isInternetWorking()) {
                        val deviceData = AuraSwitch().apply {
                            name = device.toString()
                            uiud = sense.sense_uiud
                            thing = sense.scense_thing
                        }
                        if (sense.sense_loads.size > 1) rulesTableDo.deleteSenseDevice(sense) else rulesTableDo.deleteDevice(deviceData, Constant.UNIVERSAL_REMOTE)
                        val remoteList = localSqlRemote.getRemoteList(mdbSense!!, sense.aura_sence_name!!)
                        if (remoteList.size > 0) {
                            for (remote in remoteList) {
                                rulesTableDo.deleteRemote(Constant.IDENTITY_ID!!, remote.brandName!!, remote.modelNumber!!, Constant.HOME.toString())
                            }
                        }
                        AppExecutors().mainThread().execute {
                            IpListDevices.find { it.name == arrayList[position].deviceName!! }?.let {
                                itemListener.onItemDelete(arrayList[position].deviceName!!, it.ip
                                        ?: "", sense.sense_uiud!!, sense.aura_sense_full_name!!)
                                IpHandler().removeIpDevice(it)
                            }
                            localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", senseDeviceData)
                            localSqlRemote.deleteRemoteForSense(mdbSense!!, sense.aura_sence_name!!)
                            holder.itemView.loader.visibility = View.GONE
                            arrayList.removeAt(position)
                            adapter.notifyItemRemoved(position)
                            adapter.notifyItemChanged(position)
                            notifyDataSetChanged()
                        }
                    } else {
                        AppExecutors().mainThread().execute {
                            holder.itemView.loader.visibility = View.GONE
                            val message = "You cannot delete device from remote or without internet"
                            SingleBtnDialog.with(context).setHeading(context.getString(R.string.alert)).setMessage(message).show()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun upgrade(holder: ItemViewHolder, espDevice: String, name: String, ip: String?, uiud: String, showImage: Boolean, mdl: Int) {
        var upgradeType = ""
        holder.itemView.loader.visibility = View.GONE
        if (espDevice.contains("Sense") && !espDevice.contains("Sense Pro")) {
            if (showImage) {
                holder.itemView.device_name?.text = "Sense-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_sense)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_SENSE_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if (espDevice.contains("Wozart Switch Controller Pro") || mdl == 6) {
            if (showImage) {
                holder.itemView.device_name?.text = "Switch Pro-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_switch_controller)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_SWITCH_PRO_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if (mdl == 5) {
            if (showImage) {
                holder.itemView.device_name?.text = "Switch-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_switch_controller)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_FIVE_NODE_SWITCH_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if (espDevice.contains("Wozart Switch Controller Mini") || mdl == 2) {
            if (showImage) {
                holder.itemView.device_name?.text = "Switch Mini-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_switch_controller_mini)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_SWITCH_MINI_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if ((espDevice.contains("Wozart Switch Controller") || mdl == 4) && (!espDevice.contains("Wozart Switch Controller Pro") || !espDevice.contains("Wozart Switch Controller Mini") || !espDevice.contains("Aura Switch"))) {
            if (showImage) {
                holder.itemView.device_name?.text = "Switch-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_switch_controller)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_SWITCH_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if (espDevice.contains("Motor") || mdl == 3) {
            if (showImage) {
                holder.itemView.device_name?.text = "Motor-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_switch_controller_mini)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_CURTAIN_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if (espDevice.contains(Constant.DEVICE_LED) || mdl == 7 || mdl == 8) {

            if (showImage) {
                holder.itemView.device_name?.text = "LED-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_switch_controller_mini)
            } else {
                upgradeType = jsonHelper.upgradeType(if (mdl == 8) Constant.UPGRADE_TUNNABLE_URL else Constant.UPGRADE_LED_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }

        } else if (espDevice.contains("Plug") || mdl == 1) {
            if (showImage) {
                holder.itemView.device_name?.text = "Plug-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_smart_plug)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_PLUG_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if (espDevice.contains("Sense Pro")) {
            if (showImage) {
                holder.itemView.device_name?.text = "Sense Pro-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_sense)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_SENSE_PRO_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if (mdl == 14) {
            if (showImage) {
                holder.itemView.device_name?.text = "Fan Controller-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_switch_controller)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_FAN_URL, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if (espDevice.contains(Constant.DEVICE_UNIVERSAL_IR)) {
            if (showImage) {
                holder.itemView.device_name?.text = "Universal IR-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_sense)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_UNIVERSAL_IR, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else if (espDevice.contains("Aura Switch")) {
            if (showImage) {
                holder.itemView.device_name?.text = "Switch-$name"
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_switch_controller)
            } else {
                upgradeType = jsonHelper.upgradeType(Constant.UPGRADE_MARVEL_SWITCH, uiud, 0)
                upgradeListener?.onUpgradeDevice(pos, upgradeType, ip!!, name, Constant.UPGRADE_DEVICE)
            }
        } else {
            if (showImage) {
                holder.itemView.device_name?.text = name
                holder.itemView.IVdevice.setImageResource(R.drawable.ic_wozart_switch_controller)
            }
        }
    }

    var changingRoomName: String? = null
    private val roomsScenes = ArrayList<Scenes>()
    private fun showDialogue(device: AuraSwitch) {
        val dialogue = Dialog(context, R.style.full_screen_dialog)
        dialogue.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogue.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogue.setContentView(R.layout.dialogue_room)
        changingRoomName = device.room
        for (r in roomList) r.isSelected = r.roomName == device.room
        sceneList = localSqlScene.getAllScenes(mDbScene!!, (Constant.HOME
                ?: PreferenceManager.getDefaultSharedPreferences(context).getString("HOME", ""))!!)
        for (scene in sceneList) {
            for (s in scene.room) {
                if (s.name == changingRoomName) {
                    roomsScenes.add(Scenes(scene.name, scene.icon, scene.room, false))
                }
            }
        }
        if (roomList.size > 0) dialogueAdapter.setData(roomList)
        dialogue.recyclerview.adapter = dialogueAdapter
        dialogue.tvSubmit.setOnClickListener {
            if (device.aura_sense_confi.size == 0) {
                device.room = selectedRoomName!!
                saveData(device, sceneUpdate = sceneList.size > 0)
                dialogue.dismiss()
            } else {
                saveAuraSense(device)
                dialogue.dismiss()
            }
        }
        dialogue.rlAddRoom.setOnClickListener {
            val intent = Intent(context, AddRoomActivity::class.java)
            intent.putExtra(Constant.ROOM_CREATION, Constant.NEW_ROOM)
            intent.putExtra("ROOM_EDIT_TYPE", "create")
            context.startActivity(intent)
            dialogue.dismiss()
        }
        dialogue.ivClose.setOnClickListener {
            dialogue.dismiss()
        }
        dialogue.show()
    }

    private fun updateSceneRoom(device: AuraSwitch) {
        val gson = Gson()
        var updateScene: Boolean = false
        for (s in roomsScenes) {
            val dList: MutableList<Device> = ArrayList()
            val roomsList: ArrayList<String> = ArrayList()
            for (rooms in s.rooms) {
                for (dev in rooms.deviceList) {
                    if (dev.deviceName == device.name) {
                        dev.roomName = device.room
                        updateScene = true
                    }
                    rooms.name = dev.roomName
                    dList.add(dev)
                    var nameExists = false
                    for (n in roomsList) {
                        if (n == dev.roomName) {
                            nameExists = true
                        }
                    }
                    if (!nameExists) {
                        roomsList.add(dev.roomName)
                    }
                }
            }
            if (updateScene) {
                localSqlScene.updateScene(mDbScene!!, s.title.toString(), gson.toJson(s.rooms), gson.toJson(roomsList), Constant.HOME!!, s.iconUrl, s.title.toString())
            }
        }
    }

    private fun saveAuraSense(device: AuraSwitch) {
        senseDeviceList.clear()
        var flag = false
        var senseDevice = RemoteModel()
        for (d in localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")) {
            flag = false
            senseDevice = d
            if (d.aura_sence_name == device.name) {
                flag = true
                d.room = selectedRoomName
                senseDevice.room = selectedRoomName
                senseDeviceList.add(d)
                break
            }
        }
        if (!flag) {
            senseDeviceList.add(senseDevice)
        }
        localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", senseDeviceList)
        AppExecutors().diskIO().execute {
            if (senseDevice.sense_loads.size > 1) rulesTableDo.insertSenseDevice(senseDevice) else rulesTableDo.insertUniversalDevice(device)
            AppExecutors().mainThread().execute {
                onStartActivity.onStartActivity()
            }
        }

    }

    private fun saveData(device: AuraSwitch, sceneUpdate: Boolean) {
        val gson = Gson()
        if (device.thing == null) {
            device.thing = localSqlDatabase.getThingForDevice(mDb!!, device.name)
            localSqlDatabase.insertDevice(mDb!!, Constant.HOME!!, device.room, device.uiud, device.name, gson.toJson(device.loads), device.thing!!)
        } else {
            localSqlDatabase.insertDevice(mDb!!, Constant.HOME!!, device.room, device.uiud, device.name, gson.toJson(device.loads), device.thing!!)
        }

        auraSwitches.find { it.name == device.name }?.room = device.room
        for (iplist in IpListDevices) {
            if (iplist.room == changingRoomName) {
                iplist.room = device.room
                IP.registerIpDevice(iplist)
            }
        }
        localSqlUtils.replaceIpList(mDbUtils!!, "device", IpListDevices)
        if (sceneUpdate) {
            updateSceneRoom(device)
        }
        AppExecutors().diskIO().execute {
            rulesTableDo.insertDeviceLoads(device)
            AppExecutors().mainThread().execute {
                onStartActivity.onStartActivity()
            }
        }

    }

    private fun updateAwsDevice(auraSwitch: AuraSwitch): MutableList<AuraSwitch> {
        if (auraSwitch.aws == 1) {
            for (awsDeviceConnect in auraSwitches) {
                if (auraSwitch.name == awsDeviceConnect.name) {
                    awsDeviceConnect.aws = auraSwitch.aws
                    awsDeviceConnect.error = auraSwitch.error
                }
            }
        }
        notifyDataSetChanged()
        return auraSwitches
    }

    private fun onItemLongClick(position: Int) {
        for (i in 0 until arrayList.size) {
            if (i != pos)
                adapter.notifyItemChanged(i)
        }
        selectedPosition = if (position == selectedPosition) {
            -1
        } else position

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

}

