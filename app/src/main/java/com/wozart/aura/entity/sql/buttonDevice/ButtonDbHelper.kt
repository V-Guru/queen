package com.wozart.aura.entity.sql.buttonDevice

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


/**
 * Created by Saif on 12/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class ButtonDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "auraButton.db"
        private const val DATABASE_VERSION = 3
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = "CREATE TABLE " +
                ButtonDbContract.ButtonDbEntry.TABLE_NAME + " (" +
                ButtonDbContract.ButtonDbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ButtonDbContract.ButtonDbEntry.BUTTON_NAME + " TEXT, " +
                ButtonDbContract.ButtonDbEntry.BUTTON_ID + " TEXT, " +
                ButtonDbContract.ButtonDbEntry.BUTTON_TAP + " TEXT, " +
                ButtonDbContract.ButtonDbEntry.LOAD + " TEXT, " +
                ButtonDbContract.ButtonDbEntry.BUTTON_UNICAST + " TEXT, " +
                ButtonDbContract.ButtonDbEntry.ROOM + " TEXT, " +
                ButtonDbContract.ButtonDbEntry.HOME + " TEXT, " +
                ButtonDbContract.ButtonDbEntry.SENSE_UIUD + " TEXT, " +
                ButtonDbContract.ButtonDbEntry.SENSE_NAME + " TEXT, " +
                ButtonDbContract.ButtonDbEntry.SENSE_THING + " TEXT" +
                ");"

        db!!.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + ButtonDbContract.ButtonDbEntry.TABLE_NAME)
        onCreate(db)
    }
}