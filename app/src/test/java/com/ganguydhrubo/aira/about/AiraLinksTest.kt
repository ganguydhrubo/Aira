package com.ganguydhrubo.aira.about

import org.junit.Rule
import com.ganguydhrubo.aira.i18n.ChineseTexts
import softassert.assertEquals
import softassert.assertFalse
import softassert.assertTrue
import org.junit.Test
import java.net.URI
import java.net.URLDecoder

class AiraLinksTest {
    @get:Rule val texts = ChineseTexts()

    private val device = AiraLinks.Device("Google", "Pixel 8", "14", 34, "0.1.0", "65fcf92")

    @Test
    fun bodyHasThreePrefilledLines() {
        val lines = AiraLinks.issueBody(device).lines()
        assertEquals("- Device: Google Pixel 8", lines[0])
        assertEquals("- Android version: 14 (SDK 34)", lines[1])
        assertEquals("- Aira version: 0.1.0 (build 65fcf92)", lines[2])
        assertTrue(lines.contains("### What went wrong"))
        assertTrue(lines.contains("### Steps you took"))
        assertTrue(lines.contains("### What you expected"))
        // 诊断信息不自动带上:只提示用户去哪里复制
        assertTrue(lines.contains("### Diagnostics"))
        assertTrue(lines.contains("(Optional) In Aira, open About › Send feedback › Copy diagnostics, then paste it here"))
    }

    /** 型号里已经带厂商名时不重复写。 */
    @Test
    fun modelNotDoubled() {
        val body = AiraLinks.issueBody(device.copy(manufacturer = "samsung", model = "SAMSUNG SM-S9180"))
        assertTrue(body.startsWith("- Device: SAMSUNG SM-S9180\n"))
    }

    @Test
    fun issueUrlIsEncoded() {
        val url = AiraLinks.issueUrl(device)
        assertTrue(url.startsWith("https://github.com/ganguydhrubo/Aira/issues/new?title=&body="))
        val query = url.substringAfter('?')
        assertFalse("no raw spaces or newlines", query.any { it == ' ' || it == '\n' || it == '+' })
        assertFalse("no raw non-ASCII", query.any { it.code > 127 })
        // 能被当成合法 URI 解析,正文解码回原样
        URI(url)
        val body = URLDecoder.decode(query.substringAfter("body="), "UTF-8")
        assertEquals(AiraLinks.issueBody(device), body)
    }
}
