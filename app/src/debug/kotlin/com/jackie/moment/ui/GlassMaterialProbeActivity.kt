package com.jackie.moment.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.jackie.moment.preferences.Palette

/** Diagnostic fixture only; never packaged in release. The grid and lens share one scene. */
class GlassMaterialProbeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val frosted = intent.getBooleanExtra("frosted", false)
        setContent {
            val scene = remember { GlassScene(diagnostic = true) }
            MaterialTheme(colorScheme = paletteScheme(Palette.IRIS, false)) {
                CompositionLocalProvider(LocalGlassAppearance provides GlassAppearance(enabled = true, frosted = frosted),
                    LocalGlassScene provides scene) {
                    Box(Modifier.fillMaxSize()) {
                        if (frosted) FrostedBackdrop(Modifier.fillMaxSize()) else GlassBackdrop(Modifier.fillMaxSize())
                        GlassControlSurface(Modifier.offset(x = 18.dp, y = 300.dp)
                            .width(312.dp).height(144.dp).testTag("opticalProbe"), radius = 40.dp) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("清晰文字 · Glass", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}
