package com.wozart.aura.ui.dashboard.listener


/**
 * Created by Saif on 09/11/21.
 * mds71964@gmail.com
 */
interface UpgradeFirmwareListener {
    fun onUpgradeDevice(position: Int, data: Any, deviceIp:String?,deviceMdl:Any,viewType: Any)
}