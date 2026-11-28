package com.ganguydhrubo.aira.settings.ui

import org.junit.Rule
import com.ganguydhrubo.aira.i18n.ChineseTexts
import com.ganguydhrubo.aira.core.agent.RunProgress
import com.ganguydhrubo.aira.core.agent.TaskState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import com.ganguydhrubo.aira.settings.AccessibilityState
import com.ganguydhrubo.aira.settings.Problem
import org.junit.Test
import com.ganguydhrubo.aira.chat.ui.ChatViewModel
import com.ganguydhrubo.aira.chat.ui.Exchange
import com.ganguydhrubo.aira.chat.ui.ExchangeStatus
import com.ganguydhrubo.aira.chat.ui.StepRow
import com.ganguydhrubo.aira.chat.ui.liveLine
import com.ganguydhrubo.aira.ui.firstThatStarts

class StatusCopyTest {
    @get:Rule val texts = ChineseTexts()

    // 频道设置页打不开时依次退回,第一个能开的就停
    @Test
    fun firstThatStartsFallsBack() {
        val tried = mutableListOf<String>()
        assertTrue(firstThatStarts(listOf("channel", "app")) { tried += it; it == "app" })
        assertEquals(listOf("channel", "app"), tried)
        tried.clear()
        assertTrue(firstThatStarts(listOf("channel", "app")) { tried += it; true })
        assertEquals(listOf("channel"), tried)
        assertFalse(firstThatStarts(listOf("channel", "app")) { false })
    }

    @Test
    fun unavailableAccessibilityDistinguishesEnabledFromOff() {
        assertEquals("Aira’s accessibility service is off. Turn on Aira under Accessibility in system settings.", StatusCopy.accessibilityUnavailable(enabled = false))
        val enabled = StatusCopy.accessibilityUnavailable(enabled = true)
        assertTrue(enabled.contains("On"))
        assertTrue(enabled.contains("关闭后重新开启"))
        assertTrue(enabled.contains("允许受限制的设置"))
        assertFalse(enabled.contains("没有开启"))
    }

    @Test
    fun restrictedAccessibilityPointsToAppInfoInsteadOfOnlyToggling() {
        val p = Problem.Accessibility(AccessibilityState.EnabledNotRunning, restrictedSettings = true)
        assertTrue(StatusCopy.banner(p).detail.contains("允许受限制的设置"))
        val e = StatusCopy.explain(p, "14")
        assertEquals(Fix.OpenAppInfo, e.fix)
        assertTrue(e.body.contains("应用信息"))
        assertTrue(e.body.contains("允许受限制的设置"))
    }

    @Test
    fun lockedAccessibilityKeepsTheUnlockGuidanceEvenWhenRestricted() {
        val p = Problem.Accessibility(
            AccessibilityState.EnabledNotRunning,
            userLocked = true,
            restrictedSettings = true,
        )
        assertTrue(StatusCopy.banner(p).detail.contains("解锁一次"))
        val e = StatusCopy.explain(p, "14")
        assertNull(e.fix)
        assertNull(e.action)
        assertTrue(e.body.contains("解锁一次"))
    }

    @Test
    fun paymentLinesOnePerField() {
        assertEquals("Item: 拿铁 大杯\nAmount: ¥18\nAddress: 公司前台", StatusCopy.paymentLines("拿铁 大杯", "¥18", "公司前台"))
        assertEquals("Amount: ¥18", StatusCopy.paymentLines(null, "¥18", " "))
        assertNull(StatusCopy.paymentLines(null, null, null))
    }

    // 照着记住的路线做、一次做几步时,卡片上写明在干什么
    @Test
    fun replayAndBatchCopy() {
        assertEquals("Replaying a known route (step 2/5)", StatusCopy.progress(RunProgress.Replay(2, 5)))
        assertEquals("Running 3 步", StatusCopy.progress(RunProgress.Batch(3)))
        assertEquals("已收集 · 12/36 · 2026-10-31", StatusCopy.progress(RunProgress.Task("已收集", 12, 36, "2026-10-31")))

        val ex = Exchange("查电量", emptyList(), ExchangeStatus.Working, live = true)
        val replaying = ChatViewModel.reduce(ex, TaskState.Running(0, "按以往的做法操作", progress = RunProgress.Replay(2, 5)))
        assertEquals("Replaying a known route (step 2/5)", liveLine(replaying))
        val batching = ChatViewModel.reduce(replaying, TaskState.Running(1, "一起做", "do_steps", RunProgress.Batch(3)))
        assertEquals("Running 3 步", liveLine(batching))
        // 步骤列表只列实际做过的事
        assertEquals(emptyList<StepRow>(), batching.steps)
    }

    @Test
    fun headsUpCopyForEachCombination() {
        val both = Problem.AttentionLowered(attention = true, result = true)
        val b = StatusCopy.banner(both)
        assertEquals("「Needs you” notifications won’t pop up", b.title)
        assertEquals(
            "When you need to take over or approve, these notifications now land quietly in the shade and are easy to miss. 「Task result”. Tap “去Open ” and turn on “Pop on screen”.\n" +
                "Settings › Apps › Aira › Notifications › Needs you › Pop on screen (some phones call it “Banner” or “Floating notification”)",
            b.detail,
        )
        assertEquals("Turn on", b.link)
        assertEquals(Fix.OpenAttentionChannel, StatusCopy.directFix(both))
        assertEquals(Fix.OpenAttentionChannel, StatusCopy.explain(both, "14").fix)

        val attentionOnly = StatusCopy.banner(Problem.AttentionLowered(true, false))
        assertFalse(attentionOnly.detail.contains("也一样"))

        val resultOnly = Problem.AttentionLowered(attention = false, result = true)
        assertEquals("「Task result” notifications won’t pop up", StatusCopy.banner(resultOnly).title)
        assertTrue(StatusCopy.banner(resultOnly).detail.contains("通知 › Task result › 在屏幕上弹出"))
        assertEquals(Fix.OpenResultChannel, StatusCopy.directFix(resultOnly))
        val e = StatusCopy.explain(resultOnly, "14")
        assertEquals("Turn on", e.action)
        assertEquals(Fix.OpenResultChannel, e.fix)
    }
}
