package com.wozart.aura.ui.dashboard.more

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.wozart.aura.R
import kotlinx.android.synthetic.main.activity_device_faq.*

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
class DeviceConfigureFaq : AppCompatActivity() {

    var card_openCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_faq)

        card_feedback.setOnClickListener {

            if(!card_openCheck){
                card_openCheck  = true
                layout_details_1.visibility = View.VISIBLE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
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
                layout_details_3.visibility = View.GONE
                layout_details_2.visibility = View.VISIBLE
                layout_details_4.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                FAQ_.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_2.visibility = View.GONE
                FAQ_.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
            }
        }
        card_faq_1.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.VISIBLE
                layout_details_4.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                FAQ_1.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_3.visibility = View.GONE
                FAQ_1.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)

            }

        }
        card_faq_2.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_4.visibility = View.VISIBLE
                layout_details_5.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                img_arrow.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_4.visibility = View.GONE
                img_arrow.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)

            }

        }
        card_faq_3.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_5.visibility = View.VISIBLE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                img_arrow_1.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_5.visibility = View.GONE
                img_arrow_1.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)

            }

        }
        card_faq_4.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_6.visibility = View.VISIBLE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                img_arrow_2.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_6.visibility = View.GONE
                img_arrow_2.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
            }

        }
        card_faq_5.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_7.visibility = View.VISIBLE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                img_arrow_3.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_7.visibility = View.GONE
                img_arrow_3.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)

            }

        }

        card_faq_6.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.VISIBLE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                img_arrow_4.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)

            }else{
                card_openCheck = false
                layout_details_8.visibility = View.GONE
                img_arrow_4.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
            }

        }
        card_faq_7.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_9.visibility = View.VISIBLE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                img_arrow_5.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_9.visibility = View.GONE
                img_arrow_5.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
            }

        }
        card_faq_8.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_10.visibility = View.VISIBLE
                layout_details_11.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                img_arrow_6.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_10.visibility = View.GONE
                img_arrow_6.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
            }

        }
        card_faq_9.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_11.visibility = View.VISIBLE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.GONE
                img_arrow_7.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_11.visibility = View.GONE
                img_arrow_7.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
            }

        }
        card_faq_10.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_12.visibility = View.VISIBLE
                layout_details_13.visibility = View.GONE
                img_arrow_8.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_12.visibility = View.GONE
                img_arrow_8.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)

            }
        }

        card_faq_11.setOnClickListener {
            if(!card_openCheck){
                card_openCheck = true
                layout_details_1.visibility = View.GONE
                layout_details_2.visibility = View.GONE
                layout_details_3.visibility = View.GONE
                layout_details_5.visibility = View.GONE
                layout_details_4.visibility = View.GONE
                layout_details_6.visibility = View.GONE
                layout_details_7.visibility = View.GONE
                layout_details_8.visibility = View.GONE
                layout_details_9.visibility = View.GONE
                layout_details_10.visibility = View.GONE
                layout_details_12.visibility = View.GONE
                layout_details_13.visibility = View.VISIBLE
                img_arrow_8.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }else{
                card_openCheck = false
                layout_details_13.visibility = View.GONE
                img_arrow_8.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
            }
        }

        home.setOnClickListener {
            finish()
        }
    }
}