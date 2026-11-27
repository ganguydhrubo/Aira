package com.ganguydhrubo.aira.core.action

import com.ganguydhrubo.aira.core.assembly.toolDispatcher
import com.ganguydhrubo.aira.core.safety.gate.ScreenRecoveryRequired
import com.ganguydhrubo.aira.core.safety.gate.TargetAppRestoreFailed
import com.ganguydhrubo.aira.core.safety.guard.OperationGuards
import com.ganguydhrubo.aira.core.model.Part
import com.ganguydhrubo.aira.core.run.record.RunSink
import com.ganguydhrubo.aira.core.tool.FakeScreen
import com.ganguydhrubo.aira.core.screen.uistate.ActionResult
import com.ganguydhrubo.aira.core.screen.uistate.ScreenPort
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 锁屏周期后准备好但还没执行的动作不再执行:模型收到一条说明结果,先重读屏再决定下一步。 */
class ScreenRecoveryTest {
    @Test fun lockedMidRunDoesNotExecutePendingTap() = runTest {
        val content = tapBlockedBy(ScreenRecoveryRequired())
        assertEquals(JsonPrimitive("screen_changed_during_lock"), content["error"])
    }

    @Test fun targetRestoreFailureLeavesPendingTapUnexecuted() = runTest {
        val content = tapBlockedBy(TargetAppRestoreFailed("com.chat", "Could not reopen com.chat."))
        assertEquals(JsonPrimitive("target_app_restore_failed"), content["error"])
        assertEquals(JsonPrimitive("com.chat"), content["package"])
        assertEquals(JsonPrimitive(true), content["recoverable"])
        assertEquals(JsonPrimitive(false), content["counts_as_failure"])
    }

    private suspend fun tapBlockedBy(failure: Exception): JsonObject {
        var taps = 0
        val screen = object : ScreenPort by FakeScreen() {
            override suspend fun tapXY(x: Int, y: Int): ActionResult {
                taps++
                return ActionResult.Ok(changed = true)
            }
        }
        val tools = toolDispatcher(screen)
        val executor = ActionExecutor(tools, OperationGuards(), null,
            gate = { throw failure }, recorder = ActionRecorder(RunSink.NONE), autonomous = true)
        val result = executor.run(Part.ToolCall("tap", "phone_tap", """{"x":10,"y":20}"""), Origin.Model(1))
        assertTrue(result is ActionOutcome.Ran)
        val content = (result as ActionOutcome.Ran).content
        assertEquals(JsonPrimitive(false), content["executed"])
        assertEquals(0, taps)
        return content
    }
}
