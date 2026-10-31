# 片刻 0.1.13 · 调色盘下滑联动

调色盘下滑时，编辑主界面的遮罩随抽屉实际露出比例连续减弱。手指按住、反向拖回、松手回弹和最终收起使用同一位置进度；关闭按钮先完成收起动画，再移除抽屉。

## 修复依据

实际依赖为 Compose Material3 1.4.0。其默认 Scrim 使用 `targetValue != Hidden` 独立淡入淡出，因此无法表达手指停在中途时的进度。保留原组件的手势、锚点、嵌套滚动、外部点击与返回键语义，将其 scrim 设为透明；通过 `SheetState.requireOffset()` 与布局测量计算实际露出比例，在主界面 Canvas 绘制原有 32% 遮罩。进度直接用于绘制，不追加另一条补间动画。

悬浮主题还会将 Activity 的原生 dim-behind 配置继承给调色盘 Dialog，产生一层直到窗口销毁才消失的固定遮罩。通过公开 `DialogWindowProvider` 仅移除这个 Dialog 的额外 dim-behind；主窗口的透明度、模糊半径与兼容策略保持原参数。

公开接口依据：[SheetState](https://developer.android.com/reference/kotlin/androidx/compose/material3/SheetState)、[窗口 dim-behind](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#FLAG_DIM_BEHIND)。

## 验证方法

`SheetDismissTest` 向原生窗口注入 DOWN / MOVE / UP，在手指仍按住时采样屏幕亮度，而非仅比较松手前后。依次下滑 30% / 58%、反向拖回 18% / 0%，再下滑 22% / 52% / 86% 并松手。断言亮度随下滑严格增加、反向拖回立即减少，且大部分恢复发生在松手之前。另验证关闭按钮、空白处点击和返回键关闭，以及草稿、撤销与重做。

测试必须使用抽屉内部拖动柄的实际坐标，并在每次原生 MOVE 后同步 Compose 测试帧；公开 sheet modifier 位于内部 offset 外侧，不能将其 top 直接当作抽屉屏幕坐标。未降低亮度变化断言。

原始截图、亮度 JSON、构建 / lint / 设备日志、正式清单和签名结果保存在 `artifacts/validation-0.1.13/`。

## 检查结果

- 9 项单元测试通过；debug / androidTest / release 构建成功。lint 0 errors、7 条既有 warning。
- 主回归 17 项通过，132.724 秒：包含调色盘手势与三条关闭路径、两种玻璃材质、剪贴板会话、完成复制退出、撤销 / 重做、磁贴状态、窗口重建、键盘与光标、悬浮及模糊禁用兼容。
- 额外液态玻璃悬浮调色盘 2 项通过，18.712 秒；毛玻璃悬浮调色盘 2 项通过，18.642 秒。两种悬浮主题键盘各 3 项、毛玻璃窗口专项 2 项通过。本轮最终通过的设备检查合计 29 项。
- Material 的空白背景采样亮度为：展开 173 → 按住下滑 30% 时 197 → 58% 时 220；反向拖回 18% 时 187，回到展开为 173。第二次下滑 86% 时为 243，完全关闭为 255。
- 液态玻璃悬浮相同过程为：102.46 → 116.62 → 130.31；反向拖回为 110.75，恢复展开为 102.46。第二次下滑 86% 时为 143.85，完全关闭为 151.03。松手前已经完成大部分亮度恢复。采样是指定场景中的屏幕亮度，并非性能帧率或物理透光率。

## 交付

- 版本 0.1.13 / versionCode 14，APK：`artifacts/moment-0.1.13.apk`，2,306,475 字节。
- SHA-256：`5B532EF33CC4666D0485CA8BCAA344C798D4C3F1F6D0D7BF3B54F132DC613ABA`。
- 复用既有签名身份，签名与 16KB 对齐验证通过。正式清单不包含调试测试入口。
- 正式包从 0.1.12 覆盖升级，液态玻璃、悬浮、浅色及配色设置保持一致，前后原生 UI 状态见 `upgrade-verification.json`。
- 正式包原生慢速下滑后抽屉消失、63 字符两行草稿保留，录屏见 `palette-drag-0.1.13.mp4`。完成后返回 SettingsHomepageActivity，通过快捷开关再次启动成功；系统模糊保持开启。

## 范围

Android 17 / API 37、Pixel 9 模拟器验证。未在厂商真机和 Android 10–16 实测。没有新增依赖、权限或剪贴板后台行为。
