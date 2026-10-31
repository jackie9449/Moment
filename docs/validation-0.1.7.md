# 片刻 0.1.7 · 独立按压反馈与快捷开关状态

## 修改

- 移除底部工具栏共用的 MutableInteractionSource。撤销、重做、主题、缩小及放大字号各自使用原生按钮的独立交互状态。
- 移除单个按钮引起的整条玻璃工具栏压缩与高光反馈；完成按钮仍保留自身独立的玻璃按压动效。
- 新增 QuickTileState，使用独立偏好文件观察系统 TileService 添加 / 移除回调。系统添加成功或确认已存在后隐藏整张添加卡片，移除后恢复；已打开的个性化页面同步更新。
- 磁贴采用 active 模式，通过公开的 requestListeningState 和监听回调恢复旧版本已添加但没有本地记录的状态。确认移除后，迟到的监听回调不能覆盖移除状态；关闭 Activity 时注销偏好监听器。
- 仅系统 ADDED / ALREADY_ADDED 返回值确认添加成功，点击请求按钮或取消确认不会被记作添加成功。不增加权限或读取受限系统设置。
- versionCode 8，versionName 0.1.7。

实现依据：[Android TileService 生命周期](https://developer.android.com/reference/android/service/quicksettings/TileService)、[快捷开关添加请求](https://developer.android.com/reference/android/app/StatusBarManager#requestAddTileService(android.content.ComponentName,%20java.lang.CharSequence,%20android.graphics.drawable.Icon,%20java.util.concurrent.Executor,%20java.util.function.Consumer%3Cjava.lang.Integer%3E))。

## 验证

- 单元测试 7 项通过，lint 0 errors、7 条既有警告，debug / release 构建通过。
- Android 17 / API 37 Pixel 9 模拟器完整设备回归 10 项通过，耗时 48.53 秒。
- 新增真实 SystemUI 添加 / 移除回归：卡片随系统添加隐藏、重建后保持隐藏；清除状态偏好而保留系统磁贴时能够恢复；移除后卡片出现，再次重建后仍显示，编辑文字保持不变。
- 正式签名包重新安装，启用系统动画，用实际触摸 DOWN / CANCEL 对比 Material 与液态玻璃的按钮反馈。按下缩小字号时，仅目标区域产生反馈，其他四个按钮区域像素完全不变；见 `independent-press-analysis.json` 与两套 idle / pressed 截图。
- 正式包在个性化页面打开期间，通过实际系统添加 / 移除再次验证：`tile-missing.xml`、`tile-added.xml`、`tile-removed.xml` 及对应截图记录卡片出现 / 消失。最后保留模拟器磁贴已添加状态。
- 原始日志和视觉证据保存在 `artifacts/validation-0.1.7/`；页面截图为 `artifacts/tile-missing-0.1.7.png`、`tile-added-0.1.7.png`、`tile-removed-0.1.7.png`。

## 交付

- APK：`artifacts/moment-0.1.7.apk`，沿用原签名，可覆盖升级，签名及对齐校验通过。
- SHA-256：`51C9C6EEE2F947DAB6F850393DC95B3112EEB1E9154A4772096075BC091ED5DF`。
- 本轮为 Android 17 模拟器验证，未在实际厂商手机上复测。
