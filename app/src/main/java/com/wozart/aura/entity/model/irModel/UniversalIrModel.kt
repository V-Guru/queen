package com.wozart.aura.entity.model.irModel

import com.wozart.aura.ui.auraSense.RemoteIconModel


/**
 * Created by Saif on 08/02/22.
 * mds71964@gmail.com
 */
class UniversalIrModel {
    var brandName: String? = null
    var modelNumber: String? = null
    var typeAppliances : String ?= null
    var buttons: MutableList<LearnRemoteDataModel> = arrayListOf()
}