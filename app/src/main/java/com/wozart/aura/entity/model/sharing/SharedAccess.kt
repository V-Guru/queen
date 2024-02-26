package com.wozart.aura.entity.model.sharing

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

class SharedAccess {
    var access = "false"
    var home: String? = null
    var userId: String? = null
    var name: String? = null

     constructor(access: String, home: String, userId: String, name: String) {
        this.access = access
        this.home = home
        this.userId = userId
        this.name = name
    }
}