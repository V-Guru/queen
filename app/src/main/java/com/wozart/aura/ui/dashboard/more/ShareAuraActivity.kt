package com.wozart.aura.ui.dashboard.more

import android.app.Dialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.Window
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.data.sqlLite.SceneTable
import com.wozart.aura.data.sqlLite.ScheduleTable
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.amazonaws.models.nosql.RulesDO
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.adapter.ShareMasterAdapter
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.*
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_share_aura.*
import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.coroutines.coroutineContext

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 12/09/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0.6
 * ____________________________________________________________________________
 *
 *****************************************************************************/

class ShareAuraActivity : BaseAbstractActivity(), RecyclerItemClicked {


    private lateinit var shareMasterAdapter: ShareMasterAdapter
    private val localSqlUtils = UtilsTable()
    private var mDbUtils: SQLiteDatabase? = null
    private var localSqlDevices = DeviceTable()
    private val localSqlSceneDatabase = SceneTable()
    private var mDb: SQLiteDatabase? = null
    private var mDbScene: SQLiteDatabase? = null
    private var mDbSchedule: SQLiteDatabase? = null
    private val localSqlScheduleDatabase = ScheduleTable()
    lateinit var progressBar: ProgressBar
    var list_room: MutableList<RoomModelJson> = ArrayList()
    val rulesTableHandler = RulesTableHandler()
    var shareEmail: String = ""
    var titleDialogBox: String? = null
    var owner: RulesDO? = null
    var userType = false
    val sharedMasterForAccess: MutableList<MutableMap<String, String>> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_aura)

        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        val dbHelper = DeviceDbHelper(this)
        mDb = dbHelper.writableDatabase

        val dbHelperScene = SceneDbHelper(this)
        mDbScene = dbHelperScene.writableDatabase

        val dbHelperSchedule = ScheduleDbHelper(this)
        mDbSchedule = dbHelperSchedule.writableDatabase
        MainScope().launch {
            init()
        }
    }


    private fun init() {
        val textLayoutShare = findViewById<RelativeLayout>(R.id.text_layout_share)
        val btnAddEmail = findViewById<ImageView>(R.id.btn_add_email)
        val btnShareHome = findViewById<ImageView>(R.id.text_tap)
        progressBar = findViewById(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE
        textLayoutShare.visibility = View.GONE
        back.setOnClickListener {
            this.finish()
        }

        btnAddEmail.setOnClickListener {
            popupDialogue(btnAddEmail)
        }

        btnShareHome.setOnClickListener {
            popupDialogue(text_tap)
        }

        val homeData = localSqlUtils.getHomeData(mDbUtils!!, "home")
        list_room.addAll(homeData.filter { it.type == "home" })

        CoroutineScope(Dispatchers.IO).launch {
            owner = rulesTableHandler.getUser()
            if (owner != null) {
                withContext(Dispatchers.Main) {
                    sharedMasterForAccess.clear()
                    for (x in list_room) {
                        if (x.sharedHome == "master" && x.name == Constant.HOME) {
                            val name = "${owner?.firstName} ${owner?.LastName}"
                            owner_name.text = name
                            if (owner?.master != null && owner?.master!!.size > 0) {
                                for (sharing in owner?.master!!) {
                                    val sharedData = HashMap<String, String>()
                                    sharedData["Access"] = sharing["Access"]!!
                                    sharedData["Home"] = sharing["Home"] ?: ""
                                    sharedData["Name"] = sharing["Name"] ?: ""
                                    sharedData["Email"] = sharing["Email"] ?: ""
                                    sharedData["Status"] = sharing["Status"] ?: ""
                                    sharedData["User"] = "Master"
                                    if (sharing["Access"] == "invite") {
                                        sharedMasterForAccess.add(sharedData)
                                    }
                                }
                                progressBar.visibility = View.GONE
                                updateAdapter()

                            } else {
                                progressBar.visibility = View.GONE
                                textLayoutShare.visibility = View.VISIBLE
                            }
                        } else {
                            if (x.sharedHome == "guest") {
                                if ((owner?.guest != null) && (owner?.guest!!.size > 0)) {
                                    for (shareGuest in owner?.guest!!) {
                                        val name = "${owner?.firstName ?: ""} ${owner?.LastName}"
                                        val sharedData = HashMap<String, String>()
                                        if (shareGuest["Home"] == Constant.HOME) {
                                            owner_name.text = shareGuest["Name"]
                                            sharedData["Name"] = name
                                        }else{
                                            sharedData["Name"] = shareGuest["Name"].toString()
                                        }
                                        sharedData["Access"] = shareGuest["Access"]!!
                                        sharedData["Home"] = shareGuest["Home"] ?: ""
                                        sharedData["Email"] = shareGuest["Email"] ?: ""
                                        sharedData["Status"] = shareGuest["Status"] ?: ""
                                        sharedData["User"] = "Guest"
                                        sharedMasterForAccess.add(sharedData)
                                        textLayoutShare.visibility = View.GONE
                                        progressBar.visibility = View.GONE
                                        updateAdapter()
                                    }
                                } else {
                                    textLayoutShare.visibility = View.VISIBLE
                                    progressBar.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            } else {
                AppExecutors().mainThread().execute {
                    progressBar.visibility = View.GONE
                    selected_contacts_layout.visibility = View.GONE
                    SingleBtnDialog.with(this@ShareAuraActivity).setHeading(getString(R.string.alert)).setMessage(getString(R.string.user_profile_upload_error)).show()
                }
            }
        }
    }

    private fun updateAdapter() {
        sharing_to_layout.visibility = if (sharedMasterForAccess.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        shareMasterAdapter = ShareMasterAdapter(this, this, sharedMasterForAccess)
        master_access.adapter = shareMasterAdapter
        shareMasterAdapter.notifyDataSetChanged()
    }


    private fun shareDevices(input_email: String) {

        Utils.showCustomDialog(this, "Share Home", getString(R.string.dialog_message_add_contact, input_email), R.layout.dialog_popup_sharing, object : DialogListener {

            override fun onOkClicked() {
                progressBar.visibility = View.VISIBLE
                list_room = localSqlUtils.getHomeData(mDbUtils!!, "home")
                for (x in list_room) {
                    if ((x.name == Constant.HOME) and (x.type == "home")) {
                        if (x.sharedHome == "master") {
                            MainScope().launch {
                                var result = false
                                CoroutineScope(Dispatchers.IO).launch {
                                    result = rulesTableHandler.shareDevices(input_email, Constant.HOME!!)
                                    withContext(Dispatchers.Main) {
                                        if (result) {
                                            updateList(input_email)
                                            progressBar.visibility = View.GONE
                                            SingleBtnDialog.with(this@ShareAuraActivity).setHeading(getString(R.string.success)).setMessage(getString(R.string.invitation_share, input_email)).show()
                                            updateAdapter()
                                        } else {
                                            progressBar.visibility = View.GONE
                                            SingleBtnDialog.with(this@ShareAuraActivity).setHeading(getString(R.string.alert)).setMessage(getString(R.string.failed_sharing)).show()
                                        }
                                    }
                                }
                            }
                            break
                        } else {
                            progressBar.visibility = View.GONE
                            SingleBtnDialog.with(this@ShareAuraActivity).setHeading(getString(R.string.alert)).setMessage(getString(R.string.can_not_share)).show()
                        }
                    }
                }
            }

            override fun onCancelClicked() {

            }
        })
    }

    fun updateList(inputEmail: String) {
        val sharedData = HashMap<String, String>()
        sharedData["Access"] = "invite"
        sharedData["Home"] = Constant.HOME!!
        sharedData["Name"] = Common.sharingUserName.toString()
        sharedData["Email"] = inputEmail
        sharedData["Status"] = "modified"
        sharedData["User"] = "Master"
        sharedMasterForAccess.add(sharedData)
    }

    private fun popupDialogue(btn_add_email: ImageView) {
        val dialogue = Dialog(this)
        dialogue.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogue.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogue.setContentView(R.layout.dialogue_request_mail_share)
        val btnCancel = dialogue.findViewById(R.id.btn_cancel) as Button
        val tvShare = dialogue.findViewById<Button>(R.id.tv_share)
        val inputEmail = dialogue.findViewById<TextInputEditText>(R.id.input_email)

        inputEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                shareEmail = p0.toString()
            }

        })

        btnCancel.setOnClickListener { dialogue.dismiss() }
        tvShare.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            Utils.hideSoftKeyboard(this)
            if (!shareEmail.isEmpty() && shareEmail.isValidEmail()) {
                thread {
                    if (Encryption.isInternetWorking()) {
                        val checkExistMail = rulesTableHandler.checkEmailExist(inputEmail.text.toString())
                        runOnUiThread {
                            if (checkExistMail == "SUCCESS") {
                                progressBar.visibility = View.GONE
                                shareDevices(inputEmail.text.toString())
                            } else {
                                progressBar.visibility = View.GONE
                                SingleBtnDialog.with(this@ShareAuraActivity).setHeading(getString(R.string.alert)).setMessage(getString(R.string.login_signup, inputEmail.text)).show()
                            }

                        }
                    } else {
                        progressBar.visibility = View.GONE
                        SingleBtnDialog.with(this@ShareAuraActivity).setHeading(getString(R.string.alert)).setMessage(getString(R.string.user_profile_upload_error)).show()
                    }
                }
            } else if (shareEmail.isEmpty()) {
                inputEmail.error = getString(R.string.valid_email)
                inputEmail.requestFocus()
            } else if (!shareEmail.isValidEmail()) {
                inputEmail.error = getString(R.string.valid_email_address)
                inputEmail.requestFocus()
            }
            dialogue.cancel()
        }
        dialogue.show()
    }

    private fun String.isValidEmail(): Boolean =
            Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun startActivity() {
        startActivity(Intent(this, NotificationActivity::class.java))
        this.finish()
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        val shareList = data as MutableMap<String, String>
        when (viewType) {
            Constant.REVOKE_ACCESS -> {
                if (owner != null) {
                    userType = true
                }
                val dataMessage = getString(R.string.guest_revoke, shareList["Name"]!!, shareList["Home"]!!)
                if (shareList["User"] == "Guest") {
                    titleDialogBox = getString(R.string.share_text_invoke)
                } else {
                    titleDialogBox = getString(R.string.share_text_invoke_master)
                }
                displayDialog(dataMessage, shareList)
            }
        }
    }

    private fun displayDialog(dataMessage: String, shareList: MutableMap<String, String>) {

        Utils.showCustomDialog(this, titleDialogBox!!, dataMessage, R.layout.dialogue_invoke_guest, object : DialogListener {
            override fun onOkClicked() {
                if (userType) {
                    if (shareList["User"] == "Guest") {
                        progressBar.visibility = View.VISIBLE
                        val homeName = shareList["Home"]
                        val homeDetailsNew: MutableList<RoomModelJson> = java.util.ArrayList()
                        for (item in list_room) {
                            if (item.type == "home" && item.name != homeName) {
                                homeDetailsNew.add(item)
                            }
                        }

                        localSqlUtils.replaceHome(mDbUtils!!, "home", homeDetailsNew)
                        localSqlDevices.deleteHome(mDb!!, homeName!!)
                        localSqlSceneDatabase.deleteHomeScenes(mDbScene!!, homeName)
                        localSqlScheduleDatabase.deleteHomeSchedules(mDbSchedule!!, homeName)
                        shareMasterAdapter.notifyDataSetChanged()

                        thread {
                            val userId = Constant.IDENTITY_ID
                            rulesTableHandler.deleteGuestAccessFromMaster(homeName, userId!!)
                            runOnUiThread {
                                progressBar.visibility = View.GONE
                                sharedMasterForAccess.remove(sharedMasterForAccess.find { it["Home"] == shareList["Home"] })
                                updateAdapter()
                            }


//                            runOnUiThread {
//                                val prefEditor = PreferenceManager.getDefaultSharedPreferences(this@ShareAuraActivity).edit()
//                                prefEditor.putString("HOME", "My Home")
//                                prefEditor.apply()
//                                Constant.HOME = "My Home"
//                                val intent = Intent(this@ShareAuraActivity, DashboardActivity::class.java)
//                                intent.putExtra("TAB_SET", Constant.HOME_TAB)
//                                startActivity(intent)
//                            }
                        }
                    } else {
                        progressBar.visibility = View.VISIBLE
                        val homeNmae = shareList["Home"]
                        val email = shareList["Email"]
                        shareMasterAdapter.notifyDataSetChanged()
                        thread {
                            rulesTableHandler.deleteGuestAccessmaster(homeNmae!!, Constant.IDENTITY_ID!!, email)
                            runOnUiThread {
                                progressBar.visibility = View.GONE
                                sharedMasterForAccess.remove(sharedMasterForAccess.find { it["Home"] == shareList["Home"] })
                                updateAdapter()
                            }
                        }
//                        runOnUiThread {
//                            val prefEditor = PreferenceManager.getDefaultSharedPreferences(this@ShareAuraActivity).edit()
//                            prefEditor.putString("HOME", "My Home")
//                            prefEditor.apply()
//                            val intent = Intent(this@ShareAuraActivity, DashboardActivity::class.java)
//                            intent.putExtra("TAB_SET", Constant.MORE_TAB)
//                            startActivity(intent)
//                        }
                    }
                }
            }

            override fun onCancelClicked() {

            }
        })

    }

}



