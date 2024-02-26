package com.wozart.aura.entity.model.aura

import com.wozart.aura.utilities.Constant

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 04/05/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class AuraSwitch {
    var type = -1
    var name = "Error"
    var thing: String? = null
    var state = intArrayOf(0, 0, 0, 0,0)
    var dim = intArrayOf(100, 100, 100, 100,100)
    var mdl: Int = 0
    var ip: String? = null
    var uiud = Constant.UNPAIRED
    var aws = 0
    var error: Int = -1
    var online = -1
    var led = -1
    var id: String? = null
    val dsy = -1
    val t = -1
    val r = -1
    val k = -1
    val c = -1
    val p = -1
    var data :String ?= null
    var loads : MutableList<AuraSwitchLoad> = ArrayList()
    var aura_sense_confi : MutableList<AuraSenseConfigure> = ArrayList()
    var room = "Living Room"
    var key = 0
    var WIFI :Int = -1
    var CloudPresence : Boolean = false
    var esp_device: String ?= null
    var temp : String ?= null
    var humid : String ?= null
    var motion : String ?= null
    var ldr : String ?= null
    var unicast : String ?= null
    var command : Int ?= null
    var p0 : Int = 0
    var brightness = 100
    var saturation = 0
    var hue = 0
    var temperature = 0
    var deviceEndName =""

//    fun AuraSwitch() {
//        val l = AuraSwitchLoad()
//        this.loads = l.defaultLoadList(dummyDevice.mdl)
//    }

    fun setDummyStates(node: Int) {
        for (i in 0..3) {
            if (i == node) {
                if (this.state[i] == 0)
                    this.state[i] = 1
                else
                    this.state[i] = 0
            }
        }
    }

    fun setDummyDims(node: Int, dim: Int) {
        for (i in 0..3) {
            if (i == node) {
                this.dim[i] = dim
            }
        }
    }
}