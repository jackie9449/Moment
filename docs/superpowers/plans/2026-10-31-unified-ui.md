# Unified Editor UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Unify all three themes and personalization around a quieter, coherent layout and ship signed 0.1.11.

**Architecture:** Put layout constants into MomentLayout.kt, retain material renderers, and share one editor card / toolbar layout across styles. Split personalization into its sheet, selection controls and theme preview code. Preserve existing editor state and window behavior.

**Tech Stack:** Existing Compose Material3, Canvas, glass renderers. No dependency / permission changes.

**Spec:** docs/ui-design-system.md and docs/spec.md.

## Global Constraints

API 29 minimum, target 37. 48dp targets. No top-left brand / marketing copy. Preserve exact clipboard import / copy / exit, draft and undo, tile state, floating compatibility and stable keyboard geometry. Every interaction source remains local. Execute inline in the current checkout; no Git repository is present.

### Task 1: Shared editor structure

Files: new ui/MomentLayout.kt; EditorScreen.kt; EditorChrome.kt; EditorDocumentSurface.kt; GlassButtons.kt; MomentTheme.kt; build.gradle.kts.

- [x] Define `MomentLayout.PageCorner=28.dp`, `ChromeCorner=24.dp`, `ControlCorner=16.dp`, `PageGutter=20.dp`, `Gap=12.dp`. Use 320ms theme transitions already present.
- [x] Replace outer theme-specific sheet with `Column(spacedBy(12.dp)) { EditorDocumentSurface(weight(1f)) { Column { Header(); Divider(); Box(weight(1f)) { existing BasicTextField } } }; Toolbar() }`. Keep caret effects / IME constraints and text tags unchanged.
- [x] Give Material the same reading card and separate rounded toolbar as both glass styles. Remove floating status badge and unify metadata padding; preserve `textMetrics` and `done` tags.
- [x] Use a primary Button shape16 / min48 for Done in every style. Use shared 48dp rounded toolbar buttons, local ripple for Material, local glow / spring for glass. Keep content descriptions and undo / redo semantics.
- [x] Build and inspect Material and glass main screens before setting-page refinement.

### Task 2: Personalization controls

Files: AppearanceSheet.kt; new AppearanceControls.kt, ThemePreview.kt; strings.xml.

- [x] Add functional heading 个性化 with close tag; set section gaps24, item gaps12, common option corners16 and muted section labels.
- [x] Draw a miniature shared page for each theme in `ThemePreview(style, modifier)` using Canvas. Show three equal style choices with name and common selection outline / check; remove redundant descriptions from this view.
- [x] Implement shared `SelectionSegment(selected, modifier, onClick, content)` and a parent segmented Surface with clipped independent radio-button feedback. Keep `style_*`, `mode_*`, `palette_*` tags and selected semantics.
- [x] Implement `AppearanceToggleRow` for floating and dynamic color; clickable row with one Switch semantic target and no nested competing click. Preserve `floatingMode`, `dynamicColor` tags.
- [x] Replace add-tile marketing card with compact icon / description / add action row; keep `quickTileCard` visibility condition and `addTile` action.
- [x] Inspect light/dark settings, large-font short-window behavior and round-corner press feedback.

### Task 3: Regression and release

Files: existing Android and unit tests, docs/validation-0.1.11.md; artifacts/validation-0.1.11/; README and spec.

- [x] Run unit tests, lint and debug / release builds. Run full 15 device checks, frosted floating pair, caret matrices for both glass themes with floating on/off as relevant to the new common layout.
- [x] Check native screenshots across Material / Liquid / Frosted, light/dark and floating. Measure toolbar press differences to verify only one target changes; confirm normal IME top / height and caret tests still pass.
- [x] Sign using existing identity, cover-install over 0.1.10, verify persisted settings and Done returning to the previous app. Save readable native before / after evidence and final APK SHA; update documentation and deliver.
