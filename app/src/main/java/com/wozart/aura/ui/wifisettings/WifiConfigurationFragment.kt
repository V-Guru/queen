package com.wozart.aura.ui.wifisettings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.security.NetworkSecurityPolicy
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import com.wozart.aura.R
import com.wozart.aura.ui.auraswitchlist.AuraListActivity
import kotlinx.android.synthetic.main.fragment_wifi_configration.*
import android.webkit.WebViewClient


class  WifiConfigurationFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wifi_configration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUI()

        back.setOnClickListener {
            requireActivity().finish()
//            val manager = activity!!.supportFragmentManager
//            val trans = manager.beginTransaction()
//            trans.remove(this)
//            trans.commit()
//            manager.popBackStack()
        }

        tvNext.setOnClickListener {
            val intent = Intent(activity, AuraListActivity::class.java)
            startActivity(intent)
        }

        button.setOnClickListener {
            webview.reload()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadUI() {

        webview.webViewClient = CustomWebViewClient()
        webview.settings.javaScriptEnabled = true
        webview.settings.loadWithOverviewMode = true
        webview.settings.useWideViewPort = true
        webview.settings.allowFileAccess = true
        webview.settings.domStorageEnabled = true
        webview.settings.allowFileAccessFromFileURLs = true
        webview.settings.setSupportZoom(true)
        webview.settings.javaScriptCanOpenWindowsAutomatically = true
        webview.loadUrl(getString(R.string.wifi_settings_configure_url))

    }

    class CustomWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view?.loadUrl(request?.url.toString())
            return true
        }

    }

}
