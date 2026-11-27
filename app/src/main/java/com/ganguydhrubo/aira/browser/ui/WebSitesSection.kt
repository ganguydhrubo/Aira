package com.ganguydhrubo.aira.browser.ui

import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.i18n.tr
import androidx.compose.foundation.layout.Column
import com.ganguydhrubo.aira.ui.components.AiraDialog
import com.ganguydhrubo.aira.ui.components.AiraNoticeDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ganguydhrubo.aira.cap.web.SiteEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.ganguydhrubo.aira.i18n.uiLocale
import com.ganguydhrubo.aira.ui.components.GroupCard
import com.ganguydhrubo.aira.ui.components.GroupLabel
import com.ganguydhrubo.aira.ui.components.ListRow
import com.ganguydhrubo.aira.ui.components.RowValue
import com.ganguydhrubo.aira.ui.components.ValueTone
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import com.ganguydhrubo.aira.ui.theme.AiraIcons
import com.ganguydhrubo.aira.ui.components.TileTone
import com.ganguydhrubo.aira.ui.components.FooterNote
import com.ganguydhrubo.aira.memory.ui.Link

/**
 * 设置页「网站登录」一节的数据与操作。[onOpenLogin] 为 null 时不提供「打开登录」。
 * [onLogout] 返回过期掉的 Cookie 个数。
 */
class WebSitesState(
    val sites: List<SiteEntry>,
    val onOpenLogin: ((String) -> Unit)?,
    val onLogout: (String) -> Int,
    val onLogoutAll: () -> Unit,
)

val WEB_LOGOUT_PARTIAL: String get() = tr(R.string.web_logout_partial)
val WEB_CHALLENGED_NOTE: String get() = tr(R.string.web_challenged_note)
val WEB_DATA_NOTE: String get() = tr(R.string.web_data_note)

/** 设置页的「网站登录」:列出内置浏览器访问过的网站(登录过的在前),可以打开登录、退出单个网站、全部退出。 */
@Composable
fun WebSitesSection(state: WebSitesState, labeled: Boolean = true) {
    var picked by remember { mutableStateOf<SiteEntry?>(null) }
    var confirmAll by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }

    if (labeled) GroupLabel(tr(R.string.settings_web_logins))
    GroupCard {
        if (state.sites.isEmpty()) {
            ListRow(AiraIcons.Globe, tr(R.string.web_none), subtitle = tr(R.string.web_none_sub), first = true)
        }
        val now = System.currentTimeMillis()
        state.sites.forEachIndexed { i, s ->
            ListRow(
                AiraIcons.Globe, s.host,
                subtitle = listOfNotNull(
                    tr(R.string.web_signed_in_by_you).takeIf { s.loggedInByUser },
                    tr(R.string.web_challenged).takeIf { s.challenged(now) },
                    tr(R.string.web_last_visit, day(s.lastVisitMs)),
                ).joinToString(" · "),
                first = i == 0,
                onClick = { picked = s },
            ) { RowValue(tr(R.string.web_manage), ValueTone.Link) }
        }
        ListRow(
            AiraIcons.Close, tr(R.string.web_logout_all_title), titleColor = AiraTheme.colors.bad, tone = TileTone.Bad,
            onClick = { confirmAll = true },
        )
    }
    FooterNote(WEB_DATA_NOTE)

    picked?.let { s ->
        AiraDialog(
            onDismissRequest = { picked = null },
            title = { Text(s.host) },
            text = {
                val base = if (s.loggedInByUser) tr(R.string.web_you_signed_in) else tr(R.string.web_aira_visited)
                Text(if (s.challenged(System.currentTimeMillis())) "$base\n\n$WEB_CHALLENGED_NOTE" else base)
            },
            confirmButton = {
                Column {
                    state.onOpenLogin?.let { open ->
                        TextButton({ picked = null; open(s.host) }) { Text(tr(R.string.web_open_login)) }
                    }
                    val clearing = s.challenged(System.currentTimeMillis())
                    TextButton({
                        picked = null
                        state.onLogout(s.host)
                        notice = if (clearing) tr(R.string.web_cleared, s.host) else tr(R.string.web_signed_out, s.host, WEB_LOGOUT_PARTIAL)
                    }) { Text(if (clearing) tr(R.string.web_clear_site) else tr(R.string.web_sign_out_site), color = AiraTheme.colors.bad) }
                }
            },
            dismissButton = { TextButton({ picked = null }) { Text(tr(R.string.cancel)) } },
        )
    }

    if (confirmAll) {
        AiraDialog(
            onDismissRequest = { confirmAll = false },
            title = { Text(tr(R.string.web_logout_all_q)) },
            text = { Text(tr(R.string.web_logout_all_body)) },
            confirmButton = {
                TextButton({ confirmAll = false; state.onLogoutAll(); notice = tr(R.string.web_logged_out_all) }) {
                    Text(tr(R.string.web_logout_all), color = AiraTheme.colors.bad)
                }
            },
            dismissButton = { TextButton({ confirmAll = false }) { Text(tr(R.string.cancel)) } },
        )
    }

    notice?.let { n ->
        AiraNoticeDialog(n, onDismiss = { notice = null })
    }
}

private fun day(ms: Long): String = SimpleDateFormat(tr(R.string.date_pattern_md_compact), uiLocale()).format(Date(ms))
