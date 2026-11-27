package com.ganguydhrubo.aira.telegram

import com.ganguydhrubo.aira.core.run.Channel
import com.ganguydhrubo.aira.core.run.ChannelInputSink
import com.ganguydhrubo.aira.core.run.ChannelPeer
import com.ganguydhrubo.aira.core.run.Delivery
import com.ganguydhrubo.aira.core.run.Outbox
import com.ganguydhrubo.aira.core.run.SendOutcome
import com.ganguydhrubo.aira.core.cap.telegram.TelegramAccount
import com.ganguydhrubo.aira.core.cap.telegram.TelegramOwnerResult
import com.ganguydhrubo.aira.settings.KeyCipher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** 机主结果推送发送器:绑定快照比对(TOCTOU)与经 Outbox 的同文幂等。 */
class TelegramOwnerSenderTest {
    @get:Rule val tmp = TemporaryFolder()
    private val cipher = object : KeyCipher {
        override fun encrypt(plain: ByteArray): ByteArray = plain
        override fun decrypt(data: ByteArray): ByteArray = data
    }

    /** 只数调用次数的假渠道;断言换绑/停用后一次都没被调到。 */
    private class CountingChannel(var outcome: SendOutcome = SendOutcome.Delivered) : Channel {
        override val id = TelegramChannel.ID
        override val confirms = false
        var sends = 0
            private set

        override fun start(sink: ChannelInputSink) = AutoCloseable {}
        override suspend fun send(peer: ChannelPeer, delivery: Delivery): SendOutcome {
            sends++
            return outcome
        }
    }

    private fun boundStore(dir: File) = TelegramStore(dir, cipher).apply {
        saveAccount(TelegramAccount("secret", "https://example.test", "bot", "owner"))
        setEnabled(true)
        rememberContext("owner", "context")
        peer("owner")
    }

    @Test fun deliversToTheBoundOwnerWhileTheBindingStaysTheSame() = runTest {
        val store = boundStore(tmp.newFolder())
        val channel = CountingChannel()
        val outbox = Outbox()
        val port = AndroidTelegramOwnerPort(store, channel, outbox, "run-1")

        val result = port.send("任务结果")

        assertEquals(TelegramOwnerResult.Status.Delivered, result.status)
        assertEquals(1, channel.sends)
        val record = outbox.records().single()
        assertTrue(record.delivery.id.startsWith("owner-send:run-1:"))
        assertEquals(store.boundOwnerPeer(), record.peer)
    }

    @Test fun rebindingAfterTheSnapshotMakesSendUnavailableWithoutSending() = runTest {
        val store = boundStore(tmp.newFolder())
        val channel = CountingChannel()
        val port = AndroidTelegramOwnerPort(store, channel, Outbox(), "run-1")
        store.saveAccount(TelegramAccount("new-token", "https://example.test", "bot", "owner"))

        val result = port.send("任务结果")

        assertEquals(TelegramOwnerResult.Status.Unavailable, result.status)
        assertEquals(0, channel.sends)
    }

    @Test fun disablingTheChannelAfterTheSnapshotMakesSendUnavailableWithoutSending() = runTest {
        val store = boundStore(tmp.newFolder())
        val channel = CountingChannel()
        val port = AndroidTelegramOwnerPort(store, channel, Outbox(), "run-1")
        store.setEnabled(false)

        val result = port.send("任务结果")

        assertEquals(TelegramOwnerResult.Status.Unavailable, result.status)
        assertEquals(0, channel.sends)
    }

    @Test fun sendingTheSameTextTwiceDeliversOnceAndReturnsTheFirstStatus() = runTest {
        val store = boundStore(tmp.newFolder())
        val channel = CountingChannel()
        val port = AndroidTelegramOwnerPort(store, channel, Outbox(), "run-1")

        val first = port.send("任务结果")
        val second = port.send("任务结果")

        assertEquals(TelegramOwnerResult.Status.Delivered, first.status)
        assertEquals(first.status, second.status)
        assertEquals(1, channel.sends)
    }

    @Test fun anUncertainDeliveryIsNotResent() = runTest {
        val store = boundStore(tmp.newFolder())
        val channel = CountingChannel(SendOutcome.Uncertain)
        val port = AndroidTelegramOwnerPort(store, channel, Outbox(), "run-1")

        val first = port.send("任务结果")
        val second = port.send("任务结果")

        assertEquals(TelegramOwnerResult.Status.Uncertain, first.status)
        assertEquals(TelegramOwnerResult.Status.Uncertain, second.status)
        assertEquals(1, channel.sends)
    }

    @Test fun aRunWithoutABoundOwnerChatIsUnavailableWithoutSending() = runTest {
        val store = TelegramStore(tmp.newFolder(), cipher)
        val channel = CountingChannel()
        val port = AndroidTelegramOwnerPort(store, channel, Outbox(), "run-1")

        val result = port.send("任务结果")

        assertEquals(TelegramOwnerResult.Status.Unavailable, result.status)
        assertEquals(0, channel.sends)
    }
}
