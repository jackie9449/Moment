# 液态玻璃主题设计依据

研究来源：Apple 官方 [Meet Liquid Glass](https://developer.apple.com/videos/play/wwdc2025/219/)、[Materials](https://developer.apple.com/design/human-interface-guidelines/materials) 与 [Applying Liquid Glass to custom views](https://developer.apple.com/documentation/SwiftUI/Applying-Liquid-Glass-to-custom-views)。

采用的原则：控制层通过透镜感、环境反光与触摸响应体现材质；正文作为独立阅读层；同组控件共用一层玻璃，避免多层叠加。片刻保留 Material 风格，并让液态玻璃支持明暗与配色。

## 0.1.9 材质重制

之前的简化边缘偏移、较厚白色填充和宽亮边容易显得像塑料。新实现将场景、光学程序、表面容器和按钮交互分开：

- 根据圆角矩形距离场构造四分之一椭圆截面的倒角，由高度导数求三维法线。平面中央法线稳定，不产生位置偏移。
- 以折射率 1.46 计算 Snell 透射方向，再按玻璃厚度求背景采样偏移；Schlick Fresnel 描述掠射角的反射变化。两束方向不同的环境光形成局部、细窄的高光。
- 使用线性 sRGB 计算吸收、反射及着色，再转回显示色彩；减轻统一乳白色覆盖和外围阴影。背景降低色场饱和度与视觉噪声。
- 完成按钮与底部工具栏使用同一个材质模型。工具栏内部每个按钮有独立交互源、局部柔光与弹性缩放，48dp 点击范围不变；Material 继续使用原生点击反馈。
- 普通模式只采样应用自己绘制的色场，不捕获正文。debug 专用网格诊断场景复用生产着色器，验证边缘的真实采样偏移与中央网格对齐。

Android 技术依据：[AGSL 与 GLSL 的差异](https://developer.android.com/develop/ui/views/graphics/agsl/agsl-vs-glsl)、[Using AGSL](https://developer.android.com/develop/ui/views/graphics/agsl/using-agsl)、[Compose brushes](https://developer.android.com/develop/ui/compose/graphics/draw/brush)。API 33+ 使用 AGSL；API 29–32 使用半透明填充、方向性细边与阴影的兼容表现。没有持续运行的动画循环，按压、主题切换与系统输入法动画才触发动态变化。

## 悬浮模式与边界

Android 11+ 通过公开的 [Activity.setTranslucent](https://developer.android.com/reference/android/app/Activity#setTranslucent(boolean)) 保持之前窗口可见；Android 12+ 监听[系统跨窗口模糊](https://source.android.com/docs/core/display/window-blurs)能力。0.1.9 将背景模糊半径从 12dp 减为 6dp，并减轻整体遮罩，增强真实背景的可辨识度。系统不支持模糊时提高阅读面不透明度。

悬浮材质也使用曲面法线与反射模型，输出预乘透明度，由 Android 将后面的窗口合成进来。**它无法取得其他应用的像素，因此不会对其他应用画面进行逐像素折射**；跨应用通透感由真实透明合成、系统柔化和表面反光共同提供。普通模式则能折射自身的程序化背景。

正文保持高不透明度的阅读面，前景文字不参与模糊或扭曲。未添加截图、录屏、辅助功能或系统悬浮窗权限；背景也不是冻结截屏。这是 Android 上依据上述原则制作的独立实现，不使用 Apple 的渲染器，不保证与 Apple 系统材质逐像素相同。

## 0.1.12 通透度调整

普通阅读卡片的填充不透明度由 94% 减至 78%。支持系统模糊的悬浮模式中，浅色阅读卡片由 78% 减至 64%，深色由 82% 减至 70%，使背景透出比例分别达到 36% 与 30%。系统模糊不可用时保留较厚的兼容阅读层。

悬浮工具栏基准不透明度由浅 / 深 58% / 70% 减至 46% / 60%；图标区域用很薄的局部中性色层维持辨识度，中心外观按钮使用较明确的主题底色。普通光学材质的均匀雾化混合由 11% / 16% 减至 4% / 7%，保留曲面反射与折射。悬浮元信息提高前景强度，使状态与字数在更通透的卡片中仍清楚。
