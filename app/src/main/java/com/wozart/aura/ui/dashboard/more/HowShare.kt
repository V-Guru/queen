package com.wozart.aura.ui.dashboard.more

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wozart.aura.R
import kotlinx.android.synthetic.main.activity_invite_help.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-11-28
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class HowShare : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_help)

        home.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}