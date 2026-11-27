package com.ganguydhrubo.aira.voice.model.adapters

import android.content.Context
import com.ganguydhrubo.aira.core.model.TtsCapabilities
import com.ganguydhrubo.aira.core.model.TtsModel
import com.ganguydhrubo.aira.voice.PcmRecorder
import com.ganguydhrubo.aira.voice.ProviderSpeech
import com.ganguydhrubo.aira.voice.SpeechEngineSpec
import com.ganguydhrubo.aira.voice.SpeechSession
import com.ganguydhrubo.aira.voice.model.ResolvedVoiceModel
import com.ganguydhrubo.aira.voice.model.SttProtocol
import com.ganguydhrubo.aira.voice.model.TtsProtocol
import com.ganguydhrubo.aira.voice.model.VoiceCapability
import com.ganguydhrubo.aira.voice.model.VoiceCredentials
import com.ganguydhrubo.aira.voice.model.VoiceModelError
import com.ganguydhrubo.aira.voice.vendors.PieceSynthesizer
import com.ganguydhrubo.aira.voice.vendors.UtteranceRecognizer
import com.ganguydhrubo.aira.voice.vendors.VendorStt
import com.ganguydhrubo.aira.voice.vendors.VendorTts
import com.ganguydhrubo.aira.voice.vendors.VendorTtsModel

/** Recognition through a vendor's own API: records (or is fed) one utterance, uploads it on release. */
class VendorSttAdapter(
    override val protocol: SttProtocol,
    private val sessionFactory: SttSessionFactory? = null,
) : SttAdapter {
    init {
        require(protocol in PROTOCOLS) { "Not a vendor STT protocol" }
    }

    override fun spec(model: ResolvedVoiceModel): SpeechEngineSpec {
        model.sttBinding(protocol)
        return SpeechEngineSpec.Vendor(model.model.id)
    }

    override fun start(context: Context, model: ResolvedVoiceModel, language: String?, hooks: SttHooks): SpeechSession {
        spec(model)
        val tag = languageTag(context, language)
        return categorizedSession {
            sessionFactory?.invoke(context, model, tag, hooks) ?: run {
                val recognizer = recognizer(model)
                ProviderSpeech(hooks.recorder ?: PcmRecorder(), { wav -> recognizer.recognize(wav, tag) })
            }
        }
    }

    override fun utterance(context: Context, model: ResolvedVoiceModel, language: String?): UtteranceTranscribe {
        spec(model)
        val tag = languageTag(context, language)
        val recognizer = recognizer(model)
        return { wav -> recognizer.recognize(wav, tag) }
    }

    internal fun recognizer(model: ResolvedVoiceModel): UtteranceRecognizer {
        val binding = model.sttBinding(protocol)
        val c = model.account.credentials
        return when (protocol) {
            SttProtocol.ElevenLabsUpload -> VendorStt.elevenLabs(c.need { apiKey }, model.model.id)
            SttProtocol.DeepgramUpload -> VendorStt.deepgram(c.need { apiKey }, model.model.id)
            SttProtocol.AzureShortAudio -> VendorStt.azure(c.need { apiKey }, binding.region.needRegion())
            SttProtocol.TencentSentence -> VendorStt.tencent(c.need { appId }, c.need { apiKey }, model.model.id)
            SttProtocol.XfyunDictation -> VendorStt.xfyun(c.need { appId }, c.need { apiKey }, c.need { accessKey })
            else -> throw VoiceModelError.Unsupported(VoiceCapability.STT, protocol.name)
        }
    }

    companion object {
        val PROTOCOLS = setOf(
            SttProtocol.ElevenLabsUpload,
            SttProtocol.DeepgramUpload,
            SttProtocol.AzureShortAudio,
            SttProtocol.TencentSentence,
            SttProtocol.XfyunDictation,
        )
    }
}

/** Speech through a vendor's own API; long text is synthesized piece by piece and returned as one WAV. */
class VendorTtsAdapter(override val protocol: TtsProtocol) : TtsAdapter {
    init {
        require(protocol in PROTOCOLS) { "Not a vendor TTS protocol" }
    }

    override fun create(context: Context, model: ResolvedVoiceModel, capabilities: TtsCapabilities): TtsModel {
        val binding = model.ttsBinding(protocol)
        val c = model.account.credentials
        val id = model.model.id
        val (pieceChars, synth) = when (protocol) {
            TtsProtocol.VolcSpeech -> {
                if (!c.configured) throw VoiceModelError.MissingCredentials()
                300 to VendorTts.volc(binding.endpoint, binding.region.ifBlank { id }, c.apiKey, c.appId, c.accessKey)
            }
            TtsProtocol.BailianQwenTts -> 500 to VendorTts.qwen(binding.endpoint, id, c.need { apiKey })
            TtsProtocol.MiniMaxT2a -> 1000 to VendorTts.miniMax(binding.endpoint, id, c.need { apiKey })
            TtsProtocol.ElevenLabsSpeech -> 1500 to VendorTts.elevenLabs(id, c.need { apiKey })
            TtsProtocol.DeepgramSpeak -> 1500 to VendorTts.deepgram(c.need { apiKey })
            TtsProtocol.AzureSpeech -> 1000 to VendorTts.azure(binding.region.needRegion(), c.need { apiKey })
            TtsProtocol.TencentVoice -> 140 to VendorTts.tencent(c.need { appId }, c.need { apiKey })
            TtsProtocol.XfyunSynthesis -> 600 to VendorTts.xfyun(c.need { appId }, c.need { apiKey }, c.need { accessKey })
            else -> throw VoiceModelError.Unsupported(VoiceCapability.TTS, protocol.name)
        }
        val caps = capabilities.copy(voices = binding.voices.toSet().ifEmpty { null }, formats = setOf("wav"))
        return categorized(VendorTtsModel(id, caps, binding.voice, pieceChars, synth as PieceSynthesizer))
    }

    companion object {
        val PROTOCOLS = setOf(
            TtsProtocol.VolcSpeech,
            TtsProtocol.BailianQwenTts,
            TtsProtocol.MiniMaxT2a,
            TtsProtocol.ElevenLabsSpeech,
            TtsProtocol.DeepgramSpeak,
            TtsProtocol.AzureSpeech,
            TtsProtocol.TencentVoice,
            TtsProtocol.XfyunSynthesis,
        )
    }
}

private inline fun VoiceCredentials.need(field: VoiceCredentials.() -> String): String =
    field().ifBlank { throw VoiceModelError.MissingCredentials() }

private fun String.needRegion(): String = ifBlank { throw VoiceModelError.IncompleteConfig("Region is missing") }
