package com.wozart.aura.entity.sql.camera

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


/**
 * Created by Saif on 18/01/21.
 * mds71964@gmail.com
 */
class CameraDbHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "camera.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        val SQL_TABLE = "CREATE TABLE " +
                CameraContract.CameraEntry.TABLE_NAME + " (" +
                CameraContract.CameraEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CameraContract.CameraEntry.CAMERA_NAME + " TEXT, " +
                CameraContract.CameraEntry.HOME + " TEXT, " +
                CameraContract.CameraEntry.ROOM + " TEXT, " +
                CameraContract.CameraEntry.CAMERA_URL + " TEXT, " +
                CameraContract.CameraEntry.FAVOURITE + " TEXT " +
        ");"
        p0?.execSQL(SQL_TABLE)

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("DROP TABLE IF EXISTS "+CameraContract.CameraEntry.TABLE_NAME)
        onCreate(p0)
    }
}