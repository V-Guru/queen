package com.wozart.aura.ui.auraSense

import android.app.Dialog
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.R
import com.wozart.aura.data.device.IpHandler
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.sqlLite.AuraSenseTable
import com.wozart.aura.entity.model.irModel.LearnRemoteDataModel
import com.wozart.aura.entity.model.irModel.UniversalIrModel
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.entity.network.Nsd
import com.wozart.aura.entity.service.AwsPubSub
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.adapter.ChannelListAdapter
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import com.wozart.aura.utilities.dialog.DoubleBtnDialog
import com.wozart.aura.utilities.dialog.NumberPadDialog
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_remote_new_layout.*
import kotlinx.android.synthetic.main.dialogue_remote_learn.*
import kotlinx.android.synthetic.main.layout_header.view.*
import kotlinx.android.synthetic.main.progress_listner_dialog.*
import org.jetbrains.anko.longToast
import kotlin.concurrent.thread


/**
 * Created by Saif on 01/02/21.
 * mds71964@gmail.com
 */

class RemoteCreateActivity : AppCompatActivity(), EspHandler.OnEspHandlerMessage, View.OnLongClickListener, RecyclerItemClicked {

    private var localSqlRemote = AuraSenseTable()
    private var mdbSense: SQLiteDatabase? = null
    var mdbUtils: SQLiteDatabase? = null
    private var jsonHelper: JsonHelper = JsonHelper()
    private lateinit var gson: Gson
    var remoteListData = RemoteListModel()
    private var nsd: Nsd? = null
    var learbtncount = 0
    var rulesTableHandler = RulesTableHandler()
    var localControlFlag = true
    var IP = IpHandler()
    var learningStarted = false
    var learningdialog: Dialog? = null
    private var test_btn_list = RemoteIconModel()
    lateinit var channelShortcutAdapter: ChannelListAdapter
    var channelShortCutList: MutableList<RemoteIconModel> = arrayListOf()
    var internetChannelList: MutableList<RemoteIconModel> = arrayListOf()
    var listInternetchannel = arrayListOf("Netflix", "Hotstar", "YouTube", "Amazon Prime")
    var dummyInternetChannelListt: MutableList<RemoteIconModel> = arrayListOf()
    private var awsPubSub: AwsPubSub? = null
    internal var mBounded: Boolean = false
    private var serviceConnection = false
    var espHandler: EspHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_new_layout)
        initialize()
        setListener()
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNSDServiceResolved)
        nsd = null
    }

    private fun pusblishDataToShadow(thing: String, data: String) {
        if (awsPubSub != null) {
            awsPubSub!!.publishToAws(thing, data)
        }
    }

    private fun setButtonView(remote: RemoteIconModel) {
        if (remote.remoteButtonName == Common.buttonList.get(0) && remote.isSelected) {
            rlPower.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(1) && remote.isSelected) {
            rlVolume.setBackgroundResource(R.drawable.rounded_layout)
        } else if (remote.remoteButtonName == Common.buttonList.get(2) && remote.isSelected) {
            rlVolume.setBackgroundResource(R.drawable.rounded_layout)
        } else if (remote.remoteButtonName == Common.buttonList.get(3) && remote.isSelected) {
            rlChannel.setBackgroundResource(R.drawable.rounded_layout)
        } else if (remote.remoteButtonName == Common.buttonList.get(4) && remote.isSelected) {
            rlChannel.setBackgroundResource(R.drawable.rounded_layout)
        } else if (remote.remoteButtonName == Common.buttonList.get(5) && remote.isSelected) {
            rlMute.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(6) && remote.isSelected) {
            rlTv.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(7) && remote.isSelected) {
            rlMenu.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(34) && remote.isSelected) {
            rlPause.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(35) && remote.isSelected) {
            rlForward.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(33) && remote.isSelected) {
            rlBackwrd.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(37) && remote.isSelected) {
            rlPlay.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(38) && remote.isSelected) {
            rlStop.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(39) && remote.isSelected) {
            rlSetting.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(21) && remote.isSelected) {
            rlHome.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(27) && remote.isSelected) {
            rlOK.setBackgroundResource(R.drawable.circle_no_theme)
        } else if (remote.remoteButtonName == Common.buttonList.get(29) && remote.isSelected) {
            rlBack.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (remote.remoteButtonName == Common.buttonList.get(40) && remote.isSelected) {
            rlReturn.setBackgroundResource(R.drawable.curved_rectangle_bg)
        } else if (isNumberKeyLearnCount >= 10) {
            rlReturn.setBackgroundResource(R.drawable.curved_rectangle_bg)
        }
    }

    override fun onResume() {
        super.onResume()
        if (nsd == null) {
            nsd = Nsd()
            nsd?.getInstance(this, "HOME")
        }
        nsdDiscovery()
        LocalBroadcastManager.getInstance(this).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
    }

    var defaultKeyPadAdded: Boolean = false
    fun initialize() {
        header.tvTitle.setTextColor(ContextCompat.getColor(baseContext, R.color.white))
        header.home.setColorFilter(ContextCompat.getColor(baseContext, R.color.white), PorterDuff.Mode.SRC_ATOP)
        header.tvNext.setTextColor(ContextCompat.getColor(baseContext, R.color.white))
        header.tvTitle.text = intent.getStringExtra(Constant.REMOTE)
        header.tvNext.text = getString(R.string.text_save)
        val dbUtils = UtilsDbHelper(this)
        mdbUtils = dbUtils.writableDatabase
        val senseDbHelper = AuraSenseDbHelper(this)
        mdbSense = senseDbHelper.writableDatabase
        val remoteData: String = intent?.getStringExtra("remote")!!
        gson = Gson()
        remoteListData = gson.fromJson(remoteData, RemoteListModel::class.java)

        for (l in IP.getIpDevices()) {
            if (l.name == remoteListData.auraSenseName) {
                remoteListData.senseIp = l.ip ?: ""
                break
            }
        }

        nsd = Nsd()
        nsd?.getInstance(this, "HOME")
        nsdDiscovery()
        LocalBroadcastManager.getInstance(this).registerReceiver(
                onNSDServiceResolved, IntentFilter("nsdDiscoverWifi"))
        internetChannelList = remoteListData.dynamicRemoteIconList.filter { it.internetChannel } as ArrayList<RemoteIconModel>
        channelShortCutList = remoteListData.dynamicRemoteIconList.filter { it.channelShortCut } as ArrayList<RemoteIconModel>
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remoteListData.dataType != "rNew" && (remote.remoteButtonName == "KEY_1" || remote.remoteButtonName == "KEY_2" || remote.remoteButtonName == "KEY_3" || remote.remoteButtonName == "KEY_4" || remote.remoteButtonName == "KEY_5" || remote.remoteButtonName == "KEY_6" || remote.remoteButtonName == "KEY_7" || remote.remoteButtonName == "KEY_8" || remote.remoteButtonName == "KEY_9" || remote.remoteButtonName == "KEY_0")) {
                isNumberKeyLearnCount += 1
                remote.isSelected = true
                remoteListData.keyNumbers.add(remote)
            }
            setButtonView(remote)
        }
        if (internetChannelList.isEmpty()) {
            internetChannelList.addAll(getDummyInternetChannel())
            remoteListData.dynamicRemoteIconList.addAll(internetChannelList)
        }
        setInternetChannelAdapter()
        if (remoteListData.keyNumbers.size == 0) {
            defaultKeyPadAdded = true
            remoteListData.keyNumbers.addAll(Common.getArrayNumber())
        }
        if (remoteListData.keyNumbers.size > 0 && remoteListData.keyNumbers.size != 10) {
            for (keyNumber in Common.getArrayNumber()) {
                var keyExist: Boolean = false
                for (key in remoteListData.keyNumbers) {
                    if (key.remoteButtonName == keyNumber.remoteButtonName) {
                        keyExist = true
                        break
                    }
                }
                if (!keyExist) {
                    remoteListData.keyNumbers.add(keyNumber)
                }
            }

        }

        learningdialog = Dialog(this)
        setAdapter()
    }

    private fun setInternetChannelAdapter() {
        channelShortcutAdapter = ChannelListAdapter(this, this)
        rvInternet.adapter = channelShortcutAdapter
        channelShortcutAdapter.viewType = Constant.INTERNET_CHANNEL
        (rvInternet.adapter as ChannelListAdapter).setData(internetChannelList)
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
            if (remoteListData.auraSenseName == device) {
                remoteListData.senseIp = ip!!.toString()
            }
        }
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
                dataSend = "{\"type\":4,\"dType\":\"${remoteButton.dType}\",\"rArr\":${remoteButton.rArr?.asList()},\"sz\":${remoteButton.sz},\"fz\":${remoteButton.fz},\"uiud\":\"${remoteListData.senseUiud}\"}"
            } else if (remoteButton.dType == "AC") {
                dataSend = "{\"type\":4,\"dType\":\"${remoteButton.dType}\",\"state\":${remoteButton.state.asList()},\"proto\":${remoteButton.proto},\"sz\":${remoteButton.sz},\"uiud\":\"${remoteListData.senseUiud}\"}"
            } else if (remoteButton.dType == "RMT") {
                dataSend = "{\"type\":4,\"dType\":\"${remoteButton.dType}\",\"value\":${remoteButton.value},\"proto\":${remoteButton.proto},\"sz\":${remoteButton.sz},\"uiud\":\"${remoteListData.senseUiud}\"}"
            }
            sendCommandToHandler(dataSend!!, remoteButton.remoteButtonName)
        }
    }

    private fun sendCommandToHandler(dataSend: String, remoteButtonName: String?) {
        sendEspHandler(dataSend, remoteListData.senseIp, remoteListData.auraSenseName!!, remoteButtonName!!)
    }

    fun forwardButton(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(35) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun backWard(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(33) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun pauseButton(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(34) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun playButton(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(37) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun backButton(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(29) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun openHome(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(21) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun openSettings(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(39) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun stopButton(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(38) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun powerClick(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(0) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun sourceClick(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(6) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun menuOpen(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(7) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun mutePressed(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(5) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun numberClicked(view: View) {
        openNumPad(view)
    }

    fun returnClicked(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if ((remote.remoteButtonName == Common.buttonList.get(29) || remote.remoteButtonName == Common.buttonList.get(40)) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun volumeUpClick(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(1) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun volumeDownClick(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(2) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun channelUp(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList[3] && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun channelDown(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(4) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun upArrowClick(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(22) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun leftArrowClick(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(24) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun rightArrowClick(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(23) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun bottomArrowClick(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(25) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    fun okClick(view: View) {
        startVibrator()
        var flag = false
        for (remote in remoteListData.dynamicRemoteIconList) {
            if (remote.remoteButtonName == Common.buttonList.get(26) && remote.isSelected) {
                flag = true
                remoteTriggerAction(remote)
                break
            }
        }
        if (!flag) {
            longToast(getString(R.string.button_not_learn))
        }
    }

    private fun addNumberKeyToList(remoteButton: RemoteIconModel) {
        for (checkKeyNum in remoteListData.dynamicRemoteIconList) {
            if (checkKeyNum.remoteButtonName == remoteButton.remoteButtonName) {
                remoteListData.dynamicRemoteIconList.remove(checkKeyNum)
                break
            }
        }
        remoteListData.dynamicRemoteIconList.add(remoteButton)

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
                            finish()
                        }

                    }).show()
        } else {
            super.onBackPressed()
            finish()
        }
    }

    private fun saveAllRemote() {
        progress_bar.visibility = View.VISIBLE
        if (defaultKeyPadAdded && keyNumberLearn) {
            remoteListData.dynamicRemoteIconList.addAll(remoteListData.keyNumbers)
        }

        if (localSqlRemote.insertRemote(mdbSense!!, remoteListData.auraSenseName!!, remoteListData.remoteName!!, remoteListData.brandName!!, remoteListData.modelNumber!!, gson.toJson(remoteListData.dynamicRemoteIconList), gson.toJson(remoteListData.favChannelList), remoteListData.typeAppliances
                        ?: "TV", remoteListData.remoteLocation ?: "", (Constant.HOME
                        ?: PreferenceManager.getDefaultSharedPreferences(this).getString("HOME", ""))!!)) {
            AppExecutors().diskIO().execute {
                val userId = Constant.IDENTITY_ID
                rulesTableHandler.updateAuraSenseRemote(remoteListData, userId!!, type = "create")
                val universalIrModel = UniversalIrModel()
                universalIrModel.brandName = remoteListData.brandName
                universalIrModel.modelNumber = remoteListData.modelNumber
                universalIrModel.typeAppliances = remoteListData.typeAppliances
                val typeToken = object : TypeToken<MutableList<LearnRemoteDataModel>>() {}.type
                val remoteListGson = Gson().toJson(remoteListData.dynamicRemoteIconList.filter { !it.internetChannel && !it.channelShortCut })
                universalIrModel.buttons = Gson().fromJson(remoteListGson, typeToken)
                val data = gson.toJson(universalIrModel)
                if (remoteListData.dataType == "rNew") {
                    RemoteStoreToCloud.storeRemoteToCloud(data, remoteListData.dataType.toString())
                }
                Thread.sleep(1000)
                AppExecutors().mainThread().execute {
                    progress_bar.visibility = View.GONE
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            progress_bar.visibility = View.GONE
            SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.exist_remote, remoteListData.remoteLocation)).show()
        }

    }

    override fun onBackPressed() {
        goBack()
    }

    private fun setListener() {
        header.home.setOnClickListener {
            goBack()
        }

        header.tvNext.setOnClickListener {
            saveAllRemote()
        }

        ivPower.setOnLongClickListener(this)
        rlPower.setOnLongClickListener(this)
        rlHome.setOnLongClickListener(this)
        ivHome.setOnLongClickListener(this)
        rlMenu.setOnLongClickListener(this)
        ivMenu.setOnLongClickListener(this)
        rlMute.setOnLongClickListener(this)
        ivMute.setOnLongClickListener(this)
        rlTv.setOnLongClickListener(this)
        ivTv.setOnLongClickListener(this)
        ivVolumeDown.setOnLongClickListener {
            val remote = RemoteIconModel()
            remote.remoteButtonName = Common.buttonList.get(2)
            remote.isSelected = true
            remote.name = remoteListData.auraSenseName
            remote.remoteModel = remoteListData.modelNumber
            makeLearnedButtonList(remote)
            true
        }
        ivChannelUp.setOnLongClickListener {
            val remote = RemoteIconModel()
            remote.remoteButtonName = Common.buttonList.get(3)
            remote.isSelected = true
            remote.name = remoteListData.auraSenseName
            remote.remoteModel = remoteListData.modelNumber
            makeLearnedButtonList(remote)
            true
        }
        ivChannnelDown.setOnLongClickListener {
            val remote = RemoteIconModel()
            remote.remoteButtonName = Common.buttonList.get(4)
            remote.isSelected = true
            remote.name = remoteListData.auraSenseName
            remote.remoteModel = remoteListData.modelNumber
            makeLearnedButtonList(remote)
            true
        }
        ivVolumeUp.setOnLongClickListener {
            val remote = RemoteIconModel()
            remote.remoteButtonName = Common.buttonList.get(1)
            remote.isSelected = true
            remote.name = remoteListData.auraSenseName
            remote.remoteModel = remoteListData.modelNumber
            makeLearnedButtonList(remote)
            true
        }
        rlOK.setOnLongClickListener(this)
        tvOk.setOnLongClickListener(this)
        rlRightArrow.setOnLongClickListener {
            val remote = RemoteIconModel()
            remote.remoteButtonName = Common.buttonList.get(23)
            remote.isSelected = true
            remote.name = remoteListData.auraSenseName
            remote.remoteModel = remoteListData.modelNumber
            makeLearnedButtonList(remote)
            true
        }
        ivUpArrow.setOnLongClickListener {
            val remote = RemoteIconModel()
            remote.remoteButtonName = Common.buttonList.get(22)
            remote.isSelected = true
            remote.name = remoteListData.auraSenseName
            remote.remoteModel = remoteListData.modelNumber
            makeLearnedButtonList(remote)
            true
        }
        rlBottomArrow.setOnLongClickListener {
            val remote = RemoteIconModel()
            remote.remoteButtonName = Common.buttonList.get(25)
            remote.isSelected = true
            remote.name = remoteListData.auraSenseName
            remote.remoteModel = remoteListData.modelNumber
            makeLearnedButtonList(remote)
            true
        }
        rlLeftArrow.setOnLongClickListener {
            val remote = RemoteIconModel()
            remote.remoteButtonName = Common.buttonList.get(24)
            remote.isSelected = true
            remote.name = remoteListData.auraSenseName
            remote.remoteModel = remoteListData.modelNumber
            makeLearnedButtonList(remote)
            true
        }
        ivLeftArrow.setOnLongClickListener {
            val remote = RemoteIconModel()
            remote.remoteButtonName = Common.buttonList.get(24)
            remote.isSelected = true
            remote.name = remoteListData.auraSenseName
            remote.remoteModel = remoteListData.modelNumber
            makeLearnedButtonList(remote)
            true
        }
        rlForward.setOnLongClickListener(this)
        ivForward.setOnLongClickListener(this)
        rlBackwrd.setOnLongClickListener(this)
        ivBackward.setOnLongClickListener(this)
        rlPlay.setOnLongClickListener(this)
        ivPlay.setOnLongClickListener(this)
        rlPause.setOnLongClickListener(this)
        ivPause.setOnLongClickListener(this)
        rlSetting.setOnLongClickListener(this)
        ivSetting.setOnLongClickListener(this)
        rlStop.setOnLongClickListener(this)
        ivStop.setOnLongClickListener(this)
        rlBack.setOnLongClickListener(this)
        ivBack.setOnLongClickListener(this)
        rlReturn.setOnLongClickListener(this)
        ivReturn.setOnLongClickListener(this)

    }

    fun addButtonCustom(view: View) {
        if (isNumberKeyLearnCount >= 10) {
            startActivityForResult(Intent(this, AddChannelActivity::class.java).putExtra(Constant.REMOTE_DATA, Gson().toJson(remoteListData)), Constant.DATA_REQUEST)
        } else {
            SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.please_learn_number)).show()
        }

    }

    override fun onEspHandlerMessage(decryptedData: String, name: String) {
        AppExecutors().mainThread().execute {
            updateData(decryptedData, name)
        }
    }

    var numpadDialog: NumberPadDialog? = null
    private fun openNumPad(view: View) {
        if (numpadDialog == null) {
            numpadDialog = NumberPadDialog(this)
        }
        numpadDialog?.numberButtonList = remoteListData.keyNumbers
        numpadDialog?.recyclerclicked = this
        numpadDialog?.show()
    }

    private fun makeLearnedButtonList(remoteButton: RemoteIconModel) {
        var remoteExist = false
        var existingRemote = RemoteIconModel()
        if (!isNumberLearn) {
            for (r in remoteListData.dynamicRemoteIconList) {
                remoteExist = false
                if (r.remoteButtonName == remoteButton.remoteButtonName) {
                    if (!r.isSelected) r.isSelected = true
                    existingRemote = r
                    remoteExist = true
                    break
                }
            }
            if (!remoteExist) {
                remoteListData.dynamicRemoteIconList.add(remoteButton)
            }
        }
        openDialogue(if (!remoteExist) remoteButton else existingRemote, remoteButton.remoteButtonName!!)
    }

    private fun openDialogue(remoteButton: RemoteIconModel, remoteBtnName: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialogue_remote_learn)
        dialog.tvor.visibility = if (!isNumberLearn) View.VISIBLE else View.GONE
        dialog.switchFav.visibility = if (!isNumberLearn) View.VISIBLE else View.GONE
        dialog.switchFav.isChecked = remoteButton.btnFavourite
        dialog.setCanceledOnTouchOutside(true)
        dialog.learn_remote.setOnClickListener {
            learningStarted = true
            if (remoteListData.dataType == "rOld") remoteListData.dataType = "rNew"
            startLearning(remoteButton, remoteBtnName, "edit")
            dialog.dismiss()
        }
        dialog.switchFav.setOnCheckedChangeListener { buttonView, isChecked ->
            for (remote in remoteListData.dynamicRemoteIconList) {
                if (remote.remoteButtonName == remoteButton.remoteButtonName) {
                    remote.btnFavourite = isChecked
                    break
                }
            }
            if (remoteListData.dataType == "rOld") remoteListData.dataType = "rNew"
            remoteButton.btnFavourite = isChecked
        }
        dialog.cancel_btn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    var chechBtnWorked = true

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
        dialog.learn_remote.text = if (chechBtnWorked) getString(R.string.test_open) else getString(R.string.confirmed)
        dialog.learn_remote.setOnClickListener {
            if (chechBtnWorked) {
                dialog.cancel_btn.visibility = View.VISIBLE
                appliancesAction(remoteIconModel, remoteIconModel.remoteButtonName!!)
                dialog.dismiss()
            } else {
                learbtncount += 1
                sendConfirmationDevice(remoteIconModel)
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

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNSDServiceResolved)
        nsd = null
    }


    private fun sendConfirmationDevice(remoteButton: RemoteIconModel) {
        val dataSend = jsonHelper.confirmRemoteData(remoteButton, remoteListData.senseUiud, remoteListData.modelNumber)
        sendCommandToHandler(dataSend, remoteButton.remoteButtonName)
    }

    private fun startLearning(remoteButton: RemoteIconModel, name: String, type: String) {
        showLearningDialog()
        sendDataToLearn(remoteButton, name)
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

    private fun controlCloudRemote(btn_name: String) {
        pusblishDataToShadow(remoteListData.senseThing
                ?: "", jsonHelper.serializeRemoteControl(remoteListData.senseUiud
                ?: "", remoteListData, btn_name))
    }


    private fun sendDataToLearn(remoteButton: RemoteIconModel, name: String) {
        if (remoteButton.name == remoteListData.auraSenseName) {
            val learingData = "{\"type\":3,\"uiud\":\"${remoteListData.senseUiud}\",\"model\":\"${remoteListData.modelNumber}\",\"btnName\":\"${remoteButton.remoteButtonName}\"}"
            sendCommandToHandler(learingData, remoteButton.remoteButtonName)
        }
    }


    var keyNumberLearn = false

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
                    }
                }
                3 -> {
                    if (remote_data.error == 1) {
                        learningdialog?.dismiss()
                        SingleBtnDialog.with(this).setHeading(getString(R.string.alert))
                                .setMessage(getString(R.string.please_press_button_remote)).show()
                    } else {
                        if (remoteListData.auraSenseName == remote_data.name) {
                            if (!isNumberLearn) {
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
                                        test_btn_list = btn_data
                                        setButtonView(btn_data)
                                        break
                                    }
                                }
                            } else {
                                for (btn_data in remoteListData.keyNumbers) {
                                    if (btn_data.remoteButtonName == btn_name) {
                                        btn_data.name = remote_data.name
                                        btn_data.dType = remote_data.dType
                                        btn_data.remoteModel = remoteListData.modelNumber
                                        btn_data.fz = remote_data.fz
                                        btn_data.sz = remote_data.sz
                                        btn_data.isSelected = true
                                        btn_data.proto = remote_data.proto
                                        btn_data.rArr = remote_data.rArr
                                        btn_data.state = remote_data.state
                                        btn_data.value = remote_data.value
                                        test_btn_list = btn_data
                                        break
                                    }
                                }
                            }
                            if (numpadDialog != null && isNumberLearn) {
                                keyNumberLearn = true
                                isNumberLearn = false
                                numpadDialog?.numberButtonList = remoteListData.keyNumbers
                                numpadDialog?.numpadAdapter?.notifyDataSetChanged()
                            }
                            if (!defaultKeyPadAdded) {
                                addNumberKeyToList(test_btn_list)
                            }
                            learningStarted = false
                            learningdialog?.dismiss()
                            chechBtnWorked = true
                            openTestDialog(test_btn_list, btn_name)

                        }
                    }
                }
                4 -> {
                    chechBtnWorked = false
                    openTestDialog(test_btn_list, btn_name)
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

    private fun makeChannelFav(remoteButton: RemoteIconModel) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialogue_remote_learn)
        dialog.tvor.visibility = View.VISIBLE
        dialog.switchFav.visibility = View.VISIBLE
        dialog.switchFav.isChecked = remoteButton.btnFavourite
        dialog.setCanceledOnTouchOutside(true)
        dialog.learn_remote.visibility = View.GONE
        dialog.switchFav.setOnCheckedChangeListener { buttonView, isChecked ->
            for (remote in remoteListData.dynamicRemoteIconList) {
                if (remoteButton.channelNumber == remote.channelNumber) {
                    remote.btnFavourite = isChecked
                    break
                }
            }
            remoteButton.btnFavourite = isChecked
        }
        dialog.cancel_btn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun startVibrator() {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
            } else {
                vibrator.vibrate(200)
            }

        }
    }

    override fun onLongClick(p0: View?): Boolean {
        when (p0?.id) {
            R.id.ivHome, R.id.rlHome -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(21)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlOK, R.id.tvOk -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(26)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlPower, R.id.ivPower -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(0)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlMenu, R.id.ivMenu -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(7)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlTv, R.id.ivTv -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(6)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlMute, R.id.ivMute -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(5)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlBackwrd, R.id.ivForward -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(33)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlForward, R.id.ivBackward -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(35)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlSetting, R.id.ivSetting -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(39)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlStop, R.id.ivStop -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(38)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlBack, R.id.ivBack -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(29)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlPause, R.id.ivPause -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(34)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlPlay, R.id.ivPlay -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(37)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }
            R.id.rlReturn, R.id.ivReturn -> {
                val remote = RemoteIconModel()
                remote.remoteButtonName = Common.buttonList.get(40)
                remote.isSelected = true
                remote.name = remoteListData.auraSenseName
                remote.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(remote)
            }

        }
        return true
    }


    private fun setChannelData(remoteButton: RemoteIconModel) {
        remoteListData.dynamicRemoteIconList.add(remoteButton)
        channelShortCutList.add(remoteButton)
        setAdapter()

    }

    fun setAdapter() {
        if (channelShortCutList.size > 0) {
            tvChannelShortcut.visibility = View.VISIBLE
            rvShortcut.visibility = View.VISIBLE
        }
        channelShortcutAdapter = ChannelListAdapter(this, this)
        channelShortcutAdapter.viewType = Constant.CHANNEL_SHORTCUT
        rvShortcut.adapter = channelShortcutAdapter
        (rvShortcut.adapter as ChannelListAdapter).channelShortcutList = channelShortCutList
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            val remoteIconModel = RemoteIconModel()
            remoteIconModel.remoteButtonName = data.getStringExtra(Constant.CHANNEL_NAME)
            remoteIconModel.channelNumber = data.getStringExtra(Constant.CHANNEL_NUMBER) ?: ""
            remoteIconModel.name = remoteListData.auraSenseName
            remoteIconModel.remoteModel = remoteListData.modelNumber
            remoteIconModel.brandName = remoteListData.brandName
            remoteIconModel.isSelected = true
            remoteIconModel.channelShortCut = true
            setChannelData(remoteIconModel)
        }
    }

    var isNumberLearn: Boolean = false
    var isNumberKeyLearnCount = 0
    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        when (viewType) {
            "KEY_PAD_CONTROL" -> {
                if ((data is RemoteIconModel)) {
                    if (data.isSelected) {
                        remoteTriggerAction(data as RemoteIconModel)
                    } else {
                        SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.button_not_learn)).show()
                    }
                }

            }
            "KEY_PAD_LEARN" -> {
                isNumberKeyLearnCount += 1
                isNumberLearn = true
                (data as RemoteIconModel).isSelected = true
                data.name = remoteListData.auraSenseName
                data.remoteModel = remoteListData.modelNumber
                makeLearnedButtonList(data as RemoteIconModel)
            }

            Constant.INTERNET_CHANNEL_LEARN -> {
                if (data is RemoteIconModel) {
                    data.isSelected = true
                    data.name = remoteListData.auraSenseName
                    data.remoteModel = remoteListData.modelNumber
                    makeLearnedButtonList(data as RemoteIconModel)
                }
            }

            Constant.INTERNET_CHANNEL -> {
                if (data is RemoteIconModel) {
                    if (data.isSelected) {
                        remoteTriggerAction(data as RemoteIconModel)
                    } else {
                        SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.button_not_learn)).show()
                    }
                }

            }
            Constant.CHANNEL_ADDED -> {
                makeChannelFav(data as RemoteIconModel)
            }
            Constant.CHANEL_CREATED -> {
                remoteTriggerAction(data as RemoteIconModel)
            }
        }
    }

    private fun getDummyInternetChannel(): MutableList<RemoteIconModel> {
        dummyInternetChannelListt.clear()
        for (num in listInternetchannel) {
            val numberData = RemoteIconModel()
            numberData.remoteButtonName = num
            numberData.internetChannel = true
            numberData.name = remoteListData.auraSenseName
            numberData.remoteModel = remoteListData.modelNumber
            dummyInternetChannelListt.add(numberData)
        }
        return dummyInternetChannelListt
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