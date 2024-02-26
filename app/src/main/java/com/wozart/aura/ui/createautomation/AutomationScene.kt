package com.wozart.aura.ui.createautomation

import com.wozart.aura.ui.auraSense.RemoteIconModel

class AutomationScene{
    var name  : String? = null
    var icon : Int = 0
    var room : ArrayList<RoomModel> = ArrayList()
    var load : MutableList<RoomModel> = ArrayList()
    var property : MutableList<GeoModal> = ArrayList()
    var time : String? =null
    var type : String? =null
    var endTime : String? = null
    var routine : String? = null
    var home :String ?= null
    var isSelected : Boolean = false
    var remote : MutableList<RemoteIconModel> = ArrayList()
    var updatedRoutine :String ?= null

}