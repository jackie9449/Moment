package com.jackie.moment.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object MomentIcons {
    val Edit = icon("Edit") {
        moveTo(14f, 5f); lineTo(19f, 10f)
        moveTo(4f, 16f); lineTo(15.5f, 4.5f); curveTo(17f, 3f, 21f, 7f, 19.5f, 8.5f)
        lineTo(8f, 20f); lineTo(3f, 21f); close()
        moveTo(14f, 20f); lineTo(21f, 20f)
    }
    val Check = icon("Check") { moveTo(5f, 12f); lineTo(10f, 17f); lineTo(20f, 7f) }
    val DropDown = icon("DropDown") { moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f) }
    val Palette = icon("Palette") {
        moveTo(12f, 3f); curveTo(6.8f, 3f, 3f, 6.8f, 3f, 12f)
        curveTo(3f, 17.2f, 7f, 21f, 12f, 21f); lineTo(13f, 21f)
        curveTo(15f, 21f, 15.3f, 19.3f, 14.1f, 18f)
        curveTo(13f, 16.6f, 14.4f, 15f, 16f, 15f); lineTo(18f, 15f)
        curveTo(20f, 15f, 21f, 13.4f, 21f, 11.6f)
        curveTo(21f, 6.8f, 17f, 3f, 12f, 3f); close()
        moveTo(7.5f, 11f); lineTo(7.6f, 11f)
        moveTo(10f, 7f); lineTo(10.1f, 7f)
        moveTo(15f, 7.2f); lineTo(15.1f, 7.2f)
        moveTo(18f, 10.8f); lineTo(18.1f, 10.8f)
    }
    val Clipboard = icon("Clipboard") {
        moveTo(9f, 5f); lineTo(6f, 5f); lineTo(6f, 21f); lineTo(20f, 21f); lineTo(20f, 5f); lineTo(17f, 5f)
        moveTo(9f, 3f); lineTo(17f, 3f); lineTo(17f, 7f); lineTo(9f, 7f); close()
        moveTo(10f, 11f); lineTo(16f, 11f); moveTo(10f, 15f); lineTo(16f, 15f)
    }
    val Undo = icon("Undo", true) {
        moveTo(8f, 5f); lineTo(3f, 10f); lineTo(8f, 15f)
        moveTo(3f, 10f); lineTo(14f, 10f); curveTo(22f, 10f, 22f, 20f, 14f, 20f)
    }
    val Redo = icon("Redo", true) {
        moveTo(16f, 5f); lineTo(21f, 10f); lineTo(16f, 15f)
        moveTo(21f, 10f); lineTo(10f, 10f); curveTo(2f, 10f, 2f, 20f, 10f, 20f)
    }
    val Minus = icon("Minus") { moveTo(6f, 12f); lineTo(18f, 12f) }
    val Plus = icon("Plus") { moveTo(6f, 12f); lineTo(18f, 12f); moveTo(12f, 6f); lineTo(12f, 18f) }
    val Close = icon("Close") { moveTo(6f, 6f); lineTo(18f, 18f); moveTo(18f, 6f); lineTo(6f, 18f) }
    val Sun = icon("Sun") {
        moveTo(16f, 12f); arcTo(4f, 4f, 0f, true, true, 8f, 12f); arcTo(4f, 4f, 0f, true, true, 16f, 12f)
        moveTo(12f, 2f); lineTo(12f, 4f); moveTo(12f, 20f); lineTo(12f, 22f)
        moveTo(2f, 12f); lineTo(4f, 12f); moveTo(20f, 12f); lineTo(22f, 12f)
        moveTo(5f, 5f); lineTo(6.5f, 6.5f); moveTo(17.5f, 17.5f); lineTo(19f, 19f)
        moveTo(5f, 19f); lineTo(6.5f, 17.5f); moveTo(17.5f, 6.5f); lineTo(19f, 5f)
    }
    val Moon = icon("Moon") {
        moveTo(20f, 15f); curveTo(17f, 16.2f, 14f, 15.6f, 12f, 13.6f)
        curveTo(9.4f, 11f, 9.2f, 7f, 11f, 4f)
        curveTo(6f, 4.5f, 3f, 9f, 4.2f, 14f)
        curveTo(5.8f, 20.5f, 15.2f, 23.1f, 20f, 15f); close()
    }
    val Device = icon("Device") {
        moveTo(7f, 3f); lineTo(17f, 3f); lineTo(17f, 21f); lineTo(7f, 21f); close()
        moveTo(10f, 17.5f); lineTo(14f, 17.5f)
    }
    val Spark = icon("Spark") {
        moveTo(12f, 3f); lineTo(14.7f, 9.3f); lineTo(21f, 12f); lineTo(14.7f, 14.7f)
        lineTo(12f, 21f); lineTo(9.3f, 14.7f); lineTo(3f, 12f); lineTo(9.3f, 9.3f); close()
    }
    val Quick = icon("Quick") {
        moveTo(13f, 2f); lineTo(5f, 13f); lineTo(11f, 13f); lineTo(10f, 22f)
        lineTo(19f, 10f); lineTo(13f, 10f); close()
    }

    private fun icon(name: String, mirrored: Boolean = false, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = mirrored).apply {
            path(
                fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
                pathBuilder = block,
            )
        }.build()
}
