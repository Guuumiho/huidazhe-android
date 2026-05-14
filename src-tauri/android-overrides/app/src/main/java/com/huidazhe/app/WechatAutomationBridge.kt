package com.huidazhe.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.webkit.JavascriptInterface
import org.json.JSONObject

class WechatAutomationBridge(private val activity: MainActivity) {
  @JavascriptInterface
  fun startAutomation(rawJson: String): String {
    return try {
      val payload = JSONObject(rawJson)
      val recipient = payload.optString("recipientAlias").trim()
      val message = payload.optString("message").trim()

      if (recipient.isEmpty() || message.isEmpty()) {
        return result(false, "联系人或消息为空，未启动微信自动化。")
      }

      if (!WechatAutomationService.isRunning()) {
        activity.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        return result(false, "请先开启“回答者”无障碍服务，然后回到应用重新确认。")
      }

      if (!Settings.canDrawOverlays(activity)) {
        val intent = Intent(
          Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:${activity.packageName}")
        )
        activity.startActivity(intent)
        return result(false, "请先允许悬浮窗权限，然后回到应用重新确认。")
      }

      WechatAutomationService.startWechatSend(activity, recipient, message)
      result(true, "微信自动化已启动，执行中可点悬浮 X 取消。")
    } catch (error: Exception) {
      result(false, "微信自动化启动失败：${error.message}")
    }
  }

  @JavascriptInterface
  fun stopAutomation(): String {
    WechatAutomationService.cancelCurrentRun()
    return result(true, "已请求停止微信自动化。")
  }

  private fun result(ok: Boolean, message: String): String {
    return JSONObject()
      .put("ok", ok)
      .put("message", message)
      .toString()
  }
}
