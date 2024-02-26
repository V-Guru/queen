package com.wozart.aura.ui.auraSense

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.ui.base.IconsAdapter
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.more.EditProfile
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_add_channel.*
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.layout_header.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


/**
 * Created by Saif on 31/08/20.
 * EZJobs
 * mdsaif@onata.com
 */
class AddChannelActivity : AppCompatActivity(), View.OnClickListener, RecyclerItemClicked {

    var channelNumber: String? = null
    var channelImageArray: MutableList<Int> = arrayListOf(R.drawable.ic_aajtak, R.drawable.ic_colorstv, R.drawable.ic_espn,
            R.drawable.ic_etv, R.drawable.ic_geminitv, R.drawable.ic_hbo, R.drawable.ic_mnx, R.drawable.ic_moviesnow,
            R.drawable.ic_ndtvindia, R.drawable.ic_ntv, R.drawable.ic_sonysab, R.drawable.ic_sonysab, R.drawable.ic_starplus,
            R.drawable.ic_starmovies, R.drawable.ic_starsports, R.drawable.ic_starmaa, R.drawable.ic_tv9telugu, R.drawable.ic_zeetelugu, R.drawable.ic_zeetv, R.drawable.ic_add_channel_new
    )
    lateinit var channelIconAdapter: IconsAdapter
    var channelSelectedImage: String? = null
    var insertImagePos: Int? = null
    var remoteData = RemoteListModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_channel)
        initialize()
        setListener()
    }

    fun initialize() {
        remoteData = Gson().fromJson(intent.getStringExtra(Constant.REMOTE_DATA), RemoteListModel::class.java)
        rvImages.layoutManager = GridLayoutManager(this, 4)
        rvImages.setHasFixedSize(true)
        channelIconAdapter = IconsAdapter { pos: Int, Boolean ->

        }
        channelIconAdapter.viewTypeIcon = Constant.CHANNEL_ICON_VIEW
        rvImages.adapter = channelIconAdapter
        tvTitle.text = getString(R.string.add_channel)
        tvNext.text = getString(R.string.text_save)
        setAdapter()

    }

    private fun setAdapter() {
        channelIconAdapter.init(channelImageArray, 0)
    }

    private fun setListener() {
        tvNext.setOnClickListener(this)
        home.setOnClickListener(this)
        etNumber_1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (etNumber_1.text.toString().length == 1) {
                    etNumber_1.clearFocus()
                    etNumber_2.requestFocus()
                    etNumber_2.isCursorVisible = true

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        etNumber_2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (etNumber_2.text.toString().length == 1) {
                    etNumber_2.clearFocus()
                    etNumber_3.requestFocus()
                    etNumber_3.isCursorVisible = true

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        etNumber_3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (etNumber_3.text.toString().length == 1) {
                    etNumber_3.clearFocus()
                    etNumber_4.requestFocus()
                    etNumber_4.isCursorVisible = true

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        etNumber_4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (etNumber_4.text.toString().length == 1) {
                    Utils.hideSoftKeyboard(this@AddChannelActivity)
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvNext -> {
                if (inputChannelName.text.toString().isEmpty() || (etNumber_1.text.toString().isEmpty() && etNumber_2.text.toString().isEmpty() && etNumber_3.text.toString().isEmpty() && etNumber_4.text.toString().isEmpty())) {
                    if (inputChannelName.text.toString().isEmpty()) {
                        inputChannelName.error = getString(R.string.please_give_channel_name)
                        inputChannelName.requestFocus()
                    } else {
                        SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.please_give_number)).show()
                    }

                } else {
                    if (etNumber_1.text.toString() == "0" && etNumber_2.text.toString() == "0" && etNumber_3.text.toString() == "0") {
                        channelNumber = etNumber_4.text.toString()
                    } else if (etNumber_1.text.toString() == "0" && etNumber_2.text.toString() == "0") {
                        channelNumber = etNumber_3.text.toString() + etNumber_4.text.toString()
                    } else if (etNumber_1.text.toString() == "0") {
                        channelNumber = etNumber_2.text.toString() + etNumber_3.text.toString() + etNumber_4.text.toString()
                    } else {
                        channelNumber = etNumber_1.text.toString() + etNumber_2.text.toString() + etNumber_3.text.toString() + etNumber_4.text.toString()
                    }

                    if (checkChannelPresence(inputChannelName.text.toString(), channelNumber!!)) {
                        SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.channel_exist)).show()
                    } else {
                        //Common.addChangelist(inputChannelName.text.toString(), channelNumber!!)
                        val intent = Intent()
                        intent.putExtra(Constant.CHANNEL_NAME, inputChannelName.text!!.trim().toString())
                        intent.putExtra(Constant.CHANNEL_NUMBER, channelNumber)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }

            }
            R.id.home -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        when (viewType) {
            Constant.CAMERA -> {
                val checkSelfPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                } else {
                    showPictureDialog()
                }
            }
        }
    }

    private fun checkChannelPresence(channelName: String, channelNumber: String): Boolean {
        var flag = false
        for (channel in remoteData.dynamicRemoteIconList) {
            if (channel.channelNumber == channelNumber || channel.remoteButtonName == channelName) {
                flag = true
                break
            }
        }
        return flag
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showPictureDialog() {
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
                    channelSelectedImage = saveImage(bitmap)
                    channelImageArray.add(insertImagePos!!)
                    setAdapter()
                    // Glide.with(this).load(channelSelectedImage).into(logo_iv)
                } catch (e: Exception) {
                }

            }

        } else if (requestCode == Constant.CAMERA) {
            if (data!!.extras!!.get("data") != null) {
                val thumbnail = data.extras!!.get("data") as Bitmap
                channelSelectedImage = saveImage(thumbnail)
                channelImageArray.add(insertImagePos!!)
                setAdapter()
                //  Glide.with(this).load(channelSelectedImage).into(logo_iv)
            }
        }

    }

    private fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File((Environment.getExternalStorageDirectory()).toString() + EditProfile.IMAGE_DIRECTORY)
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }
        try {
            val f = File(wallpaperDirectory, ((Calendar.getInstance().timeInMillis).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this,
                    arrayOf(f.path),
                    arrayOf("image/jpeg"), null)
            fo.close()
            return f.absolutePath
        } catch (e1: Exception) {

        }
        return ""
    }
}