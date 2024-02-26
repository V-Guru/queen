package com.wozart.aura.ui.createautomation

import com.wozart.aura.ui.dashboard.Device

/**
 * Created by Niranjan P on 3/22/2018.
 */
// Assumming this could be room model
class RoomModel {
    var name: String? = null
    var deviceList: ArrayList<Device> = ArrayList()
    fun copy(): RoomModel {
        val roomModel = RoomModel()
        roomModel.name = this.name   //copy all fields if new fields added to this model
        roomModel.deviceList = deviceList
        return roomModel
    }
}
