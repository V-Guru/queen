package com.wozart.aura.data.dynamoDb

import android.R
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import aura.wozart.com.aura.entity.amazonaws.models.nosql.DevicesTableDO
import com.wozart.aura.entity.amazonaws.models.nosql.UserTableDO
import com.wozart.aura.utilities.Constant
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.wozart.aura.data.model.Notification
import com.wozart.aura.data.sqlLite.DeviceTable
import com.wozart.aura.entity.model.sharing.SharedAccess
import com.wozart.aura.entity.sql.device.DeviceDbHelper
import java.util.ArrayList
import kotlin.concurrent.thread
import com.amazonaws.regions.Regions
import com.facebook.FacebookSdk.getApplicationContext
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import org.jetbrains.anko.design.snackbar


/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 04/05/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class UserTableHandler {
    private val LOG_TAG = UserTableHandler::class.java.simpleName
    private var deviceTableHandler = DeviceTableHandler()

    companion object {
        private var dynamoDBMapper: DynamoDBMapper?
        var credentialsProvider = CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:52da6706-7a78-41f4-950c-9d940b890788", // Identity Pool ID
                Regions.US_EAST_1 // Region
        )

        init {
            val dynamoDBClient = AmazonDynamoDBClient(credentialsProvider)
            this.dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(AWSMobileClient.getInstance().configuration)
                    .build()
        }
    }


    fun getUser(): UserTableDO? {
        try {
            return dynamoDBMapper!!.load(UserTableDO::class.java, Constant.IDENTITY_ID)
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return null
        }
    }





    fun getUserwithID(id : String): UserTableDO? {
        try {
            return dynamoDBMapper!!.load(UserTableDO::class.java, id)
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return null
        }
    }

    fun insertUser(userId: String, userName: String?, email: String?) {
        try {
            val userTableDO = UserTableDO()
            userTableDO.userId = userId

            if (userName != null) userTableDO.name = userName
            if (email != null) userTableDO.email = email

            Thread(Runnable { dynamoDBMapper!!.save<UserTableDO>(userTableDO) }).start()
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
        }
    }


    fun isUserAlreadyRegistered(id: String): Boolean {
        return try {
            val checkUser = dynamoDBMapper!!.load<UserTableDO>(UserTableDO::class.java, id)
            checkUser != null
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return false
        }

    }



    fun updateUserDevices(deviceId: String) {
        try {
            val user: UserTableDO = dynamoDBMapper!!.load(UserTableDO::class.java, Constant.IDENTITY_ID)
            if (user.devices != null) {
                for (x in user.devices!!) {
                    if (x == deviceId) {
                        return
                    }
                }
            }

            var devices: MutableList<String>  = ArrayList()
            if (user.devices != null)
                devices = user.devices!!
            devices.add(deviceId)
            user.devices = devices
            dynamoDBMapper!!.save<UserTableDO>(user)
            Log.i(LOG_TAG, "User Device Updated")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error : $e")
        }
    }


    private fun isDeviceAlreadyPresentInDynamoDb(device: String): Boolean {
        try {
            val updateDevice = dynamoDBMapper!!.load(UserTableDO::class.java, Constant.IDENTITY_ID)
            if (updateDevice.devices?.size == 0 || updateDevice.devices == null) return false
            for (x in updateDevice.devices!!) {
                if (x == device) {
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error : $e")
            return false
        }

    }
    private fun getGuestDevicesFromDynamoDb(): MutableList<String>? {
        var updateDevice : UserTableDO ? = null
        try {
             updateDevice = dynamoDBMapper!!.load(UserTableDO::class.java, Constant.IDENTITY_ID)
            return updateDevice.devices
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return updateDevice!!.devices

        }
        return updateDevice!!.devices
    }

    fun deleteUserDevice(home:String,devices: MutableList<String>) {
        try {

            val user = dynamoDBMapper!!.load(UserTableDO::class.java, Constant.IDENTITY_ID)

           var newDeviceList : MutableList<String> = ArrayList()
            if(user != null){
                 if(user.devices != null){
                      for(d in user.devices!!){
                             var flag = false
                             for(delDevice in devices){
                                  if(delDevice == d){
                                        flag = true
                                  }
                             }
                             if(!flag){
                                 newDeviceList.add(d)
                             }
                      }
                      user.devices = newDeviceList
                 }

            }

            dynamoDBMapper!!.save<UserTableDO>(user)
            return
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error : $e")
        }

    }


    fun shareDevices(shareId: String, home: String,userId:String): Boolean {
        try {
            var sharedUser: UserTableDO
            val availableThings = HashMap<String, AttributeValue>()
            availableThings[":val1"] = AttributeValue().withS(shareId)
            val sharedData = HashMap<String, String>()
            val sharedMasterData = HashMap<String, String>()
            val scanExpression = DynamoDBScanExpression()
                    .withFilterExpression("Email = :val1").withExpressionAttributeValues(availableThings)
             thread {
                 val owner =  getUser()

                 if(owner == null){
                     returnError()
                 }else{
                     val scanResult = dynamoDBMapper!!.scan<UserTableDO>(UserTableDO::class.java, scanExpression)
                     when {
                         scanResult.isEmpty() ->  { }
                         scanResult[0].userId.equals(Constant.IDENTITY_ID) ->  { }
                         else -> {
                             sharedUser = scanResult[0]
                             val sharedAccess = SharedAccess("invite", home, sharedUser.userId!!, userId)

                             sharedData["Access"] = sharedAccess.access
                             sharedData["Home"] = sharedAccess.home!!
                             sharedData["UserID"] = Constant.IDENTITY_ID!!
                             sharedData["Name"] = sharedAccess.name!!

                             sharedMasterData["Access"] = "guest"
                             sharedMasterData["Home"] = sharedAccess.home!!
                             sharedMasterData["UserID"] = sharedUser.userId!!
                             sharedMasterData["Name"] = sharedUser.name!!


                             if (sharedUser.sharedAccess != null) {
                                 var sharedFlagUpdate = false
                                 var sharedFlagAdd = true
                                 for (share in sharedUser.sharedAccess!!) {
                                     if((share["UserID"] == Constant.IDENTITY_ID) and (share["Home"] == home)){
                                         if(share["Access"] == "invite"){
                                             sharedFlagUpdate = false
                                             sharedFlagAdd = false
                                         }else {
                                             share["Access"] = "invite"
                                             sharedFlagUpdate = true
                                             sharedFlagAdd = false
                                         }
                                         break
                                     }
                                 }
                                 if(sharedFlagAdd){
                                     val sharedAccessForUser = sharedUser.sharedAccess
                                     sharedAccessForUser?.add(sharedData)
                                     sharedUser.sharedAccess = (sharedAccessForUser)
                                 }
                                 if((sharedFlagUpdate) or (sharedFlagAdd)){
                                     dynamoDBMapper!!.save<UserTableDO>(sharedUser)
                                 }
                             }else{
                                 val sharedAccessForUser: MutableList<MutableMap<String, String>> = ArrayList()
                                 sharedAccessForUser.add(sharedData)
                                 sharedUser.sharedAccess = (sharedAccessForUser)
                                 dynamoDBMapper!!.save<UserTableDO>(sharedUser)
                             }

                             if (owner!!.sharedAccess != null) {
                                 var sharedFlagMasterAdd = true
                                 for (share in owner!!.sharedAccess!!) {
                                     if((share["UserID"] == sharedUser.userId!!) and (share["Home"] == sharedAccess.home!!) and (share["Access"] == "guest")){
                                         sharedFlagMasterAdd = false
                                     }
                                 }
                                 if(sharedFlagMasterAdd){
                                     val sharedAccessForMaster = owner!!.sharedAccess
                                     sharedAccessForMaster?.add(sharedMasterData)
                                     owner!!.sharedAccess = (sharedAccessForMaster)
                                     dynamoDBMapper!!.save<UserTableDO>(owner)

                                 }
                             }else{
                                 val newGuestList: MutableList<MutableMap<String, String>> = ArrayList()
                                 newGuestList.add(sharedMasterData)
                                 owner!!.sharedAccess = (newGuestList)
                                 dynamoDBMapper!!.save<UserTableDO>(owner)
                             }


                         }
                     }
                 }

            }
            return true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error : $e")
            return false
        }
    }

    fun returnError():Boolean?{
        return false
    }


    fun updateDeclinedRequest(request :  Notification){
        try{
           updateSharedAccess(request, false)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error : $e")
        }
    }

    fun updateSharedDevices(sharedData: Notification, mContext: Context): Boolean {
        return try {
            val sharedDevices = getDevicesOfMasterUser(sharedData)
            updateDevicesInDynamoDb(sharedDevices)
            updateLocalAndSlavesDatabase(sharedData,sharedDevices, mContext)
            updateSharedAccess(sharedData, true)
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error : $e")
            false
        }
    }

    private fun getDevicesOfMasterUser(sharedData: Notification): ArrayList<DevicesTableDO> {
        val home = sharedData.sharedHome
        val userId = sharedData.sharedUserId
        val deviceWithSpecifiedHome = ArrayList<DevicesTableDO>()
        val masterUser = dynamoDBMapper!!.load<UserTableDO>(UserTableDO::class.java, userId)
        if(masterUser != null){
            val sharedDevices = deviceTableHandler.userDevices(masterUser.devices)
            for (device in sharedDevices!!) {
                if (device.home!!.equals(home))
                    deviceWithSpecifiedHome.add(device)
            }
        }

        return deviceWithSpecifiedHome
    }

    private fun updateDevicesInDynamoDb(sharedDevices: ArrayList<DevicesTableDO>) {
        val ownedDevices = getGuestDevicesFromDynamoDb()
        var returnList: ArrayList<DevicesTableDO> = ArrayList()
        for (device in sharedDevices) {
            returnList.add(device)
        }
        for (device in returnList) {
            if (ownedDevices != null) {
                for(d in ownedDevices){
                       if(d == device.deviceId){
                           sharedDevices.remove(device)
                       }
                }
            }
        }
        if(!sharedDevices.isEmpty()){
            val updateDevice = dynamoDBMapper!!.load(UserTableDO::class.java, Constant.IDENTITY_ID)
            var devices: MutableList<String> = ArrayList()
            if (updateDevice.devices != null)
                devices = updateDevice.devices!!
            for (x in sharedDevices)
                devices.add(x.deviceId!!)
            updateDevice.devices = (devices)
            dynamoDBMapper!!.save<UserTableDO>(updateDevice)
        }

    }

    private fun updateSharedAccess(sharedData: Notification, accepted: Boolean) {
        val user = getUser()

        for (shared in user!!.sharedAccess!!) {
            var homeCheck = false
            var userIdCheck = false
            var access = false
            if (shared["Home"] == sharedData.sharedHome) homeCheck = true
            if (shared["UserID"] == sharedData.sharedUserId) userIdCheck = true
            if (shared["Access"] == sharedData.access) access = true
            if (homeCheck && userIdCheck && access) {
                if (accepted) shared["Access"] = "accepted"
                else shared["Access"] = "declined"
                dynamoDBMapper!!.save<UserTableDO>(user)
            }
        }
    }

    fun updateModifiedAccess(home:String){
        val owner = getUser()
        var guestList: MutableList<String> = ArrayList()
        if(owner!!.sharedAccess != null){
            for (shared in owner!!.sharedAccess!!) {
                if(shared["Home"] == home){
                    guestList.add(shared["UserID"]!!)
                }
            }
            for(guest in guestList){
                val user = getUserwithID(guest)
                for(access in user!!.sharedAccess!!){
                    if((access["UserID"] == owner.userId) and (access["Home"] == home)){
                        access["Access"] = "modified"
                        break
                    }
                }
                dynamoDBMapper!!.save<UserTableDO>(user)
            }
        }

    }

    fun deleteGuestAccessFromGuest(home:String,type:Boolean){
        val owner = getUser()
        //var guestList =  owner!!.sharedAccess!!
        var guestList : MutableList<MutableMap<String,String>>  = ArrayList()
        for(shared in owner!!.sharedAccess!!){
            guestList.add(shared)
        }
        for (shared in owner!!.sharedAccess!!) {
            var sharedHome = shared["Home"]!!.split("?")[0]
            var sharedUserList = shared["Name"]!!.split(" ")
            var sharedUser = sharedUserList[0]
            var homeName = "$sharedHome($sharedUser)"
            if(type){
                if((sharedHome == home) and (shared["Access"] == "guest")){
                    guestList.remove(shared!!)
                }
            }else{
                if((homeName == home) and (shared["Access"] == "accepted")){
                    guestList.remove(shared!!)
                }
            }

        }
        owner!!.sharedAccess = guestList
        dynamoDBMapper!!.save<UserTableDO>(owner)
    }


    fun deleteGuestAccessFromMaster(home:String,name:String,userType:Boolean,owner:UserTableDO) {

        val sharedUserForAccess: MutableList<MutableMap<String, String>> = ArrayList()

       if(owner!= null){

           for(shared in owner!!.sharedAccess!!){
               sharedUserForAccess.add(shared)
           }
               for (sharing in owner!!.sharedAccess!!) {
                    var guestHome = sharing["Home"]!!.split("?")[0]
                    var guestName = sharing["Name"]!!

                   if(userType) {
                       if ((sharing["Access"] == "invite") || (sharing["Access"] == "accepted") || (sharing["Access"] == "modified") and (guestHome == home)) {

                           sharedUserForAccess.remove(sharing)

                       }
                   }
                       else{
                        if((sharing["Access"] == "invite") and (guestName == name)){
                            sharedUserForAccess.remove(sharing)
                        }
                    }

            }
           owner!!.sharedAccess =sharedUserForAccess
           dynamoDBMapper!!.save<UserTableDO>(owner)

        }
    }

    fun deleteGuestAccessmaster(home:String,name:String,userType:Boolean,owner:UserTableDO) {

        val sharedUserForAccess: MutableList<MutableMap<String, String>> = ArrayList()

        if(owner!= null){

            for(shared in owner!!.sharedAccess!!){
                sharedUserForAccess.add(shared)
            }
            for (sharing in owner!!.sharedAccess!!) {

                if(userType) {
                    if (sharing["Access"] == "guest" ) {

                        sharedUserForAccess.remove(sharing)

                    }
                }

            }
            owner!!.sharedAccess =sharedUserForAccess
            dynamoDBMapper!!.save<UserTableDO>(owner)

        }


    }



    private fun updateLocalAndSlavesDatabase(sharedData: Notification,devices: ArrayList<DevicesTableDO>, context: Context) {
        val localDeviceTable = DeviceTable()
        val mDb: SQLiteDatabase
        val dbHelper = DeviceDbHelper(context)
        mDb = dbHelper.writableDatabase
        val deviceDynamoDb = DeviceTableHandler()
        deviceDynamoDb.insertSlave(devices)

        val gson = Gson()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val type = object : TypeToken<List<RoomModelJson>>() {}.type
        val dataRoomHome = sharedPref.getString("HOME_ROOM_DATA", "NULL")
        var list_room : MutableList<RoomModelJson> = ArrayList()
        list_room = gson.fromJson(dataRoomHome,type)

        val home = sharedData.sharedHome!!.split("?")
        var homeBg : String?=null
        if(home[1] == null){
             homeBg = "0"
        }else{
             homeBg = home[1]
        }
        var flag = true
        val homeName = home[0]+"(${sharedData.masterName})"
        for(y in list_room){
            if((y.type == "home") and (y.sharedHome == "guest") and (y.name == homeName)){
                flag = false
            }
        }
        if(flag){
            var rm = RoomModelJson(home[0]+"(${sharedData.masterName})","home","guest",homeBg,0,0.0,0.0,"")
            list_room.add(rm)
        }

        for(x in devices){
            val room = x.room!!.split("?")
            var roomBg = 0
            var roomIc = 0
            if(room.size != 1){
                roomBg = room[1].toInt() / 10
                roomIc = room[1].toInt() % 10
            }
            var flag = true
            for(y in list_room){
                if((y.type == "room") and (y.sharedHome == "guest") and (y.name == room[0])){
                    flag = false
                }
            }
            if(flag){
                var rm = RoomModelJson(room[0],"room","guest",roomBg.toString(),roomIc,0.0,0.0,"")
                list_room.add(rm)
            }
        }

        val prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        prefEditor.putString("HOME_ROOM_DATA", gson.toJson(list_room))
        prefEditor.apply()

        localDeviceTable.insertDeviceFromAws(mDb, devices,sharedData.masterName)
    }

    fun deleteUser(userId: String) : String?{
        try {
           // var userTable = dynamoDBMapper!!.load(UserTableDO::class.java, userId)
            var userTable = UserTableDO()
            userTable.userId = userId
            dynamoDBMapper!!.delete(userTable)
            return "SUCCESS"
        }catch (e: Exception){
            Log.e("DELETE_EXCEPTION","DELETE_USER")
            return "ERROR"
        }
    }
}