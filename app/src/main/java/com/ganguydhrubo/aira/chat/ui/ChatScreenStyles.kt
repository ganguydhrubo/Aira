package com.ganguydhrubo.aira.chat.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganguydhrubo.aira.ui.theme.AiraShapes
import com.ganguydhrubo.aira.ui.theme.AiraType

/** 跳转目标行高亮停留多久(毫秒)。 */
internal const val JUMP_HIGHLIGHT_MS = 1_800L

/** 竖线的位置(从左边算起);步骤点、转圈的中心都在这条线上。 */
internal val StepRail = 8.dp

/** 输入栏外壳的左右留白;空态的推荐卡片也按它和输入框左右对齐。 */
internal val ComposerInset = 12.dp

internal val FileTileShape = AiraShapes.Tile
internal val EmptyLead = AiraType.Value
/** 空对话的问候:比大标题轻一档(中粗),字距略收。 */
internal val EmptyGreeting = AiraType.LargeTitle.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, letterSpacing = (-0.3).sp)
internal val BubbleText = AiraType.Body.copy(fontSize = 15.sp)
/** 回答:正文字号,两端对齐(中文整段行尾整齐,最后一行除外)。 */
/** Aira 回答的正文:左对齐(两端对齐会把字撑开)、行高约 1.55 倍(段内紧凑,段与段靠段距拉开)、字距略松。 */
internal val AnswerText = AiraType.Body.copy(fontSize = 15.sp, lineHeight = 23.5.sp, letterSpacing = 0.2.sp, textAlign = TextAlign.Start)
internal val NowText = AiraType.Value
internal val ClockText = AiraType.Hint.copy(fontFeatureSettings = "tnum")
internal val StepText = AiraType.Caption
internal val StepCurrentText = AiraType.Caption.copy(fontWeight = FontWeight.Medium)
/** 插话收成的小字:14sp,比正文淡一点点。 */
internal val SupplementText = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal)
/** 干活中插话的右侧胶囊:比用户气泡小一圈,右下尾角。 */
internal val SteerPillShape = RoundedCornerShape(14.dp, 14.dp, 5.dp, 14.dp)
internal val NoteText = AiraType.Hint
internal val DoneText = AiraType.Caption
internal val NeedTitle = AiraType.Value.copy(fontWeight = FontWeight.SemiBold)
internal val CardBody = AiraType.Body
private val PillText = AiraType.Caption
private val ChipText = AiraType.Hint.copy(fontWeight = FontWeight.Medium)
internal val FileName = AiraType.Body.copy(fontWeight = FontWeight.Medium)
internal val FileMeta = AiraType.Hint

/**
 * 聊天回答的 Markdown 排版:正文深灰、加粗纯黑,段距 14(比行距多出约半行),标题不放大,列表点淡,
 * 引用和行内代码是比回答气泡深一级的浅色底。
 */
internal fun chatMarkdownLook(c: com.ganguydhrubo.aira.ui.theme.AiraColors): com.ganguydhrubo.aira.ui.MarkdownLook {
    val deeper = if (c.isDark) androidx.compose.ui.graphics.Color(0x14FFFFFF) else androidx.compose.ui.graphics.Color(0xFFE9EBEF)
    return com.ganguydhrubo.aira.ui.MarkdownLook(
        gap = 14,
        flatHeadings = true,
        bodyInk = if (c.isDark) androidx.compose.ui.graphics.Color(0xFFE2E2E6) else androidx.compose.ui.graphics.Color(0xFF2B2E34),
        strongInk = c.ink,
        markerInk = c.ink3,
        quoteFill = deeper,
        codeFill = deeper,
    )
}
