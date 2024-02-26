package com.wozart.aura.ui.sceneController

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraSceneButton
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.adapter.SenseListAdapter
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.DialogListener
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_sense_selection.*
import kotlinx.android.synthetic.main.layout_header.*
import kotlin.concurrent.thread


/**
 * Created by Saif on 05/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */

class SenseSelectionActivity : AppCompatActivity(), RecyclerItemClicked, View.OnClickListener, EspHandler.OnEspHandlerMessage {

    private val localSqlUtils = UtilsTable()
    private var mDbUtils: SQLiteDatabase? = null
    private var senseDeviceList: MutableList<RemoteModel> = ArrayList()
    lateinit var adapter: SenseListAdapter
    var senseDevice: RemoteModel? = null
    private var jsonHelper: JsonHelper = JsonHelper()
    var rulesTableHandler = RulesTableHandler()
    private lateinit var nsd: Nsd
    val sceneButton = ButtonModel()
    var IpListDevices: MutableList<IpModel> = ArrayList()
    private var IP = IpHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sense_selection)
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase
        initalize()
        setAdapter()
        listener()
    }

    private fun initalize() {
        IpListDevices = IP.getIpDevices()
        nsd = Nsd()
        nsd.getInstance(this, "HOME")
        nsdDiscovery()
        LocalBroadcastManager.getInstance(this).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        senseDeviceList = localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")
        adapter = SenseListAdapter(this, this)
        tvTitle.text = getString(R.string.select_sense)
        sense_list.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
        sense_list.setHasFixedSize(true)
        sense_list.adapter = adapter
    }

    private fun nsdDiscovery() {
        nsd.setBroadcastType("HOME")
        nsd.initializeNsd()
        nsd.discoverServices()

    }

    private var onNSDServiceResolved = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val name = intent!!.getStringExtra("name")
            val ip = intent.getStringExtra("ip")
            val device = name!!.substring(name.length - 6, name.length)
            for (sense in localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")) {
                if (sense.aura_sence_name == device) {
                    sense.sense_ip = ip!!
                }
            }
        }

    }

    private fun setAdapter() {
        adapter.setData(senseDeviceList)
    }

    fun listener() {
        btnConnect.setOnClickListener(this)
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if (data is RemoteModel) {
            when (viewType) {
                R.id.cardView -> {
                    if (data.isSelected) {
                        senseDevice = data
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnConnect -> {
                if (senseDevice != null) {
                    showPopUp()
                } else {

                }

            }
        }
    }

    private fun showPopUp() {
        Utils.showCustomDialogOk(this, getString(R.string.configure_scene_control), getString(R.string.message_connection_button), R.layout.dialog_layout, object : DialogListener {
            override fun onOkClicked() {
                sendMessageForConnection()
            }

            override fun onCancelClicked() {

            }

        })
    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        runOnUiThread {
            updateData(decryptedData)
        }
    }

    private fun updateData(decryptedData: String) {
        if (decryptedData.contains("ERROR")) {

        } else {
            val data = jsonHelper.deserializeTcp(decryptedData)
            if (data.command == 8) {
                when (data.type) {
                    7 -> {
                        val buttonDataList = localSqlUtils.getButtonData(mDbUtils!!, "button_device")
                        var flag = true
                        for (button in buttonDataList) {
                            if (button.auraButtonName == data.name) {
                                button.unicastAddress = data.unicast
                                flag = false
                                break
                            }
                        }
                        if (flag) {
                            sceneButton.senseName = senseDevice!!.aura_sence_name
                            sceneButton.thing = senseDevice!!.scense_thing
                            val buttonModel = AuraSceneButton()
                            sceneButton.buttonList = buttonModel.buttonData()
                            sceneButton.unicastAddress = data.unicast
                            sceneButton.command = data.command
                            sceneButton.senseUiud = senseDevice!!.sense_uiud
                            sceneButton.senseMacId = senseDevice!!.senseMacId
                            sceneButton.room = intent.getStringExtra(Constant.BUTTON_ROOM)
                            sceneButton.auraButtonName = intent.getStringExtra(Constant.BUTTON_NAME)
                            buttonDataList.add(sceneButton)
                        }
                        localSqlUtils.replaceButtonData(mDbUtils!!, "button_device", buttonDataList)
                        thread {
                            rulesTableHandler.insertButtonDevice(sceneButton)
                            runOnUiThread {
                                val intent = Intent(applicationContext, DashboardActivity::class.java)
                                this.startActivity(intent)
                                this.finish()
                            }
                        }

                    }
                }
            } else if (data.command == 128) {
                showPopUpError(data.command!!)
            } else if (data.command == 2048) {
                showPopUpError(data.command!!)
            }
        }

    }

    private fun showPopUpError(command: Int) {
        if (command == 128) {
            SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.already_configured)).show()
        } else {
            SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.no_button_pressed)).show()
        }
    }

    fun sendMessageForConnection() {
        for (i in IpListDevices) {
            if (i.name == senseDevice!!.aura_sence_name) {
                senseDevice!!.sense_ip = i.ip!!
                break
            }
        }
        EspHandler(this).getResponseData(jsonHelper.auraButtonConfigure(senseDevice!!.sense_uiud.toString()), senseDevice!!.sense_ip, senseDevice!!.aura_sence_name.toString(), "")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onStart() {
        super.onStart()
    }
}