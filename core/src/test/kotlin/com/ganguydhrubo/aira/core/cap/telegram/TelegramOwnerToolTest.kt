package com.ganguydhrubo.aira.core.cap.telegram

import com.ganguydhrubo.aira.core.capability.PromptEnv
import com.ganguydhrubo.aira.core.tool.AgentTool
import com.ganguydhrubo.aira.core.tool.ArgError
import com.ganguydhrubo.aira.core.tool.Args
import com.ganguydhrubo.aira.core.tool.EvidenceLog
import com.ganguydhrubo.aira.core.tool.Risk
import com.ganguydhrubo.aira.core.tool.ToolConfig
import com.ganguydhrubo.aira.core.tool.ToolContext
import com.ganguydhrubo.aira.core.tool.ToolIndex
import com.ganguydhrubo.aira.core.screen.visual.Shots
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** telegram_send_owner 工具:供给随端口有无,参数上限拒超,端口结局如实进结果。 */
class TelegramOwnerToolTest {
    private class FakePort(var result: TelegramOwnerResult) : TelegramOwnerPort {
        val sent = mutableListOf<String>()
        override suspend fun send(text: String): TelegramOwnerResult {
            sent += text
            return result
        }
    }

    private fun ctx(capability: TelegramOwnerCapability): ToolContext {
        val ctx = ToolContext(
            ToolConfig(false, null, false, null, false, false, null, null, null, null, null),
            EvidenceLog(), ToolIndex(capability.tools()), null, Shots(),
        )
        capability.onTaskStart(ctx)
        return ctx
    }

    private fun args(tool: AgentTool, text: String) = Args(tool.def, buildJsonObject { put("text", text) })

    @Test fun noPortOffersNothing() {
        val capability = TelegramOwnerCapability(null)
        assertTrue(capability.tools().isEmpty())
        assertTrue(capability.sections(PromptEnv(offered = setOf("telegram_send_owner"), inlineScreen = false)).isEmpty())
    }

    @Test fun toolAndSectionOfferedWithPort() {
        val capability = TelegramOwnerCapability(FakePort(TelegramOwnerResult(TelegramOwnerResult.Status.Delivered)))
        val tool = capability.tools().single()
        assertEquals("telegram_send_owner", tool.name)
        assertEquals(Risk.Write, tool.risk)
        assertTrue(tool.offScreen)
        assertTrue(tool.logged)
        assertTrue(tool.available(ctx(capability)))
        assertFalse(capability.sections(PromptEnv(offered = setOf("telegram_send_owner"), inlineScreen = false)).isEmpty())
        // 工具不在本任务供给里时守则也不出现
        assertTrue(capability.sections(PromptEnv(offered = emptySet(), inlineScreen = false)).isEmpty())
    }

    @Test fun overlongTextRejected() {
        val port = FakePort(TelegramOwnerResult(TelegramOwnerResult.Status.Delivered))
        val capability = TelegramOwnerCapability(port)
        val tool = capability.tools().single()
        val ctx = ctx(capability)
        assertThrows(ArgError::class.java) {
            runBlocking { tool.execute(args(tool, "x".repeat(4001)), ctx) }
        }
        assertTrue(port.sent.isEmpty())
    }

    @Test fun unavailableReportedHonestly() = runTest {
        val port = FakePort(TelegramOwnerResult(TelegramOwnerResult.Status.Unavailable, "绑定已失效"))
        val capability = TelegramOwnerCapability(port)
        val result = capability.tools().single().execute(args(capability.tools().single(), "结果正文"), ctx(capability)).content
        assertEquals(false, result.getValue("ok").jsonPrimitive.content.toBoolean())
        assertEquals("unavailable", result.getValue("status").jsonPrimitive.content)
        assertEquals("绑定已失效", result.getValue("text").jsonPrimitive.content)
        assertEquals(listOf("结果正文"), port.sent)
    }

    @Test fun deliveredMapsToOk() = runTest {
        val port = FakePort(TelegramOwnerResult(TelegramOwnerResult.Status.Delivered))
        val capability = TelegramOwnerCapability(port)
        val result = capability.tools().single().execute(args(capability.tools().single(), "结果正文"), ctx(capability)).content
        assertEquals(true, result.getValue("ok").jsonPrimitive.content.toBoolean())
        assertEquals("delivered", result.getValue("status").jsonPrimitive.content)
    }
}
