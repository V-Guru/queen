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

@DynamoDBTable(tableName = "wozartaura-mobilehub-1863763842-ThingTable")
class ThingTableDO {
    @get:DynamoDBHashKey(attributeName = "Thing")
    @get:DynamoDBAttribute(attributeName = "Thing")
    var thing: String? = null
    @get:DynamoDBAttribute(attributeName = "Available")
    var available: Double? = null
    @get:DynamoDBAttribute(attributeName = "Certificate")
    var certificate: String? = null
    @get:DynamoDBAttribute(attributeName = "PrivateKey")
    var privateKey: String? = null
    @get:DynamoDBAttribute(attributeName = "Region")
    var region: String? = null

}