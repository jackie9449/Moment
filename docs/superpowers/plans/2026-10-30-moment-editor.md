# Moment Text Editor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Deliver an installable, polished Chinese Material 3 clipboard editor with Quick Settings launch and copy-and-exit.

**Architecture:** A single Compose activity owns a foreground-only clipboard import gate and delegates text/history to an editor state holder. A small preferences store owns theme settings. A TileService starts the same editor with a fresh-session intent. UI, clipboard lifecycle, and platform integration remain separately testable.

**Tech Stack:** Kotlin, Android SDK 37, Jetpack Compose Material 3, Gradle, JUnit, AndroidX instrumentation and Compose UI tests.

**Spec:** `docs/spec.md`

## Global Constraints

- Application ID `com.jackie.moment`; app name `片刻`.
- minSdk 29, compileSdk 37, targetSdk 37.
- Foreground window focus is required before clipboard access.
- Keep exact text, including whitespace, newlines, and emoji.
- No network or storage permission in the production manifest.
- Complete the authorized implementation inline; no execution-choice approval gate is needed.

## File Structure

| File | Responsibility |
| --- | --- |
| `app/build.gradle.kts`, root Gradle files | Pinned Android/Compose dependencies and test configuration |
| `app/src/main/AndroidManifest.xml` | Activity and exported, permission-protected TileService declarations |
| `.../MainActivity.kt` | Window lifecycle, clipboard access, tile-add request, copy and finish |
| `.../editor/EditorState.kt` | Exact text, bounded undo/redo history, selection and counts |
| `.../preferences/AppearanceStore.kt` | Local appearance preferences |
| `.../ui/MomentTheme.kt` | Color schemes, typography, animated theme colors |
| `.../ui/EditorScreen.kt` | Adaptive editor layout and editing actions |
| `.../ui/AppearanceSheet.kt` | Appearance controls and Quick Settings entry |
| `.../ui/MomentIcons.kt` | Small code-native vector icon set |
| `.../QuickEditTileService.kt` | Secure, version-compatible launch from Quick Settings |
| `app/src/test/.../EditorStateTest.kt` | Unicode, whitespace, undo/redo, session behavior |
| `app/src/androidTest/.../EditorFlowTest.kt` | Real clipboard, focus/recreation, theme, completion, tile flow |
| `app/src/debug/.../ClipboardProbeActivity.kt` | Debug-only foreground clipboard fixture for device tests |
| `README.md`, `artifacts/` | Build/install directions, APK and visual evidence |

### Task 1: Buildable project and platform lifecycle

**Interfaces:** `EditorSession.import(text: String)` starts one new session; `MainActivity.finishEditing(text: String)` writes clipboard before finishing; the tile sends `MainActivity.ACTION_EDIT_CLIPBOARD`.

- [x] Scaffold the official Compose empty-activity template, retain its Gradle wrapper, and configure SDK 37/minSdk 29 and application ID.
- [x] Add a foreground-only, one-shot clipboard gate. The core lifecycle contract is:

```kotlin
override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus && pendingClipboardImport) {
        window.decorView.post { importClipboardWhenFocused() }
    }
}
```

- [x] Implement a TileService with `BIND_QUICK_SETTINGS_TILE`, a monochrome 24dp icon, an immutable activity PendingIntent on API 34+, and the legacy Intent branch below API 34. Use `unlockAndRun` when locked.
- [x] Add the in-app tile placement request using `StatusBarManager.requestAddTileService` on API 33+ and Chinese instructions below it.
- [x] Build `:app:assembleDebug` and confirm manifest permissions and exported components.

### Task 2: Editor and Material 3 appearance

**Interfaces:** `EditorState` exposes `value: TextFieldValue`, `canUndo`, `canRedo`, `update(value)`, `undo()`, `redo()`, and `startSession(text)`. `AppearanceStore` exposes mode, palette, dynamic color and font size and saves only these preferences.

- [x] Add meaningful state tests before the history implementation. Verify exact round-trip text and Unicode history:

```kotlin
@Test fun undoRedoPreservesWhitespaceAndEmoji() {
    val state = EditorState("  第一行\n🙂  ")
    state.update(TextFieldValue("  第一行\n🙂 完成  "))
    state.undo()
    assertEquals("  第一行\n🙂  ", state.value.text)
    state.redo()
    assertEquals("  第一行\n🙂 完成  ", state.value.text)
}
```

- [x] Implement bounded edit history; selection-only changes must not destroy redo. A new clipboard session resets history, while configuration restoration preserves the current text and selection.
- [x] Implement the responsive editor, theme sheet, string resources, accessible vector icons, adaptive launcher icon and fixed top-right “完成”. Provide animated theme color changes, page entrance and completion feedback without delaying the clipboard write.
- [x] Use local preferences for `SYSTEM`, `LIGHT`, `DARK`, palette, dynamic color and editor font size. Theme switching never reloads the clipboard.
- [x] Run `:app:testDebugUnitTest` and inspect generated UI with keyboard expanded, in light/dark, at large font and wide viewport.

### Task 3: Device validation and deliverables

**Interfaces:** Instrumentation tests seed/read the real clipboard through a debug-only foreground activity. The production release contains no clipboard fixture or debugging entry point.

- [x] Create an Android 17 emulator and wait for `sys.boot_completed=1` before installation.
- [x] Write and run real-device integration tests for launch paste → edit → copy-and-finish; focus loss and recreation; appearance persistence; Quick Settings tile launch. Assert task/clipboard outcomes rather than implementation details.
- [x] Run `:app:lintDebug`, `:app:assembleDebug`, `:app:assembleRelease`, the AndroidJUnitRunner suite through ADB (`adb -s emulator-5554 shell am instrument -w -r com.jackie.moment.test/androidx.test.runner.AndroidJUnitRunner`). Repair actual failures, then repeat only affected checks.
- [x] Review screenshots of the editor, theme drawer, dark appearance and keyboard layout. Fix clipping, contrast, spacing, status/navigation bars and touch targets.
- [x] Copy the verified installable debug APK and screenshots into `artifacts/`, write build/install/usage instructions, and disclose any real device/manufacturer limitations with exact validation evidence.

## Verification Commands

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
.\gradlew.bat :app:connectedDebugAndroidTest
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r artifacts\moment-debug.apk
```
