package com.wozart.aura.entity.sql.users

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-11-26
 * Description :
 *****************************************************************************/
class UserDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION) {
    override fun onCreate(p0: SQLiteDatabase?) {
        val SQL_CREATE_TABLE = "CREATE TABLE " +
               TABLE_NAME + " (" +
                UserContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                USER_FIRSTNAME + " TEXT, " +
                UserContract.UserEntry.USER_EMAIL + " TEXT, " +
                UserContract.UserEntry.USER_CONTACT + " TEXT, " +
                UserContract.UserEntry.USER_PROFILE + " TEXT, " +
                UserContract.UserEntry.USER_HOME + " TEXT, " +
                UserContract.UserEntry.USER_ID + " TEXT, " +
                UserContract.UserEntry.USER_LASTNAME + " TEXT " +
                        ");"

        p0!!.execSQL(SQL_CREATE_TABLE)


    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0!!.execSQL("DROP IF TABLE EXIST" + UserContract.UserEntry.TABLE_NAME)
        onCreate(p0)

    }



    companion object{
        private val DATABASE_NAME = "userDb"
        private val DATABASE_VERSION = 1

        const val TABLE_NAME = "userTable"
        const val USER_FIRSTNAME = "firstname"
        const val USER_EMAIL = "email"
        const val USER_CONTACT = "contact"
        const val USER_PROFILE = "profile"
        const val USER_HOME = "home"
        const val USER_ID = "userid"
        const val USER_LASTNAME = "lastname"
    }
}