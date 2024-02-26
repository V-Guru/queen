package com.wozart.aura.ui.dashboard.more

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.wozart.aura.R
import kotlinx.android.synthetic.main.activity_about.*
import java.text.SimpleDateFormat
import java.util.*

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
class AboutUsActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NewApi", "SimpleDateFormat", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val versionName = this.packageManager
                .getPackageInfo(this.packageName, 0).versionName
        version_number.text = versionName
        val versionBuildDate = this.packageManager.getPackageInfo(this.packageName,0).lastUpdateTime
        val date = SimpleDateFormat("dd/MM/yyyy")
        val date_got = date.format(Date(versionBuildDate))
        build_date_info.text = " $date_got"

        home.setOnClickListener {
            finish()
        }

        info_company_mail.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.wozart.com/"))
            startActivity(intent)
        }
    }
}