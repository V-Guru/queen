package com.wozart.aura.ui.wifisettings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.wozart.aura.R
import kotlinx.android.synthetic.main.layout_zmote_configuration.*

class ZemoteConfigurationFragment : androidx.fragment.app.Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootview = inflater.inflate(R.layout.layout_zmote_configuration,container,false)
        return rootview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUI()
        back.setOnClickListener {
            val manager = activity!!.supportFragmentManager
            val trans = manager.beginTransaction()
            trans.remove(this)
            trans.commit()
            manager.popBackStack()
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
        webview.loadUrl("http://www.zmote.io/start")
    }
    class CustomWebViewClient : WebViewClient(){

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view?.loadUrl(request?.url.toString())
            return true
        }

    }
}