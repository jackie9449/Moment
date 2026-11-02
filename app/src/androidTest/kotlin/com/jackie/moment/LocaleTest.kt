package com.jackie.moment

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.LocaleList
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import com.jackie.moment.testing.ClipboardProbeActivity
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class LocaleTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private var activity: ActivityScenario<MainActivity>? = null
    @After fun close() {
        activity?.close()
        context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.getEmptyLocaleList()
    }
    @Test fun english() = checkLocale("en", "Done", "Personalization")
    @Test fun traditionalHongKong() = checkLocale("zh-HK", "完成", "個人化")
    @Test fun japanese() = checkLocale("ja", "完了", "カスタマイズ")
    private fun checkLocale(tag: String, done: String, title: String) {
        context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(tag)
        val previous = ClipboardProbeActivity.observations
        ActivityScenario.launch<ClipboardProbeActivity>(Intent(context, ClipboardProbeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            putExtra(ClipboardProbeActivity.EXTRA_SEED, "locale seed")
        }).use {
            compose.waitUntil(10_000) { ClipboardProbeActivity.observations > previous }
        }
        activity = ActivityScenario.launch(Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("editor").fetchSemanticsNodes().singleOrNull()
                ?.config?.get(SemanticsProperties.EditableText)?.text == "locale seed"
        }
        compose.onNodeWithTag("editor").performTextReplacement("A moment.\nひととき · 片刻")
        compose.onNodeWithText(when(tag) { "en" -> "Editing"; "ja" -> "編集中"; else -> "正在編輯" }).assertIsDisplayed()
        compose.onNodeWithTag("done").assertTextContains(done).assertIsDisplayed()
        compose.onNodeWithTag("appearance").performClick()
        compose.onNodeWithText(title).assertIsDisplayed()
        compose.onNodeWithTag("closeAppearance").performClick()
        compose.onNodeWithTag("editor").assertTextEquals("A moment.\nひととき · 片刻")
    }
}
