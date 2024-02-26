package com.wozart.aura.aura.utilities

import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TimePicker
import java.util.*

class SetTime(private val editText: EditText, var ctx: Context) : View.OnClickListener, TimePickerDialog.OnTimeSetListener {
        private val myCalendar: Calendar
        init {
            this.editText.setOnClickListener(this)
            this.myCalendar = Calendar.getInstance()

        }

        override fun onClick(v: View?) {
            val hour = myCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = myCalendar.get(Calendar.MINUTE)
            TimePickerDialog(ctx, this, hour, minute, false).show()
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            var hour = if(hourOfDay <= 9) "0$hourOfDay" else hourOfDay.toString()
            var minute = if(minute <= 9) "0$minute" else minute.toString()
            this.editText.setText("$hour:$minute")


        }



    }