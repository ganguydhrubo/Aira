package com.ganguydhrubo.aira.telegram

import com.ganguydhrubo.aira.AiraApp
import com.ganguydhrubo.aira.core.run.Channel
import com.ganguydhrubo.aira.core.run.Delivery
import com.ganguydhrubo.aira.core.run.DeliveryState
import com.ganguydhrubo.aira.core.run.Outbox
import com.ganguydhrubo.aira.core.cap.telegram.TelegramAccount
import com.ganguydhrubo.aira.core.cap.telegram.TelegramOwnerPort
import com.ganguydhrubo.aira.core.cap.telegram.TelegramOwnerResult
import java.security.MessageDigest

/**
 * 机主结果推送的出口:构造时快照账号与投递目标,发送时重读比对——运行中途换绑、停用或线程失效都判
 * 不可用且一次都不发。投递经 [Outbox]:同一运行同一段文本只发一次,结局不明也不自动重发。
 */
class AndroidTelegramOwnerPort internal constructor(
    private val store: TelegramStore,
    private val channel: Channel,
    private val outbox: Outbox,
    private val runKey: String,
) : TelegramOwnerPort {
    constructor(app: AiraApp, runKey: String) : this(app.telegram.store, app.telegram.channel, app.outbox, runKey)

    private val expectedAccount: TelegramAccount? = store.account()
    private val expectedPeer = store.boundOwnerPeer()

    override suspend fun send(text: String): TelegramOwnerResult {
        val account = expectedAccount
        val peer = expectedPeer
        if (account == null || peer == null) {
            return TelegramOwnerResult(TelegramOwnerResult.Status.Unavailable,
                "No Telegram chat is bound to the owner; nothing was sent.")
        }
        if (store.account() != account || store.boundOwnerPeer() != peer) {
            return TelegramOwnerResult(TelegramOwnerResult.Status.Unavailable,
                "The Telegram link changed or was paused during this run; nothing was sent.")
        }
        val id = "owner-send:$runKey:${sha256(peer.threadId + text)}"
        val record = outbox.deliver(channel, peer, Delivery.Result(id, peer.threadId, text))
        return when (record.state) {
            DeliveryState.Delivered -> TelegramOwnerResult(TelegramOwnerResult.Status.Delivered)
            DeliveryState.Uncertain -> TelegramOwnerResult(TelegramOwnerResult.Status.Uncertain,
                "The message was handed to the channel but delivery was not confirmed; it was not resent.")
            else -> TelegramOwnerResult(TelegramOwnerResult.Status.Failed,
                "The Telegram channel did not accept the message.")
        }
    }

    private companion object {
        fun sha256(text: String): String = MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }.take(16)
    }
}
