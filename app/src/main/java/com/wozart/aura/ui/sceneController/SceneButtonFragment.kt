package com.wozart.aura.ui.sceneController

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.entity.model.aura.AuraSceneButton
import com.wozart.aura.ui.adapter.DeviceButtonAdapter
import com.wozart.aura.ui.dashboard.GridAutoFitLayoutManager
import com.wozart.aura.ui.dashboard.GridListSpacingItemDecoration
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.fragment_scene_button.*


/**
 * Created by Saif on 10/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class SceneButtonFragment : androidx.fragment.app.Fragment(),RecyclerItemClicked,View.OnClickListener {

    var activity :Activity ?= null
    lateinit var adapter : DeviceButtonAdapter
    var buttonList : MutableList<AuraSceneButton> = ArrayList()
    var buttonModel = ButtonModel()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
        buttonModel = Gson().fromJson(activity?.intent!!.getStringExtra(Constant.BUTTON_DEVICE_DATA),ButtonModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scene_button,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initialize()

    }

    fun initialize(){
        back_btn.setOnClickListener(this)
        adapter = DeviceButtonAdapter(requireActivity(),this)
        adapter.setData(buttonModel.buttonList)
        rvButton.layoutManager = GridLayoutManager(activity, 2)
        rvButton.setHasFixedSize(true)
//        val spacing_sense_load = resources.getDimensionPixelSize(R.dimen.uniform_half_spacing)
//        rvButton.addItemDecoration(GridListSpacingItemDecoration(spacing_sense_load))
        rvButton.adapter = adapter

    }

    override fun onRecyclerItemClicked(position: Int, data: Any, viewType: Any) {
        if(data is AuraSceneButton){
            val intent = Intent(activity,SceneButtonActivity::class.java)
            intent.putExtra(Constant.PARAM_FRAGMENT,Constant.BUTTON_PRESSED_FRAGMENT)
            intent.putExtra(Constant.BUTTON_PRESSED_DATA,Gson().toJson(data))
            intent.putExtra(Constant.BUTTON_DATA,Gson().toJson(buttonModel))
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