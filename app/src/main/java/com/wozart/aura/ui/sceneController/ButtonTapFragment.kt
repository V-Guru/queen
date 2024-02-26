package com.wozart.aura.ui.sceneController

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.sqlLite.AuraButtonTable
import com.wozart.aura.entity.model.aura.AuraLongSingleDouble
import com.wozart.aura.entity.model.aura.AuraSceneButton
import com.wozart.aura.entity.sql.buttonDevice.ButtonDbHelper
import com.wozart.aura.ui.adapter.ButtonTapAdapter
import com.wozart.aura.ui.customview.LinearLayoutManager
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.Constant.Companion.BUTTON_DATA
import com.wozart.aura.utilities.Constant.Companion.BUTTON_PRESSED_DATA
import com.wozart.aura.utilities.Constant.Companion.BUTTON_SELECTED_ID
import com.wozart.aura.utilities.Constant.Companion.BUTTON_TAP_ID
import com.wozart.aura.utilities.Constant.Companion.PARAM_FRAGMENT
import kotlinx.android.synthetic.main.fragment_scene_button.*


/**
 * Created by Saif on 11/08/20.
 * EZJobs
 * mdsaif@onata.com
 */
class ButtonTapFragment : androidx.fragment.app.Fragment(),RecyclerItemClicked,View.OnClickListener {

    var activity : Activity?= null
    lateinit var adapter : ButtonTapAdapter
    var selectedButton = AuraSceneButton()
    var buttonModel = ButtonModel()
    private var localSqlButton = AuraButtonTable()
    private var mdbButton : SQLiteDatabase ?= null
    var databasebuttonModel :MutableList<ButtonModel> = ArrayList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity =  context as Activity
        selectedButton = Gson().fromJson(activity?.intent!!.getStringExtra(BUTTON_PRESSED_DATA),AuraSceneButton::class.java)
        buttonModel = Gson().fromJson(activity?.intent!!.getStringExtra(BUTTON_DATA),ButtonModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scene_button,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    fun initialize(){
        val dbButton = ButtonDbHelper(requireContext())
        mdbButton = dbButton.writableDatabase
        back_btn.setOnClickListener(this)
        databasebuttonModel = localSqlButton.getAllSceneControllerData(mdbButton!!, buttonModel.auraButtonName!!)
//        if(buttonModel.buttonTapName == "Single Tap"){
//            val tappingArrayId= arrayOf(Constant.BTN1_SINGL_TAP,Constant.BTN1_DOUBL_TAP,Constant.BTN1_LONG_TAP)
//            for(i in tappingArrayId){
//                val buttonModel = localSqlButton.getSceneButtonDataContainScene(mdbButton!!,buttonModel,i)
//                sceneSelectedTap.add(buttonModel)
//            }
//
//        }
        tvButtonName.visibility = View.VISIBLE
        tvButtonName.text = selectedButton.buttonName
        adapter = ButtonTapAdapter(requireActivity(),this)
        scenesButton.text = getString(R.string.button_tap_title)
        rvButton.layoutManager = LinearLayoutManager(requireActivity(),LinearLayout.VERTICAL,false)
        rvButton.adapter = adapter
        adapter.setData(selectedButton.singleDoubleLong, databasebuttonModel)
    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if(data is AuraLongSingleDouble){
            buttonModel.buttonTapName = data.btnName
            val intent = Intent(activity,SceneButtonActivity::class.java)
            intent.putExtra(PARAM_FRAGMENT,Constant.SELECT_LOAD_FRAGMENT)
            intent.putExtra(BUTTON_TAP_ID,data.btnId)
            intent.putExtra(BUTTON_SELECTED_ID,selectedButton.buttonId)
            intent.putExtra(BUTTON_DATA,Gson().toJson(buttonModel))
            startActivity(intent)
        }

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.back_btn -> {
                activity?.onBackPressed()
            }
        }
    }
}