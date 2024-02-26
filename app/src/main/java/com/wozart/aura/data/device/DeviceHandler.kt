package com.wozart.aura.data.device

import android.util.Log
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aws.AwsState
import com.wozart.aura.utilities.JsonHelper
import java.util.ArrayList

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 15/05/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class DeviceHandler {

    companion object {
        @JvmStatic
        private val AuraFourNodeDevice: MutableList<AuraSwitch> = ArrayList()
    }

    fun registerDevice(device: AuraSwitch, ip: String, uiud: String) {
        Log.d("REGISTERDEVICE","Register aura device " + device.name)
        var flag: Boolean? = true
        for (x in AuraFourNodeDevice) {
            if (x.name == device.name) {
                x.ip = (ip)
                x.type = (device.type)
                x.uiud = (uiud)
                x.online = (1)
                x.id = (device.id)
                x.state = (device.state)
                x.aws = (device.aws)
                flag = false
            }
        }
        if (flag!!) {
            val singleDevice = AuraSwitch()
            singleDevice.name = (device.name)
            singleDevice.ip = (ip)
            singleDevice.type = (device.type)
            singleDevice.uiud = (uiud)
            singleDevice.online = (1)
            singleDevice.id = (device.id)
            singleDevice.state = (device.state)
            singleDevice.aws = (device.aws)
            AuraFourNodeDevice.add(singleDevice)
        }
    }

//    fun cloudDevices(shadow: AwsState?, thing: String, device: String) {
//        var flag: Boolean? = true
//        for (x in AuraFourNodeDevice) {
//            if (x.name == device) {
//                if (shadow!!.state.size != 0) {
//                    x.state = (shadow.getStates())
//                    x.dim = (shadow.getDims())
//                    x.led = (shadow.led)
//                }
//                flag = false
//            }
//        }
//        if (flag!!) {
//            val singleDevice = AuraSwitch()
//            singleDevice.name = (device)
//            singleDevice.online = (1)
//            if (shadow != null && shadow.state.size != 0 && shadow.dim.size != 0) {
//                singleDevice.state = (shadow.getStates())
//                singleDevice.dim = (shadow.getDims())
//                singleDevice.led = (shadow.led)
//            }
//
//            singleDevice.thing = (thing)
//            singleDevice.aws = (1)
//            AuraFourNodeDevice.add(singleDevice)
//        }
//    }


    fun updateStates(device: AuraSwitch) {
        for (c in AuraFourNodeDevice) {
            if (c.name == device.name) {
                c.state = device.state
                c.dim = device.dim
            }
        }
    }

    fun isDeviceLocallyAvailable(device: String): Boolean {
        for (c in AuraFourNodeDevice) {
            if (c.name == device) {
                return true
            }
        }

        return false
    }

    fun getInfo(deviceName: String): AuraSwitch {
        var singleDevice = AuraSwitch()
        for (c in AuraFourNodeDevice) {
            if (deviceName == c.name) {
                singleDevice = c
                return singleDevice
            }
        }
        return singleDevice
    }

    fun getStatesDims(deviceName: String): AuraSwitch {
        val singleDevice = AuraSwitch()
        for (c in AuraFourNodeDevice) {
            if (deviceName == c.name) {
                val name = deviceName.substring(deviceName.lastIndexOf('-') + 1)
                singleDevice.name = (name)
                singleDevice.state = (c.state)
                singleDevice.dim = (c.dim)
            }
        }
        return singleDevice
    }

    fun getDescribencyDevices(devices: MutableList<String>, allDevices: ArrayList<String>) : ArrayList<String>{
        val device : ArrayList<String> = ArrayList()
        var isPresent = true
        for(cloudDevice in devices){
            val deviceToCheck  = cloudDevice.substring(cloudDevice.length- 6)
            for(localDevice in allDevices){
                if(localDevice == deviceToCheck){
                    isPresent = false
                }
            }
            if(isPresent){
                device.add(cloudDevice)
            }
        }
        return device
    }

    fun getAllOnSwitches() : Int{
        var onSwitches = 0
        for(device in AuraFourNodeDevice){
            for(state in device.state){
                if(state == 1) onSwitches++
            }
        }

        return onSwitches
    }

    fun getAllOffSwitches(): Int{
        var onSwitches = 0
        for(device in AuraFourNodeDevice){
            for(state in device.state){
                if(state == 0) onSwitches++
            }
        }

        return onSwitches
    }

    fun removeDevice(deviceName : String){
        for(device in AuraFourNodeDevice){
            if(device.name == deviceName)
                AuraFourNodeDevice.remove(device)
        }
    }

}
