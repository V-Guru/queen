package com.wozart.aura.ui.splashscreen

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.sqlLite.UserTable
import com.wozart.aura.entity.service.AwsPubSub
import com.wozart.aura.entity.service.TcpServerModified
import com.wozart.aura.entity.sql.users.UserDbHelper
import com.wozart.aura.ui.createautomation.GeofenceService
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.UserProfile
import com.wozart.aura.ui.login.GoogleLoginActivity
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 15/05/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class SplashActivity : AppCompatActivity() {

    private lateinit var client: OkHttpClient
    var rulesDynamoTable = RulesTableHandler()
    var userList: ArrayList<User> = ArrayList()
    private var localSQLUser = UserTable()
    private var mdbUser: SQLiteDatabase? = null
    var userId: String? = null
    var email: String? = null
    var profilePic: String? = null
    var newUserId: String? = null

    companion object {
        private val LOG_TAG = SplashActivity::class.java.simpleName
    }


    private fun checkInternet() {
        CoroutineScope(Dispatchers.IO).launch {
            if (isInternetWorking) {
                startService(Intent(this@SplashActivity, AwsPubSub::class.java))
                startService(Intent(this@SplashActivity, GeofenceService::class.java))
                val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
                val verified = prefs.getString("VERIFIED_EMAIL", "NULL")
                //setFireBase()
                if (isLoggedIn) {
                    if (verified == "true") {
                        if (userList.size > 0) {
                            val mainIntent = Intent(this@SplashActivity, DashboardActivity::class.java)
                            startActivity(mainIntent)
                            finish()
                        } else {
                            getUserDetails(newUserId!!)
                        }
                    } else {
                        val mainIntent = Intent(this@SplashActivity, GoogleLoginActivity::class.java)
                        mainIntent.putExtra("type", "Verify account")
                        startActivity(mainIntent)
                        finish()
                    }
                } else {
                    val mainIntent = Intent(this@SplashActivity, GoogleLoginActivity::class.java)
                    mainIntent.putExtra("type", "Login")
                    startActivity(mainIntent)
                    finish()
                }

            } else {
                withContext(Dispatchers.Main) {
                    SingleBtnDialog.with(this@SplashActivity).setHeading(getString(R.string.alert)).setMessage(getString(R.string.user_profile_upload_error)).show()
                    if (userList.size > 0) {
                        val mainIntent = Intent(this@SplashActivity, DashboardActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    } else {
                        val mainIntent = Intent(this@SplashActivity, UserProfile::class.java)
                        startActivity(mainIntent)
                        finish()
                    }
                }
            }
        }
    }


    private val isLoggedIn: Boolean
        get() {
            var flag = true
            val account = Amplify.Auth.currentUser
            val auth_account = GoogleSignIn.getLastSignedInAccount(this)
            if (auth_account != null || userId != "NULL") {
                Constant.GOOGLE_LOGIN_EXIST = true
                userList.clear()
                localSQLUser.deleteTable(mdbUser!!)
                getSharedPreferences("USERNAME", 0).edit().clear().apply()
                getSharedPreferences("EMAIL", 0).edit().clear().apply()
                getSharedPreferences("ID", 0).edit().clear().apply()
                getSharedPreferences("PROFILE_PICTURE", 0).edit().clear().apply()
                getSharedPreferences("VERIFIED_EMAIL", 0).edit().clear().apply()
                getSharedPreferences("TOKEN", 0).edit().clear().apply()
                val prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                prefEditor.clear()
                prefEditor.apply()
                flag = false
            } else {
                if (newUserId == "NULL" && account == null) flag = false
            }
            return flag
        }

    private val isInternetWorking: Boolean
        get() {
            var success = false
            try {
                val url = URL("https://google.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 1000
                connection.connect()
                success = connection.responseCode == 200
            } catch (e: IOException) {
                Log.d(LOG_TAG, "Check Internet connection")
            }
            return success
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.BLACK
        val dbUserHelper = UserDbHelper(this)
        mdbUser = dbUserHelper.writableDatabase
        initialize()
        MainScope().launch {
            checkInternet()
        }

    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, TcpServerModified::class.java))
    }


    fun initialize() {

        CoroutineScope(Dispatchers.Main).launch {
            try {
                AWSMobileClient.getInstance()
                Amplify.addPlugin(AWSCognitoAuthPlugin())
                val config = AmplifyConfiguration.builder(applicationContext).devMenuEnabled(false).build()
                Amplify.configure(config, applicationContext)
            } catch (error: AmplifyException) {

            }
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        userId = prefs.getString("ID", "NULL")
        newUserId = prefs.getString("ID_", "NULL")
        email = prefs.getString("EMAIL", "NULL")
        profilePic = prefs.getString("PROFILE_PICTURE", "NULL")
        userList = localSQLUser.getUsers(mdbUser!!, userId.toString())
        if (!newUserId.equals("NULL")) {
            userList = localSQLUser.getUsers(mdbUser!!, newUserId ?: "")
        }
        Constant.IS_LOGOUT = true
        setTimeZone()

        client = OkHttpClient()
    }

    private fun setTimeZone() {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getDefault()
        val currentLocalTime = calendar.getTime()
        val date = SimpleDateFormat("Z")
        val localTime = date.format(currentLocalTime)
        val offsetTimetype = localTime[0]
        val offsetTimeHours = localTime.substring(1, 3).toInt()
        val offsetTimeMinutes = localTime.substring(3).toInt()
        (if (offsetTimetype.equals("+")) {
            "-$offsetTimeHours:$offsetTimeMinutes"
        } else "-$offsetTimeHours:$offsetTimeMinutes").also { Constant.TIME_ZONE = it }
    }

    private fun getUserDetails(userId: String) {
        AppExecutors().diskIO().execute {
            if (rulesDynamoTable.isUserAlreadyRegistered(userId)) {
                if (userList.size == 0) {
                    Constant.IDENTITY_ID = userId
                    val data = rulesDynamoTable.getUser()
                    if (data != null && (!data.firstName.isNullOrEmpty() || !data.LastName.isNullOrEmpty() || !data.phoneNumber.isNullOrEmpty())) {
                        val userModal = User()
                        userModal.firstName = data.firstName
                        userModal.lastName = data.LastName
                        userModal.email = email
                        userModal.mobile = data.phoneNumber
                        userModal.user_id = userId
                        userModal.profileImage = profilePic
                        userModal.userHome = ""
                        userList.add(userModal)
                        localSQLUser.insertUser(mdbUser!!, userModal.firstName!!, userModal.email!!, userModal.mobile!!, userModal.profileImage!!, userModal.userHome!!, userModal.user_id!!, userModal.lastName!!)
                    }
                }
                runOnUiThread {
                    if (userList.size > 0) {
                        val mainIntent = Intent(this, DashboardActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    } else {
                        val mainIntent = Intent(this, UserProfile::class.java)
                        startActivity(mainIntent)
                        finish()
                    }
                }
            }

        }
    }
}

