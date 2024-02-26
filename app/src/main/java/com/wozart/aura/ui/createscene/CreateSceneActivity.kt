package com.wozart.aura.ui.createscene


import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import com.wozart.aura.R
import com.wozart.aura.ui.createautomation.OnFragmentInteractionListener
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.data.sqlLite.UtilsTable
import com.wozart.aura.entity.sql.utils.UtilsDbHelper
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.activity_create_automation.*


class CreateSceneActivity : BaseAbstractActivity(), OnFragmentInteractionListener {
    override fun onRoomBtnClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var sceneName: String? = null
    private var sceneType: String? = null
    private var sceneIconUrl : Int ?=0
    private var mDbUtils: SQLiteDatabase? = null
    private val localSqlUtils = UtilsTable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_automation)
        val dbUtils = UtilsDbHelper(this)
        mDbUtils = dbUtils.writableDatabase

        var listRoom: MutableList<RoomModelJson> = ArrayList()
        listRoom = localSqlUtils.getHomeData(mDbUtils!!, "home")

        for(x in listRoom){
            if(x.name == Constant.HOME) {

                Utils.setDrawable(this, containerAutomation, x.bgUrl.toInt())

            }

        }
        //Utils.setDrawable(this, containerAutomation)
        val intent = intent
        sceneType = intent.getStringExtra("inputSceneType")
        sceneName = intent.getStringExtra("inputSceneName")
        sceneIconUrl = intent.getIntExtra("inputSceneIconUrl",0)
        init()
    }

    private fun init() {
       //val scenelist =  localSqlScene.getSceneByName(mDbScene!!, intent.getStringExtra("inputSceneName"))
        //Log.d("TAG", scenelist.toString())

        navigateToFragment(SelectDeviceForSceneFragment(), getString(R.string.empty_tag), true, true)
    }

    override fun onHomeBtnClicked() {
        onBackPressed()
    }



     fun getSceneName() : String?{
        return sceneName
    }

    fun getSceneIconUrl() : Int?{
        return sceneIconUrl
    }

    fun getSceneType() : String?{
        return sceneType
    }


}