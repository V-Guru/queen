package com.wozart.aura.data.sqlLite

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.entity.sql.scheduling.ScheduleContract
import com.wozart.aura.entity.sql.users.UserContract
import com.wozart.aura.utilities.Constant.Companion.CHECK_USER_PRESENCE
import com.wozart.aura.utilities.Constant.Companion.DELETE_USER_TABLE
import com.wozart.aura.utilities.Constant.Companion.GET_USER_LIST
import java.lang.Exception

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-11-26
 * Description :
 *****************************************************************************/

//this class we used to call for any quesries
//all methods for the queries related will exist here
class UserTable {
    //insert method which will insert user to table
    fun insertUser(db: SQLiteDatabase, name: String, email: String, contact: String, profile: String, userHome: String, userId: String, lastName: String):Boolean {

       val param = arrayOf(userId) // will check wether this userid exist in table and that param will be passed to rawQuery
        val cursor = db.rawQuery(CHECK_USER_PRESENCE,param)
        //if user exist count will be > 0
        if(cursor.count != 0){
            //if exist will delete table and create new one
            db.delete(UserContract.UserEntry.TABLE_NAME,DELETE_USER_TABLE,param)
        }
        cursor.close()

        //for putting values into table
        val cnv = ContentValues()
       // ContentValues cnv = new ContentValues()
        cnv.put(UserContract.UserEntry.USER_FIRSTNAME,name)
        cnv.put(UserContract.UserEntry.USER_EMAIL,email)
        cnv.put(UserContract.UserEntry.USER_CONTACT,contact)
        cnv.put(UserContract.UserEntry.USER_PROFILE,profile)
        cnv.put(UserContract.UserEntry.USER_HOME,userHome)
        cnv.put(UserContract.UserEntry.USER_ID,userId)
        cnv.put(UserContract.UserEntry.USER_LASTNAME,lastName)

        try{
            db.beginTransaction()
            db.insert(UserContract.UserEntry.TABLE_NAME,null,cnv)
            db.setTransactionSuccessful()

        }catch (e: Exception){

        } finally {
            db.endTransaction()
        }
        return true
    }

    fun getUsers(db: SQLiteDatabase,userId: String): ArrayList<User>{
        val param = arrayOf(userId)
        val cursor = db.rawQuery(GET_USER_LIST,param)
        val userList = ArrayList<User>()
        while (cursor.moveToNext()){
            val users = User()
            users.firstName = cursor.getString(0)
            users.lastName = cursor.getString(1)
            users.mobile = cursor.getString(2)
            users.profileImage = cursor.getString(3)
            userList.add(users)
        }
        cursor.close()
        return userList
    }

    fun deleteTable(db: SQLiteDatabase) {
        db.delete(UserContract.UserEntry.TABLE_NAME, null, null)
    }
}