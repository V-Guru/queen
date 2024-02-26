package com.wozart.aura.data.sqlLite

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.entity.sql.buttonDevice.ButtonDbContract
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.utilities.Constant.Companion.CHECK_BUTTON_QUERIES
import com.wozart.aura.utilities.Constant.Companion.DELETE_BUTTON_QUERY
import com.wozart.aura.utilities.Constant.Companion.DELETE_EXISTING_BUTTON
import com.wozart.aura.utilities.Constant.Companion.GET_ALL_BUTTON
import com.wozart.aura.utilities.Constant.Companion.GET_BUTTON_DATA
import com.wozart.aura.utilities.Constant.Companion.GET_SCENE_ASSIGN_BUTTON


/**
 * Created by Saif on 12/08/20.
 * EZJobs
 * mdsaif@onata.com
 */
class AuraButtonTable {

    fun insertButtonDevice(db: SQLiteDatabase, buttonName: String, buttonId: String, buttonTapName: String, buttonUnicast: String, loads: String, home: String, room: String, senseuiud: String, sensethings: String, senseName: String) {
        val params = arrayOf<String>(buttonName, buttonId)
        val cursor = db.rawQuery(CHECK_BUTTON_QUERIES, params)
        if (cursor.count != 0) {
            db.delete(ButtonDbContract.ButtonDbEntry.TABLE_NAME, DELETE_EXISTING_BUTTON, params)
        }
        cursor.close()
        val cv = ContentValues()
        cv.put(ButtonDbContract.ButtonDbEntry.BUTTON_NAME, buttonName)
        cv.put(ButtonDbContract.ButtonDbEntry.BUTTON_ID, buttonId)
        cv.put(ButtonDbContract.ButtonDbEntry.BUTTON_TAP, buttonTapName)
        cv.put(ButtonDbContract.ButtonDbEntry.LOAD, loads)
        cv.put(ButtonDbContract.ButtonDbEntry.BUTTON_UNICAST, buttonUnicast)
        cv.put(ButtonDbContract.ButtonDbEntry.HOME, home)
        cv.put(ButtonDbContract.ButtonDbEntry.ROOM, room)
        cv.put(ButtonDbContract.ButtonDbEntry.SENSE_THING, sensethings)
        cv.put(ButtonDbContract.ButtonDbEntry.SENSE_UIUD, senseuiud)
        cv.put(ButtonDbContract.ButtonDbEntry.SENSE_NAME, senseName)

        try {
            db.beginTransaction()
            db.insert(ButtonDbContract.ButtonDbEntry.TABLE_NAME, null, cv)
            db.setTransactionSuccessful()
        } catch (e: Exception) {

        } finally {
            db.endTransaction()
        }
        return
    }

    @SuppressLint("Recycle")
    fun getSceneControllerData(db: SQLiteDatabase, buttonName: String, buttonId: String): ButtonModel {
        val param = arrayOf(buttonName, buttonId)
        val cursor = db.rawQuery(GET_BUTTON_DATA, param)
        val gson = Gson()
        val buttonModel = ButtonModel()
        while (cursor.moveToNext()) {
            var load: MutableList<RoomModel>
            val load_type = object : TypeToken<List<RoomModel>>() {}.type
            buttonModel.auraButtonName = cursor.getString(0)
            buttonModel.buttonId = cursor.getString(1)
            buttonModel.buttonTapName = cursor.getString(2)
            val data1 = cursor.getString(3)
            load = gson.fromJson(data1, load_type)
            buttonModel.load = load
            buttonModel.unicastAddress = cursor.getString(4)
            buttonModel.room = cursor.getString(5)
            buttonModel.home = cursor.getString(6)
            buttonModel.senseUiud = cursor.getString(7)
            buttonModel.senseName = cursor.getString(8)
            buttonModel.thing = cursor.getString(9)
        }
        cursor.close()
        return buttonModel
    }

    fun getAllSceneControllerData(db: SQLiteDatabase, buttonName: String): MutableList<ButtonModel> {
        val param = arrayOf(buttonName)
        val cursor = db.rawQuery(GET_ALL_BUTTON, param)
        val gson = Gson()
        val buttonList: MutableList<ButtonModel> = ArrayList()
        while (cursor.moveToNext()) {
            var load: MutableList<RoomModel>
            val buttonModel = ButtonModel()
            val load_type = object : TypeToken<List<RoomModel>>() {}.type
            buttonModel.auraButtonName = cursor.getString(0)
            buttonModel.buttonId = cursor.getString(1)
            buttonModel.buttonTapName = cursor.getString(2)
            val data1 = cursor.getString(3)
            load = gson.fromJson(data1, load_type)
            buttonModel.load = load
            buttonModel.unicastAddress = cursor.getString(4)
            buttonModel.room = cursor.getString(5)
            buttonModel.home = cursor.getString(6)
            buttonModel.senseUiud = cursor.getString(7)
            buttonModel.senseName = cursor.getString(8)
            buttonModel.thing = cursor.getString(9)
            buttonList.add(buttonModel)
        }
        cursor.close()
        return buttonList
    }

    fun deleteButtonDevice(db: SQLiteDatabase, buttonName: String) {
        val param = arrayOf(buttonName)
        db.delete(ButtonDbContract.ButtonDbEntry.TABLE_NAME, DELETE_BUTTON_QUERY, param)
    }

    @SuppressLint("Recycle")
    fun getSceneButtonDataContainScene(db: SQLiteDatabase, button: ButtonModel, buttonId: String): ButtonModel {
        val buttonModel = ButtonModel()
        val param = arrayOf(button.buttonTapName, button.auraButtonName, buttonId)
        val gson = Gson()
        val cursor = db.rawQuery(GET_SCENE_ASSIGN_BUTTON, param)
        while (cursor.moveToNext()) {
            var load: MutableList<RoomModel>
            val load_type = object : TypeToken<List<RoomModel>>() {}.type
            buttonModel.auraButtonName = cursor.getString(0)
            buttonModel.buttonId = cursor.getString(1)
            buttonModel.buttonTapName = cursor.getString(2)
            val data1 = cursor.getString(3)
            load = gson.fromJson(data1, load_type)
            buttonModel.load = load
            buttonModel.unicastAddress = cursor.getString(4)
            buttonModel.room = cursor.getString(5)
            buttonModel.home = cursor.getString(6)
            buttonModel.senseUiud = cursor.getString(7)
            buttonModel.senseName = cursor.getString(8)
            buttonModel.thing = cursor.getString(9)

        }
        cursor.close()
        return buttonModel
    }


}