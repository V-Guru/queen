package com.wozart.aura.ui.sceneController

import android.os.Bundle
import com.wozart.aura.R
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.entity.model.aura.AuraSceneButton
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import com.wozart.aura.ui.createautomation.OnFragmentInteractionListener
import com.wozart.aura.ui.dashboard.Scenes
import com.wozart.aura.utilities.Constant


/**
 * Created by Saif on 10/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class SceneButtonActivity : BaseAbstractActivity(), OnFragmentInteractionListener {

    var buttonList: MutableList<AuraSceneButton> = ArrayList()
    var buttonModel = ButtonModel()
    lateinit var scene : Scenes


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_automation)
        //buttonModel = Gson().fromJson(intent.getStringExtra(Constant.BUTTON_DATA), ButtonModel::class.java)
        init()
    }

    fun init() {

        if (intent.getStringExtra(Constant.PARAM_FRAGMENT) == Constant.SCENE_BUTTON_FRAGMENT) {
            navigateToFragment(SceneButtonFragment(), getString(R.string.empty_tag), true, true)
        } else if (intent.getStringExtra(Constant.PARAM_FRAGMENT) == Constant.BUTTON_PRESSED_FRAGMENT) {
            navigateToFragment(ButtonTapFragment(), "", true, true)
        } else if (intent.getStringExtra(Constant.PARAM_FRAGMENT) == Constant.SELECT_LOAD_FRAGMENT) {
            navigateToFragment(SelectLoadFragment(), "", true, true)
        } else if (intent.getStringExtra(Constant.PARAM_FRAGMENT) == Constant.SET_ACTION_FRAGMENT) {
            navigateToFragment(SetActionButtonFragment(), "", true, true)
        }

    }

    override fun onHomeBtnClicked() {
        onBackPressed()
    }

    override fun onRoomBtnClicked() {
        this.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}