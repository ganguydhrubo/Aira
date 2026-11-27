package com.ganguydhrubo.aira.frontdesk

import android.content.Context
import com.ganguydhrubo.aira.core.model.ChatModel
import com.ganguydhrubo.aira.core.model.ModelRole
import com.ganguydhrubo.aira.core.model.ModelSession
import com.ganguydhrubo.aira.settings.ModelChoice
import com.ganguydhrubo.aira.settings.ProviderStore
import com.ganguydhrubo.aira.settings.modelFor
import com.ganguydhrubo.aira.voice.model.ThinkModel
import com.ganguydhrubo.aira.voice.model.VoiceModelEnvironment

/**
 * 前台(聊天和通话)用的思考模型:按「通话方式」里选的轻/主模型取;选了轻模型但没配时退回主模型。
 * 两个都没配返回 null,入口据此引导用户先去设置,不静默失败。
 */
fun deskModel(
    context: Context,
    session: ModelSession = ModelSession.fresh(),
    mainOverride: ModelChoice? = null,
): ChatModel? {
    val think = VoiceModelEnvironment.get(context).catalog.state.value.selections.think
    val store = ProviderStore.get(context)
    return when (think) {
        ThinkModel.Light -> store.modelFor(ModelRole.Light, session, mainOverride) ?: store.modelFor(ModelRole.Main, session, mainOverride)
        ThinkModel.Main -> store.modelFor(ModelRole.Main, session, mainOverride)
    }
}
