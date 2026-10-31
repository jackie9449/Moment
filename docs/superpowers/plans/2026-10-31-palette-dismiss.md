# Palette Dismiss Gesture Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Make the editor progressively return to full clarity while the personalization sheet is dragged down, including reversal and settling, shipping 0.1.13 / code 14.

**Architecture:** Keep Material3's sheet gestures, anchors, dialog, nested scroll and outside / back dismissal. Replace its visible-state scrim with a transparent touch-dismiss scrim, then draw the visible scrim over the editor using the actual exposed sheet fraction. Report offset / measured-height progress using snapshotFlow; no second animation or target-state approximation.

**Tech Stack:** Existing Compose Material3 1.4.0 SheetState.requireOffset, Modifier.layout, snapshotFlow, Canvas, coroutine hide().

**Spec:** User request and docs/ui-design-system.md. Material3 1.4.0 source confirms the original scrim animates `targetValue != Hidden` instead of drag position.

**Verified adjustment:** The floating Activity's `backgroundDimEnabled` is also inherited by Material3's dialog window. Native held-drag tests exposed its fixed dim layer, which remained until window dismissal. Clear only the sheet dialog's `FLAG_DIM_BEHIND` and dim amount through public `DialogWindowProvider`; retain the Activity's existing dim / blur and the sheet's native input / accessibility behavior. Liquid floating held-drag test passes after this correction.

## Constraints

Preserve all themes, floating transparency / blur parameters, draft / undo, exact copy / exit, tile state, IME geometry and accessibility. No dependencies or permissions. Public APIs only. Execute inline in the existing non-Git workspace.

### Task 1: Position-driven backdrop

Files: AppearanceSheet.kt, EditorScreen.kt, build.gradle.kts.

- [x] Retain `sheetState` and measure the incoming bounded container height and actual sheet height without changing layout. In a LaunchedEffect, snapshotFlow the finite initialized offset and calculate `((containerHeight-offset)/min(containerHeight,sheetHeight)).coerceIn(0f,1f)`.
- [x] Add `onVisibilityChanged: (Float) -> Unit`; use rememberUpdatedState for the callback and reset to zero on disposal. Disable only the library's scrim paint using Color.Transparent, keeping outside-tap / back behavior.
- [x] In the editor, draw the existing 32% scrim with Canvas alpha equal to this fraction; read state in the draw phase. Preserve the initial fully exposed shade during Activity state restoration.
- [x] Route the close button through `sheetState.hide()` and dismiss only after Hidden. Keep standard fling, reverse and cancellation handling.
- [x] Build debug and inspect slow drag / reverse / dismissal in the native app.

### Task 2: Behavioral and native rendering verification

Files: new androidTest SheetDismissTest.kt; existing EditorFlowTest, FloatingModeTest and CaretVisibilityTest; validation docs and APK.

- [x] Launch with a known draft; inject a held pointer on the handle, sample background pixels at multiple drag distances before release and assert gradual brightening. Reverse the pointer and assert dimming returns before closing. Complete a drag and check draft preservation.
- [x] Exercise animated close, back and outside taps. Run the new checks for Material and floating Liquid / Frosted; keep existing editor, floating and caret regressions appropriate to lifecycle changes.
- [x] Build / unit / lint / release; sign with the existing identity, cover-install, retain native drag screenshots / recording and verify return to the previous app. Update README, design notes and docs/validation-0.1.13.md.
