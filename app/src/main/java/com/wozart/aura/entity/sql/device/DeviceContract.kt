package com.wozart.aura.entity.sql.device

import android.provider.BaseColumns

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 30/04/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/

class DeviceContract {
    class DeviceEntry : BaseColumns {
        companion object : KBaseColumns(){
            const val TABLE_NAME = "device"
            const val LOAD = "load"
            const val HOME = "home"
            const val ROOM = "room"
            const val THING = "thing"
            const val UIUD = "uiud"
            const val ACCESS = "access"
            const val DEVICE = "name"
        }
    }

    open class KBaseColumns  {
        val _ID = "_id"
    }
}