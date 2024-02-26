package com.wozart.aura.entity.sql.scheduling

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class ScheduleDbHelper(context: Context): SQLiteOpenHelper(context,ScheduleDbHelper.DATABASE_NAME,null,ScheduleDbHelper.DATABASE_VERSION) {


    override fun onCreate(p0: SQLiteDatabase?) {
        val SQL_CREATE_SCHEDULE_TABLE =  "CREATE TABLE " +
        ScheduleContract.ScheduleEntry.TABLE_NAME + " (" +
                ScheduleContract.ScheduleEntry.ID + "  INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ScheduleContract.ScheduleEntry.SCHEDULE_NAME + " TEXT, " +
                ScheduleContract.ScheduleEntry.SCHEDULE_PROPERTY + " TEXT, " +
                ScheduleContract.ScheduleEntry.SCHEDULE_TYPE + " TEXT, "  +
                ScheduleContract.ScheduleEntry.START_TIME + " TEXT, " +
                ScheduleContract.ScheduleEntry.END_TIME + " TEXT, " +
                ScheduleContract.ScheduleEntry.ROUTINE + " TEXT, " +
                ScheduleContract.ScheduleEntry.LOAD + " TEXT, " +
                ScheduleContract.ScheduleEntry.ICON + " INTEGER, " +
                ScheduleContract.ScheduleEntry.ROOM + " TEXT, " +
                ScheduleContract.ScheduleEntry.HOME + " Text, " +
                ScheduleContract.ScheduleEntry.REMOTE + " TEXT " +
        ");"

        if (p0 != null) {
            p0.execSQL(SQL_CREATE_SCHEDULE_TABLE)
        }

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        if (p0 != null) {
            p0.execSQL("DROP TABLE IF EXISTS" + ScheduleContract.ScheduleEntry.TABLE_NAME)
        }

    }
    companion object {
        private val DATABASE_NAME = "scheduledb.db"
        private val DATABASE_VERSION = 14
    }
}