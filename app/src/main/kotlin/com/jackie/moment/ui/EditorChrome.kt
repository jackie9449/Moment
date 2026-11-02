package com.jackie.moment.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jackie.moment.R
import com.jackie.moment.editor.EditorState
import com.jackie.moment.editor.TextMetrics
import com.jackie.moment.preferences.AppearanceStore

@Composable
internal fun EditorSheetHeader(importedText: Boolean, edited: Boolean, metrics: TextMetrics,
    compact: Boolean, minimal: Boolean, completing: Boolean, onDone: () -> Unit) {
    val glass = LocalGlassAppearance.current
    val metadataColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (glass.floating && !glass.frosted) .84f else .76f)
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = if (compact) 10.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(stringResource(if (edited) R.string.editing else if (importedText) R.string.clipboard_loaded else R.string.clipboard_empty),
                style = MaterialTheme.typography.labelMedium, color = metadataColor,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!minimal) Text(stringResource(R.string.text_counts, metrics.characters, metrics.lines),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = metadataColor, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("textMetrics"))
        }
        Button(onClick = onDone, enabled = !completing,
            shape = RoundedCornerShape(MomentLayout.ControlCorner),
            modifier = Modifier.heightIn(min = 48.dp).testTag("done"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            Icon(MomentIcons.Check, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.done), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
internal fun EditorSheetToolbar(editor: EditorState, appearance: AppearanceStore, onAppearance: () -> Unit) {
    EditorToolbarSurface {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f)) {
                GlassToolbarButton(editor::undo, Modifier.testTag("undo"), enabled = editor.canUndo) {
                    Icon(MomentIcons.Undo, stringResource(R.string.undo), Modifier.size(20.dp))
                }
                GlassToolbarButton(editor::redo, Modifier.testTag("redo"), enabled = editor.canRedo) {
                    Icon(MomentIcons.Redo, stringResource(R.string.redo), Modifier.size(20.dp))
                }
            }
            GlassToolbarButton(onAppearance, Modifier.testTag("appearance"), tonal = true) {
                Icon(MomentIcons.Palette, stringResource(R.string.appearance), Modifier.size(20.dp))
            }
            val smaller = stringResource(R.string.font_smaller)
            val larger = stringResource(R.string.font_larger)
            val size = stringResource(R.string.font_size, appearance.fontSize)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                GlassToolbarButton({ appearance.changeFont(-1) }, enabled = appearance.fontSize > 14,
                    modifier = Modifier.semantics { contentDescription = "$smaller，$size" }) {
                    FontAction(MomentIcons.Minus)
                }
                GlassToolbarButton({ appearance.changeFont(1) }, enabled = appearance.fontSize < 26,
                    modifier = Modifier.semantics { contentDescription = "$larger，$size" }) {
                    FontAction(MomentIcons.Plus)
                }
            }
        }
    }
}

@Composable
private fun FontAction(mark: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        Text("A", style = MaterialTheme.typography.titleMedium)
        Icon(mark, null, Modifier.size(11.dp))
    }
}

@Composable
private fun EditorToolbarSurface(content: @Composable () -> Unit) {
    if (LocalGlassAppearance.current.enabled) {
        GlassControlSurface(Modifier.fillMaxWidth(), radius = MomentLayout.ChromeCorner, content = content)
    } else Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(MomentLayout.ChromeCorner),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f)), content = content)
}
