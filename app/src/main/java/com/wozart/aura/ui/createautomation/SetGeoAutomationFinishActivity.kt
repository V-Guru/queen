package com.wozart.aura.aura.ui.createautomation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.wozart.aura.R
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.dashboard.room.RoomModelJson


class SetGeoAutomationFinishActivity : BaseAbstractActivity() {

    lateinit var btn_call: Button
    lateinit var btn_mail: Button
    lateinit var tvTitle: TextView
    lateinit var home: ImageView
    private var mDbUtils: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    var listRoom: MutableList<RoomModelJson> = ArrayList()
    val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geo_automation_finish)
        btn_call = findViewById(R.id.btn_call)
        btn_mail = findViewById(R.id.btn_mail)
        tvTitle = findViewById(R.id.tvTitle)
        home = findViewById(R.id.home)
        tvTitle.setTextColor(ContextCompat.getColor(baseContext, R.color.black_d_n))
        home.setColorFilter(ContextCompat.getColor(baseContext, R.color.black_d_n), PorterDuff.Mode.SRC_ATOP);
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        init()
    }

    @SuppressLint("MissingPermission")
    private fun init() {

        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")
//        for (x in listRoom) {
//            if (x.name == Constant.HOME) {
//
//                Utils.setDrawable(this, activity_geo_automation_finish, x.bgUrl.toInt())
//            }
//        }

        btn_call.setOnClickListener {
            if (checkAndRequestPermissions()) {

                val number = "+918309591336"
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$number")
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this as Activity,
//                                Manifest.permission.CALL_PHONE)) {
//                } else {
//                    ActivityCompat.requestPermissions(this,
//                            arrayOf(Manifest.permission.CALL_PHONE),
//                            MY_PERMISSIONS_REQUEST_CALL_PHONE)
//                }
//            }
                startActivity(intent)
            }

        }

        btn_mail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            val recipients = arrayOf("info@wozart.com")
            intent.putExtra(Intent.EXTRA_EMAIL, recipients)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject text here...")
            intent.putExtra(Intent.EXTRA_TEXT, "Body of the content here...")
            intent.putExtra(Intent.EXTRA_CC, "mailcc@gmail.com")
            intent.type = "text/html"
            intent.setPackage("com.google.android.gm")
            startActivity(Intent.createChooser(intent, "Send mail"))
        }

        home.setOnClickListener {
            finish()
        }

    }



//    fun checkAndroidVersion() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkAndRequestPermissions()) {
//            } else {
//
//            }
//
//        } else {
//            // do code for pre-lollipop devices
//        }
//
//    }



    private fun checkAndRequestPermissions(): Boolean {
        val call = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
        val listPermissionsNeeded = ArrayList<String>()
        if (call != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.d("in fragment on request", "Permission callback called-------")
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.CALL_PHONE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    if (perms[Manifest.permission.CALL_PHONE] == PackageManager.PERMISSION_GRANTED
                    ) {
                        print("Storage permissions are required")
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("in fragment on request", "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show()
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }

    }

}
