package com.wozart.aura.ui.auraSense

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.activity_create_remote.*
import android.widget.TextView
import com.wozart.aura.entity.network.EspHandler
import com.wozart.aura.ui.dashboard.GridAutoFitLayoutManager
import com.wozart.aura.ui.dashboard.GridListSpacingItemDecoration
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.dialog.DoubleBtnDialog
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import org.jetbrains.anko.toast


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-02-03
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class CreateSenseRemote : AppCompatActivity(), RemoteButtonIconAdapter.RemoteButtonSelected {

    lateinit var buttonAdapter: RemoteButtonIconAdapter
    var iconList: MutableList<RemoteIconModel> = ArrayList()
    private var selectedIconList: MutableList<RemoteIconModel> = ArrayList()
    private var remoteDataList: MutableList<RemoteListModel> = ArrayList()
    lateinit var btnSave: TextView
    private var mdbSense: SQLiteDatabase? = null
    private var mdbUtils: SQLiteDatabase? = null
    var gson = Gson()
    var appliancesTypeSelected: String? = null
    var remoteData = RemoteListModel()
    var btnName: Array<String> = arrayOf()
    var temperatureList = arrayOf("KEY_POWER","ON", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30")
    var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_remote)
        val dbUtilsHelper = UtilsDbHelper(this)
        mdbUtils = dbUtilsHelper.writableDatabase
        val remoteDbHelper = AuraSenseDbHelper(this)
        mdbSense = remoteDbHelper.writableDatabase
        initialize()

        back_btn.setOnClickListener {
            goBack()
        }

        btnSave.setOnClickListener {
            if (selectedIconList.size > 0) {
                saveData()
            } else {
                SingleBtnDialog.with(this).setHeading(getString(R.string.alert))
                        .setMessage(getString(R.string.please_select_atleast_one_button)).show()
            }
        }

        buttonAdapter = RemoteButtonIconAdapter(this, iconList, this)
        icon_list.layoutManager = GridAutoFitLayoutManager(this, resources.getDimensionPixelSize(R.dimen.device_item_size))
        icon_list.setHasFixedSize(true)
        val spacing = resources.getDimensionPixelSize(R.dimen.normal_margin)
        icon_list.addItemDecoration(GridListSpacingItemDecoration(spacing))
        icon_list.adapter = buttonAdapter
        buttonAdapter.setData(iconList)
    }


    fun initialize() {
        btnSave = findViewById(R.id.btn_save)
        btnName = Common.buttonList
        val info = intent.getStringExtra("REMOTE_DATA")
        remoteData = Gson().fromJson(info, RemoteListModel::class.java)
        type = intent.getStringExtra(Constant.DEVICE_TYPE)
        appliancesTypeSelected = remoteData.typeAppliances

        if (type == "edit") {
            selectedIconList.addAll(remoteData.dynamicRemoteIconList)
        }

        if (appliancesTypeSelected == "AC") {
            iconList.clear()
            for (i in 0 until temperatureList.size) {
                var flag = false
                for (btn in selectedIconList) {
                    if(btn.remoteButtonName == temperatureList.get(i)){
                        btn.remoteIsselected = true
                        btn.remoteIconButton = -1
                        iconList.add(btn)
                        flag = true
                        break
                    }
                }
                if(!flag){
                    val remote = RemoteIconModel()
                    remote.remoteButtonName = temperatureList.get(i)
                    remote.remoteIconButton = -1
                    iconList.add(remote)
                }

            }
        } else {
            iconList.clear()
            for (i in btnName.indices) {
                var flag = false
                for (btn in selectedIconList) {
                    flag = false
                    if (btn.remoteButtonName == btnName.get(i)) {
                        btn.remoteIsselected = true
                        iconList.add(btn)
                        flag = true
                        break
                    }
                }
                if (!flag) {
                    val remote = RemoteIconModel()
                    remote.remoteButtonName = btnName.get(i)
                    iconList.add(remote)
                }
            }
        }
    }

    override fun onButtonSelected(remote_Icon_Model: RemoteIconModel, position: Int) {
        var remoteExist = false
        for (r in selectedIconList) {
            remoteExist = false
            if (r.remoteButtonName == remote_Icon_Model.remoteButtonName) {
                remoteExist = true
                selectedIconList.remove(r)
                break
            }
        }
        if (!remoteExist) {
            selectedIconList.add(remote_Icon_Model)
        }
    }

    private fun goBack() {
        DoubleBtnDialog.with(this).setHeading(getString(R.string.alert))
                .setMessage(getString(R.string.going_back_will_discard))
                .setCallback(object : DoubleBtnDialog.OnActionPerformed {
                    override fun negative() {

                    }

                    override fun positive() {
                    clearList()
                    }

                }).show()



//        iconList.clear()
//        startActivity(Intent(this, DownloadRemoteActivity::class.java)
//                .putExtra("REMOTE_DATA", Gson().toJson(remoteData)))
//        finish()
    }

    private fun clearList(){
        iconList.clear()
        startActivity(Intent(this, DownloadRemoteActivity::class.java)
                .putExtra("REMOTE_DATA", Gson().toJson(remoteData)))
        finish()
    }

    private fun saveData() {
        val remoteListModel = RemoteListModel()
        val applianceBrandName = remoteData.brandName
        val appliancesModel = remoteData.modelNumber
        remoteListModel.auraSenseName = remoteData.auraSenseName
        remoteListModel.remoteLocation = remoteData.remoteLocation
        remoteListModel.dynamicRemoteIconList = selectedIconList
        remoteListModel.typeAppliances = appliancesTypeSelected
        remoteListModel.zmote_ip = remoteData.zmote_ip
        remoteListModel.remoteName = remoteData.remoteName
        remoteListModel.brandName = applianceBrandName
        remoteListModel.modelNumber = appliancesModel
        remoteListModel.senseUiud = remoteData.senseUiud
        remoteListModel.home = remoteData.home
        remoteListModel.dataType = "rNew"
        remoteDataList.add(remoteListModel)

        val intent = Intent(this, SenseRemoteActivity::class.java)
        intent.putExtra("remote", gson.toJson(remoteListModel))
        intent.putExtra(Constant.REMOTE, "Learn Remote")
        intent.putExtra(Constant.DEVICE_TYPE, type)
        startActivity(intent)
        this.finish()
    }

    override fun onBackPressed() {
        goBack()
//        super.onBackPressed()
    }


}