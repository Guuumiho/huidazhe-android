package com.huidazhe.app

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge

class MainActivity : TauriActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
  }

  override fun onWebViewCreate(webView: WebView) {
    super.onWebViewCreate(webView)
    webView.addJavascriptInterface(WechatAutomationBridge(this), "HuidazheWechat")
    webView.addJavascriptInterface(LogExportBridge(this), "HuidazheLogs")
  }
}
