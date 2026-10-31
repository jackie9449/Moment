# 片刻 0.1.14 · 点叉关闭节奏

点叉关闭个性化面板使用 420ms 的缓入缓出下滑，曲线为 `(0.32, 0, 0.18, 1)`。轻缓起步、柔和收尾，在完整滑出屏幕后才移除。主界面的遮罩继续与实际位移同步减弱。

## 实现与范围

本地实际依赖 Material3 1.4.0 的 `hide()` 固定使用内部 FastEffects 规格，尚未提供公开的 motionScheme / hide 动画配置。点叉操作使用一个 `Animatable` 平移剩余收起距离，并将现有遮罩露出比例乘以相同的剩余进度；结束后直接移除面板，不叠加第二次原生 hide。只在点叉动画期间合并重复关闭，禁用竞争的拖动、空白点击与返回关闭。

手动下滑、反向拖回、松手回弹、普通空白点击和返回键保留原生行为；浮窗额外 dim 的上一版修复保留。平移参数在 graphics layer 阶段读取，普通拖动时不额外跟踪 offset。无新增依赖、权限或后台行为，最低 API 29 / target API 37。

Compose `Animatable` 负责动画与系统时长缩放，接口依据见 [Android 官方文档](https://developer.android.com/reference/kotlin/androidx/compose/animation/core/Animatable)。

## 验证方法

新增受控帧时钟检查：充分展开后暂停自动推进，点叉后推进 96ms、再推进 160ms，要求拖动柄连续下移且面板仍存在；再推进 240ms 后要求面板消失、草稿原样保留。保留上一版真实窗口按住拖动 / 反向 / 最终收起及关闭按钮、空白处、返回键测试。

原生截图前等待编辑窗口的绘制提交至系统合成器，避免将上一帧遮罩误当作当前帧。亮度变化断言保持原标准。

原始证据保存在 `artifacts/validation-0.1.14/`。本轮使用 Android 17 / API 37 Pixel 9 模拟器，未在厂商真机和 Android 10–16 实测。

## 构建与交付

- 9 项单元测试通过；debug / androidTest / release 构建成功，lint 0 errors、7 条既有 warning。
- Material、液态玻璃悬浮、毛玻璃悬浮的面板专项各 3 项通过，分别用时 26.735 / 23.686 / 23.835 秒：覆盖新点叉动画持续时间、三条关闭路径、原生按住拖动与反向恢复。
- EditorFlowTest 8 项通过，73.111 秒，覆盖剪贴板主动启动、编辑和精确复制退出、文本与撤销、主题 / 窗口重建、短窗口 / 大字及快捷开关状态；本轮设备检查合计 17 项。
- APK：`artifacts/moment-0.1.14.apk`，0.1.14 / versionCode 15。复用既有签名身份，签名及 16KB 对齐检查通过，正式清单不含测试入口。
- SHA-256：`4E04ABD11F417768B12BE4EBE52DE42537CA908CEEF6AC55DA9F0D6DD0C2FB6B`。
- 正式包由 0.1.13 覆盖升级成功，液态玻璃、悬浮、浅色和配色设置均保留；原生状态对照见 `upgrade-verification.json`。
- 系统动画缩放 1，正式包原生点叉关闭录屏见 `palette-close-0.1.14.mp4`。关闭后 61 字符两行草稿完整保留；完成退出返回 SettingsHomepageActivity。系统模糊保持启用，正式验证记录见 `release-verification.json`。
