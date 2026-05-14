# 回答者 Android 源码版说明

本目录是从桌面版项目复制出来的手机适配源码。

## 当前范围

- 中间问答区作为手机主屏幕。
- 左上角按钮打开对话窗口列表。
- 右上角按钮打开思维栏占位。
- 手机端思维栏暂不运行旧思维地图功能，只保留后续迭代入口说明。
- Tauri 应用名已设置为 `huidazhe`。
- Android 包标识已设置为 `com.huidazhe.app`。

## 隐私边界

生成源码时已排除：

- `.git`
- `settings.json`
- `qa_records.db`
- `model_calls.jsonl`
- `note.json`
- `*.db / *.db-shm / *.db-wal`
- `*.exe`
- `src-tauri/target`
- `src-tauri/gen`

## 后续打包

当前是“先源码后打包”阶段，尚未生成 APK。

本目录提供 3 个脚本：

- `init-android.cmd`：初始化 Tauri Android 工程。
- `run-android-dev.cmd`：连接手机后运行调试版。
- `build-android.cmd`：生成 Android APK。
- `build-android-debug.cmd`：生成更适合手机测试安装的 debug APK。

首次打包前需要准备：

- Rust。
- Tauri CLI：`cargo install tauri-cli --version "^2"`。
- JDK 17。
- Android Studio / Android SDK。
- 手机开启 USB 调试。

推荐顺序：

1. 运行 `init-android.cmd`。
2. 连接 iQOO Neo8 Pro，确认 USB 调试授权。
3. 运行 `run-android-dev.cmd` 做真机预览。
4. 确认没问题后运行 `build-android.cmd`。

`build-android.cmd` 会在打包前检查源码根目录是否误放了 `settings.json`、`qa_records.db`、`model_calls.jsonl`、`note.json`。

## 当前已生成 APK

- 测试安装包：`huidazhe-android-debug.apk`
- 未签名 release 包：`huidazhe-android-release-unsigned.apk`

优先把 `huidazhe-android-debug.apk` 发到手机上测试。

## 本机打包注意

- Windows 当前没有开启开发者模式，Tauri 默认 symlink 会失败。
- 本机已临时安装 patched Tauri CLI：当 symlink 权限不足时 fallback 为复制文件。
- 如果以后换电脑打包，推荐优先开启 Windows 开发者模式，或继续使用同样的 patched CLI 方案。
- Gradle Kotlin daemon 可能输出 “different roots” 警告，但本次已 fallback 成功，不影响 APK 生成。
