# Editor Visual Balance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Replace the disconnected top-right action cluster with one coherent Material 3 editing sheet.

**Architecture:** Keep session, selection and IME scrolling in EditorScreen. Put the sheet header and toolbar in EditorChrome.kt; header contains clipboard status, counts and Done, footer groups undo/redo, appearance and type size. Window-derived compact mode stays outside imePadding.

**Tech Stack:** Existing Kotlin / Compose Material 3; no new dependencies.

**Spec:** docs/spec.md, current user request for harmonious visual balance.

## Global Constraints

- Keep the brand icon/name absent, and keep Done at the upper right.
- Preserve exact clipboard text, themes, Quick Settings, caret visibility and manual scroll.
- Keep 48dp interactive targets, safe drawing insets and stable IME layout.
- Deliver signed version 0.1.4 using the existing signing identity.

### Task 1: Unified editing sheet

**Files:** Modify EditorScreen.kt; create app/src/main/kotlin/com/jackie/moment/ui/EditorChrome.kt; modify app/build.gradle.kts and docs/spec.md.

**Interfaces:** EditorSheetHeader(importedText: Boolean, edited: Boolean, metrics: TextMetrics, compact: Boolean, minimal: Boolean, completing: Boolean, onDone: () -> Unit). EditorSheetToolbar(editor: EditorState, appearance: AppearanceStore, onAppearance: () -> Unit).

- [x] Add the header: weighted two-line metadata column, trailing 48dp Done button; one-line truncated metadata keeps height stable.
- [x] Add footer: undo/redo, centered tonal appearance button, A− / A+, with flexible gaps and 48dp hit areas.
- [x] Replace the disconnected rows with one rounded sheet, header divider, padded editor and footer divider. Remove TEXT and Spark decoration.
- [x] Keep `val compact = maxHeight < 540.dp` outside the inner `BoxWithConstraints(Modifier.fillMaxSize().imePadding())`; derive minimal from inner height only.
- [x] Set `versionCode = 5`, `versionName = "0.1.4"`.

### Task 2: Verify and deliver

**Files:** Existing EditorFlowTest.kt and CaretVisibilityTest.kt; new docs/validation-0.1.4.md and artifacts/validation-0.1.4/; README.md.

- [x] Run `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease :app:assembleDebugAndroidTest --offline`.
- [x] Run the existing eight Android 17 device regressions, including keyboard geometry and large fonts / short window. No new tests mirroring visual implementation.
- [x] Visually inspect light, dark, keyboard-open and keyboard-closed states; correct spacing if any controls collide or hierarchy feels uneven.
- [x] Run `tools/package.ps1 -SkipBuild`, install signed release on the explicit test emulator, restore system animation scales to 1, capture final UI evidence.
- [x] Save logs, hash and validation notes, update README, deliver the APK and a preview.

Execution is inline under the user's authorization to complete the UI improvement. This workspace has no Git repository; no commit step applies.
