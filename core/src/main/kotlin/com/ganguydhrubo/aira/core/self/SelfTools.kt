package com.ganguydhrubo.aira.core.self

import com.ganguydhrubo.aira.core.prompt.Prompts
import com.ganguydhrubo.aira.core.action.ActionRisk
import com.ganguydhrubo.aira.core.action.Risk as ApprovalRisk
import com.ganguydhrubo.aira.core.tool.AgentTool
import com.ganguydhrubo.aira.core.tool.ArgError
import com.ganguydhrubo.aira.core.tool.Args
import com.ganguydhrubo.aira.core.tool.Concurrency
import com.ganguydhrubo.aira.core.tool.Risk
import com.ganguydhrubo.aira.core.tool.ToolContext
import com.ganguydhrubo.aira.core.tool.ToolDef
import com.ganguydhrubo.aira.core.tool.ToolExecution
import com.ganguydhrubo.aira.core.tool.Verb
import com.ganguydhrubo.aira.core.tool.argStr
import com.ganguydhrubo.aira.core.tool.bool
import com.ganguydhrubo.aira.core.tool.fail
import com.ganguydhrubo.aira.core.tool.ok
import com.ganguydhrubo.aira.core.tool.str
import com.ganguydhrubo.aira.core.workspace.SandboxPaths
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * 自身工具:读自身状态、打开自己的页面/文件、发通知、写剪贴板。
 *
 * 这些工具不动屏幕(offScreen),不经过手机/网页那套屏幕关卡;它们的授权是用户设置里的
 * 「允许 Aira 操作自己的界面」,敏感动作还要在 execute 里用 [com.ganguydhrubo.aira.core.tool.AskPort] 当次问用户。
 * 手机工具仍然不能碰 Aira 自己的包(见 ConfirmDecision.ownApp),这里不是绕过它,而是另一条正门。
 */
internal interface SelfTool : AgentTool {
    override val offScreen: Boolean get() = true
    override fun available(ctx: ToolContext) = ctx.config.self != null
}

private fun port(ctx: ToolContext): SelfPort =
    ctx.config.self ?: error("self tools are only offered to UI tasks")

private fun operate(ctx: ToolContext): SelfPort =
    ctx.config.selfOperate ?: error("self tools are only offered when the user allows it")

/** 只读:自身状态。 */
internal object AiraStatus : SelfTool {
    override val verb = Verb.SelfStatus
    override val label = "查看自身状态"
    override val risk = Risk.Read
    /** 只读、不碰屏幕;走只读调用那条路,不经过写操作入口。 */
    override val offScreen = false
    override val concurrency = Concurrency.ParallelSafe
    override val def = ToolDef("aira_status", Prompts.tool("aira_status"))

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val status = port(ctx).status()
        return ok { put("status", status.toJson()) }
    }
}

/** 打开 Aira 自己的一页;只改用户眼前的页面,不改任何设置。 */
internal object AiraOpenScreen : SelfTool {
    override val verb = Verb.SelfOpenScreen
    override val label = "打开 Aira 的页面"
    override val risk = Risk.Write
    override val def = ToolDef(
        "aira_open_screen",
        Prompts.tool("aira_open_screen"),
        mapOf(
            "screen" to str("Which Aira screen to open.", SelfScreen.entries.map { it.wire }),
            "note" to str("Optional short reason shown to the user, in the user's language."),
        ),
        listOf("screen"),
    )

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val wire = a.str("screen")!!
        val screen = SelfScreen.of(wire) ?: throw ArgError("screen", "must be one of ${SelfScreen.entries.joinToString("|") { it.wire }}")
        val note = a.str("note", max = 200)
        return ToolExecution(operate(ctx).openScreen(screen, note).toJson())
    }
}

/** 用 App 内查看器打开工作区里的文件。 */
internal object AiraOpenFile : SelfTool {
    override val verb = Verb.SelfOpenFile
    override val label = "打开文件"
    override val risk = Risk.Write
    override val def = ToolDef(
        "aira_open_file",
        Prompts.tool("aira_open_file"),
        mapOf(
            "path" to str("Sandbox path of a file under /workspace, e.g. /workspace/out/report.pdf."),
            "name" to str("Optional display name; defaults to the file name."),
        ),
        listOf("path"),
    )

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val path = a.str("path", max = 1000)!!
        if (!path.startsWith(SandboxPaths.WORKSPACE + "/") || path.split('/').any { it == ".." }) {
            return fail("invalid_path", "path must be a file under ${SandboxPaths.WORKSPACE}/")
        }
        val name = a.str("name", max = 200)
        return ToolExecution(operate(ctx).openFile(path, name).toJson())
    }
}

/** 给用户发一条通知;每任务最多 3 条。 */
internal object AiraNotify : SelfTool {
    override val verb = Verb.SelfNotify
    override val label = "发通知"
    override val risk = Risk.Write
    override val def = ToolDef(
        "aira_notify",
        Prompts.tool("aira_notify"),
        mapOf(
            "title" to str("Short notification title, in the user's language."),
            "body" to str("What happened, in one or two sentences, in the user's language."),
            "screen" to str("Optional Aira screen to open from the notification.", SelfScreen.entries.map { it.wire }),
        ),
        listOf("title", "body"),
    )

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        if (ctx.selfNotifies >= MAX_NOTIFY) return fail("rate_limited", "At most $MAX_NOTIFY notifications per task.")
        val title = a.str("title", max = 120)!!
        val body = a.str("body", max = 500)!!
        val screen = a.str("screen")?.let { wire ->
            SelfScreen.of(wire) ?: throw ArgError("screen", "must be one of ${SelfScreen.entries.joinToString("|") { it.wire }}")
        }
        val result = operate(ctx).notify(title, body, screen)
        if (result.notified) ctx.selfNotifies++
        return ToolExecution(result.toJson())
    }

    private const val MAX_NOTIFY = 3
}

/** 写剪贴板;不读。 */
internal object AiraCopy : SelfTool {
    override val verb = Verb.SelfCopy
    override val label = "复制文字"
    override val risk = Risk.Write
    override val def = ToolDef(
        "aira_copy",
        Prompts.tool("aira_copy"),
        mapOf("text" to str("The text to put on the clipboard.")),
        listOf("text"),
    )

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val text = a.str("text", max = 4000)!!
        if (!operate(ctx).copy(text)) return fail("copy_failed", "The clipboard is not available.")
        return ok { put("copied", text.length) }
    }
}

/** 在用户看得见的浏览器里打开一页;用户当次点头才开。 */
internal object AiraOpenInBrowser : SelfTool {
    override val verb = Verb.SelfOpenBrowser
    override val label = "在浏览器里打开"
    override val risk = Risk.External
    override val approvalRisk = ApprovalRisk(ActionRisk.External)
    override val logged = true
    override val def = ToolDef(
        "aira_open_in_browser",
        Prompts.tool("aira_open_in_browser"),
        mapOf(
            "url" to str("http/https URL to open in the user's browser."),
            "show" to bool("Switch to the page now (default true). Background tabs need multi-tab support."),
        ),
        listOf("url"),
    )

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val url = a.str("url", max = SelfUrlPolicy.MAX_URL)!!.trim()
        SelfUrlPolicy.check(url)?.let { return fail("invalid_url", it) }
        val show = a.bool("show", true)
        val port = operate(ctx)
        val state = port.browserState()
        if (state.loaned) return fail("page_in_use", "That page is borrowed by a task right now; do not take it over.")
        if (!show && !state.multiTab) return fail("not_supported", "Background tabs need multi-tab; open the page in the browser instead.")
        if (ctx.selfBrowserOps >= MAX_BROWSER) return fail("rate_limited", "At most $MAX_BROWSER browser actions per task.")
        val ask = ctx.ask ?: return fail("needs_user", "No one can be asked to approve opening the page; nothing was opened.")
        val consent = port.browserConsent(url, null, state.pageOpen && !state.multiTab)
        ctx.selfBrowserOps++
        val answer = ask.awaitAnswer(consent.prompt, listOf(consent.allow, consent.deny))
        if (answer != consent.allow) return fail("declined", "The user did not approve opening the page.")
        return ToolExecution(port.openInBrowser(url, show, a.str("reason", max = 200)).toJson())
    }

    override fun logTarget(o: kotlinx.serialization.json.JsonObject, r: ToolExecution, ctx: ToolContext): String? =
        o.argStr("url")?.let(SelfUrlPolicy::host) ?: o.argStr("url")
}

/** 用浏览器的默认搜索引擎搜索并打开结果页。 */
internal object AiraSearchInBrowser : SelfTool {
    override val verb = Verb.SelfSearch
    override val label = "在浏览器里搜索"
    override val risk = Risk.External
    override val approvalRisk = ApprovalRisk(ActionRisk.External)
    override val logged = true
    override val def = ToolDef(
        "aira_search_in_browser",
        Prompts.tool("aira_search_in_browser"),
        mapOf(
            "query" to str("What to search for, 1-200 characters."),
            "show" to bool("Switch to the results now (default true)."),
        ),
        listOf("query"),
    )

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val query = a.str("query", max = 200)!!.trim()
        if (query.isEmpty()) return fail("invalid_query", "query must not be blank")
        val show = a.bool("show", true)
        val port = operate(ctx)
        val state = port.browserState()
        if (state.loaned) return fail("page_in_use", "That page is borrowed by a task right now; do not take it over.")
        if (!show && !state.multiTab) return fail("not_supported", "Background tabs need multi-tab; open the results in the browser instead.")
        if (ctx.selfBrowserOps >= MAX_BROWSER) return fail("rate_limited", "At most $MAX_BROWSER browser actions per task.")
        val ask = ctx.ask ?: return fail("needs_user", "No one can be asked to approve the search; nothing was opened.")
        val consent = port.browserConsent("", query, state.pageOpen && !state.multiTab)
        ctx.selfBrowserOps++
        val answer = ask.awaitAnswer(consent.prompt, listOf(consent.allow, consent.deny))
        if (answer != consent.allow) return fail("declined", "The user did not approve the search.")
        return ToolExecution(port.searchInBrowser(query, show).toJson())
    }

    override fun logTarget(o: kotlinx.serialization.json.JsonObject, r: ToolExecution, ctx: ToolContext): String? = o.argStr("query")
}

/** 站点登记表:哪些已登录、哪些最近被验证码/风控拦过。只读。 */
internal object AiraSites : SelfTool {
    override val verb = Verb.SelfSites
    override val label = "查看网站登录"
    override val risk = Risk.Read
    /** 走只读调用那条路,不碰屏幕。 */
    override val offScreen = false
    override val concurrency = Concurrency.ParallelSafe
    override val def = ToolDef("aira_sites", Prompts.tool("aira_sites"))

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val sites = operate(ctx).sites()
        return ok {
            putJsonArray("sites") {
                sites.forEach { s ->
                    addJsonObject {
                        put("host", s.host)
                        put("loggedIn", s.loggedIn)
                        if (s.challenged) put("challenged", true)
                    }
                }
            }
        }
    }
}

/** 用户浏览器此刻的页面;只读。 */
internal object AiraUserPage : SelfTool {
    override val verb = Verb.SelfUserPage
    override val label = "查看正在看的网页"
    override val risk = Risk.Read
    override val offScreen = false
    override val concurrency = Concurrency.ParallelSafe
    override val def = ToolDef("aira_user_page", Prompts.tool("aira_user_page"))

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val state = operate(ctx).browserState()
        return ok {
            put("pageOpen", state.pageOpen)
            state.host?.let { put("host", it) }
            state.title?.let { put("title", it) }
            state.url?.let { put("url", it) }
            put("loaned", state.loaned)
            put("multiTab", state.multiTab)
        }
    }
}

/** 打开「给 Aira 登录」页,让用户自己登录。 */
internal object AiraLoginSite : SelfTool {
    override val verb = Verb.SelfLogin
    override val label = "请你登录网站"
    override val risk = Risk.External
    override val approvalRisk = ApprovalRisk(ActionRisk.External)
    override val logged = true
    override val def = ToolDef(
        "aira_login_site",
        Prompts.tool("aira_login_site"),
        mapOf("host" to str("Site host to sign in to, e.g. example.com.")),
        listOf("host"),
    )

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val raw = a.str("host", max = 300)!!.trim()
        val host = SelfUrlPolicy.hostOf(raw) ?: return fail("invalid_host", "host must be a site host such as example.com")
        val port = operate(ctx)
        val state = port.browserState()
        if (state.loaned) return fail("page_in_use", "That page is borrowed by a task right now; do not take it over.")
        if (ctx.selfBrowserOps >= MAX_BROWSER) return fail("rate_limited", "At most $MAX_BROWSER browser actions per task.")
        val ask = ctx.ask ?: return fail("needs_user", "No one can be asked to approve the sign-in page; nothing was opened.")
        val consent = port.loginConsent(host, state.pageOpen)
        ctx.selfBrowserOps++
        val answer = ask.awaitAnswer(consent.prompt, listOf(consent.allow, consent.deny))
        if (answer != consent.allow) return fail("declined", "The user did not approve opening the sign-in page.")
        return ToolExecution(port.loginSite(host).toJson())
    }

    override fun logTarget(o: kotlinx.serialization.json.JsonObject, r: ToolExecution, ctx: ToolContext): String? = o.argStr("host")
}

/** 用户说登录好了:核对一次并记下登录状态。 */
internal object AiraConfirmLogin : SelfTool {
    override val verb = Verb.SelfConfirmLogin
    override val label = "核对登录"
    override val risk = Risk.Write
    override val logged = true
    override val def = ToolDef("aira_confirm_login", Prompts.tool("aira_confirm_login"))

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution =
        ToolExecution(operate(ctx).confirmLogin().toJson())
}

/** 常用网站:list / add / remove;改动可逆,不要当次同意。 */
internal object AiraFavorites : SelfTool {
    override val verb = Verb.SelfFavorites
    override val label = "常用网站"
    override val risk = Risk.Write
    override val logged = true
    override val def = ToolDef(
        "aira_favorites",
        Prompts.tool("aira_favorites"),
        mapOf(
            "op" to str("list (default), add, or remove.", listOf("list", "add", "remove")),
            "host" to str("Site host for add/remove, e.g. example.com."),
        ),
    )

    override fun available(ctx: ToolContext) = ctx.config.selfOperate != null

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val op = a.str("op") ?: "list"
        if (op !in setOf("list", "add", "remove")) throw ArgError("op", "must be list, add or remove")
        val host = if (op == "list") null else {
            val raw = a.str("host", max = 300) ?: return fail("missing_host", "host is required for add and remove.")
            SelfUrlPolicy.hostOf(raw) ?: return fail("invalid_host", "host must be a site host such as example.com")
        }
        val list = operate(ctx).favorites(op, host)
        return ok {
            putJsonArray("favorites") {
                list.forEach { f ->
                    addJsonObject {
                        put("host", f.host)
                        put("loggedIn", f.loggedIn)
                    }
                }
            }
        }
    }

    override fun logTarget(o: kotlinx.serialization.json.JsonObject, r: ToolExecution, ctx: ToolContext): String? =
        listOfNotNull(o.argStr("op"), o.argStr("host")).joinToString(" ").ifEmpty { null }
}

private const val MAX_BROWSER = 5

internal object SelfGroup {
    val tools: List<AgentTool> = listOf(
        AiraStatus, AiraOpenScreen, AiraOpenFile, AiraNotify, AiraCopy,
        AiraOpenInBrowser, AiraSearchInBrowser, AiraSites, AiraUserPage, AiraLoginSite, AiraConfirmLogin, AiraFavorites,
    )
}
