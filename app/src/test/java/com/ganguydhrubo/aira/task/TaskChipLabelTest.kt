package com.ganguydhrubo.aira.task

import com.ganguydhrubo.aira.core.agent.GateReason
import com.ganguydhrubo.aira.core.agent.RunProgress
import com.ganguydhrubo.aira.core.agent.TaskStage
import com.ganguydhrubo.aira.core.agent.TaskState
import com.ganguydhrubo.aira.core.safety.gate.ConfirmationRequest
import com.ganguydhrubo.aira.core.safety.gate.IrrevAction
import com.ganguydhrubo.aira.core.run.RunOrigin
import com.ganguydhrubo.aira.i18n.ChineseTexts
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TaskChipLabelTest {
    @get:Rule val texts = ChineseTexts()
    private val running = TaskState.Running(3, "private note", reason = "private reason")
    private val stage = TaskStage(1, 3, "查票", "铁路12306 · 查票")
    private fun label(state: TaskState) = taskChip(RunOrigin.Ui, state, stage)

    @Test fun onlyActiveUserStartedTasksRequestAChip() {
        for (origin in RunOrigin.entries - RunOrigin.Ui) assertNull(taskChip(origin, running))
        for (state in listOf(TaskState.Idle, TaskState.Held(GateReason.queued("next")), TaskState.Cancelled, TaskState.Succeeded("done"))) {
            assertNull(label(state))
        }
    }

    @Test fun currentStageSurvivesToolsThinkingAndUnrelatedQuantities() {
        for (tool in listOf(null, "phone_observe", "phone_tap", "web_navigate")) {
            assertEquals("1/3 查票", label(running.copy(tool = tool, progress = RunProgress.Batch(20))))
        }
        assertEquals("1/3 查票", label(running.copy(progress = RunProgress.Task("private", 5, 8, null))))
        assertEquals("1/3 · 铁路12306 · 查票", chipDetail(running, stage))
        assertEquals("Preparing", taskChip(RunOrigin.Ui, running.copy(step = 0)))
        assertEquals("Running", taskChip(RunOrigin.Ui, running))
        assertEquals("Thinking", chipDetail(running))
    }

    @Test fun personalDetailsNeverBecomeWaitingLabels() {
        val request = ConfirmationRequest(IrrevAction.order, "private recipient", "private content", null, "app")
        assertEquals("Waiting for your OK", label(TaskState.WaitingConfirmation(request)))
        assertEquals("Waiting for your answer", label(TaskState.WaitingInput("private question")))
        assertEquals("Take over", label(TaskState.WaitingTakeover("payment", "private", amount = "999")))
        assertEquals("Waiting for unlock", label(TaskState.Held(TaskGate.LOCKED)))
    }
}
