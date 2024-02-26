package com.wozart.aura.ui.dashboard.more

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.wozart.aura.R
import kotlinx.android.synthetic.main.activity_data_security_faq.*

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
class DeviceSecurityFaq : AppCompatActivity(){

    var card_openCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_security_faq)

        card_feedback.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.VISIBLE
                layout_details_2.visibility = View.GONE
                icon_click.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_1.visibility = View.GONE
                icon_click.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
            }

        }

        card_faq.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.VISIBLE
                FAQ_.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_2.visibility = View.GONE
                FAQ_.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
            }
        }

        home.setOnClickListener {
            finish()
        }
    }
}