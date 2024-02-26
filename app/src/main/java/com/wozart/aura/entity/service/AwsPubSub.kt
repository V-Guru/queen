package com.wozart.aura.entity.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.regions.Regions
import com.wozart.aura.utilities.AppExecutors
import com.wozart.aura.utilities.Constant
import kotlinx.coroutines.*
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 30/04/18
 * Description :
 *****************************************************************************/

class AwsPubSub : Service() {

    internal var mBinder: IBinder = LocalAwsBinder()

    inner class LocalAwsBinder : Binder() {
        fun getServerInstance(): AwsPubSub {
            return this@AwsPubSub
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // Initialize the AWS Cognito credentials provider
        credentialsProvider = CognitoCachingCredentialsProvider(
                this.applicationContext,
                COGNITO_POOL_ID,
                MY_REGION
        )
        mqttManager = AWSIotMqttManager(getClientId()
                ?: UUID.randomUUID().toString(), CUSTOMER_SPECIFIC_ENDPOINT)
        mqttManager?.isAutoReconnect = true
        mqttManager?.keepAlive = 500000
        Thread(Runnable {
            connectMqtt()
        }).start()
        return START_NOT_STICKY
    }

    private fun getClientId(): String? {
        if (clientId.isNullOrEmpty()) {
            clientId = UUID.randomUUID().toString()
        }
        return clientId
    }

    private fun startReconnect(client_id: String): Int {

        credentialsProvider = CognitoCachingCredentialsProvider(
                applicationContext, // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        )

        mqttManager = AWSIotMqttManager(client_id, CUSTOMER_SPECIFIC_ENDPOINT)
        Thread(Runnable {
            connectMqtt()
        }).start()
        return START_NOT_STICKY
    }


    private fun connectMqtt() {
        try {
            mqttManager!!.connect(credentialsProvider!!) { status, throwable ->
                Log.d(LOG_TAG, "Status = " + (status).toString())
                AppExecutors().mainThread().execute {
                    if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting) {
                        isAwsConnected = false
                        notifyConnectionEstablished("Connecting")
                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                        isAwsConnected = true
                        notifyConnectionEstablished("Connected")
                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting) {
                        isAwsConnected = false
                        notifyConnectionEstablished("Reconnecting")
                        Constant.IS_FIRST = true
                        if (throwable != null) {
                            Log.d(LOG_TAG, "Reconnecting", throwable)
                            throwable.printStackTrace()
                        }
                    } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost) {
                        isAwsConnected = false
                        notifyConnectionEstablished("Connection lost")
                        if (throwable != null) {
                            Log.d(LOG_TAG, "Connection error.", throwable)
                            throwable.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun subscribeToAws(device: String) {
        if (!isAwsConnected) {
            return
        }
        val topic = String.format(Constant.AWS_UPDATE_ACCEPTED, device)
        try {
            mqttManager!!.subscribeToTopic(topic, AWSIotMqttQos.QOS0
            ) { topic, data ->
                try {
                    val message = String(data, Charset.forName("UTF-8"))
                    Log.d(LOG_TAG, "   Topic: $topic")
                    Log.d(LOG_TAG, " Message: $message")
                    val segments = topic.split(("/").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    sendDataToActivity(message + "/" + segments[2] + "/" + segments[5])
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Subscription error.", e)
        }
    }

    fun publishToAws(device: String, data: String) {
        if (!isAwsConnected) {
            return
        }
        val topic = String.format(Constant.AWS_UPDATE, device)
        try {
            if (mqttManager == null) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this@AwsPubSub)
                val client_id = prefs.getString("CLIENT_ID", "NULL")
                if (client_id == "NULL") {
                    val prefEditor = PreferenceManager.getDefaultSharedPreferences(this@AwsPubSub).edit()
                    prefEditor.putString("CLIENT_ID", UUID.randomUUID().toString())
                    prefEditor.apply()
                }
                mqttManager = AWSIotMqttManager(client_id!!, CUSTOMER_SPECIFIC_ENDPOINT)
                mqttManager!!.disconnect()
                startReconnect(client_id)
            } else {
                mqttManager!!.publishString(data, topic, AWSIotMqttQos.QOS0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getShadowData(device: String) {
        if (isAwsConnected) {
            return
        }
        val topic = String.format(Constant.AWS_GET_ACCEPTED, device)
        try {
            mqttManager!!.subscribeToTopic(topic, AWSIotMqttQos.QOS0
            ) { topic, data ->
                runOnUiThread {
                    try {
                        val message = String(data, Charset.forName("UTF-8"))
                        Log.d(LOG_TAG, "   Topic: $topic")
                        Log.d(LOG_TAG, " Message: $message")
                        val segments = topic.split(("/").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        sendDataToActivity(message + "/" + segments[2] + "/" + segments[5])
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Subscription error.", e)
        }
    }


    private fun sendDataToActivity(message: String) {
        val intent = Intent("AwsShadow")
        intent.putExtra("data", message)
        LocalBroadcastManager.getInstance(this@AwsPubSub).sendBroadcast(intent)
    }

    private fun notifyConnectionEstablished(message: String) {
        val intent = Intent("aws_connected")
        intent.putExtra("data", message)
        LocalBroadcastManager.getInstance(this@AwsPubSub).sendBroadcast(intent)
    }

    companion object {

        var isAwsConnected = false
        private val LOG_TAG = "AWS IoT PubSub"
        private val CUSTOMER_SPECIFIC_ENDPOINT = "a15bui8ebaqvjn-ats.iot.us-east-1.amazonaws.com"
        private val COGNITO_POOL_ID = "us-east-1:52da6706-7a78-41f4-950c-9d940b890788"
        val MY_REGION = Regions.US_EAST_1
        private var mqttManager: AWSIotMqttManager? = null
        private var clientId: String? = null
        var mBounded: Boolean = false
        private var credentialsProvider: CognitoCachingCredentialsProvider? = null

    }
}
