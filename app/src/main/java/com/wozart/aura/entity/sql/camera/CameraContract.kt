package com.wozart.aura.entity.sql.camera

import android.provider.BaseColumns


/**
 * Created by Saif on 18/01/21.
 * mds71964@gmail.com
 */

class CameraContract {
    class CameraEntry : BaseColumns{
        companion object : KbaseColumns(){
            const val TABLE_NAME = "camera"
            const val HOME = "home"
            const val ROOM = "room"
            const val CAMERA_NAME = "name"
            const val CAMERA_URL = "url"
            const val FAVOURITE = "fav"
        }
    }

    open class KbaseColumns{
        val _ID = "_id"
    }
}