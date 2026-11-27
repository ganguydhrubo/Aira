package com.ganguydhrubo.aira.core.cap.video

import com.ganguydhrubo.aira.core.tool.AgentTool

internal object VideoGroup {
    val tools: List<AgentTool> = listOf(VideoFrames, VideoInfoTool, VideoSave(ask = true), VideoGenerate)
}
