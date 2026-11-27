package com.ganguydhrubo.aira.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 词标「Aira」统一用这个字重:比标题轻一档,顶栏、侧栏、回答头看起来是同一个名字。 */
val WordmarkWeight = FontWeight.Medium

/** 字号与字重。只定大小、字重、行高,颜色由用的地方给。 */
object AiraType {
    /** 大标题:空状态、欢迎页。 */
    val LargeTitle = TextStyle(fontSize = 28.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold)
    /** 页面标题:顶栏。 */
    val Title = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
    /** 正文、行标题。 */
    val Body = TextStyle(fontSize = 15.5.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal)
    /** 卡片标题等需要强调的正文。 */
    val BodyStrong = TextStyle(fontSize = 15.5.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
    /** 说明文字。 */
    val Caption = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)
    /** 分组小标题。 */
    val Label = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
    /** 提示、页脚。 */
    val Hint = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Normal)
    /** 行尾的值。 */
    val Value = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal)
    /** 徽标、标签、极小的计数。 */
    val Micro = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Normal)
    /** 超大数字:总量、验证码等展示用大数字。 */
    val Display = TextStyle(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
}

/** 间距。页面左右留 [Page]。 */
object AiraSpace {
    val S4 = 4.dp
    val S8 = 8.dp
    val S12 = 12.dp
    val S16 = 16.dp
    val S20 = 20.dp
    val S24 = 24.dp
    val Page = 16.dp
}

/** 各类元素的尺寸。 */
object AiraSize {
    val TopBar = 56.dp
    val IconButton = 40.dp
    val RowMin = 52.dp
    val Button = 52.dp
    val Composer = 52.dp
    val Icon = 22.dp
    /** 列表行左边的图标块,里面的图形是 [TileIcon]。 */
    val Tile = 30.dp
    val TileIcon = 18.dp
    /** 行尾箭头等小图标。 */
    val SmallIcon = 16.dp
    const val IconStroke = 1.7f
}

/** 圆角。 */
object AiraRadius {
    /** 极小:徽标、圆点。 */
    val Micro = 4.dp
    val BubbleTail = 6.dp
    val Tile = 9.dp
    /** 小卡片、图片、芯片。 */
    val Chip = 12.dp
    val Button = 14.dp
    val Card = 16.dp
    val Bubble = 20.dp
    /** 聊天里用户、回答气泡的圆角。 */
    val ChatBubble = 16.dp
    /** 大卡片、对话框、底部弹窗。 */
    val Sheet = 24.dp
}

/** 常用形状;顶层常量,重组时不新建。 */
object AiraShapes {
    val Micro = RoundedCornerShape(AiraRadius.Micro)
    val Card = RoundedCornerShape(AiraRadius.Card)
    val Chip = RoundedCornerShape(AiraRadius.Chip)
    val Sheet = RoundedCornerShape(AiraRadius.Sheet)
    /** 底部弹窗:上面两个角圆、下面直角。 */
    val SheetTop = RoundedCornerShape(topStart = AiraRadius.Bubble, topEnd = AiraRadius.Bubble)
    /** 用户消息气泡:右下角是尾角。 */
    val UserBubble = RoundedCornerShape(AiraRadius.ChatBubble, AiraRadius.ChatBubble, AiraRadius.BubbleTail, AiraRadius.ChatBubble)
    /** Aira 的回答:左下角是尾角,和用户气泡对称。 */
    val AnswerBubble = RoundedCornerShape(AiraRadius.ChatBubble, AiraRadius.ChatBubble, AiraRadius.ChatBubble, AiraRadius.BubbleTail)
    /** Aira 连发时后面那几条:和上一条相连的左上角也收小。 */
    val AnswerBubbleJoined = RoundedCornerShape(AiraRadius.BubbleTail, AiraRadius.ChatBubble, AiraRadius.ChatBubble, AiraRadius.BubbleTail)
    val Button = RoundedCornerShape(AiraRadius.Button)
    val Tile = RoundedCornerShape(AiraRadius.Tile)
    /** 输入栏外壳:固定圆角,多行时圆角不变(不像 [Pill] 那样随高度变圆)。 */
    val Composer = RoundedCornerShape(AiraRadius.Sheet)
    val Pill = CircleShape
}
