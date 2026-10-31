# 片刻 0.1.0 验证记录

日期：2026-10-30（Asia/Shanghai）。

## 交付

- 安装包：`artifacts/moment-0.1.0.apk`，2,285,995 字节，独立本地密钥签名的 R8 压缩 release。
- 应用 ID：`com.jackie.moment`；minSdk 29，compileSdk 37.0，targetSdk 37。
- SHA-256：`36AA7B21300DD405BAD493A07D8DA4BAC698DFBA502217B3D86FA87772F6EF74`。
- 已验证 APK v3 签名与 16KB 原生库对齐。正式包不含 `ClipboardProbeActivity`，无网络、存储或悬浮窗权限。

## 构建与测试

- `testDebugUnitTest`：7 项通过，0 失败。覆盖精确文本、中文 IME 组合输入、emoji、空白换行、光标移动、撤销重做、历史边界及新会话重置。
- `lintDebug`：0 errors，7 条信息性警告（依赖更新提示及资源目录版本限定提示）。
- `assembleDebug`、`assembleRelease`、`assembleDebugAndroidTest`：全部通过。
- Pixel 9 虚拟设备，Android 17 / API 37，Google APIs 37.0 system image rev 8，Emulator 37.2.13，WHPX 硬件加速。
- 通过 ADB 直接运行 AndroidJUnitRunner：5 项通过，0 失败，耗时 48.437 秒。

设备测试包括：

1. 真实系统剪贴板自动载入 → 修改中文、emoji、换行和空白 → 完成复制 → 确认任务退出及剪贴板逐字一致。
2. 撤销、重做，以及将空文本复制并退出。
3. 通过真实 SystemUI 快捷设置磁贴启动并载入最新剪贴板。
4. 其他前台窗口更改剪贴板后返回、切换深色和配色、Activity 重建，均保留编辑内容及主题选择。
5. 1600×1100 窗口与 1.5 倍系统字体下，编辑区仍可见且高度不小于 64dp，并能够编辑、复制及退出。

`connectedDebugAndroidTest` 的控制器额外下载等待被终止，实际设备执行使用以下等效入口；原始结果保存在 `artifacts/validation/android17-instrumentation.txt`：

```powershell
adb -s emulator-5554 shell am instrument -w -r com.jackie.moment.test/androidx.test.runner.AndroidJUnitRunner
```

## 视觉检查

签名 release 已重新安装并启动，检查浅色、深色、主题抽屉、键盘展开和宽屏大字体。低高度时收起装饰与次要控件，优先保证编辑区和“完成”按钮。截图均为模拟器实拍。

- `artifacts/editor-light.png`
- `artifacts/editor-dark.png`
- `artifacts/appearance.png`
- `artifacts/editor-keyboard.png`
- `artifacts/editor-wide-large-font.png`

本轮运行验证使用 Android 17 模拟器，尚未在实际厂商手机或 Android 10–16 设备上运行。不同厂商快捷设置面板的具体排布由系统决定。
