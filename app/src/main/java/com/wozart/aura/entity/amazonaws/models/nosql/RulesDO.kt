package com.wozart.aura.entity.amazonaws.models.nosql


import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable


@DynamoDBTable(tableName = "wozartaura-mobilehub-1863763842-Rules")
open class RulesDO {
    @get:DynamoDBHashKey(attributeName = "userId")
    @get:DynamoDBAttribute(attributeName = "userId")
    var userId: String? = null
    @get:DynamoDBAttribute(attributeName = "_id")
    var _id : String ?= null
    @get:DynamoDBAttribute(attributeName = "Configuration")
    var configurationList : MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>> ?= null
    @get:DynamoDBAttribute(attributeName = "Devices")
    var devices:MutableList<MutableMap<String, String>>? = null
    @get:DynamoDBAttribute(attributeName = "SenseDevice")
    var senseDevice:MutableList<MutableMap<String, String>>? = null
    @get:DynamoDBAttribute(attributeName = "ButtonDevices")
    var buttonDevice:MutableList<MutableMap<String, String>>? = null
    @get:DynamoDBAttribute(attributeName = "Email")
    var email: String? = null
    @get:DynamoDBAttribute(attributeName = "Guest")
    var guest: MutableList<MutableMap<String, String>>? = null
    @get:DynamoDBAttribute(attributeName = "Homes")
    var homes: MutableList<MutableMap<String, String>>? = null
    @get:DynamoDBAttribute(attributeName = "AuraSenseRemote")
    var sense_remote: MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>>? = null
    @get:DynamoDBAttribute(attributeName = "SenseMotion")
    var sense_motion: MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>>? = null
    @get:DynamoDBAttribute(attributeName = "Loads")
    var loads: MutableList<MutableList<MutableMap<String,String>>>? = null
    @get:DynamoDBAttribute(attributeName = "Master")
    var master: MutableList<MutableMap<String, String>>? = null
    @get:DynamoDBAttribute(attributeName = "Name")
    var name: String? = null
    @get:DynamoDBAttribute(attributeName = "Notifications")
    var notifications: List<String>? = null
    @get:DynamoDBAttribute(attributeName = "Message")
    var _messages:MutableList<MutableMap<String,String>>? = null
    @get:DynamoDBAttribute(attributeName = "Scenes")
    var scenes: MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>>? = null
    @get:DynamoDBAttribute(attributeName = "Schedules")
    var schedules: MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>> ? = null
    @get:DynamoDBAttribute(attributeName = "ButtonControl")
    var buttonControl: MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>>? = null
    @get:DynamoDBAttribute(attributeName = "PhoneNumber")
    var phoneNumber: String? = null
    @get:DynamoDBAttribute(attributeName = "Time_Zone_Local")
    var timeZone: String? = null
    @get:DynamoDBAttribute(attributeName = "UserThing")
    var userThing: String? = null
    @get:DynamoDBAttribute(attributeName = "FirstName")
    var firstName: String? = null
    @get:DynamoDBAttribute(attributeName = "LastName")
    var LastName: String? = null
    @get:DynamoDBAttribute(attributeName = "MasterDevices")
    var masterDevices: MutableList<MutableMap<String, String>>? = null
    @get:DynamoDBAttribute(attributeName = "MasterHomes")
    var masterHomes: MutableList<MutableMap<String, String>>? = null
    @get:DynamoDBAttribute(attributeName = "MasterLoads")
    var masterLoads: MutableList<MutableList<MutableMap<String,String>>>? = null
    @get:DynamoDBAttribute(attributeName = "MasterScenes")
    var masterScenes:  MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>>?  = null
    @get:DynamoDBAttribute(attributeName = "MasterSchedules")
    var masterSchedules: MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>>? = null
    @get:DynamoDBAttribute(attributeName = "Verified")
    var verified: String? = null
    @get:DynamoDBAttribute(attributeName = "AppVersion")
    var appVersion: String? = null
    @get:DynamoDBAttribute(attributeName = "MasterAuraSenseRemote")
    var masterSenseRemote: MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>>? = null
    @get:DynamoDBAttribute(attributeName = "MasterSenseMotion")
    var masterSenseMotion: MutableList<MutableMap<String,MutableList<MutableMap<String,String>>>>? = null
    @get:DynamoDBAttribute(attributeName = "MasterSenseDevice")
    var masterSenseDevice:MutableList<MutableMap<String, String>>? = null
}