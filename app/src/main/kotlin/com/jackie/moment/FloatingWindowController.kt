package com.jackie.moment

import android.app.Activity
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toDrawable
import java.util.function.Consumer

/** Lets Android compose the previous window underneath us; never captures its pixels. */
class FloatingWindowController(private val activity: Activity) : AutoCloseable {
    private val window = activity.window
    private val manager = activity.windowManager
    private val decor = window.decorView
    private var dark = false
    private var frosted = false
    private var registered = false
    var blurAvailable by mutableStateOf(Build.VERSION.SDK_INT >= 31 && manager.isCrossWindowBlurEnabled)
        private set
    private val blurListener = Consumer<Boolean> {
        blurAvailable = it
        applyAppearance()
    }
    private val attachment = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) { register() }
        override fun onViewDetachedFromWindow(v: View) { unregister() }
    }

    init {
        window.setFormat(PixelFormat.TRANSLUCENT)
        window.setBackgroundDrawable(android.graphics.Color.TRANSPARENT.toDrawable())
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        decor.addOnAttachStateChangeListener(attachment)
        if (decor.isAttachedToWindow) register()
        applyAppearance()
    }

    fun updateAppearance(value: Boolean, frost: Boolean) {
        if (dark != value || frosted != frost) { dark = value; frosted = frost; applyAppearance() }
    }

    private fun register() {
        if (Build.VERSION.SDK_INT >= 31 && !registered) {
            manager.addCrossWindowBlurEnabledListener(activity.mainExecutor, blurListener)
            registered = true
        }
    }

    private fun unregister() {
        if (Build.VERSION.SDK_INT >= 31 && registered) {
            manager.removeCrossWindowBlurEnabledListener(blurListener)
            registered = false
        }
    }

    private fun applyAppearance() {
        if (BuildConfig.DEBUG) android.util.Log.d("MomentFloating", "blur=$blurAvailable dark=$dark")
        window.setDimAmount(if (blurAvailable) .035f else if (dark) .15f else .08f)
        if (Build.VERSION.SDK_INT >= 31) {
            if (blurAvailable) window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            else window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply {
                blurBehindRadius = if (blurAvailable) ((if (frosted) 20f else 6f) * activity.resources.displayMetrics.density).toInt() else 0
            }
        }
    }

    override fun close() {
        unregister()
        decor.removeOnAttachStateChangeListener(attachment)
    }
}
