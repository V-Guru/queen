package com.wozart.aura.entity.model.scene

import com.wozart.aura.ui.dashboard.Device

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 11/06/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class SceneList {
    var device : String ?= null
    var loads : ArrayList<Device> = ArrayList()
}