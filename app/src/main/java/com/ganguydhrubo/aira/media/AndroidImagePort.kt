package com.ganguydhrubo.aira.media

import android.content.Context
import android.graphics.BitmapFactory
import com.ganguydhrubo.aira.cap.image.ImageExporter
import com.ganguydhrubo.aira.cap.sandbox.Attachments
import com.ganguydhrubo.aira.cap.sandbox.SandboxDirs
import com.ganguydhrubo.aira.core.cap.image.ImagePort
import com.ganguydhrubo.aira.core.cap.image.SavedImage
import com.ganguydhrubo.aira.core.cap.image.StoredImage
import com.ganguydhrubo.aira.core.chat.GeneratedPhoto
import com.ganguydhrubo.aira.core.model.GeneratedImage
import com.ganguydhrubo.aira.core.model.ImageCapabilities
import com.ganguydhrubo.aira.core.model.ImageError
import com.ganguydhrubo.aira.core.model.ImageModel
import com.ganguydhrubo.aira.core.model.ImageReference
import com.ganguydhrubo.aira.core.model.ImageRequest
import com.ganguydhrubo.aira.core.model.ImageResponse
import com.ganguydhrubo.aira.core.workspace.SandboxPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 图片能力的手机实现:模型调用直接用 [model];产物经工作区文件闸门写进 `out/images/`;
 * 存相册走 [ImageExporter](MediaStore Pictures/Aira,不需要存储权限)。
 * [onGenerated]/[onSaved] 把结果交给任务,聊天里据此出现照片格子与「已存入相册」卡片。
 */
class AndroidImagePort(
    private val context: Context,
    private val conversationId: String,
    private val model: ImageModel,
    override val capabilities: ImageCapabilities,
    private val onGenerated: (GeneratedPhoto) -> Unit = {},
    private val onSaved: (SavedImage) -> Unit = {},
) : ImagePort {
    override val modelName: String get() = model.name

    private val dirs by lazy { SandboxDirs(context) }
    private val workspace: File by lazy { dirs.workspace(com.ganguydhrubo.aira.chat.WorkspaceResolver.resolveFor(context, conversationId)) }

    override suspend fun generate(request: ImageRequest): ImageResponse = model.generate(request)

    override suspend fun readReference(path: String): ImageReference {
        val rel = relative(path) ?: throw ImageError.Unsupported("the path must be under ${SandboxPaths.WORKSPACE}")
        return dirs.tree.shared {
            withContext(Dispatchers.IO) {
                dirs.gate.openRead(workspace, rel).use { g ->
                    if (g.size > MAX_REFERENCE_BYTES) throw ImageError.TooLarge(g.size)
                    val bytes = g.read(0, g.size.toInt())
                    val mime = imageMime(bytes) ?: throw ImageError.BadImage("$path is not a PNG, JPEG or WebP image")
                    ImageReference(bytes, mime)
                }
            }
        }
    }

    override suspend fun store(image: GeneratedImage, nameHint: String?, prompt: String): StoredImage {
        val name = baseName(nameHint, prompt, image.mime)
        return dirs.tree.shared {
            withContext(Dispatchers.IO) {
                val (actual, g) = dirs.gate.createNumbered(workspace, DIRECTORY, name)
                try {
                    g.write(image.bytes)
                    val path = "${SandboxPaths.WORKSPACE}/$DIRECTORY/$actual"
                    onGenerated(GeneratedPhoto(path, image.width, image.height, prompt, System.currentTimeMillis()))
                    StoredImage(path, image.bytes.size, image.width, image.height)
                } finally {
                    g.close()
                }
            }
        }
    }

    override suspend fun saveToGallery(path: String, name: String?): SavedImage {
        val rel = relative(path) ?: throw ImageError.Unsupported("the path must be under ${SandboxPaths.WORKSPACE}")
        return dirs.tree.shared {
            withContext(Dispatchers.IO) {
                val bytes = dirs.gate.openRead(workspace, rel).use { g ->
                    if (g.size > MAX_GALLERY_BYTES) throw ImageError.TooLarge(g.size)
                    g.read(0, g.size.toInt())
                }
                val mime = imageMime(bytes) ?: throw ImageError.BadImage("$path is not a PNG, JPEG or WebP image")
                val saved = ImageExporter(context).save(bytes, mime, name ?: path.substringAfterLast('/'))
                onSaved(saved)
                saved
            }
        }
    }

    /** `/workspace/...` → 工作区里的相对路径;越界、空段、`.`、`..` 都不收。 */
    private fun relative(path: String): String? {
        val prefix = SandboxPaths.WORKSPACE + "/"
        val p = path.trim()
        if (!p.startsWith(prefix)) return null
        val rel = p.removePrefix(prefix)
        if (rel.isEmpty()) return null
        if (rel.split('/').any { it.isEmpty() || it == "." || it == ".." }) return null
        return rel
    }

    /** 文件名提示:去掉路径分隔符等危险字符,没有扩展名时按图片类型补上。 */
    private fun baseName(hint: String?, prompt: String, mime: String): String {
        val raw = hint?.takeIf { it.isNotBlank() } ?: prompt.lineSequence().firstOrNull().orEmpty().take(24)
        val safe = Attachments.safeName(raw).ifBlank { "image" }
        return if (safe.substringAfterLast('.', "").isEmpty()) "$safe.${extensionOf(mime)}" else safe
    }

    private fun imageMime(bytes: ByteArray): String? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        return opts.outMimeType?.takeIf { it.startsWith("image/") && (opts.outWidth > 0 || opts.outHeight > 0) }
    }

    private fun extensionOf(mime: String): String = when (mime.substringAfter('/', "").lowercase()) {
        "jpeg", "jpg" -> "jpg"
        "webp" -> "webp"
        "gif" -> "gif"
        else -> "png"
    }

    private companion object {
        const val DIRECTORY = "out/images"
        const val MAX_REFERENCE_BYTES = 10L * 1024 * 1024
        const val MAX_GALLERY_BYTES = 20L * 1024 * 1024
    }
}
