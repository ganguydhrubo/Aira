package com.ganguydhrubo.aira.mcp.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.core.cap.mcp.McpPreset
import com.ganguydhrubo.aira.core.cap.mcp.McpPresetField
import com.ganguydhrubo.aira.i18n.AppLanguage
import com.ganguydhrubo.aira.ui.components.ButtonKind
import com.ganguydhrubo.aira.ui.components.FooterNote
import com.ganguydhrubo.aira.ui.components.GroupCard
import com.ganguydhrubo.aira.ui.components.GroupLabel
import com.ganguydhrubo.aira.ui.components.InputRow
import com.ganguydhrubo.aira.ui.components.ListRow
import com.ganguydhrubo.aira.ui.components.AiraButton
import com.ganguydhrubo.aira.ui.components.PageBackground
import com.ganguydhrubo.aira.ui.components.RowChevron
import com.ganguydhrubo.aira.ui.components.TileTone
import com.ganguydhrubo.aira.ui.components.TopBar
import com.ganguydhrubo.aira.ui.theme.AiraIcons
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import com.ganguydhrubo.aira.ui.theme.AiraType

/** 设置 › MCP 服务器 › 引导式连接:只呈现预设声明的字段,连接/授权/确认包/成功/失败按 [McpConnectorState]。 */
@Composable
fun McpConnectScreen(
    connector: McpConnectorState,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val stage by connector.stage.collectAsState()
    val context = LocalContext.current
    val langTag = remember(context) { AppLanguage.resolved(context).tag ?: "en" }
    val openBrowser: (String) -> Unit = remember(context) { { link ->
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(link))
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } }

    when (val s = stage) {
        McpConnectStage.Browsing -> Unit
        is McpConnectStage.Form -> ConnectForm(s.preset, s, langTag, openBrowser, connector, onBack)
        is McpConnectStage.Connected -> ConnectSuccess(s.tools, onDone, onBack)
    }
}

@Composable
private fun ConnectForm(
    preset: McpPreset,
    form: McpConnectStage.Form,
    langTag: String,
    openBrowser: (String) -> Unit,
    connector: McpConnectorState,
    onBack: () -> Unit,
) {
    val c = AiraTheme.colors
    val category = McpCategory.of(preset)
    val authLabel = stringResource(
        when {
            preset.transport == "stdio" -> R.string.mcp_connect_auth_stdio
            preset.auth == "oauth" -> R.string.mcp_connect_auth_oauth
            preset.auth == "fields" -> R.string.mcp_connect_auth_fields
            else -> R.string.mcp_connect_auth_none
        },
    )
    val spec = preset.packageSpec
    val needsConsent = form.needsPackageConsent && spec != null
    val connecting = form.connecting

    fun value(field: McpPresetField): String =
        if (field.secret) form.secretValues[field.id].orEmpty() else form.values[field.id].orEmpty()

    PageBackground {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars).windowInsetsPadding(WindowInsets.ime)) {
            TopBar(preset.name, AiraIcons.Back, onBack, stringResource(R.string.back))
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            ) {
                GroupCard(Modifier.padding(top = 8.dp)) {
                    ListRow(
                        McpCategory.icon(category), authLabel, subtitle = preset.summary.resolve(langTag), first = true,
                    )
                }
                if (preset.fields.isNotEmpty()) {
                    GroupCard {
                        preset.fields.forEachIndexed { i, field ->
                            InputRow(
                                label = field.label.resolve(langTag),
                                value = value(field),
                                onChange = { connector.updateField(field.id, it) },
                                placeholder = field.help?.resolve(langTag).orEmpty(),
                                first = i == 0,
                                secret = field.secret,
                                modifier = Modifier.testTag("mcp-field-${field.id}"),
                            )
                        }
                    }
                }
                if (needsConsent) {
                    GroupCard {
                        ListRow(
                            AiraIcons.Download, stringResource(R.string.mcp_connect_package_title),
                            subtitle = "${spec.name} ${spec.version}", first = true, tone = TileTone.Warn,
                        )
                        FooterNote(stringResource(R.string.mcp_connect_package_body, spec.name, spec.version, spec.source))
                    }
                }

                val primaryLabel = stringResource(
                    when {
                        needsConsent -> R.string.mcp_connect_package_confirm
                        preset.auth == "oauth" -> R.string.mcp_connect_authorize
                        form.error != null -> R.string.mcp_connect_retry
                        else -> R.string.mcp_connect_connect
                    },
                )
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (connecting) {
                        AiraButton(stringResource(R.string.mcp_connect_cancel), { connector.cancel() }, Modifier.weight(1f), kind = ButtonKind.Secondary)
                    } else {
                        AiraButton(
                            primaryLabel,
                            {
                                when {
                                    needsConsent -> connector.confirmPackage()
                                    preset.auth == "oauth" -> connector.authorize(openBrowser)
                                    else -> connector.connect()
                                }
                            },
                            Modifier.weight(1f).testTag("mcp-connect-action"),
                        )
                    }
                }
                if (connecting) {
                    Text(
                        stringResource(R.string.mcp_connect_connecting), Modifier.padding(top = 8.dp),
                        style = AiraType.Caption, color = c.ink3,
                    )
                } else if (form.error != null) {
                    Text(form.error, Modifier.padding(top = 8.dp), style = AiraType.Caption, color = c.bad)
                }

                GroupLabel(stringResource(R.string.mcp_connect_about))
                GroupCard {
                    ListRow(
                        AiraIcons.Info, stringResource(R.string.mcp_connect_official), first = true,
                        titleColor = c.accent, onClick = { openBrowser(preset.officialSource) },
                    ) { RowChevron() }
                    if (preset.auth == "fields") {
                        ListRow(
                            AiraIcons.Key, stringResource(R.string.mcp_connect_help),
                            titleColor = c.accent, onClick = { openBrowser(preset.officialSource) },
                        ) { RowChevron() }
                    }
                }
                FooterNote(stringResource(R.string.mcp_connect_data_note))
            }
        }
    }
}

@Composable
private fun ConnectSuccess(tools: List<String>, onDone: () -> Unit, onBack: () -> Unit) {
    var showTools by remember { mutableStateOf(false) }
    PageBackground {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
            TopBar(stringResource(R.string.mcp_connect_success_title), AiraIcons.Back, onBack, stringResource(R.string.back))
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            ) {
                GroupCard(Modifier.padding(top = 8.dp)) {
                    ListRow(
                        AiraIcons.Check, stringResource(R.string.mcp_connect_success_title),
                        subtitle = stringResource(R.string.mcp_connect_success_tools, tools.size), first = true, tone = TileTone.Ok,
                    )
                }
                GroupCard {
                    ListRow(
                        AiraIcons.ListChecks, stringResource(R.string.mcp_connect_view_tools), first = true,
                        onClick = { showTools = !showTools },
                    ) { RowChevron() }
                    if (showTools) tools.forEach { tool -> ListRow(null, tool) }
                }
                AiraButton(stringResource(R.string.mcp_connect_done), onDone, Modifier.fillMaxWidth().padding(top = 12.dp))
            }
        }
    }
}
