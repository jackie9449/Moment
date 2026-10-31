package com.jackie.moment.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A thicker reading material; native blur stays below the document's foreground. */
@Composable
internal fun EditorDocumentSurface(modifier: Modifier, content: @Composable () -> Unit) {
    val appearance = LocalGlassAppearance.current
    if (appearance.frosted) {
        FrostedSurface(modifier,MomentLayout.PageCorner,Color.Transparent,null,content,thick=true)
    } else Surface(modifier=modifier,
        shape=RoundedCornerShape(MomentLayout.PageCorner),
        color=if (appearance.enabled) MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha=appearance.documentOpacity)
            else MaterialTheme.colorScheme.surfaceContainerLowest,
        border=BorderStroke(.7.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=.5f)),
        content=content)
}
