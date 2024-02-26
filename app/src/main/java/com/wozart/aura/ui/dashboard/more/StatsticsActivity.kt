package com.wozart.aura.aura.ui.dashboard.more

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.wozart.aura.R
import com.wozart.aura.data.model.SectionModel
import com.wozart.aura.aura.data.model.Statistics
import com.wozart.aura.aura.ui.adapter.SectionAdapter
import com.wozart.aura.aura.utilities.CalendarUtils
import kotlinx.android.synthetic.main.activity_statstics.*
import java.util.*


class StatsticsActivity : AppCompatActivity() {


    private var statisticsType = 0
    private lateinit var cal:Calendar

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statstics)
        setUpRecyclerView()
        populateRecyclerView()
        setUpSpinner();
        init();



    }

    private fun setUpSpinner(){
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.satatistics))
        cal= Calendar.getInstance()
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = dataAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                cal= Calendar.getInstance()
                when (position) {
                    0 -> {

                        text_month.text = CalendarUtils.getCurrentWeek(cal)
                        statisticsType = 0
                    }
                    1 -> {
                        text_month.text = CalendarUtils.getCurrentMonth(cal)
                        statisticsType = 1
                    }
                    2 -> {
                        text_month.text = CalendarUtils.getCurrentYear(cal)
                        statisticsType = 2
                    }
                }

            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
    }
    private fun init(){
        back.setOnClickListener(View.OnClickListener {
            this.finish()
        })
        text_month.text = CalendarUtils.getCurrentWeek(cal)
        IVpreviousMonth.setOnClickListener( {


            when (statisticsType) {
                0 -> text_month.text = CalendarUtils.getLastWeek(cal)
                1 -> text_month.text = CalendarUtils.getLastMonth(cal)
                2 -> text_month.text = CalendarUtils.getLastYear(cal)
            }

        }
        )
        IVnextMonth.setOnClickListener( {

            when (statisticsType) {
                0 ->{
                    val nextWeek:String=CalendarUtils.getNextWeek(cal)
                    if(!nextWeek.equals(""))
                    text_month.text = nextWeek}
                1 -> {
                    val nextMonth:String=CalendarUtils.getNextMonth(cal)
                    if(!nextMonth.equals(""))
                    text_month.text =nextMonth}
                2 -> {
                    val nextYear:String=CalendarUtils.getNextYear(cal)
                    if(!nextYear.equals(""))
                    text_month.text = nextYear}
            }
        }
        )
    }

    private fun setUpRecyclerView() {
       // sectioned_recycler_view.setHasFixedSize(true)
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        sectioned_recycler_view.layoutManager = linearLayoutManager
    }

    //populate recycler view
    private fun populateRecyclerView() {
        val sectionModelArrayList = ArrayList<SectionModel>()
        //for loop for sections
        for (i in 1..5) {
            var itemArrayList:MutableList<Statistics> = ArrayList()
            //for loop for items
            for (j in 1..5) {
                val statistic=Statistics()
                statistic.loadName="Light"+j
                statistic.loadImage="svg_bulb_inactive"
                statistic.powerConsumed="144KWH"
                itemArrayList.add(statistic)
            }

            sectionModelArrayList.add(SectionModel("Section " + i, itemArrayList))
        }

        val adapter = SectionAdapter(this, sectionModelArrayList)
        sectioned_recycler_view.adapter = adapter
    }

}
