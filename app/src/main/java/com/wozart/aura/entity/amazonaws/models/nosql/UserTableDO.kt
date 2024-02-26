package com.wozart.aura.entity.amazonaws.models.nosql

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

@DynamoDBTable(tableName = "wozartaura-mobilehub-1863763842-UserTable")
class UserTableDO {
    @get:DynamoDBHashKey(attributeName = "UserId")
    @get:DynamoDBAttribute(attributeName = "UserId")
    var userId: String? = null
    @get:DynamoDBAttribute(attributeName = "Devices")
    var devices: MutableList<String>? = null
    @get:DynamoDBAttribute(attributeName = "Email")
    var email: String? = null
    @get:DynamoDBAttribute(attributeName = "Name")
    var name: String? = null
    @get:DynamoDBAttribute(attributeName = "SharedAccess")
    var sharedAccess: MutableList<MutableMap<String, String>>? = null

}
