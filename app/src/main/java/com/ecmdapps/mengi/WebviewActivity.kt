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
import android.view.*
import android.webkit.*
import kotlinx.android.synthetic.main.activity_webview.*
import java.util.*
import android.webkit.WebView

class WebviewActivity : AppCompatActivity(){
    private lateinit var webView : AutoHidingWebView
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
            lastViewLink = intent.getStringExtra(SourceDbManager.colLastViewLink)
            sourceId = intent.getLongExtra(SourceDbManager.colId, 0)
        } catch (ex: Exception) {
            Log.d("ErrorInWebviewGet", ex.message)
        }

        swipeLayout = findViewById(R.id.swipe)
        swipeLayout.setOnRefreshListener {
            webView.loadUrl(lastViewLink)
        }

        webView = findViewById(R.id.webview)
        webView.setGD(GestureDetector(this, ScrollDetectorListener(this)))
        webView.settings.javaScriptEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.setAppCacheEnabled(true)
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.webViewClient = AWebViewClient(this)
        webView.webChromeClient = AWebChromeClient()

        val cookieManager = CookieManager.getInstance()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView,true)
        } else {
            cookieManager.setAcceptCookie(true)
        }

        webView.loadUrl(lastViewLink)
    }

    class ScrollDetectorListener(context: Context) : GestureDetector.SimpleOnGestureListener() {
        private val wa : WebviewActivity = context as WebviewActivity
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if ( e1 == null || e2 == null ) return false
            if ( e1.pointerCount > 1 || e2.pointerCount > 1) return false
            else {
                try {
                    if (e1.y - e2.y > 20) {
                        wa.supportActionBar?.hide()
                        wa.findViewById<AutoHidingWebView>(R.id.webview).invalidate()
                        return false
                    } else if (e2.y - e1.y > 20 ){
                        wa.supportActionBar?.show()
                        wa.findViewById<AutoHidingWebView>(R.id.webview).invalidate()
                        return false
                    }
                } catch (ex: Exception) {
                    wa.findViewById<AutoHidingWebView>(R.id.webview).invalidate()
                }
                return false
            }
        }
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

    class AWebChromeClient : WebChromeClient(){
        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            super.onReceivedIcon(view, icon)
            lastViewFavicon = icon
        }
    }

    class AWebViewClient(ctxt: Context) : WebViewClient() {
        private var context = ctxt

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            val history = History(context)
            val a = context as Activity
            val swipeLayout: SwipeRefreshLayout = a.findViewById(R.id.swipe)
            swipeLayout.isRefreshing = false
            lastViewLink = url ?: lastViewLink
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