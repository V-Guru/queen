package com.wozart.aura.ui.dashboard.more

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wozart.aura.R
import kotlinx.android.synthetic.main.activity_faq.*

/***************************************************************************
 * File Name :
 * Author : Saif
 * Date of Creation : 2019-12-03
 * Description :
 * Revision History :
 * ____________________________________________________________________________
 * 29/12/17  Aarth Tandel - Initial Commit
 * ____________________________________________________________________________
 * 29/12/17 Version 1.0
 * ____________________________________________________________________________
 *
 *****************************************************************************/
class FaqWozart : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        card_feedback.setOnClickListener {
            val intent = Intent(this, AboutWozartFaq::class.java)
            startActivity(intent)
        }
        card_faq.setOnClickListener {
            val intent = Intent(this, UsingWozartFaq::class.java)
            startActivity(intent)
        }
        card_faq_1.setOnClickListener {
            val intent = Intent(this, DeviceConfigureFaq::class.java)
            startActivity(intent)
        }
        card_faq_3.setOnClickListener {
            val intent = Intent(this, VoiceControlFaq::class.java)
            startActivity(intent)
        }
        card_faq_4.setOnClickListener {
            val intent = Intent(this, InviteMemberFaq::class.java)
            startActivity(intent)
        }
        card_faq_5.setOnClickListener {
            val intent = Intent(this, DeviceSecurityFaq::class.java)
            startActivity(intent)
        }

        home.setOnClickListener {
            finish()
        }
    }
}