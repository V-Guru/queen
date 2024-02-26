package com.wozart.aura.ui.auraSense

import com.wozart.aura.entity.model.aura.AuraSenseConfigure

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-01-30
 * Description :
 * Revision History :
 * ____________________________________________________________________________

 *****************************************************************************/
class SenseLoad{
    var sense_device_name : String ?= null
    var sense_uiud : String ?= null
    var sense_ip : String ?= null
    var sense_load : MutableList<AuraSenseConfigure> = ArrayList()
}