package com.jackie.moment.preferences

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale
import androidx.core.content.edit

enum class AppLanguage(val tag: String, val nativeName: String) {
    SYSTEM("", ""), SIMPLIFIED("zh-CN", "简体中文"), TRADITIONAL("zh-Hant", "繁體中文"),
    ENGLISH("en", "English"), JAPANESE("ja", "日本語");

    companion object {
        fun fromTag(tag: String): AppLanguage {
            val locale = Locale.forLanguageTag(tag)
            return when (locale.language) {
                "zh" -> if (locale.script == "Hant" || (locale.script.isEmpty() && locale.country in setOf("TW", "HK", "MO"))) TRADITIONAL else SIMPLIFIED
                "en" -> ENGLISH
                "ja" -> JAPANESE
                else -> SYSTEM
            }
        }
    }
}

object AppLanguages {
    private const val FILE = "app_language"
    private const val KEY = "locale_tag"
    fun selected(context: Context): AppLanguage = if (Build.VERSION.SDK_INT >= 33) {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        AppLanguage.fromTag(if (locales.isEmpty) "" else locales[0].toLanguageTag())
    } else AppLanguage.fromTag(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, "").orEmpty())

    fun select(activity: Activity, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= 33) {
            val manager = activity.getSystemService(LocaleManager::class.java)
            val requested = LocaleList.forLanguageTags(language.tag)
            if (manager.applicationLocales == requested) return
            manager.applicationLocales = requested
        } else {
            val preferences = activity.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            if (preferences.getString(KEY, "") == language.tag) return
            preferences.edit { putString(KEY, language.tag) }
            activity.recreate()
        }
    }

    fun wrap(context: Context): Context {
        if (Build.VERSION.SDK_INT >= 33) return context
        val tag = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, "").orEmpty()
        return legacyContext(context, tag)
    }

    internal fun legacyContext(context: Context, tag: String): Context {
        if (tag.isEmpty()) return context
        return context.createConfigurationContext(Configuration(context.resources.configuration).apply {
            setLocales(LocaleList.forLanguageTags(tag))
        })
    }

    /** Move a pre-Android13 choice once, without overriding a system selection. */
    fun migrate(context: Context) {
        if (Build.VERSION.SDK_INT < 33) return
        val preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val tag = preferences.getString(KEY, null) ?: return
        val manager = context.getSystemService(LocaleManager::class.java)
        if (manager.applicationLocales.isEmpty && tag.isNotEmpty()) {
            manager.applicationLocales = LocaleList.forLanguageTags(tag)
        }
        preferences.edit { remove(KEY) }
    }
}
