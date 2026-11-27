package com.ganguydhrubo.aira.voice.ondevice

import com.ganguydhrubo.aira.voice.model.SttBinding
import com.ganguydhrubo.aira.voice.model.SttProtocol
import com.ganguydhrubo.aira.voice.model.VoiceAccount
import com.ganguydhrubo.aira.voice.model.VoiceCapability
import com.ganguydhrubo.aira.voice.model.VoiceModel
import com.ganguydhrubo.aira.voice.model.VoiceModelOrigin
import com.ganguydhrubo.aira.voice.model.VoiceModelRef
import com.ganguydhrubo.aira.voice.model.VoiceService

/**
 * 已安装的语音包在语音目录里的样子:固定服务 [SERVICE] 下,每个包一个模型(模型 id 就是包 id)。
 * 和其他模型一样存进目录快照(不像 android-system 那样每次临时生成),备援链、整理规则都不用特殊处理;
 * 文件不在时模型和选择照留,用到时报 [com.ganguydhrubo.aira.voice.model.VoiceModelError.PackMissing]。
 */
object OnDeviceCatalog {
    const val SERVICE_ID = "on-device"
    val SERVICE = VoiceService(SERVICE_ID, "On-device", VoiceAccount.None)

    fun model(entry: VoicePackEntry): VoiceModel = VoiceModel(
        serviceId = SERVICE_ID,
        id = entry.id,
        displayName = entry.displayName,
        stt = SttBinding(SttProtocol.OnDevice, endpoint = entry.id),
        origin = VoiceModelOrigin.Builtin,
    )

    fun ref(packId: String): VoiceModelRef = VoiceModelRef(SERVICE_ID, packId, VoiceCapability.STT)

    fun isOnDevice(ref: VoiceModelRef): Boolean = ref.serviceId == SERVICE_ID
}
