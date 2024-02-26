package com.wozart.aura.ui.sceneController

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.activity_aura_scene_controll.*


/**
 * Created by Saif on 04/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
*/

class SceneControllerSetUpActivity : AppCompatActivity() {

    private var mDbUtils: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    private var roomList: MutableList<String> = ArrayList()
    private var listRoom: MutableList<RoomModelJson> = ArrayList()
    var roomSelected: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aura_scene_controll)
        initialize()
        listner()
    }

    fun initialize(){
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase
        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
        for (dummy in listRoom) {
            if (dummy.type == "room") {
                if ((dummy.sharedHome == Constant.HOME) || (dummy.sharedHome == "default")) {
                    roomList.add(dummy.name)
                }
            }
        }

    }

    private fun listner(){
        btn_submit.setOnClickListener {
            val intent = Intent(this, SenseSelectionActivity::class.java)
            intent.putExtra(Constant.BUTTON_ROOM,roomSelected)
            intent.putExtra(Constant.BUTTON_NAME,input_name.text.toString())
            startActivity(intent)
            this.finish()
        }

        val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_room_selection, roomList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roomlistSpinner!!.adapter = spinnerAdapter

        roomlistSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {


            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                roomSelected = roomlistSpinner.selectedItem.toString()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}