package com.wozart.aura.entity.model.aura

import java.io.Serializable

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 22/08/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class AuraLoad : Serializable {
    var name : String? = null
    var favourite : Boolean? = false
    var dimmable : Boolean? = false
    var icon : Int? = 0
    var index : Int ?= 0
    var status : Boolean? = false
    var inputName : String ?= null
    var type :String ?= null
    var hue : Int = 100
    var saturation : Int = 100
    var brightness : Int = 100
    var tempLight : Int = 100
    var module : Int = -1
    var curtainState : String = ""
    var curtainState0 = 0
    var curtainState1 = 0
}