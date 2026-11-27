package com.ganguydhrubo.aira.voice.ondevice

import com.ganguydhrubo.aira.i18n.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.util.Locale

class OnDeviceLanguageTest {
    private val catalog = VoicePackCatalog.parse(File("src/main/assets/voice-models/catalog.json").readText()) { _, _ -> true }

    @Test
    fun scriptAndRegionTagsMapToLanguagePart() {
        assertEquals("zh", OnDeviceLanguage.languagePart("zh-Hans-CN"))
        assertEquals("pt", OnDeviceLanguage.languagePart("pt-BR"))
        assertEquals("id", OnDeviceLanguage.languagePart("in-ID"))
        assertNull(OnDeviceLanguage.languagePart(null))
        assertNull(OnDeviceLanguage.languagePart(""))
    }

    @Test
    fun pickReturnsNullWhenUnsupported() {
        assertNull(OnDeviceLanguage.pick("fa-IR", listOf("zh", "en")))
        assertEquals("en", OnDeviceLanguage.pick("en-US", listOf("zh", "en")))
        assertNull(OnDeviceLanguage.pick(null, listOf("zh", "en")))
    }

    @Test
    fun pickUsesModelLegacyCodes() {
        assertEquals("iw", OnDeviceLanguage.pick("he-IL", listOf("en", "iw")))
        assertEquals("jw", OnDeviceLanguage.pick("jv-ID", listOf("en", "jw")))
    }

    @Test
    fun systemLanguageResolvesRecommendation() {
        assertEquals("dolphin-base-ctc-int8", OnDeviceLanguage.recommended(catalog, AppLanguage.System, Locale("vi", "VN"))?.id)
        assertEquals("sensevoice-small-int8", OnDeviceLanguage.recommended(catalog, AppLanguage.System, Locale("zh", "TW"))?.id)
        assertEquals("whisper-base-int8", OnDeviceLanguage.recommended(catalog, AppLanguage.System, Locale("in", "ID"))?.id)
        // 不支持的系统语言落英文。
        assertEquals("sensevoice-small-int8", OnDeviceLanguage.recommended(catalog, AppLanguage.System, Locale("de", "DE"))?.id)
    }

    @Test
    fun explicitAppLanguageWins() {
        assertEquals("whisper-base-int8", OnDeviceLanguage.recommended(catalog, AppLanguage.Turkish, Locale("vi", "VN"))?.id)
        assertEquals("dolphin-base-ctc-int8", OnDeviceLanguage.recommended(catalog, AppLanguage.Persian)?.id)
    }

    @Test
    fun everyAppLanguageHasRecommendation() {
        AppLanguage.entries.filter { it.tag != null }.forEach {
            assertEquals(it.name, true, OnDeviceLanguage.recommended(catalog, it) != null)
        }
    }

}
