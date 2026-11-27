package com.ganguydhrubo.aira.chat.ui

import com.ganguydhrubo.aira.core.workspace.SandboxPaths
import com.ganguydhrubo.aira.ui.MarkdownModel

/** 把回复里抽出的 `/workspace/...` 引用转成附件渲染模型(照片/文件分好,大小留空);越界(`..`)的引用丢弃。 */
fun outputRefsParsed(answer: String): AttachmentMessage.Parsed {
    val items = MarkdownModel.workspaceRefs(answer)
        .filter { SandboxPaths.normalize(it) != null }
        .map { AttachmentMessage.Item(it, size = "") }
    return AttachmentMessage.Parsed(photos = items.filter { it.photo }, files = items.filterNot { it.photo }, words = "")
}
