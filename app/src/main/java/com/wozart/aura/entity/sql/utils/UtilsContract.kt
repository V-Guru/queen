package com.wozart.aura.entity.sql.utils

import android.provider.BaseColumns

class UtilsContract {
    class UtilsEntry : BaseColumns {
        companion object : KBaseColumns(){
            const val TABLE_NAME = "utils"
            const val KEY = "key"
            const val VALUE = "value"
        }
    }

    open class KBaseColumns  {
        val _ID = "_id"
    }
}