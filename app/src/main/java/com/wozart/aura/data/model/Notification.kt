package com.wozart.aura.data.model

/**
 * Created by Drona Sahoo on 3/20/2018.
 */

class Notification {
    var notificationTitle: String? = null
    var sharedUserId: String? = null
    var notificationMessage: String? = null
    var sharedHome: String ?= null
    var access: String  ?= null
    var masterName: String? = null
    var status: Int = 0
    var ownerId : String ?= null
    var notificationDate : String ?= null
    var notificationTime : String ?= null
    var mode :String ?= null
    var notificationError : MutableList<MutableMap<String,String>> ?= null
}
