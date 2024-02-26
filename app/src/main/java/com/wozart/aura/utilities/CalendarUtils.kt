package com.wozart.aura.aura.utilities

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * Created by Drona Sahoo on 3/19/2018.
 */

object CalendarUtils {

    var months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    var graph_months = arrayOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
    var weeks = arrayOf("W1", "W2", "W3", "W4","W5")

    var days = arrayOf("M", "T", "W","T","F","S","S")
    fun getCurrentWeek(mCalendar: Calendar): String {
        val date = Date()
        mCalendar.time = date

        // 1 = Sunday, 2 = Monday, etc.
        val day_of_week = mCalendar.get(Calendar.DAY_OF_WEEK)

        val monday_offset: Int
        if (day_of_week == 1) {
            monday_offset = -6
        } else
            monday_offset = 2 - day_of_week // need to minus back
        mCalendar.add(Calendar.DAY_OF_YEAR, monday_offset)

        val mDateMonday = mCalendar.time

        // return 6 the next days of current day (object cal save current day)
        mCalendar.add(Calendar.DAY_OF_YEAR, 6)
        val mDateSunday = mCalendar.time

        //Get format date
        val strDateFormat = "dd MMM yyyy"
        val sdf = SimpleDateFormat(strDateFormat)

        var MONDAY = sdf.format(mDateMonday)
        val SUNDAY = sdf.format(mDateSunday)

        if (MONDAY.substring(7, 10) == SUNDAY.substring(7, 10)) {
            MONDAY = MONDAY.substring(0, 6)
        }
        if (MONDAY.substring(3, 6) == SUNDAY.substring(3, 6)) {
            MONDAY = MONDAY.substring(0, 2)
        }


        return MONDAY + " - " + SUNDAY
    }

    fun getLastWeek(mCalendar: Calendar): String {
        // Monday
        mCalendar.add(Calendar.DAY_OF_YEAR, -13)
        val mDateMonday = mCalendar.time

        // Sunday
        mCalendar.add(Calendar.DAY_OF_YEAR, 6)
        val mDateSunday = mCalendar.time

        // Date format
        val strDateFormat = "dd MMM yyyy"
        val sdf = SimpleDateFormat(strDateFormat)

        var MONDAY = sdf.format(mDateMonday)
        val SUNDAY = sdf.format(mDateSunday)

        if (MONDAY.substring(7, 10) == SUNDAY.substring(7, 10)) {
            MONDAY = MONDAY.substring(0, 6)
        }
        if (MONDAY.substring(3, 6) == SUNDAY.substring(3, 6)) {
            MONDAY = MONDAY.substring(0, 2)
        }

        return MONDAY + " - " + SUNDAY
    }

    fun getNextWeek(mCalendar: Calendar): String {
        // Monday
        mCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val mDateMonday = mCalendar.time

        if (mCalendar.time > Calendar.getInstance().time) {
            return ""
        } else{
        // Sunday
            mCalendar.add(Calendar.DAY_OF_YEAR, 6)
        val Week_Sunday_Date = mCalendar.time

        val strDateFormat = "dd MMM yyyy"
        val sdf = SimpleDateFormat(strDateFormat)

        var MONDAY = sdf.format(mDateMonday)
        val SUNDAY = sdf.format(Week_Sunday_Date)

        if (MONDAY.substring(7, 10) == SUNDAY.substring(7, 10)) {
            MONDAY = MONDAY.substring(0, 6)
        }
        if (MONDAY.substring(3, 6) == SUNDAY.substring(3, 6)) {
            MONDAY = MONDAY.substring(0, 2)
        }

        return MONDAY + " - " + SUNDAY
    }
    }

    fun getCurrentMonth(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        return months[m] + " " + year
    }

    fun getNextMonth(calendar: Calendar): String {
        calendar.add(Calendar.MONTH, 1)
        val year = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        if(m>Calendar.getInstance().get(Calendar.MONTH))
        {
            return ""
        }
        else
        return months[m] + " " + year
    }

    fun getLastMonth(calendar: Calendar): String {
        calendar.add(Calendar.MONTH, -1)
        val year = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        return months[m] + " " + year
    }

    fun getCurrentYear(calendar: Calendar): String {
        return calendar.get(Calendar.YEAR).toString() + ""
    }

    fun getNextYear(calendar: Calendar): String {
        calendar.add(Calendar.YEAR, 1)
        if(calendar.get(Calendar.YEAR)>Calendar.getInstance().get(Calendar.YEAR))
        {
            return ""
        }
        else
        return calendar.get(Calendar.YEAR).toString() + ""
    }

    fun getLastYear(calendar: Calendar): String {
        calendar.add(Calendar.YEAR, -1)
        return calendar.get(Calendar.YEAR).toString() + ""
    }
}
