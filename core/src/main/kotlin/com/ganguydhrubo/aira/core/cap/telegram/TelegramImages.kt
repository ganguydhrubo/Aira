package com.ganguydhrubo.aira.core.cap.telegram

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** A picture the owner sent to the bot: only Telegram's file reference, never the bytes. */
@Serializable
data class TelegramImage(
    val fileId: String = "",
    val fileSize: Long? = null,
)

data class TelegramDownloadedImage(val bytes: ByteArray, val mimeType: String, val extension: String)

/** Client resource limits. Telegram lets bots download up to 20 MB and send photos up to 10 MB. */
object TelegramImages {
    const val MAX_BYTES = 10 * 1024 * 1024
    const val MAX_IMAGES = 8

    /** Inspect file signatures rather than trusting a file name or response Content-Type. */
    fun validate(bytes: ByteArray): TelegramDownloadedImage {
        if (bytes.isEmpty() || bytes.size > MAX_BYTES) throw IOException("Telegram image exceeds the 10 MiB limit or is empty")
        val format = when {
            bytes.startsWith(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)) -> "image/png" to "png"
            bytes.size >= 3 && bytes[0] == 0xff.toByte() && bytes[1] == 0xd8.toByte() && bytes[2] == 0xff.toByte() -> "image/jpeg" to "jpg"
            bytes.startsWith("GIF87a".toByteArray()) || bytes.startsWith("GIF89a".toByteArray()) -> "image/gif" to "gif"
            bytes.size >= 12 && bytes.startsWith("RIFF".toByteArray()) &&
                bytes.copyOfRange(8, 12).contentEquals("WEBP".toByteArray()) -> "image/webp" to "webp"
            else -> throw IOException("Unsupported Telegram image format (use PNG, JPEG, GIF or WebP)")
        }
        return TelegramDownloadedImage(bytes, format.first, format.second)
    }

    private fun ByteArray.startsWith(prefix: ByteArray) = size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
}

/** Cancel the HTTP call while connecting or reading its body, and close responses on every path. */
internal suspend fun <T> awaitTelegramResponse(client: OkHttpClient, request: Request, decode: (Response) -> T): T =
    suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!continuation.isActive) return
                    try {
                        val result = decode(it)
                        if (continuation.isActive) continuation.resume(result)
                    } catch (e: Exception) {
                        if (continuation.isActive) continuation.resumeWithException(e)
                    }
                }
            }
        })
    }
