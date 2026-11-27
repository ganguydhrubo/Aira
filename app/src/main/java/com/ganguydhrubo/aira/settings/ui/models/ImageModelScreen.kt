package com.ganguydhrubo.aira.settings.ui.models

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.core.model.ImageAspect
import com.ganguydhrubo.aira.core.model.ImageRequest
import com.ganguydhrubo.aira.i18n.tr
import com.ganguydhrubo.aira.settings.ImageChoice
import com.ganguydhrubo.aira.settings.ImageModelSettings
import com.ganguydhrubo.aira.settings.ImageProtocol
import com.ganguydhrubo.aira.settings.ProviderConfig
import com.ganguydhrubo.aira.settings.ProviderStore
import com.ganguydhrubo.aira.settings.imageModel
import com.ganguydhrubo.aira.ui.components.FooterNote
import com.ganguydhrubo.aira.ui.components.GroupCard
import com.ganguydhrubo.aira.ui.components.GroupLabel
import com.ganguydhrubo.aira.ui.components.InputRow
import com.ganguydhrubo.aira.ui.components.ListRow
import com.ganguydhrubo.aira.ui.components.AiraButton
import com.ganguydhrubo.aira.ui.components.AiraSwitch
import com.ganguydhrubo.aira.ui.components.RadioRow
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import kotlinx.coroutines.launch

/**
 * 设置 › 模型 › 图片模型:选服务商、模型、接口,打开总开关;保存到相册另有开关和每次询问。
 * 不在 [com.ganguydhrubo.aira.core.model.ModelRole] 里:图片模型不参与对话、工具调用和上下文。
 */
@Composable
fun ImageModelScreen(store: ProviderStore, onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = remember { ImageModelSettings.get(context) }
    val state by settings.state.collectAsState()
    val providers by store.providers.collectAsState()
    val scope = rememberCoroutineScope()
    val c = AiraTheme.colors
    var model by remember { mutableStateOf(state.choice?.model.orEmpty()) }
    var test by remember { mutableStateOf<String?>(null) }
    val eligibleProviders = providers
    val choice = state.choice?.takeIf { selected -> eligibleProviders.any { it.id == selected.providerId } }

    Page(tr(R.string.role_image), onBack) {
        Text(tr(R.string.image_hint), Modifier.padding(top = 4.dp, bottom = 12.dp), fontSize = 14.5.sp, lineHeight = 22.sp, color = c.ink2)
        GroupCard {
            ListRow(
                null, tr(R.string.image_enable), note = tr(R.string.image_enable_note), first = true,
                trailing = { AiraSwitch(state.enabled, settings::setEnabled) },
            )
        }

        GroupLabel(tr(R.string.image_provider), Modifier.padding(top = 12.dp))
        if (eligibleProviders.isEmpty()) {
            FooterNote(tr(R.string.image_no_providers))
        } else {
            GroupCard {
                eligibleProviders.forEachIndexed { i, p ->
                    RadioRow(
                        ModelsCopy.providerName(p), selected = choice?.providerId == p.id, first = i == 0,
                        subtitle = p.baseUrl,
                        onClick = {
                            val protocol = if (p.kind == ProviderConfig.Kind.Gemini) ImageProtocol.GeminiImage else ImageProtocol.OpenAiImages
                            settings.setChoice(ImageChoice(p.id, model, protocol))
                            test = null
                        },
                    )
                }
            }
        }

        choice?.let { choice ->
            GroupCard {
                InputRow(
                    tr(R.string.image_model_name), model,
                    onChange = {
                        model = it
                        settings.setChoice(choice.copy(model = it))
                        test = null
                    },
                    placeholder = "gpt-image-1 / gemini-3-pro-image-preview", first = true,
                )
            }
            GroupLabel(tr(R.string.image_protocol))
            GroupCard {
                RadioRow(
                    tr(R.string.image_protocol_openai), choice.protocol == ImageProtocol.OpenAiImages, first = true,
                    onClick = { settings.setChoice(choice.copy(protocol = ImageProtocol.OpenAiImages)); test = null },
                )
                RadioRow(
                    tr(R.string.image_protocol_gemini), choice.protocol == ImageProtocol.GeminiImage,
                    onClick = { settings.setChoice(choice.copy(protocol = ImageProtocol.GeminiImage)); test = null },
                )
            }
            GroupCard {
                ListRow(
                    null, tr(R.string.image_supports_edit), first = true,
                    trailing = { AiraSwitch(state.supportsEdit, settings::setSupportsEdit) },
                )
            }
        }

        GroupLabel(tr(R.string.image_limits), Modifier.padding(top = 12.dp))
        GroupCard {
            InputRow(
                tr(R.string.image_max_per_task), state.maxPerTask.toString(),
                onChange = { it.toIntOrNull()?.let(settings::setMaxPerTask) }, placeholder = "8",
                keyboard = KeyboardType.Number, first = true,
            )
            ListRow(
                null, tr(R.string.image_save_enabled), note = tr(R.string.image_save_enabled_note),
                trailing = { AiraSwitch(state.saveEnabled, settings::setSaveEnabled) },
            )
            ListRow(
                null, tr(R.string.image_ask_before_save),
                trailing = { AiraSwitch(state.askBeforeSave, settings::setAskBeforeSave, enabled = state.saveEnabled) },
            )
            ListRow(
                null, tr(R.string.image_ask_before_generate),
                trailing = { AiraSwitch(state.askBeforeGenerate, settings::setAskBeforeGenerate) },
            )
        }

        AiraButton(
            tr(R.string.image_test),
            onClick = {
                val choice = choice ?: return@AiraButton
                val caps = settings.capabilities() ?: return@AiraButton
                val port = store.imageModel(choice, caps) ?: return@AiraButton
                scope.launch {
                    test = tr(R.string.image_testing)
                    test = try {
                        val response = port.generate(ImageRequest("A small red apple on a white table", ImageAspect.Square, 1))
                        if (response.images.isEmpty()) tr(R.string.image_test_failed, "no image") else tr(R.string.image_test_ok)
                    } catch (e: Exception) {
                        tr(R.string.image_test_failed, e.message ?: e.javaClass.simpleName)
                    }
                }
            },
            enabled = choice != null && model.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
        test?.let { FooterNote(it, Modifier.padding(top = 8.dp)) }
        FooterNote(tr(R.string.image_footer), Modifier.padding(top = 10.dp))
    }
}
