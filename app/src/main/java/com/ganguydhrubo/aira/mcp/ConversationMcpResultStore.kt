package com.ganguydhrubo.aira.mcp

import com.ganguydhrubo.aira.cap.sandbox.SandboxDirs
import com.ganguydhrubo.aira.cap.sandbox.WorkspaceKey
import com.ganguydhrubo.aira.core.cap.mcp.McpResultStore
import com.ganguydhrubo.aira.core.workspace.SandboxPaths
import java.util.UUID

/**
 * 外部 MCP 工具的长结果写进当前任务工作区,模型可以用 read_file 读。
 * 只写 `mcp-results/`,文件名里的 server/tool 只保留安全字符。
 */
class ConversationMcpResultStore(
    private val dirs: SandboxDirs,
    private val workspace: WorkspaceKey,
) : McpResultStore {
    constructor(dirs: SandboxDirs, conversationId: String) : this(dirs, WorkspaceKey.Conversation(conversationId))

    override fun save(serverId: String, toolName: String, content: String): String {
        val ws = dirs.workspace(workspace)
        dirs.ensureBindSources(ws)
        val server = serverId.replace(Regex("[^A-Za-z0-9_-]"), "_").take(40)
        val tool = toolName.replace(Regex("[^A-Za-z0-9_-]"), "_").take(40)
        val rel = "mcp-results/$server-$tool-${UUID.randomUUID()}.txt"
        dirs.gate.createNew(ws, rel).use { it.write(content.toByteArray(Charsets.UTF_8)) }
        return "${SandboxPaths.WORKSPACE}/$rel"
    }
}
