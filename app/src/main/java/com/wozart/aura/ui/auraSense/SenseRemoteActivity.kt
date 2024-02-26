package com.wozart.aura.ui.auraSense

import android.app.Dialog
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.R
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.entity.model.irModel.LearnRemoteDataModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.entity.model.irModel.UniversalIrModel
import com.wozart.aura.data.sqlLite.AuraSenseTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.entity.service.AwsPubSub
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.GridAutoFitLayoutManager
import com.wozart.aura.ui.dashboard.GridListSpacingItemDecoration
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.DoubleBtnDialog
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.dialogue_remote_learn.*
import kotlinx.android.synthetic.main.progress_listner_dialog.*
import kotlinx.android.synthetic.main.sense_remote_activity.*
import kotlin.concurrent.thread


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-01-30
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 *****************************************************************************/
class SenseRemoteActivity : BaseAbstractActivity(), EspHandler.OnEspHandlerMessage {


    private var localSqlUtils = UtilsTable()
    private var localSqlRemote = AuraSenseTable()
    private var mdbSense: SQLiteDatabase? = null
    var mdbUtils: SQLiteDatabase? = null
    lateinit var senseAdapter: SenseRemoteAdapter
    var getConfiguredRemote: MutableList<RemoteModel> = ArrayList()
    private var jsonHelper: JsonHelper = JsonHelper()
    private lateinit var gson: Gson
    var remoteListData = RemoteListModel()
    private lateinit var nsd: Nsd
    var progress: ProgressBar? = null
    var flag = false
    var IP = IpHandler()
    private var testBtnList = RemoteIconModel()
    var chechBtnWorked = true
    var learbtncount = 0
    private var awsPubSub: AwsPubSub? = null
    internal var mBounded: Boolean = false
    private var serviceConnection = false
    var rulesTableHandler = RulesTableHandler()
    var totalBtnSize = 0
    var learningStarted = false
    var learningdialog: Dialog? = null
    var espHandler: EspHandler? = null
    var localControlFlag = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sense_remote_activity)
        val dbUtils = UtilsDbHelper(this)
        mdbUtils = dbUtils.writableDatabase
        val senseDbHelper = AuraSenseDbHelper(this)
        mdbSense = senseDbHelper.writableDatabase
        Common.channelList.clear()
        Common.numberChannelList.clear()
        val remoteData: String = intent?.getStringExtra("remote")!!
        val titleText = intent.getStringExtra(Constant.REMOTE)
        text_temp.text = Constant.TEMP
        aura_sense.text = titleText?.toString()
        gson = Gson()
        remoteListData = gson.fromJson(remoteData, RemoteListModel::class.java)
        for (l in IP.getIpDevices()) {
            if (l.name == remoteListData.auraSenseName) {
                remoteListData.senseIp = l.ip ?: ""
                break
            }
        }
        nsd = Nsd()
        nsd.getInstance(this, "HOME")
        nsdDiscovery()
        LocalBroadcastManager.getInstance(this).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        totalBtnSize = remoteListData.dynamicRemoteIconList.size
        if (remoteListData.dataType == "rOld" && !remoteListData.isDownloadedRemote) {
            tvSave.text = getString(R.string.edit)
            learbtncount = remoteListData.dynamicRemoteIconList.size
        }
        if (intent.getStringExtra(Constant.DEVICE_TYPE) == "edit") {
            learbtncount = remoteListData.dynamicRemoteIconList.size
        }
        init()

        tvSave.setOnClickListener {
            if (remoteListData.dataType == "rOld" && !remoteListData.isDownloadedRemote) {
                startActivity(Intent(this@SenseRemoteActivity, CreateSenseRemote::class.java)
                        .putExtra("REMOTE_DATA", Gson().toJson(remoteListData)).putExtra(Constant.DEVICE_TYPE, "edit"))
                finish()
            } else {
//                if (learbtncount < totalBtnSize) {
//                    SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.please_learn_all)).show()
//                } else {
                    circularProgress.visibility = View.VISIBLE
                    saveAllRemote()
               // }
            }
        }

    }

    fun onbackPressed(view: View) {
        goBack()
    }

    private fun goBack() {
        if (remoteListData.dataType == "rNew" || remoteListData.isDownloadedRemote) {
            DoubleBtnDialog.with(this).setHeading(getString(R.string.alert))
                    .setMessage(getString(R.string.going_back_will_discard))
                    .setCallback(object : DoubleBtnDialog.OnActionPerformed {
                        override fun negative() {

                        }

                        override fun positive() {
                            onBackPressedRemoteDelete()
                            startActivity(Intent(this@SenseRemoteActivity, CreateSenseRemote::class.java)
                                    .putExtra("REMOTE_DATA", Gson().toJson(remoteListData)).putExtra(Constant.DEVICE_TYPE, "edit"))
                            finish()
                        }

                    }).show()
        } else {
            finish()
        }
    }

    private fun nsdDiscovery() {

        nsd.setBroadcastType("HOME")
        nsd.initializeNsd()
        nsd.discoverServices()
    }

    private var onNSDServiceResolved = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val name = p1!!.getStringExtra("name")
            val ip = p1.getStringExtra("ip")
            val device = name!!.substring(name.length - 6, name.length)
            if (remoteListData.auraSenseName == device) {
                remoteListData.senseIp = ip!!.toString()
            }
        }
    }

    fun init() {
        learningdialog = Dialog(this)
        if (remoteListData.typeAppliances == "AC") {
            default_ac_btn_layout.visibility = View.VISIBLE
            ac_remote_temp_btn_list.visibility = View.VISIBLE
            Glide.with(this).load(R.drawable.ac_wave).into(ivAc)
        }
        getConfiguredRemote = localSqlUtils.getRemoteData(mdbUtils!!, "remote_device")

        senseAdapter = SenseRemoteAdapter(this, remoteListData.dynamicRemoteIconList, remoteListData.typeAppliances!!) { remote_button: RemoteIconModel, remote_btn_name, b ->
            if (b) {
                openDialogue(remote_button, remote_btn_name)
            } else {
               // if (remoteListData.dataType == "rOld") {
                    remoteTriggerAction(remote_button)
              /*  } else if (!remote_button.dType.isNullOrEmpty()) {
                    remoteTriggerAction(remote_button)
                }*/
            }

        }
        ac_remote_temp_btn_list.layoutManager = GridAutoFitLayoutManager(this, resources.getDimensionPixelSize(R.dimen.device_item_size))
        ac_remote_temp_btn_list.setHasFixedSize(true)
        val spacing = resources.getDimensionPixelSize(R.dimen.normal_margin)
        ac_remote_temp_btn_list.addItemDecoration(GridListSpacingItemDecoration(spacing))
        ac_remote_temp_btn_list.adapter = senseAdapter

    }

    private fun openDialogue(remoteButton: RemoteIconModel, remoteBtnName: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialogue_remote_learn)
        dialog.tvor.visibility = View.VISIBLE
        dialog.switchFav.visibility = View.VISIBLE
        dialog.switchFav.isChecked = remoteButton.btnFavourite
        dialog.setCanceledOnTouchOutside(true)
        dialog.learn_remote.setOnClickListener {
            learningStarted = true
            if (tvSave.text == getString(R.string.edit)) tvSave.text = getString(R.string.text_save)
            if (remoteListData.dataType == "rOld") remoteListData.dataType = "rNew"
            startLearning(remoteButton, remoteBtnName, "edit")
            dialog.dismiss()
        }
        dialog.switchFav.setOnCheckedChangeListener { buttonView, isChecked ->
            for (r in remoteListData.dynamicRemoteIconList) {
                if (r.remoteButtonName == remoteButton.remoteButtonName) {
                    r.btnFavourite = isChecked
                    r.remoteModel = remoteListData.modelNumber
                    r.name = remoteListData.auraSenseName
                }
            }
            if (tvSave.text == getString(R.string.edit)) tvSave.text = getString(R.string.text_save)
            if (remoteListData.dataType == "rOld") remoteListData.dataType = "rNew"
            remoteButton.btnFavourite = isChecked
        }
        dialog.cancel_btn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openTestDialog(remoteIconModel: RemoteIconModel, btnName: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialogue_remote_learn)
        dialog.tvor.visibility = View.GONE
        dialog.switchFav.visibility = View.GONE
        dialog.setCanceledOnTouchOutside(true)
        dialog.tv_title.text = getString(R.string.test_remote)
        dialog.message.text = getString(R.string.desplay_test_remote)
        dialog.learn_remote.text = "Test"

        dialog.learn_remote.setOnClickListener {
            if (chechBtnWorked) {
                chechBtnWorked = false
                dialog.learn_remote.text = getString(R.string.confirmed)
                dialog.cancel_btn.visibility = View.VISIBLE
                appliancesAction(remoteIconModel, remoteIconModel.remoteButtonName!!)
            } else {
                learbtncount += 1
                sendConfirmationDevice(remoteIconModel)
                chechBtnWorked = true
                dialog.dismiss()
            }
        }

        dialog.cancel_btn.setOnClickListener {
            chechBtnWorked = true
            startLearning(remoteIconModel, remoteIconModel.remoteButtonName!!, "edit")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun sendConfirmationDevice(remoteButton: RemoteIconModel) {
        val dataSend = jsonHelper.confirmRemoteData(remoteButton, remoteListData.senseUiud, remoteListData.modelNumber)
        sendCommandToHandler(dataSend, remoteButton.remoteButtonName)

    }

    private fun startLearning(remoteButton: RemoteIconModel, name: String, type: String) {
        showLearningDialog()
        sendDataToLearn(remoteButton, name)
    }

    private fun sendCommandToHandler(dataSend: String, remoteButtonName: String?) {
        sendEspHandler(dataSend, remoteListData.senseIp, remoteListData.auraSenseName!!, remoteButtonName!!)
    }


    private fun saveAllRemote() {

        if (localSqlRemote.insertRemote(mdbSense!!, remoteListData.auraSenseName!!, remoteListData.remoteName!!, remoteListData.brandName!!, remoteListData.modelNumber!!, gson.toJson(remoteListData.dynamicRemoteIconList), "", remoteListData.typeAppliances
                        ?: "AC", remoteListData.remoteLocation!!, (Constant.HOME
                        ?: PreferenceManager.getDefaultSharedPreferences(this).getString("HOME", ""))!!)) {
            thread {
                val userId = Constant.IDENTITY_ID
                rulesTableHandler.updateAuraSenseRemote(remoteListData, userId!!, type = "create")
                val universalIrModel = UniversalIrModel()
                universalIrModel.brandName = remoteListData.brandName
                universalIrModel.modelNumber = remoteListData.modelNumber
                universalIrModel.typeAppliances = remoteListData.typeAppliances
                val typeToken = object : TypeToken<MutableList<LearnRemoteDataModel>>() {}.type
                val remoteListGson = Gson().toJson(remoteListData.dynamicRemoteIconList)
                universalIrModel.buttons = Gson().fromJson(remoteListGson,typeToken)
                val data = gson.toJson(universalIrModel)
                if (remoteListData.dataType == "rNew") {
                    storeRemote(data)
                }
                runOnUiThread {
                    circularProgress.visibility = View.GONE
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.exist_remote)).show()
        }

    }

    private fun storeRemote(message: String) {
        RemoteStoreToCloud.storeRemoteToCloud(message, remoteListData.dataType.toString())

    }

    private fun remoteTriggerAction(remoteButton: RemoteIconModel) {
        if (remoteButton.name == remoteListData.auraSenseName) {
            if (localControlFlag) {
                val dataTrigger = jsonHelper.triggerRemoteButton(remoteButton, remoteListData.modelNumber, remoteListData.senseUiud!!)
                sendCommandToHandler(dataTrigger, remoteButton.remoteButtonName)
            } else {
                controlCloudRemote(remoteButton.remoteButtonName ?: "")
            }

        }
    }

    private fun appliancesAction(remoteButton: RemoteIconModel, remoteBtnName: String) {
        if (remoteButton.name == remoteListData.auraSenseName) {
            var dataSend: String? = null
            if (remoteButton.dType.isNullOrEmpty()) {
                remoteButton.dType = "PRV"
                dataSend = "{\"type\":4,\"dType\":\"${remoteButton.dType}\",\"s1\":${remoteButton.start2},\"s2\":${remoteButton.start2},\"ze\":${remoteButton.zero_end},\"oe\":${remoteButton.one_end},\"hex\":\"${remoteButton.senseHex}\",\"uiud\":\"${remoteListData.senseUiud}\"}"
            } else if (remoteButton.dType == "RAW") {
                dataSend = "{\"type\":4,\"dType\":\"${remoteButton.dType}\",\"rArr\": ${remoteButton.rArr?.asList()},\"sz\":${remoteButton.sz},\"fz\":${remoteButton.fz},\"uiud\":\"${remoteListData.senseUiud}\"}"
            } else if (remoteButton.dType == "AC") {
                dataSend = "{\"type\":4,\"dType\":\"${remoteButton.dType}\",\"state\":${remoteButton.state.asList()},\"proto\":${remoteButton.proto},\"sz\":${remoteButton.sz},\"uiud\":\"${remoteListData.senseUiud}\"}"
            } else if (remoteButton.dType == "RMT") {
                dataSend = "{\"type\":4,\"dType\":\"${remoteButton.dType}\",\"value\":${remoteButton.value},\"proto\":${remoteButton.proto},\"sz\":${remoteButton.sz},\"uiud\":\"${remoteListData.senseUiud}\"}"
            }
            sendCommandToHandler(dataSend!!, remoteButton.remoteButtonName)
        }
    }

    private fun sendDataToLearn(remoteButton: RemoteIconModel, name: String) {
        val learingData = "{\"type\":3,\"uiud\":\"${remoteListData.senseUiud}\",\"model\":\"${remoteListData.modelNumber}\",\"btnName\":\"${remoteButton.remoteButtonName}\"}"
        sendCommandToHandler(learingData, remoteButton.remoteButtonName)
    }

    override fun onEspHandlerMessage(decryptedData: String, btn_name: String) {
        AppExecutors().mainThread().execute {
            updateData(decryptedData, btn_name)
        }
    }

    private fun controlCloudRemote(btn_name: String) {
        pusblishDataToShadow(remoteListData.senseThing
                ?: "", jsonHelper.serializeRemoteControl(remoteListData.senseUiud
                ?: "", remoteListData, btn_name))
    }

    private fun updateData(message: String, btn_name: String) {
        if (message.contains("ERROR")) {
            val deviceErrorName: String
            val data = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            deviceErrorName = data[1]
            if (learningStarted) {
                learningdialog?.dismiss()
                SingleBtnDialog.with(this).setHeading(getString(R.string.alert))
                        .setMessage(getString(R.string.error_learning)).show()
            } else {
                localControlFlag = false
                if (remoteListData.auraSenseName == deviceErrorName) {
                    if (remoteListData.senseThing != null) {
                        controlCloudRemote(btn_name)
                    }
                }
            }

        } else {
            val remote_data = jsonHelper.deserializeRemoteData(message)
            localControlFlag = true
            when (remote_data.type) {
                1 -> {
                    if (remoteListData.auraSenseName == remote_data.name) {
                        localControlFlag = true
                        flag = true
                    }
                }
                3 -> {
                    if (remote_data.error == 1) {
                        learningdialog?.dismiss()
                        SingleBtnDialog.with(this).setHeading(getString(R.string.alert))
                                .setMessage(getString(R.string.please_press_button_remote)).show()
                    } else {
                        if (remoteListData.auraSenseName == remote_data.name) {
                            for (btn_data in remoteListData.dynamicRemoteIconList) {
                                if (btn_data.remoteButtonName == btn_name) {
                                    btn_data.name = remote_data.name
                                    btn_data.dType = remote_data.dType
                                    btn_data.remoteModel = remoteListData.modelNumber
                                    btn_data.fz = remote_data.fz
                                    btn_data.sz = remote_data.sz
                                    btn_data.proto = remote_data.proto
                                    btn_data.rArr = remote_data.rArr
                                    btn_data.state = remote_data.state
                                    btn_data.value = remote_data.value
                                    btn_data.isSelected = true
                                    testBtnList = btn_data
                                    break
                                }
                            }
                            learningStarted = false
                            learningdialog?.dismiss()
                            openTestDialog(testBtnList, btn_name)
                            senseAdapter.notifyDataSetChanged()
                        }
                    }
                }
                4 -> {

                }
                13 -> {
                    if (remote_data.error == 0) {
                        Toast.makeText(this, getString(R.string.data_send_to_device), Toast.LENGTH_SHORT).show()
                    }
                }
                14 -> {
                    if (remote_data.error == 0) {
                        Toast.makeText(this, getString(R.string.action_executed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    /**
     * AWS IoT Subscribe to shadow broadcast receiver
     */

    override fun onStart() {
        val mIntent = Intent(this, AwsPubSub::class.java)
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE)
        super.onStart()
    }

    private var mConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
            mBounded = false
            awsPubSub = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mBounded = true
            val mLocalBinder = service as AwsPubSub.LocalAwsBinder
            awsPubSub = mLocalBinder.getServerInstance()
            serviceConnection = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (mBounded) {
            unbindService(mConnection)
            mBounded = false
        }
    }

    private fun pusblishDataToShadow(thing: String, data: String) {
        if (awsPubSub != null) {
            awsPubSub!!.publishToAws(thing, data)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goBack()
    }

    private fun showLearningDialog() {
        val inflate = LayoutInflater.from(this).inflate(R.layout.progress_listner_dialog, null)
        learningdialog?.setContentView(inflate)
        learningdialog?.window?.setGravity(Gravity.BOTTOM)
        learningdialog?.setCancelable(false)
        learningdialog?.setCanceledOnTouchOutside(false)
        learningdialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        learningdialog?.btnStop!!.setOnClickListener {
            learningdialog?.dismiss()
        }
        learningdialog?.show()
    }


    private fun onBackPressedRemoteDelete() {
        val learingData = "{\"type\":13,\"dType\":\"delete\",\"uiud\":\"${remoteListData.senseUiud}\",\"model\":\"${remoteListData.modelNumber}\"}"
        sendEspHandler(learingData, remoteListData.senseIp, remoteListData.auraSenseName!!, Constant.REMOTE_DELETE)
    }

    private fun sendEspHandler(data: String, ip: String, name: String, type: String) {
        AppExecutors().diskIO().execute {
            try {
                if (espHandler == null) {
                    espHandler = EspHandler(this)
                }
                espHandler?.getResponseData(data, ip, name, type)
            } catch (e: Exception) {
                Log.d("error", "Error in ESP_Handler_Connection")
            }
        }
    }

}