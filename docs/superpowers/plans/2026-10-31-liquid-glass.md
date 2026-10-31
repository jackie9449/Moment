# Liquid Glass Theme Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Add a selectable Liquid Glass style while preserving Material and all clipboard editing behavior.

**Architecture:** Persist an independent InterfaceStyle enum. A shared procedural scene is rendered behind the editor and sampled with rounded lens refraction in small glass controls on API 33+, with a Canvas fallback for API 29–32. Keep the document matte and legible, and keep all keyboard geometry independent of material rendering.

**Tech Stack:** Existing Kotlin, Compose Material 3, Android RuntimeShader / AGSL, Canvas; no new dependency or permission.

**Spec:** docs/spec.md and docs/liquid-glass-design.md.

## Global Constraints

- Retain Material as the default for existing users and support explicit style switching.
- Preserve display mode, palette, font size, current text / selection, undo and clipboard semantics.
- Keep Done at the upper right, theme entry centered below, 48dp touch targets and keyboard avoidance.
- Glass belongs to controls; text remains sharp and high contrast. Avoid layered glass surfaces.
- Respect system animation scale; no permanent animation loop, screenshots of private content, or sensor permission.
- Ship versionCode 6 / versionName 0.1.5 signed by the existing identity.

### Task 1: Theme selection and optical renderer

**Files:** AppearanceStore.kt, MomentTheme.kt, AppearanceSheet.kt, strings.xml; new ui/LiquidGlass.kt.

**Interfaces:** `enum class InterfaceStyle { MATERIAL, LIQUID_GLASS }`, `AppearanceStore.updateStyle(value: InterfaceStyle)`, `LocalGlassAppearance`, `LocalGlassScene`, `GlassBackdrop(modifier: Modifier)`, `GlassControlSurface(modifier: Modifier, radius: Dp, tint: Color, press: State<Float>?, content: @Composable () -> Unit)`.

- [x] Add persisted style with MATERIAL fallback and a two-choice selector tagged `style_MATERIAL` / `style_LIQUID_GLASS`.
- [x] Supply style and resolved light / dark mode through a CompositionLocal from MomentTheme.
- [x] Render a deterministic soft blue / violet / warm scene. Share coordinates between backdrop and controls.
- [x] API 33+ uses remembered RuntimeShader and ShaderBrush: rounded signed distance, rim refraction, soft scattering, directional highlights, tinted fill, pressed light response. API 29–32 uses gradients and edge highlights.
- [x] Build debug APK and launch Liquid Glass on emulator; inspect shader compile / rendering errors.

### Task 2: Integrate and verify behavior

**Files:** EditorScreen.kt, EditorChrome.kt, EditorFlowTest.kt, CaretVisibilityTest.kt, app/build.gradle.kts; README / spec / validation notes.

- [x] Keep Material rendering; for glass use a matte document, floating Done capsule, and one glass toolbar with thin overlay controls.
- [x] Share the toolbar interaction source for light feedback; animate primary button compression with Compose spring and no layout resize.
- [x] Add device regression for style persistence after recreation and exact draft preservation / completion. Settings nodes use `performScrollTo()` as the sheet becomes taller.
- [x] Let CaretVisibilityTest select a style through instrumentation argument `interface_style`, then run its three tests in both styles.
- [x] Run unit, lint, debug / release builds and the nine-test default suite; run three glass caret regressions with system animation enabled.
- [x] Visually check light / dark, empty / long text and keyboard. Sign APK, reinstall release, save screenshots and validation logs.

Execution stays inline under the existing authorization. This workspace has no Git repository.
