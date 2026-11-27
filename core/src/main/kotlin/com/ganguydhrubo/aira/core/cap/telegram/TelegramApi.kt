package com.ganguydhrubo.aira.core.cap.telegram

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom

private const val DEFAULT_BASE_URL = "https://api.telegram.org"
private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

/** The owner's bot: its token, the bot's own id and username, and the one Telegram user it obeys. */
@Serializable
data class TelegramAccount(
    val token: String,
    val botId: String,
    val botUsername: String = "",
    val ownerId: String,
)

@Serializable
data class TelegramUser(
    val id: Long,
    @SerialName("is_bot") val isBot: Boolean = false,
    val username: String? = null,
    @SerialName("first_name") val firstName: String? = null,
)

@Serializable
data class TelegramChat(val id: Long, val type: String = "")

@Serializable
data class TelegramPhotoSize(
    @SerialName("file_id") val fileId: String,
    @SerialName("file_size") val fileSize: Long? = null,
    val width: Int = 0,
    val height: Int = 0,
)

@Serializable
data class TelegramDocument(
    @SerialName("file_id") val fileId: String,
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
)

@Serializable
data class TelegramUserContent(
    val text: String,
    val images: List<TelegramImage>,
)

@Serializable
data class TelegramMessage(
    @SerialName("message_id") val messageId: Long = 0,
    val from: TelegramUser? = null,
    val chat: TelegramChat = TelegramChat(0),
    val date: Long = 0,
    val text: String? = null,
    val caption: String? = null,
    val photo: List<TelegramPhotoSize> = emptyList(),
    val document: TelegramDocument? = null,
    @SerialName("reply_to_message") val replyTo: TelegramMessage? = null,
) {
    val fromUserId: String? get() = from?.id?.toString()

    /** Stable across restarts: a message id is unique only inside its chat. */
    fun serverMessageId(): String? = if (messageId > 0 && chat.id != 0L) "${chat.id}:$messageId" else null

    private fun isUserPrivateMessage() = chat.type == "private" && from != null && !from.isBot && serverMessageId() != null

    /** Pictures on this message, or on the message it replies to when it carries none of its own. */
    private fun images(): List<TelegramImage> {
        fun own(m: TelegramMessage): List<TelegramImage> {
            val photo = m.photo.maxByOrNull { (it.fileSize ?: 0L) + it.width.toLong() * it.height }
                ?.let { listOf(TelegramImage(it.fileId, it.fileSize)) }.orEmpty()
            val doc = m.document?.takeIf { it.mimeType?.startsWith("image/") == true }
                ?.let { listOf(TelegramImage(it.fileId, it.fileSize)) }.orEmpty()
            return photo + doc
        }
        return own(this).ifEmpty { replyTo?.let(::own).orEmpty() }
    }

    fun userText(): String? {
        if (!isUserPrivateMessage()) return null
        return (text ?: caption)?.takeIf { it.isNotBlank() }
    }

    fun userContent(): TelegramUserContent? {
        if (!isUserPrivateMessage()) return null
        val body = (text ?: caption).orEmpty().trim()
        val pictures = images()
        return TelegramUserContent(body, pictures).takeIf { body.isNotBlank() || pictures.isNotEmpty() }
    }
}

@Serializable
data class TelegramUpdate(
    @SerialName("update_id") val updateId: Long,
    val message: TelegramMessage? = null,
)

/** One long-poll result. [cursor] is the next offset to ask for, as text so it can be stored as before. */
data class TelegramUpdates(val msgs: List<TelegramMessage>, val cursor: String)

@Serializable
private data class Envelope<T>(
    val ok: Boolean = false,
    val result: T? = null,
    val description: String? = null,
    @SerialName("error_code") val errorCode: Int? = null,
)

@Serializable
private data class TelegramFile(
    @SerialName("file_id") val fileId: String = "",
    @SerialName("file_size") val fileSize: Long? = null,
    @SerialName("file_path") val filePath: String? = null,
)

class TelegramApiException(val code: Int, message: String) : IOException(message)
class TelegramHttpException(val httpCode: Int) : IOException("Telegram HTTP $httpCode")

/** Result of pairing: the owner who sent the code, and the offset just after that message. */
data class TelegramPairing(val account: TelegramAccount, val cursor: String)

/** HTTPS client for the Telegram Bot API. Protocol only; no task or Android lifecycle. */
class TelegramApi(
    private val client: OkHttpClient,
    @Suppress("unused") private val clientVersion: String = "",
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    private val mediaClient = client.newBuilder().callTimeout(60, java.util.concurrent.TimeUnit.SECONDS).build()

    /** Checks the token and returns the bot itself. */
    fun getMe(token: String): TelegramUser = call(token, "getMe", FormBody.Builder().build(), TelegramUser.serializer())

    /** Long polling and webhooks exclude each other; keep any pending messages. */
    fun deleteWebhook(token: String) {
        call(token, "deleteWebhook", FormBody.Builder().add("drop_pending_updates", "false").build(),
            Boolean.serializer())
    }

    fun updates(account: TelegramAccount, cursor: String): TelegramUpdates =
        toUpdates(call(account.token, "getUpdates", updatesBody(cursor), ListSerializer(TelegramUpdate.serializer())), cursor)

    /** Cancelled with the calling coroutine, so a stopped or rebound connection leaves no request behind. */
    suspend fun updatesCancellable(account: TelegramAccount, cursor: String): TelegramUpdates =
        toUpdates(callCancellable(account.token, "getUpdates", updatesBody(cursor),
            ListSerializer(TelegramUpdate.serializer())), cursor)

    /** Raw updates for pairing, before an owner exists. */
    suspend fun rawUpdates(token: String, cursor: String): List<TelegramUpdate> =
        callCancellable(token, "getUpdates", updatesBody(cursor), ListSerializer(TelegramUpdate.serializer()))

    private fun updatesBody(cursor: String) = FormBody.Builder()
        .apply { cursor.toLongOrNull()?.let { add("offset", it.toString()) } }
        .add("timeout", POLL_SECONDS.toString())
        .add("allowed_updates", "[\"message\"]")
        .build()

    private fun toUpdates(list: List<TelegramUpdate>, cursor: String): TelegramUpdates {
        val next = list.maxOfOrNull { it.updateId }?.let { (it + 1).toString() } ?: cursor
        return TelegramUpdates(list.mapNotNull { it.message }, next)
    }

    /** Plain text, split at Telegram's 4096-character message limit. */
    fun sendText(account: TelegramAccount, peerId: String, @Suppress("UNUSED_PARAMETER") clientId: String,
        text: String, @Suppress("UNUSED_PARAMETER") contextToken: String?) {
        chunks(text).forEach { part ->
            call(account.token, "sendMessage", FormBody.Builder()
                .add("chat_id", peerId).add("text", part).add("disable_web_page_preview", "true").build(),
                TelegramMessage.serializer())
        }
    }

    suspend fun downloadImage(account: TelegramAccount, image: TelegramImage): TelegramDownloadedImage {
        if (image.fileId.isBlank()) throw IOException("Missing Telegram file id")
        if ((image.fileSize ?: 0L) > TelegramImages.MAX_BYTES) throw IOException("Telegram image is larger than 10 MiB")
        val file = callCancellable(account.token, "getFile",
            FormBody.Builder().add("file_id", image.fileId).build(), TelegramFile.serializer())
        val path = file.filePath?.takeIf { it.isNotBlank() } ?: throw IOException("Telegram did not return a file path")
        if ((file.fileSize ?: 0L) > TelegramImages.MAX_BYTES) throw IOException("Telegram image is larger than 10 MiB")
        val url = baseUrl.toHttpUrl().newBuilder().addPathSegment("file").addPathSegment("bot${account.token}")
            .addPathSegments(path).build()
        val bytes = awaitTelegramResponse(mediaClient, Request.Builder().url(url).get().build()) { response ->
            if (!response.isSuccessful) throw TelegramHttpException(response.code)
            readLimited(response)
        }
        return TelegramImages.validate(bytes)
    }

    /** One image per message. GIFs go as documents so Telegram keeps them as files. */
    suspend fun sendImage(account: TelegramAccount, peerId: String, bytes: ByteArray) {
        val image = TelegramImages.validate(bytes)
        val gif = image.extension == "gif"
        val field = if (gif) "document" else "photo"
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", peerId)
            .addFormDataPart(field, "image.${image.extension}", bytes.toRequestBody(image.mimeType.toMediaType()))
            .build()
        callCancellable(account.token, if (gif) "sendDocument" else "sendPhoto", body, TelegramMessage.serializer(), mediaClient)
    }

    private fun readLimited(response: Response): ByteArray {
        val source = response.body?.byteStream() ?: throw IOException("Empty Telegram file")
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(64 * 1024)
        source.use {
            while (true) {
                val count = it.read(buffer)
                if (count < 0) break
                if (out.size() + count > TelegramImages.MAX_BYTES) throw IOException("Telegram image is larger than 10 MiB")
                out.write(buffer, 0, count)
            }
        }
        return out.toByteArray()
    }

    private fun method(token: String, name: String): HttpUrl {
        require(TOKEN.matches(token)) { "Invalid bot token" }
        return baseUrl.toHttpUrl().newBuilder().addPathSegment("bot$token").addPathSegment(name).build()
    }

    private fun <T> call(token: String, name: String, body: RequestBody, serializer: KSerializer<T>): T =
        client.newCall(Request.Builder().url(method(token, name)).post(body).build()).execute().use { decode(it, serializer) }

    private suspend fun <T> callCancellable(token: String, name: String, body: RequestBody,
        serializer: KSerializer<T>, http: OkHttpClient = client): T =
        awaitTelegramResponse(http, Request.Builder().url(method(token, name)).post(body).build()) { decode(it, serializer) }

    private fun <T> decode(response: Response, serializer: KSerializer<T>): T {
        val text = response.body?.string().orEmpty()
        val envelope = runCatching {
            json.decodeFromString(Envelope.serializer(serializer), text)
        }.getOrNull()
        if (response.code == 401 || response.code == 404) throw TelegramHttpException(401)
        if (envelope == null) {
            if (!response.isSuccessful) throw TelegramHttpException(response.code)
            throw IOException("Telegram returned an unreadable response")
        }
        if (!envelope.ok || envelope.result == null) {
            val code = envelope.errorCode ?: response.code
            if (code == 401) throw TelegramHttpException(401)
            throw TelegramApiException(code, envelope.description ?: "Telegram API error $code")
        }
        return envelope.result
    }

    companion object {
        const val POLL_SECONDS = 25
        private val TOKEN = Regex("""\d{5,16}:[A-Za-z0-9_-]{30,64}""")
        private val random = SecureRandom()

        fun isTokenShaped(token: String) = TOKEN.matches(token.trim())

        /** A short one-time code the owner sends to the bot to prove the chat is theirs. */
        fun pairingCode(): String = (100_000 + random.nextInt(900_000)).toString()

        /** True when [text] is the pairing code, typed alone or sent by the bot's start link. */
        fun matchesPairing(text: String?, code: String): Boolean {
            val t = text?.trim() ?: return false
            return t == code || t == "/start $code" || t.startsWith("/start@") && t.endsWith(" $code")
        }

        internal fun chunks(text: String, limit: Int = 4096): List<String> {
            if (text.length <= limit) return listOf(text)
            val parts = mutableListOf<String>()
            var rest = text
            while (rest.length > limit) {
                val cut = rest.lastIndexOf('\n', limit).takeIf { it > limit / 2 } ?: limit
                parts += rest.substring(0, cut)
                rest = rest.substring(cut).trimStart('\n')
            }
            if (rest.isNotEmpty()) parts += rest
            return parts
        }
    }
}
