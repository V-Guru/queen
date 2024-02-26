package com.wozart.aura.data.model

import com.wozart.aura.entity.model.aura.AuraSenseConfigure


/**
 * Created by Saif on 12/08/21.
 * mds71964@gmail.com
 */
class SenseRoomSeperation {
    var roomName : String ?= null
    var senseLoadList : MutableList<AuraSenseConfigure> = arrayListOf()
}