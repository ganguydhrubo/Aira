package com.ganguydhrubo.aira.voice.ui

import softassert.assertFalse
import softassert.assertTrue
import org.junit.Test
import java.util.Locale

class VoiceSampleTest {
    @Test fun voiceInAUiLanguageSpeaksThatLanguage() {
        for (tag in listOf("en-US", "ja-JP", "ar-EG", "fa-IR", "in-ID", "id-ID", "pt-BR", "vi-VN")) {
            assertTrue(tag, hasUiTranslation(Locale.forLanguageTag(tag)))
        }
    }

    @Test fun otherLanguagesFallBackToTheUiLanguage() {
        for (tag in listOf("fr-FR", "de-DE", "hi-IN", "und", "zh-CN", "zh-TW")) {
            assertFalse(tag, hasUiTranslation(Locale.forLanguageTag(tag)))
        }
    }
}
