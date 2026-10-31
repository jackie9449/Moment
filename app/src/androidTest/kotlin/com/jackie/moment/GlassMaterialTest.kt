package com.jackie.moment

import android.graphics.Bitmap
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jackie.moment.ui.GlassMaterialProbeActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

/** Checks rendered output: curved grid displacement with a geometrically stable flat face. */
@RunWith(AndroidJUnit4::class)
class GlassMaterialTest {
    @get:Rule val compose = createAndroidComposeRule<GlassMaterialProbeActivity>()

    @Test fun bevelRefractsGridWhileFlatFaceKeepsGridAligned() {
        compose.waitForIdle()
        val bounds = compose.onNodeWithTag("opticalProbe").fetchSemanticsNode().boundsInRoot
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val density = compose.activity.resources.displayMetrics.density
        // Vertical grid profiles, away from horizontal lines and the foreground label.
        val image = instrumentation.uiAutomation.takeScreenshot()
        try {
            val file = File(compose.activity.getExternalFilesDir(null), "glass-optical-grid.png")
            file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
            val y = (bounds.top + 54*density).toInt()
            val referenceY = (270*density).toInt()
            fun luminance(x: Int, row: Int): Float {
                val color = image.getPixel(x, row)
                return (android.graphics.Color.red(color)*.2126f + android.graphics.Color.green(color)*.7152f +
                    android.graphics.Color.blue(color)*.0722f)
            }
            fun lineAt(expected: Float, row: Int): Int =
                ((expected-4*density).toInt()..(expected+4*density).toInt()).minBy { luminance(it,row) }
            val centerLine = 168*density
            val flatShift = abs(lineAt(centerLine,y)-lineAt(centerLine,referenceY))
            assertTrue("Flat face must not warp grid geometry: $flatShift px", flatShift <= 2)
            val edgeLine = 24*density
            val edgeShift = abs(lineAt(edgeLine,y)-lineAt(edgeLine,referenceY))
            assertTrue("Curved edge must visibly displace the transmitted grid: $edgeShift px", edgeShift >= 3)
        } finally { image.recycle() }
    }
}
