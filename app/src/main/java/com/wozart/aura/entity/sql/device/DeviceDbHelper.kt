package com.wozart.aura.entity.sql.device

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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

class DeviceDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val SQL_CREATE_DEVICE_TABLE = "CREATE TABLE " +
                DeviceContract.DeviceEntry.TABLE_NAME + " (" +
                DeviceContract.DeviceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DeviceContract.DeviceEntry.DEVICE + " TEXT, " +
                DeviceContract.DeviceEntry.LOAD + " TEXT, " +
                DeviceContract.DeviceEntry.HOME + " TEXT, " +
                DeviceContract.DeviceEntry.ROOM + " TEXT DEFAULT 'Hall', " +
                DeviceContract.DeviceEntry.THING + " TEXT, " +
                DeviceContract.DeviceEntry.UIUD + " TEXT, " +
                DeviceContract.DeviceEntry.ACCESS + " TEXT" +
                ");"

        sqLiteDatabase.execSQL(SQL_CREATE_DEVICE_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DeviceContract.DeviceEntry.TABLE_NAME)
        onCreate(sqLiteDatabase)
    }

    companion object {
        private const val DATABASE_NAME = "device.db"
        private const val DATABASE_VERSION = 2
    }
}