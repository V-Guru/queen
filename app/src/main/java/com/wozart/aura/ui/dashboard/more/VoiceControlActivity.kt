package com.wozart.aura.ui.dashboard.more

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wozart.aura.R
import kotlinx.android.synthetic.main.activity_voiceontrol.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-11-26
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class VoiceControlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voiceontrol)


        aura_configure_card.setOnClickListener {

            val intent = Intent(this, VoiceControlWebPage::class.java)
            startActivity(intent)
        }

        aura_configure_card_2.setOnClickListener {
            val intent = Intent(this, GoogleAssistantActivity::class.java)
            startActivity(intent)
        }

        aura_configure_card_3.setOnClickListener {

            val intent = Intent(this, SmartThingActivity::class.java)
            startActivity(intent)
        }

        home.setOnClickListener {
            finish()
        }
    }
}