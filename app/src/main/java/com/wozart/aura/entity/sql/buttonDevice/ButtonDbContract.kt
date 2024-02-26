package com.wozart.aura.entity.sql.buttonDevice

import android.provider.BaseColumns


/**
 * Created by Saif on 12/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class ButtonDbContract {
    class ButtonDbEntry : BaseColumns{
        companion object : KBaseColumns(){
            const val TABLE_NAME = "auraButton"
            const val BUTTON_NAME = "buttonName"
            const val BUTTON_ID = "id"
            const val BUTTON_TAP = "tapName"
            const val LOAD = "load"
            const val BUTTON_UNICAST ="unicast"
            const val HOME = "home"
            const val ROOM = "room"
            const val SENSE_THING = "thing"
            const val SENSE_UIUD = "uiud"
            const val SENSE_NAME ="senseName"
            const val ACCESS = "access"
        }
    }

    open class KBaseColumns  {
        val _ID = "_id"
    }
}