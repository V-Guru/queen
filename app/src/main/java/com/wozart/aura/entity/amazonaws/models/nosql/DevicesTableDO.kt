package aura.wozart.com.aura.entity.amazonaws.models.nosql

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 30/04/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/

@DynamoDBTable(tableName = "wozartaura-mobilehub-1863763842-DevicesTable")
class DevicesTableDO {
    @get:DynamoDBHashKey(attributeName = "DeviceId")
    @get:DynamoDBAttribute(attributeName = "DeviceId")
    var deviceId: String? = null
    @get:DynamoDBAttribute(attributeName = "Home")
    var home: String? = null
    @get:DynamoDBAttribute(attributeName = "Loads")
    var loads: List<String>? = null
    @get:DynamoDBAttribute(attributeName = "Master")
    var master: String? = null
    @get:DynamoDBAttribute(attributeName = "Name")
    var name: String? = null
    @get:DynamoDBAttribute(attributeName = "Room")
    var room: String? = null
    @get:DynamoDBAttribute(attributeName = "Slave")
    var slave: List<String>? = null
    @get:DynamoDBAttribute(attributeName = "Thing")
    var thing: String? = null
    @get:DynamoDBAttribute(attributeName = "UIUD")
    var uiud: String? = null

}