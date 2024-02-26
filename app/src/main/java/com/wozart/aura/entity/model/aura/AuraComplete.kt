package com.wozart.aura.entity.model.aura

import com.wozart.aura.utilities.Constant

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
class AuraComplete {
        var name = "Error"
        var id: String? = null
        var state = intArrayOf(0, 0, 0, 0,0)
        var dim = intArrayOf(100, 100, 100, 100,100)
        var room: String? = null
        var home:String?=null
        var uiud = Constant.UNPAIRED
        var loads : MutableList<AuraLoad> = ArrayList()
}