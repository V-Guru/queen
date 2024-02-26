package com.wozart.aura.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.warkiz.widget.IndicatorSeekBar
import com.wozart.aura.R
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.ui.createautomation.MotionSelectionFragment
import com.wozart.aura.ui.dashboard.listener.RecyclerItemClicked
import com.wozart.aura.utilities.Constant
import kotlinx.android.synthetic.main.dynamic_set_motionvalue_layout.view.*


/**
 * Created by Saif on 14/10/21.
 * mds71964@gmail.com
 */
class DynamicMotionInputAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var recyclerItemClicked: RecyclerItemClicked? = null
    var listInputSense: MutableList<AuraSenseConfigure>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var isDetect: Boolean = false
    private var isStopDetect: Boolean = false

    inner class MotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        @SuppressLint("SetTextI18n")
        fun bind(auraSenseConfigure: AuraSenseConfigure) {
            itemView.tvSenseDeviceName.text = itemView.context.getString(R.string.sense_loads)+ "-" +auraSenseConfigure.senseDeviceName
            setView(auraSenseConfigure)
            setListener(auraSenseConfigure)
        }

        private fun setView(auraSenseConfigure: AuraSenseConfigure) {

            if (MotionSelectionFragment.automationScheduleType == "create") {
                if (auraSenseConfigure.auraSenseName == "Temperature") {
                    itemView.rbAboveTemp.isChecked = true
                    auraSenseConfigure.above = itemView.rbAboveTemp.isChecked
                    itemView.llTempRadio.visibility = View.VISIBLE
                    itemView.llTemperatureShow.visibility = View.VISIBLE
                    itemView.tvTempValue.text = Constant.TEMP
                    itemView.aboveTempBar.visibility = View.VISIBLE
                    itemView.aboveTempBar.setProgress(auraSenseConfigure.range.toFloat())
                } else if (auraSenseConfigure.auraSenseName == "Humidity") {
                    itemView.rbAboveHumidity.isChecked = true
                    auraSenseConfigure.above = itemView.rbAboveHumidity.isChecked
                    itemView.llHumidityRadio.visibility = View.VISIBLE
                    itemView.llhumidityShow.visibility = View.VISIBLE
                    itemView.tvHumidityValue.text = Constant.HUMIDITY
                    itemView.aboveHumidityBar.setProgress(auraSenseConfigure.range.toFloat())
                    itemView.aboveHumidityBar.visibility = View.VISIBLE
                } else if (auraSenseConfigure.auraSenseName == "Light Intensity") {
                    itemView.rbAboveLight.isChecked = true
                    auraSenseConfigure.above = itemView.rbAboveLight.isChecked
                    itemView.llLightRadio.visibility = View.VISIBLE
                    itemView.llIntensityShow.visibility = View.VISIBLE
                    itemView.tvLuxValue.text = Constant.LUX
                    itemView.aboveLightBar.visibility = View.VISIBLE
                    itemView.aboveLightBar.setProgress(auraSenseConfigure.range.toFloat())
                } else if (auraSenseConfigure.auraSenseName == "Motion") {
                    itemView.rbDetectMotion.isChecked = true
                    auraSenseConfigure.range = 1
                    isDetect = itemView.rbDetectMotion.isChecked
                    itemView.llRadio.visibility = View.VISIBLE
                    itemView.tvMotion.visibility = View.VISIBLE
                }
            } else {
                if (auraSenseConfigure.auraSenseName == "Temperature") {
                    if (auraSenseConfigure.above) {
                        itemView.rbAboveTemp.isChecked = true
                        itemView.ivTemp.setImageResource(R.drawable.ic_temp_greater)
                        itemView.aboveTempBar.visibility = View.VISIBLE
                        itemView.aboveTempBar.setProgress(auraSenseConfigure.range.toFloat())
                    } else {
                        itemView.rbBelowTemp.isChecked = true
                        itemView.ivTemp.setImageResource(R.drawable.ic_less_temp)
                        itemView.belowTempBar.visibility = View.VISIBLE
                        itemView.belowTempBar.setProgress(auraSenseConfigure.range.toFloat())
                    }
                    itemView.tvTempValue.text = auraSenseConfigure.range.toString()
                    itemView.llTempRadio.visibility = View.VISIBLE
                    itemView.llTemperatureShow.visibility = View.VISIBLE
                } else if (auraSenseConfigure.auraSenseName == "Humidity") {
                    if (auraSenseConfigure.above) {
                        itemView.rbAboveHumidity.isChecked = true
                        itemView.ivHumidity.setImageResource(R.drawable.ic_temp_greater)
                        itemView.aboveHumidityBar.visibility = View.VISIBLE
                        itemView.aboveHumidityBar.setProgress(auraSenseConfigure.range.toFloat())
                    } else {
                        itemView.rbBelowHumidity.isChecked = true
                        itemView.ivHumidity.setImageResource(R.drawable.ic_less_temp)
                        itemView.belowHumidityBar.visibility = View.VISIBLE
                        itemView.belowHumidityBar.setProgress(auraSenseConfigure.range.toFloat())
                    }
                    itemView.tvHumidityValue.text = auraSenseConfigure.range.toString()
                    itemView.llHumidityRadio.visibility = View.VISIBLE
                    itemView.llhumidityShow.visibility = View.VISIBLE
                } else if (auraSenseConfigure.auraSenseName == "Light Intensity") {
                    if (auraSenseConfigure.above) {
                        itemView.rbAboveLight.isChecked = true
                        itemView.rbBelowLight.isChecked = false
                        itemView.ivLightIntensity.setImageResource(R.drawable.ic_temp_greater)
                        itemView.aboveLightBar.visibility = View.VISIBLE
                        itemView.belowLightBar.visibility = View.GONE
                        itemView.aboveLightBar.setProgress(auraSenseConfigure.range.toFloat())
                    } else {
                        itemView.rbBelowLight.isChecked = true
                        itemView.rbAboveLight.isChecked = false
                        itemView.ivLightIntensity.setImageResource(R.drawable.ic_less_temp)
                        itemView.belowLightBar.visibility = View.VISIBLE
                        itemView.aboveLightBar.visibility = View.GONE
                        itemView.belowLightBar.setProgress(auraSenseConfigure.range.toFloat())
                    }
                    itemView.tvLuxValue.text = auraSenseConfigure.range.toString()
                    itemView.llLightRadio.visibility = View.VISIBLE
                    itemView.llIntensityShow.visibility = View.VISIBLE
                } else if (auraSenseConfigure.auraSenseName == "Motion") {
                    itemView.llRadio.visibility = View.VISIBLE
                    if (auraSenseConfigure.range == 1) {
                        itemView.rbDetectMotion.isChecked = true
                        isDetect = true
                    } else {
                        itemView.rbStopMotion.isChecked = true
                        isStopDetect = true
                    }
                }
            }
        }

        private fun setListener(auraSenseConfigure: AuraSenseConfigure) {

            itemView.aboveTempBar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                    itemView.tvTempValue.text = progress.toString()
                    itemView.ivTemp.setImageResource(R.drawable.ic_temp_greater)
                    auraSenseConfigure.range = progress
                }

                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

                }

                override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

                }

                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

                }

            })

            itemView.belowTempBar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                    itemView.tvTempValue.text = progress.toString()
                    itemView.ivTemp.setImageResource(R.drawable.ic_less_temp)
                    auraSenseConfigure.range = progress
                }

                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

                }

                override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

                }

                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

                }

            })

            itemView.aboveHumidityBar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                    itemView.tvHumidityValue.text = progress.toString()
                    itemView.ivHumidity.setImageResource(R.drawable.ic_temp_greater)
                    auraSenseConfigure.range = progress
                }

                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

                }

                override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

                }

                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

                }

            })

            itemView.belowHumidityBar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                    itemView.tvHumidityValue.text = progress.toString()
                    itemView.ivHumidity.setImageResource(R.drawable.ic_less_temp)
                    auraSenseConfigure.range = progress
                }

                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

                }

                override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

                }

                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

                }

            })

            itemView.aboveLightBar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                    itemView.tvLuxValue.text = progress.toString()
                    itemView.ivLightIntensity.setImageResource(R.drawable.ic_temp_greater)
                    auraSenseConfigure.range = progress
                }

                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

                }

                override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

                }

                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

                }

            })

            itemView.belowLightBar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                    itemView.tvLuxValue.text = progress.toString()
                    itemView.ivLightIntensity.setImageResource(R.drawable.ic_less_temp)
                    auraSenseConfigure.range = progress
                }

                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

                }

                override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

                }

                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

                }

            })

            itemView.rbDetectMotion.setOnClickListener {
                if (itemView.rbDetectMotion.isChecked) {
                    itemView.rbStopMotion.isChecked = false
                }
                auraSenseConfigure.range = 1

                isDetect = itemView.rbDetectMotion.isChecked
            }

            itemView.rbStopMotion.setOnClickListener {
                if (itemView.rbStopMotion.isChecked) {
                    itemView.rbDetectMotion.isChecked = false
                }
                auraSenseConfigure.range = 0

                isStopDetect = itemView.rbStopMotion.isChecked
            }

            itemView.rbAboveTemp.setOnClickListener {
                if (itemView.rbAboveTemp.isChecked) {
                    itemView.ivTemp.setImageResource(R.drawable.ic_temp_greater)
                    itemView.rbBelowTemp.isChecked = false
                }
                auraSenseConfigure.let {
                    it.above = itemView.rbAboveTemp.isChecked
                    it.below = false
                }

                itemView.aboveTempBar.setProgress(auraSenseConfigure.range.toFloat())
                itemView.aboveTempBar.visibility = View.VISIBLE
                itemView.belowTempBar.visibility = View.GONE
            }

            itemView.rbBelowTemp.setOnClickListener {
                if (itemView.rbBelowTemp.isChecked) {
                    itemView.ivTemp.setImageResource(R.drawable.ic_less_temp)
                    itemView.rbAboveTemp.isChecked = false
                }

                auraSenseConfigure.let {
                    it.below = itemView.rbBelowTemp.isChecked
                    it.above = false
                }
                itemView.belowTempBar.setProgress(auraSenseConfigure.range.toFloat())
                itemView.aboveTempBar.visibility = View.GONE
                itemView.belowTempBar.visibility = View.VISIBLE
            }

            itemView.rbAboveHumidity.setOnClickListener {
                if (itemView.rbAboveHumidity.isChecked) {
                    itemView.ivHumidity.setImageResource(R.drawable.ic_temp_greater)
                    itemView.rbBelowHumidity.isChecked = false
                }
                auraSenseConfigure.let {
                    it.above = itemView.rbAboveHumidity.isChecked
                    it.below = false
                }
                itemView.aboveHumidityBar.setProgress(auraSenseConfigure.range.toFloat())
                itemView.aboveHumidityBar.visibility = View.VISIBLE
                itemView.belowHumidityBar.visibility = View.GONE
            }

            itemView.rbBelowHumidity.setOnClickListener {
                if (itemView.rbBelowHumidity.isChecked) {
                    itemView.ivHumidity.setImageResource(R.drawable.ic_less_temp)
                    itemView.rbAboveHumidity.isChecked = false
                }
                auraSenseConfigure.let {
                    it.below = itemView.rbBelowHumidity.isChecked
                    it.above = false
                }
                itemView.belowHumidityBar.setProgress(auraSenseConfigure.range.toFloat())
                itemView.aboveHumidityBar.visibility = View.GONE
                itemView.belowHumidityBar.visibility = View.VISIBLE
            }

            itemView.rbAboveLight.setOnClickListener {
                if (itemView.rbAboveLight.isChecked) {
                    itemView.ivLightIntensity.setImageResource(R.drawable.ic_temp_greater)
                    itemView.rbBelowLight.isChecked = false
                }
                auraSenseConfigure.let {
                    it.above = itemView.rbAboveLight.isChecked
                    it.below = false
                }
                itemView.aboveLightBar.setProgress(auraSenseConfigure.range.toFloat())
                itemView.aboveLightBar.visibility = View.VISIBLE
                itemView.belowLightBar.visibility = View.GONE
            }

            itemView.rbBelowLight.setOnClickListener {
                itemView.ivLightIntensity.setImageResource(R.drawable.ic_less_temp)
                if (itemView.rbBelowLight.isChecked) {
                    itemView.rbAboveLight.isChecked = false
                }
                auraSenseConfigure.let {
                    it.below = itemView.rbBelowLight.isChecked
                    it.above = false
                }
                itemView.belowLightBar.setProgress(auraSenseConfigure.range.toFloat())
                itemView.aboveLightBar.visibility = View.GONE
                itemView.belowLightBar.visibility = View.VISIBLE
            }
        }

        override fun onClick(v: View?) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MotionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.dynamic_set_motionvalue_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MotionViewHolder).bind(listInputSense?.get(position)!!)
    }

    override fun getItemCount(): Int {
        return listInputSense?.size ?: 0
    }


}