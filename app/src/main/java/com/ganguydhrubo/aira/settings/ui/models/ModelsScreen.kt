package com.ganguydhrubo.aira.settings.ui.models

import com.ganguydhrubo.aira.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ganguydhrubo.aira.settings.Provider
import com.ganguydhrubo.aira.settings.ProviderStore
import com.ganguydhrubo.aira.ui.components.Hairline
import com.ganguydhrubo.aira.ui.components.PageBackground
import com.ganguydhrubo.aira.ui.components.RowChevron
import com.ganguydhrubo.aira.ui.components.TopBar
import com.ganguydhrubo.aira.ui.theme.AiraIcons
import com.ganguydhrubo.aira.ui.theme.AiraShapes
import com.ganguydhrubo.aira.ui.theme.AiraSize
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import com.ganguydhrubo.aira.ui.theme.AiraType

/** 设置 › 模型:和上手时的配置模型是同一页([ModelSetupScreen]),不在引导流程里。 */
@Composable
fun ModelsScreen(
    store: ProviderStore, onBack: () -> Unit,
    onOpenSpeech: (() -> Unit)? = null,
    onOpenVoice: (() -> Unit)? = null,
    onOpenImage: (() -> Unit)? = null,
    onOpenVideo: (() -> Unit)? = null,
    onOpenUsage: (() -> Unit)? = null,
    onOpenVoiceCatalog: (() -> Unit)? = null,
    onOpenCallMode: (() -> Unit)? = null,
) {
    ModelSetupScreen(store, onboarding = false, onBack = onBack, onDone = onBack, onOpenSpeech = onOpenSpeech, onOpenVoice = onOpenVoice, onOpenImage = onOpenImage, onOpenVideo = onOpenVideo, onOpenUsage = onOpenUsage, onOpenVoiceCatalog = onOpenVoiceCatalog, onOpenCallMode = onOpenCallMode)
}

/** 子页面共用的骨架:顶栏 + 可滚动内容。 */
@Composable
internal fun Page(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    val scrollState = rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) {
        androidx.compose.foundation.ScrollState(0)
    }
    PageBackground {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars).windowInsetsPadding(WindowInsets.ime)) {
            TopBar(title, AiraIcons.Back, onBack, stringResource(R.string.back))
            Column(Modifier.weight(1f).verticalScroll(scrollState).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
                content()
            }
        }
    }
}

/** 用途卡:大图标块、「名字 · 做什么」、选了什么(模型名用等宽字)、说明。 */
@Composable
internal fun RoleCard(icon: ImageVector, label: String, line: ModelsCopy.RoleLine, first: Boolean = false, tag: String, onClick: () -> Unit) {
    val c = AiraTheme.colors
    Column {
        if (!first) Hairline(start = 0.dp)
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(tag).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(40.dp).background(c.neutralSoft, RoleTileShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(21.dp), tint = c.ink)
            }
            Column(Modifier.weight(1f)) {
                Text(label, style = AiraType.Hint, color = c.ink3)
                Text(
                    line.title, Modifier.padding(top = 1.dp), style = AiraType.BodyStrong,
                    fontFamily = if (line.isModel) FontFamily.Monospace else null, color = if (line.unset) c.ink3 else c.ink,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                if (line.subtitle.isNotEmpty()) {
                    Text(
                        line.subtitle, Modifier.padding(top = 1.dp), style = AiraType.Caption,
                        color = if (line.subtitleOk) c.ok else c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            RowChevron()
        }
    }
}

private val RoleTileShape = AiraShapes.Chip

/** 服务商一行:左边是名字首字的图标块;[warning] 是要提醒的一句(用提醒色另起一行)。 */
@Composable
internal fun ProviderRow(p: Provider, line: String, first: Boolean, warning: String? = null, onClick: () -> Unit) {
    val c = AiraTheme.colors
    val shownName = ModelsCopy.providerName(p)
    Column {
        if (!first) Hairline()
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("provider-${p.name}").padding(horizontal = 16.dp).padding(vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(AiraSize.Tile).background(c.neutralSoft, AiraShapes.Tile), contentAlignment = Alignment.Center) {
                Text(shownName.take(1).uppercase(), style = AiraType.Caption.copy(fontWeight = FontWeight.Bold), color = c.ink)
            }
            Column(Modifier.weight(1f)) {
                Text(shownName, style = AiraType.Body, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val poolCount = com.ganguydhrubo.aira.core.model.KeyPoolManager.shared.keyCount(p.id)
                Text(KeyPoolCopy.providerLine(line, poolCount), Modifier.padding(top = 2.dp), style = AiraType.Caption, color = if (poolCount > 1) c.ok else c.ink3)
                warning?.let { Text(it, Modifier.padding(top = 2.dp).testTag("provider-warning-${p.name}"), style = AiraType.Caption, color = c.warn) }
            }
            RowChevron()
        }
    }
}

// ---------- 选某个用途用哪个模型 ----------
