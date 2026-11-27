package com.ganguydhrubo.aira.core.cap.transcribe

import com.ganguydhrubo.aira.core.tool.AgentTool

internal object TranscribeGroup {
    val tools: List<AgentTool> = listOf(AudioTranscribe)
}
