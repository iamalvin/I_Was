package com.ecmdapps.mengi

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream
import java.util.*


class WebviewActivity : AppCompatActivity(){
    private lateinit var webview : WebView
    private lateinit var swipeLayout: SwipeRefreshLayout

    companion object {
        var pageID: String = ""
        var lastViewLink = "http://topwebfiction.com"
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

    class MengiWebChromeClient : WebChromeClient()

    class MengiWebViewClient(ctxt: Context) : WebViewClient() {
        private var context = ctxt

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            pageID = UUID.randomUUID().toString()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            val a: Activity = context as Activity
            val swipeLayout: SwipeRefreshLayout = a.findViewById<SwipeRefreshLayout>(R.id.swipe)
            swipeLayout.isRefreshing = false
            lastViewLink = url ?: lastViewLink
            Log.d("pagefinish sourceid", sourceId.toString())
            sourceId = store(url, sourceId, pageID, view?.favicon, view?.title)
        }

        private fun store(url: String?, sourceId: Long, pageID: String, favicon: Bitmap?, currentTitle: String?) : Long {
            val title = currentTitle ?: url
            val fav = favicon ?: BitmapFactory.decodeResource(context.resources, R.drawable.default_favicon)

            val stream = ByteArrayOutputStream()
            fav.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val fByteArray = stream.toByteArray()

            val values = ContentValues()

            values.put(SourceDbManager.colId, sourceId)
            values.put(SourceDbManager.colLastViewLink, url!!)
            values.put(SourceDbManager.colLastViewId, pageID)
            values.put(SourceDbManager.colSourceImage, fByteArray)
            values.put(SourceDbManager.colLastViewTitle, title)
            values.put(SourceDbManager.colLastViewTime, System.currentTimeMillis())

            val dbManager = SourceDbManager(context)
            val newId = dbManager.add(values)

            val webHistory = HistoryDbManager(context)
            webHistory.add(values)

            return newId
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