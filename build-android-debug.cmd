@echo off
setlocal

set "ROOT=%~dp0"
set "CARGO=%USERPROFILE%\.cargo\bin\cargo.exe"
set "TAURI=%USERPROFILE%\.cargo\bin\cargo-tauri.exe"
set "ANDROID_DIR=%ROOT%src-tauri\gen\android"
set "DEFAULT_JDK=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
set "DEFAULT_ANDROID_HOME=C:\MyDownload\AndroidSDK"
set "OUTPUT=%ANDROID_DIR%\app\build\outputs\apk\universal\debug\app-universal-debug.apk"
set "ROOT_OUTPUT=%ROOT%huidazhe-android-debug.apk"

cd /d "%ROOT%"

if not exist "%CARGO%" (
  echo [ERROR] cargo.exe not found.
  echo Please install Rust first.
  exit /b 1
)

if not exist "%TAURI%" (
  echo [ERROR] Tauri CLI is not installed.
  echo Install it with:
  echo   cargo install tauri-cli --version "^2"
  exit /b 1
)

if "%JAVA_HOME%"=="" if exist "%DEFAULT_JDK%\bin\java.exe" (
  set "JAVA_HOME=%DEFAULT_JDK%"
  set "PATH=%JAVA_HOME%\bin;%PATH%"
)

if "%ANDROID_HOME%"=="" if "%ANDROID_SDK_ROOT%"=="" (
  if exist "%DEFAULT_ANDROID_HOME%\platform-tools\adb.exe" (
    set "ANDROID_HOME=%DEFAULT_ANDROID_HOME%"
    set "ANDROID_SDK_ROOT=%DEFAULT_ANDROID_HOME%"
  )
)

if not "%ANDROID_HOME%"=="" (
  set "PATH=%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%"
)

if not exist "%ANDROID_DIR%" (
  echo [ERROR] Android project has not been initialized.
  echo Run init-android.cmd first.
  exit /b 1
)

powershell -ExecutionPolicy Bypass -File "%ROOT%scripts\sync-android-overrides.ps1"
if errorlevel 1 (
  echo [ERROR] Failed to sync Android overrides.
  exit /b 1
)

echo Building Android debug APK for arm64 phones...
"%CARGO%" tauri android build --debug --target aarch64 --apk --ci
if errorlevel 1 (
  echo [ERROR] Android debug build failed.
  exit /b 1
)

if exist "%OUTPUT%" (
  copy /Y "%OUTPUT%" "%ROOT_OUTPUT%" >nul
  echo.
  echo Debug APK:
  echo   %ROOT_OUTPUT%
) else (
  echo [WARN] Build finished but expected APK was not found:
  echo   %OUTPUT%
)

endlocal
