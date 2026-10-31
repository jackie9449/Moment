# 片刻 0.1.11 · 界面设计语言统一

## 改动

设计规范见 `ui-design-system.md`。三种主题共用“编辑卡片＋独立工具栏”，状态、字数与完成统一收进卡片页首；移除悬浮模式额外的状态胶囊。编辑卡片 28dp、工具栏 24dp、按钮 / 选项 16dp 圆角，48dp 操作范围。所有主题使用同一主色完成按钮，工具栏左右分组、主题入口居中，A− / A+ 使用相同基准字号。

个性化页面采用功能标题、同规格主题页面图示、紧凑分段选项和一致的设置行；快捷启动改为紧凑功能入口。保留主题材质、明暗、配色、悬浮、快捷开关状态与编辑功能。没有重新添加主界面品牌或营销标题，没有增加权限或依赖。

版本为 0.1.11 / code 12，沿用原发布身份签名。

## 验证

- 9 项单元测试通过。debug / release / androidTest 构建成功，lint 0 errors、7 条既有 Warning。
- Android 17 / API 37 Pixel 9 模拟器，完整 15 项设备回归通过，103.380 秒：剪贴板精确导入 / 复制 / 完成退出、主题与撤销保留、快捷开关生命周期、小窗口 / 1.5 倍系统字体、键盘几何与长文本光标、玻璃渲染及悬浮兼容。
- 毛玻璃悬浮专项 2 项通过，20.482 秒。
- 液态玻璃普通 / 悬浮键盘矩阵各 3 项通过，21.195 / 20.918 秒；毛玻璃普通 / 悬浮各 3 项通过，21.502 / 23.930 秒。包含文末与中间光标、手动滚动保留及输入法只占用相应空间的严格断言。共 29 项设备检查。
- 最后仅调整页首元信息颜色，提高通透背景下的辨识度；重新构建、执行单元测试和 lint，正式包再完成原生视觉与交互检查。功能和布局逻辑没有进一步改动。
- 正式包从 0.1.10 成功覆盖升级，保留毛玻璃选择、浅色、字号及 60 字符示例内容；`upgrade-main.xml` / `upgrade-settings.xml` 留存状态。`editor-before-0.1.10.png` 与 `frosted-light.png`、`settings-before-0.1.10.png` 与 `settings-light.png` 可比较旧版和改版。
- 原生页面树中，三张风格卡片高度均为 283px。三种主题的编辑视口和字号按钮坐标完全一致，分别记录于 `style-choice-geometry.json` 与 `shared-editor-geometry.json`。
- 正式包按住缩小字号：Material / 液态玻璃 / 毛玻璃分别有 14263 / 6862 / 1773 个变化像素，其余四个按钮的变化像素均为 0。见 `independent-press.json` 与各主题按压截图。
- 设置风格卡片按下时，圆角内部 85378 个像素变化，圆角外部变化像素为 0；没有方形反馈溢出。见 `choice-rounding.json`。
- 逐一检查三种主题的浅色 / 深色、两种玻璃的悬浮状态，以及深浅个性化页面和悬浮键盘界面。悬浮背景为真实系统 Settings；完成后返回 SettingsHomepageActivity，并通过真实系统快捷开关再次启动。
- 签名及 16KB 对齐校验通过，正式包不含测试入口；系统动画比例恢复为 1，跨窗口模糊开启。仅操作模拟器。

## 交付

- APK：`artifacts/moment-0.1.11.apk`，2,290,091 字节，可覆盖安装。
- SHA-256：`2C024C06FB73A26CCD0E5D9131A047200076F00C51C3C958FD5DC199209AB386`。
- 原始日志、单元测试 XML、签名、清单、几何 / 像素分析与原生截图在 `artifacts/validation-0.1.11/`。
- 主界面：`material-light.png` / `material-dark.png`、`liquid-light.png` / `liquid-dark.png`、`frosted-light.png` / `frosted-dark.png`。
- 设置：`settings-light.png` / `settings-dark.png`。悬浮：`liquid-floating-light.png` / `liquid-floating-dark.png`、`frosted-floating-light.png` / `frosted-floating-dark.png`、`frosted-floating-keyboard.png`。
- 本轮设备验证范围为 Android 17 模拟器，未在厂商真机或 Android 10–16 实测；不同系统的模糊支持与兼容表现沿用既有策略。
