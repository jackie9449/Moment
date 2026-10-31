# Independent Feedback and Tile State Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Isolate toolbar press effects and show the add-tile card only while the tile is absent.

**Architecture:** Native IconButtons own separate interaction sources; the glass toolbar surface stays steady. A small SharedPreferences-backed QuickTileState observes system TileService lifecycle callbacks and successful request results. Active listening requests reconcile existing tiles after upgrade without restricted settings reads.

**Tech Stack:** Existing Kotlin / Compose, SharedPreferences listener, Android TileService public lifecycle APIs.

**Spec:** docs/spec.md and current user request.

## Global Constraints

- Preserve clipboard text, selection, undo, themes and keyboard geometry.
- No new permissions, hidden API, secure-setting reads or application shell commands.
- Record real system addition / removal; never mark a tile added just because the request button was pressed.
- Hide the whole card, not just its add button; update an already-open appearance sheet.
- Ship signed versionCode 8 / versionName 0.1.7 with the existing identity.

### Task 1: Interaction isolation

**Files:** ui/EditorChrome.kt.

- [x] Remove the shared toolbar MutableInteractionSource and aggregate pressed feedback.
- [x] Let each IconButton / FilledTonalIconButton own its native interaction source; retain Done's private source.
- [x] Remove the glass toolbar's group compression / light response to a child press.
- [x] Verify held presses in Material and glass: only the target button changes.

### Task 2: Real tile lifecycle and card visibility

**Files:** new preferences/QuickTileState.kt; MainActivity.kt; QuickEditTileService.kt; AndroidManifest.xml; ui/EditorScreen.kt; ui/AppearanceSheet.kt; androidTest/EditorFlowTest.kt.

**Interfaces:** `QuickTileState(context)` exposes observable `added: Boolean`, `refresh()` and `close()`; companion `record(context, added)` writes a dedicated preference file. `EditorScreen` and `AppearanceSheet` receive `tileAdded: Boolean`.

- [x] Persist / observe tile addition in a dedicated file; unregister the preference listener when Activity is destroyed.
- [x] Record true in `onTileAdded`, confirmed `onStartListening`, and successful ADDED / ALREADY_ADDED request results; record false in `onTileRemoved`.
- [x] Enable ACTIVE_TILE and call `TileService.requestListeningState` when the Activity resumes / regains focus to resync pre-existing tiles after upgrade.
- [x] Show `quickTileCard` only when `tileAdded` is false, retaining the native add dialog flow.
- [x] Add an end-to-end device regression using actual SystemUI add/remove commands; verify live card hiding, recreation, missing preference recovery and reappearance after removal.
- [x] Build / lint / run existing + new device regressions, sign and install release, capture held-press evidence and tile-card states, update validation / README.

Execution is inline under existing authorization. This workspace has no Git repository.
