# 片刻 0.1.15 · 侧滑返回的柔和收起

系统侧滑返回时，个性化面板和主界面遮罩一起跟随手势预览。松手确认返回后，从当前预览位置用与点叉一致的 420ms 缓入缓出动画收起；取消手势时用 260ms 平滑复位。普通返回键也使用相同收起动画。

## 实现

Material3 1.4.0 原生预测性返回在提交时仍调用内部 FastEffects hide，因此上一版只调整点叉不足以覆盖侧滑返回。新增 `SheetBackHandler`，通过公开的 ViewTree OnBackPressedDispatcherOwner 在面板自己的 ComponentDialog 窗口注册 OnBackPressedCallback，关闭该面板自带的原生 back callback。回调随组合销毁移除，事件使用最新处理函数。

系统手势进度直接更新现有 Animatable，预览最多平移剩余距离的 24%，主界面遮罩使用同一进度。确认返回取消预览任务并从当前值收起；取消则柔和回到零。若复位未结束就开始新手势，保留当前位置作为起点。重复返回不会绕过收起动画；主 Activity 不接管面板的返回。

保留点叉、手动垂直拖动 / 反向 / 松手回弹、空白点击、草稿与撤销、精确复制退出，以及浮窗模糊 / dim 处理。Android 14+ 可提供连续系统手势进度，旧系统的返回键沿同一柔和动画关闭。系统动画时长设置继续生效。没有新增依赖、权限或后台行为。

接口依据：[Android 预测性返回](https://developer.android.com/develop/ui/compose/system/predictive-back)、[OnBackPressedCallback](https://developer.android.com/reference/androidx/activity/OnBackPressedCallback)。

## 验证方法

新增受控帧时钟测试，在实际 ComponentDialog dispatcher 上派发左侧 start / progress，验证预览位移递增；取消后验证原位恢复。再派发右侧手势，确认后在 96ms / 256ms 时要求面板仍存在，完整动画后消失，并验证文本、撤销与重做。

另从模拟器屏幕边缘注入真实系统返回滑动，要求只关闭个性化、保留主编辑页和草稿。保留既有点叉动画、三条关闭路径与原生按住垂直拖动测试，覆盖 Material 和两种玻璃悬浮主题。

- 9 项单元测试通过；debug / androidTest / release 构建成功，lint 0 errors、7 条既有 warning。
- Material、液态玻璃悬浮、毛玻璃悬浮的面板专项各 5 项通过，用时分别为 36.771 / 34.918 / 34.451 秒，包含真实边缘侧滑、受控预测进度 / 取消 / 提交、新旧关闭路径与垂直拖动回归。
- EditorFlowTest 8 项通过，70.241 秒：剪贴板导入、精确复制退出、撤销 / 文本保留、主题与窗口重建、短窗口 / 大字及磁贴状态。本轮设备检查合计 23 项。

## 交付

- 版本 0.1.15 / versionCode 16，APK：`artifacts/moment-0.1.15.apk`，2,306,475 字节。
- SHA-256：`C5F0799E4035AF721343A9306BD66A91E2DE8F3BEF7009D6358334A195D0B3ED`。
- 复用既有签名身份，签名与 16KB 对齐检查通过。正式清单不包含测试入口。
- 原始截图、测试 / 构建 / lint 日志、签名结果及原生录屏保存在 `artifacts/validation-0.1.15/`。
- 正式包从 0.1.14 覆盖升级成功，液态玻璃、悬浮、浅色与配色设置保持一致；状态对照见 `upgrade-verification.json`。
- 系统动画缩放 1，正式包右侧屏幕边缘返回成功，保留两行演示草稿，原生录屏为 `palette-back-0.1.15.mp4`。完成操作返回 SettingsHomepageActivity，系统模糊保持启用。结果见 `release-verification.json`。
- 本轮在 Android 17 / API 37 Pixel 9 模拟器验证，未在厂商真机或 Android 10–16 实测。
