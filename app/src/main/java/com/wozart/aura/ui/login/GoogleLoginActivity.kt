package com.wozart.aura.ui.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import butterknife.ButterKnife
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.core.Amplify
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.data.dynamoDb.RulesTableHandler
import com.wozart.aura.data.sqlLite.UserTable
import com.wozart.aura.entity.model.aws.CognitoLoginData
import com.wozart.aura.entity.service.AwsPubSub
import com.wozart.aura.entity.sql.users.UserDbHelper
import com.wozart.aura.espProvision.Provision
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.dashboard.DashboardActivity
import com.wozart.aura.ui.dashboard.UserProfile
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.Encryption.isInternetWorking
import com.wozart.aura.utilities.dialog.DoubleBtnDialog
import kotlinx.android.synthetic.main.activity_google_login.*
import kotlinx.coroutines.*


/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */

class GoogleLoginActivity : BaseAbstractActivity() {

    private var credentialsProvider: CognitoCachingCredentialsProvider? = null
    private var isLoginButtonClick: Boolean = false
    private var localSQLUser = UserTable()
    private var mdbUser: SQLiteDatabase? = null
    var userExist: Boolean = false
    var userList: ArrayList<User> = ArrayList()
    var personPhoto: String = ""
    private var loginType: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_login)
        val dbUserHelper = UserDbHelper(this)
        mdbUser = dbUserHelper.writableDatabase
        val btn_submit = findViewById<Button>(R.id.btn_submit)
        ButterKnife.bind(this)
        isLoginButtonClick = false
        loginType = intent.getStringExtra("type")
        if (loginType == "Verify account") {
            btn_submit.text = loginType
        } else {
            btn_submit.text = loginType
        }
        init()
    }

    private fun loginStart() {
        Amplify.Auth.signInWithWebUI(
                this,
                { result: AuthSignInResult ->
                    if (result.isSignInComplete) {
                        fetchData(result.nextStep.additionalInfo!!.getValue("token"))
                    } else {
                        runOnUiThread {
                            progress_bar.visibility = View.GONE
                            btn_submit.visibility = View.VISIBLE
                        }

                    }
                },
                { error: AuthException ->

                }
        )
    }

    private fun fetchData(token: String) {
        Amplify.Auth.fetchUserAttributes(
                {
                    Log.i("AuthQuickStart", "77---------User attributes = $it")
                    val cognitoLoginData = CognitoLoginData()
                    cognitoLoginData.cognitoUserId = it[0].value
                    if (it[1].key.keyString.contains("identities")) {
                        val googleData = it[1].value
                        val typeData = object : TypeToken<MutableList<CognitoLoginData>>() {}.type
                        cognitoLoginData.identities = Gson().fromJson(googleData, typeData)
                        cognitoLoginData.userId = cognitoLoginData.identities[0].userId
                        cognitoLoginData.providerName = cognitoLoginData.identities[0].providerName
                        cognitoLoginData.emailVerified = it[2].value
                        cognitoLoginData.name = it.find { it.key.keyString.contentEquals("name") }.toString()
                        val givenNameAttribute = it.find { it.key.keyString.contains("given_name") }
                        cognitoLoginData.given_name = givenNameAttribute?.value ?: ""
                        val emailIndex = it.find { it.key.keyString == "email" }
                        cognitoLoginData.email = emailIndex?.value ?: ""
                        val pictureIndex = it.find { it.key.keyString.contains("picture") }
                        cognitoLoginData.picture = pictureIndex?.value ?: ""
                    } else {
                        cognitoLoginData.providerName = "Cognito"
                        cognitoLoginData.emailVerified = true.toString()
                        val emailIndex = it.find { it.key.keyString == "email" }
                        cognitoLoginData.email = emailIndex?.value ?: ""
                    }
                    personPhoto = cognitoLoginData.picture
                    cognitoLoginData.token = token
                    updateUserData(cognitoLoginData, "Login")
                },
                {

                }
        )
    }


    private fun updateUserData(cognitoLoginData: CognitoLoginData, type: String) {
        val prefEditor = PreferenceManager.getDefaultSharedPreferences(this@GoogleLoginActivity).edit()
        prefEditor.putString("EMAIL", cognitoLoginData.email)
        prefEditor.putString("ID_", cognitoLoginData.email)
        prefEditor.putString("USER_ID", "us-east-1:${cognitoLoginData.cognitoUserId}")
        prefEditor.putString("PROFILE_PICTURE", personPhoto)
        prefEditor.putString("VERIFIED_EMAIL", cognitoLoginData.emailVerified)
        prefEditor.putString("HOME", "My Home")
        prefEditor.putString("TOKEN", cognitoLoginData.token)
        prefEditor.putString("LOGIN_PROVIDER", cognitoLoginData.providerName)
        prefEditor.apply()

        if (cognitoLoginData.emailVerified == "true" && loginType == "Login") {
            val userId = "us-east-1:${cognitoLoginData.cognitoUserId}"
            Constant.USER_ID = userId
            Constant.IDENTITY_ID = cognitoLoginData.email.toString()
            userList = localSQLUser.getUsers(mdbUser!!, cognitoLoginData.email.toString())
            registerUserRulesTable(userId, cognitoLoginData)
            progress_bar.visibility = View.GONE
        } else if (cognitoLoginData.emailVerified == "true" && loginType == "Verify account") {
            progress_bar.visibility = View.GONE
            btn_submit.text = getString(R.string.text_done)
            verify_account.visibility = View.GONE
            verify_account_1.visibility = View.GONE
            verified_mail.visibility = View.GONE
            btn_submit.visibility = View.VISIBLE
            loginType = "Login"
        } else {
            if (type == "Login") {
                verify_account.visibility = View.VISIBLE
                verify_account_1.visibility = View.VISIBLE
                progress_bar.visibility = View.GONE
                btn_submit.text = getString(R.string.verify_email)
                btn_submit.visibility = View.VISIBLE
                loginType = "Verify account"
            } else {
                verifyEmail()
            }

        }
    }


    private fun verifyEmail() {
        val intent = packageManager.getLaunchIntentForPackage("com.google.android.gm")
        startActivity(intent)
    }


    private fun checkUserExistLocal(data: User) {
        if (userList.size == 0) {
            userExist = false
            if (!data.firstName.isNullOrEmpty() || !data.lastName.isNullOrEmpty() || !data.mobile.isNullOrEmpty()) {
                val userModal = User()
                userModal.firstName = data.firstName
                userModal.lastName = data.lastName
                userModal.email = data.email
                userModal.mobile = data.mobile
                userModal.user_id = Constant.IDENTITY_ID
                userModal.profileImage = personPhoto
                userModal.userHome = ""
                userList.add(userModal)
                userExist = true
                localSQLUser.insertUser(mdbUser!!, userModal.firstName!!, userModal.email!!, userModal.mobile!!, userModal.profileImage!!, userModal.userHome!!, userModal.user_id!!, userModal.lastName!!)
            }
        }
        if (userList.size > 0 || userExist) {
            val mainIntent = Intent(this@GoogleLoginActivity, DashboardActivity::class.java)
            this@GoogleLoginActivity.startActivity(mainIntent)
            this@GoogleLoginActivity.finish()
        } else if (!userExist) {
            val mainIntent = Intent(this@GoogleLoginActivity, UserProfile::class.java)
            this@GoogleLoginActivity.startActivity(mainIntent)
            this@GoogleLoginActivity.finish()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Provision.REQUEST_PERMISSIONS_CODE -> {
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    verify_account.visibility = View.GONE
                    verify_account_1.visibility = View.GONE
                    progress_bar.visibility = View.VISIBLE
                    loginStart()
                    //loginWithEmail()
                    btn_submit.visibility = View.GONE
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Provision.REQUEST_PERMISSIONS_CODE)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Provision.REQUEST_PERMISSIONS_CODE -> {
                setResult(resultCode)
            }
            Constant.REQUEST_ENABLE_GPS -> {
                checkLocationRequest()
            }
            AWSCognitoAuthPlugin.WEB_UI_SIGN_IN_ACTIVITY_CODE -> {
                Amplify.Auth.handleWebUISignInResponse(data)
            }
        }
    }

    private fun registerUserRulesTable(userId: String, cognitoLoginData: CognitoLoginData) {
        val rulesDynamoTable = RulesTableHandler()
        MainScope().launch {
            AppExecutors().diskIO().execute {
                if (isInternetWorking()) {
                    val data = rulesDynamoTable.checkEmailExist(cognitoLoginData.email.toString())
                    if (data != "SUCCESS") {
                        rulesDynamoTable.insertUser(userId, if (cognitoLoginData.given_name.isEmpty()) cognitoLoginData.name else cognitoLoginData.given_name, cognitoLoginData.email.toString(), cognitoLoginData.emailVerified.toString(), Constant.NEW_USER)
                        runOnUiThread {
                            if (userList.size > 0 || userExist) {
                                val mainIntent = Intent(this@GoogleLoginActivity, DashboardActivity::class.java)
                                this@GoogleLoginActivity.startActivity(mainIntent)
                                this@GoogleLoginActivity.finish()
                            } else if (!userExist) {
                                val mainIntent = Intent(this@GoogleLoginActivity, UserProfile::class.java)
                                this@GoogleLoginActivity.startActivity(mainIntent)
                                this@GoogleLoginActivity.finish()
                            }
                        }
                    } else {
                        val userFound = rulesDynamoTable.getUserScanDetail(cognitoLoginData.email.toString())
                        rulesDynamoTable.insertUser(userFound.user_id!!, userFound.gName, cognitoLoginData.email.toString(), cognitoLoginData.emailVerified.toString(), Constant.EXIST_USER)
                        Thread.sleep(300)
                        checkUserExistLocal(userFound)
                    }
                }
            }
        }
    }

    fun init() {
        credentialsProvider = CognitoCachingCredentialsProvider(
                applicationContext,
                "us-east-1_O450PqbeG",
                AwsPubSub.MY_REGION
        )
        Glide.with(this).load(R.drawable.login_screen_empty).into(background_login)
        btn_submit.setOnClickListener {
            if (loginType == "Verify account") {
                verify_account.visibility = View.GONE
                verify_account_1.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                val tokens = prefs.getString("TOKEN", "NULL")
                fetchData(tokens ?: "")
            } else {
                checkLocationRequest()
            }
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constant.NEW_USER, true).apply()
        }
    }

    private fun checkLocationRequest() {
        DoubleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.location_info))
                .setOptionPositive(getString(R.string.proceed))
                .setOptionNegative(getString(R.string.txt_cancel))
                .setCallback(object : DoubleBtnDialog.OnActionPerformed {
                    override fun positive() {
                        proceedToLocation()
                    }

                    override fun negative() {

                    }

                }).show()

    }

    fun proceedToLocation() {
        if (Common.isLocationEnabled(applicationContext)) {
            if (Common.isLocationPermissionsGranted(applicationContext)) {
                verify_account.visibility = View.GONE
                verify_account_1.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                loginStart()
                btn_submit.visibility = View.INVISIBLE
            } else {
                progress_bar.visibility = View.GONE
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Provision.REQUEST_PERMISSIONS_CODE)
            }
        } else {
            verify_account.visibility = View.VISIBLE
            verify_account_1.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, Constant.REQUEST_ENABLE_GPS)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()

    }
}

