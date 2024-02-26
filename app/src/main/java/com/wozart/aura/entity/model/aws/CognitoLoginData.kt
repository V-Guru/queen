package com.wozart.aura.entity.model.aws


/**
 * Created by Saif on 13/03/21.
 * mds71964@gmail.com
 */
class CognitoLoginData {
    var sub : String ?= null
    var identities : MutableList<CognitoLoginData> = arrayListOf()
    var userId : String ?= null
    var cognitoUserId : String ?= null
    var emailVerified : String ?= null
    var email : String ?= null
    var token : String ?= null
    var providerName : String ?= null
    var providerType : String ?= null
    var email_verified : Boolean = false
    var name  =""
    var nickname :String ?= null
    var given_name =""
    var picture :String = ""

}