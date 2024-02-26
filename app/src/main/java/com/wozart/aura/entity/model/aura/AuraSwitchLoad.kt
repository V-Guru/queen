package com.wozart.aura.entity.model.aura


import java.io.Serializable

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
class AuraSwitchLoad : Serializable {
    var name: String? = null
    var favourite: Boolean? = false
    var dimmable: Boolean? = false
    var icon: Int? = 0
    var index: Int? = 0
    var type: String? = null
    var hue: Int = 100
    var saturation: Int = 100
    var brightness: Int = 100
    var tempLight: Int = 100
    var module: Int = -1
    var curtainState: String = ""
    var curtainState0 = -1
    var curtainState1 = -1
    var isAdaptive: Boolean = false
    var loadType: String? = null


    fun defaultLoadList(mdl: Int): MutableList<AuraSwitchLoad> {
        val loadList: MutableList<AuraSwitchLoad> = ArrayList()
        val load = AuraSwitchLoad()
        val load1 = AuraSwitchLoad()
        val load2 = AuraSwitchLoad()
        val load3 = AuraSwitchLoad()
        load1.name = "Switch 1"
        load1.dimmable = true
        load1.favourite = true
        load1.icon = 4
        load1.index = 0
        load1.loadType = "Switch"
        load1.module = mdl
        loadList.add(load1)

        load2.name = "Switch 2"
        load2.dimmable = true
        load2.favourite = true
        load2.icon = 4
        load2.index = 1
        load2.loadType = "Switch"
        load2.module = mdl
        loadList.add(load2)

        load3.name = "Switch 3"
        load3.dimmable = true
        load3.favourite = true
        load3.icon = 4
        load3.index = 2
        load3.loadType = "Switch"
        load3.module = mdl
        loadList.add(load3)

        load.name = "Switch 4"
        load.dimmable = false
        load.favourite = true
        load.icon = 4
        load.index = 3
        load.loadType = "Switch"
        load.module = mdl
        loadList.add(load)
        return loadList
    }

    fun fiveLoadList(mdl: Int): MutableList<AuraSwitchLoad> {
        val loadList: MutableList<AuraSwitchLoad> = ArrayList()
        val load = AuraSwitchLoad()
        val load1 = AuraSwitchLoad()
        val load2 = AuraSwitchLoad()
        val load3 = AuraSwitchLoad()
        val load4 = AuraSwitchLoad()
        load1.name = "Switch 1"
        load1.dimmable = true
        load1.favourite = true
        load1.icon = 4
        load1.index = 0
        load1.loadType = "Switch"
        load1.module = mdl
        loadList.add(load1)

        load2.name = "Switch 2"
        load2.dimmable = true
        load2.favourite = true
        load2.icon = 4
        load2.index = 1
        load2.loadType = "Switch"
        load2.module = mdl
        loadList.add(load2)

        load3.name = "Switch 3"
        load3.dimmable = true
        load3.favourite = true
        load3.icon = 4
        load3.index = 2
        load3.loadType = "Switch"
        load3.module = mdl
        loadList.add(load3)

        load.name = "Switch 4"
        load.dimmable = false
        load.favourite = true
        load.icon = 4
        load.index = 3
        load.loadType = "Switch"
        load.module = mdl
        loadList.add(load)

        load4.name = "Switch 5"
        load4.dimmable = false
        load4.favourite = true
        load4.icon = 4
        load4.index = 4
        load4.loadType = "Switch"
        load4.module = mdl
        loadList.add(load4)
        return loadList
    }

    fun defaultSwitchProLoadList(mdl: Int): MutableList<AuraSwitchLoad> {
        val loadList: MutableList<AuraSwitchLoad> = ArrayList()
        val load = AuraSwitchLoad()
        val load1 = AuraSwitchLoad()
        val load2 = AuraSwitchLoad()
        val load3 = AuraSwitchLoad()
        load1.name = "Switch 1"
        load1.dimmable = false
        load1.favourite = true
        load1.icon = 4
        load1.index = 0
        load1.loadType = "Switch"
        load1.module = mdl
        loadList.add(load1)

        load2.name = "Switch 2"
        load2.dimmable = false
        load2.favourite = true
        load2.icon = 4
        load2.index = 1
        load2.loadType = "Switch"
        load2.module = mdl
        loadList.add(load2)

        load3.name = "Fan"
        load3.dimmable = true
        load3.favourite = true
        load3.icon = 3
        load3.index = 2
        load3.loadType = "Fan"
        load3.module = mdl
        loadList.add(load3)
        if (mdl != 14) {
            load.name = "Light"
            load.dimmable = true
            load.favourite = true
            load.icon = 0
            load.index = 3
            load.loadType = "Light"
            load.module = mdl
            loadList.add(load)
        } else {
            load.name = "Switch 3"
            load.dimmable = false
            load.favourite = true
            load.icon = 0
            load.loadType = "Switch"
            load.index = 3
            load.module = mdl
            loadList.add(load)
        }

        return loadList
    }

    fun twoModuleLoad(mdl: Int): MutableList<AuraSwitchLoad> {
        val listLoad: MutableList<AuraSwitchLoad> = ArrayList()
        val load1 = AuraSwitchLoad()
        val load2 = AuraSwitchLoad()
        load1.name = "Switch 1"
        load1.dimmable = false
        load1.favourite = true
        load1.icon = 4
        load1.index = 0
        load1.loadType = "Switch"
        load1.module = mdl
        listLoad.add(load1)

        load2.name = "Switch 2"
        load2.dimmable = false
        load2.favourite = true
        load2.icon = 4
        load2.index = 1
        load2.loadType = "Switch"
        load2.module = mdl
        listLoad.add(load2)

        return listLoad
    }

    fun twoModuleDimmerLoad(mdl: Int): MutableList<AuraSwitchLoad> {
        val listLoad: MutableList<AuraSwitchLoad> = ArrayList()
        val load1 = AuraSwitchLoad()
        val load2 = AuraSwitchLoad()
        load1.name = "Light"
        load1.dimmable = true
        load1.favourite = true
        load1.icon = 4
        load1.index = 0
        load1.loadType = "Light"
        load1.module = mdl
        listLoad.add(load1)

        load2.name = "Switch"
        load2.dimmable = false
        load2.favourite = true
        load2.icon = 4
        load2.index = 1
        load2.loadType = "Switch"
        load2.module = mdl
        listLoad.add(load2)

        return listLoad
    }

    fun auraPlugLoad(mdl: Int): MutableList<AuraSwitchLoad> {
        val listLoad: MutableList<AuraSwitchLoad> = ArrayList()
        val load1 = AuraSwitchLoad()
        load1.name = "Plug"
        load1.dimmable = false
        load1.favourite = true
        load1.icon = if (mdl == 1) 8 else 7
        load1.index = 0
        load1.loadType = "Plug"
        load1.module = mdl
        listLoad.add(load1)
        return listLoad
    }

    fun rgbLoads(mdl: Int): MutableList<AuraSwitchLoad> {
        val listLoad: MutableList<AuraSwitchLoad> = ArrayList()
        val load1 = AuraSwitchLoad()
        load1.name = "Light"
        load1.dimmable = true
        load1.favourite = true
        load1.icon = 0
        load1.type = "rgbDevice"
        load1.index = 0
        load1.loadType = "rgbLight"
        load1.brightness = 100
        load1.saturation = 0
        load1.hue = 0
        load1.module = mdl
        listLoad.add(load1)
        return listLoad
    }

    fun rgbTunableLoads(mdl: Int): MutableList<AuraSwitchLoad> {
        val listLoad: MutableList<AuraSwitchLoad> = ArrayList()
        val load1 = AuraSwitchLoad()
        load1.name = "Light"
        load1.dimmable = true
        load1.favourite = true
        load1.icon = 0
        load1.type = "tunableDevice"
        load1.index = 0
        load1.loadType = "tunableLight"
        load1.brightness = 100
        load1.saturation = 0
        load1.hue = 0
        load1.tempLight = 0
        load1.module = mdl
        listLoad.add(load1)
        return listLoad
    }

    fun getLoadType(mdl: Int, index: Int): String {
        when (mdl) {
            1 -> return "Plug"
            2, 4, 5, 12, 20 -> return "Switch"
            3 -> return "Curtain"
            6, 23, 14 -> if (index == 2) {
                return "Fan"
            } else if (index == 3 && (mdl == 6 || mdl == 23)) {
                return "Light"
            } else {
                return "Switch"
            }
            7, 13 -> {
                return "rgbLight"
            }
            8 -> return "tunableLight"
            11 -> if (index == 0) return "Light" else return "Switch"
            else -> return "Switch"

        }
    }

}