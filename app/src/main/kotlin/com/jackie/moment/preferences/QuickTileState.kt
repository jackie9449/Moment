package com.jackie.moment.preferences

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.service.quicksettings.TileService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.jackie.moment.QuickEditTileService

/** Addition/removal comes from SystemUI, not from whether the user tapped Add. */
class QuickTileState(context: Context) : AutoCloseable {
    private val appContext = context.applicationContext
    private val preferences = preferences(appContext)
    var added by mutableStateOf(preferences.getBoolean(KEY_ADDED, false))
        private set
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_ADDED || key == null) added = preferences.getBoolean(KEY_ADDED, false)
    }

    init { preferences.registerOnSharedPreferenceChangeListener(listener) }

    fun refresh() {
        added = preferences.getBoolean(KEY_ADDED, false)
        if (!added && preferences.contains(KEY_ADDED)) return
        // An existing tile can predate these preferences (e.g. an app upgrade).
        // SystemUI only binds an installed tile; onStartListening confirms it.
        try {
            TileService.requestListeningState(appContext, ComponentName(appContext, QuickEditTileService::class.java))
        } catch (_: RuntimeException) {
            // Keep the latest confirmed lifecycle state if SystemUI is unavailable.
        }
    }

    override fun close() { preferences.unregisterOnSharedPreferenceChangeListener(listener) }

    companion object {
        private const val KEY_ADDED = "added"
        private fun preferences(context: Context) = context.getSharedPreferences("quick_tile", Context.MODE_PRIVATE)
        fun record(context: Context, added: Boolean) {
            preferences(context.applicationContext).edit { putBoolean(KEY_ADDED, added) }
        }

        fun confirmListening(context: Context) {
            val preferences = preferences(context.applicationContext)
            // A late listening callback must not undo an explicit removal.
            if (!preferences.contains(KEY_ADDED)) record(context, true)
        }
    }
}
