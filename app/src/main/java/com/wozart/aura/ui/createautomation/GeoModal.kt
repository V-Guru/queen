package com.wozart.aura.ui.createautomation

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 06/02/19
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class GeoModal {
    var newGeoLat : Double ?= 0.0
    var newGeolong : Double ?= 0.0
    var newGeoRadius : Float ?= 0f
    var AutomationEnable : Boolean = false
    var triggerType : String ?= null
    var triggerWhen : String ?= null
    var triggerSpecificStartTime : String ?= null
    var triggerSpecificEndTime : String ?= null
    var turnOff : String ?= null
    var automationCheckedOnScene : Boolean = false
}