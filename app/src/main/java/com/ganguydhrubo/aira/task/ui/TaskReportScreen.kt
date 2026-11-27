package com.ganguydhrubo.aira.task.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.ui.components.*
import com.ganguydhrubo.aira.ui.theme.AiraIcons
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import com.ganguydhrubo.aira.ui.theme.AiraType
import com.ganguydhrubo.aira.ui.theme.AiraRadius
import com.ganguydhrubo.aira.ui.theme.AiraShapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.ganguydhrubo.aira.memory.ui.Row
import com.ganguydhrubo.aira.ui.MarkdownText
import com.ganguydhrubo.aira.ui.Route

@Composable
internal fun TaskReportEntries(onReport: () -> Unit, onReplay: () -> Unit) {
    val c = AiraTheme.colors
    Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth().clip(AiraShapes.Card).background(c.surface)
            .clickable(onClick = onReport).padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(AiraIcons.Doc, null, Modifier.size(26.dp), tint = c.accent)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.report_title), style = AiraType.BodyStrong, color = c.ink)
                Text(stringResource(R.string.report_entry_hint), style = AiraType.Hint, color = c.ink2)
            }
            Icon(AiraIcons.Chevron, null, Modifier.size(16.dp), tint = c.ink3)
        }
        Row(Modifier.fillMaxWidth().clip(AiraShapes.Card).clickable(onClick = onReplay)
            .heightIn(min = 48.dp).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(AiraIcons.Route, null, Modifier.size(18.dp), tint = c.accent)
            Text(stringResource(R.string.playback_title), Modifier.weight(1f), style = AiraType.Caption, color = c.accent)
            Icon(AiraIcons.Chevron, null, Modifier.size(16.dp), tint = c.ink3)
        }
    }
}

@Composable
internal fun rememberReport(vm: TaskReportViewModel): Pair<Boolean, TaskReport?> {
    var loaded by remember(vm) { mutableStateOf(false) }
    var report by remember(vm) { mutableStateOf<TaskReport?>(null) }
    LaunchedEffect(vm) { report = withContext(Dispatchers.IO) { vm.load() }; loaded = true }
    return loaded to report
}

@Composable
fun TaskReportScreen(vm: TaskReportViewModel, onBack: () -> Unit, onReplay: (Int?) -> Unit, onTimeline: () -> Unit) {
    BackHandler(onBack = onBack)
    val (loaded, report) = rememberReport(vm)
    val c = AiraTheme.colors
    PageBackground {
        Column(Modifier.fillMaxSize()) {
            TopBar(stringResource(R.string.report_title), AiraIcons.Back, onBack, stringResource(R.string.back),
                right = { ReportExportMenu(vm.runId, report) })
            val r = report
            if (r == null) {
                if (loaded) EmptyNote(TimelineUi.EXPIRED_TEXT, Modifier.padding(top = 70.dp))
            } else LazyColumn(Modifier.fillMaxSize().testTag("report-scroll"),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item("document") {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(AiraRadius.Bubble)).background(c.surface).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text(r.header.goal, style = AiraType.Title.copy(fontWeight = FontWeight.Bold), color = c.ink)
                        Row(Modifier.fillMaxWidth().clip(AiraShapes.Tile).background(c.accentSoft).height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(3.dp).fillMaxHeight().background(c.accent))
                            Text(r.header.outcome, Modifier.weight(1f).padding(12.dp), style = AiraType.Label, color = c.accent)
                            r.header.duration?.let { Text(it, Modifier.padding(end = 12.dp), style = AiraType.Hint, color = c.ink2) }
                        }
                        if (r.header.criteria.isNotEmpty()) Text(r.header.criteria.joinToString(" · "), style = AiraType.Caption, color = c.ink2)
                        if (r.answer.isBlank()) Text(stringResource(R.string.report_no_answer), style = AiraType.Body, color = c.ink2)
                        else MarkdownText(TaskReportExport.displayMarkdown(r.answer, r.header.goal), AiraType.Body.copy(color = c.ink, lineHeight = 25.sp), Modifier.fillMaxWidth(), reportStyle = true)
                    }
                }
                if (r.pages.isNotEmpty()) {
                    item("screens-heading") { Text(stringResource(R.string.report_screens), style = AiraType.BodyStrong, color = c.ink) }
                    itemsIndexed(r.pages, key = { _, p -> "page-${p.screenshotId}" }) { _, page ->
                        Row(Modifier.fillMaxWidth().clip(AiraShapes.Card).background(c.surface)
                            .clickable { onReplay(page.screenshotId) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StaticPlaybackImage(vm, page.screenshotId, Modifier.size(46.dp, 72.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(page.title, style = AiraType.BodyStrong, color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(stringResource(R.string.report_view_step, page.stepIndex + 1), style = AiraType.Hint, color = c.ink2)
                            }
                            Icon(AiraIcons.Chevron, null, Modifier.size(14.dp), tint = c.ink3)
                        }
                    }
                }
                item("replay") { AiraButton(stringResource(R.string.playback_title), { onReplay(null) }, Modifier.fillMaxWidth(), kind = ButtonKind.Primary) }
                item("timeline") { AiraButton(stringResource(R.string.chat_view_process), onTimeline, Modifier.fillMaxWidth(), kind = ButtonKind.Tonal, size = ButtonSize.Small) }
                item("privacy") { Text(stringResource(R.string.report_local_note), style = AiraType.Hint, color = c.ink2) }
            }
        }
    }
}
