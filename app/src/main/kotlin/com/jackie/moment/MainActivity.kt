package com.jackie.moment

import android.app.StatusBarManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.jackie.moment.editor.EditorSessionModel
import com.jackie.moment.preferences.AppearanceStore
import com.jackie.moment.preferences.InterfaceStyle
import com.jackie.moment.preferences.QuickTileState
import com.jackie.moment.ui.ClipboardSession
import com.jackie.moment.ui.EditorScreen
import com.jackie.moment.ui.MomentTheme

class MainActivity : ComponentActivity() {
    private var pendingClipboardImport = false
    private var clipboardSession by mutableStateOf<ClipboardSession?>(null)
    private var openAppearance by mutableStateOf(false)
    private lateinit var appearance: AppearanceStore
    private lateinit var quickTileState: QuickTileState
    private var floatingAtCreation = false
    private var floatingWindow: FloatingWindowController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        appearance = AppearanceStore(this)
        floatingAtCreation = appearance.useFloatingWindow
        setTheme(if (floatingAtCreation) R.style.Theme_Moment_Floating else R.style.Theme_Moment)
        super.onCreate(savedInstanceState)
        // The theme styles our window; this public API also updates the
        // system-server occlusion state so the previous task stays visible.
        if (Build.VERSION.SDK_INT >= 30) setTranslucent(floatingAtCreation)
        quickTileState = QuickTileState(this)
        val editorSession = ViewModelProvider(this)[EditorSessionModel::class.java]
        pendingClipboardImport = savedInstanceState == null
        openAppearance = intent.action == TileService.ACTION_QS_TILE_PREFERENCES
        enableEdgeToEdge()
        if (floatingAtCreation) floatingWindow = FloatingWindowController(this)
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.moment_enter, R.anim.moment_stay)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.moment_stay, R.anim.moment_exit)
        }
        setContent {
            LaunchedEffect(appearance.useFloatingWindow) {
                if (appearance.useFloatingWindow != floatingAtCreation) recreate()
            }
            MomentTheme(appearance, floatingAtCreation, floatingWindow?.blurAvailable == true) { dark ->
                SideEffect {
                    floatingWindow?.updateAppearance(dark, appearance.style == InterfaceStyle.FROSTED_GLASS)
                    val transparent = android.graphics.Color.TRANSPARENT
                    val barStyle = if (dark) SystemBarStyle.dark(transparent)
                        else SystemBarStyle.light(transparent, transparent)
                    enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                }
                EditorScreen(
                    appearance = appearance,
                    editorSession = editorSession,
                    tileAdded = quickTileState.added,
                    session = clipboardSession,
                    openAppearance = openAppearance,
                    onAppearanceConsumed = { openAppearance = false },
                    onAddTile = ::requestQuickTile,
                    onFinish = ::finishEditing,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == TileService.ACTION_QS_TILE_PREFERENCES) {
            openAppearance = true
        } else {
            pendingClipboardImport = true
            window.decorView.post(::importClipboardWhenFocused)
        }
    }

    override fun onResume() {
        super.onResume()
        quickTileState.refresh()
        window.decorView.post(::importClipboardWhenFocused)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && ::quickTileState.isInitialized) quickTileState.refresh()
        if (hasFocus && pendingClipboardImport) {
            window.decorView.post(::importClipboardWhenFocused)
        }
    }

    override fun onDestroy() {
        floatingWindow?.close()
        if (::quickTileState.isInitialized) quickTileState.close()
        super.onDestroy()
    }

    private fun importClipboardWhenFocused() {
        if (!pendingClipboardImport || !hasWindowFocus() ||
            !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        pendingClipboardImport = false
        try {
            val clipboard = getSystemService(ClipboardManager::class.java)
            val clip = clipboard.primaryClip
            // Text only. Never dereference a copied content URI or execute a copied Intent.
            val text = if (clip != null && clip.itemCount > 0) clip.getItemAt(0).text?.toString() else null
            clipboardSession = ClipboardSession(
                id = android.os.SystemClock.elapsedRealtimeNanos(),
                text = text.orEmpty(),
                hasText = !text.isNullOrEmpty(),
            )
        } catch (_: RuntimeException) {
            showMessage(R.string.clipboard_failed)
        }
    }

    private fun finishEditing(text: String): Boolean = try {
        getSystemService(ClipboardManager::class.java).setPrimaryClip(
            ClipData.newPlainText(getString(R.string.app_name), text)
        )
        if (Build.VERSION.SDK_INT < 33) showMessage(R.string.copied)
        finishAndRemoveTask()
        if (Build.VERSION.SDK_INT < 34) {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.moment_stay, R.anim.moment_exit)
        }
        true
    } catch (_: RuntimeException) {
        showMessage(R.string.copy_failed)
        false
    }

    private fun requestQuickTile() {
        if (Build.VERSION.SDK_INT < 33) {
            showMessage(R.string.tile_manual)
            return
        }
        try {
            getSystemService(StatusBarManager::class.java).requestAddTileService(
                ComponentName(this, QuickEditTileService::class.java),
                getString(R.string.tile_label),
                Icon.createWithResource(this, R.drawable.ic_quick_edit),
                mainExecutor,
            ) { result ->
                if (result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED ||
                    result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED) {
                    QuickTileState.record(this, true)
                }
                showMessage(when (result) {
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> R.string.tile_added
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> R.string.tile_already_added
                    else -> R.string.tile_not_added
                })
            }
        } catch (_: RuntimeException) {
            showMessage(R.string.tile_manual)
        }
    }

    private fun showMessage(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val ACTION_EDIT_CLIPBOARD = "com.jackie.moment.EDIT_CLIPBOARD"
    }
}
