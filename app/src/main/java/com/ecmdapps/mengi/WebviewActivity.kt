package com.ecmdapps.mengi

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import kotlinx.android.synthetic.main.activity_webview.*
import java.util.*
import android.webkit.WebView




class WebviewActivity : AppCompatActivity(){
    private lateinit var webview : WebView
    private lateinit var swipeLayout: SwipeRefreshLayout

    companion object {
        var pageID: String = ""
        var lastViewLink = "http://topwebfiction.com"
        var lastViewFavicon : Bitmap? = null
        var sourceId = 0L
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        setSupportActionBar(toolbar)

        try {
            WebviewActivity.lastViewLink = intent.getStringExtra(SourceDbManager.colLastViewLink)
            WebviewActivity.sourceId = intent.getLongExtra(SourceDbManager.colId, 0)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        swipeLayout = findViewById<SwipeRefreshLayout>(R.id.swipe)
        swipeLayout.setOnRefreshListener {
            webview.loadUrl(lastViewLink)
        }

        webview = findViewById<WebView>(R.id.webview)
        webview.settings.javaScriptEnabled = true
        webview.settings.loadsImagesAutomatically = true
        webview.settings.setAppCacheEnabled(true)
        webview.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webview.webViewClient = MengiWebViewClient(this)
        webview.webChromeClient = MengiWebChromeClient()

        val cookieManager = CookieManager.getInstance()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webview,true)
        } else {
            cookieManager.setAcceptCookie(true)
        }

        webview.loadUrl(lastViewLink)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_webview, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val history = History(this)
        return when (item.itemId) {
            R.id.action_history -> history.showWithId(sourceId)
            else -> super.onOptionsItemSelected(item)
        }
    }

    class MengiWebChromeClient : WebChromeClient(){
        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            super.onReceivedIcon(view, icon)
            lastViewFavicon = icon
        }
    }

    class MengiWebViewClient(ctxt: Context) : WebViewClient() {
        private var context = ctxt

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            val history = History(context)
            val a = context as Activity
            val swipeLayout: SwipeRefreshLayout = a.findViewById<SwipeRefreshLayout>(R.id.swipe)
            swipeLayout.isRefreshing = false
            lastViewLink = url ?: lastViewLink
            Log.d("pagefinish sourceid", sourceId.toString())
            pageID = UUID.nameUUIDFromBytes(lastViewLink.toByteArray()).toString()
            sourceId = history.store(lastViewLink, sourceId, pageID, lastViewFavicon, view?.title)
        }

        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            super.onReceivedHttpError(view, request, errorResponse)
        }
    }
}