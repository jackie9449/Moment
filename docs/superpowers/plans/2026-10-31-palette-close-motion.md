# Palette Close Motion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Give the personalization close button a deliberate 420ms eased slide while preserving the position-linked backdrop, delivering 0.1.14 / code 15.

**Architecture:** Material3 1.4.0 hardcodes FastEffects for hide and exposes no public motion override. Use one Animatable fraction only for close-button dismissal, translate the sheet's graphics layer by its remaining distance to Hidden, multiply its measured exposure by the remaining fraction for the existing scrim, and remove after the animation completes. Keep native drag / settling / outside / back otherwise. Lock competing dismiss gestures and repeat clicks during the short exit; retain system duration scaling via Compose animation APIs.

**Tech Stack:** Existing Compose Animatable, tween(420, CubicBezierEasing(.32f, 0f, .18f, 1f)), graphicsLayer, snapshotFlow, ModalBottomSheetProperties.

**Spec:** Current user request; docs/ui-design-system.md.

## Global Constraints

Minimum API 29 / target API 37; no dependency or permission changes. Preserve all themes, floating blur / dim handling, drafts, undo, copy / exit, native manual dragging and IME geometry. Execute inline in the existing non-Git workspace.

### Task 1: Close-button motion

Files: AppearanceSheet.kt; app/build.gradle.kts.

- [x] Add `val closeProgress = remember { Animatable(0f) }` and a guarded `closing` state. On close, set the guard synchronously, animate to 1 using the 420ms curve, then invoke the latest onDismiss callback. Do not append native `hide()` after reaching the screen edge.
- [x] Calculate backdrop fraction as `((containerHeight-offset)/travel).coerceIn(0f,1f) * (1f-closeProgress.value)`. Apply graphics-layer `translationY = max(0f, containerHeight-offset) * closeProgress.value`; read state in the layer lambda and preserve measurement.
- [x] While closing, disable sheet gestures and dialog back / outside dismissal and ignore repeat close clicks. Outside / back / drag otherwise remain native. Bump to 0.1.14 / code 15.

### Task 2: Verify and deliver

Files: SheetDismissTest.kt, README.md, docs/ui-design-system.md, docs/validation-0.1.14.md; artifacts/validation-0.1.14/.

- [x] Add a deterministic Compose-clock check: freeze after fully opening, click close, advance 96ms and 160ms, assert the sheet persists and its handle moves progressively down; finish past 420ms and assert dismissal and preserved draft. Existing native held-drag / reverse / close paths remain unchanged.
- [x] Build debug / androidTest / release, run unit / lint, run SheetDismissTest for Material and floating Liquid / Frosted, and EditorFlowTest for recreation / clipboard interactions.
- [x] Sign with the existing identity, cover-install over 0.1.13, record native close-button animation with system animation scale 1, check final draft and Done exit, retain evidence and update version / validation docs. Mark steps complete only after checks pass.
