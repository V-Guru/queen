package com.wozart.aura.aura.ui.dashboard.rooms

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.Wallpaper
import com.wozart.aura.aura.ui.adapter.RoomWallpaperAdapter
import com.wozart.aura.aura.ui.adapter.WallpaperAdapter
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.model.Room
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.adapter.RoomAdapter
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.more.CustomizationActivity
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.DialogListener
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_room_wallpaper.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.File


class AddRoomActivity : AppCompatActivity(), WallpaperAdapter.WallpaperListener, RoomAdapter.RoomListener {

    private val localSqlDatabase = DeviceTable()
    private val localSqlDatabaseScene = SceneTable()
    private var mDb: SQLiteDatabase? = null
    private var mdbScene: SQLiteDatabase? = null
    var wallpaperCheckFlag = false

    override fun onAddRoomClicked() {

    }

    override fun onRoomClicked() {

    }

    var room_icons = arrayOf(R.drawable.ic_living_room_off)
    var wallpaperList: MutableList<Wallpaper> = ArrayList()
    var wallPaperAdapter: WallpaperAdapter? = null
    private var roomWallpaper: RoomWallpaperAdapter? = null
    var roomList: MutableList<Room> = ArrayList()
    private var wallpaperIndex: Int = 0
    private var roomIconIndex: Int = 0
    private var editRoomType: String? = null
    private var editRoomName: String? = null
    private var rulesTableDo = RulesTableHandler()
    private val localSqlUtils = UtilsTable()
    var mDbUtils: SQLiteDatabase? = null
    var homeDetails: MutableList<RoomModelJson> = java.util.ArrayList()
    var editableRoomlist: MutableList<RoomModelJson> = ArrayList()
    var setBackGround = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_wallpaper)

        editRoomType = intent.getStringExtra("ROOM_EDIT_TYPE")
        editRoomName = intent.getStringExtra("NAME_ROOM")

        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase

        val dbhelper = SceneDbHelper(this)
        mdbScene = dbhelper.writableDatabase

        homeDetails = localSqlUtils.getHomeData(mDbUtils!!, "home")
        for (dummy in homeDetails) {
            if (dummy.type == "room") {
                if (dummy.name == editRoomName) {
                    wallpaperIndex = dummy.bgUrl.toInt()
                    selected_wallpaper.setImageResource(Utils.getRoomWallpaperDrawables(wallpaperIndex))
                    Utils.setRoomDrawable(this, activity_main_room, wallpaperIndex)
                    roomIconIndex = dummy.roomIcon
                    break
                }
            }

        }

        wallPaperAdapter = WallpaperAdapter(this, this, wallpaperList)

        btn_delete_room.setOnClickListener {
            if (editRoomName == null) {
                Utils.showCustomDialogOk(this, this.getString(R.string.delete_room), this.getString(R.string.dialog_delete_room_name), R.layout.dialog_layout, object : DialogListener {
                    override fun onOkClicked() {}
                    override fun onCancelClicked() {}
                })
            } else {
                var flag = true
                for (dummy in homeDetails) {
                    if (dummy.type == "room") {
                        if ((dummy.name == editRoomName) and (dummy.sharedHome == "default")) {
                            flag = false
                        }
                    }
                }
                if (flag) {
                    Utils.showCustomDialogOk(this, "Delete Room", this.getString(R.string.dialog_delete_room, editRoomName), R.layout.dialog_layout, object : DialogListener {
                        override fun onOkClicked() {
                            deleteRoom()
                        }

                        override fun onCancelClicked() {}
                    })
                } else {
                    longToast("Default room cannot be deleted.")
                }
            }
        }

        init()
        initializeRoom()
    }

    fun deleteRoom() {
        val listOfDevices: ArrayList<AuraSwitch> = localSqlDatabase.getDevicesForRoom(mDb!!, Constant.HOME!!, editRoomName!!)
        if (listOfDevices.size > 0) {
            SingleBtnDialog.with(this).setHeading(getString(R.string.alert))
                    .setMessage(getString(R.string.device_deleting_error_room)).show()
        } else {
            secondBar.visibility = View.VISIBLE
            var removeIndexCount = 0
            var removeIndex = 1
            for (dummy in homeDetails) {
                if (dummy.type == "room") {
                    if ((dummy.name == editRoomName) and (dummy.sharedHome == Constant.HOME)) {
                        removeIndex = removeIndexCount
                    }
                    removeIndexCount++
                } else {
                    removeIndexCount++
                }
            }
            homeDetails.removeAt(removeIndex)
            localSqlUtils.replaceHome(mDbUtils!!, "home", homeDetails)
            AppExecutors().diskIO().execute {
                val userId = Constant.IDENTITY_ID
                rulesTableDo.homeSQLtoDYNAMO(homeDetails, userId!!)
            }
            runOnUiThread {
                secondBar.visibility = View.GONE
                val intent = Intent(applicationContext, DashboardActivity::class.java)
                intent.putExtra("TAB_SET", Constant.ROOMS_TAB)
                this.startActivity(intent)
                this.finish()
            }
        }

    }

    private fun initializeWallPaperAdapter() {
        val drawables: IntArray = intArrayOf(R.drawable.eleven_new_room_bg, R.drawable.twelve_new_room_bg, R.drawable.one_home_bg, R.drawable.two_home_bg, R.drawable.five_home_bg, R.drawable.three_home_bg, R.drawable.four_home_bg, R.drawable.six_home_bg, R.drawable.seven_home_bg, R.drawable.eight_home_bg, R.drawable.nine_home_bg, R.drawable.ten_new_room_bg, R.drawable.thrteen_new_room)
        for (i in 0 until drawables.size) {
            val wallpaper = Wallpaper()
            wallpaper.id = drawables[i]
            wallpaper.isSelected = false
            if (editRoomType == "create") {
                if (i == Utils.getWallpaperId(this))
                    wallpaper.isSelected = true

            } else {
                if (i == wallpaperIndex)
                    wallpaper.isSelected = true
            }

            wallpaperList.add(wallpaper)
        }

        wallPaperAdapter = WallpaperAdapter(this, this, wallpaperList)
        wallpaper_room_grid.isNestedScrollingEnabled = false
        wallpaper_room_grid.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
        wallpaper_room_grid.adapter = wallPaperAdapter
    }

    private fun initializeRoom() {
        for (i in room_icons.indices) {
            val room = Room()
            room.url = room_icons[i]
            room.isSelected = false
            room.isSelected = i == roomIconIndex
            roomList.add(room)
        }
        roomWallpaper = RoomWallpaperAdapter(this, roomList) { pos: Int, Boolean ->

        }

    }

    private fun init() {
        var listRoom: MutableList<RoomModelJson> = ArrayList()
        if (editRoomType == "edit") {
            tvTitle.text = "Edit Room"
            input_room_name.setText(editRoomName)
            btn_delete_room.visibility = View.VISIBLE

        } else {
            btn_delete_room.visibility = View.GONE
            tvTitle.text = "New Room"
            listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
            for (x in listRoom) {
                if (x.name == Constant.HOME) {
                    Utils.setDrawable(this, activity_main_room, x.bgUrl.toInt())
                }
            }
        }
        home.setOnClickListener {
            this.finish()
        }

        change_wallpaper.setOnClickListener {
            if (!setBackGround) {
                setBackGround = true
                change_wallpaper.setImageResource(R.drawable.ic_hide_home_wallpaper)
                wallpaperList.clear()
                wallpaper_room_grid.visibility = View.VISIBLE
                initializeWallPaperAdapter()
            } else {
                setBackGround = false
                change_wallpaper.setImageResource(R.drawable.ic_add_wallpaper_home)
                wallpaper_room_grid.visibility = View.GONE
            }
        }

        tvNext.setOnClickListener {
            secondBar.visibility = View.VISIBLE
            if (editRoomType == "edit") {
                editableRoomlist.clear()
                if (editRoomName != input_room_name.text.toString()) {
                    var flagRoomCheck = false
                    val homeData = homeDetails.find { it.type == "room" && it.name == input_room_name.text.toString() }
                    if (homeData != null) {
                        flagRoomCheck = true
                    }
                    if (!flagRoomCheck) {
                        val new_room = RoomModelJson(input_room_name.text.toString(), "room", Constant.HOME!!, "0", 0, 0.0, 0.0, "null")
                        homeDetails.add(new_room)
                    }
                } else {
                    for (dummy in homeDetails.filter { it.type == "room" }) {
                        if (dummy.name == input_room_name.text.toString() && dummy.sharedHome == Constant.HOME) {
                            if (wallpaperCheckFlag) {
                                dummy.bgUrl = wallPaperAdapter!!.getWallPaperId().toString()
                            } else {
                                dummy.bgUrl = dummy.bgUrl
                            }
                            dummy.roomIcon = 0
                        } else if (dummy.name == input_room_name.text.toString() && dummy.sharedHome == "default") {
                            if (wallpaperCheckFlag) {
                                dummy.bgUrl = wallPaperAdapter!!.getWallPaperId().toString()
                            } else {
                                dummy.bgUrl = dummy.bgUrl
                            }
                            dummy.roomIcon = 0
                        } else if (dummy.name == input_room_name.text.toString()) {
                            if (wallpaperCheckFlag) {
                                dummy.bgUrl = wallPaperAdapter!!.getWallPaperId().toString()
                            } else {
                                dummy.bgUrl = dummy.bgUrl
                            }
                            dummy.roomIcon = 0
                        }
                        editableRoomlist.add(dummy)
                    }
                }

                if (editableRoomlist.size == 0) localSqlUtils.replaceHome(mDbUtils!!, "home", homeDetails) else localSqlUtils.replaceHome(mDbUtils!!, "home", editableRoomlist)
                localSqlDatabase.updateRoom(mDb!!, editRoomName!!, input_room_name.text.toString())
                localSqlDatabaseScene.updateRoom(mdbScene!!, editRoomName!!, input_room_name.text.toString())

                AppExecutors().diskIO().execute {
                    var userId = Constant.IDENTITY_ID
                    if (userId == null) {
                        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                        userId = prefs.getString("ID_", "NO USER")
                    }
                    if (editableRoomlist.size == 0) rulesTableDo.homeSQLtoDYNAMO(homeDetails, userId!!) else rulesTableDo.homeSQLtoDYNAMO(editableRoomlist, userId!!)
                    AppExecutors().mainThread().execute {
                        if (intent.getStringExtra(Constant.ROOM_CREATION) == Constant.NEW_ROOM) {
                            val intent = Intent(applicationContext, CustomizationActivity::class.java)
                            startActivity(intent)
                            this.finish()
                        } else {
                            val intent = Intent(applicationContext, DashboardActivity::class.java)
                            intent.putExtra("TAB_SET", Constant.ROOMS_TAB)
                            this.startActivity(intent)
                            this.finish()
                        }
                    }
                }

                wallPaperAdapter.let {
                    Utils.saveWallPaperId(applicationContext, wallPaperAdapter!!.getWallPaperId(), input_room_name.text.toString())
                }

            } else {
                val text = input_room_name.text.toString()
                if (text.isEmpty()) {
                    toast("Empty name is not allowed")
                } else {
                    wallPaperAdapter.let {
                        Utils.saveWallPaperId(applicationContext, wallPaperAdapter!!.getWallPaperId(), text)
                    }
                    var flagExists: Int = 0
                    for (dummy in homeDetails) {
                        if (dummy.type == "room") {
                            flagExists = 0
                            if ((dummy.name == text && dummy.sharedHome == Constant.HOME) || (dummy.name == text && dummy.sharedHome == "default")) {
                                flagExists = 1
                                break
                            }
                        }
                    }
                    if (flagExists == 0) {
                        val home = Constant.HOME
                        val new_room = RoomModelJson(text, "room", home!!, wallPaperAdapter!!.getWallPaperId().toString(), 0, 0.0, 0.0, "null")
                        homeDetails.add(new_room)
                        localSqlUtils.replaceHome(mDbUtils!!, "home", homeDetails)
                        AppExecutors().diskIO().execute {
                            val userId = Constant.IDENTITY_ID
                            rulesTableDo.homeSQLtoDYNAMO(homeDetails, userId!!)
                        }
                        val intent = Intent(applicationContext, DashboardActivity::class.java)
                        intent.putExtra("TAB_SET", Constant.ROOMS_TAB)
                        this.startActivity(intent)
                        this.finish()
                    } else {
                        Toast.makeText(this, "Room already Exist with same name", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }


    override fun onWallpaperSelect(wallpaerId: Int) {
        wallpaperCheckFlag = true
        Utils.setRoomDrawable(this, activity_main_room, wallpaerId)
        selected_wallpaper.setImageResource(Utils.getRoomWallpaperDrawables(wallpaerId))

    }


    private fun addWallPaper(file: File?) {
        Uri.fromFile(file)
        val wallpaper = Wallpaper()
        wallpaper.url = Uri.fromFile(file).toString()
        wallpaper.isSelected = false
        wallpaperList.add(wallpaper)
        wallPaperAdapter?.update(wallpaperList)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}
