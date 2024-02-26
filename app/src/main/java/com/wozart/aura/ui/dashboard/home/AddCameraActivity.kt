package com.wozart.aura.ui.dashboard.home

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.wozart.aura.R
import com.wozart.aura.data.sqlLite.CameraTable
import com.wozart.aura.entity.sql.camera.CameraDbHelper
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.dialog.SingleBtnDialog


/**
 * Created by Saif on 21/12/20.
 * mds71964@gmail.com
 */
class AddCameraActivity : AppCompatActivity() {

    var currentDeviceConnected = false
    var localSQLite = CameraTable ()
    var mdbCamera : SQLiteDatabase ?=  null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_camera_activity)
        init()
    }

    companion object {
        var rtspURI: String? = null
    }

    fun init() {
        val dbHelper = CameraDbHelper(this)
        mdbCamera = dbHelper.writableDatabase
    }

    fun backPressed(view: View){
        onBackPressed()
    }

    fun buttonClicked(view: View) {
        val ipAddress = (findViewById<TextInputEditText>(R.id.ipAddress)).text.toString()
        val login = (findViewById<TextInputEditText>(R.id.login)).text.toString()
        val password = (findViewById<TextInputEditText>(R.id.password)).text.toString()
        val cameraName = (findViewById<TextInputEditText>(R.id.etCameraName)).text.toString()


        if (ipAddress.isNotEmpty() &&
                login.isNotEmpty() &&
                password.isNotEmpty() && !cameraName.isEmpty()) {
            currentDeviceConnected = true
            val streamURI = "$login:$password@$ipAddress"
            rtspURI = appendCredentials(streamURI)
            localSQLite.insert(mdbCamera!!,Constant.HOME!!,"Living Room",cameraName, rtspURI!!,"create",favourite = true)
            startActivity(Intent(this,DashboardActivity::class.java))

        } else {
            SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.please_fill_all_details)).show()
        }

//        if (currentDeviceConnected) {
//            rtspURI?.let { uri ->
//                val intent = Intent(this, StreamActivity::class.java).apply {
//                    putExtra(RTSP_URL, uri)
//                }
//                startActivity(intent)
//            } ?: run {
//                Toast.makeText(this, "RTSP URI haven't been retrieved", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    private fun appendCredentials(streamURI: String): String {
        val protocol = "rtsp://"
        val port: String = "554"
        val path = "stream1"
        return "$protocol$streamURI:$port/$path"
    }

}