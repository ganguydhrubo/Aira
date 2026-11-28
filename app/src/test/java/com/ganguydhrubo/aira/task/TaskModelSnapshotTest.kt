package com.ganguydhrubo.aira.task

import com.ganguydhrubo.aira.chat.ConversationSettings
import com.ganguydhrubo.aira.chat.ConversationStore
import com.ganguydhrubo.aira.core.agent.GroupPool
import com.ganguydhrubo.aira.core.model.Capabilities
import com.ganguydhrubo.aira.core.model.ChatModel
import com.ganguydhrubo.aira.core.model.GroupMember
import com.ganguydhrubo.aira.core.model.ListedModel
import com.ganguydhrubo.aira.core.model.Message
import com.ganguydhrubo.aira.core.model.ModelGroup
import com.ganguydhrubo.aira.core.model.ModelReply
import com.ganguydhrubo.aira.core.model.ModelRole
import com.ganguydhrubo.aira.core.model.ModelSession
import com.ganguydhrubo.aira.core.model.StopReason
import com.ganguydhrubo.aira.core.model.ToolSpec
import com.ganguydhrubo.aira.core.model.Usage
import com.ganguydhrubo.aira.settings.AesGcmKeyCipher
import com.ganguydhrubo.aira.settings.ModelChoice
import com.ganguydhrubo.aira.settings.Provider
import com.ganguydhrubo.aira.settings.ProviderConfig
import com.ganguydhrubo.aira.settings.ProviderStore
import com.ganguydhrubo.aira.settings.RoleChoice
import com.ganguydhrubo.aira.settings.choose
import com.ganguydhrubo.aira.settings.connect
import com.ganguydhrubo.aira.settings.ensureConfig
import com.ganguydhrubo.aira.settings.removeProvider
import com.ganguydhrubo.aira.settings.saveGroup
import com.ganguydhrubo.aira.settings.setMainFallback
import com.ganguydhrubo.aira.settings.setFallback
import softassert.assertEquals
import softassert.assertFalse
import softassert.assertNull
import org.junit.Assert.assertSame
import softassert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.crypto.KeyGenerator

class TaskModelSnapshotTest {
    @get:Rule val tmp = TemporaryFolder()
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    private fun providers() = ProviderStore(tmp.newFolder("providers"), AesGcmKeyCipher { key }) { cfg, _, _, _ ->
        FakeModel(cfg.model, cfg.capabilities)
    }

    private fun ProviderStore.connectOne(name: String, vararg models: String, untested: Boolean = false): Provider =
        connect(name, ProviderConfig.Kind.OpenAiCompat, "https://${name.lowercase()}.test/v1", "k",
            if (untested) emptyList() else models.map { ListedModel(it) }, untested = untested)

    private fun ProviderStore.member(p: Provider, model: String): String = ensureConfig(p.id, model,
        Capabilities(toolCalls = true, vision = false, contextTokens = 64_000, reasoning = false))!!

    @Test fun snapshotFreezesOverrideFallbacksAndRetestMetadata() {
        val s = providers()
        val global = s.connectOne("Global", "global")
        val selected = s.connectOne("Selected", "selected", untested = true)
        val backup = s.connectOne("Backup", "backup")
        val globalId = s.member(global, "global")
        assertTrue(s.choose(ModelRole.Main, RoleChoice.Model(globalId)))
        val selectedId = s.member(selected, "selected")
        assertTrue(s.setMainFallback(0, selectedId))
        // ensureConfig 后若一直不被用途/备援引用，下一次 prune 会删掉它；逐个建并逐个挂上。
        val backupId = s.member(backup, "backup")
        assertTrue(s.setMainFallback(1, backupId))
        val choice = ModelChoice.Model(selectedId)

        val snapshot = TaskModelSnapshot.capture(s, ModelSession.of("chat"), choice)
        val frozenMain = snapshot.main
        val frozenFallbacks = snapshot.mainFallbacks
        assertEquals("selected", frozenMain!!.name)
        assertEquals(listOf("backup"), frozenFallbacks.map { it.name })
        assertEquals(listOf(selected.id, backup.id), snapshot.mainProviderIds)
        assertEquals(selected.id, snapshot.retestProvider?.id)
        assertFalse(snapshot.routeLightSeparately)

        s.removeProvider(selected.id)
        assertSame(frozenMain, snapshot.main)
        assertSame(frozenFallbacks, snapshot.mainFallbacks)
        assertEquals(selected.id, snapshot.retestProvider?.id)
        assertEquals(listOf(selected.id, backup.id), snapshot.mainProviderIds)

        val next = TaskModelSnapshot.capture(s, ModelSession.of("chat"), choice)
        assertEquals("global", next.main!!.name)
        assertNull(next.retestProvider)
    }

    @Test fun captureReadsTheLatestConversationChoice() {
        val conversations = ConversationStore(tmp.newFolder("chats"))
        val s = providers()
        val global = s.connectOne("Global", "global")
        val selected = s.connectOne("Selected", "selected")
        val globalId = s.member(global, "global")
        assertTrue(s.choose(ModelRole.Main, RoleChoice.Model(globalId)))
        val selectedId = s.member(selected, "selected")
        val id = "scheduled-or-channel"

        assertEquals("global", TaskModelSnapshot.capture(s, ModelSession.of(id), conversations.settings(id).main).main!!.name)
        conversations.setSettings(id, ConversationSettings(ModelChoice.Model(selectedId)))
        assertEquals("selected", TaskModelSnapshot.capture(s, ModelSession.of(id), conversations.settings(id).main).main!!.name)
        conversations.setSettings(id, ConversationSettings(ModelChoice.Model("deleted")))
        assertEquals("global", TaskModelSnapshot.capture(s, ModelSession.of(id), conversations.settings(id).main).main!!.name)
    }

    @Test fun directModelLightSameAsMainExcludesTheConversationPrimaryFromFallbacks() {
        val s = providers()
        val p = s.connectOne("P", "global", "selected")
        val globalId = s.member(p, "global")
        assertTrue(s.choose(ModelRole.Main, RoleChoice.Model(globalId)))
        val selectedId = s.member(p, "selected")
        assertTrue(s.setFallback(ModelRole.Light, 0, selectedId))

        val snapshot = TaskModelSnapshot.capture(s, ModelSession.of("chat"), ModelChoice.Model(selectedId))

        assertEquals("selected", snapshot.light!!.name)
        assertEquals(emptyList<String>(), snapshot.lightFallbacks.map { it.name })
    }

    @Test fun directModelVisionSameAsMainExcludesTheConversationPrimaryFromFallbacks() {
        val s = providers()
        val p = s.connectOne("P", "global", "selected")
        val globalId = s.member(p, "global")
        assertTrue(s.choose(ModelRole.Main, RoleChoice.Model(globalId)))
        assertTrue(s.choose(ModelRole.Vision, RoleChoice.SameAsMain))
        val selectedId = s.member(p, "selected")
        assertTrue(s.setFallback(ModelRole.Vision, 0, selectedId))

        val snapshot = TaskModelSnapshot.capture(s, ModelSession.of("chat"), ModelChoice.Model(selectedId))

        assertEquals("selected", snapshot.vision!!.name)
        assertEquals(emptyList<String>(), snapshot.visionFallbacks.map { it.name })
    }

    @Test fun explicitDifferentLightStillUsesStepRouting() {
        val s = providers()
        val p = s.connectOne("P", "main", "override", "light")
        val main = s.member(p, "main")
        assertTrue(s.choose(ModelRole.Main, RoleChoice.Model(main)))
        val light = s.member(p, "light")
        assertTrue(s.choose(ModelRole.Light, RoleChoice.Model(light)))
        val override = s.member(p, "override")

        val snapshot = TaskModelSnapshot.capture(s, ModelSession.of("chat"), ModelChoice.Model(override))
        assertEquals("override", snapshot.main!!.name)
        assertEquals("light", snapshot.light!!.name)
        assertTrue(snapshot.routeLightSeparately)
    }

    @Test fun mainGroupWithLightSameAsMainSkipsStepRouting() {
        val s = providers()
        val p = s.connectOne("P", "m1", "m2", "light")
        val a = s.member(p, "m1")
        val b = s.member(p, "m2")
        assertTrue(s.saveGroup(ModelGroup("g", "g", members = listOf(GroupMember(a, 0), GroupMember(b, 1)))))
        assertTrue(s.choose(ModelRole.Main, RoleChoice.Group("g")))

        val same = TaskModelSnapshot.capture(s, ModelSession.of("chat"))
        assertTrue(same.main is GroupPool)
        assertTrue(same.light is GroupPool)
        assertFalse(same.routeLightSeparately)

        // 轻量另选了模型时照旧逐步分工
        val light = s.member(p, "light")
        assertTrue(s.choose(ModelRole.Light, RoleChoice.Model(light)))
        val split = TaskModelSnapshot.capture(s, ModelSession.of("chat"))
        assertTrue(split.main is GroupPool)
        assertEquals("light", split.light!!.name)
        assertTrue(split.routeLightSeparately)
    }

    private class FakeModel(override val name: String, override val capabilities: Capabilities) : ChatModel {
        override suspend fun complete(system: String, messages: List<Message>, tools: List<ToolSpec>): ModelReply =
            ModelReply(emptyList(), StopReason.EndTurn, Usage(1, 1))
    }
}
