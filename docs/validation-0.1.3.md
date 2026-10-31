# 片刻 0.1.3 · 输入法展开布局稳定性

## 修改

- 移除底部“整理好，点‘完成’即可复制并退出”提示及其淡出后移除布局的动画。
- 常规 / 紧凑装饰布局改为依据未扣除输入法空间的窗口高度选择。编辑区域仍按真实输入法 inset 缩放，避免键盘动画中途触发布局断点。
- 保留极小可用区域的必要控件折叠，以及 0.1.2 的长文本光标避让与自由滚动。
- versionCode 4，versionName 0.1.3。

## 复现与验证

新增设备回归使用 60 行文本，关闭并重新展开真实输入法，检查编辑区顶部保持不变，底部缩减量等于输入法 inset 与导航栏 inset 的差值。

修复前同一测试失败：编辑区顶部从 641px 跳到 459px。修复后启用系统动画运行通过，耗时 19.617 秒；原始记录为 `artifacts/validation-0.1.3/layout-before.txt` 与 `layout-with-ime-animation.txt`。

单元测试 7 项通过；lint 0 errors、7 条既有警告；debug/release 构建、签名及对齐验证通过。

完整设备回归 8 项通过，耗时 65.08 秒，包括剪贴板导入、编辑复制退出、撤销重做、快捷设置磁贴、主题与重建、大字体与小窗口，以及 3 项输入法布局 / 光标回归。首轮运行遇到模拟器系统崩溃（SystemUI / Phone 报 DeadSystemException），崩溃日志单独保留；冷启动模拟器并使用软件渲染后重跑成功。

## 交付

- 签名 APK：`artifacts/moment-0.1.3.apk`，复用原有发布签名，可覆盖升级。
- SHA-256：`BD49CF9894F4BFE33D704F3504BB13CF1BDD09D6A67CA9C0CD2D3F751FF4AFDE`。
- API 29–37，release 清单不含调试剪贴板测试 Activity。
- 正式签名包已在模拟器重新安装并恢复系统动画；界面截图为 `artifacts/editor-0.1.3.png` 与 `artifacts/editor-keyboard-0.1.3.png`，长文本第 60 行光标可见，底部完成提示已移除。
- 本轮使用 Android 17 / API 37 Pixel 9 模拟器，未在实际厂商手机上复测。
