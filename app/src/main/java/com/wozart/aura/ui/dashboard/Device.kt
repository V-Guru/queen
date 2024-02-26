package com.wozart.aura.ui.dashboard

/***
 * Created by Kiran on 14-03-2018.
 */

class Device(deviceType: Int, isOn: Boolean,isSelected : Boolean ,dim: Int, deviceName: String, room: String, device: String, node: Int,dimType:Boolean) {
    // dummy values need to be removed. Keeping right now for testing
    var name: String = deviceName
    var roomName: String = room
    var dimVal: Int = dim
    var isTurnOn = isOn
    var status: String = "off"
    var isSelected: Boolean = isSelected
    var type: Int = deviceType
    var deviceName: String = device
    var index: Int = node
    var dimmable :Boolean = dimType
    var devicePresent : Boolean = true
    var deviceChecked : Boolean = false
    var fullDeviceName : String ?=null
    var checkType : String  = ""
    var power : Int = 0
    var hueValue : Int= 100
    var saturationValue : Int = 100
    var tempValue :Int = 100
    var curtainState : String = ""
    var curtainState0 = -1
    var curtainState1 = -1
}
