package com.ganguydhrubo.aira.voice.model

import com.ganguydhrubo.aira.calls.OpenAiCallProtocol
import com.ganguydhrubo.aira.calls.VolcRealtimeConfig
import softassert.assertEquals
import softassert.assertTrue
import org.junit.Test

class VoiceVoicesTest {
    @Test fun `realtime defaults are in their protocol lists`() {
        assertTrue(VolcRealtimeConfig.DEFAULT_SPEAKER in VoiceVoices.realtime(RealtimeProtocol.VolcDuplex))
        assertTrue(OpenAiCallProtocol.VOICE in VoiceVoices.realtime(RealtimeProtocol.OpenAiRealtime))
    }

    @Test fun `a saved custom voice is kept in front`() {
        val known = VoiceVoices.realtime(RealtimeProtocol.VolcDuplex)
        assertEquals(listOf("S_clone1") + known, VoiceVoices.withCurrent(known, "S_clone1"))
        assertEquals(known, VoiceVoices.withCurrent(known, known[1]))
        assertEquals(known, VoiceVoices.withCurrent(known, " "))
    }

    @Test fun `volc ids get a short label and others stay as they are`() {
        assertEquals("Vv", VoiceVoices.label("zh_female_vv_jupiter_bigtts"))
        assertEquals("Yunzhou", VoiceVoices.label("zh_male_yunzhou_jupiter_bigtts"))
        assertEquals("S_clone1", VoiceVoices.label("S_clone1"))
        assertEquals("marin", VoiceVoices.label("marin"))
    }

    @Test fun `volcano voices follow the synthesis resource and read as their published names`() {
        fun model(resource: String) = VoiceModel("volc", resource, resource,
            tts = TtsBinding(TtsProtocol.VolcSpeech, region = resource, voice = "zh_female_vv_uranus_bigtts"))
        val two = VoiceVoices.tts(model("seed-tts-2.0"))
        val one = VoiceVoices.tts(model("seed-tts-1.0"))
        softassert.assertTrue(two.size > 400 && "zh_female_vv_uranus_bigtts" in two)
        softassert.assertTrue(one.size > 100 && "zh_female_cancan_mars_bigtts" in one)
        softassert.assertEquals("Vivi 2.0", VoiceVoices.label("zh_female_vv_uranus_bigtts"))
    }

    @Test fun `a fetched voice name wins over the id`() {
        val model = VoiceModel("el", "m", "m", tts = TtsBinding(TtsProtocol.ElevenLabsSpeech, voices = listOf("abc"), voiceNames = mapOf("abc" to "Rachel")))
        softassert.assertEquals("Rachel", VoiceVoices.label(model, "abc"))
        softassert.assertEquals("zzz", VoiceVoices.label(model, "zzz"))
    }
}
