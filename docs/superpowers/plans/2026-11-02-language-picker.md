# In-app Language Picker Plan

**Goal:** Add System / 简体中文 / 繁體中文 / English / 日本語 selection in personalization, 0.1.17 / code 18.

**Architecture:** API33+ reads / writes LocaleManager so system settings and in-app settings share one source. API29–32 stores a locale tag and wraps Activity / TileService base configuration, recreating only after a real change. Retain ViewModel undo and saved editor / sheet state. Native language names stay recognizable after switching.

- [x] Implement AppLanguage mapping, persistence / framework synchronization, legacy context wrapper and migration on OS upgrade.
- [x] Add a localized language setting with selected value and a five-option menu. Preserve all modal back behavior.
- [x] Test selection of every language, reset to System, external system changes, recreation / persistence, exact draft / undo and unchanged clipboard; verify legacy configuration path.
- [x] Build / lint / unit / device checks, sign, cover-install and inspect native picker. Document and commit locally.
