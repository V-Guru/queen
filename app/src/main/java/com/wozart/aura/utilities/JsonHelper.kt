package com.wozart.aura.utilities

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.FacebookSdk.getApplicationContext
import com.google.gson.Gson
import com.wozart.aura.R
import com.wozart.aura.data.model.AwsSenseState
import com.wozart.aura.data.model.RemoteModel
import com.wozart.aura.entity.model.aura.AuraSenseConfigure
import com.wozart.aura.entity.model.aura.AuraSwitch
import com.wozart.aura.entity.model.aws.AwsState
import com.wozart.aura.entity.model.scene.SceneList
import com.wozart.aura.ui.auraSense.RemoteIconModel
import com.wozart.aura.ui.auraSense.RemoteListModel
import com.wozart.aura.ui.createautomation.AutomationScene
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.Device
import org.json.JSONObject
import java.net.UnknownHostException

/***************************************************************************
 * File Name :
 * Author : Saif
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
class JsonHelper {
    private val LOG_TAG = JsonHelper::class.java.simpleName

    fun deserializeAwsData(Data: String): AwsState {
        val gson = Gson()
        var receivedData = AwsState()
        try {
            val reported = JSONObject(Data)
            val flag = reported.getJSONObject("state").isNull("reported")
            val checkDelta = reported.getJSONObject("state").isNull("delta")
            if (!flag) {
                if (!checkDelta) {
                    val deltaData = gson.fromJson<AwsState>(reported.getJSONObject("state").getString("delta"), AwsState::class.java)
                }
                receivedData = gson.fromJson<AwsState>(reported.getJSONObject("state").getString("reported"), AwsState::class.java)
            } else {
                return receivedData
            }
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error Parsing JsonHelper Data: $e")
        }
        return receivedData
    }

    fun convertMotionState(State: String): AwsSenseState {
        val gson = Gson()
        var receivedData = AwsSenseState()
        try {
            var rep = JSONObject(State)
            receivedData = gson.fromJson<AwsSenseState>(State, AwsSenseState::class.java)
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Error Parsing JsonHelper Data: $e")
        }
        return receivedData
    }


    fun awsRegionThing(region: String, thing: String): String {
        var data: String? = null
        data = "{\"type\":7,\"thing\":\"$thing\",\"region\":\"$region\"}"
        return data
    }

    fun certificates(data: String): ArrayList<String> {
        val certificates = SegregateData.Segregate(data)
        var jsonCertificate: String
        val dataCertificates = ArrayList<String>()
        var length = 0
        for ((pktNo, fragment) in certificates.withIndex()) {
            jsonCertificate = "{\"type\":6,\"pos\":$length,\"pktno\":$pktNo,\"data\":\"$fragment\"}"
            dataCertificates.add(jsonCertificate)
            length += fragment.length + 1
        }
        return dataCertificates
    }

    fun privateKeys(data: String): ArrayList<String> {
        val privateKey = SegregateData.Segregate(data)
        var jsonPrivateKey: String
        var length = 0
        val dataPrivarteKey = ArrayList<String>()
        for ((pktNo, fragment) in privateKey.withIndex()) {
            jsonPrivateKey = "{\"type\":5,\"pos\":$length,\"pktno\":$pktNo,\"data\":\"$fragment\"}"
            dataPrivarteKey.add(jsonPrivateKey)
            length += fragment.length + 1
        }
        return dataPrivarteKey
    }

    fun serialize(device: Device, uiud: String, module: Int, type: Boolean): String {
        if (module == 2 || module == 12 || module == 20 || module == 11) {
            val plc = IntArray(2)
            plc[device.index] = 1
            val states = intArrayOf(0, 0)
            val dims = intArrayOf(100, 100)
            if (!type) {
                if (device.isTurnOn) {
                    states[device.index] = 0
                } else {
                    states[device.index] = 1
                }
            } else {
                if (device.dimVal > 0) {
                    states[device.index] = 1
                } else {
                    states[device.index] = 0
                }
            }
            dims[device.index] = device.dimVal

            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "],\"plc\":[" + plc[0] + "," + plc[1] + "]}")
        } else if (module == 1) {
            val plc = IntArray(1)
            plc[device.index] = 1
            val states = intArrayOf(0)
            val p0 = intArrayOf(0)
            if (!type) {
                if (device.isTurnOn) {
                    states[device.index] = 0
                } else {
                    states[device.index] = 1
                }
            } else {
                if (device.dimVal > 0) {
                    states[device.index] = 1
                } else {
                    states[device.index] = 0
                }
            }
            p0[device.index] = device.dimVal

            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "],\"dim\":["
                    + p0[0] + "],\"plc\":[" + plc[0] + "]}")
        } else if (module == 5) {
            val plc = IntArray(5)
            plc[device.index] = 1
            val states = intArrayOf(0, 0, 0, 0, 0)
            val dims = intArrayOf(100, 100, 100, 100, 100)
            if (!type) {
                if (device.isTurnOn) {
                    states[device.index] = 0
                } else {
                    states[device.index] = 1
                }
            } else {
                if (device.dimVal > 0) {
                    states[device.index] = 1
                } else {
                    states[device.index] = 0
                }
            }
            dims[device.index] = device.dimVal

            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "," + states[2] + "," + states[3] + "," + states[4] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "," + dims[2] + "," + dims[3] + "," + dims[4] + "],\"plc\":[" + plc[0] + "," + plc[1] + "," + plc[2] + "," + plc[3] + "," + plc[4] + "]}")
        } else {
            val plc = IntArray(4)
            plc[device.index] = 1
            val states = intArrayOf(0, 0, 0, 0)
            val dims = intArrayOf(100, 100, 100, 100)
            if (!type) {
                if (device.isTurnOn) {
                    states[device.index] = 0
                } else {
                    states[device.index] = 1
                }
            } else {
                if (device.dimVal > 0) {
                    states[device.index] = 1
                } else {
                    states[device.index] = 0
                }
            }
            dims[device.index] = device.dimVal

            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "," + states[2] + "," + states[3] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "," + dims[2] + "," + dims[3] + "],\"plc\":[" + plc[0] + "," + plc[1] + "," + plc[2] + "," + plc[3] + "]}")
        }
    }

    fun serializeDataToAws(device: Device, uiud: String, module: Int, type: Boolean): String {
        val data: String
        var state = 0
        val cState = arrayOf(0, 0)
        val led = System.currentTimeMillis() / 1000

        if (!type) {
            if (device.isTurnOn) {
                if (module == 3 || device.checkType == "Curtain") {
                    cState[0] = 0
                    cState[1] = 1
                } else {
                    state = 0
                }
            } else {
                if (module == 3 || device.checkType == "Curtain") {
                    cState[0] = 1
                    cState[1] = 0
                } else {
                    state = 1
                }
            }
        } else {
            if (device.dimVal > 0) {
                state = 1
            } else {
                state = 0
            }
        }
        if (module == 2 || module == 1 || module == 12 || module == 20) {
            data = "{\"state\":{\"desired\": {\"uiud\": \"" + uiud + "\" ,\"led\": " + led + ",\"state\": {\"s" + device.index + "\":" + state + "}}}}"
        } else if (module == 3) {
            data = "{\"state\":{\"desired\": {\"uiud\": \"" + uiud + "\" ,\"led\": " + led + ",\"state\": {\"s0\":" + cState[0] + ",\"s1\":" + cState[1] + "}}}}"
        } else {
            data = "{\"state\":{\"desired\": {\"uiud\": \"" + uiud + "\" ,\"led\": " + led + ",\"dim\": {\"d" + device.index + "\":" + device.dimVal + "},\"state\": {\"s" + device.index + "\":" + state + "}}}}"
        }
        return data
    }

    fun curtainCloudControl(context: Context, uiud: String, type: String, device: Device): String {
        val cState = arrayOf(0, 0)
        val led = System.currentTimeMillis() / 1000
        when (type) {
            context.getString(R.string.open_curtain), context.getString(R.string.close_curtaing) -> {
                if (device.curtainState == type) {
                    cState[0] = device.curtainState0
                    cState[1] = device.curtainState1
                } else {
                    if (device.curtainState0 == 0) {
                        cState[0] = 1
                        cState[1] = 0
                    } else {
                        cState[0] = 0
                        cState[1] = 1
                    }
                }
            }
            context.getString(R.string.stop) -> {
                cState[0] = 0
                cState[1] = 0
            }
        }
        return "{\"state\":{\"desired\": {\"uiud\": \"" + uiud + "\" ,\"led\": " + led + ",\"state\": {\"s0\":" + cState[0] + ",\"s1\":" + cState[1] + "}}}}"
    }

    fun serializeLEDData(): String {
        val data: String
        val led = (System.currentTimeMillis() / 1000)
        data = "{\"state\":{\"desired\": {\"led\": $led}}}"
        return data
    }

    @Throws(UnknownHostException::class)
    fun initialData(uiud: String): String {

        return "{\"type\":1,\"ip\":" + convertIP() + ",\"time\":" + System.currentTimeMillis() / 1000 + ",\"uiud\":\"" + uiud + "\" }"
    }

    fun deserializeTcp(data: String): AuraSwitch {
        val gson = Gson()
        var device = AuraSwitch()
        try {
            device = gson.fromJson<AuraSwitch>(data, AuraSwitch::class.java)
        } catch (e: Exception) {
            Log.d("DATA_ERROR : ", data)
            Log.d(LOG_TAG, "Illegal Message $e")
        } finally {
            return device
        }
    }

    fun deserializeRemoteData(data: String): RemoteIconModel {
        return Gson().fromJson(data, RemoteIconModel::class.java)
    }

    fun pairingData(uiud: String, pin: String): String {
        return "{\"type\":2,\"hash\":\"$pin\",\"uiud\":\"$uiud\"}"
    }

    /**
     * Scenes Methods
     */

    fun serializeSceneData(scene: SceneList, uiud: String, module: Int, isSceneOn: Boolean): String {
        if (module == 5) {
            val plc = IntArray(5)
            val states = IntArray(5)
            val dims: IntArray = intArrayOf(100, 100, 100, 100, 100)

            for (device in scene.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }

            return ("{\"type\":4, \"ip\":" + convertIP() + ",\"name\":\"" + scene.device + "\",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "," + states[2] + "," + states[3] + "," + states[4] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "," + dims[2] + "," + dims[3] + "," + dims[4] + "], \"plc\":[" + plc[0] + "," + plc[1] + "," + plc[2] + "," + plc[3] + "," + plc[4] + "]}")

        } else if (module == 2 || module == 12 || module == 11 || module == 20) {
            val plc = IntArray(2)
            val states = IntArray(2)
            val dims: IntArray = intArrayOf(100, 100)

            for (device in scene.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }
            return ("{\"type\":4, \"ip\":" + convertIP() + ",\"name\":\"" + scene.device + "\",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "], \"plc\":[" + plc[0] + "," + plc[1] + "]}")
        } else if (module == 3) {
            val plc = IntArray(2)
            plc[0] = 1
            plc[1] = 1
            val states = intArrayOf(0, 0)
            val dims = intArrayOf(100, 100)
            if (isSceneOn) {
                states[0] = scene.loads[0].curtainState0
                states[1] = scene.loads[0].curtainState1
            } else {
                states[0] = 0
                states[1] = 0
            }
            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "],\"plc\":[" + plc[0] + "," + plc[1] + "]}")
        } else if (module == 7 || module == 13) {
            val plc = IntArray(1)
            val states = IntArray(1)
            val dims: IntArray = intArrayOf(100)
            for (device in scene.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }
            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "],\"hue\":${scene.loads[0].hueValue},\"saturation\":${scene.loads[0].saturationValue},\"brightness\":${dims[0]}}")
        } else if (module == 8) {
            val plc = IntArray(1)
            val states = IntArray(1)
            val dims: IntArray = intArrayOf(100)
            for (device in scene.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }
            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "],\"temperature\":${scene.loads[0].tempValue},\"hue\":${scene.loads[0].hueValue},\"saturation\":${scene.loads[0].saturationValue},\"brightness\":${dims[0]}}")
        } else if (module == 1) {
            val plc = IntArray(1)
            val states = IntArray(1)
            val dims: IntArray = intArrayOf(100)

            for (device in scene.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }
            return ("{\"type\":4, \"ip\":" + convertIP() + ",\"name\":\"" + scene.device + "\",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "],\"dim\":["
                    + dims[0] + "], \"plc\":[" + plc[0] + "]}")
        } else {
            val plc = IntArray(4)
            val states = IntArray(4)
            val dims: IntArray = intArrayOf(100, 100, 100, 100)

            for (device in scene.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }

            return ("{\"type\":4, \"ip\":" + convertIP() + ",\"name\":\"" + scene.device + "\",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "," + states[2] + "," + states[3] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "," + dims[2] + "," + dims[3] + "], \"plc\":[" + plc[0] + "," + plc[1] + "," + plc[2] + "," + plc[3] + "]}")
        }

    }


    fun serializeSceneDataForAws(scene: SceneList, isSceneOn: Boolean, module: Int): String {
        val plc = IntArray(5)
        val states = IntArray(5)
        val dims = IntArray(5)
        var aws_data = "{\"state\":{\"desired\":{"
        val led = System.currentTimeMillis() / 1000


        aws_data = "$aws_data\"led\": $led,\"state\":{"

        for (device in scene.loads) {
            plc[device.index] = 1
            if (device.isTurnOn) states[device.index] = 1
            else states[device.index] = 0
            if (isSceneOn) {
                if (device.checkType == "Curtain") {
                    aws_data = aws_data + "\"s0\":" + device.curtainState0 + "," + "\"s1\":" + device.curtainState1 + ","
                } else {
                    aws_data = aws_data + "\"s" + device.index + "\":" + states[device.index] + ","
                }
            } else {
                if (device.checkType == "Curtain") {
                    aws_data = "$aws_data\"s0\":0,\"s1\":0,"
                } else {
                    aws_data = aws_data + "\"s" + device.index + "\":0,"
                }
            }
        }
        aws_data = aws_data.substring(0, aws_data.length - 1)
        if (scene.loads[0].checkType == "Curtain") {
            aws_data += "}}}}"
        } else if (scene.loads[0].checkType == "rgbDevice") {
            aws_data += "},\"hue\": ${scene.loads[0].hueValue},\"saturation\":${scene.loads[0].saturationValue},\"dim\": {\"d" + 0 + "\" : " + scene.loads[0].dimVal + "}}}}}"
        } else if (scene.loads[0].checkType == "tunableDevice") {
            aws_data += "},\"temperature\":${scene.loads[0].tempValue},\"brightness\": ${scene.loads[0].dimVal}}}}"
        } else {
            aws_data += "},\"dim\":{"
            for (device in scene.loads) {
                plc[device.index] = 1
                dims[device.index] = device.dimVal
                aws_data = aws_data + "\"d" + device.index + "\":" + dims[device.index] + ","
            }
            aws_data = aws_data.substring(0, aws_data.length - 1)

            aws_data += "}}}}"
        }
        return aws_data
        //{"state":{"desired": {"led": -285489, "dim": {"d0":0},"state": {"s0":1}}}}

    }

    @Throws(UnknownHostException::class)
    fun convertIP(): Int {
        val mWifi = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = mWifi.connectionInfo
        return wifiInfo.ipAddress
    }

    /**
     *All Device On
     */

    fun serializeOnAllDevice(devices: SceneList, uiud: String, isSceneOn: Boolean, name: String, module: Int): String {
        if (module == 5) {
            val plc = IntArray(5)
            val states = IntArray(5)
            val dims: IntArray = intArrayOf(100, 100, 100, 100, 100)

            for (device in devices.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }

            return ("{\"type\":4, \"ip\":" + convertIP() + ",\"name\":\"" + name + "\",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "," + states[2] + "," + states[3] + "," + states[4] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "," + dims[2] + "," + dims[3] + "," + dims[4] + "], \"plc\":[" + plc[0] + "," + plc[1] + "," + plc[2] + "," + plc[3] + "," + plc[4] + "]}")
        } else if (module == 1 || module == 7 || module == 8 || module == 13) {
            val plc = IntArray(1)
            val states = IntArray(1)
            val dims: IntArray = intArrayOf(100, 100)

            for (device in devices.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }

            return if (module == 1) {
                "{\"type\":4, \"ip\":" + convertIP() + ",\"name\":\"" + name + "\",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "],\"dim\":[" + dims[0] + "], \"plc\":[" + plc[0] + "]}"
            } else if (module == 7 || module == 13) {
                "{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "],\"hue\":${devices.loads[0].hueValue},\"saturation\":${devices.loads[0].saturationValue},\"brightness\":${devices.loads[0].dimVal}}"
            } else {
                "{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "],\"hue\":${devices.loads[0].hueValue},\"saturation\":${devices.loads[0].saturationValue},\"brightness\":${dims[0]}}"
            }

        } else if (module == 2 || module == 12 || module == 11 || module == 20) {
            val plc = IntArray(2)
            val states = IntArray(2)
            val dims: IntArray = intArrayOf(100, 100)

            for (device in devices.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }

            return ("{\"type\":4, \"ip\":" + convertIP() + ",\"name\":\"" + name + "\",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "], \"plc\":[" + plc[0] + "," + plc[1] + "]}")
        } else if (module == 3) {
            val plc = IntArray(2)
            plc[0] = 1
            plc[1] = 1
            val states = intArrayOf(0, 0)
            val dims = intArrayOf(100, 100)
            if (devices.loads[0].isTurnOn) {
                if (devices.loads[0].curtainState != "Open") {
                    if (devices.loads[0].curtainState0 == 0) {
                        devices.loads[0].curtainState0 = 1
                        devices.loads[0].curtainState1 = 0
                    } else {
                        devices.loads[0].curtainState0 = 0
                        devices.loads[0].curtainState1 = 1
                    }
                } else {
                    states[0] = devices.loads[0].curtainState0
                    states[1] = devices.loads[0].curtainState1
                }
            } else {
                states[0] = 0
                states[1] = 0
            }
            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "],\"plc\":[" + plc[0] + "," + plc[1] + "]}")
        } else {
            val plc = IntArray(4)
            val states = IntArray(4)
            val dims: IntArray = intArrayOf(100, 100, 100, 100)

            for (device in devices.loads) {
                plc[device.index] = 1
                if (isSceneOn) {
                    if (device.isTurnOn) states[device.index] = 1
                    else states[device.index] = 0
                    dims[device.index] = device.dimVal
                } else {
                    states[device.index] = 0
                }
            }

            return ("{\"type\":4, \"ip\":" + convertIP() + ",\"name\":\"" + name + "\",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "," + states[2] + "," + states[3] + "],\"dim\":["
                    + dims[0] + "," + dims[1] + "," + dims[2] + "," + dims[3] + "], \"plc\":[" + plc[0] + "," + plc[1] + "," + plc[2] + "," + plc[3] + "]}")
        }
    }

    fun serializeOnAllDeviceAws(devices: SceneList, isSceneOn: Boolean): String {
        val plc = IntArray(5)
        val states = IntArray(5)
        val dims = IntArray(5)
        var aws_data = "{\"state\":{\"desired\":{"
        val led = System.currentTimeMillis() / 1000

        aws_data = "$aws_data\"led\": $led,\"state\":{"

        for (device in devices.loads) {
            if (device.checkType == "Curtain") {
                aws_data = "$aws_data\"s0\":0,\"s1\":0,"
            } else {
                plc[device.index] = 1
                if (device.isTurnOn) states[device.index] = 1
                else states[device.index] = 0
                if (isSceneOn)
                    aws_data = aws_data + "\"s" + device.index + "\":" + states[device.index] + ","
                else
                    aws_data = aws_data + "\"s" + device.index + "\":0,"
            }
        }
        aws_data = aws_data.substring(0, aws_data.length - 1)
        if (devices.loads[0].checkType == "rgbDevice") {
            aws_data += "},\"hue\": \"${devices.loads[0].hueValue}\",\"saturation\":\"${devices.loads[0].saturationValue}\",\"dim\": {\"d" + 0 + "\" : " + devices.loads[0].dimVal + "}}}}}"
        } else if (devices.loads[0].checkType == "tunableDevice") {
            aws_data += "},\"temperature\":${devices.loads[0].tempValue},\"brightness\": ${devices.loads[0].dimVal}}}}"
        } else {
            aws_data += "},\"dim\":{"
            for (device in devices.loads) {
                plc[device.index] = 1
                dims[device.index] = device.dimVal
                aws_data = aws_data + "\"d" + device.index + "\":" + dims[device.index] + ","
            }
            aws_data = aws_data.substring(0, aws_data.length - 1)

            aws_data += "}}}}"

        }

        return aws_data
    }

    fun motionData(uiud: String): String {
        val data = "{\"type\":5,\"ip\":" + convertIP() + ",\"uiud\":\"$uiud\"}"
        return data
    }


    fun auraButtonConfigure(uiud: String): String {
        val data = "{\"type\":7,\"ip\":" + convertIP() + ",\"uiud\":\"$uiud\"}"
        return data
    }

    fun testCurtain(uiud: String): String {
        val plc = IntArray(2)
        plc[0] = 1
        plc[1] = 1
        val states = intArrayOf(0, 0)
        val dims = intArrayOf(100, 100)
        states[0] = 0
        states[1] = 1

        return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "],\"dim\":["
                + dims[0] + "," + dims[1] + "],\"plc\":[" + plc[0] + "," + plc[1] + "]}")
    }

    fun testAgainCurtain(uiud: String): String {
        val plc = IntArray(2)
        plc[0] = 1
        plc[1] = 1
        val states = intArrayOf(0, 0)
        val dims = intArrayOf(100, 100)
        states[0] = 1
        states[1] = 0

        return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "],\"dim\":["
                + dims[0] + "," + dims[1] + "],\"plc\":[" + plc[0] + "," + plc[1] + "]}")
    }

    fun setActionCurtain(context: Context, device: Device, uiud: String, curtainState: String, stateSet: Boolean): String {
        val plc = IntArray(2)
        plc[0] = 1
        plc[1] = 1
        val states = intArrayOf(0, 0)
        val dims = intArrayOf(100, 100)
        if (stateSet) {
            when (curtainState) {
                context.getString(R.string.open_curtain), context.getString(R.string.close_curtaing) -> {
                    if (device.curtainState == curtainState) {
                        states[0] = device.curtainState0
                        states[1] = device.curtainState1
                    } else {
                        if (device.curtainState0 == 0) {
                            states[0] = 1
                            states[1] = 0
                        } else {
                            states[0] = 0
                            states[1] = 1
                        }
                    }
                }
                context.getString(R.string.stop) -> {
                    states[0] = 0
                    states[1] = 0
                }
            }
        } else {
            states[0] = 0
            states[1] = 0
        }
        return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "," + states[1] + "],\"dim\":["
                + dims[0] + "," + dims[1] + "],\"plc\":[" + plc[0] + "," + plc[1] + "]}")

    }


    fun serializeRemoteControl(uiud: String, remoteListModel: RemoteListModel, btnName: String): String {
        val led = System.currentTimeMillis() / 1000
        val cNumber = ArrayList<String>(arrayListOf())
        val modeL = ArrayList<String>(arrayListOf())
        val btnNameArr = ArrayList<String>(arrayListOf())

        for (remoteIcon in remoteListModel.dynamicRemoteIconList) {
            if (remoteIcon.remoteButtonName == btnName) {
                cNumber.add(remoteIcon.channelNumber)
                modeL.add(remoteListModel.modelNumber!!)
                btnNameArr.add(remoteIcon.remoteButtonName!!)
                break
            }
        }

        val modelArray = modeL.joinToString { it -> "\"${it}\"" }
        val channelArray = cNumber.joinToString { it -> "\"${it}\"" }
        val btnNameArray = btnNameArr.joinToString { it -> "\"${it}\"" }

        return "{\"state\":{\"desired\": {\"uiud\": \"$uiud\" ,\"led\": $led,\"state\": {\"model\":[$modelArray],\"btnName\":[$btnNameArray],\"cNumber\":[$channelArray],\"ir_flag\":1}}}}"
    }

    fun serializeSceneRemoteControl(uiud: String, modal: ArrayList<String>, cNumber: ArrayList<String>, btnName: ArrayList<String>): String {
        val led = System.currentTimeMillis() / 1000
        val ir_flag = modal.size
        val modelArray = modal.joinToString { it -> "\"${it}\"" }
        val channelArray = cNumber.joinToString { it -> "\"${it}\"" }
        val btnNameArray = btnName.joinToString { it -> "\"${it}\"" }

        return "{\"state\":{\"desired\": {\"uiud\": \"$uiud\" ,\"led\": $led,\"state\": {\"model\":[$modelArray],\"btnName\":[$btnNameArray],\"cNumber\":[$channelArray],\"ir_flag\":$ir_flag}}}}"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun sendSceneData(sceneName: String, scene: MutableList<RoomModel>, sceneOldName: String, sceneType: String, uiudList: ArrayList<String>, deviceName: ArrayList<String>, mdlArray: ArrayList<Int>, favRemoteButtonList: MutableList<RemoteIconModel>, senseDeviceList: MutableList<RemoteModel>): String {
        val states: MutableList<Int> = ArrayList()
        val plc: MutableList<Int> = ArrayList()
        val dim: MutableList<Int> = ArrayList()
        val cNumber = ArrayList<String>(arrayListOf())
        val modeL = ArrayList<String>(arrayListOf())
        val btnName = ArrayList<String>(arrayListOf())
        val deviceList: ArrayList<Device> = ArrayList()
        val autAdvance = ArrayList<String>(arrayListOf())
        val autStatus = ArrayList<Int>(arrayListOf())
        val cNumberOthers = ArrayList<String>(arrayListOf())
        val modeLOthers = ArrayList<String>(arrayListOf())
        val btnNameOthers = ArrayList<String>(arrayListOf())
        val senseNameOthers = ArrayList<String>(arrayListOf())
        val senseUiudArrayOthers = ArrayList<String>(arrayListOf())
        for (remote in favRemoteButtonList) {
            var checksameIr = false
            if (remote.name == senseDeviceList[0].aura_sence_name) {
                checksameIr = true
            }
            if (checksameIr) {
                if (remote.channelNumber.isNotEmpty()) {
                    cNumber.add(remote.channelNumber)
                }
                modeL.add(remote.remoteModel!!)
                btnName.add(remote.remoteButtonName!!)
            }
            if (!checksameIr) {
                if (remote.channelNumber.isNotEmpty()) {
                    cNumberOthers.add(remote.channelNumber)
                }
                modeLOthers.add(remote.remoteModel!!)
                btnNameOthers.add(remote.remoteButtonName!!)
                senseNameOthers.add(remote.name!!)
                val sensors = senseDeviceList.find { it.aura_sence_name == remote.name }
                senseUiudArrayOthers.add(sensors?.sense_uiud!!)
            }
        }

        for (automation in scene) {
            for (aDevice in automation.deviceList) {
                if (aDevice.deviceName == "Automation") {
                    autAdvance.add(aDevice.name)
                    if (aDevice.isTurnOn) {
                        autStatus.add(1)
                    } else {
                        autStatus.add(0)
                    }
                }
                deviceList.add(aDevice)
            }
        }
        var size: Int = 0
        size = uiudList.size
        val modelArray = modeL.joinToString { it -> "\"${it}\"" }
        val channelArray = cNumber.joinToString { it -> "\"${it}\"" }
        val btnNameArray = btnName.joinToString { it -> "\"${it}\"" }
        val commaSeperatedString = deviceName.joinToString { it -> "\"${it}\"" }
        val advanceAut = autAdvance.joinToString { "\"${it}\"" }
        val remoteModelOthers = modeLOthers.joinToString { it -> "\"${it}\"" }
        val remoteCnumberOther = cNumberOthers.joinToString { it -> "\"${it}\"" }
        val remoteButtonOther = btnNameOthers.joinToString { it -> "\"${it}\"" }
        val senseNameOther = senseNameOthers.joinToString { it -> "\"${it}\"" }
        val senseUiudOther = senseUiudArrayOthers.joinToString { it -> "\"${it}\"" }
        var local_data = "{\"type\":11,\"uiud\":\"${senseDeviceList[0].sense_uiud}\",\"index_s\":$size,\"scene_name\":\"${sceneName}\",\"d_name\":[" + commaSeperatedString + "],\"otherIR\":{\"devName\":[" + senseNameOther + "],\"devUiud\":[" + senseUiudOther + "],\"devModel\":[" + remoteModelOthers + "],\"devBtn\":[" + remoteButtonOther + "],\"devcNumber\":[" + remoteCnumberOther + "]},\"model\":[" + modelArray + "],\"cNumber\":[" + channelArray + "],\"btnName\":[" + btnNameArray + "],\"uiud_s\":[" + uiudList.joinToString { "\"$it\"" } + "],\"state_s\":{"
        //for (device in scene) {
        for (d in 0 until deviceName.size) {
            var flag = false
            states.clear()
            for (loads in deviceList) {
                if (deviceName[d].contains(loads.deviceName)) {
                    if (mdlArray.get(d) == 4 || mdlArray.get(d) == 6 || mdlArray.get(d) == 23) {
                        if (!flag) {
                            flag = true
                            states.add(0, 0)
                            states.add(1, 0)
                            states.add(2, 0)
                            states.add(3, 0)
                        }
                        if (loads.isTurnOn) {
                            states.removeAt(loads.index)
                            states.add(loads.index, 1)
                        }
                    } else if (mdlArray.get(d) == 3) {
                        if (loads.isTurnOn) {
                            states.add(0, 1)
                            states.add(1, 0)
                        } else {
                            states.add(0, 0)
                            states.add(1, 1)
                        }
                    } else if (mdlArray.get(d) == 5) {
                        if (!flag) {
                            flag = true
                            states.add(0, 0)
                            states.add(1, 0)
                            states.add(2, 0)
                            states.add(3, 0)
                            states.add(4, 0)
                        }
                        if (loads.isTurnOn) {
                            states.removeAt(loads.index)
                            states.add(loads.index, 1)
                        }
                    } else if (mdlArray.get(d) == 2 || mdlArray.get(d) == 12 || mdlArray.get(d) == 20) {
                        if (!flag) {
                            flag = true
                            states.add(0, 0)
                            states.add(1, 0)
                        }
                        if (loads.isTurnOn) {
                            states.removeAt(loads.index)
                            states.add(loads.index, 1)
                        }
                    } else {
                        states.add(loads.index, if (loads.isTurnOn) 1 else 0)
                    }
                }
            }
            local_data = local_data + "\"" + d + "\"" + ":" + states.toList() + ","
        }
        local_data = local_data.substring(0, local_data.length - 1)
        local_data += "},\"plc_s\":{"
        for (d in 0 until deviceName.size) {
            plc.clear()
            var flag = false
            for (loads in deviceList) {
                if (deviceName[d].contains(loads.deviceName)) {
                    if (mdlArray.get(d) == 4 || mdlArray.get(d) == 6 || mdlArray.get(d) == 23) {
                        if (!flag) {
                            flag = true
                            plc.add(0, 0)
                            plc.add(1, 0)
                            plc.add(2, 0)
                            plc.add(3, 0)
                        }
                        plc.removeAt(loads.index)
                        plc.add(loads.index, 1)
                    } else if (mdlArray.get(d) == 3) {
                        if (loads.isTurnOn) {
                            plc.add(0, 1)
                            plc.add(1, 1)
                        } else {
                            plc.add(0, 1)
                            plc.add(1, 1)
                        }
                    } else if (mdlArray.get(d) == 5) {
                        if (!flag) {
                            flag = true
                            plc.add(0, 0)
                            plc.add(1, 0)
                            plc.add(2, 0)
                            plc.add(3, 0)
                            plc.add(4, 0)
                        }
                        plc.removeAt(loads.index)
                        plc.add(loads.index, 1)
                    } else if (mdlArray.get(d) == 2 || mdlArray.get(d) == 12 || mdlArray.get(d) == 20) {
                        if (!flag) {
                            flag = true
                            plc.add(0, 0)
                            plc.add(1, 0)
                        }
                        plc.removeAt(loads.index)
                        plc.add(loads.index, 1)
                    } else {
                        plc.add(loads.index, 1)
                    }
                }
            }
            local_data = local_data + "\"" + "p" + d + "\"" + ":" + plc.toList() + ","
        }
        local_data = local_data.substring(0, local_data.length - 1)
        local_data += "},\"dim_s\":{"
        for (d in 0 until deviceName.size) {
            dim.clear()
            var flag = false
            for (loads in deviceList) {
                if (deviceName[d].contains(loads.deviceName)) {
                    if (mdlArray.get(d) == 4 || mdlArray.get(d) == 6 || mdlArray.get(d) == 23) {
                        if (!flag) {
                            flag = true
                            dim.add(0, 100)
                            dim.add(1, 100)
                            dim.add(2, 100)
                            dim.add(3, 100)
                        }
                        dim.removeAt(loads.index)
                        dim.add(loads.index, loads.dimVal)
                    } else if (mdlArray.get(d) == 3) {
                        if (loads.isTurnOn) {
                            dim.add(0, 100)
                            dim.add(1, 100)
                        } else {
                            dim.add(0, 100)
                            dim.add(1, 100)
                        }
                    } else if (mdlArray.get(d) == 5) {
                        if (!flag) {
                            flag = true
                            dim.add(0, 100)
                            dim.add(1, 100)
                            dim.add(2, 100)
                            dim.add(3, 100)
                            dim.add(4, 100)
                        }
                        dim.removeAt(loads.index)
                        dim.add(loads.index, 100)
                    } else if (mdlArray.get(d) == 2 || mdlArray.get(d) == 12 || mdlArray.get(d) == 20) {
                        if (!flag) {
                            flag = true
                            dim.add(0, 100)
                            dim.add(1, 100)
                        }
                        dim.removeAt(loads.index)
                        dim.add(loads.index, 100)
                    } else if (mdlArray.get(d) == 7 || mdlArray.get(d) == 8 || mdlArray.get(d) == 13) {
                        dim.add(0, loads.dimVal)
                        dim.add(1, loads.hueValue)
                        dim.add(2, loads.saturationValue)
                        dim.add(3, loads.tempValue)
                    } else {
                        dim.add(loads.index, 100)
                    }
                }
            }
            local_data = local_data + "\"" + "d" + d + "\"" + ":" + dim.toList() + ","
        }
        local_data = local_data.substring(0, local_data.length - 1)
        local_data += "},\"scene_name_old\":\"$sceneOldName\",\"s_type\":\"$sceneType\",\"mdl\":${mdlArray.toList()},\"advSetting\":{\"autName\":[" + advanceAut + "],\"autStat\":${autStatus.toList()}}}"
        return local_data
    }

    fun sendAutomationData(automation: AutomationScene, autoOldName: String, scheduleType: String, deviceName: ArrayList<String>, uiudList: ArrayList<String>, turnOff: String, sceneSize: Int, sceneNameArray: ArrayList<String>, c_type: String, mdlArray: ArrayList<Int>, routineLocal: ArrayList<Int>, favRemoteButtonList: MutableList<RemoteIconModel>, senseDeviceList: MutableList<RemoteModel>): String {
        val states: MutableList<Int> = ArrayList()
        val plc: MutableList<Int> = ArrayList()
        val dim: MutableList<Int> = ArrayList()
        var size: Int = 0
        var turnOffData = 0
        val deviceList: ArrayList<Device> = ArrayList()
        var enable: Int = 0
        val cNumber = ArrayList<String>(arrayListOf())
        val modeL = ArrayList<String>(arrayListOf())
        val btnName = ArrayList<String>(arrayListOf())
        val cNumberOthers = ArrayList<String>(arrayListOf())
        val modeLOthers = ArrayList<String>(arrayListOf())
        val btnNameOthers = ArrayList<String>(arrayListOf())
        val senseNameOthers = ArrayList<String>(arrayListOf())
        val senseUiudArrayOthers = ArrayList<String>(arrayListOf())

        if (automation.property[0].AutomationEnable) {
            enable = 1
        }

        for (allDevice in automation.load) {
            for (devices in allDevice.deviceList) {
                deviceList.add(devices)
            }
        }

        if (turnOff != "Never") {
            turnOffData = turnOff.toInt() / 60
        }
        for (remote in favRemoteButtonList) {
            var checksameIr = false
            if (remote.name == senseDeviceList[0].aura_sence_name) {
                checksameIr = true
            }
            if (checksameIr) {
                if (remote.channelNumber.isNotEmpty()) {
                    cNumber.add(remote.channelNumber)
                }
                modeL.add(remote.remoteModel!!)
                btnName.add(remote.remoteButtonName!!)
            }
            if (!checksameIr) {
                if (remote.channelNumber.isNotEmpty()) {
                    cNumberOthers.add(remote.channelNumber)
                }
                modeLOthers.add(remote.remoteModel!!)
                btnNameOthers.add(remote.remoteButtonName!!)
                senseNameOthers.add(remote.name!!)
                val sensors = senseDeviceList.find { it.aura_sence_name == remote.name }
                senseUiudArrayOthers.add(sensors?.sense_uiud!!)
            }
        }

        val modelArray = modeL.joinToString { it -> "\"${it}\"" }
        val channelArray = cNumber.joinToString { it -> "\"${it}\"" }
        val btnNameArray = btnName.joinToString { it -> "\"${it}\"" }
        val commaSeperatedString = deviceName.joinToString { it -> "\"${it}\"" }
        val sceneNameData = sceneNameArray.joinToString { it -> "\"${it}\"" }
        val remoteModelOthers = modeLOthers.joinToString { it -> "\"${it}\"" }
        val remoteCnumberOther = cNumberOthers.joinToString { it -> "\"${it}\"" }
        val remoteButtonOther = btnNameOthers.joinToString { it -> "\"${it}\"" }
        val senseNameOther = senseNameOthers.joinToString { it -> "\"${it}\"" }
        val senseUiudOther = senseUiudArrayOthers.joinToString { it -> "\"${it}\"" }
        size = uiudList.size
        var local_data: String = ""
        if (deviceName.size > 0) {
            local_data = "{\"type\":12,\"scene_name\":[" + sceneNameData + "],\"a_type\":\"$c_type\",\"conf_wday\":1,\"time_of_day\":\"" + automation.time + "\",\"a_name_old\":\"" + autoOldName + "\",\"status\":$enable,\"sc_idx\":$sceneSize,\"day_of_week\":${routineLocal.toList()},\"otherIR\":{\"devName\":[" + senseNameOther + "],\"devUiud\":[" + senseUiudOther + "],\"devModel\":[" + remoteModelOthers + "],\"devBtn\":[" + remoteButtonOther + "],\"devcNumber\":[" + remoteCnumberOther + "]},\"turn_off\":$turnOffData,\"uiud\":\"${senseDeviceList[0].sense_uiud}\",\"ld_index\":$size,\"a_name\":\"${automation.name}\",\"model\":[" + modelArray + "],\"cNumber\":[" + channelArray + "],\"btnName\":[" + btnNameArray + "],\"loads\":{\"index_s\":${deviceName.size},\"d_name\":[" + commaSeperatedString + "],\"uiud_s\":[" + uiudList.joinToString { "\"$it\"" } + "],\"state_s\":{"
            for (d in 0 until deviceName.size) {
                var flag = false
                states.clear()
                for (loads in deviceList) {
                    if (deviceName[d].contains(loads.deviceName)) {
                        if (mdlArray.get(d) == 4 || mdlArray.get(d) == 6 || mdlArray.get(d) == 23) {
                            if (!flag) {
                                flag = true
                                states.add(0, 0)
                                states.add(1, 0)
                                states.add(2, 0)
                                states.add(3, 0)
                            }
                            if (loads.isTurnOn) {
                                states.removeAt(loads.index)
                                states.add(loads.index, 1)
                            }
                        } else if (mdlArray.get(d) == 3) {
                            if (loads.isTurnOn) {
                                states.add(0, 1)
                                states.add(1, 0)
                            } else {
                                states.add(0, 0)
                                states.add(1, 1)
                            }
                        } else if (mdlArray.get(d) == 5) {
                            if (!flag) {
                                flag = true
                                states.add(0, 0)
                                states.add(1, 0)
                                states.add(2, 0)
                                states.add(3, 0)
                                states.add(4, 0)
                            }
                            if (loads.isTurnOn) {
                                states.removeAt(loads.index)
                                states.add(loads.index, 1)
                            }
                        } else if (mdlArray.get(d) == 2 || mdlArray.get(d) == 12 || mdlArray.get(d) == 20) {
                            if (!flag) {
                                flag = true
                                states.add(0, 0)
                                states.add(1, 0)
                            }
                            if (loads.isTurnOn) {
                                states.removeAt(loads.index)
                                states.add(loads.index, 1)
                            }
                        } else {
                            states.add(loads.index, if (loads.isTurnOn) 1 else 0)
                        }
                    }
                }
                local_data = local_data + "\"" + d + "\"" + ":" + states.toList() + ","
            }
            local_data = local_data.substring(0, local_data.length - 1)
            local_data += "},\"plc_s\":{"
            for (d in 0 until deviceName.size) {
                plc.clear()
                var flag = false
                for (loads in deviceList) {
                    if (deviceName[d].contains(loads.deviceName)) {
                        if (mdlArray.get(d) == 4 || mdlArray.get(d) == 6 || mdlArray.get(d) == 23) {
                            if (!flag) {
                                flag = true
                                plc.add(0, 0)
                                plc.add(1, 0)
                                plc.add(2, 0)
                                plc.add(3, 0)
                            }
                            plc.removeAt(loads.index)
                            plc.add(loads.index, 1)
                        } else if (mdlArray.get(d) == 3) {
                            if (loads.isTurnOn) {
                                plc.add(0, 1)
                                plc.add(1, 1)
                            } else {
                                plc.add(0, 1)
                                plc.add(1, 1)
                            }
                        } else if (mdlArray.get(d) == 5) {
                            if (!flag) {
                                flag = true
                                plc.add(0, 0)
                                plc.add(1, 0)
                                plc.add(2, 0)
                                plc.add(3, 0)
                                plc.add(4, 0)
                            }
                            plc.removeAt(loads.index)
                            plc.add(loads.index, 1)
                        } else if (mdlArray.get(d) == 2 || mdlArray.get(d) == 12 || mdlArray.get(d) == 20) {
                            if (!flag) {
                                flag = true
                                plc.add(0, 0)
                                plc.add(1, 0)
                            }
                            plc.removeAt(loads.index)
                            plc.add(loads.index, 1)
                        } else {
                            plc.add(loads.index, 1)
                        }
                    }
                }
                local_data = local_data + "\"" + "p" + d + "\"" + ":" + plc.toList() + ","
            }
            local_data = local_data.substring(0, local_data.length - 1)
            local_data += "},\"dim_s\":{"
            for (d in 0 until deviceName.size) {
                dim.clear()
                var flag = false
                for (loads in deviceList) {
                    if (deviceName[d].contains(loads.deviceName)) {
                        if (mdlArray.get(d) == 4 || mdlArray.get(d) == 6 || mdlArray.get(d) == 23) {
                            if (!flag) {
                                flag = true
                                dim.add(0, 100)
                                dim.add(1, 100)
                                dim.add(2, 100)
                                dim.add(3, 100)
                            }
                            dim.removeAt(loads.index)
                            dim.add(loads.index, loads.dimVal)
                        } else if (mdlArray.get(d) == 3) {
                            if (loads.isTurnOn) {
                                dim.add(0, 100)
                                dim.add(1, 100)
                            } else {
                                dim.add(0, 100)
                                dim.add(1, 100)
                            }
                        } else if (mdlArray.get(d) == 5) {
                            if (!flag) {
                                flag = true
                                dim.add(0, 100)
                                dim.add(1, 100)
                                dim.add(2, 100)
                                dim.add(3, 100)
                                dim.add(4, 100)
                            }
                            dim.removeAt(loads.index)
                            dim.add(loads.index, 100)
                        } else if (mdlArray.get(d) == 2 || mdlArray.get(d) == 12 || mdlArray.get(d) == 20) {
                            if (!flag) {
                                flag = true
                                dim.add(0, 100)
                                dim.add(1, 100)
                            }
                            dim.removeAt(loads.index)
                            dim.add(loads.index, 100)
                        } else if (mdlArray.get(d) == 7 || mdlArray.get(d) == 8 || mdlArray.get(d) == 13) {
                            dim.add(0, loads.dimVal)
                            dim.add(1, loads.hueValue)
                            dim.add(2, loads.saturationValue)
                            dim.add(3, loads.tempValue)
                        } else {
                            dim.add(loads.index, 100)
                        }
                    }
                }
                local_data = local_data + "\"" + "d" + d + "\"" + ":" + dim.toList() + ","
            }
            local_data = local_data.substring(0, local_data.length - 1)
            local_data += "},\"mdl\":${mdlArray.toList()}}}"
        } else {
            local_data = "{\"type\":12,\"scene_name\":[" + sceneNameData + "],\"a_type\":\"$c_type\",\"conf_wday\":1,\"time_of_day\":\"" + automation.time + "\",\"a_name_old\":\"" + autoOldName + "\",\"status\":$enable,\"sc_idx\":$sceneSize,\"day_of_week\":${routineLocal.toList()},\"otherIR\":{\"devName\":[" + senseNameOther + "],\"devUiud\":[" + senseUiudOther + "],\"devModel\":[" + remoteModelOthers + "],\"devBtn\":[" + remoteButtonOther + "],\"devcNumber\":[" + remoteCnumberOther + "]},\"turn_off\":$turnOffData,\"uiud\":\"${senseDeviceList[0].sense_uiud}\",\"ld_index\":$size,\"a_name\":\"${automation.name}\",\"model\":[" + modelArray + "],\"cNumber\":[" + channelArray + "],\"btnName\":[" + btnNameArray + "]}"
        }
        return local_data
    }


    fun sendAutomationSenseData(automation: AutomationScene, autoOldName: String, scheduleType: String, deviceName: ArrayList<String>, uiudList: ArrayList<String>, turnOff: String, senseDataList: MutableList<AuraSenseConfigure>, sceneSize: Int, sceneNameArray: ArrayList<String>, c_type: String, mdlArray: ArrayList<Int>, favRemoteButtonList: MutableList<RemoteIconModel>, senseDeviceList: MutableList<RemoteModel>): String {
        val states: MutableList<Int> = ArrayList()
        val plc: MutableList<Int> = ArrayList()
        val dim: MutableList<Int> = ArrayList()
        var enable: Int = 0
        var turnOffData = 0
        val temp_reange: ArrayList<Int> = arrayListOf()
        val lux_range: ArrayList<Int> = arrayListOf()
        val hum_range: ArrayList<Int> = arrayListOf()
        val triggerArray: ArrayList<Int> = arrayListOf()
        var triggerValue: Int = 0
        val deviceList: ArrayList<Device> = ArrayList()
        val cNumber = ArrayList<String>(arrayListOf())
        val modeL = ArrayList<String>(arrayListOf())
        val btnName = ArrayList<String>(arrayListOf())
        val senseName = ArrayList<String>(arrayListOf())
        val cNumberOthers = ArrayList<String>(arrayListOf())
        val modeLOthers = ArrayList<String>(arrayListOf())
        val btnNameOthers = ArrayList<String>(arrayListOf())
        val senseNameOthers = ArrayList<String>(arrayListOf())
        val senseUiudArrayOthers = ArrayList<String>(arrayListOf())
        when (automation.property[0].triggerWhen) {
            Constant.DURING_THE_DAY -> {
                automation.property[0].triggerSpecificStartTime = Common.getSunsetSunrise("5:45")
                automation.property[0].triggerSpecificEndTime = Common.getSunsetSunrise("18:00")
            }
            Constant.AT_NIGHT -> {
                automation.property[0].triggerSpecificStartTime = Common.getSunsetSunrise("18:00")
                automation.property[0].triggerSpecificEndTime = Common.getSunsetSunrise("6:00")
            }
            Constant.ANY_TIME -> {
                automation.property[0].triggerSpecificStartTime = "00:00"
                automation.property[0].triggerSpecificEndTime = "23:59"
            }
        }

        for (remote in favRemoteButtonList) {
            var checksameIr = false
            if (remote.name == senseDeviceList[0].aura_sence_name) {
                checksameIr = true
            }
            if (checksameIr) {
                if (remote.channelNumber.isNotEmpty()) {
                    cNumber.add(remote.channelNumber)
                }
                modeL.add(remote.remoteModel!!)
                btnName.add(remote.remoteButtonName!!)
            }
            if (!checksameIr) {
                if (remote.channelNumber.isNotEmpty()) {
                    cNumberOthers.add(remote.channelNumber)
                }
                modeLOthers.add(remote.remoteModel!!)
                btnNameOthers.add(remote.remoteButtonName!!)
                senseNameOthers.add(remote.name!!)
                val sensors = senseDeviceList.find { it.aura_sence_name == remote.name }
                senseUiudArrayOthers.add(sensors?.sense_uiud!!)
            }
        }

        for (sense in senseDataList) {
            var senseNameExist = false
            for (senseFilter in senseName) {
                if (senseFilter == sense.senseDeviceName) {
                    senseNameExist = true
                    break
                }
            }
            if (!senseNameExist) {
                senseName.add(sense.senseDeviceName!!)
            }
        }

        val senseArrayName = senseName.joinToString { it -> "\"${it}\"" }
        val modelArray = modeL.joinToString { it -> "\"${it}\"" }
        val channelArray = cNumber.joinToString { it -> "\"${it}\"" }
        val btnNameArray = btnName.joinToString { it -> "\"${it}\"" }
        val commaSeperatedString = deviceName.joinToString { it -> "\"${it}\"" }
        val sceneNameData = sceneNameArray.joinToString { it -> "\"${it}\"" }
        val senseNameOther = senseNameOthers.joinToString { it -> "\"${it}\"" }
        val senseUiudOther = senseUiudArrayOthers.joinToString { it -> "\"${it}\"" }
        val remoteModelOthers = modeLOthers.joinToString { it -> "\"${it}\"" }
        val remoteCnumberOther = cNumberOthers.joinToString { it -> "\"${it}\"" }
        val remoteButtonOther = btnNameOthers.joinToString { it -> "\"${it}\"" }
        for (senseMatch in senseName) {
            for (sense in senseDataList) {
                if (sense.senseDeviceName == senseMatch) {
                    if (sense.auraSenseName == "Motion") {
                        if (sense.range == 1) {
                            triggerValue += Constant.MTN_DTCT
                        } else {
                            triggerValue += Constant.MTN
                        }
                    } else if (sense.auraSenseName == "Temperature") {
                        if (sense.above) {
                            triggerValue += Constant.TEMP_GTR
                            temp_reange.add(sense.range)
                        } else {
                            triggerValue += Constant.TMP
                            temp_reange.add(sense.range)
                        }
                    } else if (sense.auraSenseName == "Humidity") {
                        if (sense.above) {
                            triggerValue += Constant.HUM_GTR
                            hum_range.add(sense.range)
                        } else {
                            triggerValue += Constant.HUM
                            hum_range.add(sense.range)
                        }
                    } else if (sense.auraSenseName == "Light Intensity") {
                        if (sense.above) {
                            triggerValue += Constant.LUX_GTR
                            lux_range.add(sense.range)
                        } else {
                            triggerValue += Constant.LUX_VAL
                            lux_range.add(sense.range)
                        }
                    }
                }
            }
            triggerArray.add(triggerValue)
        }

        for (allDevice in automation.load) {
            for (devices in allDevice.deviceList) {
                deviceList.add(devices)
            }
        }

        if (turnOff != "Never") {
            turnOffData = turnOff.toInt() / 60
        }

        if (automation.property[0].AutomationEnable) {
            enable = 1
        }
        var local_data = ""
        if (deviceName.size > 0) {
            local_data = "{\"type\":12,\"uiud\":\"${senseDeviceList[0].sense_uiud}\",\"a_old_name\":\"$autoOldName\",\"start_time\":\"${automation.property[0].triggerSpecificStartTime}\",\"end_time\":\"${automation.property[0].triggerSpecificEndTime}\",\"ld_index\":${deviceName.size},\"a_name\":\"${automation.name}\",\"sc_idx\":$sceneSize,\"a_type\":\"$c_type\",\"model\":[" + modelArray + "],\"cNumber\":[" + channelArray + "],\"btnName\":[" + btnNameArray + "],\"otherIR\":{\"devName\":[" + senseNameOther + "],\"devUiud\":[" + senseUiudOther + "],\"devModel\":[" + remoteModelOthers + "],\"devBtn\":[" + remoteButtonOther + "],\"devcNumber\":[" + remoteCnumberOther + "]},\"a_name_old\":\"$autoOldName\",\"conf_wday\":0,\"status\":$enable,\"turn_off\": $turnOffData,\"scene_name\":[" + sceneNameData + "],\"loads\":{\"index_s\":${deviceName.size},\"d_name\":[" + commaSeperatedString + "],\"uiud_s\":[" + uiudList.joinToString { "\"$it\"" } + "],\"state_s\":{"
            for (d in 0 until deviceName.size) {
                var flag = false
                states.clear()
                for (loads in deviceList) {
                    if (deviceName[d].contains(loads.deviceName)) {
                        if (mdlArray.get(d) == 4 || mdlArray.get(d) == 6 || mdlArray.get(d) == 23) {
                            if (!flag) {
                                flag = true
                                states.add(0, 0)
                                states.add(1, 0)
                                states.add(2, 0)
                                states.add(3, 0)
                            }
                            if (loads.isTurnOn) {
                                states.removeAt(loads.index)
                                states.add(loads.index, 1)
                            }
                        } else if (mdlArray.get(d) == 3) {
                            if (loads.isTurnOn) {
                                states.add(0, 1)
                                states.add(1, 0)
                            } else {
                                states.add(0, 0)
                                states.add(1, 1)
                            }
                        } else if (mdlArray.get(d) == 5) {
                            if (!flag) {
                                flag = true
                                states.add(0, 0)
                                states.add(1, 0)
                                states.add(2, 0)
                                states.add(3, 0)
                                states.add(4, 0)
                            }
                            if (loads.isTurnOn) {
                                states.removeAt(loads.index)
                                states.add(loads.index, 1)
                            }
                        } else if (mdlArray.get(d) == 2 || mdlArray.get(d) == 12 || mdlArray.get(d) == 20) {
                            if (!flag) {
                                flag = true
                                states.add(0, 0)
                                states.add(1, 0)
                            }
                            if (loads.isTurnOn) {
                                states.removeAt(loads.index)
                                states.add(loads.index, 1)
                            }
                        } else {
                            states.add(loads.index, if (loads.isTurnOn) 1 else 0)
                        }
                    }
                }
                local_data = local_data + "\"" + d + "\"" + ":" + states.toList() + ","
            }
            local_data = local_data.substring(0, local_data.length - 1)
            local_data += "},\"plc_s\":{"
            for (d in 0 until deviceName.size) {
                var flag = false
                plc.clear()
                for (loads in deviceList) {
                    if (deviceName[d].contains(loads.deviceName)) {
                        if (mdlArray.get(d) == 4 || mdlArray.get(d) == 6 || mdlArray.get(d) == 23) {
                            if (!flag) {
                                flag = true
                                plc.add(0, 0)
                                plc.add(1, 0)
                                plc.add(2, 0)
                                plc.add(3, 0)
                            }
                            plc.removeAt(loads.index)
                            plc.add(loads.index, 1)
                        } else if (mdlArray.get(d) == 3) {
                            if (loads.isTurnOn) {
                                plc.add(0, 1)
                                plc.add(1, 1)
                            } else {
                                plc.add(0, 1)
                                plc.add(1, 1)
                            }
                        } else if (mdlArray.get(d) == 5) {
                            if (!flag) {
                                flag = true
                                plc.add(0, 0)
                                plc.add(1, 0)
                                plc.add(2, 0)
                                plc.add(3, 0)
                                plc.add(4, 0)
                            }
                            plc.removeAt(loads.index)
                            plc.add(loads.index, 1)
                        } else if (mdlArray.get(d) == 2 || mdlArray.get(d) == 12 || mdlArray.get(d) == 20) {
                            if (!flag) {
                                flag = true
                                plc.add(0, 0)
                                plc.add(1, 0)
                            }
                            plc.removeAt(loads.index)
                            plc.add(loads.index, 1)
                        } else {
                            plc.add(loads.index, 1)
                        }
                    }
                }
                local_data = local_data + "\"" + "p" + d + "\"" + ":" + plc.toList() + ","
            }
            local_data = local_data.substring(0, local_data.length - 1)
            local_data += "},\"dim_s\":{"
            for (d in 0 until deviceName.size) {
                var flag = false
                dim.clear()
                for (loads in deviceList) {
                    if (deviceName[d].contains(loads.deviceName)) {
                        if (mdlArray.get(d) == 4 || mdlArray.get(d) == 6 || mdlArray.get(d) == 23) {
                            if (!flag) {
                                flag = true
                                dim.add(0, 100)
                                dim.add(1, 100)
                                dim.add(2, 100)
                                dim.add(3, 100)
                            }
                            dim.removeAt(loads.index)
                            dim.add(loads.index, loads.dimVal)
                        } else if (mdlArray.get(d) == 3) {
                            if (loads.isTurnOn) {
                                dim.add(0, 100)
                                dim.add(1, 100)
                            } else {
                                dim.add(0, 100)
                                dim.add(1, 100)
                            }
                        } else if (mdlArray.get(d) == 5) {
                            if (!flag) {
                                flag = true
                                dim.add(0, 100)
                                dim.add(1, 100)
                                dim.add(2, 100)
                                dim.add(3, 100)
                                dim.add(4, 100)
                            }
                            dim.removeAt(loads.index)
                            dim.add(loads.index, 100)
                        } else if (mdlArray.get(d) == 2 || mdlArray.get(d) == 12 || mdlArray.get(d) == 20) {
                            if (!flag) {
                                flag = true
                                dim.add(0, 100)
                                dim.add(1, 100)
                            }
                            dim.removeAt(loads.index)
                            dim.add(loads.index, 100)
                        } else if (mdlArray.get(d) == 7 || mdlArray.get(d) == 8 || mdlArray.get(d) == 13) {
                            dim.add(0, loads.dimVal)
                            dim.add(1, loads.hueValue)
                            dim.add(2, loads.saturationValue)
                            dim.add(3, loads.tempValue)
                        } else {
                            dim.add(loads.index, 100)
                        }
                    }
                }
                local_data = local_data + "\"" + "d" + d + "\"" + ":" + dim.toList() + ","
            }
            local_data = local_data.substring(0, local_data.length - 1)
            local_data += "},\"mdl\":${mdlArray.toList()}},\"sensors\":{\"senseName\":[" + senseArrayName + "],\"triggers\": ${triggerArray.toList()},\"temp_val\":${temp_reange.toList()},\"lux_val\":${lux_range.toList()},\"humid_val\":${hum_range.toList()}}}"
        } else {
            local_data = "{\"type\":12,\"uiud\":\"${senseDeviceList[0].sense_uiud}\",\"a_old_name\":\"$autoOldName\",\"start_time\":\"${automation.property[0].triggerSpecificStartTime}\",\"end_time\":\"${automation.property[0].triggerSpecificEndTime}\",\"ld_index\":${deviceName.size},\"a_name\":\"${automation.name}\",\"sc_idx\":$sceneSize,\"a_type\":\"$c_type\",\"model\":[" + modelArray + "],\"cNumber\":[" + channelArray + "],\"btnName\":[" + btnNameArray + "],\"otherIR\":{\"devName\":[" + senseNameOther + "],\"devUiud\":[" + senseUiudOther + "],\"devModel\":[" + remoteModelOthers + "],\"devBtn\":[" + remoteButtonOther + "],\"devcNumber\":[" + remoteCnumberOther + "]},\"a_name_old\":\"$autoOldName\",\"conf_wday\":0,\"status\":$enable,\"turn_off\": $turnOffData,\"scene_name\":[" + sceneNameData + "],\"sensors\":{\"senseName\":[" + senseArrayName + "],\"triggers\": ${triggerArray.toList()},\"temp_val\":${temp_reange.toList()},\"lux_val\":${lux_range.toList()},\"humid_val\":${hum_range.toList()}}}"
        }
        return local_data
    }


    fun serializeDatargb(device: Device, uiud: String, type: Boolean, type1: String): String {
        val plc = IntArray(1)
        plc[device.index] = 1
        val states = intArrayOf(0)
        if (!type) {
            if (device.isTurnOn) {
                states[device.index] = 0
            } else {
                states[device.index] = 1
            }
        } else {
            if (device.dimVal > 0) {
                states[device.index] = 1
            } else {
                states[device.index] = 0
            }
        }
        if (type1 == "RGB") {
            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "],\"hue\":${device.hueValue},\"saturation\":${device.saturationValue},\"brightness\":${device.dimVal}}")
        } else {
            return ("{\"type\":4,\"ip\":" + convertIP() + ",\"uiud\":\"" + uiud + "\",\"state\":[" + states[0] + "],\"temperature\":${device.tempValue},\"hue\":${device.hueValue},\"saturation\":${device.saturationValue},\"brightness\":${device.dimVal}}")
        }

    }

    fun serializeRGBTdata(device: Device, uiud: String, module: Int, type: Boolean): String {
        val data: String
        var state = 0
        val led = System.currentTimeMillis() / 1000

        if (!type) {
            if (device.isTurnOn) {
                state = 0
            } else {
                state = 1
            }
        } else {
            if (device.dimVal > 0) {
                state = 1
            } else {
                state = 0
            }
        }
        if (module == 7) {
            data = "{\"state\":{\"desired\": {\"uiud\": \"" + uiud + "\" ,\"led\": " + led + ",\"hue\": " + device.hueValue + ",\"saturation\": " + device.saturationValue + ",\"state\": {\"s" + device.index + "\":" + state + "},\"dim\": {\"d" + device.index + "\":" + device.dimVal + "}}}}"
        } else {
            data = "{\"state\":{\"desired\": {\"uiud\": \"" + uiud + "\" ,\"led\": " + led + ",\"temperature\": " + device.tempValue + ",\"state\": {\"s" + device.index + "\":" + state + "},\"brightness\": " + device.dimVal + "}}}"
        }
        return data
    }

    fun localDataDelete(name: String, delete: String, type: String, senseUiud: String?): String {
        if (type == "Automation") {
            return "{\"type\":12,\"uiud\":\"$senseUiud\",\"a_type\":\"$delete\",\"a_name\":\"$name\"}"
        } else {
            return "{\"type\":11,\"uiud\":\"$senseUiud\",\"s_type\":\"$delete\",\"scene_name\":\"$name\"}"
        }
    }

    fun confirmRemoteData(remoteButton: RemoteIconModel, senseUiud: String?, modelNumber: String?): String {
        return "{\"type\":13,\"dType\":\"${remoteButton.dType}\",\"rArr\": ${remoteButton.rArr?.asList()},\"state\":${remoteButton.state.asList()},\"proto\":${remoteButton.proto},\"sz\":${remoteButton.sz},\"fz\":${remoteButton.fz},\"value\":${remoteButton.value},\"uiud\":\"$senseUiud\",\"model\":\"$modelNumber\",\"btnName\":\"${remoteButton.remoteButtonName}\"}"
    }

    fun triggerRemoteButton(remoteButton: RemoteIconModel, modelNumber: String?, senseUiud: String): String {
        return "{\"type\":14,\"dType\":\"${remoteButton.dType}\",\"model\":\"$modelNumber\",\"btnName\":\"${remoteButton.remoteButtonName}\",\"uiud\":\"$senseUiud\",\"cNumber\":\"${remoteButton.channelNumber}\"}"
    }

    fun notifyDevice(senseName: String, senseUiud: String, deviceUiud: String, SENSE_MODE: String?): String {
        return "{\"type\":15,\"master_name\":\"$senseName\",\"master_uiud\":\"$senseUiud\",\"uiud\":\"$deviceUiud\",\"mode\":\"$SENSE_MODE\"}"
    }

    fun upgradeType(url: String, uiud: String, wifi: Int): String {
        return "{\"type\":9,\"url\":\"$url\",\"uiud\":\"$uiud\",\"WIFI\":\"$wifi\"}"
    }

    fun setAdaptiveLight(uiud: String, isAaptive: Boolean): String {
        return "{\"type\":5,\"uiud\":\"$uiud\",\"isAdaptive\":$isAaptive,\"userTZ\":\"${Constant.TIME_ZONE}\"}"
    }

}