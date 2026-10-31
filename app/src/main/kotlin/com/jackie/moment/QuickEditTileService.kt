package com.jackie.moment

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.jackie.moment.preferences.QuickTileState

class QuickEditTileService : TileService() {
    override fun onTileAdded() {
        super.onTileAdded()
        if (BuildConfig.DEBUG) android.util.Log.d("MomentTile", "onTileAdded")
        QuickTileState.record(this, true)
    }

    override fun onTileRemoved() {
        if (BuildConfig.DEBUG) android.util.Log.d("MomentTile", "onTileRemoved")
        QuickTileState.record(this, false)
        super.onTileRemoved()
    }

    override fun onStartListening() {
        super.onStartListening()
        if (BuildConfig.DEBUG) android.util.Log.d("MomentTile", "onStartListening; tile=${qsTile != null}")
        qsTile?.apply {
            QuickTileState.confirmListening(this@QuickEditTileService)
            state = Tile.STATE_INACTIVE
            label = getString(R.string.tile_label)
            subtitle = getString(R.string.app_name)
            contentDescription = getString(R.string.tile_label)
            icon = Icon.createWithResource(this@QuickEditTileService, R.drawable.ic_quick_edit)
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        if (isLocked) unlockAndRun(::launchEditor) else launchEditor()
    }

    // Intent is the only overload on API 29–33; the modern overload is used
    // exclusively on API 34+. Lint's target-SDK-only detector misses this guard.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun launchEditor() {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = MainActivity.ACTION_EDIT_CLIPBOARD
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
