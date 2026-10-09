package com.armin.arcade

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        web = findViewById(R.id.web)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true        // رکوردها در localStorage ذخیره میشه
            cacheMode = WebSettings.LOAD_NO_CACHE
            allowFileAccess = true
            builtInZoomControls = false
            textZoom = 100
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        web.webViewClient = WebViewClient()
        web.webChromeClient = object : WebChromeClient() {
            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                AlertDialog.Builder(this@GameActivity)
                    .setMessage(message)
                    .setPositiveButton("تأیید") { _, _ -> result?.confirm() }
                    .setNegativeButton("لغو") { _, _ -> result?.cancel() }
                    .setOnCancelListener { result?.cancel() }
                    .show()
                return true
            }
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                AlertDialog.Builder(this@GameActivity)
                    .setMessage(message)
                    .setPositiveButton("باشه") { _, _ -> result?.confirm() }
                    .show()
                return true
            }
        }

        val file = intent.getStringExtra("file") ?: "snake.html"
        web.loadUrl("file:///android_asset/games/$file")
    }

    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
