package com.wozart.aura.entity.sql.users

import android.provider.BaseColumns

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-11-26
 *****************************************************************************/
class UserContract {
    class UserEntry : BaseColumns{
        companion object : KbaseColumns(){
            const val TABLE_NAME = "userTable"
            const val USER_FIRSTNAME = "firstname"
            const val USER_EMAIL = "email"
            const val USER_CONTACT = "contact"
            const val USER_PROFILE = "profile"
            const val USER_HOME = "home"
            const val USER_ID = "userid"
            const val USER_LASTNAME = "lastname"
        }
        open class KbaseColumns{
            val _ID = "_id"
        }
    }
}