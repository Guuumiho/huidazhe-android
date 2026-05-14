package com.huidazhe.app

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import org.json.JSONObject

class LogExportBridge(private val activity: MainActivity) {
  private val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/huidazhe-logs/"

  @JavascriptInterface
  fun exportToDownloads(rawJson: String): String {
    return try {
      val payload = JSONObject(rawJson)
      val files = payload.optJSONArray("files") ?: return result(false, "No files to export.")
      var exportedCount = 0

      for (index in 0 until files.length()) {
        val file = files.optJSONObject(index) ?: continue
        val fileName = sanitizeFileName(file.optString("fileName"))
        val content = file.optString("content")
        if (fileName.isEmpty()) {
          continue
        }

        writeTextFile(fileName, content)
        exportedCount += 1
      }

      result(true, "模型日志已导出到：下载/huidazhe-logs（${exportedCount} 个文件）")
    } catch (error: Exception) {
      result(false, "模型日志导出失败：${error.message}")
    }
  }

  private fun writeTextFile(fileName: String, content: String) {
    val resolver = activity.contentResolver
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MediaStore.Downloads.EXTERNAL_CONTENT_URI
    } else {
      MediaStore.Files.getContentUri("external")
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      resolver.delete(
        collection,
        "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
        arrayOf(fileName, relativePath)
      )
    }

    val values = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
      put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
      }
    }

    val uri = resolver.insert(collection, values)
      ?: throw IllegalStateException("Cannot create $fileName in Downloads.")

    resolver.openOutputStream(uri)?.use { output ->
      output.write(content.toByteArray(Charsets.UTF_8))
    } ?: throw IllegalStateException("Cannot open $fileName for writing.")
  }

  private fun sanitizeFileName(fileName: String): String {
    return fileName
      .trim()
      .replace("/", "_")
      .replace("\\", "_")
      .take(80)
  }

  private fun result(ok: Boolean, message: String): String {
    return JSONObject()
      .put("ok", ok)
      .put("message", message)
      .toString()
  }
}
