package com.wozart.aura.ui.dashboard.more

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wozart.aura.R
import kotlinx.android.synthetic.main.activity_help.*

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
class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        aura_configure_card.setOnClickListener {
            val intent = Intent(this,AuraConfigurationActicity::class.java)
            startActivity(intent)
        }

        aura_create_room.setOnClickListener {
            val intent = Intent(this,HelpHome::class.java)
            startActivity(intent)
        }
        aura_create_home.setOnClickListener {
            val intent = Intent(this,HelpRoomCreate::class.java)
            startActivity(intent)
        }
        aura_create_scenes.setOnClickListener {
            val intent = Intent(this,HelpScene::class.java)
            startActivity(intent)
        }
        aura_create_automation.setOnClickListener {
            val intent = Intent(this,HowAutomation::class.java)
            startActivity(intent)
        }

        aura_invite_member.setOnClickListener {
            val intent = Intent(this,HowShare::class.java)
            startActivity(intent)
        }

        home.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}