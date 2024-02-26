package com.wozart.aura.aura.ui.dashboard.more

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.github.mikephil.charting.data.BarEntry
import com.wozart.aura.R
import com.wozart.aura.aura.utilities.CalendarUtils
import com.wozart.aura.aura.utilities.GraphUtils
import com.wozart.aura.aura.utilities.Utils
import kotlinx.android.synthetic.main.activity_statistics_graph.*
import java.util.*


class StatisticsGraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics_graph)
        chart_layout.layoutParams.width=Utils.getScreenWidthInDP(this)
        chart_layout.layoutParams.height=Utils.getScreenWidthInDP(this)
        chart.layoutParams.width=Utils.getScreenWidthInDP(this)
        chart.layoutParams.height=Utils.getScreenWidthInDP(this)
        chart_layout.requestLayout()
        chart.requestLayout()
        loadGraph()
        back.setOnClickListener(View.OnClickListener {
            this.finish()
        })

    }

    private fun loadGraph() {

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 30f))
        entries.add(BarEntry(1f, 80f))
        entries.add(BarEntry(2f, 205f))
        entries.add(BarEntry(3f, 200f))
        // gap of 2f
        entries.add(BarEntry(4f, 30f))
        entries.add(BarEntry(5f, 70f))
        entries.add(BarEntry(6f, 60f))
        entries.add(BarEntry(7f, 30f))
        entries.add(BarEntry(8f, 70f))
        entries.add(BarEntry(9f, 60f))
        entries.add(BarEntry(10f, 30f))
        entries.add(BarEntry(11f, 70f))
        GraphUtils.drawGraph(this, chart, entries, CalendarUtils.graph_months)

    }
}
