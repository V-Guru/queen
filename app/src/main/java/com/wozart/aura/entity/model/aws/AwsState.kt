package com.wozart.aura.entity.model.aws

import java.util.HashMap

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 04/05/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class AwsState {
    var led = 0
    val state = HashMap<String, Any>()
    val dim = HashMap<String, Int>()
    val user: String? = null
    val uiud: String? = null
    var brightness : Int = 100
    var saturation : Int = 0
    var hue : Int = 0
    var temperature : Int = 0
    var source : String ?= null
    var onlineStatus : Boolean = true


    fun AwsState() {
        this.state["s0"] = 0
        this.state["s1"] = 1
        this.state["s2"] = 0
        this.state["s3"] = 0
        this.dim["d0"] = 100
        this.dim["d2"] = 100
        this.dim["d3"] = 100
        this.dim["d4"] = 100
    }

    fun setState(state: Int) {
        val value = "s$state"
        if (this.state[value] == 1)
            this.state[value] = 0
        else
            this.state[value] = 1
    }

    fun setDimm(dimm: Int) {
        val value = "d$dim"
        if (this.dim[value] == 1)
            this.dim[value] = 100
        else
            this.dim[value] = 1
    }

//    fun getStates(): IntArray {
//        val states = IntArray(4)
//        for (x in states.indices) {
//            states[x] = this.state["s$x"]!!
//        }
//        return states
//    }

    fun getDims(): IntArray {
        val dims = IntArray(4)
        for (x in dims.indices) {
            dims[x] = this.dim["d$x"]!!
        }
        return dims
    }
}