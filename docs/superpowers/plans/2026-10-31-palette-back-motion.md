# Palette Back Motion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Make personalization side-back gestures preview continuously and settle with the same gentle exit as the close button, shipping 0.1.15 / code 16.

**Architecture:** Register a public OnBackPressedCallback on the sheet's own dialog ViewTree dispatcher and disable Material3's built-in back callback, whose commit otherwise uses internal FastEffects. Feed predictive progress into the existing shared sheet / scrim Animatable, preview up to 24% of remaining travel, finish from that position in 420ms, or return in 260ms on cancellation. Legacy back routes to the same close action; native vertical drag stays intact.

**Tech Stack:** AndroidX Activity 1.13.0 OnBackPressedCallback / BackEventCompat / ViewTree dispatcher, existing Compose Animatable / graphicsLayer / snapshotFlow.

**Spec:** Current user request; docs/ui-design-system.md.

## Global Constraints

Minimum API 29 / target API 37, no new dependencies or permissions, existing signing identity. Preserve drafts / undo / exact copy / exit, all themes and floating dim / blur, native drag / reversal, outside tap, IME geometry and system duration scaling. Execute inline in the current non-Git workspace.

### Task 1: Dialog-scoped predictive back

Files: new ui/SheetBackHandler.kt; ui/AppearanceSheet.kt; app/build.gradle.kts.

- [x] Implement `SheetBackHandler(onStart: () -> Unit, onProgress: (Float) -> Unit, onCancel: () -> Unit, onCommit: () -> Unit)`. Read LocalView inside the modal, find its ViewTree OnBackPressedDispatcherOwner, register an always-enabled callback through DisposableEffect, use rememberUpdatedState for event handlers and remove on disposal.
- [x] Set `shouldDismissOnBackPress=false` on Material3's dialog. Track a back gesture and return-animation Job. Start cancels an unfinished return and retains current fraction. Progress snaps the shared close fraction to `start + (.24f-start)*progress.coerceIn(0f,1f)`; cancel animates to zero in 260ms and unlocks drag. Commit cancels pending preview work and uses existing 420ms close from the current position. Ignore competing back events while closing.
- [x] Disable native vertical gestures during preview / rollback as well as closing, leave outside-tap behavior otherwise intact, bump 0.1.15 / code 16.

### Task 2: Verify and ship

Files: SheetDismissTest.kt; README.md; design and validation docs; artifacts/validation-0.1.15/.

- [x] With a frozen Compose clock, dispatch start / progress on the actual ComponentDialog dispatcher, verify increasing preview displacement; cancel and advance 300ms, assert original position and draft. Start on the opposite edge, progress, commit, verify sheet still exists at 96ms / 256ms and disappears after 420ms; verify draft / undo / redo.
- [x] Inject an actual emulator edge swipe to verify dialog wiring, close only personalization and retain the editor. Retain existing close-button and native vertical drag checks for Material and floating Liquid / Frosted; run EditorFlowTest for clipboard / recreation. Build debug / androidTest / release, unit and lint checks.
- [x] Sign, cover-install over 0.1.14, record native edge-back with animation scale 1, verify preferences / draft / Done exit, update README and validation record, complete checkboxes after verification.
