package com.jackie.moment

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.jackie.moment.testing.ClipboardProbeActivity
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CaretVisibilityTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val device get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private var activity: ActivityScenario<MainActivity>? = null
    private val text = (1..60).joinToString("\n") { "第${it.toString().padStart(2, '0')}行：长文本编辑测试。" }

    @Before fun setAppearance() {
        val style = InstrumentationRegistry.getArguments().getString("interface_style", "MATERIAL")
        val floating = InstrumentationRegistry.getArguments().getString("floating_mode", "false").toBoolean()
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit().clear().putString("style", style)
            .putBoolean("floating_mode", floating).commit()
    }

    @After fun close() { activity?.close() }

    @Test fun keyboardChangesOnlyTheImeSpaceWithoutMovingEditorTop() {
        openLongDocument()
        hideKeyboard()
        val before = compose.onNodeWithTag("editor").fetchSemanticsNode().boundsInRoot
        var navigationBottom = 0
        activity!!.onActivity {
            navigationBottom = WindowInsetsCompat.toWindowInsetsCompat(it.window.decorView.rootWindowInsets)
                .getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        }
        showKeyboard()
        val after = compose.onNodeWithTag("editor").fetchSemanticsNode().boundsInRoot
        var imeBottom = 0
        activity!!.onActivity {
            imeBottom = WindowInsetsCompat.toWindowInsetsCompat(it.window.decorView.rootWindowInsets)
                .getInsets(WindowInsetsCompat.Type.ime()).bottom
        }
        assertEquals("Keyboard must not switch decorations or move the editor's top", before.top, after.top, 1f)
        assertEquals("Only IME space should reduce the editor viewport; no footer removal jump",
            (imeBottom - navigationBottom).toFloat(), before.bottom - after.bottom, 2f)
    }

    @Test fun reopeningKeyboardKeepsEndCaretVisible() {
        openLongDocument()
        hideKeyboard()
        placeCaretNearViewportBottom(text.length)
        showKeyboard()
        assertCaretVisible(text.length)
    }

    @Test fun reopeningKeyboardKeepsMiddleCaretVisibleAndTypingInPlace() {
        openLongDocument()
        hideKeyboard()
        val offset = text.indexOf("\n第36行")
        placeCaretNearViewportBottom(offset)
        showKeyboard()
        assertCaretVisible(offset)
        compose.onNodeWithTag("editor").performTextInput("\n继续编辑🙂")
        compose.waitForIdle()
        assertCaretVisible(offset + "\n继续编辑🙂".length)
        val before = compose.onNodeWithTag("editor").fetchSemanticsNode()
            .config[SemanticsProperties.VerticalScrollAxisRange].value()
        compose.onNodeWithTag("editor").performSemanticsAction(SemanticsActions.ScrollBy) {
            assertTrue(it(0f, -300f))
        }
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        val after = compose.onNodeWithTag("editor").fetchSemanticsNode()
            .config[SemanticsProperties.VerticalScrollAxisRange].value()
        assertTrue("Manual browsing must not snap back to the caret", after < before - 200f)
    }

    private fun openLongDocument() {
        val before = ClipboardProbeActivity.observations
        ActivityScenario.launch<ClipboardProbeActivity>(Intent(context, ClipboardProbeActivity::class.java).apply {
            putExtra(ClipboardProbeActivity.EXTRA_SEED, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }).use {
            val deadline = SystemClock.uptimeMillis() + 10_000
            while (ClipboardProbeActivity.observations <= before && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(25)
            assertTrue(ClipboardProbeActivity.observations > before)
        }
        activity = ActivityScenario.launch(Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        compose.waitForIdle()
        awaitKeyboard(true)
    }

    private fun hideKeyboard() {
        activity!!.onActivity {
            WindowCompat.getInsetsController(it.window, it.window.decorView).hide(WindowInsetsCompat.Type.ime())
        }
        awaitKeyboard(false)
    }

    private fun showKeyboard() {
        activity!!.onActivity {
            WindowCompat.getInsetsController(it.window, it.window.decorView).show(WindowInsetsCompat.Type.ime())
        }
        awaitKeyboard(true)
    }

    private fun awaitKeyboard(shown: Boolean) {
        var lastGeometry: Triple<Int, Int, Int>? = null
        var stableSince = SystemClock.uptimeMillis()
        compose.waitUntil(10_000) {
            var visible = false
            var bottom = 0
            activity!!.onActivity {
                val insets = WindowInsetsCompat.toWindowInsetsCompat(it.window.decorView.rootWindowInsets)
                visible = insets.isVisible(WindowInsetsCompat.Type.ime())
                bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            }
            val bounds = compose.onNodeWithTag("editor").fetchSemanticsNode().boundsInRoot
            val geometry = Triple(bottom, bounds.top.toInt(), bounds.bottom.toInt())
            val now = SystemClock.uptimeMillis()
            if (visible != shown || geometry != lastGeometry) stableSince = now
            lastGeometry = geometry
            // Visibility changes at the start of a native IME animation. Wait for
            // both the final insets and the actual Compose viewport to settle.
            visible == shown && now - stableSince >= 300
        }
        device.waitForIdle()
        compose.waitForIdle()
    }

    private fun placeCaretNearViewportBottom(offset: Int) {
        val previous = compose.onNodeWithTag("editor").fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]
        if (previous.start != offset || previous.end != offset) {
            compose.onNodeWithTag("editor").performSemanticsAction(SemanticsActions.SetSelection) {
                assertTrue(it(offset, offset, false))
            }
        }
        compose.waitForIdle()
        val result = layout()
        val node = compose.onNodeWithTag("editor").fetchSemanticsNode()
        val scroll = node.config[SemanticsProperties.VerticalScrollAxisRange]
        val margin = 16f
        val target = (result.getCursorRect(offset).bottom - node.boundsInRoot.height + margin)
            .coerceIn(0f, scroll.maxValue())
        val delta = target - scroll.value()
        if (kotlin.math.abs(delta) > 1f) {
            compose.onNodeWithTag("editor").performSemanticsAction(SemanticsActions.ScrollBy) {
                assertTrue(it(0f, delta))
            }
        }
        compose.waitForIdle()
        assertCaretVisible(offset)
    }

    private fun assertCaretVisible(offset: Int) {
        val result = layout()
        val node = compose.onNodeWithTag("editor").fetchSemanticsNode()
        val selection = node.config[SemanticsProperties.TextSelectionRange]
        assertEquals("Keyboard must not move the insertion point", offset, selection.end)
        val scroll = node.config[SemanticsProperties.VerticalScrollAxisRange].value()
        val cursor = result.getCursorRect(offset)
        val top = cursor.top - scroll
        val bottom = cursor.bottom - scroll
        assertTrue("Caret top $top is outside editor viewport", top >= -1f)
        assertTrue("Caret bottom $bottom is below viewport ${node.boundsInRoot.height}; scroll=$scroll",
            bottom <= node.boundsInRoot.height + 1f)
    }

    private fun layout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag("editor").performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
            assertTrue(it(results))
        }
        return results.single()
    }
}
