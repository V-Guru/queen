package com.wozart.aura.ui.auraSense

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-01-31
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class RemoteIconModel {
    var type = -1
    var name :String ? = null
    var remoteButtonName : String ?= null
    var remoteIconButton = 0
    var remoteIsselected : Boolean = false
    var senseHex : String = ""
    var start1 = 0
    var start2= 0
    var zero_end = 0
    var one_end = 0
    var channelNumber = ""
    var btnFavourite = false
    var remoteModel : String ?= null
    var dType :String ?= null
    var rArr : Array<Int> = arrayOf()
    var sz : Int = 0
    var fz :Int = 0
    var proto :Int = 0
    var state : Array<Int> = arrayOf()
    var value : Long = 0
    var isSelected = false
    var error  = -1
    var brandName : String ?= null
    var remoteName : String ?= null
    var channelShortCut : Boolean = false
    var internetChannel : Boolean = false

}