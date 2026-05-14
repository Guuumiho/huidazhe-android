@echo off
setlocal

set "ROOT=%~dp0"
set "CARGO=%USERPROFILE%\.cargo\bin\cargo.exe"
set "TAURI=%USERPROFILE%\.cargo\bin\cargo-tauri.exe"
set "ANDROID_DIR=%ROOT%src-tauri\gen\android"
set "DEFAULT_JDK=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
set "DEFAULT_ANDROID_HOME=C:\MyDownload\AndroidSDK"

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

if "%JAVA_HOME%"=="" (
  echo [WARN] JAVA_HOME is not set. Android builds usually need JDK 17.
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

if "%ANDROID_HOME%"=="" if "%ANDROID_SDK_ROOT%"=="" (
  echo [WARN] ANDROID_HOME / ANDROID_SDK_ROOT is not set.
)

if exist "%ANDROID_DIR%" (
  echo Android project already exists:
  echo   %ANDROID_DIR%
  echo Nothing to initialize.
  exit /b 0
)

echo Initializing Tauri Android project...
"%CARGO%" tauri android init --ci
if errorlevel 1 (
  echo [ERROR] Android initialization failed.
  exit /b 1
)

echo.
echo Android project initialized:
echo   %ANDROID_DIR%

endlocal
