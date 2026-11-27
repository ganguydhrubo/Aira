package com.ganguydhrubo.aira.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ganguydhrubo.aira.EnglishUi
import com.ganguydhrubo.aira.core.agent.GoalChecklist
import com.ganguydhrubo.aira.core.agent.RunProgress
import com.ganguydhrubo.aira.core.agent.TaskState
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.ganguydhrubo.aira.chat.ui.ChatViewModel
import com.ganguydhrubo.aira.chat.ui.Exchange
import com.ganguydhrubo.aira.chat.ui.ExchangeStatus
import com.ganguydhrubo.aira.chat.ui.WorkBlock

@RunWith(AndroidJUnit4::class)
class TaskGoalProgressTest {
    @get:Rule(order = Int.MIN_VALUE) val chinese = EnglishUi()
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun cardKeepsActionAndExpandsTheLastConfirmedGoals() {
        val items = listOf("确认地址", "核实店名", "给出推荐")
        fun running(ids: Set<Int>, stale: Boolean, action: String): Exchange {
            val progress = RunProgress.Task("", ids.size, items.size, null, GoalChecklist(items, ids, stale))
            return ChatViewModel.reduce(Exchange("找咖啡馆", emptyList(), ExchangeStatus.Working, true, "coffee"),
                TaskState.Running(4, action, "phone_observe", progress, narration = action))
        }
        val ex = mutableStateOf(running(setOf(1), false, "打开店铺页"))
        compose.setContent { AiraTheme { WorkBlock(ex.value, emptyList(), true, false) {} } }
        compose.onNodeWithText("打开店铺页").assertIsDisplayed()
        compose.onNodeWithText("已确认目标 1/3").performClick()
        compose.onNodeWithText("✓  确认地址").assertIsDisplayed()
        compose.onNodeWithText("○  核实店名").assertIsDisplayed()
        compose.runOnIdle { ex.value = running(setOf(1, 2), true, "整理五篇笔记") }
        compose.onNodeWithText("整理五篇笔记").assertIsDisplayed()
        compose.onNodeWithText("上次确认 2/3").assertIsDisplayed()
        compose.onNodeWithText("✓  核实店名").assertIsDisplayed()
        compose.onNodeWithText("○  给出推荐").assertIsDisplayed()
        compose.onNodeWithText("上次确认 2/3").performClick()
        compose.onNodeWithText("○  给出推荐").assertDoesNotExist()
    }
}
