package com.ganguydhrubo.aira.chat.ui

import com.ganguydhrubo.aira.core.model.Capabilities
import com.ganguydhrubo.aira.core.model.ChatModel
import com.ganguydhrubo.aira.core.model.GroupMember
import com.ganguydhrubo.aira.core.model.ListedModel
import com.ganguydhrubo.aira.core.model.Message
import com.ganguydhrubo.aira.core.model.ModelGroup
import com.ganguydhrubo.aira.core.model.ModelReply
import com.ganguydhrubo.aira.core.model.ModelRole
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
import com.ganguydhrubo.aira.settings.deleteGroup
import com.ganguydhrubo.aira.settings.ensureConfig
import com.ganguydhrubo.aira.settings.removeProvider
import com.ganguydhrubo.aira.settings.saveGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.crypto.KeyGenerator

class MainModelCatalogTest {
    @get:Rule val tmp = TemporaryFolder()
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    private fun store() = ProviderStore(tmp.root, AesGcmKeyCipher { key }, userAgent = "Aira/test") { cfg, _, _, _ ->
        FakeModel(cfg.model, cfg.capabilities)
    }

    private fun ProviderStore.connectOne(name: String, vararg models: String): Provider =
        connect(name, ProviderConfig.Kind.OpenAiCompat, "https://${name.lowercase()}.test/v1", "k", models.map { ListedModel(it) })

    private fun cap(tools: Boolean = true) = Capabilities(toolCalls = tools, vision = false, contextTokens = 64_000, reasoning = false)

    @Test fun optionsPutUsableGroupsBeforeModels() {
        val s = store()
        val p = s.connectOne("P", "m1", "m2")
        val a = s.ensureConfig(p.id, "m1", cap())!!
        val b = s.ensureConfig(p.id, "m2", cap())!!
        s.saveGroup(ModelGroup("g", "主力兜底", members = listOf(GroupMember(a), GroupMember(b))))

        val options = ProviderMainModelCatalog(s).options()
        assertEquals(listOf(ModelChoice.Group("g"), ModelChoice.Model(a), ModelChoice.Model(b)), options.map { it.choice })
        assertEquals("主力兜底", options[0].title)
        assertEquals(2, options[0].memberCount)
        assertTrue(options[0].group)
        assertEquals("P", options[1].subtitle)
        assertFalse(options[1].group)
    }

    @Test fun optionsOfferOnlyModelsThatSelectionAccepts() {
        val s = store()
        val p = s.connectOne("P", "zz-tool", "aa-plain")
        s.ensureConfig(p.id, "zz-tool", cap(tools = true))
        s.ensureConfig(p.id, "aa-plain", cap(tools = false))
        assertEquals(listOf("zz-tool"), ProviderMainModelCatalog(s).options().map { it.title })
    }

    @Test fun unusableGroupIsNotOffered() {
        val s = store()
        val p = s.connectOne("P", "plain")
        val a = s.ensureConfig(p.id, "plain", cap(tools = false))!!
        s.saveGroup(ModelGroup("g", "g", members = listOf(GroupMember(a))))
        assertTrue(ProviderMainModelCatalog(s).options().none { it.group })
    }

    @Test fun labelsCoverModelGroupAndFollowingSettings() {
        val s = store()
        val p = s.connectOne("P", "m1", "m2")
        val a = s.ensureConfig(p.id, "m1", cap())!!
        val b = s.ensureConfig(p.id, "m2", cap())!!
        s.saveGroup(ModelGroup("g", "主力兜底", members = listOf(GroupMember(a), GroupMember(b))))
        s.choose(ModelRole.Main, RoleChoice.Group("g"))
        val catalog = ProviderMainModelCatalog(s)

        assertEquals("主力兜底", catalog.globalLabel())
        assertEquals("主力兜底", catalog.label(ModelChoice.Group("g")))
        assertEquals("m2", catalog.label(ModelChoice.Model(b)))
        assertTrue(catalog.isGroup(ModelChoice.Group("g")))
        assertFalse(catalog.isGroup(ModelChoice.Model(b)))
        assertNull(catalog.label(ModelChoice.Model("gone")))
        assertNull(catalog.usable(ModelChoice.Model("gone")))
    }

    @Test fun followingASpecificMainModelUsesItsModelName() {
        val s = store()
        val p = s.connectOne("P", "m1")
        val a = s.ensureConfig(p.id, "m1", cap())!!
        s.choose(ModelRole.Main, RoleChoice.Model(a))
        assertEquals("m1", ProviderMainModelCatalog(s).globalLabel())
    }

    @Test fun catalogReflectsProviderAndGroupChangesWithoutRecreatingIt() {
        val s = store()
        val p = s.connectOne("P", "m1", "m2")
        val a = s.ensureConfig(p.id, "m1", cap())!!
        val b = s.ensureConfig(p.id, "m2", cap())!!
        s.saveGroup(ModelGroup("g", "主力兜底", members = listOf(GroupMember(a), GroupMember(b))))
        val catalog = ProviderMainModelCatalog(s)
        assertEquals(3, catalog.options().size)
        s.deleteGroup("g")
        assertTrue(catalog.options().none { it.group })
        // 删组会 prune 不再被引用的成员配置；重新加回后同一目录实例也要立刻看到。
        s.ensureConfig(p.id, "m1", cap())
        s.ensureConfig(p.id, "m2", cap())
        assertEquals(listOf("m1", "m2"), catalog.options().map { it.title })
        s.removeProvider(p.id)
        assertTrue(catalog.options().isEmpty())
        assertNull(catalog.globalLabel())
    }

    private class FakeModel(override val name: String, override val capabilities: Capabilities) : ChatModel {
        override suspend fun complete(system: String, messages: List<Message>, tools: List<ToolSpec>): ModelReply =
            ModelReply(emptyList(), StopReason.EndTurn, Usage(1, 1))
    }
}
