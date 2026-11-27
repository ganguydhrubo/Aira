package com.ganguydhrubo.aira.core.cap.telegram

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class TelegramImagesTest {
    private val token = "123456789:AAEhBP0av28Ey-test_token_value_12345"
    private val account = TelegramAccount(token, "123456789", "aira_test_bot", "42")
    private val png = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3)
    private val jpeg = byteArrayOf(0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0xe0.toByte(), 0)
    private val gif = "GIF89a....".toByteArray()

    private fun server(block: suspend (MockWebServer, TelegramApi) -> Unit) = runBlocking {
        MockWebServer().use { server ->
            server.start(InetAddress.getByName("127.0.0.1"), 0)
            block(server, TelegramApi(OkHttpClient(), "", server.url("/").toString()))
        }
    }

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    private fun privateMessage(
        text: String? = null,
        caption: String? = null,
        photo: List<TelegramPhotoSize> = emptyList(),
        document: TelegramDocument? = null,
        replyTo: TelegramMessage? = null,
    ) = TelegramMessage(7, TelegramUser(42), TelegramChat(42, "private"), 1, text, caption, photo, document, replyTo)

    @Test fun validateSniffsSignatures() {
        assertEquals("image/png", TelegramImages.validate(png).mimeType)
        assertEquals("jpg", TelegramImages.validate(jpeg).extension)
        assertEquals("gif", TelegramImages.validate(gif).extension)
        val webp = "RIFF".toByteArray() + byteArrayOf(0, 0, 0, 0) + "WEBP".toByteArray()
        assertEquals("image/webp", TelegramImages.validate(webp).mimeType)
        assertThrows(IOException::class.java) { TelegramImages.validate("hello".toByteArray()) }
        assertThrows(IOException::class.java) { TelegramImages.validate(ByteArray(0)) }
        assertThrows(IOException::class.java) { TelegramImages.validate(png + ByteArray(TelegramImages.MAX_BYTES)) }
    }

    @Test fun contentTakesTheLargestPhotoAndImageDocuments() {
        val small = TelegramPhotoSize("small", 100, 90, 90)
        val big = TelegramPhotoSize("big", 5000, 1280, 1280)
        val photo = privateMessage(caption = "what is this?", photo = listOf(small, big)).userContent()!!
        assertEquals("what is this?", photo.text)
        assertEquals(listOf(TelegramImage("big", 5000)), photo.images)

        val doc = privateMessage(document = TelegramDocument("d1", "image/png", 10)).userContent()!!
        assertEquals(listOf(TelegramImage("d1", 10)), doc.images)
        assertNull(privateMessage(document = TelegramDocument("d2", "application/pdf")).userContent())
    }

    @Test fun replyIsOnlyAnImageFallback() {
        val quoted = privateMessage(photo = listOf(TelegramPhotoSize("quoted", 10)))
        val reply = privateMessage(text = "read this", replyTo = quoted).userContent()!!
        assertEquals(listOf(TelegramImage("quoted", 10)), reply.images)
        val own = privateMessage(caption = "mine", photo = listOf(TelegramPhotoSize("own", 10)), replyTo = quoted).userContent()!!
        assertEquals(listOf(TelegramImage("own", 10)), own.images)
    }

    @Test fun downloadAsksForThePathThenFetchesTheFile() = server { server, api ->
        server.enqueue(json("""{"ok":true,"result":{"file_id":"f1","file_size":11,"file_path":"photos/file_1.png"}}"""))
        server.enqueue(MockResponse().setBody(Buffer().write(png)))
        val got = api.downloadImage(account, TelegramImage("f1", 11))
        assertArrayEquals(png, got.bytes)
        assertEquals("image/png", got.mimeType)
        assertEquals("/bot$token/getFile", server.takeRequest().path)
        assertEquals("/file/bot$token/photos/file_1.png", server.takeRequest().path)
    }

    @Test fun oversizedImagesAreRefusedEarly() = server { server, api ->
        assertThrows(IOException::class.java) {
            runBlocking { api.downloadImage(account, TelegramImage("f1", TelegramImages.MAX_BYTES + 1L)) }
        }
        assertEquals(0, server.requestCount)
        server.enqueue(json("""{"ok":true,"result":{"file_id":"f1","file_size":${TelegramImages.MAX_BYTES + 1},"file_path":"p.png"}}"""))
        assertThrows(IOException::class.java) { runBlocking { api.downloadImage(account, TelegramImage("f1")) } }
        assertEquals(1, server.requestCount)
    }

    @Test fun missingFileIdOrPathFails() = server { server, api ->
        assertThrows(IOException::class.java) { runBlocking { api.downloadImage(account, TelegramImage("")) } }
        server.enqueue(json("""{"ok":true,"result":{"file_id":"f1"}}"""))
        assertThrows(IOException::class.java) { runBlocking { api.downloadImage(account, TelegramImage("f1")) } }
    }

    @Test fun sendImageUsesPhotoOrDocument() = server { server, api ->
        repeat(2) { server.enqueue(json("""{"ok":true,"result":{"message_id":1,"chat":{"id":42}}}""")) }
        api.sendImage(account, "42", png)
        val photo = server.takeRequest()
        assertEquals("/bot$token/sendPhoto", photo.path)
        val body = photo.body.readUtf8()
        assertTrue(body.contains("name=\"photo\"; filename=\"image.png\""))
        assertTrue(body.contains("name=\"chat_id\""))
        api.sendImage(account, "42", gif)
        assertEquals("/bot$token/sendDocument", server.takeRequest().path)
    }

    @Test fun sendImageRejectsNonImages() = server { server, api ->
        assertThrows(IOException::class.java) { runBlocking { api.sendImage(account, "42", "plain text".toByteArray()) } }
        assertEquals(0, server.requestCount)
    }

    @Test fun cancellationStopsDownloadWhileReadingBody() = runBlocking {
        MockWebServer().use { server ->
            server.start(InetAddress.getByName("127.0.0.1"), 0)
            val http = OkHttpClient()
            val api = TelegramApi(http, "", server.url("/").toString())
            server.enqueue(json("""{"ok":true,"result":{"file_id":"f1","file_path":"p.png"}}"""))
            server.enqueue(MockResponse().setBody(Buffer().write(png)).throttleBody(1, 1, TimeUnit.SECONDS))
            val work = async(Dispatchers.Default) { api.downloadImage(account, TelegramImage("f1")) }
            assertNotNull(server.takeRequest(2, TimeUnit.SECONDS))
            assertNotNull(server.takeRequest(2, TimeUnit.SECONDS))
            withTimeout(5_000) {
                work.cancelAndJoin()
                while (http.dispatcher.runningCallsCount() != 0) delay(10)
            }
            assertTrue(work.isCancelled)
        }
    }
}
