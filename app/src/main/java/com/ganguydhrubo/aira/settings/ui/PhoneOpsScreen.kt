package com.ganguydhrubo.aira.settings.ui

import com.ganguydhrubo.aira.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ganguydhrubo.aira.core.device.shell.ShellTransport
import com.ganguydhrubo.aira.ui.components.SelectChip
import com.ganguydhrubo.aira.ui.components.AiraDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ganguydhrubo.aira.core.device.shell.BuiltinShellStatus
import com.ganguydhrubo.aira.settings.Prerequisites
import com.ganguydhrubo.aira.settings.Problem
import com.ganguydhrubo.aira.settings.SystemStatus
import com.ganguydhrubo.aira.ui.components.FooterNote
import com.ganguydhrubo.aira.ui.components.DetailNote
import com.ganguydhrubo.aira.ui.components.GroupCard
import com.ganguydhrubo.aira.ui.components.GroupLabel
import com.ganguydhrubo.aira.ui.components.ListRow
import com.ganguydhrubo.aira.ui.components.SegmentedControl
import com.ganguydhrubo.aira.ui.components.PageScaffold
import com.ganguydhrubo.aira.ui.components.RowChevron
import com.ganguydhrubo.aira.ui.components.RowValue
import com.ganguydhrubo.aira.ui.components.TileTone
import com.ganguydhrubo.aira.ui.components.ValueTone
import com.ganguydhrubo.aira.ui.components.focusHighlightTarget
import com.ganguydhrubo.aira.ui.theme.AiraIcons
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import com.ganguydhrubo.aira.memory.ui.Link

/** 设置 › 手机操作:权限与配对的状态,重新配对、解除配对。Root 能力集中在高级与其他的独立页面。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhoneOpsScreen(
    status: SystemStatus,
    androidRelease: String,
    onProblem: (Problem) -> Unit,
    onRepair: () -> Unit,
    onUnpair: () -> Unit,
    onBack: () -> Unit,
    onSelectTransport: (ShellTransport) -> Unit = {},
    onGrantShizuku: () -> Unit = {},
    onOpenShizukuGuide: () -> Unit = {},
    onRefreshShizuku: () -> Unit = {},
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onRefreshShizuku() }
    val check = Prerequisites.check(status, hasMainModel = true)
    var confirmUnpair by remember { mutableStateOf(false) }
    PageScaffold(stringResource(R.string.settings_phone_ops), onBack) {
        GroupLabel(stringResource(R.string.settings_status))
        GroupCard {
            val (a11yText, a11yTone) = StatusCopy.accessibilityValue(status.accessibility)
            ListRow(
                AiraIcons.Hand, stringResource(R.string.settings_a11y), first = true,
                onClick = check.problems.firstOrNull { it is Problem.Accessibility }?.let { { onProblem(it) } },
                modifier = Modifier.focusHighlightTarget(SettingsFocusKey.ACCESSIBILITY),
            ) { RowValue(a11yText, a11yTone) }
            // 安卓 12 以下手机功能不可用,不列这一行(系统版本那一行已说明)
            if (status.sdkInt >= Prerequisites.MIN_SDK) {
                val helper = status.builtinShell
                val (hText, hTone) = PhoneSetupCopy.statusValue(helper)
                val reconnect = PhoneSetupCopy.canReconnect(helper)
                ListRow(
                    AiraIcons.Link, PhoneSetupCopy.ROW_TITLE,
                    // 右边的值已说明状态;只有连不上时才在下面说原因
                    if (reconnect && helper is BuiltinShellStatus.Failed) PhoneSetupCopy.failure(helper.reason).title else null,
                    onClick = { onProblem(Problem.PhoneHelper(helper)) },
                ) { if (reconnect) RowValue(PhoneSetupCopy.RECONNECT, ValueTone.Link) else RowValue(hText, hTone) }
                val shizuku = status.shizuku
                val shizukuValue = stringResource(
                    when {
                        !shizuku.installed -> R.string.phone_shizuku_not_installed
                        !shizuku.running -> R.string.phone_shizuku_not_running
                        !shizuku.permissionGranted -> R.string.phone_shizuku_perm_needed
                        else -> R.string.phone_shizuku_ready
                    }
                )
                val shizukuTone = when {
                    shizuku.ready -> ValueTone.Ok
                    !shizuku.permissionGranted && shizuku.running -> ValueTone.Bad
                    status.shellTransport == ShellTransport.Shizuku -> ValueTone.Bad
                    else -> ValueTone.Plain
                }
                ListRow(
                    AiraIcons.Shield,
                    stringResource(R.string.phone_shizuku_group),
                    null,
                    onClick = if (shizuku.installed || shizuku.running) onGrantShizuku else onOpenShizukuGuide,
                    modifier = Modifier.testTag("phone-shizuku-status").focusHighlightTarget(SettingsFocusKey.SHIZUKU),
                ) { RowValue(shizukuValue, shizukuTone) }
            }
            val tooOld = status.sdkInt < Prerequisites.MIN_SDK
            val focusWarning = Problem.FocusMayLeak in check.problems
            ListRow(
                AiraIcons.Phone, stringResource(R.string.settings_system_version),
                when {
                    tooOld -> stringResource(R.string.settings_android, androidRelease)
                    focusWarning -> stringResource(R.string.settings_android_focus, androidRelease)
                    else -> null
                },
                onClick = when {
                    tooOld -> ({ onProblem(Problem.SystemTooOld) })
                    focusWarning -> ({ onProblem(Problem.FocusMayLeak) })
                    else -> null
                },
            ) {
                when {
                    tooOld -> RowValue(stringResource(R.string.pill_unsupported), ValueTone.Bad)
                    focusWarning -> RowValue(stringResource(R.string.pill_available), ValueTone.Plain)
                    else -> RowValue(stringResource(R.string.settings_android, androidRelease))
                }
            }
        }
        if (status.sdkInt >= Prerequisites.MIN_SDK) {
            GroupLabel(stringResource(R.string.phone_transport_title))
            GroupCard {
                Box(Modifier.fillMaxWidth().focusHighlightTarget(SettingsFocusKey.SHELL_TRANSPORT)) {
                    // 固定三选一,和其它页面一样用分段控件
                    val options = listOf(
                        ShellTransport.Auto to stringResource(R.string.phone_transport_auto),
                        ShellTransport.WirelessDebugging to stringResource(R.string.phone_transport_wireless),
                        ShellTransport.Shizuku to stringResource(R.string.phone_transport_shizuku),
                    )
                    SegmentedControl(
                        options.map { it.second },
                        options.indexOfFirst { it.first == status.shellTransport },
                        { onSelectTransport(options[it].first) },
                        Modifier.padding(12.dp),
                        tags = options.map { "phone-transport-${it.first.name}" },
                    )
                }
            }
            FooterNote(stringResource(R.string.phone_transport_sub))

            val shizuku = status.shizuku
            GroupLabel(stringResource(R.string.phone_shizuku_group))
            GroupCard {
                val actionTitle = stringResource(
                    if (shizuku.running && !shizuku.permissionGranted) R.string.phone_shizuku_grant
                    else R.string.phone_shizuku_open_app
                )
                ListRow(
                    AiraIcons.Shield,
                    actionTitle,
                    first = true,
                    onClick = if (shizuku.installed || shizuku.running) onGrantShizuku else onOpenShizukuGuide,
                    modifier = Modifier.testTag("phone-shizuku-action"),
                ) { RowChevron() }
                if (!shizuku.installed || !shizuku.running) {
                    ListRow(
                        AiraIcons.Book,
                        stringResource(R.string.phone_shizuku_guide),
                        onClick = onOpenShizukuGuide,
                        modifier = Modifier.testTag("phone-shizuku-guide"),
                    ) { RowChevron() }
                }
            }
            FooterNote(stringResource(R.string.phone_shizuku_footer))

            GroupLabel(stringResource(R.string.phone_channel))
            GroupCard {
                ListRow(AiraIcons.Link, PhoneSetupCopy.REPAIR, first = true, onClick = onRepair) {
                    RowChevron()
                }
                val paired = status.wirelessPaired ?: (status.builtinShell != BuiltinShellStatus.NotPaired)
                ListRow(
                    AiraIcons.Close, PhoneSetupCopy.UNPAIR,
                    titleColor = if (paired) AiraTheme.colors.bad else AiraTheme.colors.ink3,
                    tone = if (paired) TileTone.Bad else TileTone.Neutral,
                    onClick = if (paired) ({ confirmUnpair = true }) else null,
                )
            }
            DetailNote(
                PhoneSetupCopy.usageSummary(status.helperRestart),
                PhoneSetupCopy.usageNote(status.helperRestart),
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            )
        }
    }
    if (confirmUnpair) {
        AiraDialog(
            onDismissRequest = { confirmUnpair = false },
            title = { Text(PhoneSetupCopy.UNPAIR_TITLE) },
            text = { Text(PhoneSetupCopy.UNPAIR_BODY) },
            confirmButton = {
                TextButton({ confirmUnpair = false; onUnpair() }) { Text(PhoneSetupCopy.UNPAIR_CONFIRM, color = AiraTheme.colors.bad) }
            },
            dismissButton = { TextButton({ confirmUnpair = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

// ---------- 通知 ----------
