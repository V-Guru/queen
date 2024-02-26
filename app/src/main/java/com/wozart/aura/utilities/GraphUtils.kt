package com.wozart.aura.aura.utilities

import android.content.Context

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.wozart.aura.R

/**
 * Created by Drona Sahoo on 3/19/2018.
 */

object GraphUtils {

    fun drawGraph(context: Context, chart: BarChart, entries: List<BarEntry>, titles: Array<String>) {
        if (titles.size >= entries.size) {
            val set = BarDataSet(entries, "")
            set.setDrawValues(false)
            set.setColors(context.resources.getColor(R.color.white))
            val data = BarData(set)
            data.barWidth=0.5f

            chart.data = data
            chart.setFitBars(true) // make the x-axis fit exactly all bars
            chart.invalidate() // refresh
            chart.xAxis.setDrawGridLines(false)
            chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            val rightYAxis = chart.axisRight
            rightYAxis.isEnabled = false
            rightYAxis.setDrawGridLines(false)
            chart.xAxis.textColor = context.resources.getColor(R.color.white)
            chart.axisLeft.textColor = context.resources.getColor(R.color.white)
            chart.description.isEnabled = false
            chart.setPinchZoom(false)
            chart.isClickable = false
            chart.isDoubleTapToZoomEnabled = false
            chart.setTouchEnabled(false)
            chart.legend.isEnabled = false
            chart.extraLeftOffset=20f
            chart.xAxis.axisMinimum = -1f

            chart.xAxis.valueFormatter = IAxisValueFormatter { value, axis ->
                if (value != -1f) {

                    titles[value.toInt()]
                } else
                    ""
            }
            val leftAxis = chart.axisLeft
            leftAxis.valueFormatter = IAxisValueFormatter { value, axis ->
                if (value != -1f) {

                    value.toInt().toString()+" KWH"
                } else
                    ""
            }

            //chart.xAxis.labelRotationAngle = 70f
            chart.xAxis.setLabelCount(entries.size, false)

        } else {

        }
    }

}
