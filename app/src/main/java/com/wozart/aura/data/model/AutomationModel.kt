package com.wozart.aura.data.model

import com.wozart.aura.ui.createautomation.RoomModel

/**
 * Created by Niranjan P on 3/15/2018.
 */
class AutomationModel(
        var title: CharSequence?,
        var iconUrl: Int,
        var rooms: ArrayList<RoomModel>,
        var Automationenable: Boolean,
        var time: String?,
        var type: String?,
        var endTime: String?,
        var routine: String,
        var triigerType :String

)
