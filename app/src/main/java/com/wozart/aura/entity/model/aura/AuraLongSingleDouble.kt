package com.wozart.aura.entity.model.aura

import java.io.Serializable


/**
 * Created by Saif on 07/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */
class AuraLongSingleDouble : Serializable {
    var btnName : String ?= null
    var btnIcon = 0
    var btnIndex = 0
    var btnId = 0

    fun auraButton() : MutableList<AuraLongSingleDouble>{
        val btnList : MutableList<AuraLongSingleDouble> = ArrayList()
        val singlePress = AuraLongSingleDouble()
        val doublePress = AuraLongSingleDouble()
        val longPress = AuraLongSingleDouble()

        singlePress.btnName = "Single Tap"
        singlePress.btnIcon = 0
        singlePress.btnId = 1
        singlePress.btnIndex = 0
        btnList.add(singlePress)

        doublePress.btnName = "Double Tap"
        doublePress.btnIndex = 1
        doublePress.btnId = 2
        doublePress.btnIcon = 1
        btnList.add(doublePress)

        longPress.btnName = "Long Tap"
        longPress.btnIcon = 2
        longPress.btnId = 3
        longPress.btnIndex = 2
        btnList.add(longPress)

        return btnList
    }

}