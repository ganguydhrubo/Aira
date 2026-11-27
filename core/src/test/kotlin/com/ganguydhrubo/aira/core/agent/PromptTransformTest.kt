package com.ganguydhrubo.aira.core.agent

import com.ganguydhrubo.aira.core.assembly.toolDispatcher
import com.ganguydhrubo.aira.core.testing.fakeModel
import com.ganguydhrubo.aira.core.tool.FakeScreen
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PromptTransformTest {
    @Test fun promptTransformAppliesToSystemPrompt() = runTest {
        val m = fakeModel { answer("hi") }
        AgentLoop(m, toolDispatcher(FakeScreen()), SystemPrompt.identity(), promptTransform = { "X" }).run("t")
        assertEquals("X", m.requests.single().system)
    }
}
