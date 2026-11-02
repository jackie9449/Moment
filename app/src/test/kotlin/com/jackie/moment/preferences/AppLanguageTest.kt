package com.jackie.moment.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test fun storedTagsRoundTrip() {
        AppLanguage.entries.forEach { assertEquals(it, AppLanguage.fromTag(it.tag)) }
    }
    @Test fun regionalAndScriptChoicesRespectExplicitScript() {
        listOf("zh-TW", "zh-HK", "zh-MO", "zh-Hant-CN").forEach {
            assertEquals(AppLanguage.TRADITIONAL, AppLanguage.fromTag(it))
        }
        listOf("zh", "zh-CN", "zh-SG", "zh-Hans-HK").forEach {
            assertEquals(AppLanguage.SIMPLIFIED, AppLanguage.fromTag(it))
        }
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en-GB"))
        assertEquals(AppLanguage.JAPANESE, AppLanguage.fromTag("ja-JP"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag("fr"))
    }
}
