# 片刻 0.1.6 · 个性化页文案与圆角反馈

## 修改

- 移除“让片刻，更像你”及副文案，关闭按钮合并到“界面风格”标题行，减少顶部空白。
- 为风格、显示模式及配色的 selectable 修饰符添加外层圆角裁切，与各自 Surface 共用同一 shape。原先 selectable 的涟漪位于 Surface 内部裁切之外，因而会出现矩形反馈。
- 壁纸配色整行点击也按容器的 20dp 圆角裁切，保留 Switch 自身的反馈。
- versionCode 7，versionName 0.1.6。编辑、主题偏好及剪贴板行为沿用现有实现。

## 验证

- 单元测试 7 项通过；lint 0 errors、7 条既有警告；debug / release 构建通过。
- Android 17 / API 37 Pixel 9 模拟器完整设备回归 9 项通过，耗时 44.62 秒，包含两种风格切换、重建后保存、文字保留、复制退出及键盘避让。
- 正式签名包已重新安装，恢复系统动画后，使用真实触摸 DOWN / CANCEL 检查 4 类圆角选项的按压状态。选项内部均有可见涟漪，圆角外侧像素与未按压时一致，无矩形溢出。
- 按压截图及像素核查记录保存在 `artifacts/validation-0.1.6/pressed-*.png`、`press-targets.json` 和 `ripple-corner-analysis.json`；不新增镜像实现的应用测试。
- 页面截图 `artifacts/appearance-0.1.6.png` 可见原标题与副文案已移除，关闭及各设置入口正常。

## 交付

- APK：`artifacts/moment-0.1.6.apk`，沿用原签名，可覆盖升级，签名和对齐验证通过。
- SHA-256：`011A6CA4742BFCA34129D280B8104824AB7B1A66B49A0213D16C06C7B86428C7`。
- 原始日志位于 `artifacts/validation-0.1.6/`。本轮为模拟器验证，未在实际厂商手机上复测。
