package com.ganguydhrubo.aira.core.learn.skills

import com.ganguydhrubo.aira.core.learn.knowledge.PERSONAL_KIND
import com.ganguydhrubo.aira.core.learn.knowledge.RECOMMEND_MIN_SCORE
import com.ganguydhrubo.aira.core.learn.knowledge.GuideCatalog
import com.ganguydhrubo.aira.core.learn.knowledge.GuideView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideCatalogMergeTest {
    private val bundled = GuideCatalog.loadBundled()
    private fun merged(vararg s: PersonalSkill) = GuideCatalog.withPersonal(bundled, InMemoryPersonalSkills(*s))

    @Test fun personalOverridesBundled() {
        val c = merged(personalSkill("youtube", body = "## Entry\nmy YouTube way", display = "My YouTube"))
        val youtubes = c.items.filter { it.name == "youtube" }
        assertEquals(listOf(PERSONAL_KIND), youtubes.map { it.kind })
        val v = c.view("youtube") as GuideView.Found
        assertEquals(PERSONAL_KIND, v.info.kind)
        assertEquals("## Entry\nmy YouTube way", v.content)
        assertTrue(c.view("personal:youtube") is GuideView.Found)
        assertEquals("skill_not_found", (c.view("app:youtube") as GuideView.Failed).error)
        // 不影响别的内置技能
        assertTrue(c.view("app:telegram") is GuideView.Found)
        assertEquals(bundled.items.size, c.items.size)
    }

    @Test fun invalidPersonalFailsLoudly() {
        val broken = PersonalSkill("youtube", "没有头部的乱码", SkillStatus.active, null, null, 0L, emptyList())
        val c = merged(broken)
        assertEquals("personal_skill_invalid", (c.view("youtube") as GuideView.Failed).error)
        assertNull(c.fullText(c.items.first { it.name == "youtube" }))
        // 名字对不上、正文为空也算坏
        assertEquals("personal_skill_invalid", (merged(personalSkill("youtube").copy(name = "youtube2")).view("youtube2") as GuideView.Failed).error)
        assertEquals("personal_skill_invalid", (merged(personalSkill("my-x", body = "  ")).view("my-x") as GuideView.Failed).error)
    }

    @Test fun pendingAndSuspendedHidden() {
        val store = InMemoryPersonalSkills(personalSkill("youtube", status = SkillStatus.suspended), personalSkill("my-trip"))
        store.putPending("d1", personalSkill("my-draft"))
        val c = GuideCatalog.withPersonal(bundled, store)
        assertTrue(c.find("my-draft").isEmpty())
        assertEquals("skill_not_found", (c.view("my-draft") as GuideView.Failed).error)
        // 停用的个人技能不顶替内置
        assertEquals(listOf("app"), c.items.filter { it.name == "youtube" }.map { it.kind })
        assertEquals(listOf("my-trip"), c.find("my-trip").map { it.name })
        // 收下后立即可见
        store.approve("d1")
        assertEquals(listOf("my-draft"), c.find("my-draft").map { it.name })
    }

    @Test fun minScoreFilters() {
        val c = merged(personalSkill("family-msg", description = "给家人发消息"))
        assertEquals(listOf("family-msg"), c.find("家人", kind = PERSONAL_KIND).map { it.name })
        assertTrue(c.find("家人", kind = PERSONAL_KIND, minScore = RECOMMEND_MIN_SCORE).isEmpty())
        // 整句目标里出现显示名/别名:算部分对上
        assertTrue("youtube" in c.find("use YouTube to find a video about the route to the office", minScore = RECOMMEND_MIN_SCORE).map { it.name })
        assertFalse(bundled.find("帮我查一下明天的天气怎么样啊", minScore = RECOMMEND_MIN_SCORE).any { it.name == "youtube" })
    }
}
