package com.wozart.aura.data.model

import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.ui.auraSense.RemoteListModel


/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-12-30
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 *****************************************************************************/

class RemoteModel {
    var aura_sence_name: String? = null
    var aura_sense_full_name : String ?= null
    var sense_ip: String= ""
    var scense_thing:String?=null
    var sense_uiud:String?=null
    var sense_loads : MutableList<AuraSenseConfigure> = ArrayList()
    var sense_owned = 0
    var start1 = 0
    var zero_end = 0
    var one_end = 0
    var sense_local = false
    var sense_aws = false
    var start_parameter = 0
    var hexa_code : String ?= null
    var home:String?=null
    var room:String?=null
    var senseMacId : String ?= null
    var isSelected = false
    var role : String = "slave"

}