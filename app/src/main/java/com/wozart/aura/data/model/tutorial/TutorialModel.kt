package com.wozart.aura.data.model.tutorial

import android.view.View


/**
 * Created by Saif on 21/09/21.
 * mds71964@gmail.com
 */
class TutorialModel {
    var id: View? = null
    var title: String? = null
    var message: String? = null
    var x: Float? = null
    var y: Float? = null
    var height: Float = 0f
    var width: Float = 0f
    var isCircle: Boolean = false
    var radius: Float = 25f
    var overLayY: Float = 0f
    var overLayX: Float = 0f
}