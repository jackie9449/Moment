package com.jackie.moment.ui

import android.os.Build
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/** Only decorative draw commands enter this layer: never the document or another app. */
@Composable
internal fun FrostedBackdrop(modifier: Modifier = Modifier) {
    val scene = LocalGlassScene.current
    val layer = rememberGraphicsLayer()
    val dark = LocalGlassAppearance.current.dark
    val darkAmount = animateFloatAsState(if (dark) 1f else 0f, tween(320), label = "frostBackdropMode")
    val accent = MaterialTheme.colorScheme.primary
    SideEffect { scene.backdrop = layer }
    DisposableEffect(scene, layer) { onDispose { if (scene.backdrop === layer) scene.backdrop = null } }
    Canvas(modifier.onGloballyPositioned {
        scene.size = Size(it.size.width.toFloat(), it.size.height.toFloat())
        scene.origin = it.positionInRoot()
    }.drawWithContent {
        layer.record { this@drawWithContent.drawContent() }
        drawLayer(layer)
    }) {
        if (scene.diagnostic) {
            drawRect(Color(0xFFE8F2FA))
            val step = 24.dp.toPx()
            val ink = Color(0xFF21527A)
            for (x in 0..(size.width/step).toInt()) drawLine(ink, Offset(x*step,0f),Offset(x*step,size.height),1.2.dp.toPx())
            for (y in 0..(size.height/step).toInt()) drawLine(ink, Offset(0f,y*step),Offset(size.width,y*step),1.2.dp.toPx())
        } else {
            val progress = darkAmount.value
            drawRect(lerp(Color(0xFFE7EDF6),Color(0xFF131B28),progress))
            val fields = listOf(lerp(Color(0xFFB8D9E4),Color(0xFF294A62),progress),
                lerp(Color(0xFFD4C5E5),Color(0xFF49405F),progress),lerp(Color(0xFFF1D6C3),Color(0xFF4C3838),progress))
            val centers = listOf(Offset(size.width*.08f,size.height*.10f),Offset(size.width*1.02f,size.height*.46f),
                Offset(size.width*.05f,size.height*.94f))
            fields.forEachIndexed { i, color ->
                val r = size.width * 1.15f
                drawCircle(Brush.radialGradient(listOf(color,Color.Transparent),centers[i],r),r,centers[i])
            }
            drawRect(accent.copy(alpha = .025f))
        }
    }
}

/** Native backdrop blur + neutral material fill, with foreground content kept outside the effect. */
@Composable
internal fun FrostedSurface(modifier: Modifier, radius: Dp, tint: Color,
    press: State<Float>?, content: @Composable () -> Unit, thick: Boolean = false) {
    val appearance = LocalGlassAppearance.current
    val scene = LocalGlassScene.current
    val blurred = rememberGraphicsLayer()
    var origin by remember { mutableStateOf(Offset.Zero) }
    val shape = RoundedCornerShape(radius)
    val base by animateColorAsState(if (appearance.dark) Color(0xFF282C35) else Color(0xFFF9FAFD),
        tween(320), label = "frostMaterialMode")
    val alpha = if (thick) appearance.documentOpacity
        else if (appearance.floating && !appearance.blurAvailable) .96f
        else if (appearance.dark || appearance.floating) .76f else .69f
    Box(modifier.onGloballyPositioned { origin = it.positionInRoot()-scene.origin }
        .graphicsLayer {
            val compression = 1f-(press?.value ?: 0f)*.018f
            scaleX = compression; scaleY = compression
        }
        .shadow(10.dp,shape,clip=false,ambientColor=Color.Black.copy(alpha=.06f),spotColor=Color.Black.copy(alpha=.10f))
        .clip(shape)
        .drawWithCache {
            val padding = 54.dp.toPx()
            val source = scene.backdrop
            val hasBackdrop = !appearance.floating && source != null && Build.VERSION.SDK_INT >= 31
            if (hasBackdrop) {
                blurred.renderEffect = BlurEffect(18.dp.toPx(),18.dp.toPx(),TileMode.Clamp)
                blurred.record(size=IntSize((size.width+padding*2).toInt(),(size.height+padding*2).toInt())) {
                    translate(padding-origin.x,padding-origin.y) { drawLayer(source) }
                }
            }
            val r = radius.toPx().coerceAtMost(size.minDimension/2f)
            val separator = if (appearance.dark) Color.White.copy(alpha=.12f) else Color.White.copy(alpha=.72f)
            val faceAlpha = if (!appearance.floating && Build.VERSION.SDK_INT < 31) .93f else alpha
            onDrawBehind {
                if (hasBackdrop) translate(-padding,-padding) { drawLayer(blurred) }
                drawRect(base.copy(alpha=faceAlpha))
                drawRect(tint.copy(alpha=tint.alpha*.30f))
                drawRoundRect(separator,topLeft=Offset(.4.dp.toPx(),.4.dp.toPx()),
                    size=Size((size.width-.8.dp.toPx()).coerceAtLeast(0f),(size.height-.8.dp.toPx()).coerceAtLeast(0f)),
                    cornerRadius=CornerRadius(r),style=Stroke(.6.dp.toPx()))
                val pressed = press?.value ?: 0f
                if (pressed>0f) drawRect(Color.White.copy(alpha=pressed*.05f))
            }
        }) { content() }
}
