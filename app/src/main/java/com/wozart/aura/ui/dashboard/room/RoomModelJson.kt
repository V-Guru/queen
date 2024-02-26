package com.wozart.aura.ui.dashboard.room

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 13/08/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0.2
 * ____________________________________________________________________________
 *
 *****************************************************************************/
data class RoomModelJson(
        var name: String,
        var type : String,
        var sharedHome: String,
        var bgUrl: String,
        var roomIcon: Int,
        var homeLatitude : Double = 0.0,
        var homeLongitude : Double = 0.0,
        var homeLocation : String
)