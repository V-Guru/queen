package com.wozart.aura.data.model

class Messages {
    var notifyHome :String ?= null
    var title : String ?= null
    var notificationDate : String ?= null
    var notificationTime : String ?= null
    var notificationType : String ?= null
    var mode :String ?= null
    var errorMessage : String ?= null
    var notificationError : MutableList<DeviceMessage> = ArrayList()
}