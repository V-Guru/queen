package com.wozart.aura.data.sqlLite

import android.content.ContentValues
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.entity.sql.scheduling.ScheduleContract
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.GeoModal
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.utilities.Constant.Companion.AUTOMATION_EXIST_QUERY
import com.wozart.aura.utilities.Constant.Companion.AUTOMATION_SCENE_QUERY
import com.wozart.aura.utilities.Constant.Companion.CHECK_AUTOMATION_QUERY
import com.wozart.aura.utilities.Constant.Companion.DELETE_AUTOMATION_SCENE
import com.wozart.aura.utilities.Constant.Companion.DELETE_HOME_SCENES
import com.wozart.aura.utilities.Constant.Companion.GET_ALL_AUTOMATION
import com.wozart.aura.utilities.Constant.Companion.GET_AUTOMATION_SCENE_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_AUTOMATION_SCENE_QUERY_NAME
import com.wozart.aura.utilities.Constant.Companion.UPDATE_AUTOMATION_SCENE

class ScheduleTable {
    fun insertSchedule(db: SQLiteDatabase, name: String, icon: Int, rooms: String, load: String, property: String, starttime: String, type: String, endtime: String, routine: String, home: String, automationSceneNameOld: String, automationSceneType: String, remote: String): Boolean {

        if (automationSceneType == "create") {
            var automationSceneExist = false
            val paramcheck = arrayOf<String>(name, home)
            val cursorcheck = db.rawQuery(CHECK_AUTOMATION_QUERY, paramcheck)
            while (cursorcheck.moveToNext()) {
                automationSceneExist = true
            }
            if (automationSceneExist) {
                cursorcheck.close()
                return false
            }

            val cv = ContentValues()
            cv.put(ScheduleContract.ScheduleEntry.SCHEDULE_NAME, name)
            cv.put(ScheduleContract.ScheduleEntry.ICON, icon)
            cv.put(ScheduleContract.ScheduleEntry.ROOM, rooms)
            cv.put(ScheduleContract.ScheduleEntry.LOAD, load)
            cv.put(ScheduleContract.ScheduleEntry.SCHEDULE_PROPERTY, property)
            cv.put(ScheduleContract.ScheduleEntry.START_TIME, starttime)
            cv.put(ScheduleContract.ScheduleEntry.SCHEDULE_TYPE, type)
            cv.put(ScheduleContract.ScheduleEntry.END_TIME, endtime)
            cv.put(ScheduleContract.ScheduleEntry.ROUTINE, routine)
            cv.put(ScheduleContract.ScheduleEntry.HOME, home)
            cv.put(ScheduleContract.ScheduleEntry.REMOTE, remote)
            try {
                db.beginTransaction()
                db.insert(ScheduleContract.ScheduleEntry.TABLE_NAME, null, cv)
                db.setTransactionSuccessful()

            } catch (e: SQLException) {
                error("Error $e")
            } finally {
                db.endTransaction()
            }
            return true
        } else {
            if (name != automationSceneNameOld) {
                var automationSceneExist = false
                val paramcheck = arrayOf<String>(name, home)
                val cursorcheck = db.rawQuery(CHECK_AUTOMATION_QUERY, paramcheck)
                while (cursorcheck.moveToNext()) {
                    automationSceneExist = true
                }
                if (automationSceneExist) {
                    cursorcheck.close()
                    return false
                }
            }
            val params = arrayOf<String>(automationSceneNameOld)
            val cursor = db.rawQuery(AUTOMATION_SCENE_QUERY, params)
            if (cursor.count != 0) {
                val cnv = ContentValues()
                cnv.put(ScheduleContract.ScheduleEntry.SCHEDULE_NAME, name)
                cnv.put(ScheduleContract.ScheduleEntry.ICON, icon)
                cnv.put(ScheduleContract.ScheduleEntry.ROOM, rooms)
                cnv.put(ScheduleContract.ScheduleEntry.LOAD, load)
                cnv.put(ScheduleContract.ScheduleEntry.SCHEDULE_PROPERTY, property)
                cnv.put(ScheduleContract.ScheduleEntry.START_TIME, starttime)
                cnv.put(ScheduleContract.ScheduleEntry.SCHEDULE_TYPE, type)
                cnv.put(ScheduleContract.ScheduleEntry.END_TIME, endtime)
                cnv.put(ScheduleContract.ScheduleEntry.ROUTINE, routine)
                cnv.put(ScheduleContract.ScheduleEntry.REMOTE, remote)
                db.update(ScheduleContract.ScheduleEntry.TABLE_NAME, cnv, UPDATE_AUTOMATION_SCENE, arrayOf(automationSceneNameOld))
                cursor.close()
            }
        }
        return true
    }

    fun updatechedule(db: SQLiteDatabase, name: String, icon: Int, rooms: String, load: String, property: String, starttime: String, type: String, endtime: String, routine: String, home: String, automationSceneNameOld: String, remote: String): Boolean {

        var automationSceneExist = false
        val paramcheck = arrayOf<String>(name, home)
        val cursorcheck = db.rawQuery(CHECK_AUTOMATION_QUERY, paramcheck)
        if (cursorcheck.count != 0) {
            val params = arrayOf(automationSceneNameOld, home)
            db.delete(ScheduleContract.ScheduleEntry.TABLE_NAME, DELETE_AUTOMATION_SCENE, params)
        }
        cursorcheck.close()

        val cv = ContentValues()
        cv.put(ScheduleContract.ScheduleEntry.SCHEDULE_NAME, name)
        cv.put(ScheduleContract.ScheduleEntry.ICON, icon)
        cv.put(ScheduleContract.ScheduleEntry.ROOM, rooms)
        cv.put(ScheduleContract.ScheduleEntry.LOAD, load)
        cv.put(ScheduleContract.ScheduleEntry.SCHEDULE_PROPERTY, property)
        cv.put(ScheduleContract.ScheduleEntry.START_TIME, starttime)
        cv.put(ScheduleContract.ScheduleEntry.SCHEDULE_TYPE, type)
        cv.put(ScheduleContract.ScheduleEntry.END_TIME, endtime)
        cv.put(ScheduleContract.ScheduleEntry.ROUTINE, routine)
        cv.put(ScheduleContract.ScheduleEntry.REMOTE, remote)
        cv.put(ScheduleContract.ScheduleEntry.HOME, home)
        try {
            db.beginTransaction()
            db.insert(ScheduleContract.ScheduleEntry.TABLE_NAME, null, cv)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            return false
        } finally {
            db.endTransaction()
        }

        return true
    }

    fun getAutomationScene(db: SQLiteDatabase, home: String): ArrayList<AutomationScene> {
        val params = arrayOf(home)
        val cursor = db.rawQuery(GET_AUTOMATION_SCENE_QUERY, params)
        val automationScene: ArrayList<AutomationScene> = ArrayList()
        val gson = Gson()
        while (cursor.moveToNext()) {
            val autmationDummy = AutomationScene()
            autmationDummy.name = cursor.getString(0)
            var room: ArrayList<RoomModel>
            var load: MutableList<RoomModel>
            var GeoDetails: MutableList<GeoModal>
            val room_type = object : TypeToken<List<String>>() {}.type
            val load_type = object : TypeToken<List<RoomModel>>() {}.type
            val geoDetail = object : TypeToken<List<GeoModal>>() {}.type
            autmationDummy.icon = Integer.valueOf(cursor.getString(1))
            val data2 = cursor.getString(2)
            room = gson.fromJson(data2, room_type)
            autmationDummy.room = room
            val data1 = cursor.getString(3)
            load = gson.fromJson(data1, load_type)
            autmationDummy.load = load
            val geoData = cursor.getString(4)
            GeoDetails = gson.fromJson(geoData, geoDetail)
            autmationDummy.property = GeoDetails
            autmationDummy.time = cursor.getString(5)
            autmationDummy.type = cursor.getString(6)
            autmationDummy.endTime = cursor.getString(7)
            autmationDummy.routine = cursor.getString(8)
            automationScene.add(autmationDummy)
        }
        cursor.close()
        return automationScene

    }

    fun getAutomationScheduleScene(db: SQLiteDatabase, scheduleCheck: String, home: String): AutomationScene {
        val params = arrayOf(scheduleCheck, home)
        val cursor = db.rawQuery(CHECK_AUTOMATION_QUERY, params)
        val automationScene = AutomationScene()
        var btn_list = ArrayList<RemoteIconModel>()
        val gson = Gson()
        while (cursor.moveToNext()) {
            automationScene.name = cursor.getString(0)
            var room: ArrayList<RoomModel>
            var load: MutableList<RoomModel>
            var GeoDetails: MutableList<GeoModal>
            val room_type = object : TypeToken<List<String>>() {}.type
            val load_type = object : TypeToken<List<RoomModel>>() {}.type
            val geoDetail = object : TypeToken<List<GeoModal>>() {}.type
            automationScene.icon = Integer.valueOf(cursor.getString(1))
            val data2 = cursor.getString(2)
            room = gson.fromJson(data2, room_type)
            automationScene.room = room
            val data1 = cursor.getString(3)
            load = gson.fromJson(data1, load_type)
            automationScene.load = load
            val geoData = cursor.getString(4)
            GeoDetails = gson.fromJson(geoData, geoDetail)
            automationScene.property = GeoDetails
            automationScene.time = cursor.getString(5)
            automationScene.type = cursor.getString(6)
            automationScene.endTime = cursor.getString(7)
            automationScene.routine = cursor.getString(8)
            val typeremote = object : TypeToken<ArrayList<RemoteIconModel>>() {}.type
            val data = cursor.getString(9)
            if (!data.isNullOrEmpty()) {
                btn_list = gson.fromJson(data, typeremote)
                automationScene.remote = btn_list
            }
        }
        cursor.close()
        return automationScene

    }

    fun getScheduleTable(db: SQLiteDatabase): ArrayList<AutomationScene> {
        val cursor = db.rawQuery(GET_ALL_AUTOMATION, null)
        val automationScene: ArrayList<AutomationScene> = ArrayList()
        var btn_list = ArrayList<RemoteIconModel>()
        val gson = Gson()
        while (cursor.moveToNext()) {
            val autmationDummy = AutomationScene()
            var room: ArrayList<RoomModel>
            var load: MutableList<RoomModel>
            var GeoDetails: MutableList<GeoModal>
            val room_type = object : TypeToken<List<String>>() {}.type
            val load_type = object : TypeToken<List<RoomModel>>() {}.type
            val geoDetail = object : TypeToken<List<GeoModal>>() {}.type
            autmationDummy.name = cursor.getString(0)
            autmationDummy.icon = Integer.valueOf(cursor.getString(1))
            val data2 = cursor.getString(2)
            room = gson.fromJson(data2, room_type)
            autmationDummy.room = room
            val data1 = cursor.getString(3)
            load = gson.fromJson(data1, load_type)
            autmationDummy.load = load
            val geoData = cursor.getString(4)
            GeoDetails = gson.fromJson(geoData, geoDetail)
            autmationDummy.property = GeoDetails
            autmationDummy.time = cursor.getString(5)
            autmationDummy.type = cursor.getString(6)
            autmationDummy.endTime = cursor.getString(7)
            autmationDummy.routine = cursor.getString(8)
            autmationDummy.home = cursor.getString(10)
            val typeremote = object : TypeToken<ArrayList<RemoteIconModel>>() {}.type
            val data = cursor.getString(9)
            if (!data.isNullOrEmpty()) {
                btn_list = gson.fromJson(data, typeremote)
                autmationDummy.remote = btn_list
            }
            automationScene.add(autmationDummy)
        }
        cursor.close()
        return automationScene

    }

    fun getAutomationSceneByName(db: SQLiteDatabase, name: String): AutomationScene {
        val params = arrayOf(name)
        val cursor = db.rawQuery(GET_AUTOMATION_SCENE_QUERY_NAME, params)
//        var automationScene : ArrayList<AutomationScene> = ArrayList()
        val gson = Gson()
        var btn_list = ArrayList<RemoteIconModel>()
        val autmationDummy = AutomationScene()
        while (cursor.moveToNext()) {
            autmationDummy.name = cursor.getString(0)
            var room: ArrayList<RoomModel>
            var load: MutableList<RoomModel>
            var GeoDetails: MutableList<GeoModal>
            val room_type = object : TypeToken<List<String>>() {}.type
            val load_type = object : TypeToken<List<RoomModel>>() {}.type
            val geoDetail = object : TypeToken<List<GeoModal>>() {}.type
            autmationDummy.icon = Integer.valueOf(cursor.getString(1))
            val data2 = cursor.getString(2)
            room = gson.fromJson(data2, room_type)
            autmationDummy.room = room
            val data1 = cursor.getString(3)
            load = gson.fromJson(data1, load_type)
            autmationDummy.load = load
            val geoData = cursor.getString(4)
            GeoDetails = gson.fromJson(geoData, geoDetail)
            autmationDummy.property = GeoDetails
            autmationDummy.time = cursor.getString(5)
            autmationDummy.type = cursor.getString(6)
            autmationDummy.endTime = cursor.getString(7)
            autmationDummy.routine = cursor.getString(8)
            val typeremote = object : TypeToken<ArrayList<RemoteIconModel>>() {}.type
            val data = cursor.getString(9)
            if (!data.isNullOrEmpty()) {
                btn_list = gson.fromJson(data, typeremote)
                autmationDummy.remote = btn_list
            }

            //automationScene.add(autmationDummy)
        }
        cursor.close()
        return autmationDummy

    }

    fun checkAutomationExist(db: SQLiteDatabase, name: String, home: String): Boolean {
        var automationSceneExist = false
        val paramcheck = arrayOf<String>(name, home)
        val cursorcheck = db.rawQuery(AUTOMATION_EXIST_QUERY, paramcheck)
        while (cursorcheck.moveToNext()) {
            automationSceneExist = true
        }
        if (automationSceneExist) {
            cursorcheck.close()
            return false
        }
        return true
    }

    fun deleteAutomationScene(db: SQLiteDatabase, automationName: String, home: String) {
        val params = arrayOf(automationName, home)
        db.delete(ScheduleContract.ScheduleEntry.TABLE_NAME, DELETE_AUTOMATION_SCENE, params)
    }

    fun deleteHomeSchedules(db: SQLiteDatabase, homeName: String) {
        val params = arrayOf(homeName)
        db.delete(ScheduleContract.ScheduleEntry.TABLE_NAME, DELETE_HOME_SCENES, params)
    }

    fun deleteTable(db: SQLiteDatabase) {
        db.delete(ScheduleContract.ScheduleEntry.TABLE_NAME, null, null)
    }
}