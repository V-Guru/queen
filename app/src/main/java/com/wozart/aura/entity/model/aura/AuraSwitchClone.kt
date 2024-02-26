package com.wozart.aura.entity.model.aura

import java.util.*

/***************************************************************************
 * File Name :
 * Author : Aarth Tandel
 * Date of Creation : 18/06/18
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class AuraSwitchClone {

    private var aurAwsSwitch : AuraSwitch = AuraSwitch()

    fun AuraSwitchClone(device : AuraSwitch){
        this.aurAwsSwitch = device
    }

    fun updateState(node :Int){
        for (i in 0..3) {
            if (i == node) {
                if (this.aurAwsSwitch.state[i] == 0)
                    this.aurAwsSwitch.state[i] = 1
                else
                    this.aurAwsSwitch.state[i] = 0
            }
        }
    }
}
