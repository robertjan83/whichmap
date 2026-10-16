package app.whichmap

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/** Toont dezelfde pagina als de website, maar offline uit de assets van de app. */
class MainActivity : Activity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#0A0B09")
        window.navigationBarColor = Color.parseColor("#0A0B09")

        web = WebView(this)
        web.setBackgroundColor(Color.parseColor("#0A0B09"))
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.cacheMode = WebSettings.LOAD_DEFAULT
        web.webViewClient = WebViewClient()
        setContentView(web)

        val mode = intent.getStringExtra("mode")
        val query = if (mode != null && Rotation.ORDER.containsKey(mode)) "?mode=$mode" else ""
        web.loadUrl("file:///android_asset/index.html$query")
        MapWidgetProvider.updateAll(this)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val mode = intent.getStringExtra("mode")
        if (mode != null && Rotation.ORDER.containsKey(mode)) web.loadUrl("file:///android_asset/index.html?mode=$mode")
    }

    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
    }

    override fun onPause() {
        web.onPause()
        super.onPause()
    }
}
