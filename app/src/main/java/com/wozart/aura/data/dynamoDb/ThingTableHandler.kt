package com.wozart.aura.data.dynamoDb

import android.util.Log
import aura.wozart.com.aura.entity.amazonaws.models.nosql.ThingTableDO
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.facebook.FacebookSdk
import com.facebook.FacebookSdk.getApplicationContext
import java.util.HashMap

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
class ThingTableHandler {
    private val LOG_TAG = ThingTableHandler::class.java.simpleName

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

    fun searchAvailableDevices(): ThingTableDO? {
        try {
            var availableDevice: ThingTableDO? = null
            val availableThings = HashMap<String, AttributeValue>()
            availableThings[":val1"] = AttributeValue().withN("1")

            val scanExpression = DynamoDBScanExpression()
                    .withFilterExpression("Available = :val1").withExpressionAttributeValues(availableThings)

            val scanResult = dynamoDBMapper!!.scan<ThingTableDO>(ThingTableDO::class.java, scanExpression)
            if (scanResult != null) {
                if (scanResult.isEmpty()) {
                    Log.d(LOG_TAG, "No Devices available on AWS: $scanResult")
                    return null
                } else {
                    availableDevice = scanResult[0]
                    Log.d(LOG_TAG, "Received Thing Name: " + availableDevice!!.thing)
                }
            }
            return availableDevice
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return null
        }

    }

    fun updateAvailability(device: String) {
        try {
            val changeAvailability = dynamoDBMapper!!.load<ThingTableDO>(ThingTableDO::class.java, device)
            changeAvailability.available = 0.0
            dynamoDBMapper!!.save<ThingTableDO>(changeAvailability)
            Log.d(LOG_TAG, "Availability changed ")


        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")

        }


    }

    fun thingDetails(thing: String): ThingTableDO? {
        var thingTableDO : ThingTableDO
        try {
            thingTableDO = dynamoDBMapper!!.load<ThingTableDO>(ThingTableDO::class.java, thing)
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error : $e")
            return null
        }

        return thingTableDO
    }
}