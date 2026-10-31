package com.jackie.moment.testing

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View

/** Separate-task backdrop fixture, excluded from release. */
class ScreenBackdropActivity : Activity() {
    private var seeded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        seeded = savedInstanceState?.getBoolean("seeded") ?: false
        setContentView(object : View(this) {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                val colors = intArrayOf(Color.rgb(25, 175, 200), Color.rgb(238, 136, 60),
                    Color.rgb(155, 75, 196), Color.rgb(40, 170, 115))
                for (row in 0..1) for (column in 0..1) {
                    paint.color = colors[row * 2 + column]
                    canvas.drawRect(column * width / 2f, row * height / 2f,
                        (column + 1) * width / 2f, (row + 1) * height / 2f, paint)
                }
                paint.color = Color.WHITE
                paint.textSize = 28f * resources.displayMetrics.scaledDensity
                canvas.drawText("BEHIND THE GLASS", width * .12f, height * .25f, paint)
                canvas.drawText("REAL WINDOW", width * .2f, height * .75f, paint)
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("seeded", seeded)
        super.onSaveInstanceState(outState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) return
        val clipboard = getSystemService(ClipboardManager::class.java)
        if (!seeded) {
            clipboard.setPrimaryClip(ClipData.newPlainText("test", intent.getStringExtra("seed_text").orEmpty()))
            seeded = true
        }
        observedText = clipboard.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()
        observations++
    }

    companion object {
        @Volatile var observedText: String? = null
        @Volatile var observations = 0
    }
}
