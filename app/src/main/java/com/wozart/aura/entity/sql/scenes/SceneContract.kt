package com.wozart.aura.entity.sql.scenes

import android.provider.BaseColumns

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 04/05/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class SceneContract {
    class SceneEntry : BaseColumns {
        companion object : KBaseColumns(){
            const val TABLE_NAME = "scene"
            const val SCENE_ID = "name"
            const val HOME = "home"
            const val ROOM = "room"
            const val LOAD = "load"
            const val ICON = "icon"
            const val REMOTE = "remote"
        }
    }

    open class KBaseColumns  {
        val _ID = "_id"
    }
}