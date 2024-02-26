package com.wozart.aura.entity.sql.sense

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
class AuraSenseDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(p0: SQLiteDatabase?) {
        val SQL_CREATE_REMOTE_TABLE = "CREATE TABLE " +
                AuraSenseContract.AuraSenseEntry.TABLE_NAME + " (" +
                AuraSenseContract.AuraSenseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AuraSenseContract.AuraSenseEntry.REMOTE_NAME + " TEXT, " +
                AuraSenseContract.AuraSenseEntry.REMOTE_BRAND + " TEXT, " +
                AuraSenseContract.AuraSenseEntry.REMOTE_MODEL + " TEXT, " +
                AuraSenseContract.AuraSenseEntry.REMOTE_FOVOURITE + " TEXT, " +
                AuraSenseContract.AuraSenseEntry.REMOTE_LOCATION + " TEXT, " +
                AuraSenseContract.AuraSenseEntry.DEVICE_NAME + " TEXT, " +
                AuraSenseContract.AuraSenseEntry.BUTTON_ICON + " TEXT, " +
                AuraSenseContract.AuraSenseEntry.APPLIANCE_TYPE + " TEXT, " +
                AuraSenseContract.AuraSenseEntry.HOME_ASSOCIATE + " TEXT " +
                ");"

        p0!!.execSQL(SQL_CREATE_REMOTE_TABLE)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        if (p0!!.version < p2) {
            p0.execSQL("ALTER TABLE ${AuraSenseContract.AuraSenseEntry.TABLE_NAME} ADD COLUMN ${AuraSenseContract.AuraSenseEntry.HOME_ASSOCIATE}")
        }
        p0.execSQL("DROP TABLE IF EXISTS " + AuraSenseContract.AuraSenseEntry.TABLE_NAME)
        onCreate(p0)
    }


    companion object {
        private var DATABASE_NAME = "aurasense.db"
        private var DATABASE_VERSION = 2
    }
}