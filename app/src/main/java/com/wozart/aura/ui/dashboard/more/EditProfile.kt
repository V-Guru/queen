package com.wozart.aura.ui.dashboard.more

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.media.MediaScannerConnection.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.sqlLite.UserTable
import com.wozart.aura.entity.sql.users.UserDbHelper
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import android.graphics.Bitmap as Bitmap1

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-11-27
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class EditProfile : AppCompatActivity() {
    private var localSQLuser = UserTable()
    private var mdbUser: SQLiteDatabase? = null
    var user = User()
    var firstName: String? = null
    var lastName: String? = null
    var contactPhone: String? = null
    var selected_Image: String? = null
    var rulesTableHandler = RulesTableHandler()


    companion object {
        val IMAGE_DIRECTORY = "/wozart"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val dbUserHelper = UserDbHelper(this)
        mdbUser = dbUserHelper.writableDatabase

        val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        val currentLocalTime = calendar.time
        val date = SimpleDateFormat("Z")
        val localTime = date.format(currentLocalTime)


        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val email = prefs.getString("EMAIL", "defaultStringIfNothingFound")
        val userId = prefs.getString("ID_", "NO USER")

        val intent = intent
        firstName = intent.getStringExtra("USER_NAME")
        lastName = intent.getStringExtra("USER_LAST_NAME")
        contactPhone = intent.getStringExtra("USER_CONTACT")
        selected_Image = intent.getStringExtra("PROFILE_PICTURE")

        input_name.setText(firstName)
        input_lastname.setText(lastName)
        input_mobile.setText(contactPhone)
        input_email.setText(email)
        Glide.with(this).load(selected_Image).into(logo_iv)


        profile_pic.setOnClickListener {
            val checkSelfPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                showPictureDialog()
            }
        }

        back.setOnClickListener {
            this.finish()
        }

        save_profile.setOnClickListener {
            if (input_name.text.toString().trim().isEmpty() || input_lastname.text.toString().trim().isEmpty() || input_mobile.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Please fill all details.", Toast.LENGTH_SHORT).show()
            } else {
                localSQLuser.insertUser(mdbUser!!, input_name.text.toString(), email!!, input_mobile.text.toString(), selected_Image!!, Constant.HOME!!, userId!!, input_lastname.text.toString())
                thread {
                    rulesTableHandler.insertUserDetails(input_mobile.text.toString(), input_name.text.toString(), input_lastname.text.toString(), localTime, userId, prefs.getString("LOGIN_PROVIDER", "Cognito"))

                    runOnUiThread {
                        val mainIntent = Intent(applicationContext, ProfileActivity::class.java)
                        startActivity(mainIntent)
                        this.finish()
                    }
                }
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> openGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun takePhotoFromCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), Constant.MY_CAMERA_PERMISSION_CODE)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, Constant.CAMERA)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constant.MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, Constant.CAMERA)
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, Constant.GALLERY)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constant.GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    selected_Image = saveImage(bitmap)
                    Glide.with(this).load(selected_Image).into(logo_iv)
                } catch (e: Exception) {
                }

            }

        } else if (requestCode == Constant.CAMERA) {
            if (data!!.extras!!.get("data") != null) {
                val thumbnail = data.extras!!.get("data") as Bitmap1
                selected_Image = saveImage(thumbnail)
                Glide.with(this).load(selected_Image).into(logo_iv)
            }
        }

    }

    private fun saveImage(myBitmap: Bitmap1): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap1.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File((Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }
        try {
            val f = File(wallpaperDirectory, ((Calendar.getInstance().timeInMillis).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            scanFile(this,
                    arrayOf(f.path),
                    arrayOf("image/jpeg"), null)
            fo.close()
            return f.absolutePath
        } catch (e1: Exception) {

        }
        return ""
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

}