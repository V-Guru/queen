package com.wozart.aura.data.sqlLite

import android.content.ContentValues
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.wozart.aura.utilities.Constant.Companion.CHECK_SCENE_QUERY
import com.wozart.aura.utilities.Constant.Companion.DELETE_SCENE
import com.wozart.aura.utilities.Constant.Companion.GET_SCENE_QUERY
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.entity.sql.scenes.SceneContract
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.Constant.Companion.DELETE_HOME_SCENE_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_ALL_SCENES
import com.wozart.aura.utilities.Constant.Companion.GET_SCENE_FOR_ROOM_QUERY
import com.wozart.aura.utilities.Constant.Companion.GET_SELECTED_SCENE
import com.wozart.aura.utilities.Constant.Companion.UPDATE_ROOM_DETAILS_SCENE
import com.wozart.aura.utilities.Constant.Companion.UPDATE_SCENE_PARAMS


/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 08/06/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class SceneTable {

    fun insertScene(db: SQLiteDatabase, name: String, loads: String, rooms: String, home: String, icon: Int, oldScene: String, sceneType: String, remoteData: String): Boolean {


        if (sceneType == "create") {

            var sceneExists = false
            val paramsCheck = arrayOf<String>(name, home)
            val cursorCheck = db.rawQuery(GET_SELECTED_SCENE, paramsCheck)
            while (cursorCheck.moveToNext()) {
                sceneExists = true
            }
            if (sceneExists) {
                cursorCheck.close()
                return false
            }

            val cv = ContentValues()
            cv.put(SceneContract.SceneEntry.HOME, home)
            cv.put(SceneContract.SceneEntry.LOAD, loads)
            cv.put(SceneContract.SceneEntry.SCENE_ID, name)
            cv.put(SceneContract.SceneEntry.ROOM, rooms)
            cv.put(SceneContract.SceneEntry.ICON, icon)
            cv.put(SceneContract.SceneEntry.REMOTE, remoteData)
            try {
                db.beginTransaction()
                db.insert(SceneContract.SceneEntry.TABLE_NAME, null, cv)
                db.setTransactionSuccessful()
            } catch (e: SQLException) {
                Log.d("ERROR_SQL_SCENE", "Error : $e")
            } finally {
                db.endTransaction()
            }
            return true
        } else {

            if (name != oldScene) {
                var sceneExists = false
                val paramsCheck = arrayOf<String>(name, home)
                val cursorCheck = db.rawQuery(GET_SELECTED_SCENE, paramsCheck)
                while (cursorCheck.moveToNext()) {
                    sceneExists = true
                }
                if (sceneExists) {
                    cursorCheck.close()
                    return false
                }
            }
            val params = arrayOf<String>(oldScene)
            val cursor = db.rawQuery(CHECK_SCENE_QUERY, params)
            val gson = Gson()
            if (cursor.count != 0) {
                val cnv = ContentValues()
                cnv.put(SceneContract.SceneEntry.ICON, icon)
                cnv.put(SceneContract.SceneEntry.LOAD, loads)
                cnv.put(SceneContract.SceneEntry.ROOM, rooms)
                cnv.put(SceneContract.SceneEntry.SCENE_ID, name)
                cnv.put(SceneContract.SceneEntry.REMOTE, remoteData)
                db.update(SceneContract.SceneEntry.TABLE_NAME, cnv, UPDATE_SCENE_PARAMS, arrayOf(oldScene))
                cursor.close()
                return true
            }
        }
        return true

    }

    fun updateScene(db: SQLiteDatabase, name: String, loads: String, rooms: String, home: String, icon: Int, oldScene: String): Boolean {
        var sceneExists = false
        val paramsCheck = arrayOf<String>(oldScene, home)
        val cursorCheck = db.rawQuery(GET_SELECTED_SCENE, paramsCheck)
        if (cursorCheck.count != 0) {
            val params = arrayOf(name, home)
            db.delete(SceneContract.SceneEntry.TABLE_NAME, DELETE_SCENE, params)
        }
        cursorCheck.close()

        val cv = ContentValues()
        cv.put(SceneContract.SceneEntry.HOME, home)
        cv.put(SceneContract.SceneEntry.LOAD, loads)
        cv.put(SceneContract.SceneEntry.SCENE_ID, name)
        cv.put(SceneContract.SceneEntry.ROOM, rooms)
        cv.put(SceneContract.SceneEntry.ICON, icon)
        cv.put(SceneContract.SceneEntry.REMOTE, "")
        try {
            db.beginTransaction()
            db.insert(SceneContract.SceneEntry.TABLE_NAME, null, cv)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            Log.d("ERROR_SQL_UPDATE_SCENE", "Error : $e")
            return false
        } finally {
            db.endTransaction()
        }
        return true


    }

    fun getAllScenes(db: SQLiteDatabase, home: String): ArrayList<Scene> {
        val params = arrayOf<String>(home)
        val cursor = db.rawQuery(GET_SCENE_QUERY, params)
        val Scenes: ArrayList<Scene> = ArrayList()
        var btn_list = ArrayList<RemoteIconModel>()
        val gson = Gson()
        while (cursor.moveToNext()) {
            val dummyScene = Scene()
            dummyScene.icon = Integer.valueOf(cursor.getString(2))
            dummyScene.name = cursor.getString(0)

            var room: ArrayList<RoomModel>
            val type = object : TypeToken<List<RoomModel>>() {}.type
            room = gson.fromJson(cursor.getString(1), type)
            dummyScene.room = room

            val typeremote = object : TypeToken<ArrayList<RemoteIconModel>>() {}.type
            val data = cursor.getString(3)
            if (!data.isNullOrEmpty()) {
                btn_list = gson.fromJson(data, typeremote)
                dummyScene.remoteData = btn_list
            }
            Scenes.add(dummyScene)
        }
        cursor.close()
        return Scenes
    }

    fun getSceneTable(db: SQLiteDatabase): ArrayList<Scene> {
        val cursor = db.rawQuery(GET_ALL_SCENES, null)
        val Scenes: ArrayList<Scene> = ArrayList()
        val gson = Gson()
        var btn_list = ArrayList<RemoteIconModel>()
        while (cursor.moveToNext()) {
            val dummyScene = Scene()
            dummyScene.home = cursor.getString(0)

            var room: ArrayList<RoomModel>
            val type = object : TypeToken<List<RoomModel>>() {}.type
            room = gson.fromJson(cursor.getString(1), type)
            dummyScene.room = room

            dummyScene.name = cursor.getString(2)
            dummyScene.roomName = cursor.getString(3)

            dummyScene.icon = Integer.valueOf(cursor.getString(4))

            val typeremote = object : TypeToken<ArrayList<RemoteIconModel>>() {}.type
            val data = cursor.getString(5)
            if (!data.isNullOrEmpty()) {
                btn_list = gson.fromJson(data, typeremote)
                dummyScene.remoteData = btn_list
            }
            Scenes.add(dummyScene)
        }
        cursor.close()
        return Scenes
    }

    //GET_SELECTED_SCENE
    fun getSceneByName(db: SQLiteDatabase, sceneName: String, home: String): Scene {
        val params = arrayOf<String>(sceneName, home)
        val cursor = db.rawQuery(GET_SELECTED_SCENE, params)
        val gson = Gson()
        var btn_list = ArrayList<RemoteIconModel>()
        val dummyScene = Scene()
        while (cursor.moveToNext()) {
            dummyScene.icon = Integer.valueOf(cursor.getString(2))
            dummyScene.name = cursor.getString(0)

            var room: ArrayList<RoomModel>
            val type = object : TypeToken<List<RoomModel>>() {}.type
            room = gson.fromJson(cursor.getString(1), type)
            dummyScene.room = room

            val typeremote = object : TypeToken<ArrayList<RemoteIconModel>>() {}.type
            val data = cursor.getString(3)
            if (!data.isNullOrEmpty()) {
                btn_list = gson.fromJson(data, typeremote)
                dummyScene.remoteData = btn_list
            }


        }
        cursor.close()
        return dummyScene
    }


    fun getSceneForRoom(db: SQLiteDatabase, home: String, roomName: String): ArrayList<Scene> {

        val params = arrayOf(home, roomName)
        val cursor = db.rawQuery(GET_SCENE_FOR_ROOM_QUERY, params)
        val Scenes: ArrayList<Scene> = ArrayList()
        val gson = Gson()
        while (cursor.moveToNext()) {
            val dummyScene = Scene()
            var room: ArrayList<RoomModel>
            val type = object : TypeToken<List<RoomModel>>() {}.type
            room = gson.fromJson(cursor.getString(1), type)
            dummyScene.name = cursor.getString(0)
            dummyScene.roomName = cursor.getString(2)
            dummyScene.icon = Integer.valueOf(cursor.getString(3))
            dummyScene.room = room
            Scenes.add(dummyScene)
        }
        cursor.close()
        return Scenes
    }


    fun renameRoomForScene(db: SQLiteDatabase, scene: String, roomNameOld: String, roomNameNew: String): ArrayList<Scene> {


        val params = arrayOf<String>(scene)
        val cursor = db.rawQuery(GET_SCENE_QUERY, params)
        val Scenes: ArrayList<Scene> = ArrayList()
        val gson = Gson()
        while (cursor.moveToNext()) {
            val dummyScene = Scene()
            dummyScene.name = cursor.getString(0)

            var room: ArrayList<RoomModel>
            val type = object : TypeToken<List<RoomModel>>() {}.type
            room = gson.fromJson(cursor.getString(1), type)

            dummyScene.icon = Integer.valueOf(cursor.getString(2))
            dummyScene.room = room
            var sceneExistFlag = false
            for (scene in dummyScene.room) {
                // if(scene.name == roomName){
                sceneExistFlag = true
                //}
            }
            if (sceneExistFlag) {
                Scenes.add(dummyScene)
            }

        }
        cursor.close()
        return Scenes
    }

    fun updateRoom(db: SQLiteDatabase, roomOldName: String, roomNewName: String) {
        Log.d("ROOM_UPDATE", "updateRoom")
        val cv = ContentValues()
        cv.put(SceneContract.SceneEntry.ROOM, roomNewName)
        db.update(SceneContract.SceneEntry.TABLE_NAME, cv, UPDATE_ROOM_DETAILS_SCENE, arrayOf(roomOldName))
    }

    fun checkSceneExist(db: SQLiteDatabase, name: String, home: String): Boolean {
        var automationSceneExist = false
        val paramcheck = arrayOf<String>(name, home)
        val cursorcheck = db.rawQuery(Constant.CHECK_SCENE_EXIST_QUERY, paramcheck)
        while (cursorcheck.moveToNext()) {
            automationSceneExist = true
        }
        if (automationSceneExist) {
            cursorcheck.close()
            return false
        }
        return true
    }


    fun deleteScene(db: SQLiteDatabase, sceneName: String, home: String) {
        val params = arrayOf(sceneName, home)
        db.delete(SceneContract.SceneEntry.TABLE_NAME, DELETE_SCENE, params)
    }

    fun deleteHomeScenes(db: SQLiteDatabase, homeName: String) {
        val params = arrayOf(homeName)
        db.delete(SceneContract.SceneEntry.TABLE_NAME, DELETE_HOME_SCENE_QUERY, params)
    }

    fun deleteTable(db: SQLiteDatabase) {
        db.delete("scene", null, null)
    }

}