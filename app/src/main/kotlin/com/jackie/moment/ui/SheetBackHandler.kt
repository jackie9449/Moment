package com.jackie.moment.ui

import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalView

/** Handle back on the modal window, rather than the Activity behind it. */
@Composable
internal fun SheetBackHandler(
    onStart: () -> Unit,
    onProgress: (Float) -> Unit,
    onCancel: () -> Unit,
    onCommit: () -> Unit,
) {
    val view = LocalView.current
    val start by rememberUpdatedState(onStart)
    val progress by rememberUpdatedState(onProgress)
    val cancel by rememberUpdatedState(onCancel)
    val commit by rememberUpdatedState(onCommit)
    DisposableEffect(view) {
        val owner = checkNotNull(view.findViewTreeOnBackPressedDispatcherOwner())
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackStarted(backEvent: BackEventCompat) = start()
            override fun handleOnBackProgressed(backEvent: BackEventCompat) = progress(backEvent.progress)
            override fun handleOnBackCancelled() = cancel()
            override fun handleOnBackPressed() = commit()
        }
        owner.onBackPressedDispatcher.addCallback(callback)
        onDispose { callback.remove() }
    }
}
