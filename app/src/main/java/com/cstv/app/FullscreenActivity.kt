package com.cstv.app

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.*
import android.content.Intent
import android.webkit.WebView
import android.widget.ProgressBar

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

    }

    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var mVisible: Boolean = false
    private var mWebView: WebView? = null
    private var progressBar: ProgressBar? = null

    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        mWebView = findViewById(R.id.activity_main_webview)
        var webView: WebView = findViewById(R.id.activity_main_webview)
        val settings: WebSettings = webView.settings
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        progressBar = findViewById(R.id.progressBar);

        val sb = StringBuilder()
        sb.append("/data/data")
        sb.append(packageName)
        sb.append("/cache")

        settings.setAppCachePath(sb.toString())
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY)
        settings.javaScriptEnabled = true
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
        webView.isHapticFeedbackEnabled = true
        webView.isHorizontalScrollBarEnabled = false
        webView.isVerticalScrollBarEnabled = false
        webView.isLongClickable = true

        settings.setSupportZoom(true)

        CookieManager.setAcceptFileSchemeCookies(true)
        val instance = CookieManager.getInstance()
        instance.setAcceptCookie(true)
        instance.acceptCookie()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if( URLUtil.isNetworkUrl(url) ) {
                    return false
                }

                if (url.startsWith("intent")) {
                    try {
                        val intent: Intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        val fallbackUrl: String? = intent.getStringExtra("browser_fallback_url")
                        if (fallbackUrl != null) {
                            webView.loadUrl(fallbackUrl)
                            return true
                        }
                    } catch (e: Exception) {
                    }
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)

                return true
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(webView: WebView, i: Int) {
                if (i < 100 && progressBar!!.getVisibility() == View.GONE) {
                    progressBar!!.setVisibility(View.VISIBLE)
                }
                progressBar!!.setProgress(i)
                if (i >= 100) {
                    progressBar!!.setVisibility(View.GONE)
                }
            }
        }

        webView.loadUrl("https://cstv.fr/app/home/main")
    }

    override fun onBackPressed() {
        if (mWebView!!.canGoBack()) {
            mWebView!!.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar

        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
