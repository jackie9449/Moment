# Floating Glass Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Add an optional Liquid Glass mode that reveals the actual interface behind the editor.

**Architecture:** Persist a floating-mode preference, effective only with Liquid Glass. Resolve a translucent Activity theme before creation and use public Activity.setTranslucent on API 30+; recreate through normal saved-state restoration when effective mode changes. Use system blur behind on API 31+ where available, and translucent readable surfaces when unavailable. Keep the existing procedural optics in non-floating glass only.

**Tech Stack:** Kotlin / Compose, Android translucent Activity theme, public Window blur APIs; existing dependencies only.

**Spec:** docs/spec.md and current request for the previous phone interface as the glass background.

## Global Constraints

- Optional, default off, shown only under Liquid Glass. Material stays opaque.
- Retain the current document, selection, undo / redo and open appearance sheet across window recreation.
- No MediaProjection, screenshot capture, accessibility service or SYSTEM_ALERT_WINDOW permission in production.
- Respect system blur availability and system animation settings. Blur never applies to editor text.
- Preserve Done, clipboard import, tile status, independent button feedback and IME avoidance.
- Deliver signed 0.1.8 / versionCode 9.

### Task 1: Window mode and material

**Files:** preferences/AppearanceStore.kt; res/values/themes.xml; new FloatingWindowController.kt; MainActivity.kt; ui/MomentTheme.kt; ui/LiquidGlass.kt; ui/EditorScreen.kt; ui/EditorChrome.kt; ui/AppearanceSheet.kt; strings.xml.

**Interfaces:** AppearanceStore exposes `floatingMode`, `useFloatingWindow`, `updateFloatingMode(Boolean)`. FloatingWindowController(Activity) exposes observable `blurAvailable`, `updateDark(Boolean)`, `close()`. GlassAppearance adds `floating` / `blurAvailable`.

- [x] Add the saved switch tagged `floatingMode` in the glass-only settings section.
- [x] Select Theme.Moment.Floating before super.onCreate when enabled; preserve the opaque theme otherwise.
- [x] Recreate when the effective window mode changes; saved-state restoration prevents clipboard re-import.
- [x] Use a transparent root, soft contrast scrim, translucent document and glass edge gradients over the underlying window; skip opaque procedural shaders in floating mode.
- [x] Register / remove the API 31+ blur-availability listener; set blur-behind radius and adapt opacity when unavailable.
- [x] Build and inspect the actual emulator window before broad validation.

### Task 2: Runtime verification and delivery

**Files:** debug-only testing/ScreenBackdropActivity.kt / manifest; new androidTest/FloatingModeTest.kt; existing CaretVisibilityTest.kt and EditorFlowTest.kt; docs / artifacts.

- [x] Draw a controlled colored background in a separate test task. Launch floating editor above it and check actual composed screenshot colors at the margins, not only theme flags.
- [x] Verify floating preference persists; switch off / on without changing draft or undo; Material is opaque; Done copies exact text and returns to the previous task.
- [x] Permit the existing caret suite to run with `floating_mode=true` and Liquid Glass, and verify keyboard geometry / middle / end caret visibility.
- [x] Run unit / lint / debug / release builds and complete device regressions. Verify both blur-available and disabled rendering where supported.
- [x] Install signed release and visually inspect it over an actual system app, including dark mode and keyboard. Capture native screenshots, update docs and provide APK.

Execution is inline under the user's authorization. This workspace has no Git repository.

EditorSessionModel.kt retains undo in the Activity ViewModel; EditorSessionModelTest.kt verifies retained and process-restored state. The runtime pixel checks and system-app preview also cover transparent surfaces.
