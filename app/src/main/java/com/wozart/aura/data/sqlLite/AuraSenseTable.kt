package com.wozart.aura.data.sqlLite

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.entity.sql.sense.AuraSenseContract
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.auraSense.RemoteListModel
import com.wozart.aura.utilities.Constant.Companion.CHECK_REMOTE_FOR_DEVICE
import com.wozart.aura.utilities.Constant.Companion.DELETE_REMOTE
import com.wozart.aura.utilities.Constant.Companion.DELETE_REMOTE_SENSE
import com.wozart.aura.utilities.Constant.Companion.DELETE_SENSE_TABLE
import com.wozart.aura.utilities.Constant.Companion.GET_FAVOURITE_REMOTE
import com.wozart.aura.utilities.Constant.Companion.GET_REMOTE_LIST
import java.lang.Exception

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-02-10
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class AuraSenseTable {

    fun insertRemote(db: SQLiteDatabase,
                     sense_device: String,
                     remote_name: String,
                     remote_brand: String,
                     remote_model: String,
                     remote_button: String,
                     remote_fav: String,
                     type: String,
                     location: String,
                     homeName: String): Boolean {
        val param = arrayOf(remote_name, location)
//        var remoteExist = false
        val cursor = db.rawQuery(CHECK_REMOTE_FOR_DEVICE, param)
        if (cursor.count != 0) {
            db.delete(AuraSenseContract.AuraSenseEntry.TABLE_NAME, DELETE_SENSE_TABLE, param)
        }
        cursor.close()
        val cnv = ContentValues()
        cnv.put(AuraSenseContract.AuraSenseEntry.DEVICE_NAME, sense_device)
        cnv.put(AuraSenseContract.AuraSenseEntry.REMOTE_NAME, remote_name)
        cnv.put(AuraSenseContract.AuraSenseEntry.REMOTE_BRAND, remote_brand)
        cnv.put(AuraSenseContract.AuraSenseEntry.REMOTE_MODEL, remote_model)
        cnv.put(AuraSenseContract.AuraSenseEntry.BUTTON_ICON, remote_button)
        cnv.put(AuraSenseContract.AuraSenseEntry.REMOTE_FOVOURITE, remote_fav)
        cnv.put(AuraSenseContract.AuraSenseEntry.APPLIANCE_TYPE, type)
        cnv.put(AuraSenseContract.AuraSenseEntry.REMOTE_LOCATION, location)
        cnv.put(AuraSenseContract.AuraSenseEntry.HOME_ASSOCIATE,homeName)
        try {
            db.beginTransaction()
            db.insert(AuraSenseContract.AuraSenseEntry.TABLE_NAME, null, cnv)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.d("AURA_SENSE_TABLE", "Error in inserting")
        } finally {
            db.endTransaction()
        }
        return true
    }

    fun getRemoteList(db: SQLiteDatabase, device_name: String): MutableList<RemoteListModel> {
        val param = arrayOf(device_name)
        val cursor = db.rawQuery(GET_REMOTE_LIST, param)
        val list_remote: MutableList<RemoteListModel> = ArrayList()
        var btn_list = ArrayList<RemoteIconModel>()
        var favChanList: MutableList<RemoteIconModel> = arrayListOf()
        val gson = Gson()
        val type = object : TypeToken<ArrayList<RemoteIconModel>>() {}.type
        val type_favChan = object : TypeToken<ArrayList<RemoteIconModel>>() {}.type
        while (cursor.moveToNext()) {
            val remote = RemoteListModel()
            remote.brandName = cursor.getString(0)
            remote.remoteName = cursor.getString(1)
            remote.modelNumber = cursor.getString(2)
            remote.remoteLocation = cursor.getString(3)
            if (!cursor.getString(4).isNullOrEmpty()) {
                btn_list = gson.fromJson(cursor.getString(4), type)
                remote.dynamicRemoteIconList = btn_list
            }
            if (!cursor.getString(5).isNullOrEmpty()) {
                favChanList = gson.fromJson(cursor.getString(5), type_favChan)
                remote.favChannelList = favChanList
            }
            remote.auraSenseName = cursor.getString(6)
            remote.typeAppliances = cursor.getString(7)
            list_remote.add(remote)
        }
        cursor.close()
        return list_remote
    }


    fun getFavouriteRemote(db: SQLiteDatabase, location: String): MutableList<RemoteIconModel> {
        val param = arrayOf(location)
        val cursor = db.rawQuery(GET_FAVOURITE_REMOTE, param)
        var favRemoteList = ArrayList<RemoteIconModel>()
        val listButtonFav: MutableList<RemoteIconModel> = ArrayList()
        var btn_list = ArrayList<RemoteIconModel>()
        val type = object : TypeToken<ArrayList<RemoteIconModel>>() {}.type
        val gson = Gson()
        while (cursor.moveToNext()) {
            if (!cursor.getString(4).isNullOrEmpty()) {
                btn_list = gson.fromJson(cursor.getString(4), type)
                favRemoteList = ArrayList(btn_list.filter { it.btnFavourite })
                listButtonFav.addAll(favRemoteList)
            }
        }
        cursor.close()
        return listButtonFav
    }

    fun deleteRemote(db: SQLiteDatabase, remote_name: String, deviceName: String) {
        val param = arrayOf(remote_name, deviceName)
        db.delete(AuraSenseContract.AuraSenseEntry.TABLE_NAME, DELETE_REMOTE, param)
    }

    fun deleteRemoteForSense(db: SQLiteDatabase, remote_name: String) {
        val param = arrayOf(remote_name)
        db.delete(AuraSenseContract.AuraSenseEntry.TABLE_NAME, DELETE_REMOTE_SENSE, param)
    }

    fun deleteTable(db: SQLiteDatabase) {
        db.delete(AuraSenseContract.AuraSenseEntry.TABLE_NAME, null, null)
    }

}