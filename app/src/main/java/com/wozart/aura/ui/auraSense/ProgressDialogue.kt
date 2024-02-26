package com.wozart.aura.ui.auraSense

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import com.wozart.aura.R

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2020-02-06
 * Description :
 * Revision History :
 * ____________________________________________________________________________

 *****************************************************************************/
open class ProgressDialogue {
    companion object {
        fun progressDialog(context: Context): Dialog {
            val dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.progress_listner_dialog, null)
            dialog.setContentView(inflate)
            dialog.window?.setGravity(Gravity.BOTTOM)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            return dialog
        }
    }
}