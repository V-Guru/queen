package com.wozart.aura.ui.dashboard.more

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.bumptech.glide.Glide
import com.wozart.aura.R
import com.wozart.aura.aura.data.model.More
import com.wozart.aura.aura.ui.adapter.MoreAdapter
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.sqlLite.*
import com.wozart.aura.entity.service.AwsPubSub
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import com.wozart.aura.entity.sql.scenes.SceneDbHelper
import com.wozart.aura.entity.sql.scheduling.ScheduleDbHelper
import com.wozart.aura.entity.sql.sense.AuraSenseDbHelper
import com.wozart.aura.entity.sql.users.UserDbHelper
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.login.GoogleLoginActivity
import com.wozart.aura.utilities.*
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.stopService


/***
 * Created by Saif on 14-03-2018.
 */

class MoreFragment : androidx.fragment.app.Fragment(), MoreAdapter.OnAdapterInteractionListener {

    private val localSqlDatabase = DeviceTable()
    private val localSqlScene = SceneTable()
    private val localSqlDatabaseSchedule = ScheduleTable()
    private val localSqlSense = AuraSenseTable()
    private var mDbSchedule: SQLiteDatabase? = null
    val localSqlUtils = UtilsTable()
    private var localSQLUser = UserTable()
    var mDbUtils: SQLiteDatabase? = null
    private var mDbScene: SQLiteDatabase? = null
    private var mDb: SQLiteDatabase? = null
    private var mdbUser: SQLiteDatabase? = null
    private var mdbSense: SQLiteDatabase? = null
    private var cachingCredentialsProvider: CognitoCachingCredentialsProvider? = null
    lateinit var recyclerView : RecyclerView


    companion object {
        fun newInstance(): MoreFragment {
            return MoreFragment()
        }

        var THEME_SELECTED = false
    }

    override fun onMenuOptionsSelected(position: Int) {

        when (position) {
            0 -> {
                val intent = Intent(activity, ShareAuraActivity::class.java)
                startActivity(intent)
            }
            1 -> {
                val intent = Intent(activity, CustomizationActivity::class.java)
                startActivity(intent)
            }
            2 -> {
                val intent = Intent(activity, MessagesActivity::class.java)
                startActivity(intent)
            }
            3 -> {
                val intent = Intent(activity, NotificationActivity::class.java)
                startActivity(intent)
            }
            4 -> {
                val intent = Intent(activity, HelpActivity::class.java)
                startActivity(intent)
            }
            5 -> {
                val intent = Intent(activity, VoiceControlActivity::class.java)
                startActivity(intent)

            }
            6 -> {
                val intent = Intent(activity, FeedbackActivity::class.java)
                startActivity(intent)
            }
            7 -> {
                val intent = Intent(activity, AboutUsActivity::class.java)
                startActivity(intent)
            }
            8 -> {
                chooseThemeDialog()
            }

            9 -> {
                requireActivity().let { context ->
                    Utils.showCustomDialog(context, getString(R.string.title_logout), getString(R.string.text_logout_message), R.layout.dialog_layout, object : DialogListener {
                        override fun onOkClicked() {
                            AppExecutors().diskIO().execute {
                                if (Encryption.isInternetWorking()) {
                                    AppExecutors().mainThread().execute {
                                        recyclerView.visibility = View.GONE
                                        loading.visibility = View.VISIBLE
                                        logout()
                                    }
                                } else {
                                    AppExecutors().mainThread().execute {
                                        recyclerView.visibility = View.VISIBLE
                                        loading.visibility = View.GONE
                                        SingleBtnDialog.with(requireContext()).setHeading(getString(R.string.alert)).setMessage(getString(R.string.user_profile_upload_error)).show()
                                    }
                                }
                            }
                        }

                        override fun onCancelClicked() {
                        }
                    })
                }


            }
            else -> {

            }
        }
    }

    fun logout() {
        Amplify.Auth.signOut(
                {
                    Thread.sleep(1000)
                    requireActivity().runOnUiThread {
                        cachingCredentialsProvider?.clear()
                        cachingCredentialsProvider?.clearCredentials()
                        requireActivity().getSharedPreferences("USERNAME", 0).edit().clear().apply()
                        requireActivity().getSharedPreferences("EMAIL", 0).edit().clear().apply()
                        requireActivity().getSharedPreferences("ID_", 0).edit().clear().apply()
                        requireActivity().getSharedPreferences("PROFILE_PICTURE", 0).edit().clear().apply()
                        requireActivity().getSharedPreferences("HOME", 0).edit().clear().apply()
                        requireActivity().getSharedPreferences("HOME_ROOM_DATA", 0).edit().remove("HOME_ROOM_DATA").apply()
                        requireActivity().getSharedPreferences("VERIFIED_EMAIL", 0).edit().clear().apply()
                        requireActivity().getSharedPreferences("TOKEN", 0).edit().clear().apply()
                        val prefEditor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                        prefEditor.clear()
                        prefEditor.apply()
                        localSqlDatabase.deleteTable(mDb!!)
                        localSqlScene.deleteTable(mDbScene!!)
                        localSqlUtils.deleteUtilstTable(mDbUtils!!)
                        localSqlDatabaseSchedule.deleteTable(mDbSchedule!!)
                        localSqlSense.deleteTable(mdbSense!!)
                        localSQLUser.deleteTable(mdbUser!!)
                        Constant.IS_FIRST = true
                        Constant.IS_LOGOUT = true
                        activity?.stopService<AwsPubSub>()
                        val intent = Intent(activity, GoogleLoginActivity::class.java)
                        intent.putExtra("type", "Login")
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        requireActivity().finishAffinity()
                    }

                },
                { error ->
                    AppExecutors().mainThread().execute {
                        recyclerView.visibility = View.VISIBLE
                        loading.visibility = View.GONE
                        SingleBtnDialog.with(requireContext()).setHeading(getString(R.string.alert)).setMessage(error.message!!).show()
                    }
                }
        )
    }


    private fun chooseThemeDialog() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.set_theme))
        val styles = arrayOf("Light", "Dark", "System default")
        val checkedItem = MyPreferences(requireContext()).darkMode

        builder.setSingleChoiceItems(styles, checkedItem) { dialog, which ->

            when (which) {
                0 -> {
                    THEME_SELECTED = true
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    MyPreferences(requireContext()).darkMode = 0
                    dialog.dismiss()
                }
                1 -> {
                    THEME_SELECTED = true
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    MyPreferences(requireContext()).darkMode = 1
                    dialog.dismiss()
                }
                2 -> {
                    THEME_SELECTED = true
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    MyPreferences(requireContext()).darkMode = 2
                    dialog.dismiss()
                }

            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private var adapter: MoreAdapter = MoreAdapter(this)
    private var moreOptions: MutableList<More> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dbScene = SceneDbHelper(requireContext())
        mDbScene = dbScene.writableDatabase
        val versionName = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
        version_code.text = "V$versionName"

        val dbHelper = DeviceDbHelper(requireContext())
        mDb = dbHelper.writableDatabase
        val dbUtils = UtilsDbHelper(requireContext())
        mDbUtils = dbUtils.writableDatabase

        val dbSchedules = ScheduleDbHelper(requireContext())
        mDbSchedule = dbSchedules.writableDatabase

        val dbUserHelper = UserDbHelper(requireContext())
        mdbUser = dbUserHelper.writableDatabase

        val dbSense = AuraSenseDbHelper(requireContext())
        mdbSense = dbSense.writableDatabase

        init()
        initProfileClick()
    }

    private fun initProfileClick() {
        logo_iv.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    fun init() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val userId = prefs.getString("ID_", "NO USER")
        val userList = localSQLUser.getUsers(mdbUser!!, userId!!)
        if (userList[0].profileImage!!.isNotEmpty()) {
            Glide.with(this).load(userList[0].profileImage).into(logo_iv)
        }
        CoroutineScope(Dispatchers.Main).launch {
            try {
                //AWSMobileClient.getInstance()
                cachingCredentialsProvider = CognitoCachingCredentialsProvider(
                        context?.applicationContext,
                        "us-east-1_O450PqbeG",
                        AwsPubSub.MY_REGION
                )
                Amplify.addPlugin(AWSCognitoAuthPlugin())
                val config = AmplifyConfiguration.builder(requireContext()).devMenuEnabled(false).build()
                Amplify.configure(config, requireContext())
            } catch (error: AmplifyException) {

            }
        }
        recyclerView = requireActivity().findViewById(R.id.listOptions)
        adapter.init(moreOptions)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        test()

    }

    private fun test() {
        val menus = resources.getStringArray(R.array.menus)
        val menuImages = resources.getStringArray(R.array.menu_images)

        for (i in 0 until menus.size) {
            val more = More()
            more.optionName = menus.get(i)
            more.url = menuImages.get(i)
            moreOptions.add(more)
        }
    }

    override fun onResume() {
        super.onResume()
        checkTheme()
    }

    private fun checkTheme() {
        when (MyPreferences(requireContext()).darkMode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }


}
