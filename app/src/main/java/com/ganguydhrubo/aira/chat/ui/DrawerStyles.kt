package com.ganguydhrubo.aira.chat.ui


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.foundation.layout.size

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.ganguydhrubo.aira.ui.theme.AiraType
import com.ganguydhrubo.aira.ui.theme.AiraShapes

internal const val MENU_IN_MS = 200
internal const val MENU_OUT_MS = 140
internal val DrawerWidth = 360.dp

/** 卡片先纵向缩短,轮廓在屏幕上仍保持圆弧,不把上下角压成椭圆。 */
internal class DrawerCardShape(private val progress: Float) : Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = with(density) { 32.dp.toPx() } * progress
        return Outline.Rounded(RoundRect(
            rect = Rect(Offset.Zero, size),
            cornerRadius = CornerRadius(radius, radius / (1f - 0.04f * progress)),
        ))
    }
}
internal val NavShape = AiraShapes.Chip
internal val SectionText = AiraType.Hint.copy(fontWeight = FontWeight.Medium)
// 标题用常规字重,一列看下来不发黑;只有当前对话加粗
internal val ConversationTitle = AiraType.Body.copy(fontSize = 15.sp, letterSpacing = 0.1.sp)
internal val ConversationTitleCurrent = ConversationTitle.copy(fontWeight = FontWeight.Medium)
internal val ConversationTime = AiraType.Hint.copy(fontSize = 11.5.sp)
internal val ConversationTag = AiraType.Micro.copy(fontWeight = FontWeight.Medium)
