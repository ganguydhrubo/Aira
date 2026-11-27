package com.ganguydhrubo.aira.voice.model.adapters

import android.content.Context
import com.ganguydhrubo.aira.core.model.TtsCapabilities
import com.ganguydhrubo.aira.core.model.TtsModel
import com.ganguydhrubo.aira.i18n.AppLanguage
import com.ganguydhrubo.aira.voice.AndroidSystemTtsEngine
import com.ganguydhrubo.aira.voice.SystemTtsEngine
import com.ganguydhrubo.aira.voice.SystemTtsModel
import com.ganguydhrubo.aira.voice.model.ResolvedVoiceModel
import com.ganguydhrubo.aira.voice.model.TtsProtocol
import com.ganguydhrubo.aira.voice.model.VoiceCapability
import com.ganguydhrubo.aira.voice.model.VoiceModelError
import java.io.File

/**
 * 内置 Android 系统 TTS:一次合成一个完整 WAV,非流式,能力声明不交给云端或第二套播放路径。
 * 用哪个引擎看绑定里的 [com.ganguydhrubo.aira.voice.model.TtsBinding.engine](空 = 手机默认引擎)。
 */
class AndroidSystemTtsAdapter(
    private val engineFactory: (context: Context, enginePackage: String?) -> SystemTtsEngine =
        { context, enginePackage -> AndroidSystemTtsEngine(context, enginePackage) },
) : TtsAdapter {
    override val protocol = TtsProtocol.AndroidSystem

    override fun create(context: Context, model: ResolvedVoiceModel, capabilities: TtsCapabilities): TtsModel {
        val binding = model.model.tts ?: throw VoiceModelError.Unsupported(VoiceCapability.TTS)
        if (binding.protocol != protocol) throw VoiceModelError.Unsupported(VoiceCapability.TTS, binding.protocol.name)
        val app = context.applicationContext
        val engine = binding.engine.trim().ifEmpty { null }
        return SystemTtsModel(
            context = app,
            engineFactory = { engineFactory(it, engine) },
            resolvedLanguage = { AppLanguage.resolved(app) },
            outputDir = { File(app.cacheDir, "voice") },
            maxChars = capabilities.maxChars,
        )
    }
}
