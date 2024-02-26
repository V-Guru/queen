package com.wozart.aura.entity.model.aura

import java.io.Serializable


/**
 * Created by Saif on 07/08/20.
 * Wozart Technology Pvt Ltd
 * mds71964@gmail.com
 */

class AuraSceneButton : Serializable {
    var buttonName : String ?= null
    var buttonId = 0
    var buttonIcon = 0
    var singleDoubleLong : MutableList<AuraLongSingleDouble> = ArrayList()
    var longSingleDouble = AuraLongSingleDouble()

    fun buttonData() : MutableList<AuraSceneButton>{
        val buttonList : MutableList<AuraSceneButton> = ArrayList()
        val button1 = AuraSceneButton()
        val button2 = AuraSceneButton()
        val button3 = AuraSceneButton()
        val button4 = AuraSceneButton()

        button1.buttonName = "Button 1"
        button1.buttonIcon = 0
        button1.buttonId = 1
        button1.singleDoubleLong = longSingleDouble.auraButton()
        buttonList.add(button1)

        button2.buttonName = "Button 2"
        button2.buttonIcon = 1
        button2.buttonId = 2
        button2.singleDoubleLong = longSingleDouble.auraButton()
        buttonList.add(button2)

        button3.buttonName = "Button 3"
        button3.buttonIcon = 2
        button3.buttonId = 3
        button3.singleDoubleLong = longSingleDouble.auraButton()
        buttonList.add(button3)

        button4.buttonName = "Button 4"
        button4.buttonIcon = 3
        button4.buttonId = 4
        button4.singleDoubleLong = longSingleDouble.auraButton()
        buttonList.add(button4)

        return buttonList
    }

}