package com.ganguydhrubo.aira.voice.ui

import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.voice.model.SttSelectionController
import com.ganguydhrubo.aira.voice.model.TtsSelectionController
import com.ganguydhrubo.aira.voice.model.VoiceModel
import softassert.assertEquals
import softassert.assertNull
import org.junit.Test

class VoiceModelDisplayNameTest {
    @Test fun `built-in system models map to localized name resources`() {
        assertEquals(R.string.voice_catalog_system_stt_name, systemModelDisplayNameRes(SttSelectionController.SYSTEM_STT_MODEL))
        assertEquals(R.string.voice_catalog_system_tts_name, systemModelDisplayNameRes(TtsSelectionController.SYSTEM_TTS_MODEL))
    }

    @Test fun `user defined models keep their persisted display name`() {
        assertNull(systemModelDisplayNameRes(VoiceModel("provider:p1", "m1", "M1")))
        assertNull(systemModelDisplayNameRes(VoiceModel("android-system", "custom", "Custom")))
    }
}
