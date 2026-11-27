package com.ganguydhrubo.aira.core.cap.image

import com.ganguydhrubo.aira.core.capability.Capability
import com.ganguydhrubo.aira.core.capability.CapabilityKey
import com.ganguydhrubo.aira.core.capability.CapabilityStatus
import com.ganguydhrubo.aira.core.capability.PromptEnv
import com.ganguydhrubo.aira.core.prompt.Prompts
import com.ganguydhrubo.aira.core.tool.AgentTool
import com.ganguydhrubo.aira.core.tool.ToolContext

/** 图片能力:生成、改图、存相册。没有配置图片模型时整个能力不装,工具也不发给模型。 */
const val IMAGE = "image"

internal class ImageCapability(
    val port: ImagePort?,
    val saveEnabled: Boolean,
    private val askBeforeSave: Boolean,
    val maxPerTask: Int,
) : Capability {
    override val id = IMAGE
    override val folds = true

    private var used = 0

    override fun status(): CapabilityStatus =
        if (port != null) CapabilityStatus.Available else CapabilityStatus.Unavailable("no image model")

    override fun tools(): List<AgentTool> = ImageGroup.forTask(saveEnabled, askBeforeSave)

    override fun sections(env: PromptEnv): List<String> =
        if (env.isOpen(id) && tools().any { it.name in env.offered }) listOf(env.gate(Prompts.capability(IMAGE))) else emptyList()

    override fun onTaskStart(ctx: ToolContext) {
        ctx[KEY] = this
    }

    /** 本任务已经生成/要生成的张数;超过上限时工具直接拒绝,不去花服务商的钱。 */
    @Synchronized fun reserve(count: Int): Boolean {
        if (count < 1 || used + count > maxPerTask) return false
        used += count
        return true
    }

    @Synchronized fun refund(count: Int) {
        used = (used - count).coerceAtLeast(0)
    }

    @Synchronized fun used(): Int = used

    companion object {
        val KEY = CapabilityKey<ImageCapability>(IMAGE)
    }
}
