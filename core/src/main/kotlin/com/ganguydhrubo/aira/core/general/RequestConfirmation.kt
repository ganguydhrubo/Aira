package com.ganguydhrubo.aira.core.general

import com.ganguydhrubo.aira.core.safety.gate.ConfirmationRequest
import com.ganguydhrubo.aira.core.safety.gate.IrrevAction
import com.ganguydhrubo.aira.core.prompt.Prompts
import com.ganguydhrubo.aira.core.tool.ArgError
import com.ganguydhrubo.aira.core.tool.Args
import com.ganguydhrubo.aira.core.tool.CONFIRM_ACTIONS
import com.ganguydhrubo.aira.core.tool.CONFIRM_FIELD_MAX
import com.ganguydhrubo.aira.core.tool.HINT_PAYMENT_TAKEOVER
import com.ganguydhrubo.aira.core.tool.Outcome
import com.ganguydhrubo.aira.core.tool.Risk
import com.ganguydhrubo.aira.core.tool.ToolContext
import com.ganguydhrubo.aira.core.tool.ToolDef
import com.ganguydhrubo.aira.core.tool.ToolExecution
import com.ganguydhrubo.aira.core.tool.Verb
import com.ganguydhrubo.aira.core.tool.capField
import com.ganguydhrubo.aira.core.tool.fail
import com.ganguydhrubo.aira.core.tool.okObj
import com.ganguydhrubo.aira.core.tool.str
import kotlinx.serialization.json.put
import com.ganguydhrubo.aira.core.screen.uistate.confirmTarget
import com.ganguydhrubo.aira.core.screen.uistate.focus
import com.ganguydhrubo.aira.core.screen.uistate.lastSnapshot
import com.ganguydhrubo.aira.core.screen.uistate.session

/**
 * 花钱的那一步(下单、屏上有应付金额时的提交等)前请用户同意;询问时只有这一种要问。付款本身不在这里,交给用户亲手付。
 * 申报的前台核对用此刻重新读的一屏(不登记编号,不动模型手里的 ref)。
 */
internal object RequestConfirmation : SurfaceAware {
    override val verb = Verb.RequestConfirmation
    override val label = "请你同意"
    override val risk = Risk.Read
    override val def = ToolDef("request_confirmation",
        Prompts.tool("request_confirmation"),
        mapOf(
            "action" to str("Kind of action that spends the money (order for placing an order or booking).", CONFIRM_ACTIONS),
            "target" to str("Who or where the money goes (shop, seller, service), as shown on screen. At most $CONFIRM_FIELD_MAX characters."),
            "content" to str("What is bought: item, spec, quantity. At most $CONFIRM_FIELD_MAX characters."),
            "amount" to str("Amount and currency the user will pay, as shown on screen."),
            "app" to str(APP_NOTE),
        ), listOf("action", "target", "content", "amount", "app"))

    /** app 参数写什么按本任务的屏说:一块屏时写那块的写法,几块屏时各写一句。 */
    override fun def(ctx: ToolContext): ToolDef {
        val base = super.def(ctx)
        val surfaces = ctx.focus.surfaces
        val how = if (surfaces.size == 1) surfaces.single().targetNote
        else surfaces.joinToString("; ") { "on the ${it.id} screen, ${it.targetNote}" }
        return ToolDef(base.name, base.description, base.props + ("app" to str("$APP_NOTE: $how.")), base.required)
    }

    private const val APP_NOTE = "Where the action happens, as on screen now"

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        checkSurface(a, ctx)
        val action = a.str("action")!!.trim()
        val kind = IrrevAction.entries.firstOrNull { it.name == action }
            ?: throw ArgError("action", "must be one of ${CONFIRM_ACTIONS.joinToString("|")}")
        if (kind == IrrevAction.pay) {
            ctx.refusedPay = ConfirmationRequest(kind, capField(a.str("target", max = 10000)!!), capField(a.str("content", max = 10000)!!),
                a.str("amount", max = 10000)?.let(::capField), a.str("app")!!.trim())
            return fail("payment_requires_takeover", HINT_PAYMENT_TAKEOVER)
        }
        // 只有花钱要问:写不出金额的操作不用申请,直接做
        if (a.str("amount")?.any(Char::isDigit) != true) throw ArgError("amount",
            "required: the total the user will pay, as shown on screen. An action that spends no money needs no approval: do it directly")
        val app = ctx.confirmTarget(a.str("app")!!.trim())
        val req = ConfirmationRequest(
            kind, capField(a.str("target", max = 10000)!!), capField(a.str("content", max = 10000)!!),
            a.str("amount", max = 10000)?.let(::capField), app,
        )
        val fg = ctx.session.peek()?.packageName ?: ctx.lastSnapshot?.packageName
        return ToolExecution(okObj { put("asked", true) }, outcome = Outcome.Confirm(req, fg))
    }
}
