package com.ganguydhrubo.aira.about.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.about.AiraLinks
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import com.ganguydhrubo.aira.ui.theme.AiraType

private val Orange = Color(0xFFE8772E)
private val Violet = Color(0xFF7B4FD6)
private val LinkedInBlue = Color(0xFF0A66C2)
private val TelegramBlue = Color(0xFF229ED9)

/** Founder card on the About screen: who made Aira, and three ways to reach him. */
@Composable
internal fun FounderCard(onOpenUrl: (String) -> Unit) {
    val c = AiraTheme.colors
    val shape = RoundedCornerShape(18.dp)
    Column(Modifier.fillMaxWidth().padding(bottom = 14.dp).testTag("founder-card")) {
        Row(
            Modifier.fillMaxWidth().clip(shape).background(c.surface).border(1.dp, c.hair, shape)
                .clickable { onOpenUrl(AiraLinks.LINKEDIN) }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Avatar()
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.about_founder_label), style = AiraType.Hint, fontWeight = FontWeight.Bold,
                    color = Orange, letterSpacing = 1.2.sp)
                Text("Dhrubo", style = AiraType.Title, fontWeight = FontWeight.Bold, color = c.ink)
                Text("Dhrubojyoti Gangopadhyay", style = AiraType.Caption, color = c.ink2)
                Row(Modifier.padding(top = 8.dp)) {
                    Box(Modifier.width(3.dp).height(36.dp).background(Orange.copy(alpha = 0.7f), RoundedCornerShape(2.dp)))
                    Text(stringResource(R.string.about_founder_quote), Modifier.padding(start = 8.dp),
                        style = AiraType.Caption, color = c.ink2)
                }
            }
        }
        Text(stringResource(R.string.about_founder_talk), Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp),
            style = AiraType.Hint, fontWeight = FontWeight.Bold, color = c.ink2, letterSpacing = 1.2.sp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ContactTile(LinkedInBlue, null, "in", "LinkedIn", "Dhrubojyoti Gangopadhyay", "contact-linkedin") { onOpenUrl(AiraLinks.LINKEDIN) }
            ContactTile(TelegramBlue, PlaneIcon, null, "Telegram", AiraLinks.TELEGRAM_HANDLE, "contact-telegram") { onOpenUrl(AiraLinks.TELEGRAM) }
            ContactTile(Orange, MailIcon, null, "Email", AiraLinks.EMAIL, "contact-email") { onOpenUrl(AiraLinks.MAILTO) }
        }
    }
}

@Composable
private fun Avatar() {
    Box(Modifier.size(68.dp)) {
        Box(
            Modifier.size(68.dp).clip(CircleShape)
                .background(Brush.sweepGradient(listOf(Orange, Violet, Color(0xFF4F7BFF), Orange))).padding(3.dp)
                .clip(CircleShape).background(AiraTheme.colors.surface).padding(3.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF9B6BF2), Violet, Color(0xFFB04FC9)))),
            contentAlignment = Alignment.Center,
        ) {
            Text("D", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).size(22.dp)
                .background(AiraTheme.colors.surface, RoundedCornerShape(6.dp)).padding(2.dp)
                .background(LinkedInBlue, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("in", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ContactTile(tint: Color, icon: ImageVector?, glyph: String?, title: String, sub: String, tag: String, onClick: () -> Unit) {
    val c = AiraTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(c.surface).border(1.dp, c.hair, shape)
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(40.dp).background(tint, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            if (icon != null) Icon(icon, null, Modifier.size(22.dp), tint = Color.White)
            if (glyph != null) Text(glyph, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = AiraType.Label, fontWeight = FontWeight.Bold, color = c.ink)
            Text(sub, style = AiraType.Caption, color = c.ink2, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private val PlaneIcon: ImageVector = ImageVector.Builder("plane", 24.dp, 24.dp, 24f, 24f).apply {
    addPath(addPathNodes("M21.5 4.2 L2.9 11.4 c-0.9 0.4 -0.9 1.6 0.1 1.9 l4.6 1.4 l1.8 5.6 c0.3 0.8 1.3 1 1.9 0.4 l2.6 -2.5 l4.7 3.4 c0.7 0.5 1.6 0.1 1.8 -0.7 l3.2 -15.4 c0.2 -1 -0.8 -1.7 -1.7 -1.3 z M9.6 14.4 l8.3 -6.2 l-6.6 7.2 l-0.4 3 z"),
        fill = SolidColor(Color.Black))
}.build()

private val MailIcon: ImageVector = ImageVector.Builder("mail", 24.dp, 24.dp, 24f, 24f).apply {
    addPath(addPathNodes("M4 6 h16 a1 1 0 0 1 1 1 v10 a1 1 0 0 1 -1 1 h-16 a1 1 0 0 1 -1 -1 v-10 a1 1 0 0 1 1 -1 z M3.5 7 l8.5 6 l8.5 -6"),
        stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
}.build()
