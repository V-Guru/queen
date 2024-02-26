package com.wozart.aura.ui.home

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.Wallpaper
import com.wozart.aura.aura.ui.adapter.WallpaperAdapter
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.data.sqlLite.ScheduleTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.createautomation.CreateAutomationActivity
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.home.HomeLocationActivity
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.DialogListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_home_details.*
import kotlinx.android.synthetic.main.layout_header.*
import org.jetbrains.anko.toast
import kotlin.concurrent.thread


class HomeDetailsActivity : AppCompatActivity(), WallpaperAdapter.WallpaperListener {


    private val localSqlDatabase = DeviceTable()
    private val localSqlSceneDatabase = SceneTable()
    private val localSqlScheduleDatabase = ScheduleTable()
    private var mDb: SQLiteDatabase? = null
    private var mDbScene: SQLiteDatabase? = null
    private var mDbSchedule: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    var mDbUtils: SQLiteDatabase? = null
    private var rulesTableDo = RulesTableHandler()
    private var aura_sense_device: MutableList<RemoteModel> = ArrayList()
    var home_latitude: Double? = null
    var home_longitude: Double? = null
    private var HomeEditType: String? = null
    private lateinit var HomeEditName: String
    var homeDetails: MutableList<RoomModelJson> = java.util.ArrayList()
    var location_fetch: String? = null
    var homeLatitude: Double = 0.0
    var homeLongitude: Double = 0.0
    var locationFlag = false
    var wallpaperFlagCheck = false
    var homeLocationSet: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_details)

        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase

        val dbHelperScene = SceneDbHelper(this)
        mDbScene = dbHelperScene.writableDatabase

        val dbHelperSchedule = ScheduleDbHelper(this)
        mDbSchedule = dbHelperSchedule.writableDatabase

        btn_delete.setOnClickListener {
            if (HomeEditName == "My Home") {
                Utils.showCustomDialogOk(this, "Delete Home", this.getString(R.string.dialog_delete_default_home), R.layout.dialog_layout, object : DialogListener {
                    override fun onOkClicked() {

                    }

                    override fun onCancelClicked() {

                    }
                })
            } else {
                Utils.showCustomDialog(this, "Delete Home", this.getString(R.string.dialog_delete_home, HomeEditName), R.layout.dialog_layout, object : DialogListener {
                    override fun onOkClicked() {
                        deleteHome()

                    }

                    override fun onCancelClicked() {

                    }
                })
            }
        }

        init()
    }

    fun setLocation(view: View) {
        if (HomeEditType == "create") {
            if (input_name.text!!.isNotEmpty()) {
                location_address.isEnabled = true
                val intent = Intent(this, HomeLocationActivity::class.java)
                intent.putExtra("HOME_NAME", input_name.text.toString())
                intent.putExtra(Constant.CREATE_HOME_ROOM, "create")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter home name first", Toast.LENGTH_SHORT).show()
            }
        } else {
            location_address.isEnabled = true
            val intent = Intent(this, HomeLocationActivity::class.java)
            intent.putExtra("HOME_NAME", HomeEditName)
            intent.putExtra(Constant.CURRENT_LATITUDE, homeLatitude)
            intent.putExtra(Constant.CURRENT_LONGITUDE, homeLongitude)
            intent.putExtra(Constant.CREATE_HOME_ROOM, "edit")
            startActivity(intent)
        }
    }

    fun deleteHome() {
        var usertype = true
        if (HomeEditType == "edit") {
            var devices = localSqlDatabase.getDevicesForHome(mDb!!, HomeEditName)

            val homeDetailsNew: MutableList<RoomModelJson> = java.util.ArrayList()
            val guestHomes: MutableList<String> = ArrayList()
            for (homes in homeDetails) {
                if ((homes.type == "home") and (homes.sharedHome == "guest")) {
                    guestHomes.add(homes.name)
                }
            }
            for (item in homeDetails) {
                if (item.type == "home") {
                    if (item.name != HomeEditName) {
                        if (item.sharedHome == "guest") {
                            usertype = false
                        }
                        homeDetailsNew.add(item)
                    }
                } else if (item.sharedHome != HomeEditName) {
                    var flag = true
                    for (h in guestHomes) {
                        if (h == item.sharedHome) {
                            flag = false
                            break
                        }
                    }
                    if (flag) {
                        homeDetailsNew.add(item)
                    }
                }
            }

            val senseDelete: MutableList<RemoteModel> = ArrayList()
            for (senseDevice in localSqlUtils.getRemoteData(mDbUtils!!, "remote_device")) {
                if (senseDevice.home == HomeEditName) {
                    senseDelete.add(senseDevice)
                } else {
                    aura_sense_device.add(senseDevice)
                }
            }

            localSqlUtils.replaceRemoteData(mDbUtils!!, "remote_device", aura_sense_device)
            localSqlUtils.replaceHome(mDbUtils!!, "home", homeDetailsNew)
            localSqlDatabase.deleteHome(mDb!!, HomeEditName)
            localSqlSceneDatabase.deleteHomeScenes(mDbScene!!, HomeEditName)
            localSqlScheduleDatabase.deleteHomeSchedules(mDbSchedule!!, HomeEditName)

            thread {
                val userId = Constant.IDENTITY_ID
                rulesTableDo.deleteHomeSenseDevice(senseDelete)
                if (usertype) {
                    rulesTableDo.deleteGuestAccessFromMaster(HomeEditName, userId!!)
                } else {
                    rulesTableDo.deleteHomeDynamo(homeDetailsNew, userId!!, HomeEditName)
                }

                runOnUiThread {
                    progressBar.visibility = View.INVISIBLE
                    val prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    prefEditor.putString("HOME", "My Home")
                    prefEditor.apply()
                    val intent = Intent(applicationContext, DashboardActivity::class.java)
                    this.startActivity(intent)
                    this.finish()
                }
            }

        } else {
            Toast.makeText(this, "Home is not yet created to delete", Toast.LENGTH_SHORT).show()
        }

    }


    fun init() {
        tvTitle.setTextColor(ContextCompat.getColor(this.baseContext, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(this.baseContext, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP)
        tvNext.setTextColor(ContextCompat.getColor(this.baseContext, R.color.black_d_n))
        HomeEditType = intent.getStringExtra(Constant.CREATE_HOME_ROOM)
        HomeEditName = (intent.getStringExtra("HOME_NAME")?:Constant.HOME).toString()
        location_fetch = intent.getStringExtra("ADDRESS_LOCATION")
        home_latitude = intent.getDoubleExtra(Constant.CURRENT_LATITUDE, 0.0)
        home_longitude = intent.getDoubleExtra(Constant.CURRENT_LONGITUDE, 0.0)
        locationFlag = intent.getBooleanExtra("LOCATION_FLAG", false)
        homeLocationSet = intent.getStringExtra(Constant.SET_LOCATION)
        homeLatitude = home_latitude as Double
        homeLongitude = home_longitude as Double
        homeDetails = localSqlUtils.getHomeData(mDbUtils!!, "home")

        if (HomeEditType == "create") {
            tvTitle.text = getString(R.string.title_home_details)
            if (HomeEditName != "Home Name") {
                input_name.setText(HomeEditName)
                location_address.text = location_fetch
                homeLatitude = home_latitude as Double
                homeLongitude = home_longitude as Double
            }
            btn_delete.visibility = View.INVISIBLE
            input_name.visibility = View.VISIBLE
        } else {
            tvTitle.text = getString(R.string.edit_home)
            text_home_static.visibility = View.VISIBLE
            text_home_static.text = HomeEditName
            input_name.visibility = View.GONE
            if (HomeEditName == "My Home") {
                btn_delete.visibility = View.INVISIBLE
            } else {
                btn_delete.visibility = View.VISIBLE
            }
            homeDetails.find { it.type == "home" && it.name == HomeEditName }?.apply {
                if (locationFlag) {
                    location_address.text = location_fetch
                    homeLocation = location_fetch.toString()
                    homeLatitude = this@HomeDetailsActivity.homeLatitude
                    homeLongitude = this@HomeDetailsActivity.homeLongitude
                } else {
                    location_address.text = if (homeLocation.isEmpty() || homeLocation == "null") location_fetch else homeLocation
                    this@HomeDetailsActivity.homeLatitude = homeLatitude
                    this@HomeDetailsActivity.homeLongitude = homeLongitude
                }
            }
           /* for (x in homeDetails) {
                if (x.type == "home") {
                    if (x.name == HomeEditName) {
                        //Utils.setDrawable(this, activity_main, x.bgUrl.toInt())
                        if (x.homeLocation.isEmpty() || x.homeLocation == "null") {
                            location_address.text = location_fetch
                        } else {
                            if (locationFlag) {
                                location_address.text = location_fetch
                                x.homeLocation = location_fetch.toString()
                                x.homeLatitude = homeLatitude
                                x.homeLongitude = homeLongitude
                            } else {
                                location_address.text = x.homeLocation
                                homeLatitude = x.homeLatitude
                                homeLongitude = x.homeLongitude
                            }
                        }
                    }
                }
            }*/
        }

        text_pick.visibility = View.VISIBLE
        wallpaper_grid.visibility = View.INVISIBLE

        tvNext.text = getString(R.string.text_save)
        tvNext.setTextColor(Color.WHITE)
        tvNext.setOnClickListener {
            var isEditDone = false
            if (HomeEditType == "create") {
                input_name.visibility = View.VISIBLE
                if ((input_name.text.toString().isEmpty()) || (location_address.text.isEmpty()) || (location_address.text == "")) {
                    toast("Please enter the home name and location.")
                } else {
                    var isHomeExists = false
                    for (item in homeDetails) {
                        if ((item.type == "home") and (item.name == input_name.text.toString())) {
                            isHomeExists = true
                        }
                    }
                    if (!isHomeExists) {
                        isEditDone = true
                        homeDetails.add(RoomModelJson(input_name.text.toString(), "home", "master", "0", 0, homeLatitude, homeLongitude, location_address.text.toString()))
                        localSqlUtils.replaceHome(mDbUtils!!, "home", homeDetails)
                        AppExecutors().diskIO().execute {
                            val userId = Constant.IDENTITY_ID
                            rulesTableDo.homeSQLtoDYNAMO(homeDetails, userId!!)
                        }
                    } else {
                        toast("Home already exists!!")
                    }
                }

            } else {
                input_name.visibility = View.GONE
                for (item in homeDetails) {
                    if (item.type == "home" && item.name == text_home_static.text) {
                        item.bgUrl = "0"
                        item.homeLocation = location_address.text.toString()
                        item.homeLatitude = homeLatitude
                        item.homeLongitude = homeLongitude
                        break
                    }
                }
                localSqlUtils.replaceHome(mDbUtils!!, "home", homeDetails)
                thread {
                    val userId = Constant.IDENTITY_ID
                    rulesTableDo.homeSQLtoDYNAMO(homeDetails, userId!!)
                }
                isEditDone = true
            }
            if (isEditDone) {
                if (homeLocationSet != null) {
                    val intent = Intent(this, CreateAutomationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
        home.setOnClickListener {
            onBackPressed()
        }

    }

    /*   private fun initializeWallPaperAdapter() {
           val drawables: IntArray = intArrayOf(R.drawable.one_home_bg, R.drawable.two_home_bg, R.drawable.five_home_bg, R.drawable.three_home_bg, R.drawable.four_home_bg, R.drawable.six_home_bg, R.drawable.seven_home_bg, R.drawable.eight_home_bg, R.drawable.nine_home_bg)
           for (i in 0 until drawables.size) {
               val wallpaper = Wallpaper()
               wallpaper.id = drawables[i]
               wallpaper.isSelected = false
               if (HomeEditType == "create") {
                   if (i == Utils.getWallpaperId(this))
                       wallpaper.isSelected = true
               } else {
                   if (i == Utils.getWallpaperId(this, HomeEditName))
                       wallpaper.isSelected = true
               }
               wallpaperList.add(wallpaper)
           }

           wallPaperAdapter = WallpaperAdapter(this, this, wallpaperList)
           wallpaper_grid.isNestedScrollingEnabled = false
           wallpaper_grid.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
           wallpaper_grid.adapter = wallPaperAdapter
       }*/

    override fun onWallpaperSelect(wallpaerId: Int) {
        wallpaperFlagCheck = true
        Utils.setDrawable(this, activity_main, wallpaerId)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
