package com.wozart.aura.ui.dashboard.room

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.IBinder
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.service.AwsPubSub
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.createautomation.OnFragmentInteractionListener
import com.wozart.aura.ui.dashboard.rooms.RoomsFragment
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.JsonHelper
import kotlinx.android.synthetic.main.activity_create_automation.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 09/08/18
 * Description :
 *****************************************************************************/
class RoomActivity : BaseAbstractActivity(), OnFragmentInteractionListener {


    private var awsPubSub: AwsPubSub? = null
    internal var mBounded: Boolean = false
    private var mDb: SQLiteDatabase? = null
    private var serviceConnection = false
    private var mDbUtils: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    var listRoom: MutableList<RoomModelJson> = ArrayList()
    var IpListDevices: MutableList<IpModel> = ArrayList()


    override fun onRoomBtnClicked() {
        this.finish()
    }


    private var roomName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_automation)
        initializeAwsServices()
        Thread.sleep(500)
        init()
    }

    private fun initializeAwsServices() {
        if (!mBounded) {
            val mIntent = Intent(this, AwsPubSub::class.java)
            bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun init() {
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase

        roomName = intent.getStringExtra(Constant.ROOM_NAME)

        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        for (x in listRoom) {
            if ((x.type == "room") and (x.sharedHome == Constant.HOME)) {
                if (x.name == roomName) {
                    Utils.setRoomDrawable(this, containerAutomation, x.bgUrl.toInt())
                }
            } else if ((x.type == "room") and (x.sharedHome == "default")) {
                if (x.name == roomName) {
                    Utils.setRoomDrawable(this, containerAutomation, x.bgUrl.toInt())
                }
            }
        }
        navigateToFragment(RoomsFragment(), getString(R.string.empty_tag), true, true)
    }

    override fun onHomeBtnClicked() {
        onBackPressed()
    }

    fun getRoomName(): String? {
        return roomName
    }


    var mConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
            mBounded = false
            awsPubSub = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mBounded = true
            val mLocalBinder = service as AwsPubSub.LocalAwsBinder
            awsPubSub = mLocalBinder.getServerInstance()
           // awsPubSub!!.publishToAws(defaultThings ?: "", JsonHelper().serializeLEDData())
            serviceConnection = true
        }
    }

    var defaultThings: String? = null

    fun pusblishDataToShadow(thing: String, data: String) {
        if (!AwsPubSub.isAwsConnected) {
            //showToast(getString(R.string.please_wait_connecting_aws))
            return
        }else{
            //showToast(getString(R.string.please_success_connecting_aws))

        }
        defaultThings = thing
        if (awsPubSub == null) {
            awsPubSub = AwsPubSub().LocalAwsBinder().getServerInstance()
        }
        awsPubSub?.apply {
            publishToAws(thing, data)
            getShadowData(thing)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!mBounded) {
            val mIntent = Intent(this, AwsPubSub::class.java)
            bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

}