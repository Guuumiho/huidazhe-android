$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$androidDir = Join-Path $root "src-tauri\gen\android"
$overrideDir = Join-Path $root "src-tauri\android-overrides\app\src\main"
$mainDir = Join-Path $androidDir "app\src\main"
$manifestPath = Join-Path $mainDir "AndroidManifest.xml"
$stringsPath = Join-Path $mainDir "res\values\strings.xml"

if (-not (Test-Path $androidDir)) {
  throw "Android project has not been initialized. Run init-android.cmd first."
}

Copy-Item -Path (Join-Path $overrideDir "java\*") -Destination (Join-Path $mainDir "java") -Recurse -Force
Copy-Item -Path (Join-Path $overrideDir "res\*") -Destination (Join-Path $mainDir "res") -Recurse -Force
Copy-Item -Path (Join-Path $overrideDir "assets\*") -Destination (Join-Path $mainDir "assets") -Recurse -Force

$manifest = Get-Content -LiteralPath $manifestPath -Raw
if ($manifest -notmatch "SYSTEM_ALERT_WINDOW") {
  $manifest = $manifest -replace '(<uses-permission android:name="android.permission.INTERNET" />)', "`$1`r`n    <uses-permission android:name=`"android.permission.SYSTEM_ALERT_WINDOW`" />"
}

if ($manifest -notmatch "WechatAutomationService") {
  $service = @"

        <service
            android:name=".WechatAutomationService"
            android:exported="false"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/wechat_accessibility_config" />
        </service>
"@
  $manifest = $manifest -replace '(\s*</application>)', "$service`r`n`$1"
}
Set-Content -LiteralPath $manifestPath -Value $manifest -Encoding UTF8

$strings = Get-Content -LiteralPath $stringsPath -Raw
$strings = $strings -replace '\s*<string name="wechat_accessibility_description">.*?(\</string\>|/string>)\s*', "`r`n"
if ($strings -notmatch "wechat_accessibility_description") {
  $accessibilityString = '    <string name="wechat_accessibility_description">Allow Huidazhe to operate WeChat only after user confirmation.</string>'
  $strings = $strings -replace '(\s*</resources>)', "$accessibilityString`r`n`$1"
  Set-Content -LiteralPath $stringsPath -Value $strings -Encoding UTF8
}

Write-Host "[OK] Android overrides synced."
