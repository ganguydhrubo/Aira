package com.ganguydhrubo.aira.task

import com.ganguydhrubo.aira.core.agent.TaskState
import com.ganguydhrubo.aira.core.learn.knowledge.GuideCatalog
import com.ganguydhrubo.aira.core.learn.memory.MemoryExtractor
import com.ganguydhrubo.aira.core.learn.memory.MemoryKind
import com.ganguydhrubo.aira.core.learn.memory.MemorySource
import com.ganguydhrubo.aira.settings.MemorySwitches
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.ganguydhrubo.aira.core.assembly.knownTools

class RunLearningMemoryTest {
    private val catalog = GuideCatalog.loadBundled()
    private val extractJson = """{"ops":[{"op":"add","kind":"preference","content":"用户点外卖不要香菜","quote":"以后都不要香菜"}]}"""

    private fun aftermath(utterances: List<String>, saved: List<com.ganguydhrubo.aira.core.learn.memory.Change> = emptyList()) = RunAftermath(
        "conv-1", "run-1", "点外卖", TaskState.Succeeded("ok"), utterances, saved, listOf("x"), emptySet(), knownTools(), events = { emptyList() },
    )

    @Test fun extractsFromUserWordsWithConversationSource() {
        val store = memoryStore()
        val model = TextModel(extractJson)
        val notes = runBlocking {
            RunLearning(MemorySwitches(), store, MapSkills(), catalog, MemoryExtractor(model), null, { "d" }).learn(aftermath(listOf("点外卖,以后都不要香菜")))
        }
        assertEquals(listOf("记住了:用户点外卖不要香菜"), notes.memoryNotes.map { n -> n.text(store.items.value.find { it.id == n.memoryId }) })
        assertEquals(listOf("x"), notes.usedMemory)
        assertEquals("conv-1", store.items.value.single().source.conversationId)
        assertEquals("Ui", store.items.value.single().source.channel)
        assertEquals("run-1", notes.runId)
    }

    @Test fun autoRememberOffSkipsExtractionButKeepsSavedNotes() {
        val store = memoryStore()
        val saved = store.add(MemoryKind.fact, "用户住在张江", emptyList(), MemorySource("user_said", "我住在张江", atMs = 1)).getOrThrow()
        val model = TextModel(extractJson)
        val notes = runBlocking {
            RunLearning(MemorySwitches(autoRemember = false), store, MapSkills(), catalog, MemoryExtractor(model), null, { "d" })
                .learn(aftermath(listOf("以后都不要香菜"), listOf(saved)))
        }
        assertTrue(model.requests.isEmpty())
        assertEquals(listOf("记住了:用户住在张江"), notes.memoryNotes.map { n -> n.text(store.items.value.find { it.id == n.memoryId }) })
    }

    @Test fun noUtterancesNoCall() {
        val model = TextModel(extractJson)
        runBlocking { RunLearning(MemorySwitches(), memoryStore(), MapSkills(), catalog, MemoryExtractor(model), null, { "d" }).learn(aftermath(emptyList())) }
        assertTrue(model.requests.isEmpty())
    }

    @Test fun noRunIdNoExtraction() {
        val model = TextModel(extractJson)
        runBlocking {
            RunLearning(MemorySwitches(), memoryStore(), MapSkills(), catalog, MemoryExtractor(model), null, { "d" })
                .learn(aftermath(listOf("以后都不要香菜")).copy(runId = null))
        }
        assertTrue(model.requests.isEmpty())
    }

    // 对话里有通知提醒(别人写的字):用户在这里说的话不自动提取成记忆;任务里明说要记的(memory_save 写下的)照常
    @Test fun conversationWithNoticesIsNotMinedForMemory() {
        val store = memoryStore()
        val saved = store.add(MemoryKind.fact, "用户住在张江", emptyList(), MemorySource("user_said", "我住在张江", atMs = 1)).getOrThrow()
        val model = TextModel(extractJson)
        val notes = runBlocking {
            RunLearning(MemorySwitches(), store, MapSkills(), catalog, MemoryExtractor(model), null, { "d" })
                .learn(aftermath(listOf("好,就按他说的,以后都不要香菜"), listOf(saved)).copy(noticeTainted = true))
        }
        assertTrue(model.requests.isEmpty())
        assertEquals(listOf("记住了:用户住在张江"), notes.memoryNotes.map { n -> n.text(store.items.value.find { it.id == n.memoryId }) })
    }

    // 这次任务用工具读过通知:和读过网页一样,不自动从用户这次说的话里提取记忆
    @Test fun aRunThatReadNotificationsIsNotMinedForMemory() {
        val model = TextModel(extractJson)
        val read = com.ganguydhrubo.aira.core.run.record.RunEvent.Activity(0, 1, "observe", com.ganguydhrubo.aira.core.general.NOTICES_READ)
        runBlocking {
            RunLearning(MemorySwitches(), memoryStore(), MapSkills(), catalog, MemoryExtractor(model), null, { "d" })
                .learn(aftermath(listOf("好,就按他说的,以后都不要香菜")).copy(events = { listOf(read) }))
        }
        assertTrue(model.requests.isEmpty())
        // 别的只读工具不受影响
        val other = com.ganguydhrubo.aira.core.run.record.RunEvent.Activity(0, 1, "observe", "history_read")
        runBlocking {
            RunLearning(MemorySwitches(), memoryStore(), MapSkills(), catalog, MemoryExtractor(model), null, { "d" })
                .learn(aftermath(listOf("点外卖,以后都不要香菜")).copy(events = { listOf(other) }))
        }
        assertEquals(1, model.requests.size)
    }

    // 这次任务开工时就带着从通知里得来的那一段:用户这次说的话多半是在回应里面别人写的事,不自动提取记忆。
    // 从真的跑一遍任务开始:AgentLoop 带着那一段跑完,运行记录里有那一笔,任务结束后的学习据此不提取
    @Test fun aRunGivenNotificationContextIsNotMinedForMemory() = runBlocking {
        fun run(observed: String?): Int {
            val store = memoryStore()
            val utterances = com.ganguydhrubo.aira.core.learn.memory.UserUtterances()
            val events = mutableListOf<com.ganguydhrubo.aira.core.run.record.RunEvent>()
            val screen = java.lang.reflect.Proxy.newProxyInstance(
                com.ganguydhrubo.aira.core.screen.uistate.ScreenPort::class.java.classLoader, arrayOf(com.ganguydhrubo.aira.core.screen.uistate.ScreenPort::class.java),
            ) { _, m, _ ->
                when {
                    m.name == "getAppAccess" -> com.ganguydhrubo.aira.core.device.access.AppAccessPolicy.ALLOW
                    m.returnType == Boolean::class.javaPrimitiveType -> false
                    else -> error("Unexpected screen access: ${m.name}")
                }
            } as com.ganguydhrubo.aira.core.screen.uistate.ScreenPort
            val dispatcher = com.ganguydhrubo.aira.core.assembly.toolDispatcher(screen, memory = store, utterances = utterances)
            val agent = com.ganguydhrubo.aira.core.agent.AgentLoop(TextModel("好的"), dispatcher, "base", memory = store, observed = observed,
                sink = object : com.ganguydhrubo.aira.core.run.record.RunSink { override fun event(e: com.ganguydhrubo.aira.core.run.record.RunEvent) { events += e } })
            val goal = "好,就按他说的,以后都不要香菜"
            val end = runBlocking { agent.run(goal, emptyList()) }
            assertTrue(end is TaskState.Succeeded)
            assertEquals(observed != null, agent.carriedObserved)
            val extraction = TextModel(extractJson)
            // 对话里没有通知提醒(noticeTainted 为假):只靠运行记录里的那一笔
            val aftermath = RunAftermath("conv", "run", goal, end, utterances.all, emptyList(), emptyList(),
                dispatcher.definitions().map { it.name }.toSet(), dispatcher.index, events = { events })
            runBlocking { RunLearning(MemorySwitches(), store, MapSkills(), catalog, MemoryExtractor(extraction), null, { "id" }).learn(aftermath) }
            return extraction.requests.size
        }
        val observed = com.ganguydhrubo.aira.core.proactive.ObservedContext.render(listOf("群里的人点餐时说以后都不要香菜"), emptyList(), emptyList(), emptyList())!!
        assertEquals(0, run(observed))
        // 没带那一段的同一个任务照常提取
        assertEquals(1, run(null))
    }
}
