package com.jackie.moment

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.jackie.moment.testing.ClipboardProbeActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorFlowTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val device get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private var activity: ActivityScenario<MainActivity>? = null

    @Before fun prepare() {
        device.executeShellCommand("cmd statusbar collapse")
        val style = InstrumentationRegistry.getArguments().getString("interface_style", "MATERIAL")
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit().clear().putString("style", style).commit()
    }
    @After fun close() {
        device.executeShellCommand("cmd statusbar collapse")
        activity?.close()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val monitor = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
            androidx.test.runner.lifecycle.Stage.entries.flatMap { monitor.getActivitiesInStage(it) }
                .filterIsInstance<MainActivity>().distinct().forEach { it.finishAndRemoveTask() }
        }
    }

    @Test fun editorBackCopiesExactDraftAndRemovesTask() {
        seedClipboard("original")
        openEditor("original")
        val draft = "  Back 返回 🙂\n\n "
        compose.onNodeWithTag("editor").performTextReplacement(draft)
        // First back can belong to the IME. The next closes the editor.
        device.pressBack()
        compose.waitForIdle()
        if (activity?.state != androidx.lifecycle.Lifecycle.State.DESTROYED) device.pressBack()
        await { activity?.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        assertEquals(draft, readClipboard())
    }

    @Test fun homeAfterEditingCopiesExactDraftAndRemovesTask() {
        seedClipboard("original")
        openEditor("original")
        val draft = "  Home ホーム 🙂\n\n "
        compose.onNodeWithTag("editor").performTextReplacement(draft)
        device.executeShellCommand("input swipe ${device.displayWidth / 2} ${device.displayHeight - 20} ${device.displayWidth / 2} ${device.displayHeight / 3} 300")
        SystemClock.sleep(1200)
        java.io.File(context.getExternalFilesDir(null), "home-gesture-state.txt").writeText(
            "package=${device.currentPackageName} state=${activity?.state}\n" +
                device.executeShellCommand("dumpsys activity activities"))
        await { activity?.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        assertEquals(draft, readClipboard())
    }

    @Test fun launchPasteEditCompleteCopiesExactTextAndFinishesTask() {
        val original = "  这是剪贴板的文字\n第二行 🙂  "
        seedClipboard(original)
        openEditor(original)
        compose.onNodeWithTag("editor").assertTextEquals(original)
        val edited = "  把灵感，留在片刻。\n\n今天想做的三件小事：\n· 读几页书\n· 给老朋友发一条消息\n· 出门走走，看看天色\n\n整理好了，就点右上角的「完成」。🙂\n "
        compose.onNodeWithTag("editor").performTextReplacement(edited)
        compose.onNodeWithTag("done").performClick()
        await { activity?.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        assertEquals(edited, readClipboard())
        val tasks = context.getSystemService(android.app.ActivityManager::class.java).appTasks
        assertFalse(tasks.any { it.taskInfo?.baseIntent?.component?.className == MainActivity::class.java.name })
    }

    @Test fun focusChangesThemeAndRecreationPreserveCurrentEdit() {
        seedClipboard("启动时的原文")
        openEditor("启动时的原文")
        val draft = "正在编辑的文字\n不会被后台剪贴板覆盖🙂"
        compose.onNodeWithTag("editor").performTextReplacement(draft)
        // A different foreground activity changes the clipboard. Returning must not re-import.
        seedClipboard("后来复制的内容")
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("mode_DARK").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithTag("palette_OCEAN").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        activity!!.recreate()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("mode_DARK").assertIsSelected()
        compose.onNodeWithTag("palette_OCEAN").assertIsSelected()
    }

    @Test fun liquidGlassPersistsAndStyleChangesPreserveDraftAndClipboard() {
        val original = "一段待整理的文字\n保留空白与 emoji 🙂"
        val draft = "  让想法清晰，让界面轻盈。\n\n玻璃之上，文字依然清楚。🙂\n "
        seedClipboard(original)
        openEditor(original)
        compose.onNodeWithTag("editor").performTextReplacement(draft)
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("style_LIQUID_GLASS").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        activity!!.recreate()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("style_LIQUID_GLASS").assertIsSelected()
        compose.onNodeWithTag("style_MATERIAL").performClick().assertIsSelected()
        compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("style_LIQUID_GLASS").performScrollTo().performClick()
        compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
        compose.onNodeWithTag("done").performClick()
        await { activity?.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        assertEquals(draft, readClipboard())
    }

    @Test fun frostedThemePersistsAndGlassSwitchingPreservesTextAndUndo() {
        val original = "原始文字\n保留空白 🙂"
        val draft = "  毛玻璃之上，文字清晰。\n\n雾面柔和，编辑安静。🙂\n "
        seedClipboard(original)
        openEditor(original)
        compose.onNodeWithTag("editor").performTextReplacement(draft)
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("style_FROSTED_GLASS").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithTag("mode_DARK").performScrollTo().performClick()
        compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
        activity!!.recreate()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("style_FROSTED_GLASS").assertIsSelected()
        compose.onNodeWithTag("mode_DARK").assertIsSelected()
        compose.onNodeWithTag("style_LIQUID_GLASS").performScrollTo().performClick()
        compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
        compose.onNodeWithTag("undo").performClick()
        compose.onNodeWithTag("editor").assertTextEquals(original)
        compose.onNodeWithTag("redo").performClick()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("style_FROSTED_GLASS").performScrollTo().performClick()
        compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
        compose.onNodeWithTag("done").performClick()
        await { activity?.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        assertEquals(draft, readClipboard())
    }

    @Test fun quickSettingsTileStartsFreshClipboardSession() {
        seedClipboard("第一次编辑")
        // The system tile can replace an Activity instance. Do not attach an
        // ActivityScenario owner to an Activity launched/relaunched by SystemUI.
        device.executeShellCommand("am start -W -n com.jackie.moment/.MainActivity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER")
        waitForEditor()
        waitForEditorText("第一次编辑")
        val component = "com.jackie.moment/.QuickEditTileService"
        device.executeShellCommand("cmd statusbar remove-tile $component")
        device.executeShellCommand("cmd statusbar add-tile $component")
        device.pressHome()
        // Keep this session unmodified to exercise fresh-intent replacement.
        // Edited-session Home completion is covered by its own native test.
        seedClipboard("快捷开关启动的新文本\nAndroid 17 🙂")
        device.openQuickSettings()
        SystemClock.sleep(1200)
        device.executeShellCommand("cmd statusbar click-tile $component")
        device.executeShellCommand("cmd statusbar collapse")
        waitForEditorText("快捷开关启动的新文本\nAndroid 17 🙂")
        compose.onNodeWithTag("editor").assertTextEquals("快捷开关启动的新文本\nAndroid 17 🙂")
        compose.onNodeWithTag("done").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("editor").fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
    }

    @Test fun tileCardTracksSystemAdditionRemovalAndExistingTileAfterUpgrade() {
        val component = "com.jackie.moment/.QuickEditTileService"
        val tilePreferences = context.getSharedPreferences("quick_tile", Context.MODE_PRIVATE)
        val text = "添加或移除快捷开关时，保留正在编辑的内容。🙂"
        // Establish a real SystemUI transition even if an earlier APK uninstall
        // left a cached tile service while its visible tile was already gone.
        device.executeShellCommand("cmd statusbar add-tile $component")
        device.waitForIdle()
        seedClipboard(text)
        openEditor(text)
        try {
            device.executeShellCommand("cmd statusbar remove-tile $component")
            compose.onNodeWithTag("appearance").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("quickTileCard").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("addTile").performScrollTo().assertIsDisplayed()

            device.executeShellCommand("cmd statusbar add-tile $component")
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("quickTileCard").fetchSemanticsNodes().isEmpty() }
            compose.onNodeWithTag("addTile").assertDoesNotExist()
            activity!!.recreate()
            compose.onNodeWithTag("quickTileCard").assertDoesNotExist()

            // Simulate upgrading from a version that never stored tile state.
            // The actual system tile stays installed throughout this step.
            tilePreferences.edit().clear().commit()
            activity!!.recreate()
            await { tilePreferences.getBoolean("added", false) }
            compose.onNodeWithTag("quickTileCard").assertDoesNotExist()

            device.executeShellCommand("cmd statusbar remove-tile $component")
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("quickTileCard").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("addTile").performScrollTo().assertIsDisplayed()
            activity!!.recreate()
            compose.onNodeWithTag("addTile").performScrollTo().assertIsDisplayed()
            compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
            compose.onNodeWithTag("editor").assertTextEquals(text)
        } finally {
            device.executeShellCommand("cmd statusbar remove-tile $component")
        }
    }

    @Test fun undoRedoAndEmptyCompletionWork() {
        seedClipboard("可撤销的原文")
        openEditor("可撤销的原文")
        compose.onNodeWithTag("editor").performTextReplacement("修改后的内容")
        compose.onNodeWithTag("undo").assertIsEnabled().performClick()
        waitForEditorText("可撤销的原文")
        compose.onNodeWithTag("editor").assertTextEquals("可撤销的原文")
        compose.onNodeWithTag("redo").performClick()
        compose.onNodeWithTag("editor").assertTextEquals("修改后的内容")
        compose.onNodeWithTag("editor").performTextClearance()
        compose.onNodeWithTag("done").performClick()
        await { activity?.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        assertEquals("", readClipboard())
    }

    @Test fun shortWindowAndLargeFontsKeepAnEditableDocument() {
        try {
            device.executeShellCommand("settings put system font_scale 1.5")
            device.executeShellCommand("wm size 1600x1100")
            val text = "大字体与宽屏下，也要留足编辑空间。\n这段文字不能消失。🙂"
            seedClipboard(text)
            openEditor(text)
            device.pressBack() // Hide the IME, keeping the editor task alive.
            compose.waitForIdle()
            compose.onNodeWithTag("editor").assertIsDisplayed().assertHeightIsAtLeast(64.dp).assertTextEquals(text)
            compose.onNodeWithTag("editor").performTextReplacement(text + "\n编辑成功")
            compose.onNodeWithTag("done").performClick()
            await { activity?.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
            assertEquals(text + "\n编辑成功", readClipboard())
        } finally {
            device.executeShellCommand("settings put system font_scale 1.0")
            device.executeShellCommand("wm size reset")
        }
    }

    private fun openEditor(expectedText: String) {
        activity = ActivityScenario.launch(Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        waitForEditor()
        waitForEditorText(expectedText)
    }

    private fun waitForEditor() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("editor").fetchSemanticsNodes().isNotEmpty() }
        compose.waitForIdle()
    }

    private fun waitForEditorText(expected: String) {
        // SystemUI launches asynchronously; an existing editor node can still
        // belong to the previous session while the launch intent is in flight.
        compose.waitUntil(10_000) {
            runCatching {
                compose.onAllNodesWithTag("editor").fetchSemanticsNodes().singleOrNull()
                    ?.config?.get(SemanticsProperties.EditableText)?.text == expected
            }.getOrDefault(false)
        }
    }

    private fun seedClipboard(text: String) { probe(text) }
    private fun readClipboard(): String? = probe(null)
    private fun probe(seed: String?): String? {
        val previous = ClipboardProbeActivity.observations
        val intent = Intent(context, ClipboardProbeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            if (seed != null) putExtra(ClipboardProbeActivity.EXTRA_SEED, seed)
        }
        ActivityScenario.launch<ClipboardProbeActivity>(intent).use {
            await { ClipboardProbeActivity.observations > previous }
        }
        return ClipboardProbeActivity.observedText
    }

    private fun await(condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (!condition() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(25)
        assertTrue("Timed out waiting for Android lifecycle / clipboard", condition())
    }
}
