package com.jackie.moment

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.LocaleList
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.jackie.moment.preferences.AppLanguage
import com.jackie.moment.preferences.AppLanguages
import com.jackie.moment.testing.ClipboardProbeActivity
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class LanguagePickerTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val manager get() = context.getSystemService(LocaleManager::class.java)
    private var activity: ActivityScenario<MainActivity>? = null
    private val seed = "Language seed 🙂"
    private val draft = "  A moment.\n片刻 · ひととき 🙂\n "
    @Before fun prepare() {
        context.getSharedPreferences("app_language", Context.MODE_PRIVATE).edit().clear().commit()
        manager.applicationLocales = LocaleList.getEmptyLocaleList()
        probe(seed)
    }
    @After fun close() {
        activity?.close()
        context.getSharedPreferences("app_language", Context.MODE_PRIVATE).edit().clear().commit()
        manager.applicationLocales = LocaleList.getEmptyLocaleList()
    }

    @Test fun pickerPreservesDraftUndoClipboardAndSystemReset() {
        launch()
        compose.onNodeWithTag("editor").performTextReplacement(draft)
        for (language in listOf(AppLanguage.ENGLISH, AppLanguage.SIMPLIFIED, AppLanguage.TRADITIONAL, AppLanguage.JAPANESE, AppLanguage.SYSTEM)) {
            compose.onNodeWithTag("appearance").performClick()
            compose.onNodeWithTag("languageSelector").performScrollTo().performClick()
            compose.onNodeWithTag("language_${language.name}").performClick()
            compose.waitUntil(10_000) { AppLanguages.selected(context) == language }
            compose.onNodeWithTag("closeAppearance").performScrollTo().performClick()
            compose.onNodeWithTag("editor").assertTextEquals(draft)
            compose.onNodeWithTag("undo").performClick()
            compose.onNodeWithTag("editor").assertTextEquals(seed)
            compose.onNodeWithTag("redo").performClick()
            compose.onNodeWithTag("editor").assertTextEquals(draft)
            activity!!.recreate()
            compose.onNodeWithTag("editor").assertTextEquals(draft)
            assertEquals(language, AppLanguages.selected(context))
        }
        assertTrue(manager.applicationLocales.isEmpty)
        assertEquals(seed, probe(null))
    }

    @Test fun externalSystemChoiceAppearsInPicker() {
        launch()
        manager.applicationLocales = LocaleList.forLanguageTags("zh-HK")
        compose.waitUntil(10_000) { AppLanguages.selected(context) == AppLanguage.TRADITIONAL }
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithTag("languageSelector").performScrollTo().assertTextContains("繁體中文")
    }

    @Test fun migratesLegacyChoiceAndStillImportsInitialClipboard() {
        context.getSharedPreferences("app_language", Context.MODE_PRIVATE).edit().putString("locale_tag", "ja").commit()
        launch()
        compose.waitUntil(10_000) { AppLanguages.selected(context) == AppLanguage.JAPANESE }
        compose.onNodeWithTag("done").assertTextContains("完了")
        assertFalse(context.getSharedPreferences("app_language", Context.MODE_PRIVATE).contains("locale_tag"))
    }

    @Test fun legacyContextIsIsolatedAndSystemResetUsesOriginalContext() {
        val original = context.resources.configuration.locales.toLanguageTags()
        val japanese = AppLanguages.legacyContext(context, "ja")
        assertEquals("完了", japanese.getString(R.string.done))
        assertEquals(original, context.resources.configuration.locales.toLanguageTags())
        assertSame(context, AppLanguages.legacyContext(context, ""))
    }

    private fun launch() {
        activity = ActivityScenario.launch(Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("editor").fetchSemanticsNodes(atLeastOneRootRequired = false).singleOrNull()
                ?.config?.get(SemanticsProperties.EditableText)?.text == seed
        }
    }

    private fun probe(text: String?): String? {
        val before = ClipboardProbeActivity.observations
        ActivityScenario.launch<ClipboardProbeActivity>(Intent(context, ClipboardProbeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            if (text != null) putExtra(ClipboardProbeActivity.EXTRA_SEED, text)
        }).use { compose.waitUntil(10_000) { ClipboardProbeActivity.observations > before } }
        return ClipboardProbeActivity.observedText
    }
}
