package com.wozart.aura.entity.network

import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.*

/**
 * Created by Saif on 3/22/2018.
 */

class Nsd {

    private var mNsdManager: NsdManager? = null
    private var locationBroadcast: String = "WIFI"
    private lateinit var mContext: Context
    private var mDiscoveryListener: NsdManager.DiscoveryListener? = null


    fun initializeNsd() {
        ReportServices!!.clear()
    }

    private fun initializeDiscovery() {
        mDiscoveryListener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(p0: NsdServiceInfo?) {
                var data = p0!!.serviceName.first()
                if (p0?.serviceName!!.contains(mServiceName) || p0.serviceName.contains(serviceName) || (p0!!.serviceName.first() == 'W')) {
                    Thread.sleep(300)
                    mNsdManager?.resolveService(p0, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                            try {
                                Companion.serviceInfo = serviceInfo
                                reportedServices()
                                sendMessageToActivity(serviceInfo?.serviceName!!, serviceInfo.host!!.hostAddress)
                            } catch (e: Exception) {

                            }
                        }

                    })
                }

            }

            override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
                try {
                    mNsdManager?.stopServiceDiscovery(this)
                } catch (e: Exception) {
                }
            }

            override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
                try {
                    mNsdManager?.stopServiceDiscovery(this)
                } catch (e: Exception) {
                }
            }

            override fun onDiscoveryStarted(p0: String?) {
            }

            override fun onDiscoveryStopped(p0: String?) {
            }

            override fun onServiceLost(p0: NsdServiceInfo?) {
            }

        }
    }


    fun getInstance(context: Context, type: String) {
        this.mContext = context
        mNsdManager = mContext.getSystemService(Context.NSD_SERVICE) as NsdManager
        this.locationBroadcast = type

    }

    fun reportedServices() {
        if (ReportServices!!.contains(serviceInfo)) {
            ReportServices!!.remove(serviceInfo)
        } else {
            ReportServices!!.add(serviceInfo!!)
        }

    }

    fun setReolveFunction(p0: NsdServiceInfo?) {
        val nsdDiscoveryHandler = Handler()
        nsdDiscoveryHandler.postDelayed({
            //mNsdManager?.resolveService(p0, resolvedListner)
        }, 200)
    }

    fun stopDiscovery() {
        try {
            if (mDiscoveryListener != null) {
                mNsdManager?.stopServiceDiscovery(mDiscoveryListener)
            }
        } finally {

        }
        mDiscoveryListener = null
    }


    fun discoverServices() {
        try {
            if (mDiscoveryListener == null) {
                initializeDiscovery()
            }
            mNsdManager?.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)
        } catch (e: Exception) {

        }

    }


    fun getIP(device: String): String? {
        return ReportServices?.find { it.serviceName.contains(device) }!!.host.hostAddress
    }


    fun setBroadcastType(type: String) {
        locationBroadcast = type
    }

    private fun sendMessageToActivity(name: String, ip: String) {
        val intent = Intent("nsdDiscoverWifi")
        intent.putExtra("name", name)
        intent.putExtra("ip", ip)
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
    }

    companion object {
        val SERVICE_TYPE = "_hap._tcp."
        val TAG = "NsdClient"
        private val mServiceName = "Wozart"
        private val serviceName = "Aura"
        var serviceInfo: NsdServiceInfo? = null
        private var ReportServices: MutableList<NsdServiceInfo>? = ArrayList()

    }

}

