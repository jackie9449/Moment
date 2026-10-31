# 毛玻璃主题设计依据

参考 Apple 官方 [Materials](https://developer.apple.com/design/human-interface-guidelines/materials)、[WWDC19 · What's New in iOS Design](https://developer.apple.com/videos/play/wwdc2019/808/)、[UIBlurEffect](https://developer.apple.com/documentation/uikit/uiblureffect) 与 [UIVibrancyEffect](https://developer.apple.com/documentation/uikit/uivibrancyeffect)。研究日期：2026-10-31。

Apple 将标准材质按使用情境与厚度分层，并用背景模糊、半透明混合及前景色彩表达层级。较厚材质更适合文字与细节；较薄材质保留更多背景上下文。模糊应作用于背后的内容，前景标签仍应清晰。Vibrancy 则帮助前景在变化的背景上保持辨识度。

## 片刻的 Android 实现

新增独立的“毛玻璃 / 柔雾 · 沉静”，与 Material、液态玻璃并列；不是对液态玻璃改变一个颜色参数。毛玻璃采用平整雾面、低饱和漫射色彩、细分隔边和轻阴影，取消透镜倒角折射及强烈镜面高光。

普通模式使用 Compose GraphicsLayer 记录应用自己绘制的装饰背景。每个材质表面在带 54dp 边距的局部背景上使用原生 18dp BlurEffect，再叠加中性半透明填充与少量主题色；正文使用更厚的填充。前景文字和按钮不进入模糊图层。图层随 Compose 生命周期释放；静止时没有动画循环，也不创建截图位图。

Android 11+ 的可选悬浮模式沿用真实透明 Activity，透出启动前的应用；Android 12+ 在支持时使用系统跨窗口模糊。毛玻璃采用 20dp 的模糊半径，液态玻璃保留 6dp，两者切换时直接更新窗口材质参数，文字和撤销历史不丢失。悬浮开关在两种玻璃主题间共用；Material 始终使用普通窗口。

系统模糊被禁用或设备不支持时，监听回调提高材质不透明度，保持文字可读。Android 10–11 的普通毛玻璃使用高不透明度填充兼容表现，不声称具有原生背景模糊。悬浮仍不需要录屏、截图或系统悬浮窗权限。

前景使用现有主题的语义色、清晰图标和主次文字层级表达可读性；这不是 UIKit Vibrancy 的逐像素复刻，也不使用 Apple 的私有算法。具体半径与颜色为本应用独立调校值，Apple 文档没有提供可直接复制到 Android 的完整材质参数。

Android 依据：[Compose GraphicsLayer](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/layer/GraphicsLayer)、[Graphics modifiers](https://developer.android.com/develop/ui/compose/graphics/draw/modifiers)、[系统跨窗口模糊](https://source.android.com/docs/core/display/window-blurs)。
