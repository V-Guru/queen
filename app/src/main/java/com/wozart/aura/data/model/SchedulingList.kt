package com.wozart.aura.data.model

import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.RoomModel

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 01/10/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class SchedulingList {
    var home:String?=null
    var schduleData: MutableList<AutomationScene> = ArrayList()
}