package com.ganguydhrubo.aira

import com.ganguydhrubo.aira.core.action.ActionExecutor
import com.ganguydhrubo.aira.core.action.ActionRecorder
import com.ganguydhrubo.aira.core.action.Origin
import com.ganguydhrubo.aira.core.action.execution
import com.ganguydhrubo.aira.core.safety.guard.OperationGuards
import com.ganguydhrubo.aira.core.model.Part
import com.ganguydhrubo.aira.core.run.record.RunSink
import com.ganguydhrubo.aira.core.tool.ToolDispatcher
import com.ganguydhrubo.aira.core.tool.ToolExecution

/** 设备测试里直接做一个工具调用:和产品一样经执行入口(不配确认关卡,不等锁屏)。 */
suspend fun ToolDispatcher.runAction(call: Part.ToolCall): ToolExecution =
    ActionExecutor(this, OperationGuards(), null, gate = {}, recorder = ActionRecorder(RunSink.NONE)).run(call, Origin.Model(0)).execution()
