package com.wozart.aura.data.device

import com.wozart.aura.entity.model.scene.Scene
import com.wozart.aura.entity.model.scene.SceneList
import com.wozart.aura.ui.createautomation.RoomModel
import com.wozart.aura.ui.dashboard.Device

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 11/06/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class SceneHandler {
    fun convertToSceneModel(rooms: ArrayList<RoomModel>): ArrayList<SceneList> {
        val scenes: ArrayList<SceneList> = ArrayList()
        for (room in rooms) {
            for (device in room.deviceList) {
                if (device.deviceName != "Automation") {
                    val dummyScene = SceneList()
                    if (scenes.isEmpty()) {
                        dummyScene.device = device.deviceName
                        dummyScene.loads.add(device)
                        scenes.add(dummyScene)

                    } else {
                        val iterate = scenes.listIterator()
                        var scene: SceneList? = null
                        while (iterate.hasNext())
                            scene = iterate.next()

                        if (scene?.device == device.deviceName) {
                            scene.loads.add(device)
                        } else {
                            dummyScene.device = device.deviceName
                            dummyScene.loads.add(device)
                            scenes.add(dummyScene)
                        }
                    }
                }
            }
        }
        return scenes
    }

    fun convertToAllLoad(devicelist: ArrayList<Device>): ArrayList<SceneList> {
        val scenes: ArrayList<SceneList> = ArrayList()
        for (device in devicelist) {
            val dummyScene = SceneList()
            if (scenes.isEmpty()) {
                dummyScene.device = device.deviceName
                dummyScene.loads.add(device)
                scenes.add(dummyScene)
            } else {
                val iterate = scenes.listIterator()
                var scene: SceneList? = null
                while (iterate.hasNext())
                    scene = iterate.next()

                if (scene?.device == device.deviceName) {
                    scene.loads.add(device)
                } else {
                    dummyScene.device = device.deviceName
                    dummyScene.loads.add(device)
                    scenes.add(dummyScene)
                }
            }
        }
        return scenes
    }
}