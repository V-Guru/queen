package com.wozart.aura.entity.model.aura

import com.wozart.aura.utilities.Constant
import java.io.Serializable

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 13/02/20
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class AuraSenseConfigure : Serializable {
    var auraSenseName : String ?= null
    var auraSenseIcon = 0
    var auraSenseIndex = 0
    var auraSenseFavorite : Boolean = false
    var senseDeviceName : String ?= null
    var intensity : String ?= null
    var range = 0
    var type = "Motion"
    var above : Boolean = false
    var below : Boolean = false
    var senseMacId : String ?= null
    var senseUiud: String ?= null
    var isSelected : Boolean = false
    var displayName : String = ""

    fun auraSenseDefault() : MutableList<AuraSenseConfigure>{
        val aura_sense_list : MutableList<AuraSenseConfigure> = ArrayList()
        val irblaster = AuraSenseConfigure()
        val motionTrigger = AuraSenseConfigure()
        val humidty = AuraSenseConfigure()
        val temperature = AuraSenseConfigure()
        val lightIntensity = AuraSenseConfigure()

        irblaster.auraSenseName = "IR Blaster"
        irblaster.auraSenseFavorite = true
        irblaster.auraSenseIcon = 0
        irblaster.auraSenseIndex = 0
        irblaster.displayName = Constant.UNIVERSAL_REMOTE
        aura_sense_list.add(irblaster)

        motionTrigger.auraSenseName = "Motion"
        motionTrigger.auraSenseIndex = 1
        motionTrigger.auraSenseFavorite = true
        motionTrigger.auraSenseIcon = 1
        motionTrigger.range = 0
        motionTrigger.displayName = Constant.MOTION_SENSOR
        aura_sense_list.add(motionTrigger)

        temperature.auraSenseName = "Temperature"
        temperature.auraSenseIndex = 2
        temperature.auraSenseFavorite = true
        temperature.auraSenseIcon = 2
        temperature.range = 0
        temperature.displayName = Constant.TEMP_SENSOR
        aura_sense_list.add(temperature)

        humidty.auraSenseName = "Humidity"
        humidty.auraSenseIcon = 3
        humidty.auraSenseFavorite = true
        humidty.auraSenseIndex = 3
        humidty.range = 0
        humidty.displayName = Constant.HUMIDITY_SENSOR
        aura_sense_list.add(humidty)



        lightIntensity.auraSenseName = "Light Intensity"
        lightIntensity.auraSenseIcon = 4
        lightIntensity.auraSenseFavorite = true
        lightIntensity.auraSenseIndex = 4
        lightIntensity.range = 0
        lightIntensity.displayName = Constant.LUX_SENSOR
        aura_sense_list.add(lightIntensity)

        return aura_sense_list
    }

    fun universalSenseRemoteIr(): MutableList<AuraSenseConfigure> {
        val universalParamList: MutableList<AuraSenseConfigure> = arrayListOf()
        val irblaster = AuraSenseConfigure()
        irblaster.auraSenseName = "IR Blaster"
        irblaster.auraSenseFavorite = true
        irblaster.auraSenseIcon = 0
        irblaster.auraSenseIndex = 0
        irblaster.displayName = Constant.UNIVERSAL_REMOTE
        universalParamList.add(irblaster)
        return universalParamList
    }
}