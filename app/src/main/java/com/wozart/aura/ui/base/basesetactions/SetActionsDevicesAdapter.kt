package com.wozart.aura.ui.base.basesetactions

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.warkiz.widget.IndicatorSeekBar
import com.wozart.aura.R
import com.wozart.aura.ui.dashboard.Device
import com.wozart.aura.aura.utilities.Utils
import com.wozart.aura.aura.utilities.Utils.getIconDrawable
import com.wozart.aura.ui.createautomation.baseadapters.BaseActionDeviceAdapter
import kotlinx.android.synthetic.main.dialog_configure_dimming.*
import kotlinx.android.synthetic.main.dialog_configure_dimming.iconDevice
import kotlinx.android.synthetic.main.dialog_configure_dimming.tvDialogTitle
import kotlinx.android.synthetic.main.dialog_curtain_control_edit.*
import kotlinx.android.synthetic.main.dialogue_tunnable_set.*
import kotlinx.android.synthetic.main.fan_dimming_layout.*
import kotlinx.android.synthetic.main.item_device.view.*
import kotlinx.android.synthetic.main.item_set_actions_device.view.deiceIcon
import kotlinx.android.synthetic.main.item_set_actions_device.view.deviceCard
import kotlinx.android.synthetic.main.item_set_actions_device.view.deviceStatus

class SetActionsDevicesAdapter(deviceList: ArrayList<Device>) : BaseActionDeviceAdapter(deviceList) {


    @SuppressLint("SetTextI18n")
    override fun customizeUI(itemView: View?, device: Device) {
        itemView?.let {
            val res = itemView.context.resources
            if (device.isTurnOn) {
                if (device.dimmable) {
                    itemView.deviceStatus.text = device.dimVal.toString() + "%"
                }
                if (device.checkType == "Curtain") {
                    itemView.deviceStatus.text = "Open"
                    itemView.deiceIcon?.setImageResource(Utils.getCurtainIcon(device.checkType, device.isTurnOn))
                } else {
                    itemView.deviceStatus.text = "ON"
                    itemView.deiceIcon?.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
                }

                itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
            } else {
                itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
                if (device.checkType == "Curtain") {
                    itemView.deviceStatus.text = "Close"
                    itemView.deiceIcon?.setImageResource(Utils.getCurtainIcon(device.checkType, device.isTurnOn))
                } else {
                    itemView.deviceStatus.text = "Off"
                    itemView.deiceIcon?.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
                }
            }
        }
    }

    override fun getLayoutType(): Int {
        return R.layout.item_set_actions_device
    }

    override fun registerListeners(holder: DeviceHolder?, position: Int) {
        val zoomin = AnimationUtils.loadAnimation(holder?.itemView?.context, R.anim.zoomin)
        val zoomout = AnimationUtils.loadAnimation(holder?.itemView?.context, R.anim.zoomout)
        val device = deviceList[position]
        holder?.itemView?.setOnClickListener {
            holder.itemView.deviceCard?.startAnimation(zoomin)
            holder.itemView.deviceCard?.startAnimation(zoomout)
            device.isSelected = !device.deviceChecked
            onClick(holder, device)
        }
        holder?.itemView?.setOnLongClickListener {
            onLongClick(holder, device)
        }
    }

    private fun onLongClick(holder: DeviceHolder, device: Device): Boolean {
        if (device.checkType == "rgbDevice") {
            openColorPickerDialog(holder, device)
        } else if (device.checkType == "tunableDevice") {
            tunnableDialog(device, holder)
        } else if (device.dimmable) {
            if (device.index == 2) {
                openFanDimmingDialog(holder, device)
            } else {
                openDimmingDialog(holder, device)
            }
        }
        return false
    }

    private fun openFanDimmingDialog(holder: DeviceHolder, device: Device) {
        val dialog = Dialog(holder.itemView.context)
        val dimVal = device.dimVal
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.fan_dimming_layout)
        dialog.btnEdit_.visibility = View.GONE
        if (device.name == "Fan" && device.isTurnOn) {
            Glide.with(holder.itemView.context).load(getIconDrawable(device.type, device.isTurnOn))
                    .into(dialog.iconDevice)
        } else {
            dialog.iconDevice.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
        }
        dialog.tvDialogTitle_.text = String.format(holder.itemView.context.getString(R.string.fan_dimming), device.name)

        dialog.fanDim.setProgress(dimVal.toFloat())
        dialog.fanDim.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {
            }

            override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                device.dimVal = progress
                device.isSelected = true
                holder.itemView.deviceStatus.text = device.dimVal.toString() + "%"
                change(holder, device)
            }
        })

        dialog.btnDoneFan.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }

    private fun tunnableDialog(device: Device, holder: DeviceHolder) {
        val dialog = Dialog(holder.itemView.context)
        var dimVal = 0
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialogue_tunnable_set)
        dialog.setBrightness.setProgress(device.dimVal.toFloat())
        dialog.tempBar.setProgress(device.tempValue.toFloat())

        dialog.tempBar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                device.tempValue = progress
            }

            override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

            }

        })

        dialog.setBrightness.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                device.dimVal = progress
                device.isSelected = true
                holder.itemView.deviceStatus.text = device.dimVal.toString() + "%"
                change(holder, device)
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {

            }

            override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {

            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {

            }
        })
        dialog.btnOk.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }

    private fun setTunableTempOnColor(envelope: ColorEnvelope, device: Device) {
        val hsv = FloatArray(3)
        Color.colorToHSV(envelope.color, hsv)
        val saturation = hsv[1] * 100
        device.hueValue = hsv[0].toInt()
        device.saturationValue = saturation.toInt()

    }

    private fun openColorPickerDialog(holder: DeviceHolder, device: Device) {
        val builder: ColorPickerDialog.Builder = ColorPickerDialog.Builder(holder.itemView.context)
                .setTitle("Select Your Colour")
                .setPreferenceName("Test")
                .setPositiveButton(holder.itemView.context.getString(R.string.done), object : ColorEnvelopeListener {

                    override fun onColorSelected(envelope: com.skydoves.colorpickerview.ColorEnvelope?, fromUser: Boolean) {
                        setLayoutColor(envelope!!, device, holder)
                    }

                })
                .setNegativeButton(
                        holder.itemView.context.getString(R.string.txt_cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachBrightnessSlideBar(true)
                .attachAlphaSlideBar(false)// the default value is true.
                .setBottomSpace(12)
        builder.colorPickerView.brightnessSlider!!.alpha = device.dimVal.toFloat()
        builder.show()
    }


    @SuppressLint("SetTextI18n")
    private fun setLayoutColor(envelope: ColorEnvelope, device: Device, holder: DeviceHolder) {
        val hsv = FloatArray(3)
        Color.colorToHSV(envelope.color, hsv)
        val saturation = hsv[1] * 100
        val lightness = hsv[2] * 100
        device.hueValue = hsv[0].toInt()
        device.saturationValue = saturation.toInt()
        device.dimVal = lightness.toInt()
        device.isSelected = true
        holder.itemView.deviceStatus.text = device.dimVal.toString() + "%"
        change(holder, device)
    }

    private fun openDimmingDialog(holder: DeviceHolder, device: Device) {
        val dialog = Dialog(holder.itemView.context)
        var dimVal = device.dimVal
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_configure_dimming)
        if (device.name.equals("Fan") && device.isTurnOn) {
            Glide.with(holder.itemView).load(Utils.getIconDrawable(device.type, device.isTurnOn))
                    .into(holder.itemView.deiceIcon)
        } else {
            dialog.iconDevice.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
        }

        dialog.tvDialogTitle.text = String.format(holder.itemView.resources.getString(R.string.text_configure_device_dim), device.name)
        dialog.rangeBarDim.setOnRangeBarChangeListener { _, _, _, _, rightPinValue ->
            dimVal = Integer.parseInt(rightPinValue)
        }
        dialog.sickbar.setProgress(dimVal.toFloat())
        dialog.sickbar.setOnSeekChangeListener(object : IndicatorSeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int) {
            }

            override fun onSectionChanged(seekBar: IndicatorSeekBar?, thumbPosOnTick: Int, textBelowTick: String?, fromUserTouch: Boolean) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onProgressChanged(seekBar: IndicatorSeekBar?, progress: Int, progressFloat: Float, fromUserTouch: Boolean) {
                dimVal = progress
                device.dimVal = dimVal
                device.isSelected = true
                holder.itemView.deviceStatus.text = device.dimVal.toString() + "%"
                change(holder, device)
            }
        })
        dialog.btnDone.setOnClickListener {
            device.isTurnOn = true // may be it needs to be happen automatically
            dialog.cancel()
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun onClick(holder: DeviceHolder, device: Device) {
        val res = holder.itemView.context.resources
        if (device.name.equals("Fan") && device.isTurnOn) {
            Glide.with(holder.itemView).load(Utils.getIconDrawable(device.type, device.isTurnOn))
                    .into(holder.itemView.deiceIcon)

        } else if (device.name.equals("Exhaust Fan") && device.isTurnOn) {
            Glide.with(holder.itemView).load(Utils.getIconDrawable(device.type, device.isTurnOn))
                    .into(holder.itemView.deiceIcon)

        } else {
            holder.itemView.deiceIcon.setImageResource(getIconDrawable(device.type, device.isTurnOn))
        }
        if (!device.isSelected) {
            device.isTurnOn = false
            if (device.checkType == "Curtain") {
                holder.itemView.deiceIcon?.setImageResource(Utils.getCurtainIcon(device.checkType, device.isTurnOn))
            } else {
                holder.itemView.deiceIcon?.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
            }
            holder.itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
            holder.itemView.deviceStatus.text = if (device.checkType == "Curtain") "Close" else device.status
            device.deviceChecked = false
            // device.isSelected = false
        } else {
            if (device.isTurnOn) {
                device.isTurnOn = false
                if (device.checkType == "Curtain") {
                    holder.itemView.deiceIcon?.setImageResource(Utils.getCurtainIcon(device.checkType, device.isTurnOn))
                } else {
                    holder.itemView.deiceIcon?.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
                }
                holder.itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_gray)
                holder.itemView.deviceStatus.text = if (device.checkType == "Curtain") "Close" else device.status
                device.deviceChecked = true
            } else {
                device.isTurnOn = true
                if (!device.dimmable) {
                    holder.itemView.deviceStatus.text = if (device.checkType == "Curtain") "Open" else "ON"
                    if (device.checkType == "Curtain") {
                        holder.itemView.deiceIcon?.setImageResource(Utils.getCurtainIcon(device.checkType, device.isTurnOn))
                    } else {
                        holder.itemView.deiceIcon?.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
                    }
                    holder.itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
                    device.deviceChecked = true
                } else {
                    holder.itemView.deiceIcon?.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
                    holder.itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
                    device.deviceChecked = true
                    holder.itemView.deviceStatus.text = device.dimVal.toString() + "%"
                }
            }

        }

        //notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    fun change(holder: DeviceHolder, device: Device) {
        val res = holder.itemView.context.resources
        device.isTurnOn = true
        if (!device.dimmable) {
            holder.itemView.deviceStatus.text = if (device.checkType == "Curtain") "Open" else "ON"
            if (device.checkType == "Curtain") {
                holder.itemView.deiceIcon?.setImageResource(Utils.getCurtainIcon(device.checkType, device.isTurnOn))
            } else {
                holder.itemView.deiceIcon?.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
            }
            holder.itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
            device.isSelected = !device.isSelected
        } else {
            holder.itemView.deiceIcon?.setImageResource(Utils.getIconDrawable(device.type, device.isTurnOn))
            holder.itemView.deviceCard.setBackgroundResource(R.drawable.card_shade_white)
            device.isSelected = !device.isSelected
            holder.itemView.deviceStatus.text = device.dimVal.toString() + "%"
        }
    }

}