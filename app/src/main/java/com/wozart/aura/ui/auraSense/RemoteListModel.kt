package com.wozart.aura.ui.auraSense

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-01-31
 * Description :
 * Revision History :
 * ____________________________________________________________________________

 *****************************************************************************/
class RemoteListModel {
    var auraSenseName : String ?= null
    var brandName : String ?= null
    var modelNumber : String ?= null
    var remoteName : String ?= null
    var dynamicRemoteIconList : MutableList<RemoteIconModel> = arrayListOf()
    var keyNumbers : MutableList<RemoteIconModel> = arrayListOf()
    var favChannelList :MutableList<RemoteIconModel> = arrayListOf()
    var remoteFavourite = true
    var typeAppliances : String ?= null
    var remoteLocation :String ?= null
    var senseIp : String= ""
    var zmote_ip : String ?= null
    var deviceType : String ?= null
    var senseUiud : String ?= null
    var dataType : String ?= null
    var home : String ?= null
    var senseThing : String ?= null
    var isDownloadedRemote : Boolean = false
}