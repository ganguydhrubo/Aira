package com.ganguydhrubo.aira.task

import com.ganguydhrubo.aira.core.model.ChatModel
import com.ganguydhrubo.aira.core.model.ModelRole
import com.ganguydhrubo.aira.core.model.ModelSession
import com.ganguydhrubo.aira.settings.ModelChoice
import com.ganguydhrubo.aira.settings.Provider
import com.ganguydhrubo.aira.settings.ProviderStore
import com.ganguydhrubo.aira.settings.effectiveRoles
import com.ganguydhrubo.aira.settings.lightFallbackChain
import com.ganguydhrubo.aira.settings.mainFallbackChain
import com.ganguydhrubo.aira.settings.mainProviderId
import com.ganguydhrubo.aira.settings.modelFor
import com.ganguydhrubo.aira.settings.untestedMainProvider
import com.ganguydhrubo.aira.settings.usableMainChoice
import com.ganguydhrubo.aira.settings.visionFallbackChain

/** 一次任务不可变的模型路由；任务开始后设置或对话覆盖再变也不会改这份实例。 */
data class TaskModelSnapshot(
    val main: ChatModel?,
    val light: ChatModel?,
    val vision: ChatModel?,
    val mainFallbacks: List<ChatModel> = emptyList(),
    val mainProviderIds: List<String?> = emptyList(),
    val lightFallbacks: List<ChatModel> = emptyList(),
    val visionFallbacks: List<ChatModel> = emptyList(),
    val retestProvider: Provider? = null,
    val routeLightSeparately: Boolean = true,
    val conversationChoice: ModelChoice? = null,
) {
    companion object {
        /** 在同一个 ProviderStore 锁内解析一次完整路由，避免设置写入拼出半新半旧的快照。 */
        fun capture(store: ProviderStore, session: ModelSession, mainOverride: ModelChoice? = null): TaskModelSnapshot =
            synchronized(store) {
                val choices = store.effectiveRoles(mainOverride)
                val (mainFallbacks, fallbackProviders) = store.mainFallbackChain(session, mainOverride)
                TaskModelSnapshot(
                    main = store.modelFor(ModelRole.Main, session, mainOverride),
                    light = store.modelFor(ModelRole.Light, session, mainOverride),
                    vision = store.modelFor(ModelRole.Vision, session, mainOverride),
                    mainFallbacks = mainFallbacks.toList(),
                    mainProviderIds = (listOf(store.mainProviderId(mainOverride)) + fallbackProviders).toList(),
                    lightFallbacks = store.lightFallbackChain(session, mainOverride).toList(),
                    visionFallbacks = store.visionFallbackChain(session, mainOverride).toList(),
                    retestProvider = store.untestedMainProvider(mainOverride),
                    routeLightSeparately = !choices.sameModel(ModelRole.Main, ModelRole.Light),
                    conversationChoice = store.usableMainChoice(mainOverride),
                )
            }
    }
}
