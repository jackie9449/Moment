package com.jackie.moment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.inspector.WindowInspector
import androidx.activity.BackEventCompat
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.jackie.moment.testing.ScreenBackdropActivity
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

/** Native compositor checks while the finger is held, not just after a swipe finishes. */
@RunWith(AndroidJUnit4::class)
class SheetDismissTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val device get() = UiDevice.getInstance(instrumentation)
    private val style get() = InstrumentationRegistry.getArguments().getString("interface_style", "MATERIAL")
    private val floating get() = InstrumentationRegistry.getArguments().getString("floating_mode", "false")
    private var backdrop: ActivityScenario<ScreenBackdropActivity>? = null
    private var editor: ActivityScenario<MainActivity>? = null
    private val original = "启动原文\n保持剪贴板会话。"
    private val draft = "  调色盘随手势收起，\n文字与撤销记录保留。🙂\n "
    private var downTime = 0L
    private var fingerDown = false
    private var fingerX = 0f
    private var fingerY = 0f

    @Before fun prepare() {
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit().clear()
            .putString("style", style).putString("mode", "LIGHT")
            .putBoolean("floating_mode", floating.toBoolean()).commit()
        val observed = ScreenBackdropActivity.observations
        backdrop = ActivityScenario.launch(Intent(context, ScreenBackdropActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("seed_text", original))
        compose.waitUntil(10_000) { ScreenBackdropActivity.observations > observed }
        editor = ActivityScenario.launch(Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        compose.onNodeWithTag("editor").assertTextEquals(original).performTextReplacement(draft)
    }

    @After fun close() {
        if (fingerDown) touch(MotionEvent.ACTION_CANCEL, fingerX, fingerY)
        editor?.close()
        backdrop?.close()
    }

    @Test fun backgroundTracksHeldDragReversalAndCompletedDismiss() {
        openSheet()
        val bounds = settledSheet()
        assertTrue("Need an exposed background region", bounds.top > 160f)
        val screen = instrumentation.uiAutomation.takeScreenshot()
        val x = screen.width / 2
        val y = (bounds.top / 2).toInt()
        val screenHeight = screen.height.toFloat()
        screen.recycle()
        val samples = linkedMapOf<String, Float>()
        fun sample(phase: String) = brightness(x, y, phase).also { samples[phase] = it }
        val open = sample("open")
        val startY = bounds.top + 22 * context.resources.displayMetrics.density
        val travel = screenHeight - bounds.top
        File(context.getExternalFilesDir(null), "sheet-geometry-debug.txt")
            .writeText("bounds=$bounds startY=$startY travel=$travel screenHeight=$screenHeight")

        touch(MotionEvent.ACTION_DOWN, x.toFloat(), startY)
        touch(MotionEvent.ACTION_MOVE, x.toFloat(), startY + travel * .30f)
        val early = sample("held-30")
        touch(MotionEvent.ACTION_MOVE, x.toFloat(), startY + travel * .58f)
        val middle = sample("held-58")
        assertTrue("Background must brighten before releasing the finger: $open -> $early", early > open + 4)
        assertTrue("Further drag must continue clearing: $early -> $middle", middle > early + 4)
        touch(MotionEvent.ACTION_MOVE, x.toFloat(), startY + travel * .18f)
        val reversed = sample("reversed-18")
        assertTrue("Reversing the finger must restore dimming immediately", reversed < middle - 6)
        touch(MotionEvent.ACTION_MOVE, x.toFloat(), startY)
        touch(MotionEvent.ACTION_UP, x.toFloat(), startY)
        settledSheet()
        val restored = sample("restored")
        assertEquals("Returning to Expanded must restore its backdrop", open, restored, 5f)

        touch(MotionEvent.ACTION_DOWN, x.toFloat(), startY)
        touch(MotionEvent.ACTION_MOVE, x.toFloat(), startY + travel * .22f)
        val first = sample("dismiss-22")
        touch(MotionEvent.ACTION_MOVE, x.toFloat(), startY + travel * .52f)
        val second = sample("dismiss-52")
        touch(MotionEvent.ACTION_MOVE, x.toFloat(), startY + travel * .86f)
        val last = sample("dismiss-86")
        assertTrue("Brightness must progress with drag distance", first > open + 3 && second > first + 4 && last > second + 4)
        touch(MotionEvent.ACTION_UP, x.toFloat(), startY + travel * .86f)
        awaitClosed()
        val closed = sample("closed")
        assertTrue("Most clearing must happen before dismissal", abs(closed-last) < abs(closed-open)*.30f + 3f)
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        compose.onNodeWithTag("undo").performClick()
        compose.onNodeWithTag("editor").assertTextEquals(original)
        compose.onNodeWithTag("redo").performClick()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        File(context.getExternalFilesDir(null), "sheet-$style-$floating-samples.json")
            .writeText(JSONObject(samples.toMap()).toString(2))
    }

    @Test fun closeButtonOutsideTapAndBackAllPreserveDraft() {
        openSheet()
        settledSheet()
        compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
        awaitClosed()
        openSheet()
        val bounds = settledSheet()
        device.click((bounds.width/2).toInt(), (bounds.top/2).toInt())
        awaitClosed()
        openSheet()
        settledSheet()
        device.pressBack()
        awaitClosed()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
    }

    @Test fun closeButtonKeepsSheetAliveThroughAnEasedExit() {
        openSheet()
        val expanded = settledSheet()
        compose.mainClock.autoAdvance = false
        try {
            compose.onNodeWithTag("closeAppearance").performClick()
            compose.mainClock.advanceTimeBy(96)
            compose.waitForIdle()
            val early = compose.onNodeWithTag("appearanceDragHandle", useUnmergedTree = true)
                .fetchSemanticsNode().boundsInRoot.top
            assertTrue("Close must move down during its first 96ms", early > expanded.top + 8)
            compose.onNodeWithTag("appearanceSheet").assertExists()
            compose.mainClock.advanceTimeBy(160)
            compose.waitForIdle()
            val later = compose.onNodeWithTag("appearanceDragHandle", useUnmergedTree = true)
                .fetchSemanticsNode().boundsInRoot.top
            assertTrue("Exit must continue smoothly beyond the initial frames", later > early + 8)
            compose.onNodeWithTag("appearanceSheet").assertExists()
            compose.mainClock.advanceTimeBy(240)
            compose.waitForIdle()
            compose.onNodeWithTag("appearanceSheet").assertDoesNotExist()
        } finally {
            compose.mainClock.autoAdvance = true
        }
        compose.onNodeWithTag("editor").assertTextEquals(draft)
    }

    @Test fun predictiveBackPreviewsCancelsAndSettlesGently() {
        openSheet()
        val expanded = settledSheet()
        val dispatcher = dialogBackDispatcher()
        fun event(progress: Float, edge: Int = BackEventCompat.EDGE_LEFT) = BackEventCompat(0f, 1000f, progress, edge)
        fun handleTop() = compose.onNodeWithTag("appearanceDragHandle", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot.top
        compose.mainClock.autoAdvance = false
        try {
            compose.runOnIdle {
                dispatcher.dispatchOnBackStarted(event(0f))
                dispatcher.dispatchOnBackProgressed(event(.35f))
            }
            compose.mainClock.advanceTimeByFrame()
            compose.waitForIdle()
            val first = handleTop()
            assertTrue("Back preview must follow the gesture", first > expanded.top + 16)
            compose.runOnIdle { dispatcher.dispatchOnBackProgressed(event(.8f)) }
            compose.mainClock.advanceTimeByFrame()
            compose.waitForIdle()
            val further = handleTop()
            assertTrue("Further progress must reveal more of the editor", further > first + 16)
            compose.runOnIdle { dispatcher.dispatchOnBackCancelled() }
            compose.mainClock.advanceTimeBy(320)
            compose.waitForIdle()
            assertEquals("Cancelled back must return to its original position", expanded.top, handleTop(), 3f)
            compose.onNodeWithTag("appearanceSheet").assertExists()

            compose.runOnIdle {
                dispatcher.dispatchOnBackStarted(event(0f, BackEventCompat.EDGE_RIGHT))
                dispatcher.dispatchOnBackProgressed(event(.65f, BackEventCompat.EDGE_RIGHT))
            }
            compose.mainClock.advanceTimeByFrame()
            compose.waitForIdle()
            val preview = handleTop()
            compose.runOnIdle { dispatcher.onBackPressed() }
            compose.mainClock.advanceTimeBy(96)
            compose.waitForIdle()
            assertTrue("Committed back must continue from its preview", handleTop() > preview + 8)
            compose.onNodeWithTag("appearanceSheet").assertExists()
            compose.mainClock.advanceTimeBy(160)
            compose.waitForIdle()
            compose.onNodeWithTag("appearanceSheet").assertExists()
            compose.mainClock.advanceTimeBy(240)
            compose.waitForIdle()
            compose.onNodeWithTag("appearanceSheet").assertDoesNotExist()
        } finally { compose.mainClock.autoAdvance = true }
        compose.onNodeWithTag("editor").assertTextEquals(draft)
        compose.onNodeWithTag("undo").performClick()
        compose.onNodeWithTag("editor").assertTextEquals(original)
        compose.onNodeWithTag("redo").performClick()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
    }

    @Test fun nativeEdgeBackClosesOnlyPersonalization() {
        openSheet()
        settledSheet()
        assertTrue(device.swipe(1, device.displayHeight / 2, device.displayWidth / 2, device.displayHeight / 2, 45))
        awaitClosed()
        compose.onNodeWithTag("editor").assertTextEquals(draft)
    }

    private fun dialogBackDispatcher(): OnBackPressedDispatcher {
        lateinit var dispatcher: OnBackPressedDispatcher
        instrumentation.runOnMainSync {
            val dialog = WindowInspector.getGlobalWindowViews()
                .mapNotNull { it.findViewTreeOnBackPressedDispatcherOwner() }
                .filterIsInstance<ComponentDialog>().single()
            dispatcher = dialog.onBackPressedDispatcher
        }
        return dispatcher
    }

    private fun openSheet() { compose.onNodeWithTag("appearance").performClick() }

    private fun settledSheet(): Rect {
        var last: Rect? = null
        var stableSince = SystemClock.uptimeMillis()
        compose.waitUntil(10_000) {
            // The sheet's public modifier wraps Material3's internal offset. Its
            // handle is inside that offset, so use the actual placed handle top.
            val surface = compose.onNodeWithTag("appearanceSheet").fetchSemanticsNode().boundsInRoot
            val handle = compose.onNodeWithTag("appearanceDragHandle", useUnmergedTree = true)
                .fetchSemanticsNode().boundsInRoot
            val bounds = Rect(surface.left, handle.top, surface.right, handle.top + surface.height)
            val now = SystemClock.uptimeMillis()
            if (bounds != last) stableSince = now
            last = bounds
            bounds.height > 0 && now-stableSince >= 300
        }
        return last!!
    }

    private fun awaitClosed() {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("appearanceSheet").fetchSemanticsNodes().isEmpty() }
        compose.waitForIdle()
    }

    private fun touch(action: Int, x: Float, y: Float) {
        if (action == MotionEvent.ACTION_DOWN) { downTime = SystemClock.uptimeMillis(); fingerDown = true }
        val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)
        event.source = InputDevice.SOURCE_TOUCHSCREEN
        try { assertTrue(instrumentation.uiAutomation.injectInputEvent(event, true)) } finally { event.recycle() }
        fingerX = x; fingerY = y
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) fingerDown = false
        SystemClock.sleep(90)
        instrumentation.waitForIdleSync()
        compose.waitForIdle()
    }

    private fun brightness(x: Int, y: Int, phase: String): Float {
        // Compose's test clock advances independently of SurfaceFlinger. Allow
        // the editor window's invalidated draw to be presented before sampling.
        instrumentation.waitForIdleSync()
        SystemClock.sleep(60)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        try {
            File(context.getExternalFilesDir(null), "sheet-$style-$floating-$phase.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            var sum = 0f
            for (dy in -4..4) for (dx in -4..4) {
                val c = bitmap.getPixel(x+dx,y+dy)
                sum += Color.red(c)*.2126f+Color.green(c)*.7152f+Color.blue(c)*.0722f
            }
            return sum/81f
        } finally { bitmap.recycle() }
    }
}
