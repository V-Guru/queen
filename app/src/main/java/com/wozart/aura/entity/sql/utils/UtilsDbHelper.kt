package com.wozart.aura.entity.sql.utils

import android.database.sqlite.SQLiteDatabase
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper

class UtilsDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
            val SQL_CREATE_DEVICE_TABLE = "CREATE TABLE " +
                    UtilsContract.UtilsEntry.TABLE_NAME + " (" +
                    UtilsContract.UtilsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    UtilsContract.UtilsEntry.KEY + " TEXT, " +
                    UtilsContract.UtilsEntry.VALUE + " TEXT " +
                    ");"

            sqLiteDatabase.execSQL(SQL_CREATE_DEVICE_TABLE)
        }

        override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UtilsContract.UtilsEntry.TABLE_NAME)
            onCreate(sqLiteDatabase)
        }

        companion object {
            private const val DATABASE_NAME = "utils.db"
            private const val DATABASE_VERSION = 1
        }
}