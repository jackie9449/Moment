package com.jackie.moment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.util.TypedValue
import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.jackie.moment.testing.ScreenBackdropActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FloatingModeTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val device get() = UiDevice.getInstance(instrumentation)
    private var backdrop: ActivityScenario<ScreenBackdropActivity>? = null
    private var editor: ActivityScenario<MainActivity>? = null
    private val original = "让文字浮在眼前，\n让原来的界面留在身后。🙂"
    private val style get() = InstrumentationRegistry.getArguments().getString("floating_style", "LIQUID_GLASS")

    @Before fun prepare() {
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit().clear()
            .putString("style", style).putBoolean("floating_mode", true).commit()
    }

    @After fun close() {
        onCurrentEditor { it.finishAndRemoveTask() }
        editor?.close()
        backdrop?.close()
    }

    @Test fun previousWindowShowsThroughAndSwitchingPreservesDraftUndoAndReturn() {
        openAboveBackdrop()
        waitForTranslucent(true)
        device.pressBack() // Hide the IME.
        device.waitForIdle()
        assertBackgroundVisible()
        val draft = "  悬浮编辑，不打断思路。\n\n字句清晰，背景通透。🙂\n "
        compose.onNodeWithTag("editor").performTextReplacement(draft)
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("floatingMode").performScrollTo().assertIsOn().performClick()
        waitForTranslucent(false)
        waitForDocument(draft)
        compose.onNodeWithTag("floatingMode").performScrollTo().assertIsOff()
        closeSettings()
        compose.onNodeWithTag("undo").assertIsEnabled().performClick()
        waitForDocument(original)
        compose.onNodeWithTag("redo").performClick()
        waitForDocument(draft)

        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("floatingMode").performScrollTo().performClick()
        waitForTranslucent(true)
        waitForDocument(draft)
        val sameWindow = currentIdentity()
        val alternate = if (style == "FROSTED_GLASS") "LIQUID_GLASS" else "FROSTED_GLASS"
        compose.onNodeWithTag("style_$alternate").performScrollTo().performClick()
        waitForTranslucent(true)
        waitForDocument(draft)
        assertEquals("Changing glass material while floating must keep the window and draft", sameWindow, currentIdentity())
        assertBlurRadius(alternate)
        compose.onNodeWithTag("style_$style").performScrollTo().performClick()
        assertBlurRadius(style)
        waitForTranslucent(true)
        waitForDocument(draft)
        compose.onNodeWithTag("style_MATERIAL").performScrollTo().performClick()
        waitForTranslucent(false)
        waitForDocument(draft)
        compose.onNodeWithTag("floatingMode").assertDoesNotExist()
        compose.onNodeWithTag("style_$style").performScrollTo().performClick()
        waitForTranslucent(true)
        waitForDocument(draft)
        compose.onNodeWithTag("floatingMode").performScrollTo().assertIsOn()
        closeSettings()
        assertBackgroundVisible()

        val oldIdentity = currentIdentity()
        onCurrentEditor { it.recreate() }
        compose.waitUntil(10_000) { currentIdentity().let { it != 0 && it != oldIdentity } }
        waitForTranslucent(true)
        waitForDocument(draft)
        compose.onNodeWithTag("done").performClick()
        await { backdrop?.state == Lifecycle.State.RESUMED && ScreenBackdropActivity.observedText == draft }
    }

    @Test fun blurDisabledStillShowsPreviousWindowAndAllowsEditing() {
        val originalBlurSetting = device.executeShellCommand("settings get global disable_window_blurs").trim()
        try {
            device.executeShellCommand("settings put global disable_window_blurs 1")
            openAboveBackdrop()
            waitForTranslucent(true)
            compose.waitUntil(10_000) {
                var disabled = false
                onCurrentEditor { disabled = it.window.attributes.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND == 0 }
                disabled
            }
            device.pressBack()
            device.waitForIdle()
            assertBackgroundVisible()
            val draft = "模糊不可用时，依然能够清晰编辑。🙂"
            compose.onNodeWithTag("editor").performTextReplacement(draft)
            compose.onNodeWithTag("done").performClick()
            await { backdrop?.state == Lifecycle.State.RESUMED && ScreenBackdropActivity.observedText == draft }
        } finally {
            if (originalBlurSetting == "null") device.executeShellCommand("settings delete global disable_window_blurs")
            else device.executeShellCommand("settings put global disable_window_blurs ${if (originalBlurSetting == "1") 1 else 0}")
        }
    }

    private fun openAboveBackdrop() {
        val previous = ScreenBackdropActivity.observations
        backdrop = ActivityScenario.launch(Intent(context, ScreenBackdropActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("seed_text", original)
        })
        await { ScreenBackdropActivity.observations > previous }
        editor = ActivityScenario.launch(Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        waitForDocument(original)
    }

    private fun waitForDocument(text: String) {
        compose.waitUntil(10_000) {
            runCatching { compose.onAllNodesWithTag("editor").fetchSemanticsNodes().singleOrNull()
                ?.config?.get(SemanticsProperties.EditableText)?.text == text }.getOrDefault(false)
        }
        compose.waitForIdle()
    }

    private fun closeSettings() {
        compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("closeAppearance").fetchSemanticsNodes().isEmpty() }
        assertTrue(device.wait(Until.gone(By.desc("关闭外观设置")), 10_000))
    }

    private fun onCurrentEditor(action: (MainActivity) -> Unit) {
        instrumentation.runOnMainSync {
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<MainActivity>().firstOrNull()?.let(action)
        }
    }

    private fun currentIdentity(): Int {
        var identity = 0
        onCurrentEditor { identity = System.identityHashCode(it) }
        return identity
    }

    private fun waitForTranslucent(expected: Boolean) {
        compose.waitUntil(10_000) {
            var actual: Boolean? = null
            onCurrentEditor {
                val attribute = TypedValue()
                it.theme.resolveAttribute(android.R.attr.windowIsTranslucent, attribute, true)
                actual = attribute.data != 0
            }
            actual == expected
        }
        compose.waitForIdle()
    }

    private fun assertBlurRadius(style: String) {
        if (android.os.Build.VERSION.SDK_INT < 31) return
        compose.waitUntil(10_000) {
            var matched = false
            onCurrentEditor {
                val manager = it.getSystemService(WindowManager::class.java)
                val expected = if (manager.isCrossWindowBlurEnabled)
                    ((if (style == "FROSTED_GLASS") 20 else 6)*it.resources.displayMetrics.density).toInt() else 0
                matched = it.window.attributes.blurBehindRadius == expected
            }
            matched
        }
    }

    private fun assertBackgroundVisible() {
        device.waitForIdle()
        val deadline = SystemClock.uptimeMillis() + 5_000
        var distance: Int
        do {
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            assertNotNull(bitmap)
            val first = bitmap.getPixel((bitmap.width * .02).toInt(), (bitmap.height * .36).toInt())
            val second = bitmap.getPixel((bitmap.width * .98).toInt(), (bitmap.height * .36).toInt())
            distance = kotlin.math.abs(Color.red(first)-Color.red(second)) +
                kotlin.math.abs(Color.green(first)-Color.green(second)) + kotlin.math.abs(Color.blue(first)-Color.blue(second))
            if (distance > 100 || SystemClock.uptimeMillis() >= deadline) {
                java.io.File(context.getExternalFilesDir(null), "floating-background.png").outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
            }
            bitmap.recycle()
            if (distance > 100) return
            SystemClock.sleep(50) // Wait for the actual compositor frame, not just Compose semantics.
        } while (SystemClock.uptimeMillis() < deadline)
        fail("The real colored window must be visible through editor margins; RGB distance=$distance")
    }

    private fun await(condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (!condition() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(25)
        assertTrue("Timed out waiting for background focus / clipboard", condition())
    }
}
