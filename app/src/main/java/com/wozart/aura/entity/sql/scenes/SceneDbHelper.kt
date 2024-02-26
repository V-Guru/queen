package com.wozart.aura.entity.sql.scenes

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.wozart.aura.entity.sql.device.DeviceDbHelper

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
class SceneDbHelper(context: Context): SQLiteOpenHelper(context, SceneDbHelper.DATABASE_NAME, null, SceneDbHelper.DATABASE_VERSION) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val SQL_CREATE_DEVICE_TABLE = "CREATE TABLE " +
                SceneContract.SceneEntry.TABLE_NAME + " (" +
                SceneContract.SceneEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SceneContract.SceneEntry.SCENE_ID + " TEXT, " +
                SceneContract.SceneEntry.HOME + " TEXT, " +
                SceneContract.SceneEntry.ROOM + " TEXT, " +
                SceneContract.SceneEntry.LOAD + " TEXT, " +
                SceneContract.SceneEntry.ICON + " INTEGER, " +
                SceneContract.SceneEntry.REMOTE + " TEXT " +
                ");"

        sqLiteDatabase.execSQL(SQL_CREATE_DEVICE_TABLE)
    }




    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SceneContract.SceneEntry.TABLE_NAME)
        onCreate(sqLiteDatabase)
    }

    companion object {
        private val DATABASE_NAME = "scene.db"
        private val DATABASE_VERSION = 3
    }
}