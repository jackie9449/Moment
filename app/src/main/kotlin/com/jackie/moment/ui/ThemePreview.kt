package com.jackie.moment.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.jackie.moment.preferences.InterfaceStyle

/** Consistent miniature page diagrams, rather than unrelated decorative badges. */
@Composable
internal fun ThemePreview(style: InterfaceStyle, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val dark = LocalGlassAppearance.current.dark
    Canvas(modifier.clip(RoundedCornerShape(10.dp))) {
        val r = 5.dp.toPx()
        val inset = 6.dp.toPx()
        val glass = style != InterfaceStyle.MATERIAL
        drawRect(if (glass) Brush.linearGradient(if (dark) listOf(Color(0xFF243745),Color(0xFF3E354F))
            else listOf(Color(0xFFD1E3EA),Color(0xFFE3DAEB)))
            else Brush.linearGradient(listOf(colors.surfaceContainer,colors.surfaceContainer)))
        val paper = colors.surfaceContainerLowest.copy(alpha = if (style == InterfaceStyle.LIQUID_GLASS) .78f else .92f)
        val paperSize = Size(size.width-inset*2,size.height-inset*2-9.dp.toPx())
        drawRoundRect(paper, Offset(inset,inset),paperSize,CornerRadius(r))
        drawRoundRect(paper,Offset(inset,size.height-inset-6.dp.toPx()),
            Size(size.width-inset*2,6.dp.toPx()),CornerRadius(3.dp.toPx()))
        drawCircle(colors.primary.copy(alpha=.65f),1.dp.toPx(),Offset(size.width/2,size.height-inset-3.dp.toPx()))
        val right = size.width-inset*2
        drawRoundRect(colors.primary.copy(alpha = .75f),Offset(right-9.dp.toPx(),inset+3.dp.toPx()),
            Size(10.dp.toPx(),5.dp.toPx()),CornerRadius(2.dp.toPx()))
        drawRoundRect(colors.onSurfaceVariant.copy(alpha = .35f),Offset(inset+4.dp.toPx(),inset+4.dp.toPx()),
            Size(size.width*.18f,2.dp.toPx()),CornerRadius(1.dp.toPx()))
        repeat(3) { line ->
            drawRoundRect(colors.onSurfaceVariant.copy(alpha = .23f),Offset(inset+4.dp.toPx(),inset+(14+line*5).dp.toPx()),
                Size(size.width*(if (line==2) .38f else .58f),1.5.dp.toPx()),CornerRadius(1.dp.toPx()))
        }
        if (style == InterfaceStyle.LIQUID_GLASS) {
            drawRoundRect(Color.White.copy(alpha = if (dark) .18f else .65f),Offset(inset,inset),
                paperSize,CornerRadius(r),style=Stroke(.6.dp.toPx()))
        }
    }
}
