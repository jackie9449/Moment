package com.jackie.moment.preferences

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class Palette { IRIS, OCEAN, FOREST }
enum class InterfaceStyle { MATERIAL, LIQUID_GLASS, FROSTED_GLASS }

class AppearanceStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    var mode by mutableStateOf(readEnum("mode", ThemeMode.SYSTEM))
        private set
    var style by mutableStateOf(readEnum("style", InterfaceStyle.MATERIAL))
        private set
    var floatingMode by mutableStateOf(preferences.getBoolean("floating_mode", false))
        private set
    val useFloatingWindow get() = Build.VERSION.SDK_INT >= 30 && style != InterfaceStyle.MATERIAL && floatingMode
    var palette by mutableStateOf(readEnum("palette", Palette.IRIS))
        private set
    var dynamicColor by mutableStateOf(preferences.getBoolean("dynamic", false))
        private set
    var fontSize by mutableIntStateOf(preferences.getInt("font_size", 18).coerceIn(14, 26))
        private set

    fun updateMode(value: ThemeMode) { mode = value; preferences.edit { putString("mode", value.name) } }
    fun updateStyle(value: InterfaceStyle) { style = value; preferences.edit { putString("style", value.name) } }
    fun updateFloatingMode(value: Boolean) { floatingMode = value; preferences.edit { putBoolean("floating_mode", value) } }
    fun updatePalette(value: Palette) {
        palette = value
        dynamicColor = false
        preferences.edit { putString("palette", value.name); putBoolean("dynamic", false) }
    }
    fun setDynamic(value: Boolean) { dynamicColor = value; preferences.edit { putBoolean("dynamic", value) } }
    fun changeFont(delta: Int) {
        fontSize = (fontSize + delta).coerceIn(14, 26)
        preferences.edit { putInt("font_size", fontSize) }
    }
    private inline fun <reified T : Enum<T>> readEnum(key: String, fallback: T): T =
        enumValues<T>().find { it.name == preferences.getString(key, null) } ?: fallback
}
