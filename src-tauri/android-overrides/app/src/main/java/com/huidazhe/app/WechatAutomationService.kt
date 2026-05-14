package com.huidazhe.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import org.json.JSONObject

class WechatAutomationService : AccessibilityService() {
  private val handler = Handler(Looper.getMainLooper())
  private var cancelled = false
  private var overlayView: View? = null
  private var windowManager: WindowManager? = null

  override fun onServiceConnected() {
    instance = this
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

  override fun onInterrupt() {
    cancel()
  }

  override fun onDestroy() {
    if (instance === this) {
      instance = null
    }
    cancel()
    super.onDestroy()
  }

  private fun runWechatSend(context: Context, recipient: String, message: String) {
    val script = loadScript(context)
    val expectedWidth = script.optInt("screenWidth", 0)
    val expectedHeight = script.optInt("screenHeight", 0)
    val metrics = resources.displayMetrics
    if (expectedWidth > 0 && expectedHeight > 0) {
      val matches = metrics.widthPixels == expectedWidth && metrics.heightPixels == expectedHeight
      if (!matches) {
        return
      }
    }

    cancelled = false
    showCancelOverlay()

    val steps = listOf<() -> Unit>(
      { performGlobalAction(GLOBAL_ACTION_HOME) },
      { tap(script, "wechatIcon") },
      { tap(script, "searchBox") },
      { inputText(recipient) },
      { tap(script, "contactResult") },
      { tap(script, "messageInput") },
      { inputText(message) },
      { tap(script, "sendButton") },
      { removeCancelOverlay() }
    )

    runStep(steps, 0)
  }

  private fun runStep(steps: List<() -> Unit>, index: Int) {
    if (cancelled || index >= steps.size) {
      removeCancelOverlay()
      return
    }
    steps[index].invoke()
    handler.postDelayed({ runStep(steps, index + 1) }, STEP_DELAY_MS)
  }

  private fun tap(script: JSONObject, key: String) {
    val point = script.optJSONObject(key) ?: return
    tap(point.optInt("x"), point.optInt("y"))
  }

  private fun tap(x: Int, y: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      return
    }
    val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
    val gesture = GestureDescription.Builder()
      .addStroke(GestureDescription.StrokeDescription(path, 0, 80))
      .build()
    dispatchGesture(gesture, null, null)
  }

  private fun inputText(text: String) {
    val focused = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    val arguments = Bundle().apply {
      putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
    }
    if (focused?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments) == true) {
      return
    }

    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("huidazhe_message", text))
    focused?.performAction(AccessibilityNodeInfo.ACTION_PASTE)
  }

  private fun showCancelOverlay() {
    if (overlayView != null) {
      return
    }
    windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val button = TextView(this).apply {
      text = "×"
      textSize = 22f
      setTextColor(Color.WHITE)
      setBackgroundColor(Color.argb(210, 150, 82, 58))
      gravity = Gravity.CENTER
      setOnClickListener { cancel() }
    }
    val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
      @Suppress("DEPRECATION")
      WindowManager.LayoutParams.TYPE_PHONE
    }
    val params = WindowManager.LayoutParams(
      88,
      88,
      type,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
      PixelFormat.TRANSLUCENT
    ).apply {
      gravity = Gravity.TOP or Gravity.END
      x = 24
      y = 160
    }
    overlayView = button
    windowManager?.addView(button, params)
  }

  private fun removeCancelOverlay() {
    overlayView?.let { view ->
      try {
        windowManager?.removeView(view)
      } catch (_: Exception) {
      }
    }
    overlayView = null
  }

  private fun cancel() {
    cancelled = true
    removeCancelOverlay()
  }

  private fun loadScript(context: Context): JSONObject {
    val raw = context.assets.open("wechat_send_message.json")
      .bufferedReader()
      .use { it.readText() }
    return JSONObject(raw)
  }

  companion object {
    private const val STEP_DELAY_MS = 500L
    private var instance: WechatAutomationService? = null

    fun isRunning(): Boolean = instance != null

    fun startWechatSend(context: Context, recipient: String, message: String) {
      instance?.runWechatSend(context, recipient, message)
    }

    fun cancelCurrentRun() {
      instance?.cancel()
    }
  }
}
