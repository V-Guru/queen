package com.wozart.aura.data.sqlLite

import android.content.ContentValues
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import aura.wozart.com.aura.entity.amazonaws.models.nosql.DevicesTableDO
import com.wozart.aura.utilities.Constant.Companion.CHECK_ADD_QUERY
import com.wozart.aura.utilities.Constant.Companion.DELETE_DEVICE_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_ALL_FAV_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_ALL_LOADS
import com.wozart.aura.utilities.Constant.Companion.GET_DEVICES_FOR_THING_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_DEVICES_IN_ROOM_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_LOADS_JSON
import com.wozart.aura.utilities.Constant.Companion.GET_ROOMS_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_ROOM_FOR_DEVICE_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_THING_FOR_DEVICES_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_THING_NAME_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_UIUD_QUERY
import com.wozart.aura.utilities.Constant.Companion.INSERT_ROOMS_QUERY
import com.wozart.aura.utilities.Constant.Companion.UPDATE_LOAD
import com.google.gson.Gson
import com.wozart.aura.data.model.Room
import com.wozart.aura.entity.sql.device.DeviceContract
import java.util.ArrayList
import com.google.gson.reflect.TypeToken
import com.wozart.aura.data.model.DeviceUiud
import com.wozart.aura.entity.model.aura.*
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.ui.dashboard.room.RoomModel
import com.wozart.aura.utilities.Constant.Companion.DELETE_HOME_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_ALL_HOME
import com.wozart.aura.utilities.Constant.Companion.GET_ALL_LOADS_HOME
import com.wozart.aura.utilities.Constant.Companion.GET_ALL_LOADS_SCENES
import com.wozart.aura.utilities.Constant.Companion.GET_DEVICES_IN_HOME_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_DEVICE_TABLE
import com.wozart.aura.utilities.Constant.Companion.GET_LOAD
import com.wozart.aura.utilities.Constant.Companion.GET_ROOMS_QUERY_NEW
import com.wozart.aura.utilities.Constant.Companion.GET_UIUD_QUERY_ALL
import com.wozart.aura.utilities.Constant.Companion.UPDATE_ROOM_DETAILS


/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 08/05/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class DeviceTable {
    val TAG = "DEVICE_TABLE"
    fun insertDevice(db: SQLiteDatabase, home: String, room: String, uiud: String, name: String, loads: String, thing: String) {
        val access = "master"
        val params = arrayOf<String>(name)
        val cursor = db.rawQuery(CHECK_ADD_QUERY, params)
        if (cursor.count != 0) {
            db.delete(DeviceContract.DeviceEntry.TABLE_NAME, DELETE_DEVICE_QUERY, params)
        }
        cursor.close()
        val cv = ContentValues()
        cv.put(DeviceContract.DeviceEntry.ROOM, room)
        cv.put(DeviceContract.DeviceEntry.HOME, home)
        cv.put(DeviceContract.DeviceEntry.DEVICE, name)
        cv.put(DeviceContract.DeviceEntry.UIUD, uiud)
        cv.put(DeviceContract.DeviceEntry.ACCESS, access)
        cv.put(DeviceContract.DeviceEntry.LOAD, loads)
        cv.put(DeviceContract.DeviceEntry.THING, thing)

        try {
            db.beginTransaction()
            db.insert(DeviceContract.DeviceEntry.TABLE_NAME, null, cv)

            db.setTransactionSuccessful()
            Log.d("DEVICE_INSERTION","Device is inserted successfully!!")
        } catch (e: SQLException) {
            Log.d("DEVICE_INSERTION","Some error came while inserting device!!")
            //error("Error : $e")
        } finally {
            db.endTransaction()
        }
        return
    }


    fun insertDeviceFromAws(db: SQLiteDatabase, devices: ArrayList<DevicesTableDO>, masterName: String?) {
        Log.d(TAG,"insertDeviceFromAws")
        if (devices.size == 0) return
        for (device in devices) {
            val params = arrayOf<String>(device.name!!)
            val cursor = db.rawQuery(CHECK_ADD_QUERY, params)
            if (cursor.count != 0) {
                continue
            }
            cursor.close()
            val value = ContentValues()
            val room = device.room!!.split("?")
            val home = device.home!!.split("?")
            value.put(DeviceContract.DeviceEntry.HOME, home[0] + "($masterName)")
            value.put(DeviceContract.DeviceEntry.ROOM, room[0])
            value.put(DeviceContract.DeviceEntry.THING, device.thing)
            value.put(DeviceContract.DeviceEntry.DEVICE, device.name)
            value.put(DeviceContract.DeviceEntry.ACCESS, device.master)
            value.put(DeviceContract.DeviceEntry.UIUD, device.uiud)
            val loads: ArrayList<AuraSwitchLoad> = ArrayList()
//            for (i in 0..3) {
//                val load = AuraSwitchLoad()
//                val loadName = device.loads!![i].split("?")
//                val name = loadName[0]
//                val vars = loadName[1].toInt()
//                val fav = vars / 100
//                val dim = (vars % 100) / 10
//                val icon = (vars % 100) % 10
//                load.favourite = fav != 0
//                load.dimmable = dim != 0
//                load.name = name
//                load.index = i
//                load.icon = icon
//                loads.add(load)
//            }
            val gson = Gson()
            value.put(DeviceContract.DeviceEntry.LOAD, gson.toJson(loads))

            try {
                db.beginTransaction()
                db.insert(DeviceContract.DeviceEntry.TABLE_NAME, null, value)

                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                error("Error : $e")
            } finally {
                db.endTransaction()
            }
        }
    }


    fun insertHome(db: SQLiteDatabase, home: String) {
        Log.d(TAG,"insertHome")
        val value = ContentValues()
        value.put(DeviceContract.DeviceEntry.HOME, home)
        try {
            db.beginTransaction()
            db.insert(DeviceContract.DeviceEntry.TABLE_NAME, null, value)

            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            error("Error : $e")
        } finally {
            db.endTransaction()
        }
    }

    fun insertRoom(db: SQLiteDatabase, home: String, room: String) {
        Log.d(TAG,"insertRoom")
        val cursor = db.rawQuery(INSERT_ROOMS_QUERY, null)
        if (cursor.count == 0) {
            val value = ContentValues()
            value.put(DeviceContract.DeviceEntry.HOME, home)
            value.put(DeviceContract.DeviceEntry.ROOM, room)

            try {
                db.beginTransaction()
                db.insert(DeviceContract.DeviceEntry.TABLE_NAME, null, value)

                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                error("Error : $e")
            } finally {
                db.endTransaction()
                cursor.close()
            }
        }
    }

    fun getDevice(db: SQLiteDatabase, device: String): AuraSwitch {
        Log.d(TAG,"getDevice")
        val params = arrayOf(device)
        val cursor = db.rawQuery(GET_LOAD, params)
        val auraDevice = AuraSwitch()
        val gson = Gson()
        while (cursor.moveToNext()) {
            auraDevice.name = cursor.getString(0)
            var loads: MutableList<AuraSwitchLoad>
            val type = object : TypeToken<List<AuraSwitchLoad>>() {}.type
            loads = gson.fromJson(cursor.getString(1), type)
            auraDevice.loads = loads
            auraDevice.thing = cursor.getString(2)
            auraDevice.room =  cursor.getString(3)
            auraDevice.uiud = cursor.getString(4)
        }
        cursor.close()
        Log.d(TAG,"After close getDevice")
        return auraDevice
    }

    fun getAllDevices(db: SQLiteDatabase): ArrayList<String> {
        Log.d(TAG,"getAllDevices")
        val cursor = db.rawQuery(GET_ALL_LOADS, null)
        val devices: ArrayList<String> = ArrayList()
        while (cursor.moveToNext()) {
            val device = cursor.getString(0)
            devices.add(device)
        }
        cursor.close()
        return devices
    }


    fun getAllDevicesCount(db: SQLiteDatabase, home: String): Int {
        Log.d(TAG,"getAllDevicesCount")
        val params = arrayOf(home)
        val cursor = db.rawQuery(GET_ALL_LOADS_HOME, params)
        var deviceCount = 0
        while (cursor.moveToNext()) {
            if(cursor.getString(0) != null){
                deviceCount++
            }
        }

        cursor.close()
        return (deviceCount * 4)
    }

    fun getRoomForDevice(db: SQLiteDatabase, device: String): String {
        Log.d(TAG,"getRoomForDevice")
        val params = arrayOf(device)
        val cursor = db.rawQuery(GET_ROOM_FOR_DEVICE_QUERY, params)
        var room: String? = null
        while (cursor.moveToNext()) {
            room = cursor.getString(0)
        }
        cursor.close()
        return room!!
    }

    fun getHome(db: SQLiteDatabase): ArrayList<String> {
        Log.d(TAG,"getHome")
        val cursor = db.rawQuery(GET_ALL_HOME, null)
        val homes = ArrayList<String>()
        while (cursor.moveToNext()) {
            homes.add(cursor.getString(0))
        }
        cursor.close()
        return homes
    }

    fun getRooms(db: SQLiteDatabase, home: String): ArrayList<Room> {
        Log.d(TAG,"getRooms")
        val params = arrayOf(home)
        val cursor = db.rawQuery(GET_ROOMS_QUERY, params)
        val room = ArrayList<Room>()
        while (cursor.moveToNext()) {
            val x = Room()
            x.roomName = cursor.getString(0)
            room.add(x)
        }
        cursor.close()
        return room
    }

    fun getRoomNews(db: SQLiteDatabase, home: String): ArrayList<RoomModel> {
        Log.d(TAG,"getRoomNews")
        val params = arrayOf(home)
        val cursor = db.rawQuery(GET_ROOMS_QUERY_NEW, params)
        val room = ArrayList<RoomModel>()
        while (cursor.moveToNext()) {
            val x = RoomModel()
            if(cursor.getString(1) != null){
                x.name_room = cursor.getString(0)
                room.add(x)
            }

        }
        cursor.close()
        return room
    }

    fun getDevicesForRoom(db: SQLiteDatabase, home: String, room: String): ArrayList<AuraSwitch> {
        Log.d(TAG,"getDevicesForRoom")
        val devices = ArrayList<AuraSwitch>()
        val params = arrayOf(home, room)
        val gson = Gson()
        val cursor = db.rawQuery(GET_DEVICES_IN_ROOM_QUERY, params)
        while (cursor.moveToNext()) {
            val device = AuraSwitch()
            var loads: MutableList<AuraSwitchLoad>

            val type = object : TypeToken<List<AuraSwitchLoad>>() {}.type
            if(cursor.getString(1) != null){
                loads = gson.fromJson(cursor.getString(1), type)
                device.name = cursor.getString(0)
                device.loads = loads
                device.thing = cursor.getString(2)
                devices.add(device)
            }

        }
        cursor.close()
        return devices
    }

    fun getDevicesForHome(db: SQLiteDatabase, home: String): ArrayList<AuraSwitch> {
        Log.d(TAG,"getDevicesForHome")
        val devices = ArrayList<AuraSwitch>()
        val params = arrayOf(home)
        val gson = Gson()
        val cursor = db.rawQuery(GET_DEVICES_IN_HOME_QUERY, params)
        while (cursor.moveToNext()) {
            val device = AuraSwitch()
            var loads: MutableList<AuraSwitchLoad>

            val type = object : TypeToken<List<AuraSwitchLoad>>() {}.type
            if(cursor.getString(1) != null){
                loads = gson.fromJson(cursor.getString(1), type)
                device.name = cursor.getString(0)
                device.loads = loads
                device.thing = cursor.getString(2)
                device.uiud = cursor.getString(3)
                devices.add(device)
            }

        }
        cursor.close()
        return devices
    }

    fun getAllDevicesScenes(db: SQLiteDatabase, home: String): ArrayList<AuraComplete> {
        val params = arrayOf(home)
        val cursor = db.rawQuery(GET_ALL_LOADS_SCENES, params)
        val gson = Gson()
        val devices = ArrayList<AuraComplete>()
        while (cursor.moveToNext()) {
            val device = AuraComplete()
            var loads: MutableList<AuraLoad>
            val type = object : TypeToken<List<AuraLoad>>() {}.type
            if(cursor.getString(1) != null){
                loads = gson.fromJson(cursor.getString(1), type)
                device.name = cursor.getString(0)
                device.loads = loads
                device.room = cursor.getString(2)
                devices.add(device)
            }
        }
        cursor.close()
        return devices
    }


    fun getDeviceTable(db: SQLiteDatabase): ArrayList<DeviceTableModel> {
        val cursor = db.rawQuery(GET_DEVICE_TABLE, null)
        val gson = Gson()
        val devices = ArrayList<DeviceTableModel>()
        while (cursor.moveToNext()) {
            val device = DeviceTableModel()
            var loads: MutableList<AuraLoad>
            val type = object : TypeToken<List<AuraLoad>>() {}.type
            if(cursor.getString(6) != null){
                val data = cursor.getString(0)
                loads = gson.fromJson(data, type)  //0-LOAD
                device.home  = cursor.getString(1)                 //1-HOME
                device.room  = cursor.getString(2)                 //2-ROOM
                device.thing = cursor.getString(3)                //3-THING
                device.uiud  = cursor.getString(4)                //4-UIUD
                device.access = cursor.getString(5)             //5-ACCESS
                device.name = cursor.getString(6)                 //6 - NAME_DEVICE
                device.loads = loads
                devices.add(device)
            }
        }
        cursor.close()
        return devices
    }



    fun getThing(db: SQLiteDatabase): ArrayList<String> {
        Log.d(TAG,"getThing")
        val devices = ArrayList<String>()
        val cursor = db.rawQuery(GET_THING_NAME_QUERY, null)
        while (cursor.moveToNext()) {
            if (cursor.getString(0) != null)
                devices.add(cursor.getString(0))
        }
        cursor.close()
        return devices
    }

    fun getThingForDevice(db: SQLiteDatabase, device: String): String {
        var thing: String = ""
        val params = arrayOf(device)
        val cursor = db.rawQuery(GET_THING_FOR_DEVICES_QUERY, params)
        while (cursor.moveToNext()) {
            if (cursor.getString(0) != null)
                thing = cursor.getString(0)
        }
        cursor.close()
        return thing
    }

    fun getDeviceForThing(db: SQLiteDatabase, thing: String): String {
        var devices: String = ""
        val params = arrayOf(thing)
        val cursor = db.rawQuery(GET_DEVICES_FOR_THING_QUERY, params)
        while (cursor.moveToNext()) {
            if (cursor.getString(0) != null)
                devices = cursor.getString(0)
        }
        cursor.close()
        return devices
    }

    fun getUiud(db: SQLiteDatabase, device: String): String? {
        val params = arrayOf(device)
        val cursor = db.rawQuery(GET_UIUD_QUERY, params)
        var uiud: String? = null
        while (cursor.moveToNext()) {
            uiud = cursor.getString(0)
        }
        cursor.close()
        return uiud
    }


    fun getLoadForEditActivity(db: SQLiteDatabase, device: String, index: Int): AuraSwitchLoad {
        Log.d(TAG,"getLoadForEditActivity")
        val params = arrayOf(device)
        val gson = Gson()
        val cursor = db.rawQuery(GET_LOADS_JSON, params)
        var loads: MutableList<AuraSwitchLoad> = ArrayList()
        val type = object : TypeToken<List<AuraSwitchLoad>>() {}.type
        while (cursor.moveToNext()) {
            loads = gson.fromJson(cursor.getString(0), type)
        }
        cursor.close()
        return loads[index]
    }


    fun updateLoad(db: SQLiteDatabase, device: String, load: AuraSwitchLoad) {
        Log.d(TAG,"updateLoad")
        val params = arrayOf(device)
        val gson = Gson()
        val cursor = db.rawQuery(GET_LOADS_JSON, params)
        val type = object : TypeToken<ArrayList<AuraSwitchLoad>>() {}.type
        var loads: MutableList<AuraSwitchLoad> = ArrayList()
        while (cursor.moveToNext()) {
            loads = gson.fromJson(cursor.getString(0), type)
        }
        loads[load.index!!].name =load.name
        loads[load.index!!].icon = load.icon
        loads[load.index!!].dimmable = load.dimmable
        loads[load.index!!].favourite = load.favourite
        loads[load.index!!].module = load.module
        loads[load.index!!].isAdaptive = load.isAdaptive
        cursor.close()
        val cv = ContentValues()
        cv.put(DeviceContract.DeviceEntry.LOAD, gson.toJson(loads))
        db.update(DeviceContract.DeviceEntry.TABLE_NAME, cv, UPDATE_LOAD, arrayOf(device))
    }

    fun updateRoom(db: SQLiteDatabase,roomOldName: String, roomNewName: String) {
        Log.d(TAG,"updateRoom")
        val cv = ContentValues()
        cv.put(DeviceContract.DeviceEntry.ROOM, roomNewName)
        db.update(DeviceContract.DeviceEntry.TABLE_NAME, cv, UPDATE_ROOM_DETAILS, arrayOf(roomOldName))
    }

    fun deleteDevice(db: SQLiteDatabase, device: String) {
        Log.d(TAG,"deleteDevice")
        val params = arrayOf(device)
        db.delete(DeviceContract.DeviceEntry.TABLE_NAME, DELETE_DEVICE_QUERY, params)
    }
    fun deleteHome(db: SQLiteDatabase, home: String) {
        Log.d(TAG,"deleteHome")
        val params = arrayOf(home)
        db.delete(DeviceContract.DeviceEntry.TABLE_NAME, DELETE_HOME_QUERY, params)
    }
    fun deleteTable(db: SQLiteDatabase) {
        Log.d(TAG,"deleteTable")
        db.delete("device", null, null)
    }

//    fun deleteLoadScene(db: SQLiteDatabase, device: String, sceneName: String) {
//        Log.d(TAG,"deleteLoadScene")
//        val params = arrayOf(device)
//        val gson = Gson()
//        val cursor = db.rawQuery(GET_LOADS_JSON, params)
//        var loads: MutableList<AuraSwitchLoad> = ArrayList()
//        val type = object : TypeToken<List<AuraSwitchLoad>>() {}.type
//        while (cursor.moveToNext()) {
//            loads = gson.fromJson(cursor.getString(0), type)
//            for(i in 0..3){
//                    var sceneFlag = false
//                    for(scene in loads[i].scene){
//                        if(scene == sceneName){
//                            sceneFlag = true
//                        }
//                    }
//                    if(sceneFlag){
//                        loads[i].scene.remove(sceneName)
//                    }
//            }
//        }
//        cursor.close()
//        val cv = ContentValues()
//        cv.put(DeviceContract.DeviceEntry.LOAD, gson.toJson(loads))
//        db.update(DeviceContract.DeviceEntry.TABLE_NAME, cv, UPDATE_LOAD, arrayOf(device))
//    }

/*Favourite Queries*/

    fun getFavourite(db: SQLiteDatabase, home: String): MutableList<AuraSwitch> {
        Log.d(TAG,"getFavourite")
        val params = arrayOf(home)
        val cursor = db.rawQuery(GET_ALL_FAV_QUERY, params)
        val gson = Gson()
        val favouriteLoads: MutableList<AuraSwitch> = ArrayList()
        while (cursor.moveToNext()) {
            val device = AuraSwitch()
            var loads: MutableList<AuraSwitchLoad>
            val type = object : TypeToken<List<AuraSwitchLoad>>() {}.type
            if (cursor.getString(0) != null) {
                loads = gson.fromJson(cursor.getString(0), type)
                device.name = cursor.getString(1)
                device.loads = loads
                favouriteLoads.add(device)
            }
        }
        cursor.close()
        return favouriteLoads
    }
}