package com.ganguydhrubo.aira.pocket

import com.ganguydhrubo.aira.core.agent.FailKind
import com.ganguydhrubo.aira.core.agent.GateReason
import com.ganguydhrubo.aira.core.agent.TaskState
import org.junit.Assert.*
import org.junit.Test

class LockTriggerPolicyTest {
    @Test fun `completion failure cancellation and user waits require a check`() {
        val states = listOf(
            TaskState.Succeeded("done"), TaskState.Failed("failed", FailKind.Environment),
            TaskState.Cancelled, TaskState.Held(GateReason("blocked")),
            TaskState.WaitingInput("question"), TaskState.WaitingTakeover("reason", "message")
        )
        states.forEach { assertTrue(LockTriggerPolicy.shouldCheck(it)) }
    }

    @Test fun `running or idle must not trigger a lock check`() {
        assertFalse(LockTriggerPolicy.shouldCheck(TaskState.Idle))
        assertFalse(LockTriggerPolicy.shouldCheck(TaskState.Running(1, "working")))
        assertFalse(LockTriggerPolicy.shouldCheck(TaskState.Held(GateReason.queued("waiting"))))
        assertFalse(LockTriggerPolicy.shouldCheck(TaskState.Held(GateReason("waiting_unlock"))))
    }

    @Test fun `resuming during the half second cancels pending lock`() {
        val gate = CompletionLockGate()
        val paused = TaskState.WaitingInput("question")
        assertTrue(gate.begin(true, LockTriggerPolicy.shouldCheck(paused), true, true, 0))
        assertFalse(gate.observe(true, LockTriggerPolicy.isWaiting(TaskState.Running(2, "working")), true, 250))
        assertFalse(gate.observe(true, true, true, 600))
    }
}
