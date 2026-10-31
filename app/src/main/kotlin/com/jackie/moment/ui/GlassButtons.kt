package com.jackie.moment.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

internal class GlassTouch(val source: MutableInteractionSource, val level: State<Float>, val position: State<Offset>)

@Composable
internal fun rememberGlassTouch(): GlassTouch {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val level = animateFloatAsState(if (pressed) 1f else 0f,
        spring(dampingRatio = .82f, stiffness = 950f), label = "glassTouch")
    val position = remember { mutableStateOf(Offset.Unspecified) }
    LaunchedEffect(source) {
        source.interactions.collect { if (it is PressInteraction.Press) position.value = it.pressPosition }
    }
    return remember(source) { GlassTouch(source, level, position) }
}

internal fun Modifier.glassTouchLight(touch: GlassTouch, dark: Boolean): Modifier = drawWithCache {
    val raw = touch.position.value
    val point = if (raw.x.isFinite() && raw.y.isFinite()) raw else Offset(size.width/2f, size.height/2f)
    val glow = Brush.radialGradient(listOf(Color.White.copy(alpha = if (dark) .17f else .30f), Color.Transparent),
        center = point, radius = 28.dp.toPx())
    onDrawBehind { drawCircle(glow, radius = 28.dp.toPx(), center = point, alpha = touch.level.value) }
}

/** A single thin overlay on the shared material; no nested glass or ink disc. */
@Composable
internal fun GlassToolbarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tonal: Boolean = false,
    content: @Composable () -> Unit,
) {
    val glass = LocalGlassAppearance.current.enabled
    val clearFloating = glass && LocalGlassAppearance.current.floating && !LocalGlassAppearance.current.frosted
    val touch = rememberGlassTouch()
    val dark = LocalGlassAppearance.current.dark
    val color = (if (tonal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        .copy(alpha = if (enabled) 1f else .36f)
    Box(modifier.size(48.dp).clip(RoundedCornerShape(MomentLayout.ControlCorner))
        .background(when {
            tonal -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (clearFloating) .55f else if (glass) .30f else .70f)
            clearFloating -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = .18f)
            else -> Color.Transparent
        })
        .then(if (glass) Modifier.glassTouchLight(touch, dark) else Modifier)
        .clickable(interactionSource = touch.source, indication = if (glass) null else ripple(),
            enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center) {
        CompositionLocalProvider(LocalContentColor provides color) {
            Box(Modifier.graphicsLayer { scaleX = 1f-touch.level.value*.06f; scaleY = scaleX },
                contentAlignment = Alignment.Center) { content() }
        }
    }
}
