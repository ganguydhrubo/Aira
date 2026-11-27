package com.ganguydhrubo.aira.core.export.json

import com.ganguydhrubo.aira.core.export.AnswerOutcome
import com.ganguydhrubo.aira.core.export.ExportAssetReader
import com.ganguydhrubo.aira.core.export.ExportBackend
import com.ganguydhrubo.aira.core.export.ExportBlock
import com.ganguydhrubo.aira.core.export.ExportCapability
import com.ganguydhrubo.aira.core.export.ExportDescriptor
import com.ganguydhrubo.aira.core.export.ExportDocument
import com.ganguydhrubo.aira.core.export.ExportKind
import com.ganguydhrubo.aira.core.export.ExportPhase
import com.ganguydhrubo.aira.core.export.ExportPresentation
import com.ganguydhrubo.aira.core.export.ExportProgress
import com.ganguydhrubo.aira.core.export.ExportSink
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** 机器可读的导出;块类型和字段名保持稳定英文,不随界面语言变化。 */
class JsonExportBackend : ExportBackend {
    override val descriptor = ExportDescriptor(
        id = "core-json",
        formatId = "json",
        extension = "json",
        mime = "application/json",
        kind = ExportKind.SingleFile,
        capabilities = setOf(
            ExportCapability.ToolSteps, ExportCapability.Attachments, ExportCapability.Screens,
            ExportCapability.Timestamps, ExportCapability.ModelName, ExportCapability.Streaming,
            ExportCapability.Background,
        ),
    )

    override suspend fun render(
        document: ExportDocument,
        presentation: ExportPresentation,
        assets: ExportAssetReader,
        sink: ExportSink,
        progress: (ExportProgress) -> Unit,
    ) {
        val total = document.blocks.size
        progress(ExportProgress(ExportPhase.Rendering, 0, total))
        val root = buildJsonObject {
            put("format", "aira.conversation-export")
            put("version", 1)
            put("appVersion", document.meta.appVersion)
            put("exportedAt", document.meta.exportedAtMs)
            put("language", document.meta.language)
            put("conversation", buildJsonObject {
                put("id", document.meta.conversationId)
                put("title", document.meta.title)
            })
            put("assets", buildJsonArray {
                document.assets.forEach { asset ->
                    add(buildJsonObject {
                        put("id", asset.id)
                        put("kind", asset.kind.name.lowercase())
                        put("path", "assets/${asset.fileName}")
                        put("mime", asset.mime)
                        put("bytes", asset.sizeBytes)
                    })
                }
            })
            put("blocks", buildJsonArray {
                document.blocks.forEachIndexed { index, block ->
                    add(blockJson(block))
                    progress(ExportProgress(ExportPhase.Rendering, index + 1, total))
                }
            })
            put("warnings", buildJsonArray {
                document.warnings.forEach { w -> add(buildJsonObject { put("code", w.code); put("detail", w.detail) }) }
            })
        }
        sink.open("document.json").bufferedWriter(Charsets.UTF_8).use { it.write(Json { prettyPrint = true }.encodeToString(JsonElement.serializer(), root)) }
    }

    private fun blockJson(block: ExportBlock): JsonObject = when (block) {
        is ExportBlock.UserMessage -> buildJsonObject {
            put("id", block.id.value)
            put("type", "user")
            put("atMs", block.atMs)
            put("text", block.text)
            block.quote?.let { q ->
                put("quote", buildJsonObject { put("from", q.from.name.lowercase()); put("text", q.text) })
            }
            block.page?.let { p ->
                put("page", buildJsonObject {
                    put("url", p.url); put("title", p.title); put("selection", p.selection)
                })
            }
            put("attachments", JsonArray(block.attachments.map { asset(it) }))
            put("screens", JsonArray(block.screens.map { asset(it) }))
        }
        is ExportBlock.SteerMessage -> buildJsonObject {
            put("id", block.id.value)
            put("type", "steer")
            put("text", block.text)
            put("interrupted", block.interruptedText)
            put("finished", block.finishedText)
            put("assets", JsonArray(block.assets.map { asset(it) }))
        }
        is ExportBlock.Answer -> buildJsonObject {
            put("id", block.id.value)
            put("type", "answer")
            put("atMs", block.atMs)
            put("model", block.model)
            put("markdown", block.markdown)
            put("outcome", outcome(block.outcome))
            put("toolSteps", JsonArray(block.toolSteps.map { step ->
                buildJsonObject {
                    put("step", step.step)
                    put("tool", step.tool)
                    put("argsJson", step.argsJson)
                    put("result", step.result)
                    put("error", step.isError)
                    put("durationMs", step.durationMs)
                    put("model", step.model)
                    put("note", step.note)
                }
            }))
            put("files", JsonArray(block.files.map { asset(it) }))
        }
    }

    private fun outcome(outcome: AnswerOutcome): JsonObject = when (outcome) {
        AnswerOutcome.Succeeded -> buildJsonObject { put("status", "succeeded") }
        is AnswerOutcome.Unverified -> buildJsonObject {
            put("status", "unverified"); put("note", outcome.note)
            put("unconfirmed", JsonArray(outcome.unconfirmed.map { JsonPrimitive(it) }))
        }
        is AnswerOutcome.Failed -> buildJsonObject { put("status", "failed"); put("reason", outcome.reason) }
        is AnswerOutcome.Cancelled -> buildJsonObject { put("status", "cancelled"); put("summary", outcome.summary) }
    }

    private fun asset(asset: com.ganguydhrubo.aira.core.export.ExportAsset): JsonObject = buildJsonObject {
        put("id", asset.id)
        put("kind", asset.kind.name.lowercase())
        put("path", "assets/${asset.fileName}")
        put("mime", asset.mime)
        put("bytes", asset.sizeBytes)
    }
}
