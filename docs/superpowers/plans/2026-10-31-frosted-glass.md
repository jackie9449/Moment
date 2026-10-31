# Frosted Glass Theme Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a distinct Apple standard-material-inspired frosted theme with optional real-background floating mode, shipping signed 0.1.10 / code 11.

**Architecture:** Keep the existing glass layout and local interactions. A separate FrostedGlass.kt records only the app-owned decorative background into a Compose GraphicsLayer, blurs a padded local crop using native BlurEffect on API 31+, then composites adaptive translucent fills and a fine separator below sharp foreground content. Floating mode uses the existing transparent Activity and system cross-window blur, with a larger radius for frost and opaque fallback when unavailable.

**Tech Stack:** Existing Compose GraphicsLayer / BlurEffect, Canvas, public Android cross-window APIs; no new dependencies or permissions.

**Spec:** docs/spec.md and current user request. Research: Apple WWDC19/808 materials, UIBlurEffect and UIVibrancyEffect; Android window blur and Compose graphics documentation.

## Global Constraints

- Preserve existing Material and Liquid Glass, clipboard import, exact copy / exit, undo, tile state and keyboard geometry.
- Minimum API 29, target 37. Floating supported API 30+, cross-window blur API 31+ when enabled. Older systems use higher-opacity fallback.
- Never blur editor text or capture another app; background render layers contain decorative content only.
- 48dp targets, independent feedback, no idle animation loop. No Git repository is present; execute inline under existing authorization.

### Task 1: Theme and native material

Files: preferences/AppearanceStore.kt, ui/MomentTheme.kt, ui/AppearanceSheet.kt, ui/LiquidGlass.kt, new ui/FrostedGlass.kt, ui/EditorScreen.kt, new ui/EditorDocumentSurface.kt, FloatingWindowController.kt, MainActivity.kt, strings.xml, build.gradle.kts.

- [x] Add `InterfaceStyle.FROSTED_GLASS`; use `style != MATERIAL` for floating eligibility and common glass layout; expose `GlassAppearance.frosted` separately so Liquid rendering remains unchanged.
- [x] Add theme card labels 毛玻璃 / 柔雾 · 沉静, retain rounded clipped feedback and readable three-column choices.
- [x] Implement `FrostedBackdrop(Modifier)` and `FrostedSurface(Modifier, Dp, Color, State<Float>?, @Composable () -> Unit)`. Use `rememberGraphicsLayer()`, `record`, `drawLayer` and `BlurEffect(18.dp.toPx(),18.dp.toPx(),TileMode.Clamp)` on the background crop only; reserve 54dp padding to avoid blur crop seams. Compose adaptive neutral tint and subtle fine border after blur. No lens or specular glint in frost.
- [x] Use stronger system blur for frost (20dp) with existing listener, preserve Liquid's 6dp. Update style while floating without unnecessary Activity recreation; preserve draft and undo.
- [x] Build debug and inspect light/dark + floating before final tuning.

### Task 2: Verification and delivery

Files: debug GlassMaterialProbeActivity.kt, androidTest FrostedMaterialTest.kt, FloatingModeTest.kt, EditorFlowTest.kt; docs and artifacts/validation-0.1.10/.

- [x] Extend debug fixture with a frost intent option. Assert native screenshot grid contrast behind frost is substantially reduced while foreground text retains dark sharp detail; preserve Liquid optical check.
- [x] Add end-to-end theme persistence / floating-style switching preserving exact draft and undo. Parameterize existing floating suite for both glass styles, including blur disabled and return to underlying task.
- [x] Run unit, lint, debug/release builds, full existing suite plus new frost checks; run frost floating caret matrix with animations enabled.
- [x] Sign same identity, cover-install 0.1.10, save native light/dark ordinary and floating previews and independent-button press check. Verify Done returns previous app, update design notes, validation and README; deliver APK.
