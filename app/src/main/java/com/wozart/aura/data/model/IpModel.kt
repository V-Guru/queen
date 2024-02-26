package com.wozart.aura.data.model

class IpModel {
    var name: String? = null
    var ip: String? = null
    var fullDeviceName: String? = null
    var thing:String?=null
    var state = booleanArrayOf(false, false, false, false,false)
    var twoModuleState = booleanArrayOf(false,false)
    var auraPlugState = booleanArrayOf(false)
    var auraPlugPower : Int = 0
    var dim = intArrayOf(100, 100, 100, 100,100)
    var twoModuleDim = intArrayOf(100,100)
    var curtainState = intArrayOf(0,0)
    var curtainStates : String ?= null
    var uiud:String?=null
    var owned = 0
    var local = false
    var aws = false
    var failure = intArrayOf(0, 0, 0, 0,0)
    var curnLoad =  booleanArrayOf(false, false, false, false,false)
    var home:String?=null
    var room:String?=null
    var module : Int = 0
    var brightness : Int = 100
    var saturation = 100
    var hue = 100
    var tempLight = 100
    var condition = arrayListOf<String>("update","update","update","update","update")

}