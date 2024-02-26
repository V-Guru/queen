package com.wozart.aura.ui.dashboard

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputEditText
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.sqlLite.UserTable
import com.wozart.aura.entity.sql.users.UserDbHelper
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_userprofile.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-11-25
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/

class UserProfile : AppCompatActivity() {
    private var localSQLdatabase = UserTable()
    private var mdbUser: SQLiteDatabase? = null
    var userModal = User()
    var userList = ArrayList<User>()
    var rulesDynamoTable = RulesTableHandler()
    lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userprofile)

        val dbHelper = UserDbHelper(this)
        mdbUser = dbHelper.writableDatabase

        val userFirstName = findViewById<TextInputEditText>(R.id.input_name)
        val userLastName = findViewById<TextInputEditText>(R.id.last_input_name)
        val userPhonenumber = findViewById<TextInputEditText>(R.id.phone_number)
        val userMail = findViewById<TextInputEditText>(R.id.email_input)
        progressBar = findViewById(R.id.progress_bar)

        val calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        val currentLocalTime = calendar.getTime()
        val date = SimpleDateFormat("Z")
        val localTime = date.format(currentLocalTime)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val email = prefs.getString("EMAIL", "NO USER")
        val userId = prefs.getString("ID_", "NO USER")
        val home = prefs.getString("HOME", "NO HOME")
        val userProfilePicture = prefs.getString("PROFILE_PICTURE", "defaultStringIfNothingFound")
        userMail.setText(email)
        userList.clear()
        btn_submit.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            if (userFirstName.text.toString().trim().isEmpty() || userLastName.text.toString().trim().isEmpty() || userPhonenumber.text.toString().trim().isEmpty()) {
                progress_bar.visibility = View.GONE
                SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.please_fill_all_details)).show()
            } else {
                userModal.firstName = userFirstName.text.toString()
                userModal.lastName = userLastName.text.toString()
                userModal.email = email
                userModal.mobile = userPhonenumber.text.toString()
                userModal.user_id = userId
                userModal.profileImage = userProfilePicture
                userModal.userHome = home
                userModal.loginProvider = prefs.getString("LOGIN_PROVIDER","Cognito")
                userList.add(userModal)

                if (localSQLdatabase.insertUser(mdbUser!!, userModal.firstName!!, userModal.email!!, userModal.mobile!!, userModal.profileImage!!, userModal.userHome!!, userModal.user_id!!, userModal.lastName!!)) {
                    AppExecutors().diskIO().execute {
                        val result = rulesDynamoTable.insertUserDetails(userModal.mobile!!, userModal.firstName!!, userModal.lastName!!, localTime, userId!!,userModal.loginProvider)
                        if (!result) {
                            runOnUiThread {
                                SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.user_profile_upload_error)).show()
                            }
                        }
                    }
                    progressBar.visibility = View.GONE
                    val preferenece = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    preferenece.putString("FIRST_NAME", userModal.firstName)
                    val mainIntent = Intent(this, DashboardActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                }

            }

        }

    }
}