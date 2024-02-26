package com.wozart.aura.ui.dashboard.more

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.Customization
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.dynamoDb.UserTableHandler
import com.wozart.aura.data.sqlLite.*
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.users.UserDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.login.GoogleLoginActivity
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.DialogListener
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.longToast
import kotlin.concurrent.thread

class ProfileActivity: AppCompatActivity() {
    private var localSQLDtabaseDevice = DeviceTable()
    private var localSQLiteDatabase = SceneTable()
    private var localSqlSchedule = ScheduleTable()
    private var localSqlUtils = UtilsTable()
    private var localSQLUser = UserTable()
    private var mdbDevice: SQLiteDatabase ?= null
    private var mdbScene: SQLiteDatabase ?= null
    private var mdbSchedule: SQLiteDatabase ?= null
    private var mdbUtils: SQLiteDatabase ?= null
    private var mdbUser : SQLiteDatabase ?= null
    var userList : ArrayList<User> = ArrayList()
    var user = User()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val dbHelper = DeviceDbHelper(this)
        mdbDevice = dbHelper.writableDatabase

        val scenDbHelper = SceneDbHelper(this)
        mdbScene = scenDbHelper.writableDatabase

        val scheduleDbHelper = ScheduleDbHelper(this)
        mdbSchedule = scheduleDbHelper.writableDatabase

        val dbUtils = UtilsDbHelper(this)
        mdbUtils = dbUtils.writableDatabase

        val dbUserHelper = UserDbHelper(this)
        mdbUser = dbUserHelper.writableDatabase

        val prefs = PreferenceManager.getDefaultSharedPreferences(this@ProfileActivity)
        val email = prefs.getString("EMAIL", "defaultStringIfNothingFound")
        val userId = prefs.getString("ID_", "NO USER")

        userList = localSQLUser.getUsers(mdbUser!!,userId!!)

        for(users in userList){
            input_name.setText(users.firstName)
            input_lastname.setText(users.lastName)
            input_email.setText(email)
            input_mobile.setText(users.mobile)
            if(users.profileImage!!.isNotEmpty()){
                Glide.with(this).load(users.profileImage).into(logo_iv)
            }
            user.firstName = users.firstName
            user.lastName = users.lastName
            user.email = email
            user.mobile = users.mobile
            user.profileImage = users.profileImage
        }


        edit_profile.setOnClickListener {
            val intent = Intent(this , EditProfile::class.java)
            intent.putExtra("USER_NAME",user.firstName)
            intent.putExtra("USER_LAST_NAME",user.lastName)
            intent.putExtra("USER_CONTACT",user.mobile)
            intent.putExtra("PROFILE_PICTURE",user.profileImage)
            startActivity(intent)
        }


        delete_profile.setOnClickListener {
            Utils.showCustomDialog(this, getString(R.string.title_logout), getString(R.string.text_logout_message), R.layout.dialog_layout, object : DialogListener {
                override fun onOkClicked() {
                    getSharedPreferences("USERNAME", 0).edit().clear().apply()
                    getSharedPreferences("EMAIL", 0).edit().clear().apply()
                    getSharedPreferences("ID_", 0).edit().clear().apply()
                    getSharedPreferences("PROFILE_PICTURE", 0).edit().clear().apply()
                    getSharedPreferences("HOME", 0).edit().clear().apply()
                    getSharedPreferences("HOME_ROOM_DATA", 0).edit().remove("HOME_ROOM_DATA").apply()
                    getSharedPreferences("VERIFIED_EMAIL",0).edit().clear().apply()
                    getSharedPreferences("TOKEN",0).edit().clear().apply()

                    val prefEditor = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                    prefEditor.clear()
                    prefEditor.apply()

                    localSQLDtabaseDevice.deleteTable(mdbDevice!!)
                    localSQLiteDatabase.deleteTable(mdbScene!!)
                    localSqlUtils.deleteUtilstTable(mdbUtils!!)
                    localSqlSchedule.deleteTable(mdbSchedule!!)

                    runOnUiThread {
                        val intent = Intent(applicationContext, GoogleLoginActivity::class.java)
                        intent.putExtra("type","Login")
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }


                }

                override fun onCancelClicked() {

                }
            })
        }

        back.setOnClickListener {
            val mainIntent = Intent(applicationContext, DashboardActivity::class.java)
            mainIntent.putExtra("TAB_SET",Constant.MORE_TAB)
            startActivity(mainIntent)
            this.finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val mainIntent = Intent(applicationContext, DashboardActivity::class.java)
        mainIntent.putExtra("TAB_SET",Constant.MORE_TAB)
        startActivity(mainIntent)
        this.finish()
    }
}
