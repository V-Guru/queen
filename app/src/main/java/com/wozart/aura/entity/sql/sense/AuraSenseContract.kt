package com.wozart.aura.entity.sql.sense

import android.provider.BaseColumns

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-02-10
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class AuraSenseContract {
    class AuraSenseEntry : BaseColumns{
        companion object : KbaseColumns(){
            const val TABLE_NAME = "remote"
            const val DEVICE_NAME ="device_name"
            const val REMOTE_NAME = "remote_name"
            const val REMOTE_BRAND = "remote_brand"
            const val REMOTE_MODEL = "model_number"
            const val APPLIANCE_TYPE = "type"
            const val BUTTON_ICON = "remote_button"
            const val REMOTE_FOVOURITE = "favourite"
            const val REMOTE_LOCATION = "room"
            const val HOME_ASSOCIATE = "home"

        }

    }
    open class KbaseColumns{
        val _ID = "_id"
    }
}