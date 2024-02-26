package com.wozart.aura.data.sqlLite

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.wozart.aura.entity.sql.camera.CameraContract
import com.wozart.aura.ui.dashboard.model.CameraModel
import com.wozart.aura.utilities.Constant
import com.wozart.aura.utilities.Constant.Companion.GET_CAMERA
import java.lang.Exception


/**
 * Created by Saif on 28/01/21.
 * mds71964@gmail.com
 */
class CameraTable {

    fun insert(database: SQLiteDatabase,home: String, room: String, cameraName: String,url:String,type: String,favourite:Boolean) : Boolean{

        if(type=="create"){
            var cameraExist = false
            val paramsCheck = arrayOf<String>(cameraName,home)
            val cursorCheck = database.rawQuery(Constant.GET_CAMERA_DATA, paramsCheck)
            while (cursorCheck.moveToNext()) {
                cameraExist = true

            }
            if(cameraExist){
                cursorCheck.close()
                return false
            }
        }

        val cv = ContentValues()
        cv.put(CameraContract.CameraEntry.CAMERA_NAME,cameraName)
        cv.put(CameraContract.CameraEntry.CAMERA_URL,url)
        cv.put(CameraContract.CameraEntry.HOME,home)
        cv.put(CameraContract.CameraEntry.ROOM,room)
        cv.put(CameraContract.CameraEntry.FAVOURITE,favourite)
        try {
            database.beginTransaction()
            database.insert(CameraContract.CameraEntry.TABLE_NAME,null,cv)
            database.setTransactionSuccessful()
        }catch (e:Exception){

        }finally {
            database.endTransaction()
        }

        return true
    }

    fun getAllCameras(db:SQLiteDatabase,home: String) : MutableList<CameraModel>{
        val params = arrayOf(home)
        val cursor = db.rawQuery(GET_CAMERA,params)
        val cameraList : MutableList<CameraModel> = ArrayList()
        while (cursor.moveToNext()){
            val cameraModel = CameraModel()
            cameraModel.cameraName = cursor.getString(0)
            cameraModel.home = cursor.getString(1)
            cameraModel.room = cursor.getString(2)
            cameraModel.url = cursor.getString(3)
            cameraModel.favourite = cursor.getString(4)!!.toBoolean()
            cameraList.add(cameraModel)
        }
        cursor.close()
        return cameraList
    }

    fun delete(db: SQLiteDatabase,name:String,home: String){
        val params = arrayOf(name,home)
        db.delete(CameraContract.CameraEntry.TABLE_NAME,Constant.DELETE_CAMERA_,params)
    }
}