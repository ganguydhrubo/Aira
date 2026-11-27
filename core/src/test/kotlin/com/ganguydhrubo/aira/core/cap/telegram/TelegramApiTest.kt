package com.ganguydhrubo.aira.core.cap.telegram

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress
import java.net.URLDecoder

class TelegramApiTest {
    private val token = "123456789:AAEhBP0av28Ey-test_token_value_12345"
    private val account = TelegramAccount(token, "123456789", "aira_test_bot", "42")

    private fun server(block: (MockWebServer, TelegramApi) -> Unit) {
        MockWebServer().use { server ->
            server.start(InetAddress.getByName("127.0.0.1"), 0)
            block(server, TelegramApi(OkHttpClient(), "", server.url("/").toString()))
        }
    }

    private fun response(body: String, code: Int = 200) =
        MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body)

    private fun form(body: String): Map<String, String> = body.split('&').filter { it.isNotEmpty() }.associate {
        val (k, v) = it.split('=', limit = 2)
        URLDecoder.decode(k, "UTF-8") to URLDecoder.decode(v, "UTF-8")
    }

    @Test fun getMeReadsTheBotAndUsesTheTokenPath() = server { server, api ->
        server.enqueue(response("""{"ok":true,"result":{"id":123456789,"is_bot":true,"username":"aira_test_bot","first_name":"Aira"}}"""))
        val bot = api.getMe(token)
        assertEquals(123456789L, bot.id)
        assertTrue(bot.isBot)
        assertEquals("aira_test_bot", bot.username)
        assertEquals("/bot$token/getMe", server.takeRequest().path)
    }

    @Test fun badTokenIsRejectedBeforeAnyRequest() = server { server, api ->
        assertThrows(IllegalArgumentException::class.java) { api.getMe("not-a-token") }
        assertEquals(0, server.requestCount)
    }

    @Test fun unauthorizedMapsTo401() = server { server, api ->
        server.enqueue(response("""{"ok":false,"error_code":401,"description":"Unauthorized"}""", 401))
        val e = assertThrows(TelegramHttpException::class.java) { api.getMe(token) }
        assertEquals(401, e.httpCode)
    }

    @Test fun apiErrorsCarryCodeAndDescription() = server { server, api ->
        server.enqueue(response("""{"ok":false,"error_code":400,"description":"Bad Request: chat not found"}""", 400))
        val e = assertThrows(TelegramApiException::class.java) { api.sendText(account, "42", "c1", "hi", null) }
        assertEquals(400, e.code)
        assertEquals("Bad Request: chat not found", e.message)
    }

    @Test fun updatesAdvanceTheCursorAndKeepOnlyMessages() = server { server, api ->
        server.enqueue(response("""{"ok":true,"result":[
            {"update_id":10,"message":{"message_id":5,"from":{"id":42,"is_bot":false},"chat":{"id":42,"type":"private"},"date":1,"text":"hello"}},
            {"update_id":11,"edited_message":{"message_id":5}}
        ]}"""))
        val got = api.updates(account, "10")
        assertEquals("12", got.cursor)
        assertEquals(1, got.msgs.size)
        assertEquals("hello", got.msgs[0].userText())
        assertEquals("42:5", got.msgs[0].serverMessageId())
        val sent = form(server.takeRequest().body.readUtf8())
        assertEquals("10", sent["offset"])
        assertEquals(TelegramApi.POLL_SECONDS.toString(), sent["timeout"])
        assertEquals("[\"message\"]", sent["allowed_updates"])
    }

    @Test fun emptyUpdatesKeepTheCursor() = server { server, api ->
        server.enqueue(response("""{"ok":true,"result":[]}"""))
        assertEquals("7", api.updates(account, "7").cursor)
        server.enqueue(response("""{"ok":true,"result":[]}"""))
        api.updates(account, "")
        server.takeRequest()
        assertFalse("offset" in form(server.takeRequest().body.readUtf8()))
    }

    @Test fun rawUpdatesForPairing() = server { server, api ->
        server.enqueue(response("""{"ok":true,"result":[{"update_id":3,"message":{"message_id":1,"from":{"id":9},"chat":{"id":9,"type":"private"},"text":"/start 123456"}}]}"""))
        val list = runBlocking { api.rawUpdates(token, "") }
        assertEquals(3L, list.single().updateId)
        assertTrue(TelegramApi.matchesPairing(list.single().message?.text, "123456"))
    }

    @Test fun sendTextPostsChatAndText() = server { server, api ->
        server.enqueue(response("""{"ok":true,"result":{"message_id":1,"chat":{"id":42}}}"""))
        api.sendText(account, "42", "c1", "Done: battery is 80%", null)
        val req = server.takeRequest()
        assertEquals("/bot$token/sendMessage", req.path)
        val sent = form(req.body.readUtf8())
        assertEquals("42", sent["chat_id"])
        assertEquals("Done: battery is 80%", sent["text"])
    }

    @Test fun longTextIsSplitIntoSeveralMessages() = server { server, api ->
        repeat(2) { server.enqueue(response("""{"ok":true,"result":{"message_id":1,"chat":{"id":42}}}""")) }
        api.sendText(account, "42", "c1", "a".repeat(5000), null)
        assertEquals(4096, form(server.takeRequest().body.readUtf8())["text"]!!.length)
        assertEquals(904, form(server.takeRequest().body.readUtf8())["text"]!!.length)
    }

    @Test fun chunksPreferLineBreaks() {
        val text = "x".repeat(3000) + "\n" + "y".repeat(3000)
        assertEquals(listOf("x".repeat(3000), "y".repeat(3000)), TelegramApi.chunks(text))
        assertEquals(listOf("short"), TelegramApi.chunks("short"))
    }

    @Test fun tokenShapeAndPairingCode() {
        assertTrue(TelegramApi.isTokenShaped(" $token "))
        assertFalse(TelegramApi.isTokenShaped("123:abc"))
        val code = TelegramApi.pairingCode()
        assertEquals(6, code.length)
        assertTrue(code.all(Char::isDigit))
        assertTrue(TelegramApi.matchesPairing(code, code))
        assertTrue(TelegramApi.matchesPairing("/start $code", code))
        assertTrue(TelegramApi.matchesPairing("/start@aira_test_bot $code", code))
        assertFalse(TelegramApi.matchesPairing("hello", code))
        assertFalse(TelegramApi.matchesPairing(null, code))
    }

    @Test fun onlyPrivateMessagesFromPeopleAreUserText() {
        val person = TelegramUser(42)
        fun msg(type: String, from: TelegramUser?) = TelegramMessage(1, from, TelegramChat(42, type), text = "hi")
        assertEquals("hi", msg("private", person).userText())
        assertNull(msg("group", person).userText())
        assertNull(msg("private", TelegramUser(7, isBot = true)).userText())
        assertNull(msg("private", null).userText())
    }
}
