package com.wozart.aura.data.model

import com.wozart.aura.entity.model.aura.AuraSceneButton
import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.Scenes


/**
 * Created by Saif on 07/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class ButtonModel {
    var auraButtonName: String? = null
    var button_ip: String= ""
    var thing:String?=null
    var buttonId : String ?= null
    var buttonTapName : String ?= null
    var load : MutableList<RoomModel> = ArrayList()
    var sceneSelectedList: MutableList<Scenes> = ArrayList()
    var scenes = Scene()
    var buttonList : MutableList<AuraSceneButton> = ArrayList()
    var unicastAddress : String ?= null
    var senseUiud : String ?= null
    var senseName : String ?= null
    var senseMacId : String ?= null
    var command : Int ?= null
    var start = 5
    var end = "A"
    var room : String ?= null
    var home : String ?= null
    var senseThing : String ?= null
    var type = "create"
}