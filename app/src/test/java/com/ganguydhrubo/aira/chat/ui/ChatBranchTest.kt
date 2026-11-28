package com.ganguydhrubo.aira.chat.ui

import com.ganguydhrubo.aira.core.agent.AttachedPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.ganguydhrubo.aira.settings.ui.models.Page

/** 长按哪一条消息能分支:只有已答完的回答,且这个对话没有任务在跑。 */
class ChatBranchTest {
    private fun exchange(status: ExchangeStatus) = Exchange("Q", emptyList(), status, live = false)

    @Test fun aFinishedAnswerCanBranch() {
        val exchanges = listOf(exchange(ExchangeStatus.Done("A", null)), exchange(ExchangeStatus.Working))
        assertEquals(0, branchableTurn(exchanges, "r0", blocked = false))
    }

    @Test fun failedAndCancelledTurnsCanBranch() {
        assertEquals(0, branchableTurn(listOf(exchange(ExchangeStatus.Failed("Error"))), "r0", blocked = false))
        assertEquals(0, branchableTurn(listOf(exchange(ExchangeStatus.Cancelled())), "r0", blocked = false))
    }

    @Test fun userMessagesSteersAndStatusCardsCannotBranch() {
        val exchanges = listOf(exchange(ExchangeStatus.Done("A", null)))
        assertNull(branchableTurn(exchanges, "u0", blocked = false))
        assertNull(branchableTurn(exchanges, "m0-steer", blocked = false))
        assertNull(branchableTurn(exchanges, "selectable-answer", blocked = false))
        assertNull(branchableTurn(exchanges, "r", blocked = false))
        assertNull(branchableTurn(exchanges, "r9", blocked = false))
    }

    @Test fun unfinishedTurnsCannotBranch() {
        assertNull(branchableTurn(listOf(exchange(ExchangeStatus.Working)), "r0", blocked = false))
        assertNull(branchableTurn(listOf(exchange(ExchangeStatus.WaitingUnlock)), "r0", blocked = false))
        assertNull(branchableTurn(listOf(exchange(ExchangeStatus.Queued("Queue"))), "r0", blocked = false))
        assertNull(branchableTurn(listOf(exchange(ExchangeStatus.NeedsInput("Q", null))), "r0", blocked = false))
        assertNull(branchableTurn(listOf(exchange(ExchangeStatus.Paused("稍等"))), "r0", blocked = false))
        assertNull(branchableTurn(listOf(exchange(ExchangeStatus.NeedsTakeover("Payment", "请接管"))), "r0", blocked = false))
    }

    @Test fun aConversationWithARunningTaskCannotBranch() {
        val exchanges = listOf(exchange(ExchangeStatus.Done("A", null)))
        assertNull(branchableTurn(exchanges, "r0", blocked = true))
    }

    @Test fun onlyUserMessagesCanRetry() {
        val exchanges = listOf(exchange(ExchangeStatus.Done("A", null)))
        assertEquals(0, retryableTurn(exchanges, "u0", blocked = false))
        assertNull(retryableTurn(exchanges, "r0", blocked = false))
        assertNull(retryableTurn(exchanges, "m0-steer", blocked = false))
        assertNull(retryableTurn(exchanges, "u1", blocked = false))
        assertNull(retryableTurn(exchanges, "u", blocked = false))
        assertNull(retryableTurn(exchanges, "u0", blocked = true))
        assertNull(retryableTurn(listOf(exchange(ExchangeStatus.Done("A", null)).copy(goal = " ")), "u0", blocked = false))
    }

    /** 这一轮还没结束时用户的话也不能重新问:菜单不显示,和点了之后的判断一致。 */
    @Test fun unfinishedTurnsCannotRetry() {
        listOf(
            ExchangeStatus.Working, ExchangeStatus.WaitingUnlock, ExchangeStatus.Queued("Queue"),
            ExchangeStatus.NeedsInput("Q", null), ExchangeStatus.Paused("稍等"), ExchangeStatus.NeedsTakeover("Payment", "请接管"),
        ).forEach { status ->
            val exchanges = listOf(exchange(status))
            assertNull(retryableTurn(exchanges, "u0", blocked = false))
            assertFalse(canRetryAt(exchanges, 0, blocked = false))
        }
        assertEquals(0, retryableTurn(listOf(exchange(ExchangeStatus.Failed("Error"))), "u0", blocked = false))
        assertEquals(0, retryableTurn(listOf(exchange(ExchangeStatus.Cancelled())), "u0", blocked = false))
    }

    /** 菜单用的键判断和动作用的轮次判断是同一个:能显示就能做,不能做就不显示。 */
    @Test fun menuAndActionAgree() {
        val statuses = listOf(ExchangeStatus.Done("A", null), ExchangeStatus.Working, ExchangeStatus.Queued("Queue"), ExchangeStatus.Cancelled())
        for (status in statuses) for (blocked in listOf(true, false)) for (goal in listOf("Q", " ")) {
            val exchanges = listOf(exchange(status).copy(goal = goal))
            assertEquals(canBranchAt(exchanges, 0, blocked), branchableTurn(exchanges, "r0", blocked) != null)
            assertEquals(canRetryAt(exchanges, 0, blocked), retryableTurn(exchanges, "u0", blocked) != null)
        }
    }

    @Test fun retryTextKeepsOnlyWhatTheUserWrote() {
        assertEquals("帮我改一下", retryText("帮我改一下"))
        assertEquals(
            "帮我改一下",
            retryText("> [Quoting your earlier reply]\n> 之前的话\n\n帮我改一下"),
        )
        assertEquals("", retryText("附件:/workspace/inbox/a.pdf(1 KB)"))
        assertEquals("", retryText("[[screen:s1]]"))
        val page = AttachedPage.compose(AttachedPage.Page("https://a.example/", "页", "选中的字"), "帮我改一下")
        assertEquals("帮我改一下", retryText(page))
        assertFalse(needsReattach(page))
        assertTrue(needsReattach("[[screen:s1]]\n帮我看看"))
        assertTrue(needsReattach("看这个\n附件:/workspace/inbox/a.pdf(1 KB)"))
        assertFalse(needsReattach("只是文字"))
    }
}
