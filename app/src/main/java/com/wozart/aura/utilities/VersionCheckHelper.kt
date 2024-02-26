package com.wozart.aura.utilities

import android.os.AsyncTask
import com.wozart.aura.BuildConfig
import org.jsoup.Jsoup
import java.io.IOException


/**
 * Created by Saif on 02/03/21.
 * mds71964@gmail.com
 */
class VersionCheckHelper : AsyncTask<String,String,String>() {
    private var newVersion: String? = null
    override fun doInBackground(vararg p0: String?): String {
        try {
            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "&hl=en")
                    .timeout(10000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select(".hAyfc .htlgb")
                    .get(7)
                    .ownText()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return newVersion!!
    }
}