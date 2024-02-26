package com.wozart.aura.data.sqlLite

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.data.model.ThingError
import com.wozart.aura.entity.sql.utils.UtilsContract
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.Constant.Companion.GET_KEY_VALUE_QUERY
import com.wozart.aura.utilities.Constant.Companion.UPDATE_KEY_VALUE_QUERY
import java.sql.SQLException

class UtilsTable {

    fun insertIpList(db: SQLiteDatabase,key:String,data:IpModel) {

        val params = arrayOf<String>(key)
        val gson = Gson()
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY, params)
        var ipList: MutableList<IpModel> = ArrayList()
        if(cursor.count == 0){
            cursor.close()
            ipList.add(data)
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE,gson.toJson(ipList))
            try {
                db.beginTransaction()
                db.insert(UtilsContract.UtilsEntry.TABLE_NAME, null, cv)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                error("Error : $e")
            } finally {
                db.endTransaction()
            }
        }else{
            val type = object : TypeToken<List<IpModel>>() {}.type
            while (cursor.moveToNext()) {
                val temp = cursor.getString(0)
                ipList = gson.fromJson(temp, type)
            }
            cursor.close()
            ipList.add(data)
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE, gson.toJson(ipList))
            db.update(UtilsContract.UtilsEntry.TABLE_NAME, cv, UPDATE_KEY_VALUE_QUERY, arrayOf(key))
        }
    }
    fun replaceIpList(db: SQLiteDatabase,key:String,data:MutableList<IpModel>) {

        val params = arrayOf<String>(key)
        val gson = Gson()
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY, params)
        var ipList: MutableList<IpModel> = ArrayList()
        if(cursor.count == 0){
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE,gson.toJson(data))
            try {
                db.beginTransaction()
                db.insert(UtilsContract.UtilsEntry.TABLE_NAME, null, cv)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                error("Error : $e")
            } finally {
                db.endTransaction()
            }
        }else{
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE, gson.toJson(data))
            db.update(UtilsContract.UtilsEntry.TABLE_NAME, cv, UPDATE_KEY_VALUE_QUERY, arrayOf(key))
        }
    }

    fun getIpList(db: SQLiteDatabase,key:String): MutableList<IpModel> {

        val params = arrayOf<String>(key)
        val gson = Gson()
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY, params)


        var ipList: MutableList<IpModel> = ArrayList()
        val type = object : TypeToken<List<IpModel>>() {}.type
        while (cursor.moveToNext()) {
            val data = cursor.getString(0)
            ipList = gson.fromJson(data, type)
        }
        cursor.close()
        return  ipList
    }

    fun replaceRemoteData(db: SQLiteDatabase,key: String,data:MutableList<RemoteModel>) {
        val params = arrayOf(key)
        val gson = Gson()
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY,params)
        if(cursor.count == 0){
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE,gson.toJson(data))
            try {
                db.beginTransaction()
                db.insert(UtilsContract.UtilsEntry.TABLE_NAME, null, cv)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                error("Error : $e")
            } finally {
                db.endTransaction()
            }
        }else{
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE, gson.toJson(data))
            db.update(UtilsContract.UtilsEntry.TABLE_NAME, cv, UPDATE_KEY_VALUE_QUERY, arrayOf(key))
        }
    }

    fun getRemoteData(db: SQLiteDatabase,key: String): MutableList<RemoteModel> {
        val params = arrayOf<String>(key)
        val gson = Gson()
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY, params)
        var romteData : MutableList<RemoteModel> = ArrayList()
        val type = object : TypeToken<List<RemoteModel>>() {}.type
        while (cursor.moveToNext()) {
            val data = cursor.getString(0)
            romteData = gson.fromJson(data, type)
        }
        cursor.close()
        return  romteData
    }

    fun replaceButtonData(db: SQLiteDatabase,key: String,data:MutableList<ButtonModel>) {
        val params = arrayOf(key)
        val gson = Gson()
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY,params)
        if(cursor.count == 0){
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE,gson.toJson(data))
            try {
                db.beginTransaction()
                db.insert(UtilsContract.UtilsEntry.TABLE_NAME, null, cv)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                error("Error : $e")
            } finally {
                db.endTransaction()
            }
        }else{
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE, gson.toJson(data))
            db.update(UtilsContract.UtilsEntry.TABLE_NAME, cv, UPDATE_KEY_VALUE_QUERY, arrayOf(key))
        }
    }

    fun getButtonData(db: SQLiteDatabase,key: String): MutableList<ButtonModel> {
        val params = arrayOf<String>(key)
        val gson = Gson()
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY, params)
        var buttonDevice : MutableList<ButtonModel> = ArrayList()
        val type = object : TypeToken<List<ButtonModel>>() {}.type
        while (cursor.moveToNext()) {
            val data = cursor.getString(0)
            buttonDevice = gson.fromJson(data, type)
        }
        cursor.close()
        return  buttonDevice
    }

    fun getHomeData(db: SQLiteDatabase, key: String): MutableList<RoomModelJson> {
        val params = arrayOf<String>(key)
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY, params)
        val gson = Gson()

        var homeList: MutableList<RoomModelJson> = ArrayList()
        val type = object : TypeToken<List<RoomModelJson>>() {}.type
        while (cursor.moveToNext()) {
            val data = cursor.getString(0)
            homeList = gson.fromJson(data, type)
        }
        cursor.close()
        return homeList
    }

    fun replaceHome(db: SQLiteDatabase, key: String, homeData: MutableList<RoomModelJson>) {
        val params = arrayOf<String>(key)
        val gson = Gson()
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY, params)
        if (cursor.count == 0) {
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE, gson.toJson(homeData))
            try {
                db.beginTransaction()
                db.insert(UtilsContract.UtilsEntry.TABLE_NAME, null, cv)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                error("Error : $e")
            } finally {
                db.endTransaction()
            }
        } else {
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE, gson.toJson(homeData))
            db.update(UtilsContract.UtilsEntry.TABLE_NAME, cv, UPDATE_KEY_VALUE_QUERY, arrayOf(key))
        }

    }
    fun replaceThing(db: SQLiteDatabase, key: String, thing: ThingError) {
        val params = arrayOf<String>(key)
        val gson = Gson()
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY, params)
        if (cursor.count == 0) {
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE, gson.toJson(thing))
            try {
                db.beginTransaction()
                db.insert(UtilsContract.UtilsEntry.TABLE_NAME, null, cv)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                error("Error : $e")
            } finally {
                db.endTransaction()
            }
        } else {
            cursor.close()
            val cv = ContentValues()
            cv.put(UtilsContract.UtilsEntry.KEY, key)
            cv.put(UtilsContract.UtilsEntry.VALUE, gson.toJson(thing))
            db.update(UtilsContract.UtilsEntry.TABLE_NAME, cv, UPDATE_KEY_VALUE_QUERY, arrayOf(key))
        }

    }

    fun getThingData(db: SQLiteDatabase, key: String): ThingError {
        val params = arrayOf<String>(key)
        val cursor = db.rawQuery(GET_KEY_VALUE_QUERY, params)
        val gson = Gson()

        var thing = ThingError()
        val type = object : TypeToken<ThingError>() {}.type
        while (cursor.moveToNext()) {
            val data = cursor.getString(0)
            thing = gson.fromJson(data, type)
        }
        cursor.close()
        return thing
    }
    fun deleteUtilstTable(db: SQLiteDatabase){
        db.delete(UtilsContract.UtilsEntry.TABLE_NAME,null,null)

    }


}