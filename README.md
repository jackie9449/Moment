# 片刻 · Moment

一款为中短文本设计的原生 Android 剪贴板编辑器。复制文字 → 打开片刻 → 编辑 → 右上角「完成」复制并退出。

## 下载

已发布的签名 APK 见 [GitHub Releases](https://github.com/jackie9449/Moment/releases/latest)，本地最新构建位于 `artifacts/`。当前工作版本为 **0.1.16**。后续使用同一签名的版本可直接覆盖安装。

## 功能

- 每次从桌面或快捷设置主动启动，自动载入当前剪贴板文本。
- 真正的系统 Quick Settings 磁贴「编辑剪贴板」，支持应用内请求添加。
- 已添加快捷开关后隐藏添加卡片，系统移除磁贴后自动恢复；底部按钮的点击反馈互相独立。
- Material 3 界面，浅色 / 深色 / 跟随系统，三套配色及 Android 12+ 壁纸配色。
- “液态玻璃”界面风格：悬浮胶囊、边缘折射、触摸高光与弹性反馈；正文保持清晰。原有 Material 风格可随时切回，两种风格均支持明暗与配色。
- 液态玻璃和毛玻璃均可开启“悬浮模式”（Android 11+）：透出启动前的真实界面，设备支持时柔化背景。无需截图、录屏或悬浮窗权限，默认关闭。
- 新增“毛玻璃”主题：独立的原生背景模糊、柔雾中性色层和较厚的正文材质，支持明暗、配色与悬浮模式。Android 12+ 提供背景模糊，较旧系统保留高不透明度兼容表现。
- 页面、主题、抽屉与系统退出动画；撤销 / 重做；14–26sp 字号。
- 调色盘下滑时，主界面遮罩随手指位置逐渐淡出；反向拖回、松手回弹与关闭按钮保持同一进度。
- 输入法展开、编辑区域高度变化时自动保持当前光标可见；支持长文本中间位置，手动滚动浏览不会被强行拉回。
- 移除底部完成提示，输入法动画期间保持常规布局稳定，避免额外高度跳变。
- 三种主题共用编辑卡片与独立工具栏：统一页首、圆角、边距和强调按钮；个性化页面使用主题预览、分段选项与紧凑设置行。
- 原样保留中文、emoji、空白和换行。完成写入成功后再退出。
- 适配 Android 17 / API 37，最低支持 Android 10。键盘、安全区域、横屏和大屏布局自适应。
- 正式版不申请网络或存储权限，不保存剪贴板历史。编辑文本仅在当前会话及系统恢复状态中保留。

## 使用

1. 复制要修改的文字，打开「片刻」。
2. 编辑后点右上角「完成」，返回原应用粘贴。
3. 点底部中央调色盘，在「界面风格」中切换 Material / 液态玻璃 / 毛玻璃；两种玻璃主题下可开启「悬浮模式」。还可调整明暗、配色或点击「添加快捷开关」。Android 的添加确认需要你亲自同意。
4. 添加后，从系统下拉快捷设置点「编辑剪贴板」即可启动。

主编辑页返回与「完成」一致：原样复制并退出；个性化面板内返回只关闭面板。修改文字后主动上滑回桌面或切换应用，会自动复制并结束会话；未修改的会话、锁屏、来电及临时失焦不触发自动完成。输入法展开时，系统可能先收起输入法。切换外观保留当前编辑，从桌面或磁贴主动启动则载入新剪贴板会话。非文本或空剪贴板显示空白页。

不同厂商的快捷设置布局不同；Android 10–12 需在下拉面板的编辑页面手动添加磁贴。

## 开发

使用 Android Studio 打开项目根目录。依赖：Android SDK Platform 37.0、Build Tools 36.0.0、JDK 17+（当前环境使用 Android Studio 内置 JBR）。首次构建需要下载 Gradle 和 Maven 依赖。

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
.\gradlew.bat :app:connectedDebugAndroidTest
```

`assembleDebug` 产物可直接安装。`assembleRelease` 产物经过 R8 压缩。使用以下命令生成不含调试辅助入口的签名 APK：

```powershell
.\tools\package.ps1
```

产物为 `artifacts/moment-<版本号>.apk`，当前版本为 `0.1.16`。首次打包生成独立的本地签名身份，保存在 `%USERPROFILE%\.android\moment-signing`，后续打包复用同一个身份以便覆盖升级；密钥和凭据不进入项目或 APK。请将这个目录作为项目发布身份备份。

`app/src/debug/` 内的剪贴板测试 Activity 只用于设备仪器测试，不会进入 release APK。应用的真实剪贴板读取始终在获得前台窗口焦点后执行。

仓库包含源码、Gradle Wrapper、测试和设计 / 验收文档。`artifacts/` 下的本机截图、录屏与原始测试日志不提交；正式安装包在 Releases 提供。签名密钥、凭据、`local.properties` 和构建缓存仅保留在本机。

详细需求和验收记录见 `docs/spec.md` 与 `docs/superpowers/plans/2026-10-30-moment-editor.md`。

0.1.15 将个性化侧滑返回接入同一柔和收起动画：手势预览跟手，确认后缓缓收起，取消则平滑复位；主界面遮罩同步过渡。统一界面规范见 `docs/ui-design-system.md`，验收见 `docs/validation-0.1.15.md`。历史记录继续保留。

0.1.16 增加返回 / 主动离开时完成、360ms 柔和退出，以及英语、繁体中文（含香港）和日语。语言跟随系统，Android 13+ 也可在系统应用语言中单独选择。主屏幕上滑动画由系统桌面管理。详见 `docs/validation-0.1.16.md`。
