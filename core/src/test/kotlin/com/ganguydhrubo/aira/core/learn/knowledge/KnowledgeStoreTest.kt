package com.ganguydhrubo.aira.core.learn.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

private fun entry(
    name: String, display: String = name, pkgs: List<String> = emptyList(), domains: List<String> = emptyList(),
    aliases: List<String> = emptyList(), body: String? = "正文$name", surface: Surface = Surface.Android,
) = KnowledgeEntry(name, display, surface, pkgs, domains, aliases, null, "", body)

class KnowledgeStoreTest {
    private val bundled by lazy { KnowledgeStore.loadBundled() }

    @Test fun byPackageFindsTelegram() {
        val e = KnowledgeStore(bundled).byPackage("org.telegram.messenger")
        assertNotNull(e); assertEquals("Telegram", e!!.display)
        assertTrue(e.body!!.contains("chat", ignoreCase = true))
    }

    @Test fun userNotesAppendedAndLabelled() {
        val store = KnowledgeStore(bundled, mapOf("telegram" to "my note", "youtube.com" to "site note"))
        val body = store.byPackage("org.telegram.messenger")!!.body!!
        assertTrue(body.startsWith("## What it is"))
        assertTrue(body.endsWith("${KnowledgeStore.NOTES_HEADER}\nmy note"))
        assertTrue(store.byDomain("www.youtube.com")!!.body!!.endsWith("site note"))
        // An entry without a body can still carry only the user's note
        val bare = KnowledgeStore(listOf(entry("a", pkgs = listOf("p.a"), body = null)), mapOf("a" to "x"))
        assertEquals("${KnowledgeStore.NOTES_HEADER}\nx", bare.byPackage("p.a")!!.body)
    }

    @Test fun trialComesOnlyFromLearnedDraft() {
        val bundledA = listOf(entry("a", pkgs = listOf("p.a")))
        assertTrue(!KnowledgeStore(bundledA).byPackage("p.a")!!.trial)
        val trial = KnowledgeStore(bundledA, learnedNotes = mapOf("a" to LearnedNote("- 草稿", trial = true))).byPackage("p.a")!!
        assertTrue(trial.trial)
        assertTrue(trial.body!!.contains("- 草稿") && trial.body!!.contains("on trial"))
        val verified = KnowledgeStore(bundledA, learnedNotes = mapOf("a" to LearnedNote("- 已核实的补充", trial = false))).byPackage("p.a")!!
        assertTrue(!verified.trial && verified.body!!.contains("- 已核实的补充"))
        // 自学补充的起草要看的是内置正文本身,不带补充
        assertEquals(KnowledgeStore(bundledA).byPackage("p.a")!!.body, KnowledgeStore(bundledA, learnedNotes = mapOf("a" to LearnedNote("- x", true))).builtIn("a"))
    }

    @Test fun loadNotesReadsAppsAndSites() {
        val dir = Files.createTempDirectory("personal").toFile()
        java.io.File(dir, "apps/telegram").mkdirs(); java.io.File(dir, "apps/telegram/NOTES.md").writeText("  群聊先看置顶 \n")
        java.io.File(dir, "sites/zhihu.com").mkdirs(); java.io.File(dir, "sites/zhihu.com/NOTES.md").writeText("登录用扫码")
        java.io.File(dir, "apps/empty").mkdirs(); java.io.File(dir, "apps/empty/NOTES.md").writeText(" ")
        java.io.File(dir, "apps/none").mkdirs()
        assertEquals(mapOf("telegram" to "群聊先看置顶", "zhihu.com" to "登录用扫码"), KnowledgeStore.loadNotes(dir))
        assertTrue(KnowledgeStore.loadNotes(java.io.File(dir, "missing")).isEmpty())
    }

    @Test fun matchTaskByAlias() {
        val hit = KnowledgeStore(bundled).matchTask("send Wang a message on TG")
        assertEquals("telegram", hit.first().name)
    }

    @Test fun matchTaskUsesWordBoundariesForAscii() {
        assertTrue(KnowledgeStore(listOf(entry("mm", aliases = listOf("mm")))).matchTask("run command now").isEmpty())
    }

    @Test fun matchTaskByDomainAndBodyFirst() {
        val store = KnowledgeStore(listOf(
            entry("stub", "某站", domains = listOf("example.com"), body = null, surface = Surface.Web),
            entry("full", "某站", domains = listOf("example.com"), surface = Surface.Web),
        ))
        assertEquals(listOf("full", "stub"), store.matchTask("打开 https://www.example.com/a", 5).map { it.name })
    }

    @Test fun packageForDisplayName() {
        val store = KnowledgeStore(bundled)
        assertEquals("com.google.android.youtube", store.packageFor("YouTube"))
        assertEquals("org.telegram.messenger", store.packageFor("Telegram"))
        assertNull(store.packageFor("No Such App"))
    }

    @Test fun appWithoutHandwrittenProfileUsesSkillBody() {
        val e = KnowledgeStore(bundled).byPackage("com.whatsapp")
        assertNotNull(e); assertTrue(e!!.body!!.contains("WhatsApp"))
    }

    @Test fun handwrittenProfileBodyComesFirst() {
        val body = KnowledgeStore(bundled).byPackage("org.telegram.messenger")!!.body!!
        assertTrue(body.startsWith("## What it is"))
        assertTrue(body.contains("## Screens"))
    }

    @Test fun appAndSiteAreCrossLinked() {
        val store = KnowledgeStore(bundled)
        val app = store.byPackage("com.google.android.youtube")!!
        val site = store.byDomain("www.youtube.com")!!
        assertEquals(Surface.Android, app.surface); assertEquals(listOf("youtube.com"), app.related)
        assertEquals(Surface.Web, site.surface); assertEquals(listOf("youtube"), site.related)
        assertEquals("youtube", site.service)
        // Only one entry per service at the start of a task
        assertEquals(1, store.matchTask("watch the trending videos on YouTube", 5).count { it.service == "youtube" })
    }

    @Test fun profileMergesStubAliases() {
        val e = KnowledgeStore(bundled).byPackage("com.google.android.youtube")!!
        assertNotNull(e.body); assertTrue("YT" in e.aliases); assertEquals("android", e.surface.name.lowercase())
    }

    @Test fun genericAndNumericAliasesDoNotOverMatch() {
        val store = KnowledgeStore(bundled)
        assertTrue(store.matchTask("set an alarm for me", 5).none { it.name == "android-settings" })
        assertTrue(store.matchTask("pay 58 rupees", 5).isEmpty())
        assertEquals("youtube", store.matchTask("open YouTube").first().name)
        assertTrue(store.matchTask("open Settings and report the Android version", 5).any { it.name == "android-settings" })
    }

    @Test fun brandWordFollowedByAnotherAppNameDoesNotPickTheBrandsShopDoc() {
        val store = KnowledgeStore(bundled + listOf(
            entry("mi.com", "小米商城", domains = listOf("mi.com"), aliases = listOf("小米商城", "小米官网", "小米"), surface = Surface.Web),
            entry("market", "应用商店", pkgs = listOf("com.example.market"), aliases = listOf("应用商店")),
        ))
        val goal = "打开小米应用商店,搜索「哔哩哔哩」,告诉我评分"
        // 品牌词后面接着另一个 App 的名字(应用商店),说的是那个 App,不是品牌的商城
        assertTrue(store.matchTask(goal, 5).none { it.name == "mi.com" })
        assertTrue(store.matchTask(goal, 5, installed = listOf("com.example.market" to "应用商店")).none { it.name == "mi.com" })
        // 直接点名商城照常命中
        assertTrue(store.matchTask("小米商城上手环多少钱", 5).any { it.name == "mi.com" })
        assertTrue(store.matchTask("小米手机多少钱", 5).any { it.name == "mi.com" })
    }

    @Test fun installedLabelsAlsoCountAsAnotherAppName() {
        val store = KnowledgeStore(listOf(entry("brand", "某牌商城", aliases = listOf("某牌"), surface = Surface.Web)))
        assertTrue(store.matchTask("打开某牌乐园看看", 5).isNotEmpty())
        assertTrue(store.matchTask("打开某牌乐园看看", 5, installed = listOf("com.brand.fun" to "乐园")).isEmpty())
    }

    @Test fun installedAppsDocsComeFirst() {
        val store = KnowledgeStore(listOf(
            entry("a", "甲", pkgs = listOf("p.a"), aliases = listOf("购物助手甲")),
            entry("b", "乙乙", pkgs = listOf("p.b"), aliases = listOf("乙乙")),
        ))
        val text = "用乙乙比一下购物助手甲上的价格"
        assertEquals("a", store.matchTask(text, 1).single().name) // 命中词更长
        assertEquals("b", store.matchTask(text, 1, installed = listOf("p.b" to "乙乙")).single().name)
    }

    @Test fun linksComeFromTheIndex() {
        val loader = fixtureLoader(mapOf(appMd("demo", listOf("com.example.app"),
            extra = "links: [\"扫一扫 | someapp://scan\", \"活动页 | https://m.example.com/a | com.example.app\"]\n")))
        val e = KnowledgeStore(KnowledgeStore.loadBundled(loader)).byPackage("com.example.app")!!
        assertEquals(listOf(Link("扫一扫", "someapp://scan"), Link("活动页", "https://m.example.com/a", "com.example.app")), e.links)
        assertEquals("正文demo", e.body)
    }

    @Test fun bundledProfilesKeepTheirLinks() {
        // 内置说明书和技能索引合并后,说明书里的链接不丢(没有链接的为空列表)
        bundled.forEach { e -> e.links.forEach { assertNull(com.ganguydhrubo.aira.core.tool.LinkPolicy.check(null, it.uri, it.packageName)) } }
    }

    @Test fun domainMatchesRespectWordBoundaries() {
        val x = KnowledgeStore(listOf(entry("x", domains = listOf("x.com"))))
        assertTrue(x.matchTask("打开 https://x.com/home").any { it.name == "x" })
        assertTrue(x.matchTask("比较 xx.com 和 xxx.com").none { it.name == "x" })
        val zhihu = KnowledgeStore(listOf(entry("zhihu", domains = listOf("zhihu.com"))))
        assertTrue(zhihu.matchTask("看看 nzhihu.com").none { it.name == "zhihu" })
        assertTrue(zhihu.matchTask("打开 https://www.zhihu.com/question/1").any { it.name == "zhihu" })
    }
}
