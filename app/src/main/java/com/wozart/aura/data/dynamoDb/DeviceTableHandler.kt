package com.wozart.aura.data.dynamoDb

import android.util.Log
import aura.wozart.com.aura.entity.amazonaws.models.nosql.DevicesTableDO
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.wozart.aura.utilities.Constant
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.facebook.FacebookSdk
import com.facebook.FacebookSdk.getApplicationContext
import com.wozart.aura.data.model.ThingError
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.AuraSwitchLoad
import com.wozart.aura.entity.model.aura.DeviceTableModel
import java.util.ArrayList

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
class DeviceTableHandler {
    private val LOG_TAG = DeviceTableHandler::class.java.simpleName

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

    fun userDevices(devices: List<String>?): ArrayList<DevicesTableDO>? {
        val userDevice = ArrayList<DevicesTableDO>()
        try {
            if (devices == null) return null
            for (x in devices) {
                userDevice.add(dynamoDBMapper!!.load<DevicesTableDO>(DevicesTableDO::class.java, x))
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return null
        }

        return userDevice
    }

    fun insertDevice(device: AuraSwitch,thing:ThingError) : String?{
        try {
            val deviceId = device.uiud.substring(0, Math.min(device.uiud.length, 12))
            val newDevice = DevicesTableDO()
            newDevice.uiud = device.uiud
            newDevice.name = device.name
            newDevice.deviceId = deviceId
            if(device.thing == thing.thing){
                newDevice.master = "Version2"
                newDevice.room = thing.certificate
                newDevice.home = thing.privateKey
            }
            newDevice.thing = device.thing
            dynamoDBMapper!!.save<Any>(newDevice)
            return "SUCCESS"
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return "ERROR"
        }
        return "SUCCESS"
    }


    fun insertDeviceSync(device: DeviceTableModel, uiud: String, homeBg:String, roomBg:String, roomIcon:String){
        try {
            val deviceId = uiud.substring(0, Math.min(uiud.length, 12))
            val loads : MutableList<String> = ArrayList()
            for(load in device.loads){
                var fav = "0"
                var dim = "0"
                if(load.favourite!!){
                    fav = "1"
                }
                if(load.dimmable!!){
                    dim="1"
                }
                loads.add(load.name!!+"?"+fav+dim+load.icon)
            }
            val newDevice = DevicesTableDO()
            newDevice.uiud = uiud
            newDevice.name = device.name
            newDevice.deviceId = deviceId
            newDevice.thing = device.thing
            newDevice.master = Constant.IDENTITY_ID
            //newDevice.loads = loads
            newDevice.room = "${device.room}?$roomBg$roomIcon"
            newDevice.home = Constant.HOME + "?" + homeBg
            dynamoDBMapper!!.save<Any>(newDevice)
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return
        }
    }

    fun checkThing(deviceId: String): ThingError?{
        try {
            var thingDetails = ThingError()
            val checkDevice = dynamoDBMapper!!.load(DevicesTableDO::class.java, deviceId)

            if (checkDevice != null) {
                if(checkDevice.master != null){
                       if(checkDevice.master == "Version2"){
                           thingDetails.version = "Version2"
                           thingDetails.thing = checkDevice.thing!!
                           thingDetails.certificate = checkDevice.room  //certificate key is room
                           thingDetails.privateKey = checkDevice.home  //private key is home
                           thingDetails.error = "success"
                           thingDetails.region = "us-east-1"
                       }else{
                           thingDetails.version = "Version1"
                           thingDetails.thing = checkDevice.thing!!
                           thingDetails.error = "error"
                           thingDetails.region = "us-east-1"
                       }

                }else{
                    thingDetails.version = "Version1"
                    thingDetails.thing = checkDevice.thing!!
                    thingDetails.error = "error"
                    thingDetails.region = "us-east-1"
                }
                checkDevice.slave = null
                checkDevice.loads = null
                checkDevice.uiud = null
                dynamoDBMapper!!.save<Any>(checkDevice)
                return thingDetails
            }else{
                var details = ThingError()
                details.version = "Version1"
                details.thing = null
                details.error = "error"
                details.region = "us-east-1"
                return details
            }

        } catch (e: Exception) {
            //Log.e(LOG_TAG, "Error : $e")
            var details = ThingError()
            details.version = "error"
            details.thing = null
            details.error = "error"
            details.region = "us-east-1"
            return details
        }
    }


    fun deleteDevice(deviceId: String) : String?{
        try {

            val devicesTableDO = dynamoDBMapper!!.load(DevicesTableDO::class.java, deviceId)
            devicesTableDO.loads = null
            devicesTableDO.slave = null
            devicesTableDO.uiud = null
            dynamoDBMapper!!.save<Any>(devicesTableDO)
            return "SUCCESS"
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return "ERROR"
        }


    }

    fun updateThingForDevice(deviceId: String, thing: String) {
        try {
            val deviceThing = dynamoDBMapper!!.load<DevicesTableDO>(DevicesTableDO::class.java, deviceId)
            deviceThing.thing = thing
            dynamoDBMapper!!.save<DevicesTableDO>(deviceThing)


        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error : $e")
        }

    }

    fun updateLoadForDevice(deviceId: String, load: AuraSwitchLoad) {
        try {
            val device = dynamoDBMapper!!.load<DevicesTableDO>(DevicesTableDO::class.java, deviceId)
           var fav = "0"
            if(load.favourite!!){
                fav = "1"
            }
            var dim = "0"
            if(load.dimmable!!){
                dim = "1"
            }
            var loadName = load.name+"?"+fav+dim+load.icon.toString()

            val dLoads : MutableList<String> = ArrayList()
            for((index, l) in device.loads!!.withIndex()){
                if(index == load.index){
                    dLoads.add(loadName)
                }else{
                  //  dLoads.add(l)
                }
            }
           // device.loads = dLoads
            dynamoDBMapper!!.save<DevicesTableDO>(device)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error : $e")
        }

    }


    fun getDeviceForGuest(deviceId: String) : DevicesTableDO?{
        try {
           return dynamoDBMapper!!.load<DevicesTableDO>(DevicesTableDO::class.java, deviceId)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error : $e")
            return null
        }

    }

    fun insertSlave(devices: ArrayList<DevicesTableDO>) {
        try {

            for (device in devices) {
                val deviceThing = dynamoDBMapper!!.load(DevicesTableDO::class.java, device.deviceId)
                val slaves = ArrayList<String>()
                if (deviceThing.slave != null) {
                    for (slave in deviceThing.slave!!) {
                        if (slave == Constant.IDENTITY_ID) {
                            return
                        }
                    }
                }
                val master = deviceThing.master
                if (master == Constant.IDENTITY_ID) {
                    return
                }
                if (deviceThing.slave != null) {
                    slaves.add(Constant.IDENTITY_ID!!)
                    deviceThing.slave = slaves
                    dynamoDBMapper!!.save<DevicesTableDO>(deviceThing)
                } else {
                    val slave = ArrayList<String>()
                    slave.add(Constant.IDENTITY_ID!!)
                    deviceThing.slave = slave
                    dynamoDBMapper!!.save<DevicesTableDO>(deviceThing)
                }
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
        }

    }

//    fun removeSlaves(devices: ArrayList<SharingModel>?) {
//        try {
//            val dynamoDBClient = AmazonDynamoDBClient(AWSMobileClient.getInstance().credentialsProvider)
//            this.dynamoDBMapper = DynamoDBMapper.builder()
//                    .dynamoDBClient(dynamoDBClient)
//                    .awsConfiguration(AWSMobileClient.getInstance().configuration)
//                    .build()
//            if (devices != null) {
//                for (device in devices) {
//                    val sharedDevice = dynamoDBMapper!!.load(DevicesTableDO::class.java, device.getDeviceId())
//                    val indexes = ArrayList<Int>()
//                    if (sharedDevice.getSlave() != null) {
//                        for (i in 0 until sharedDevice.getSlave().size()) {
//                            val slave = sharedDevice.getSlave().get(i)
//                            if (sharedDevice.getSlave().get(i).equals(Constant.IDENTITY_ID)) {
//                                indexes.add(i)
//                            }
//                        }
//                        for (x in indexes) sharedDevice.getSlave().remove(x)
//                        dynamoDBMapper!!.save<DevicesTableDO>(sharedDevice)
//                    }
//
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(LOG_TAG, "Error : $e")
//        }
//    }
}