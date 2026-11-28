package com.ganguydhrubo.aira.task

import com.ganguydhrubo.aira.core.run.ChannelPeer
import com.ganguydhrubo.aira.core.run.Delivery
import com.ganguydhrubo.aira.core.run.DeliveryRecord
import com.ganguydhrubo.aira.core.run.DeliveryState
import com.ganguydhrubo.aira.core.run.Outbox
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.nio.file.Files

class FileOutboxStoreTest {
    @Test fun failedBatchWriteKeepsTheDiskAndMemoryAtThePreviousCommittedState() = runTest {
        val dir = Files.createTempDirectory("aira-outbox-failure").toFile()
        try {
            val file = dir.resolve("outbox.json")
            val peer = ChannelPeer("telegram", "owner", "thread")
            val previous = DeliveryRecord(peer, Delivery.Notice("earlier", "thread", "hello"), DeliveryState.Delivered)
            val store = FileOutboxStore(file)
            store.save(previous)
            val disk = file.readText()
            val result = Delivery.Result("result:1", "thread", "Screenshot")
            val image = Delivery.Image("result:1:image:0", "thread", "content://downloads/1", "screen.png", 12L)
            // Make the temporary output unwritable without changing the last committed file.
            assertTrue(dir.resolve("outbox.json.tmp").mkdir())
            val outbox = Outbox(store)
            try {
                outbox.prepare(peer, listOf(result, image))
                fail("the batch must not be considered prepared when persistence fails")
            } catch (_: IOException) { }
            assertEquals(listOf(previous), store.all())
            assertEquals(disk, file.readText())
            assertEquals(listOf(previous), FileOutboxStore(file).all())
            assertTrue(dir.resolve("outbox.json.tmp").delete())
            outbox.prepare(peer, listOf(result, image))
            assertEquals(listOf(previous, DeliveryRecord(peer, result, DeliveryState.Pending),
                DeliveryRecord(peer, image, DeliveryState.Pending)), FileOutboxStore(file).all())
        } finally { dir.deleteRecursively() }
    }

    @Test fun textAndImageBatchSurvivesRestartWithTheExportIdentity() {
        val dir = Files.createTempDirectory("aira-image-outbox").toFile()
        try {
            val file = dir.resolve("outbox.json")
            val peer = ChannelPeer("telegram", "owner", "thread")
            val records = listOf(
                DeliveryRecord(peer, Delivery.Result("result:1", "thread", "Screenshot"), DeliveryState.Delivered),
                DeliveryRecord(peer, Delivery.Image("result:1:image:0", "thread", "content://downloads/1", "screen.png", 12L, "a".repeat(64)), DeliveryState.Attempting),
                DeliveryRecord(peer, Delivery.Image("result:1:image:1", "thread", "content://downloads/2", "photo.jpg", 24L), DeliveryState.Pending),
            )
            FileOutboxStore(file).saveAll(records)
            assertEquals(records, FileOutboxStore(file).all())
        } finally { dir.deleteRecursively() }
    }

    @Test fun attemptingDeliverySurvivesRestartWithoutLosingItsIdentity() {
        val dir = Files.createTempDirectory("aira-outbox").toFile()
        try {
            val file = dir.resolve("outbox.json")
            val peer = ChannelPeer("local", "owner", "thread")
            val delivery = Delivery.Result("result:1", "thread", "Answer", title = "Task done")
            FileOutboxStore(file).save(DeliveryRecord(peer, delivery, DeliveryState.Attempting))
            val restored = FileOutboxStore(file).get("result:1")
            assertEquals(DeliveryRecord(peer, delivery, DeliveryState.Attempting), restored)
        } finally { dir.deleteRecursively() }
    }
}
