package com.ganguydhrubo.aira.telegram

import com.ganguydhrubo.aira.cap.sandbox.JVM_GATE
import com.ganguydhrubo.aira.core.run.ChannelPeer
import com.ganguydhrubo.aira.core.run.Delivery
import com.ganguydhrubo.aira.core.cap.telegram.TelegramImage
import com.ganguydhrubo.aira.core.cap.telegram.TelegramImages
import com.ganguydhrubo.aira.core.workspace.SandboxTree
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.Base64

class TelegramImageFilesTest {
    @get:Rule val tmp = TemporaryFolder()
    private val png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR4nGNgYPj/HwADAgH/5ncLrgAAAABJRU5ErkJggg==")
    private val peer = ChannelPeer("telegram", "owner", "thread")

    @Test fun orderedPicturesBecomeRealWorkspaceAttachmentsAndTemporaryFilesAreRemoved() = runBlocking {
        val workspace = tmp.newFolder()
        val staging = tmp.newFolder()
        val seen = mutableListOf<TelegramImage>()
        val images = listOf(TelegramImage(aesKey = "first"), TelegramImage(aesKey = "second"))
        val files = StoredTelegramImages(staging, { seen += it; TelegramImages.validate(png) },
            { PreparedTelegramImage(it.bytes, it.extension) }, { workspace }, JVM_GATE, SandboxTree(), { true }, { null })
        var stored = emptyList<SavedTelegramImage>()
        assertTrue(files.receive(peer, images.map(::DownloadTelegramImage)) { stored = it; true })
        assertEquals(listOf("/workspace/inbox/telegram-1.png", "/workspace/inbox/telegram-2.png"), stored.map { it.path })
        assertEquals(images, seen)
        stored.forEach { assertArrayEquals(png, File(workspace, it.path.removePrefix("/workspace/")).readBytes()) }
        assertEquals(0, staging.listFiles()!!.size)
    }

    @Test fun rejectedSessionAndPartialDownloadLeaveNoNewAttachments() = runBlocking {
        val workspace = tmp.newFolder()
        val inbox = File(workspace, "inbox").apply { mkdirs() }
        val existing = File(inbox, "telegram-1.png").apply { writeBytes(png) }
        val staging = tmp.newFolder()
        var downloads = 0
        val files = StoredTelegramImages(staging, {
            downloads++
            if (it.aesKey == "bad") throw IOException("download failed")
            TelegramImages.validate(png)
        }, { PreparedTelegramImage(it.bytes, it.extension) }, { workspace }, JVM_GATE, SandboxTree(), { true }, { null })
        assertFalse(files.receive(peer, listOf(TelegramImage()).map(::DownloadTelegramImage)) { false })
        assertEquals(listOf(existing.name), inbox.list()!!.toList())
        val failure = runCatching {
            files.receive(peer, listOf(TelegramImage(), TelegramImage(aesKey = "bad")).map(::DownloadTelegramImage)) { fail("Must not launch a partial batch"); true }
        }.exceptionOrNull()
        assertTrue(failure is IOException)
        assertEquals(3, downloads)
        assertEquals(listOf(existing.name), inbox.list()!!.toList())
        assertEquals(0, staging.listFiles()!!.size)
    }

    @Test fun journalFailureRollsBackTheImportAndTooManyImagesAreRejectedBeforeDownloading() = runBlocking {
        val workspace = tmp.newFolder()
        var downloads = 0
        val files = StoredTelegramImages(tmp.newFolder(), { downloads++; TelegramImages.validate(png) },
            { PreparedTelegramImage(it.bytes, it.extension) }, { workspace }, JVM_GATE, SandboxTree(), { true }, { null })
        val failure = runCatching { files.receive(peer, listOf(TelegramImage()).map(::DownloadTelegramImage)) { throw IOException("disk full") } }.exceptionOrNull()
        assertTrue(failure is IOException)
        assertEquals(0, File(workspace, "inbox").listFiles()!!.size)
        assertTrue(runCatching { files.receive(peer, List(TelegramImages.MAX_IMAGES + 1) { DownloadTelegramImage(TelegramImage()) }) { true } }.exceptionOrNull() is IOException)
        assertEquals(1, downloads)
    }

    @Test fun exportedImageMustStillMatchItsRecordedSizeAndDigest() = runBlocking {
        val digest = MessageDigest.getInstance("SHA-256").digest(png).joinToString("") { "%02x".format(it) }
        val image = Delivery.Image("result:1:image:0", "thread", "content://media/external/downloads/12", "shot.png", png.size.toLong(), digest)
        assertArrayEquals(png, readTelegramExport(image) { ByteArrayInputStream(png) })
        for (changed in listOf(image.copy(size = image.size - 1), image.copy(size = image.size + 1), image.copy(sha256 = "0".repeat(64)))) {
            assertTrue(runCatching { readTelegramExport(changed) { ByteArrayInputStream(png) } }.exceptionOrNull() is IOException)
        }
        for (uri in listOf("file:///etc/passwd", "content://other.provider/image/1", "https://example.test/private.png")) {
            assertTrue(runCatching { readTelegramExport(image.copy(uri = uri)) { fail("Must reject before opening"); null } }.exceptionOrNull() is IOException)
        }
        assertTrue(runCatching { readTelegramExport(image) { null } }.exceptionOrNull() is IOException)
    }

    @Test fun cancellationDuringSecondDownloadRemovesStagingAndNeverImportsOrJournals() = runBlocking {
        val workspace = tmp.newFolder()
        val staging = tmp.newFolder()
        val downloadingSecond = CompletableDeferred<Unit>()
        var count = 0
        val files = StoredTelegramImages(staging, {
            if (++count == 2) { downloadingSecond.complete(Unit); awaitCancellation() }
            TelegramImages.validate(png)
        }, { PreparedTelegramImage(it.bytes, it.extension) }, { workspace }, JVM_GATE, SandboxTree(), { true }, { null })
        val job = launch { files.receive(peer, listOf(TelegramImage(), TelegramImage()).map(::DownloadTelegramImage)) { fail("Cancelled batch must not be accepted"); true } }
        withTimeout(2_000) { downloadingSecond.await(); job.cancelAndJoin() }
        assertEquals(0, staging.listFiles()!!.size)
        assertFalse(File(workspace, "inbox").exists())
    }

    @Test fun workspaceQuotaReachedByTheCopyRollsBackBeforeJournal() = runBlocking {
        val workspace = tmp.newFolder()
        val files = StoredTelegramImages(tmp.newFolder(), { TelegramImages.validate(png) },
            { PreparedTelegramImage(it.bytes, it.extension) }, { workspace }, JVM_GATE, SandboxTree(),
            { File(workspace, "inbox").listFiles().orEmpty().sumOf { it.length() } < png.size }, { null })
        val failure = runCatching {
            files.receive(peer, listOf(TelegramImage()).map(::DownloadTelegramImage)) { fail("Over-quota batch must not be accepted"); true }
        }.exceptionOrNull()
        assertTrue(failure is IOException)
        assertEquals(0, File(workspace, "inbox").listFiles()!!.size)
    }
}
