package com.jackie.moment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jackie.moment.ui.GlassMaterialProbeActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class FrostedMaterialTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun backgroundLosesFineDetailWhileForegroundStaysSharp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        ActivityScenario.launch<GlassMaterialProbeActivity>(Intent(context,GlassMaterialProbeActivity::class.java)
            .putExtra("frosted",true)).use {
            compose.waitForIdle()
            val bounds = compose.onNodeWithTag("opticalProbe").fetchSemanticsNode().boundsInRoot
            val density = context.resources.displayMetrics.density
            val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            try {
                File(context.getExternalFilesDir(null),"frosted-optical-grid.png").outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,it)
                }
                fun luma(x:Int,y:Int):Float {
                    val c=bitmap.getPixel(x,y)
                    return Color.red(c)*.2126f+Color.green(c)*.7152f+Color.blue(c)*.0722f
                }
                fun contrast(row:Int):Float {
                    val samples=((90*density).toInt()..(246*density).toInt()).map { luma(it,row) }
                    return samples.max()-samples.min()
                }
                val reference=contrast((270*density).toInt())
                val frosted=contrast((bounds.top+54*density).toInt())
                assertTrue("Native blur must remove grid detail beyond translucent tint alone: $frosted / $reference",
                    reference>100 && frosted<reference*.12f)
                val xRange=((bounds.center.x-62*density).toInt()..(bounds.center.x+62*density).toInt())
                val yRange=((bounds.center.y-10*density).toInt()..(bounds.center.y+10*density).toInt())
                var darkPixels=0
                var sharpEdge=0f
                for (y in yRange) for (x in xRange) {
                    if (luma(x,y)<60f) darkPixels++
                    sharpEdge=maxOf(sharpEdge,abs(luma(x+1,y)-luma(x,y)))
                }
                assertTrue("Foreground label must retain dark ink",darkPixels>30)
                assertTrue("Foreground must not be blurred with its backdrop: $sharpEdge",sharpEdge>70f)
            } finally { bitmap.recycle() }
        }
    }
}
