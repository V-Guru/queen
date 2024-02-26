package com.wozart.aura.entity.model.scene

import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.createautomation.RoomModel

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 08/06/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class Scene {
    var name : String ?= null
    var icon : Int = 0
    var room : ArrayList<RoomModel> = ArrayList()
    var roomName : String ?= null
    var home:String?=null
    var isSceneSelected : Boolean = false
    var remoteData : ArrayList<RemoteIconModel> = ArrayList()
}