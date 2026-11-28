package com.ganguydhrubo.aira.calls.ui

import com.ganguydhrubo.aira.voice.model.SttBinding
import com.ganguydhrubo.aira.voice.model.SttProtocol
import com.ganguydhrubo.aira.voice.model.SttSelection
import com.ganguydhrubo.aira.voice.model.SttSelectionController
import com.ganguydhrubo.aira.voice.model.VoiceAccount
import com.ganguydhrubo.aira.voice.model.VoiceCatalogSnapshot
import com.ganguydhrubo.aira.voice.model.VoiceModel
import com.ganguydhrubo.aira.voice.model.VoiceSelections
import com.ganguydhrubo.aira.voice.model.VoiceService
import com.ganguydhrubo.aira.voice.ondevice.OnDeviceCatalog
import com.ganguydhrubo.aira.voice.ui.VoiceSource
import com.ganguydhrubo.aira.voice.ui.voiceOptions
import com.ganguydhrubo.aira.voice.model.VoiceCapability
import softassert.assertEquals
import softassert.assertFalse
import softassert.assertTrue
import org.junit.Test

/** 通话的听写只用云端:手机自带识别和本机语音包都不出现在通话的选择列表里,也不算通话能用的听写。 */
class CallListenOptionsTest {
    private val pack = VoiceModel(OnDeviceCatalog.SERVICE_ID, "sense", "SenseVoice Small", stt = SttBinding(SttProtocol.OnDevice, endpoint = "sense"))
    private val cloud = VoiceModel("stt-doubao", "doubao", "Doubao", stt = SttBinding(SttProtocol.DoubaoStreaming))
    private val snapshot = VoiceCatalogSnapshot(
        services = listOf(
            SttSelectionController.SYSTEM_STT_SERVICE, OnDeviceCatalog.SERVICE,
            VoiceService("stt-doubao", "Doubao", VoiceAccount.VoiceOwned("s")),
        ),
        models = listOf(SttSelectionController.SYSTEM_STT_MODEL, pack, cloud),
    )

    @Test fun callPickersNeverOfferVoicePacks() {
        assertEquals(listOf("doubao"), callListenOptions(snapshot, emptyList()).map { it.ref.modelId })
    }

    @Test fun voicePackSelectionIsNotACallListenModel() {
        assertTrue(VoiceSelections(stt = SttSelection.Model(OnDeviceCatalog.ref("sense"))).listenUnusableForCalls())
        assertFalse(VoiceSelections(stt = SttSelection.Model(cloud.let { com.ganguydhrubo.aira.voice.model.VoiceModelRef(it.serviceId, it.id, VoiceCapability.STT) })).listenUnusableForCalls())
    }

    @Test fun voicePacksAreLabelledOnDeviceOffline() {
        val option = voiceOptions(VoiceCapability.STT, snapshot, emptyList()).single { it.ref.serviceId == OnDeviceCatalog.SERVICE_ID }
        assertEquals(VoiceSource.OnDevice, option.source)
    }
}
