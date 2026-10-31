package com.jackie.moment.testing

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.TextView

/** Foreground clipboard fixture. This source set is excluded from production releases. */
class ClipboardProbeActivity : Activity() {
    private var handled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { setText(com.jackie.moment.R.string.app_name) })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus || handled) return
        handled = true
        val manager = getSystemService(ClipboardManager::class.java)
        if (intent.hasExtra(EXTRA_SEED)) {
            manager.setPrimaryClip(ClipData.newPlainText("test", intent.getStringExtra(EXTRA_SEED).orEmpty()))
        }
        observedText = manager.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()
        observations++
    }

    companion object {
        const val EXTRA_SEED = "seed_text"
        @Volatile var observedText: String? = null
        @Volatile var observations = 0
    }
}
