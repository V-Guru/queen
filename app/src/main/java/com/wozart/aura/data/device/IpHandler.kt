package com.wozart.aura.data.device

import com.wozart.aura.data.model.IpModel
import com.wozart.aura.data.model.RemoteModel

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 08/10/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0.8
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class IpHandler {


    companion object {
        @JvmStatic
        private val ipList: MutableList<IpModel> = ArrayList()

    }

    fun startDevices(ips: MutableList<IpModel>): MutableList<IpModel> {
        ipList.clear()
        for (ip in ips) {
            if (ip.owned == 0) {
                ipList.add(ip)
            }
        }
        return ipList
    }

    fun registerIpDevice(ip: IpModel): MutableList<IpModel> {
        var flag = false
        for (i in ipList) {
            if (i.name == ip.name) {
                i.state = ip.state
                i.fullDeviceName = ip.fullDeviceName
                i.dim = ip.dim
                i.ip = ip.ip
                i.module = ip.module
                i.twoModuleState = ip.twoModuleState
                i.twoModuleDim = ip.twoModuleDim
                i.auraPlugState = ip.auraPlugState
                i.curtainState = ip.curtainState
                i.uiud = ip.uiud
                i.owned = ip.owned
                i.thing = ip.thing
                i.auraPlugPower = ip.auraPlugPower
                i.aws = ip.aws
                i.local = ip.local
                i.failure = ip.failure
                i.curnLoad = ip.curnLoad
                i.home = ip.home
                i.room = ip.room
                i.condition = ip.condition
                i.tempLight = ip.tempLight
                i.brightness = ip.brightness
                i.hue = ip.hue
                i.saturation = ip.saturation
                flag = true
                break
            }
        }
        if (!flag) {
            ipList.add(ip)
        }
        return ipList
    }

    fun getIpDevices(): MutableList<IpModel> {
        return ipList
    }

    fun removeIpDevice(ip: IpModel): MutableList<IpModel> {
        var flag = false
        var list: MutableList<IpModel> = ArrayList()
        for (i in ipList) {
            if (i.name != ip.name) {
                list.add(i)
            }
        }
        startDevices(list)
        return ipList
    }

    fun isIpExists(ip: IpModel): Boolean {
        var flag = false
        for (i in ipList) {
            if (i.name == ip.name) {
                flag = true
                break
            }
        }
        return flag
    }

}
/*
var name: String? = null
var ip: String? = null
var lcl_ip: String? = null
var aws: Boolean = false
var thing:String?=null
var state = booleanArrayOf(false, false, false, false)
var dim = intArrayOf(100, 100, 100, 100)
var uiud:String?=null
var owned = 0
var status = false
var aws_fail_count=0*/