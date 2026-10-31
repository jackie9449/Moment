package com.jackie.moment.ui

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackie.moment.preferences.AppearanceStore
import com.jackie.moment.preferences.Palette
import com.jackie.moment.preferences.ThemeMode
import com.jackie.moment.preferences.InterfaceStyle

@Composable
fun MomentTheme(appearance: AppearanceStore, floating: Boolean = false, blurAvailable: Boolean = false,
    content: @Composable (Boolean) -> Unit) {
    val dark = when (appearance.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val target = if (appearance.dynamicColor && Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else paletteScheme(appearance.palette, dark)
    MaterialTheme(
        colorScheme = animatedScheme(target),
        typography = MomentTypography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
            extraLarge = RoundedCornerShape(32.dp),
        ),
    ) {
        CompositionLocalProvider(LocalGlassAppearance provides GlassAppearance(appearance.style != InterfaceStyle.MATERIAL,
            dark, floating, blurAvailable, frosted = appearance.style == InterfaceStyle.FROSTED_GLASS)) {
            content(dark)
        }
    }
}

@Composable
private fun animatedScheme(target: ColorScheme): ColorScheme {
    @Composable fun color(value: Color): Color {
        val animated by animateColorAsState(value, tween(320), label = "themeColor")
        return animated
    }
    return target.copy(
        primary = color(target.primary), onPrimary = color(target.onPrimary),
        primaryContainer = color(target.primaryContainer), onPrimaryContainer = color(target.onPrimaryContainer),
        secondary = color(target.secondary), onSecondary = color(target.onSecondary),
        secondaryContainer = color(target.secondaryContainer), onSecondaryContainer = color(target.onSecondaryContainer),
        background = color(target.background), onBackground = color(target.onBackground),
        surface = color(target.surface), onSurface = color(target.onSurface),
        surfaceVariant = color(target.surfaceVariant), onSurfaceVariant = color(target.onSurfaceVariant),
        surfaceContainer = color(target.surfaceContainer), surfaceContainerLow = color(target.surfaceContainerLow),
        surfaceContainerHigh = color(target.surfaceContainerHigh), surfaceContainerHighest = color(target.surfaceContainerHighest),
        surfaceContainerLowest = color(target.surfaceContainerLowest),
        outline = color(target.outline), outlineVariant = color(target.outlineVariant),
        inverseSurface = color(target.inverseSurface), inverseOnSurface = color(target.inverseOnSurface),
    )
}

internal fun paletteScheme(palette: Palette, dark: Boolean): ColorScheme {
    val primary = when (palette) {
        Palette.IRIS -> if (dark) Color(0xFFCDBDFF) else Color(0xFF62549F)
        Palette.OCEAN -> if (dark) Color(0xFF9FCBFF) else Color(0xFF24609D)
        Palette.FOREST -> if (dark) Color(0xFFA4D5B1) else Color(0xFF37684A)
    }
    val container = when (palette) {
        Palette.IRIS -> if (dark) Color(0xFF413367) else Color(0xFFE9E0FF)
        Palette.OCEAN -> if (dark) Color(0xFF163F69) else Color(0xFFD8E8FF)
        Palette.FOREST -> if (dark) Color(0xFF214D32) else Color(0xFFD4EDDB)
    }
    return if (dark) darkColorScheme(
        primary = primary, onPrimary = Color(0xFF292134),
        primaryContainer = container, onPrimaryContainer = Color(0xFFF2EAFF),
        secondary = Color(0xFFC8C1D7), secondaryContainer = Color(0xFF34313D),
        onSecondaryContainer = Color(0xFFEAE4F3),
        background = Color(0xFF131217), onBackground = Color(0xFFE9E5EE),
        surface = Color(0xFF19171E), onSurface = Color(0xFFE9E5EE),
        surfaceVariant = Color(0xFF302D38), onSurfaceVariant = Color(0xFFC0BACB),
        surfaceContainerLowest = Color(0xFF100F13), surfaceContainerLow = Color(0xFF1D1B23),
        surfaceContainer = Color(0xFF232129), surfaceContainerHigh = Color(0xFF2D2A34),
        surfaceContainerHighest = Color(0xFF37333E), outline = Color(0xFF928B9E),
        outlineVariant = Color(0xFF48434F),
    ) else lightColorScheme(
        primary = primary, onPrimary = Color.White,
        primaryContainer = container, onPrimaryContainer = Color(0xFF29203E),
        secondary = Color(0xFF655D73), secondaryContainer = Color(0xFFEEEBF5),
        onSecondaryContainer = Color(0xFF454052),
        background = Color(0xFFF8F7FC), onBackground = Color(0xFF23212B),
        surface = Color(0xFFFFFBFF), onSurface = Color(0xFF25222D),
        surfaceVariant = Color(0xFFECE8F2), onSurfaceVariant = Color(0xFF787080),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF2EFF7),
        surfaceContainer = Color(0xFFEDE9F3), surfaceContainerHigh = Color(0xFFE6E0EF),
        surfaceContainerHighest = Color(0xFFDFD9E8), outline = Color(0xFF817989),
        outlineVariant = Color(0xFFE4DEEA),
    )
}

private val MomentTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 30.sp, lineHeight = 40.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 31.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 23.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)
