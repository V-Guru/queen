package com.wozart.aura.data.dynamoDb

import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.QueryRequest
import com.facebook.FacebookSdk.getApplicationContext
import com.github.mikephil.charting.charts.Chart.LOG_TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wozart.aura.aura.data.model.User
import com.wozart.aura.data.model.ButtonModel
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.entity.amazonaws.models.nosql.RulesDO
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aura.DeviceThing
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.auraSense.RemoteListModel
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.room.RoomModelJson
import com.wozart.aura.utilities.Common
import com.wozart.aura.utilities.Constant

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 28/09/18
 * Description :
 *****************************************************************************/

class RulesTableHandler {

    private var LOG_TAG_SCH = RulesTableHandler::class.java


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


    fun isUserAlreadyRegistered(id: String): Boolean {
        try {
            val checkUser = dynamoDBMapper!!.load<RulesDO>(RulesDO::class.java, id)
            if (checkUser == null) {
                return false
            } else {
                return !(checkUser.email == null && checkUser.name == null && checkUser.verified == null)
            }
        } catch (e: Exception) {
            return false
        }
    }


    fun insertUser(userId: String, userName: String?, email: String?, verified: String, userType: String) {
        try {
            val appVersion = getApplicationContext().packageManager.getPackageInfo(getApplicationContext().packageName, 0).versionName
            val checkUser = dynamoDBMapper!!.load<RulesDO>(RulesDO::class.java, if (userType == Constant.NEW_USER) email else userId)
            if (checkUser == null) {
                val rulesTableDO = RulesDO()
                rulesTableDO.userId = email
                rulesTableDO._id = userId
                rulesTableDO.name = userName
                rulesTableDO.email = email
                rulesTableDO.verified = verified
                rulesTableDO.appVersion = appVersion
                dynamoDBMapper!!.save<RulesDO>(rulesTableDO)
            } else {
                val newUserCreate = checkUser
                if (userType == Constant.EXIST_USER) dynamoDBMapper!!.delete(checkUser)
                newUserCreate._id = Constant.USER_ID
                newUserCreate.userId = email
                newUserCreate.email = email
                newUserCreate.verified = verified
                checkUser.appVersion = appVersion
                dynamoDBMapper!!.save<RulesDO>(newUserCreate)
            }

        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }

    fun insertUserDetails(phone_number: String, userFirstName: String, userLastName: String, timeZome: String, userId: String, loginProvider: String?): Boolean {
        try {
            val insertUser = dynamoDBMapper!!.load<RulesDO>(RulesDO::class.java, userId)
            if (insertUser == null) {
                val rulesTableDo = RulesDO()
                rulesTableDo.firstName = userFirstName
                rulesTableDo.LastName = userLastName
                rulesTableDo.phoneNumber = phone_number
                rulesTableDo.timeZone = timeZome
                if (loginProvider.equals("Cognito")) rulesTableDo.name = "$userFirstName $userLastName"
                dynamoDBMapper!!.save<RulesDO>(rulesTableDo)
            } else {
                insertUser.firstName = userFirstName
                insertUser.LastName = userLastName
                insertUser.phoneNumber = phone_number
                insertUser.timeZone = timeZome
                if (loginProvider.equals("Cognito")) insertUser.name = "$userFirstName $userLastName"
                dynamoDBMapper!!.save<RulesDO>(insertUser)
            }
            return true
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
            return false
        }
    }

    fun getUser(): RulesDO? {
        try {
            return dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return null
        }
    }


    fun checkEmailExist(emails: String): String {
        try {
            val emailCheck = HashMap<String, AttributeValue>()
            emailCheck[":val1"] = AttributeValue().withS(emails)
            val scanExpression = DynamoDBScanExpression().withFilterExpression("Email = :val1").withExpressionAttributeValues(emailCheck)
            val checkUserEmail = dynamoDBMapper!!.scan<RulesDO>(RulesDO::class.java, scanExpression)
            if (checkUserEmail != null) {
                if (checkUserEmail[0].email == emails) {
                    return "SUCCESS"
                } else {
                    return "ERROR"
                }
            } else return "ERROR"

        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
            return "ERROR"
        }

    }

    private fun getFirstName(email: String?): String {
        var guestFirstName: String? = null
        try {
            val hasMap = HashMap<String, AttributeValue>()
            hasMap[":userId"] = AttributeValue().withS(email)
            val dynamoDBClient = AmazonDynamoDBAsyncClient(credentialsProvider)
            val queryRequest = QueryRequest().withTableName("wozartaura-mobilehub-1863763842-Rules")
            queryRequest.withKeyConditionExpression("userId = :userId").withExpressionAttributeValues(hasMap)
            val scanResult = dynamoDBClient.query(queryRequest)
            for (data_ in scanResult.items) {
                val firstName = data_["FirstName"]?.s
                val lastName = data_["LastName"]?.s
                guestFirstName = "$firstName $lastName"
                return guestFirstName.toString()
            }
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
        return guestFirstName.toString()
    }


    fun updateUserVerification(userId: String, verified: String, email: String, personName: String) {
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            user.verified = verified
            user.email = email
            user.name = personName
            dynamoDBMapper!!.save<RulesDO>(user)
        } catch (e: Exception) {
            Log.i("Error", "error ")
        }
    }

    fun updateUserSense(userId: String, home: String, schedule: AutomationScene, type: String, oldScheduleName: String, senseDataList: MutableList<AuraSenseConfigure>, deviceThing: ArrayList<DeviceThing>, turnOffTiming: Int, whenSenseTrigger: String, turnOfNever: String, senseState: String, senseMacId: String, senseThing: String, favRemoteButtonList: MutableList<RemoteIconModel>) {
        try {
            var user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            val newUser = RulesDO()
            val SceneList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val Scene = HashMap<String, MutableList<MutableMap<String, String>>>()
            val deviceList: MutableList<MutableMap<String, String>> = ArrayList()
            val map = HashMap<String, String>()
            map["id"] = senseMacId
            map["name"] = schedule.name.toString()
            map["oldName"] = oldScheduleName
            map["operation"] = type
            map["type"] = "automation"

            for (room in schedule.load) {
                for (device in room.deviceList) {
                    val map_device = HashMap<String, String>()
                    if (device.deviceName != "Scene") {
                        map_device["type"] = "device"
                        map_device["deviceName"] = device.deviceName
                    } else {
                        map_device["type"] = device.deviceName
                        map_device["deviceName"] = device.name
                    }
                    for (thing in deviceThing) {
                        if (thing.deviceName == device.deviceName) {
                            map_device["things"] = thing.deviceThing.toString()
                            break
                        }
                    }
                    if (device.checkType == "Curtain") {
                        map_device["deviceType"] = device.checkType
                        map_device["curtainSetState"] = device.curtainState
                        map_device["curtainStatus0"] = device.curtainState0.toString()
                        map_device["curtainStatus1"] = device.curtainState1.toString()
                    }
                    if (device.checkType == "rgbDevice") {
                        map_device["deviceType"] = "rgbDevice"
                        map_device["hue"] = device.hueValue.toString()
                        map_device["saturation"] = device.saturationValue.toString()
                    }
                    if (device.checkType == "tunableDevice") {
                        map_device["deviceType"] = "tunableDevice"
                        map_device["temperature"] = device.tempValue.toString()
                    }
                    map_device["level"] = device.dimVal.toString()
                    map_device["index"] = device.index.toString()
                    map_device["state"] = device.isTurnOn.toString()
                    deviceList.add(map_device)
                }
            }


            if (senseDataList.size > 1) {
                for (sense in senseDataList) {
                    val map_sense = HashMap<String, String>()
                    map_sense["senseDeviceName"] = sense.senseDeviceName.toString()
                    map_sense["senseUiud"] = sense.senseUiud.toString()
                    map_sense["senseMac"] = sense.senseMacId.toString()
                    map_sense["type"] = "sensor"
                    map_sense["sensorType"] = sense.auraSenseName.toString()
                    map_sense["sense"] = "trigger"
                    if (schedule.time != null) {
                        map_sense["startTime"] = schedule.time.toString()
                        map_sense["endTime"] = schedule.endTime.toString()
                    }
                    map_sense["above"] = sense.above.toString()
                    map_sense["below"] = sense.below.toString()
                    map_sense["favourite"] = sense.auraSenseFavorite.toString()
                    map_sense["value"] = sense.range.toString()
                    deviceList.add(map_sense)
                }
            } else {
                for (sense in senseDataList) {
                    val map_sense = HashMap<String, String>()
                    map_sense["senseDeviceName"] = sense.senseDeviceName.toString()
                    map_sense["senseUiud"] = sense.senseUiud.toString()
                    map_sense["senseMac"] = sense.senseMacId.toString()
                    map_sense["type"] = "sensor"
                    map_sense["sensorType"] = sense.auraSenseName.toString()
                    map_sense["sense"] = "trigger"
                    if (schedule.time != null) {
                        map_sense["startTime"] = schedule.time.toString()
                        map_sense["endTime"] = schedule.endTime.toString()
                    }
                    map_sense["above"] = sense.above.toString()
                    map_sense["below"] = sense.below.toString()
                    map_sense["favourite"] = sense.auraSenseFavorite.toString()
                    map_sense["value"] = sense.range.toString()
                    deviceList.add(map_sense)
                }
            }
            for (remote in favRemoteButtonList) {
                val map_remote = HashMap<String, String>()
                map_remote["type"] = "remote"
                map_remote["btnName"] = remote.remoteButtonName.toString()
                map_remote["model"] = remote.remoteModel.toString()
                map_remote["channel"] = remote.channelNumber.toString()
                map_remote["favourite"] = remote.btnFavourite.toString()
                map_remote["senseName"] = remote.name.toString()
                deviceList.add(map_remote)
            }

            val map_data = HashMap<String, String>()
            map_data["type"] = "senseDevice"
            map_data["status"] = "modified"
            map_data["home"] = home
            if (schedule.time != null) {
                map_data["startTime"] = schedule.time.toString()
                map_data["endTime"] = schedule.endTime.toString()
            }
            map_data["triggerWhen"] = whenSenseTrigger
            if (turnOffTiming != 0) {
                map_data["turnOff"] = turnOffTiming.toString()
            } else {
                map_data["turnOff"] = turnOfNever
            }
            map_data["automationEnable"] = schedule.property[0].AutomationEnable.toString()
            deviceList.add(map_data)
            Scene[schedule.name!!] = deviceList
            SceneList.clear()
            if (user == null) {
                SceneList.add(Scene)
                newUser.sense_motion = SceneList
                newUser.userId = userId
                dynamoDBMapper!!.save<RulesDO>(newUser)
            } else {
                if (user.sense_motion == null || user.sense_motion!!.size == 0) {
                    SceneList.add(Scene)
                    user.sense_motion = SceneList
                    user.userId = userId

                } else {
                    if (type == "edit") {
                        for (s in user.sense_motion!!) {
                            val keys = s.keys
                            var flag = false
                            for (k in keys) {
                                if (k == oldScheduleName) {
                                    //SceneList.remove(s)
                                    flag = true
                                }
                            }
                            if (!flag) {
                                SceneList.add(s)
                            }
                        }
                    } else {
                        for (availableSchedule in user.sense_motion!!) {
                            SceneList.add(availableSchedule)
                        }
                    }

                    SceneList.add(Scene)
                    user.sense_motion = SceneList
                    user.userId = userId
                }
                /*
                  Update to configuration
                 */
                if (senseState.contains(Constant.OFFLINE_MODE)) {
                    user = updateConfigurationColumn(user, map, senseThing)
                }
                dynamoDBMapper!!.save<RulesDO>(user)
                Thread.sleep(200)
            }
        } catch (e: Exception) {

        }
    }

    fun updateUserSchedule(userId: String, home: String, schedule: AutomationScene, type: String, oldScheduleName: String, turnOfNever: String, turnOffTiming: Int, routine: String, favRemoteButtonList: MutableList<RemoteIconModel>, senseMode: String, senseMacId: String, senseThing: String) {
        try {
            var user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            val newUser = RulesDO()
            val SceneList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val Scene = HashMap<String, MutableList<MutableMap<String, String>>>()
            val deviceList: MutableList<MutableMap<String, String>> = ArrayList()
            val map_config = HashMap<String, String>()
            map_config["id"] = senseMacId
            map_config["name"] = schedule.name.toString()
            map_config["oldName"] = oldScheduleName
            map_config["operation"] = type
            map_config["type"] = "automation"

            if (routine != "null") {
                val gson = Gson()
                val type_ = object : TypeToken<java.util.HashMap<String, Boolean>>() {}.type
                val routineNew: HashMap<String, Boolean> = gson.fromJson(schedule.routine, type_)
                schedule.updatedRoutine = "[${routineNew["Sunday"]},${routineNew["Monday"]},${routineNew["Tuesday"]},${routineNew["Wednesday"]},${routineNew["Thursday"]},${routineNew["Friday"]},${routineNew["Saturday"]}]"
            }
            for (room in schedule.load) {
                for (device in room.deviceList) {
                    val map = HashMap<String, String>()
                    if (device.deviceName != "Scene") {
                        map["type"] = "device"
                        map["deviceName"] = device.deviceName
                    } else {
                        map["type"] = device.deviceName
                        map["deviceName"] = device.name
                    }
                    map["level"] = device.dimVal.toString()
                    map["index"] = device.index.toString()
                    map["state"] = device.isTurnOn.toString()
                    map["status"] = "modified"
                    deviceList.add(map)
                }
            }
            for (remote in favRemoteButtonList) {
                val map_remote = HashMap<String, String>()
                map_remote["type"] = "remote"
                map_remote["btnName"] = remote.remoteButtonName.toString()
                map_remote["model"] = remote.remoteModel.toString()
                map_remote["channel"] = remote.channelNumber.toString()
                map_remote["favourite"] = remote.btnFavourite.toString()
                map_remote["senseName"] = remote.name.toString()
                deviceList.add(map_remote)
            }
            val map = HashMap<String, String>()
            map["type"] = "schedule"
            map["status"] = "modified"
            if (schedule.type == "geo") {
                map["name"] = schedule.name!!
                map["home"] = home
                map["stype"] = schedule.type.toString()
                map["automationEnable"] = schedule.property[0].AutomationEnable.toString()
                map["latitude"] = schedule.property[0].newGeoLat.toString()
                map["longitude"] = schedule.property[0].newGeolong.toString()
                map["radius"] = schedule.property[0].newGeoRadius.toString()
                map["triggeringType"] = schedule.property[0].triggerType.toString()
                map["triggerwhen"] = schedule.property[0].triggerWhen.toString()
                map["specifictime"] = schedule.property[0].triggerSpecificStartTime.toString()
                map["specificend"] = schedule.property[0].triggerSpecificEndTime.toString()
                map["icon"] = schedule.icon.toString()
                if (turnOffTiming != 0) {
                    map["turnOff"] = turnOffTiming.toString()
                } else {
                    map["turnOff"] = turnOfNever
                }
                deviceList.add(map)
                Scene[schedule.name!!] = deviceList
            } else {
                map["type"] = "schedule"
                map["name"] = schedule.name!!
                map["stype"] = schedule.type!!
                map["icon"] = schedule.icon.toString()
                map["routine"] = schedule.updatedRoutine!!
                map["time"] = schedule.time!!
                map["endtime"] = schedule.endTime!!
                map["home"] = Constant.HOME!!.trim()
                map["automationEnable"] = schedule.property[0].AutomationEnable.toString()
                if (turnOffTiming != 0) {
                    map["turnOff"] = turnOffTiming.toString()
                } else {
                    map["turnOff"] = turnOfNever
                }
                deviceList.add(map)
                Scene[schedule.name!!] = deviceList
            }
            if (user == null) {
                SceneList.add(Scene)
                newUser.schedules = SceneList
                newUser.userId = userId
                dynamoDBMapper!!.save<RulesDO>(newUser)
            } else {
                if ((user.schedules!!.size == 0) || (user.schedules == null)) {
                    SceneList.add(Scene)
                    user.schedules = SceneList
                    user.userId = userId
                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == home) {
                                modifiedCheck["Status"] = "modified"
                            }

                        }
                    }
                } else {
                    if (type == "edit") {
                        for (s in user.schedules!!) {
                            val keys = s.keys
                            var flag = false
                            for (k in keys) {
                                if (k == oldScheduleName) {
                                    flag = true
                                }
                            }
                            if (!flag) {
                                SceneList.add(s)
                            }
                        }
                    } else {
                        for (availableSchedule in user.schedules!!) {
                            SceneList.add(availableSchedule)
                        }
                    }
                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == home) {
                                modifiedCheck["Status"] = "modified"
                            }

                        }
                    }
                    SceneList.add(Scene)
                    user.schedules = SceneList
                    user.userId = userId
                }
                /*
                   Update To Configuration
                 */
                if (senseMode.contains(Constant.OFFLINE_MODE)) {
                    user = updateConfigurationColumn(user, map_config, senseThing)
                }
                dynamoDBMapper!!.save<RulesDO>(user)
            }
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }

    fun deleteUserSchedule(userId: String, home: String, name: String, senseMacId: String?, senseThing: String, senseMode: String) {
        try {
            var gson = Gson()
            var user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            val SceneList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val map_config = HashMap<String, String>()
            map_config["id"] = senseMacId.toString()
            map_config["name"] = name
            map_config["oldName"] = name
            map_config["operation"] = "delete"
            map_config["type"] = "automation"
            if (user == null) {
            } else {
                if (user.schedules == null) {
                } else {
                    for (s in user.schedules!!) {
                        val keys = s.keys
                        var flag = false
                        for (k in keys) {
                            if (k == name) {
                                flag = true
                                break
                            }
                        }
                        if (!flag) {
                            SceneList.add(s)
                        }
                    }
                    /*
               Update To Configuration
                */
                    if (senseMode.contains(Constant.OFFLINE_MODE)) {
                        user = updateConfigurationColumn(user, map_config, senseThing)
                    }
                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == home) {
                                modifiedCheck["Status"] = "modified"
                            }

                        }
                    }
                    user.schedules = SceneList
                    user.userId = userId
                    dynamoDBMapper!!.save<RulesDO>(user)
                }
            }
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }

    fun deleteUserScene(userId: String, home: String, name: String, senseMacId: String, senseThing: String, senseMode: String) {
        try {
            var user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            val SceneList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val map_config = HashMap<String, String>()
            map_config["id"] = senseMacId
            map_config["name"] = name
            map_config["oldName"] = name
            map_config["operation"] = "delete"
            map_config["type"] = "scene"

            for (s in user.scenes!!) {
                val keys = s.keys
                var flag = false
                for (k in keys) {
                    if (k == name) {
                        flag = true
                        break
                    }
                }
                if (!flag) {
                    SceneList.add(s)
                }
            }
            /*
               Update To Configuration
                */
            if (senseMode.contains(Constant.OFFLINE_MODE)) {
                user = updateConfigurationColumn(user, map_config, senseThing)
            }
            if (user.master != null) {
                for (modifiedCheck in user.master!!) {
                    if (modifiedCheck["Home"] == home) {
                        modifiedCheck["Status"] = "modified"
                    }

                }
            }
            user.scenes = SceneList
            user.userId = userId
            dynamoDBMapper!!.save<RulesDO>(user)
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }


    fun updateUserScene(userId: String, home: String, name: String, scene: MutableList<RoomModel>, icon: Int, type: String, oldSceneName: String, favRemoteButtonList: MutableList<RemoteIconModel>, senseMacId: String, senseThing: String, onlineMode: String) {
        try {
            var user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            val newUser = RulesDO()
            var SceneList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val Scene = HashMap<String, MutableList<MutableMap<String, String>>>()
            val map_config = HashMap<String, String>()
            map_config["id"] = senseMacId
            map_config["name"] = name
            map_config["oldName"] = oldSceneName
            map_config["operation"] = type
            map_config["type"] = "scene"

            val deviceList: MutableList<MutableMap<String, String>> = ArrayList()
            for (room in scene) {
                for (device in room.deviceList) {
                    val map = HashMap<String, String>()
                    if (device.deviceName == "Automation") {
                        map["type"] = device.deviceName
                        map["deviceName"] = device.name
                    } else {
                        map["type"] = "device"
                        map["deviceName"] = device.deviceName
                    }
                    if (device.checkType == "Curtain") {
                        map["curtainSetState"] = device.curtainState
                        map["deviceType"] = device.checkType
                        map["curtainStatus0"] = device.curtainState0.toString()
                        map["curtainStatus1"] = device.curtainState1.toString()
                    }
                    if (device.checkType == "rgbDevice") {
                        map["deviceType"] = "rgbDevice"
                        map["hue"] = device.hueValue.toString()
                        map["saturation"] = device.saturationValue.toString()
                    }
                    if (device.checkType == "tunableDevice") {
                        map["deviceType"] = "tunableDevice"
                        map["temperature"] = device.tempValue.toString()
                    }
                    map["level"] = device.dimVal.toString()
                    map["index"] = device.index.toString()
                    map["state"] = device.isTurnOn.toString()
                    map["status"] = "modified"
                    deviceList.add(map)
                }
            }
            val map = HashMap<String, String>()
            map["type"] = "scene"
            map["name"] = name
            map["status"] = "modified"
            map["icon"] = icon.toString()
            map["home"] = Constant.HOME!!
            deviceList.add(map)
            for (remote in favRemoteButtonList) {
                val map_remote = HashMap<String, String>()
                map_remote["type"] = "remote"
                map_remote["btnName"] = remote.remoteButtonName.toString()
                map_remote["model"] = remote.remoteModel.toString()
                map_remote["channel"] = remote.channelNumber.toString()
                map_remote["favourite"] = remote.btnFavourite.toString()
                map_remote["senseName"] = remote.name.toString()
                deviceList.add(map_remote)
            }
            Scene[name] = deviceList

            if (user == null) {
                SceneList.add(Scene)
                newUser.scenes = SceneList
                newUser.userId = userId
                dynamoDBMapper!!.save<RulesDO>(newUser)
            } else {
                if ((user.scenes == null) || (user.scenes!!.size == 0)) {
                    SceneList.add(Scene)
                    user.scenes = SceneList
                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == home) {
                                modifiedCheck["Status"] = "modified"
                            }

                        }
                    }
                    //  dynamoDBMapper!!.save<RulesDO>(user)
                } else {
                    if (type == "edit") {
                        for (s in user.scenes!!) {
                            val keys = s.keys
                            var flag = false
                            for (k in keys) {
                                if (k == oldSceneName) {
                                    flag = true
                                    break
                                }
                            }
                            if (!flag) {
                                SceneList.add(s)
                            }
                        }

                    } else {
                        SceneList = user.scenes!!
                    }
                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == home) {
                                modifiedCheck["Status"] = "modified"
                            }
                        }
                    }
                    SceneList.add(Scene)
                    user.scenes = SceneList
                }
                /*
                    Update to Configuration
                     */
                if (onlineMode.contains(Constant.OFFLINE_MODE)) {
                    user = updateConfigurationColumn(user, map_config, senseThing)
                }
                dynamoDBMapper!!.save<RulesDO>(user)
            }
            Log.i(LOG_TAG, "User Device Updated")
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }

    private fun updateConfigurationColumn(user: RulesDO, map_config: HashMap<String, String>, senseThing: String): RulesDO {
        val generateList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
        val listMap = HashMap<String, MutableList<MutableMap<String, String>>>()
        val createList: MutableList<MutableMap<String, String>> = ArrayList()
        if (user.configurationList == null || user.configurationList!!.size == 0) {
            createList.add(map_config)
            listMap[senseThing] = createList
            generateList.add(listMap)
            user.configurationList = generateList
        } else {
            var flag = false
            for (data in user.configurationList!!) {
                if (data.containsKey(senseThing)) {
                    for (mapExist in data) {
                        createList.addAll(mapExist.value)
                    }
                    createList.add(map_config)
                    data[senseThing] = createList
                    flag = true
                    break
                }
            }
            if (!flag) {
                generateList.add(listMap)
                user.configurationList!!.addAll(generateList)
            }
            if (flag) {
                user.configurationList = user.configurationList
            }
        }
        return user
    }

    fun storeConfigurationData(userId: String, senseUiud: String, givenName: String, oldAssignedName: String, senseThing: String, operationType: String, type: String) {
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            val newUser = RulesDO()
            val generateList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val listMap = HashMap<String, MutableList<MutableMap<String, String>>>()
            val createList: MutableList<MutableMap<String, String>> = ArrayList()
            val map = HashMap<String, String>()
            map["id"] = senseUiud
            map["name"] = givenName
            map["oldName"] = oldAssignedName
            map["operation"] = operationType
            map["type"] = type

            if (user == null) {
                createList.add(map)
                listMap[senseThing] = createList
                generateList.add(listMap)
                newUser.configurationList = generateList
                dynamoDBMapper!!.save<RulesDO>(newUser)
            } else if (user.configurationList == null || user.configurationList!!.size == 0) {
                createList.add(map)
                listMap[senseThing] = createList
                generateList.add(listMap)
                user.configurationList = generateList
                dynamoDBMapper!!.save<RulesDO>(user)
            } else {
                var flag = false
                for (data in user.configurationList!!) {
                    if (data.containsKey(senseThing)) {
                        for (mapExist in data) {
                            createList.addAll(mapExist.value)
                        }
                        createList.add(map)
                        data[senseThing] = createList
                        flag = true
                        break
                    }
                }
                if (!flag) {
                    generateList.add(listMap)
                    user.configurationList!!.addAll(generateList)
                }
                if (flag) {
                    user.configurationList = user.configurationList
                }
                dynamoDBMapper!!.save<RulesDO>(user)
            }
        } catch (e: Exception) {

        }
    }

    fun updateAuraSenseRemote(data: RemoteListModel, userId: String, type: String) {
        val remote_list: MutableList<MutableMap<String, String>> = ArrayList()
        val remote_list_data: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>>? = ArrayList()
        val remote = HashMap<String, MutableList<MutableMap<String, String>>>()
        val rulesDO = RulesDO()
        var key: String? = null
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            val mapDevice = HashMap<String, String>()
            mapDevice["deviceName"] = data.auraSenseName.toString()
            mapDevice["deviceId"] = data.senseUiud.toString()
            mapDevice["type"] = "senseDevice"
            mapDevice["home"] = data.home.toString()
            mapDevice["location"] = data.remoteLocation.toString()
            mapDevice["remoteMake"] = data.brandName.toString()
            mapDevice["remoteName"] = data.remoteName.toString()
            mapDevice["remoteModel"] = data.modelNumber.toString()
            mapDevice["appliances"] = data.typeAppliances.toString()
            mapDevice["remoteFav"] = data.remoteFavourite.toString()
            mapDevice["status"] = "modified"
            remote_list.add(mapDevice)

            for (list in data.dynamicRemoteIconList) {
                val map = HashMap<String, String>()
                map["btnName"] = list.remoteButtonName.toString()
                map["btnIcon"] = list.remoteIconButton.toString()
                map["remoteModel"] = data.modelNumber.toString()
                map["remoteMake"] = data.brandName.toString()
                map["remoteName"] = data.remoteName.toString()
                map["appliances"] = data.typeAppliances.toString()
                map["btnFav"] = list.btnFavourite.toString()
                map["dType"] = list.dType.toString()
                if (list.channelNumber.isNotEmpty()) map["cNumber"] = list.channelNumber
                map["cShortcut"] = list.channelShortCut.toString()
                map["internetChannel"] = list.internetChannel.toString()
                map["deviceName"] = data.auraSenseName.toString()
                map["type"] = "remoteButton"
                map["status"] = "modified"
                remote_list.add(map)
            }

            key = data.brandName.toString() + "_" + data.modelNumber
            remote[key] = remote_list
            remote_list_data!!.add(remote)
            if (user == null) {
                rulesDO.userId = userId
                rulesDO.sense_remote = remote_list_data
                dynamoDBMapper!!.save<RulesDO>(rulesDO)
            } else {
                if (user.sense_remote == null || user.sense_remote!!.size == 0) {
                    user.sense_remote = remote_list_data
                    dynamoDBMapper!!.save<RulesDO>(user)
                } else {
                    for (remote_data in user.sense_remote!!) {
                        val keys = remote_data.keys
                        var flag = false
                        for (k in keys) {
                            if (k == key) {
                                flag = true
                                break
                            }
                        }
                        if (!flag) {
                            remote_list_data.add(remote_data)
                        }
                    }
                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == data.home.toString()) {
                                modifiedCheck["Status"] = "modified"
                            }
                        }
                    }

                    //remote_list_data.add(remote)
                    user.sense_remote = remote_list_data
                    dynamoDBMapper!!.save<RulesDO>(user)
                }
            }

        } catch (e: Exception) {
            Log.d("AURA_SENSE_AWS", "Error in remote storage aws.")
        }
    }

    fun deleteRemote(userId: String, brandName: String, modelNumber: String, home: String) {
        try {
            var gson = Gson()
            val user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            val remote_list_data: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val match_key = brandName + "_" + modelNumber
            for (s in user.sense_remote!!) {
                val keys = s.keys
                var flag = false
                for (k in keys) {
                    if (k == match_key) {
                        flag = true
                    }
                }
                if (!flag) {
                    remote_list_data.add(s)
                }
            }
            if (user.master != null) {
                for (modifiedCheck in user.master!!) {
                    if (modifiedCheck["Home"] == home) {
                        modifiedCheck["Status"] = "modified"
                    }

                }
            }
            user.sense_remote = remote_list_data
            user.userId = userId
            dynamoDBMapper!!.save<RulesDO>(user)
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }


    fun homeSQLtoDYNAMO(data: MutableList<RoomModelJson>, userId: String) {
        val roomList: MutableList<MutableMap<String, String>> = ArrayList()
        val rulesDO = RulesDO()
        var homeName: String? = null
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            for (homes in data) {
                val map = HashMap<String, String>()
                map["bg"] = homes.bgUrl
                map["name"] = homes.name
                map["type"] = homes.type
                map["access"] = homes.sharedHome
                map["icon"] = homes.roomIcon.toString()
                map["latitude"] = homes.homeLatitude.toString()
                map["longitude"] = homes.homeLongitude.toString()
                map["location"] = homes.homeLocation.toString()
                map["status"] = "modified"
                homeName = homes.name
                roomList.add(map)
            }
            if (user == null) {
                rulesDO.userId = userId
                rulesDO.homes = roomList
                dynamoDBMapper!!.save<RulesDO>(rulesDO)
            } else {
                if (user.homes == null) {
                    user.homes = roomList
                    dynamoDBMapper!!.save<RulesDO>(user)
                } else {
                    user.homes = roomList

                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == Constant.HOME) {
                                modifiedCheck["Status"] = "modified"
                            }

                        }
                    }

                    dynamoDBMapper!!.save<RulesDO>(user)
                }
            }
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }

    }

    fun shareDevices(sharemail: String, home: String): Boolean {
        try {
            val guestFirstName = getFirstName(sharemail)
            Common.sharingUserName = guestFirstName
            val owner = getUser()
            if (owner == null) {
                returnError()
            } else {
                val sharedMasterData = HashMap<String, String>()
                sharedMasterData["Access"] = "invite"
                sharedMasterData["Home"] = home
                sharedMasterData["Email"] = sharemail
                sharedMasterData["Name"] = guestFirstName
                sharedMasterData["Status"] = "modified"
                if (owner.master != null) {
                    var sharedFlagMasterAdd = true
                    for (share in owner.master!!) {
                        if ((share["Email"] == sharemail) and (share["Home"] == home)) {
                            share["Status"] = "modified"
                            sharedFlagMasterAdd = false
                        }
                    }
                    if (sharedFlagMasterAdd) {
                        val sharedAccessForMaster = owner.master
                        sharedAccessForMaster?.add(sharedMasterData)
                        owner.master = sharedAccessForMaster
                    }
                } else {
                    val newGuestList: MutableList<MutableMap<String, String>> = ArrayList()
                    newGuestList.add(sharedMasterData)
                    owner.master = newGuestList
                }
                dynamoDBMapper!!.save<RulesDO>(owner)
            }
            return true
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
            return false
        }
    }

    fun returnError(): Boolean? {
        return false
    }

    fun deleteGuestAccessmaster(home: String, userId: String, email: String?) {
        val owner = dynamoDBMapper!!.load(RulesDO::class.java, userId)
        try {
            if (owner != null) {
                if (owner.master != null) {
                    for (sharing in owner.master!!) {
                        if ((sharing["Home"] == home) and (sharing["Email"] == email)) {
                            sharing["Status"] = "deleted"
                        }
                    }
                    dynamoDBMapper!!.save<RulesDO>(owner)
                }
            }
        } catch (e: Exception) {
            Log.i("Error", "MASTER_DELETE_ERROR" + e)

        }

    }

    fun deleteGuestAccessFromMaster(home: String, userId: String) {
        val owner = dynamoDBMapper!!.load(RulesDO::class.java, userId)
        try {
            if (owner != null) {
                if (owner.guest != null) {
                    for (sharing in owner.guest!!) {
                        if (sharing["Home"] == home) {
                            sharing["Status"] = "deleted"
                        }
                    }
                    dynamoDBMapper!!.save<RulesDO>(owner)
                }
            }
        } catch (e: Exception) {
            Log.i("Error", "GUEST_DELETE_ERROR" + e)
        }
    }

    fun deleteHomeDynamo(data: MutableList<RoomModelJson>, userId: String, home: String) {
        try {
            val roomList: MutableList<MutableMap<String, String>> = ArrayList()
            val SceneList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val ScheduleList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val devices: MutableList<MutableMap<String, String>> = ArrayList()
            val allLoads: MutableList<MutableList<MutableMap<String, String>>> = ArrayList()
            val rulesDO = RulesDO()

            val user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            for (homes in data) {
                val map = HashMap<String, String>()
                map["bg"] = homes.bgUrl
                map["name"] = homes.name
                map["type"] = homes.type
                map["access"] = homes.sharedHome
                map["icon"] = homes.roomIcon.toString()
                map["latitude"] = homes.homeLatitude.toString()
                map["longitude"] = homes.homeLongitude.toString()
                map["location"] = homes.homeLocation.toString()
                roomList.add(map)
            }
            if (user == null) {
                rulesDO.userId = userId
                rulesDO.homes = roomList
                dynamoDBMapper!!.save<RulesDO>(user)
            } else {
                user.homes = roomList

                if (user.devices != null) {
                    for (d in user.devices!!) {
                        if (d["home"] != home) {
                            devices.add(d)
                            if (user.loads != null) {
                                for (l in user.loads!!) {
                                    var flag = false
                                    for (load in l) {
                                        if ((load["type"] == "device") and (load["name"] == d["id"])) {
                                            flag = true
                                            break
                                        }
                                    }
                                    if (!flag) {
                                        allLoads.add(l)
                                        break
                                    }
                                }

                            }
                        }
                    }
                    user.devices = devices
                    user.loads = allLoads


                }
                if (user.scenes != null) {
                    for (s in user.scenes!!) {
                        val keys = s.keys
                        var flag = false
                        var header: String? = null
                        for (k in keys) {
                            header = k
                        }
                        val scene = s[header]
                        for (s in scene!!) {
                            if ((s["type"] == "scene") and (s["home"] == home)) {
                                flag = true
                                break
                            }
                        }
                        if (!flag) {
                            SceneList.add(s)
                        }
                    }
                    user.scenes = SceneList
                }
                if (user.schedules == null) {
                    for (s in user.schedules!!) {
                        val keys = s.keys
                        var flag = false
                        var header: String? = null
                        for (k in keys) {
                            header = k
                        }
                        val schedule = s[header]
                        for (s in schedule!!) {
                            if ((s["type"] == "schedule") and (s["home"] == home)) {
                                flag = true
                                break
                            }
                        }
                        if (!flag) {
                            ScheduleList.add(s)
                        }
                    }
                    user.schedules = ScheduleList
                }
                if (user.master != null) {
                    for (modifiedCheck in user.master!!) {
                        if (modifiedCheck["Home"] == Constant.HOME) {
                            modifiedCheck["Status"] = "modified"
                        }

                    }
                }
                dynamoDBMapper!!.save<RulesDO>(user)
            }
        } catch (e: Exception) {
            Log.i("Error", "GUEST_DELETE_ERROR" + e)
        }
    }

    fun getRulesTable(): RulesDO {
        try {
            val rules = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            if (rules == null) {
                val Rules = RulesDO()
                Rules.userId = "ERROR"
                return Rules
            } else {
                return rules
            }
        } catch (e: Exception) {
            val Rules = RulesDO()
            Rules.userId = "ERROR"
            return Rules
        }

    }

    fun saveRulesTable(table: RulesDO): Boolean? {
        try {
            dynamoDBMapper!!.save<RulesDO>(table)
            return true
        } catch (e: Exception) {
            return false
        }
    }


    fun insertDeviceLoads(device: AuraSwitch) {
        try {
            val allLoads: MutableList<MutableList<MutableMap<String, String>>> = arrayListOf()
            val loadList: MutableList<MutableMap<String, String>> = arrayListOf()
            val devices: MutableList<MutableMap<String, String>> = arrayListOf()
            val user = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            val newUser = RulesDO()
            val map = HashMap<String, String>()
            val deviceId = device.uiud.substring(0, Math.min(device.uiud.length, 12))
            map["deviceType"] = if (device.mdl == 15) "IR_device" else "device"
            map["id"] = deviceId
            map["name"] = device.name
            map["thing"] = device.thing!!
            map["uiud"] = device.uiud
            map["mdl"] = device.mdl.toString()
            map["room"] = device.room
            map["status"] = "modified"
            map["home"] = Constant.HOME!!
            devices.add(map)
            val m = HashMap<String, String>()
            m["device"] = deviceId
            m["type"] = "device"
            m["status"] = "modified"
            loadList.add(m)
            for (load in device.loads) {
                val hashmap = HashMap<String, String>()
                hashmap["name"] = load.name!!
                hashmap["dimmable"] = load.dimmable.toString()
                hashmap["favourite"] = load.favourite.toString()
                hashmap["icon"] = load.icon.toString()
                hashmap["index"] = load.index.toString()
                hashmap["device"] = deviceId
                hashmap["loadType"] = load.loadType.toString()
                hashmap["mdl"] = device.mdl.toString()
                if (load.type == "Curtain") {
                    hashmap["curtainStatus0"] = load.curtainState0.toString()
                    hashmap["curtainStatus1"] = load.curtainState1.toString()
                    hashmap["curtainSetState"] = load.curtainState
                    hashmap["type"] = load.type.toString()
                } else if (load.type == "rgbDevice") {
                    hashmap["type"] = load.type.toString()
                } else if (load.type == "tunableDevice") {
                    hashmap["type"] = load.type.toString()
                } else {
                    hashmap["type"] = "load"
                }
                hashmap["status"] = "modified"
                loadList.add(hashmap)
            }
            allLoads.add(loadList)
            if (user == null) {
                newUser.userId = Constant.IDENTITY_ID
                newUser.devices = devices
                newUser.loads = allLoads
            } else {
                if (user.devices == null) {
                    user.devices = devices
                } else {
                    for (d in user.devices!!) {
                        if (d["id"] != deviceId) {
                            devices.add(d)
                        }
                    }
                    user.devices = devices
                }

                if (user.loads == null) {
                    user.loads = allLoads
                } else {
                    for (l in user.loads!!) {
                        var flag = false
                        for (load in l) {
                            if ((load["type"] == "load" || load["type"] == "device") && load["device"] == deviceId) {
                                flag = true
                                break
                            }
                        }
                        if (!flag) {
                            allLoads.add(l)
                        }
                    }
                    user.loads = allLoads
                }

            }
            if (user!!.master != null) {
                for (modifiedCheck in user.master!!) {
                    if (modifiedCheck["Home"] == Constant.HOME) {
                        modifiedCheck["Status"] = "modified"

                    }
                }
            }
            dynamoDBMapper!!.save<RulesDO>(user)
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")

        }
    }

    fun insertUniversalDevice(device: AuraSwitch) {
        try {
            val devices: MutableList<MutableMap<String, String>> = arrayListOf()
            val user = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            val newUser = RulesDO()
            val map = HashMap<String, String>()
            val deviceId = device.uiud.substring(0, Math.min(device.uiud.length, 12))
            map["deviceType"] = "IR_device"
            map["id"] = deviceId
            map["name"] = device.name
            map["thing"] = device.thing!!
            map["uiud"] = device.uiud
            map["mdl"] = "15"
            map["room"] = device.room
            map["status"] = "modified"
            map["home"] = Constant.HOME!!
            devices.add(map)
            if (user == null) {
                newUser.userId = Constant.IDENTITY_ID
                newUser.devices = devices
            } else {
                if (user.devices == null) {
                    user.devices = devices
                } else {
                    for (d in user.devices!!) {
                        if (d["id"] != deviceId) {
                            devices.add(d)
                        }
                    }
                    user.devices = devices
                }
            }
            if (user!!.master != null) {
                for (modifiedCheck in user.master!!) {
                    if (modifiedCheck["Home"] == Constant.HOME) {
                        modifiedCheck["Status"] = "modified"

                    }

                }
            }
            dynamoDBMapper!!.save<RulesDO>(user)
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")

        }
    }

    fun insertSenseDevice(sense: RemoteModel) {
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            val devices: MutableList<MutableMap<String, String>> = ArrayList()
            val map = HashMap<String, String>()
            val deviceId = sense.senseMacId
            map["id"] = deviceId.toString()
            map["name"] = sense.aura_sence_name.toString()
            map["thing"] = sense.scense_thing.toString()
            map["uiud"] = sense.sense_uiud.toString()
            map["room"] = sense.room.toString()
            map["home"] = Constant.HOME!!
            map["role"] = sense.role
            map["status"] = "modified"
            devices.add(map)
            if (user != null) {
                if ((user.senseDevice == null) || (user.senseDevice!!.size == 0)) {
                    user.senseDevice = devices
                } else {
                    for (d in user.senseDevice!!) {
                        if (d["id"] != deviceId) {
                            devices.add(d)
                        }
                    }
                    user.senseDevice = devices
                }
            }
            if (user!!.master != null) {
                for (modifiedCheck in user.master!!) {
                    if (modifiedCheck["Home"] == Constant.HOME) {
                        modifiedCheck["Status"] = "modified"

                    }

                }
            }
            dynamoDBMapper!!.save<RulesDO>(user)
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }

    fun insertButtonDevice(button: ButtonModel) {
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            val devices: MutableList<MutableMap<String, String>> = ArrayList()
            val map = HashMap<String, String>()
            val deviceId = button.unicastAddress
            map["id"] = deviceId.toString()
            map["name"] = button.auraButtonName.toString()
            map["thing"] = button.thing.toString()
            map["uiud"] = button.senseUiud.toString()
            map["room"] = button.room.toString()
            map["home"] = Constant.HOME!!.trim()
            map["senseDevice"] = button.senseName.toString()
            map["senseThing"] = button.thing.toString()

            devices.add(map)
            if (user != null) {
                if ((user.buttonDevice == null) || (user.buttonDevice!!.size == 0)) {
                    user.buttonDevice = devices
                } else {
                    for (d in user.buttonDevice!!) {
                        if (d["id"] != deviceId) {
                            devices.add(d)
                        }
                    }
                    user.buttonDevice = devices
                }
            }
            if (user!!.master != null) {
                for (modifiedCheck in user.master!!) {
                    if (modifiedCheck["Home"] == Constant.HOME) {
                        modifiedCheck["Status"] = "modified"

                    }

                }
            }
            dynamoDBMapper!!.save<RulesDO>(user)
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }

    fun storeSceneControllerLoad(button: ButtonModel) {
        try {
            var gson = Gson()
            val user = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            val newUser = RulesDO()
            var SceneList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val Scene = HashMap<String, MutableList<MutableMap<String, String>>>()
            val deviceList: MutableList<MutableMap<String, String>> = ArrayList()

            for (room in button.load) {
                for (device in room.deviceList) {
                    val map = HashMap<String, String>()
                    if (device.deviceName != "Scene") {
                        map["type"] = "device"
                        map["deviceName"] = device.deviceName
                    } else {
                        map["type"] = device.deviceName
                        map["deviceName"] = device.name
                    }
                    map["level"] = device.dimVal.toString()
                    map["index"] = device.index.toString()
                    map["state"] = device.isTurnOn.toString()
                    deviceList.add(map)
                }
            }

            val map = HashMap<String, String>()
            map["type"] = "buttonControl"
            map["buttonName"] = button.auraButtonName.toString()
            map["unicastAddress"] = button.unicastAddress.toString()
            map["buttonId"] = button.buttonId.toString()
            map["home"] = Constant.HOME.toString()
            map["room"] = button.room.toString()
            map["senseThing"] = button.thing.toString()
            map["buttonTap"] = button.buttonTapName.toString()
            map["senseDevice"] = button.senseName.toString()
            map["senseUiud"] = button.senseUiud.toString()
            deviceList.add(map)
            Scene[button.buttonId.toString()] = deviceList

            if (user == null) {
                SceneList.add(Scene)
                newUser.buttonControl = SceneList
                newUser.userId = Constant.IDENTITY_ID
                dynamoDBMapper!!.save<RulesDO>(newUser)
            } else {
                if ((user.buttonControl == null) || (user.buttonControl!!.size == 0)) {
                    SceneList.add(Scene)
                    user.buttonControl = SceneList
                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == Constant.HOME.toString()) {
                                modifiedCheck["Status"] = "modified"
                            }

                        }
                    }
                    dynamoDBMapper!!.save<RulesDO>(user)
                } else {
                    if (button.type == "edit") {
                        for (s in user.scenes!!) {
                            val keys = s.keys
                            var flag = false
                            for (k in keys) {
                                if (k == button.auraButtonName) {
                                    flag = true
                                }
                            }
                            if (!flag) {
                                SceneList.add(s)
                            }
                        }

                    } else {
                        SceneList = user.buttonControl!!
                    }
                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == Constant.HOME.toString()) {
                                modifiedCheck["Status"] = "modified"
                            }
                        }
                    }
                    SceneList.add(Scene)
                    user.buttonControl = SceneList
                    dynamoDBMapper!!.save<RulesDO>(user)
                }
            }
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }

    fun deleteButtonDevice(button: ButtonModel) {
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            val deviceId = button.unicastAddress
            val devices: MutableList<MutableMap<String, String>> = ArrayList()
            val SceneList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            if (user != null) {
                if (user.buttonDevice != null) {
                    for (d in user.devices!!) {
                        if (d["id"] != deviceId) {
                            devices.add(d)
                        }
                    }
                    user.buttonDevice = devices
                }

                if (user.buttonControl != null && user.buttonControl!!.size > 0) {
                    for (s in user.buttonControl!!) {
                        val keys = s.keys
                        var flag = false
                        for (k in keys) {
                            if (k == button.auraButtonName) {
                                flag = true
                            }
                        }
                        if (!flag) {
                            SceneList.add(s)
                        }
                    }

                    user.buttonControl = SceneList
                }
                if (user.master != null) {
                    for (modifiedCheck in user.master!!) {
                        if (modifiedCheck["Home"] == Constant.HOME!!) {
                            modifiedCheck["Status"] = "modified"
                        }

                    }
                }
                dynamoDBMapper!!.save<RulesDO>(user)
            }
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }

    fun deleteDevice(device: AuraSwitch,type: String): String {
        try {
            // var gson = Gson()
            val user = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            val deviceId = device.uiud.substring(0, Math.min(device.uiud.length, 12))
            val devices: MutableList<MutableMap<String, String>> = ArrayList()
            val allLoads: MutableList<MutableList<MutableMap<String, String>>> = ArrayList()
            if (user != null) {
                if (user.devices != null) {
                    for (d in user.devices!!) {
                        if (d["id"] != deviceId) {
                            devices.add(d)
                        }
                    }
                    user.devices = devices
                }

                if(type !=Constant.UNIVERSAL_REMOTE){
                    if (user.loads != null) {
                        for (l in user.loads!!) {
                            var flag = false
                            for (load in l) {
                                if ((load["type"] == "load" || load["type"] == "device") && load["device"] == deviceId) {
                                    flag = true
                                }
                            }
                            if (!flag) {
                                allLoads.add(l)
                            }
                        }

                        user.loads = allLoads
                    }
                }

                if (user.master != null) {
                    for (modifiedCheck in user.master!!) {
                        if (modifiedCheck["Home"] == Constant.HOME!!) {
                            modifiedCheck["Status"] = "modified"
                        }

                    }
                }
                dynamoDBMapper!!.save<RulesDO>(user)
            }


            Log.i(LOG_TAG, "User Device Updated")
            return "SUCCESS"
        } catch (e: Exception) {
            return "ERROR"
        }
    }

    fun deleteSenseDevice(senseDevice: RemoteModel): String {
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            val deviceId = senseDevice.sense_uiud!!.substring(0, Math.min(senseDevice.sense_uiud!!.length, 12))
            val devices: MutableList<MutableMap<String, String>> = ArrayList()
            if (user != null) {
                if (user.senseDevice != null) {
                    for (d in user.senseDevice!!) {
                        if (d["id"] != deviceId) {
                            devices.add(d)
                        }
                    }
                    user.senseDevice = devices
                }
                if (user.master != null) {
                    for (modifiedCheck in user.master!!) {
                        if (modifiedCheck["Home"] == Constant.HOME!!) {
                            modifiedCheck["Status"] = "modified"
                        }

                    }
                }
                dynamoDBMapper!!.save<RulesDO>(user)
            }
            return "SUCCESS"
        } catch (e: Exception) {
            return "ERROR"
        }

    }

    fun deleteHomeSenseDevice(senseDevice: MutableList<RemoteModel>): String {
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, Constant.IDENTITY_ID)
            val devices: MutableList<MutableMap<String, String>> = ArrayList()
            if (user != null) {
                if (user.senseDevice != null) {
                    for (d in user.senseDevice!!) {
                        for (sense in senseDevice) {
                            if (d["id"] != sense.sense_uiud!!.substring(0, Math.min(sense.sense_uiud!!.length, 12)))
                                devices.add(d)
                        }
                    }
                    user.senseDevice = devices
                }
                if (user.master != null) {
                    for (modifiedCheck in user.master!!) {
                        if (modifiedCheck["Home"] == Constant.HOME!!) {
                            modifiedCheck["Status"] = "modified"
                        }

                    }
                }
                dynamoDBMapper!!.save<RulesDO>(user)
            }
            return "SUCCESS"
        } catch (e: Exception) {
            return "ERROR"
        }

    }

    fun deleteUserSenseMotion(userId: String, home: String, name: String, senseMacId: String?, senseThing: String, senseMode: String) {
        try {
            val user = dynamoDBMapper!!.load(RulesDO::class.java, userId)
            val senseList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val generateList: MutableList<MutableMap<String, MutableList<MutableMap<String, String>>>> = ArrayList()
            val listMap = HashMap<String, MutableList<MutableMap<String, String>>>()
            val createList: MutableList<MutableMap<String, String>> = ArrayList()
            val map_config = HashMap<String, String>()
            map_config["id"] = senseMacId.toString()
            map_config["name"] = name
            map_config["oldName"] = name
            map_config["operation"] = "delete"
            map_config["type"] = "automation"
            createList.add(map_config)
            listMap[senseThing] = createList
            if (user == null) {
            } else {
                if (user.sense_motion == null) {
                } else {
                    for (s in user.sense_motion!!) {
                        val keys = s.keys
                        var flag = false
                        var pair = ""
                        for (k in keys) {
                            if (k == name) {
                                pair = k
                                flag = true
                            }
                        }
                        if (!flag) {
                            senseList.add(s)
                        }

                    }

                    /*
                    Update Configuration
                     */
                    if (senseMode.contains(Constant.OFFLINE_MODE)) {
                        if (user.configurationList == null || user.configurationList!!.size == 0) {
                            generateList.add(listMap)
                            user.configurationList = generateList
                            dynamoDBMapper!!.save<RulesDO>(user)
                        } else {
                            var flag = false
                            for (data in user.configurationList!!) {
                                if (data.containsKey(senseThing)) {
                                    for (mapExist in data) {
                                        createList.addAll(mapExist.value)
                                    }
                                    data[senseThing] = createList
                                    flag = true
                                    break
                                }
                            }
                            if (!flag) {
                                generateList.add(listMap)
                                user.configurationList!!.addAll(generateList)
                            }
                            if (flag) {
                                user.configurationList = user.configurationList
                            }
                        }
                    }
                    if (user.master != null) {
                        for (modifiedCheck in user.master!!) {
                            if (modifiedCheck["Home"] == home) {
                                modifiedCheck["Status"] = "modified"
                            }

                        }
                    }
                    user.sense_motion = senseList
                    user.userId = userId
                    dynamoDBMapper!!.save<RulesDO>(user)
                }
            }
            Log.i(LOG_TAG, "User Device Updated")
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
    }

    fun getUserScanDetail(email: String?): User {
        val user = User()
        try {
            val hasMap = HashMap<String, AttributeValue>()
            hasMap[":userId"] = AttributeValue().withS(email)
            val dynamoDBClient = AmazonDynamoDBAsyncClient(credentialsProvider)
            val queryRequest = QueryRequest().withTableName("wozartaura-mobilehub-1863763842-Rules")
            queryRequest.withKeyConditionExpression("userId = :userId").withExpressionAttributeValues(hasMap)
            val scanResult = dynamoDBClient.query(queryRequest)
            for (data_ in scanResult.items) {
                user.email = data_["Email"]?.s
                user.firstName = data_["FirstName"]?.s ?: ""
                user.lastName = data_["LastName"]?.s ?: ""
                user.user_id = data_["userId"]?.s
                user.mobile = data_["PhoneNumber"]?.s ?: ""
                user.gName = data_["Name"]?.s ?: user.firstName + " " + user.lastName
            }
        } catch (e: Exception) {
            Log.i(LOG_TAG, "Error : $e")
        }
        return user
    }
}