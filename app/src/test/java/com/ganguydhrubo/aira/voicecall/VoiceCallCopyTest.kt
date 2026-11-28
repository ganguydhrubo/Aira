package com.ganguydhrubo.aira.voicecall

import com.ganguydhrubo.aira.core.frontdesk.DESK_RESULT_MAX_CHARS
import com.ganguydhrubo.aira.core.frontdesk.DeskEvent
import com.ganguydhrubo.aira.core.frontdesk.DeskReport
import com.ganguydhrubo.aira.i18n.ChineseTexts
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** 通话页的错误/结束/状态文案映射:8+1 类错误、5 种结束原因各占一条;RESUMED 超时落的 Ended(Background) 也有独立说法。 */
class VoiceCallCopyTest {

    @get:Rule val texts = ChineseTexts()

    @Test
    fun `每类错误各占一条文案`() {
        assertEquals("The voice credentials were rejected — check the setup", voiceCallErrorText(VoiceCallError.Auth))
        assertEquals("Connection timed out — try again", voiceCallErrorText(VoiceCallError.ConnectTimeout))
        assertEquals("The connection dropped", voiceCallErrorText(VoiceCallError.Disconnected))
        assertEquals("Too many requests — try again shortly", voiceCallErrorText(VoiceCallError.RateLimited))
        assertEquals("The voice service ran into an error", voiceCallErrorText(VoiceCallError.Provider))
        assertEquals("No sound from the microphone", voiceCallErrorText(VoiceCallError.MicSilent))
        assertEquals("Another app is using the audio channel", voiceCallErrorText(VoiceCallError.AudioFocus))
        assertEquals("Playback failed", voiceCallErrorText(VoiceCallError.Playback))
        assertEquals("A phone call is in progress — try again after it ends", voiceCallErrorText(VoiceCallError.Busy))
        assertEquals(VoiceCallError.entries.size, VoiceCallError.entries.map(::voiceCallErrorText).distinct().size)
    }

    @Test
    fun `每种结束原因各占一条文案`() {
        assertEquals("Call ended", voiceCallEndText(EndReason.Hangup))
        assertEquals("Ended from the notification", voiceCallEndText(EndReason.Notification))
        assertEquals("A phone call came in — call ended", voiceCallEndText(EndReason.IncomingCall))
        assertEquals("No activity for 5 min — call ended", voiceCallEndText(EndReason.Idle, idleMinutes = 5))
        assertEquals("No activity for 5 min — call ended", voiceCallStatusText(VoiceCallState.Ended(EndReason.Idle, idleMinutes = 5)))
        assertEquals("Return to Aira to continue", voiceCallEndText(EndReason.Background))
        assertEquals(EndReason.entries.size, EndReason.entries.map { voiceCallEndText(it, 5) }.distinct().size)
    }

    @Test
    fun `进行中的状态词`() {
        assertEquals("Connecting…", voiceCallStatusText(VoiceCallState.Connecting))
        assertEquals("Checking the microphone…", voiceCallStatusText(VoiceCallState.MicCheck))
        assertEquals("Connected", voiceCallStatusText(VoiceCallState.Active(1_000L)))
        // 终态的状态词就是结束/错误文案
        assertEquals("Call ended", voiceCallStatusText(VoiceCallState.Ended(EndReason.Hangup)))
        assertEquals("Connection timed out — try again", voiceCallStatusText(VoiceCallState.Failed(VoiceCallError.ConnectTimeout, 1)))
    }

    @Test
    fun `前台汇报说成一句话,正文截到上限`() {
        assertEquals("About 订日料: Done: 七点两位", deskReportSpeech(DeskReport(DeskEvent.Succeeded("t1", "七点两位"), "订日料")))
        assertEquals("About 订日料: Asking you: 几点？", deskReportSpeech(DeskReport(DeskEvent.NeedsAnswer("t1", "几点？", null), "订日料")))
        val long = deskReportSpeech(DeskReport(DeskEvent.Succeeded("t1", "字".repeat(DESK_RESULT_MAX_CHARS)), "订日料"))
        assertTrue(long.length <= "About 订日料: ".length + DESK_RESULT_MAX_CHARS)
    }

    @Test
    fun `打字派单的结果回到本地`() {
        fun result(status: String) = buildJsonObject { put("status", status) }
        assertEquals("Working on it", typedDeskResult(result("started")))
        assertEquals("Working on it", typedDeskResult(result("queued")))
        assertEquals("Working on it", typedDeskResult(result("already_dispatched")))
        assertEquals("Could not finish it", typedDeskResult(result("failed")))
        assertEquals("Could not finish it", typedDeskResult(result("rejected")))
    }
}
