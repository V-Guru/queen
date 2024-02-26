package com.wozart.aura.entity.sql.scheduling

import android.provider.BaseColumns


class ScheduleContract {
    class ScheduleEntry : BaseColumns{
        companion object : KbaseColumns(){
            const val TABLE_NAME = "schedule"
            const val SCHEDULE_NAME = "name"
            const val SCHEDULE_TYPE = "type"
            const val SCHEDULE_PROPERTY = "property"
            const val START_TIME = "starttime"
            const val END_TIME = "endtime"
            const val ROUTINE = "routine"
            const val ICON = "icon"
            const val LOAD = "load"
            const val ROOM = "room"
            const val HOME = "home"
            const val REMOTE = "remote"
        }
    }
    open class KbaseColumns{
        val ID = "_id"
    }

}