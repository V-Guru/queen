package com.wozart.aura.ui.dashboard.more

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import android.view.Window
import com.wozart.aura.R
import com.wozart.aura.aura.ui.createautomation.SetGeoAutomationFinishActivity
import com.wozart.aura.utilities.dialog.SingleBtnDialog
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.dialogue_feedback.*
import org.jetbrains.anko.design.longSnackbar

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
class FeedbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        card_feedback.setOnClickListener {
            showPopUp(true)
        }

        card_faq.setOnClickListener {
            val intent = Intent(this,FaqWozart::class.java)
            startActivity(intent)
        }
        card_report.setOnClickListener {
            showPopUp(false)
        }
        cvContact.setOnClickListener {
            val intent = Intent(this, SetGeoAutomationFinishActivity::class.java)
            startActivity(intent)
        }

        home.setOnClickListener {
            finish()
        }
    }

    fun showPopUp(b: Boolean) {
        val dialogue = Dialog(this)
        dialogue.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogue.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogue.setContentView(R.layout.dialogue_feedback)
        if(!b){
            dialogue.tv_title.text =getString(R.string.trouble_with_wozart)
            dialogue.message.text = getString(R.string.happy_to_help)
            dialogue.info_report.hint = getString(R.string.brief_description)
        }else{
            dialogue.info_report.hint = getString(R.string.your_query)
        }
        val name = dialogue.device_name.findViewById<AppCompatEditText>(R.id.device_name)
        val issue = dialogue.info_report.findViewById<AppCompatEditText>(R.id.info_report)
        dialogue.btn_report_issue.setOnClickListener {
            val device_name = name.text
            val issue_report = issue.text
            if((device_name!!.isEmpty()) and (issue_report.isNullOrEmpty())){
                SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.please_fill_all_details)).show()
                dialogue.dismiss()
                //longSnackbar(this.findViewById(android.R.id.content),"Please fill all the field.")
            }else{
                SingleBtnDialog.with(this).setHeading(getString(R.string.alert)).setMessage(getString(R.string.issue_submitted)).show()
                //longSnackbar(this.findViewById(android.R.id.content),"Your issue report has been submitted.")
                dialogue.dismiss()
            }

        }
        dialogue.btn_cancel_issue.setOnClickListener {
            dialogue.dismiss()
        }
        dialogue.show()
    }
}