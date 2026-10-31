package com.jackie.moment.ui

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class GlassAppearance(val enabled: Boolean = false, val dark: Boolean = false,
    val floating: Boolean = false, val blurAvailable: Boolean = false, val frosted: Boolean = false) {
    val documentOpacity get() = if (frosted) {
        if (!floating) .88f else if (blurAvailable) { if (dark) .84f else .80f } else .96f
    } else if (!floating) .78f else if (blurAvailable) {
        if (dark) .70f else .64f
    } else if (dark) .90f else .88f
}
internal val LocalGlassAppearance = staticCompositionLocalOf { GlassAppearance() }

internal class GlassScene(val diagnostic: Boolean = false) {
    var size by mutableStateOf(Size.Zero)
    var origin by mutableStateOf(Offset.Zero)
    var backdrop by mutableStateOf<GraphicsLayer?>(null)
}
internal val LocalGlassScene = staticCompositionLocalOf { GlassScene() }

/** A small contrast veil; the real previous window is composed by Android. */
@Composable
internal fun FloatingBackdrop(modifier: Modifier = Modifier) {
    val dark = LocalGlassAppearance.current.dark
    Canvas(modifier) {
        val shade = if (dark) Color.Black else Color.White
        drawRect(Brush.verticalGradient(0f to shade.copy(alpha = if (dark) .70f else .65f),
            .08f to shade.copy(alpha = 0f), .32f to shade.copy(alpha = 0f),
            .92f to shade.copy(alpha = 0f), 1f to shade.copy(alpha = .35f)))
    }
}

/** The only background sampled by the lenses. User text is never captured. */
@Composable
internal fun GlassBackdrop(modifier: Modifier = Modifier) {
    val appearance = LocalGlassAppearance.current
    val scene = LocalGlassScene.current
    val accent = MaterialTheme.colorScheme.primary
    val darkAmount = animateFloatAsState(if (appearance.dark) 1f else 0f, tween(320), label = "glassBackdropMode")
    val shader = remember { if (Build.VERSION.SDK_INT >= 33) GlassShader(false) else null }
    val modifierWithScene = modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.onGloballyPositioned {
        scene.size = Size(it.size.width.toFloat(), it.size.height.toFloat())
        scene.origin = it.positionInRoot()
    }
    Canvas(modifierWithScene) {
        if (Build.VERSION.SDK_INT >= 33 && shader != null) {
            shader.update(size, Offset.Zero, size, darkAmount.value, accent, Color.Transparent, 0f, 0f, density, diagnostic = scene.diagnostic)
            drawRect(shader.brush)
        } else {
            drawRect(Brush.linearGradient(if (appearance.dark) listOf(Color(0xFF101E35), Color(0xFF29243C), Color(0xFF162D3A))
                else listOf(Color(0xFFDDEFFA), Color(0xFFE8E3F6), Color(0xFFF5E7E4))))
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .18f), Color.Transparent),
                center = Offset(size.width * .9f, size.height * .3f), radius = size.width),
                radius = size.width, center = Offset(size.width * .9f, size.height * .3f))
        }
    }
}

/** One optical surface for a control group. Its children use thin fills, never nested glass. */
@Composable
internal fun GlassControlSurface(
    modifier: Modifier = Modifier,
    radius: Dp = 30.dp,
    tint: Color = Color.Transparent,
    press: State<Float>? = null,
    content: @Composable () -> Unit,
) {
    val appearance = LocalGlassAppearance.current
    if (appearance.frosted) {
        FrostedSurface(modifier, radius, tint, press, content)
        return
    }
    val scene = LocalGlassScene.current
    val accent = MaterialTheme.colorScheme.primary
    val darkAmount = animateFloatAsState(if (appearance.dark) 1f else 0f, tween(320), label = "glassLensMode")
    val shape = RoundedCornerShape(radius)
    var origin by remember { mutableStateOf(Offset.Zero) }
    val shader = remember { if (Build.VERSION.SDK_INT >= 33) GlassShader(true) else null }
    Box(
        modifier.onGloballyPositioned { origin = it.positionInRoot() - scene.origin }
            .graphicsLayer {
                val compression = 1f - (press?.value ?: 0f) * .025f
                scaleX = compression; scaleY = compression
            }
            .shadow(8.dp, shape, clip = false,
                ambientColor = Color.Black.copy(alpha = .07f),
                spotColor = Color.Black.copy(alpha = .14f))
            .clip(shape)
            .drawWithCache {
                val r = radius.toPx().coerceAtMost(size.minDimension / 2f)
                val face = if (appearance.dark) Color(0xFF121B26).copy(alpha = .66f)
                    else Color.White.copy(alpha = .52f)
                val rim = Brush.sweepGradient(listOf(Color.White.copy(alpha = .12f),
                    Color.White.copy(alpha = .05f), accent.copy(alpha = .20f),
                    Color.White.copy(alpha = .68f), Color.White.copy(alpha = .12f)))
                onDrawBehind {
                    val pressed = press?.value ?: 0f
                    if (Build.VERSION.SDK_INT >= 33 && shader != null) {
                        shader.update(size, origin, scene.size, darkAmount.value, accent, tint, r, pressed, density, floating = appearance.floating, diagnostic = scene.diagnostic)
                        drawRoundRect(shader.brush, cornerRadius = CornerRadius(r))
                    } else {
                        drawRoundRect(face, cornerRadius = CornerRadius(r))
                        drawRoundRect(tint.copy(alpha = tint.alpha * .5f), cornerRadius = CornerRadius(r))
                        drawRoundRect(rim, topLeft = Offset(.5f, .5f), size = Size((size.width-1f).coerceAtLeast(0f), (size.height-1f).coerceAtLeast(0f)),
                            cornerRadius = CornerRadius(r), style = Stroke(.65.dp.toPx()))
                        if (pressed > 0f) drawRoundRect(Color.White.copy(alpha = pressed * .12f), cornerRadius = CornerRadius(r))
                    }
                }
            },
    ) { content() }
}
