package com.ganguydhrubo.aira.task.ui

import org.junit.Rule
import com.ganguydhrubo.aira.i18n.ChineseTexts
import com.ganguydhrubo.aira.core.run.record.RunMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunMetricsCopyTest {
    @get:Rule val texts = ChineseTexts()

    @Test fun durations() {
        assertEquals("0 秒", RunMetricsCopy.duration(0))
        assertEquals("3.2 秒", RunMetricsCopy.duration(3_240))
        assertEquals("45 秒", RunMetricsCopy.duration(45_000))
        assertEquals("1 分 0 秒", RunMetricsCopy.duration(59_960))
        assertEquals("2 分 13 秒", RunMetricsCopy.duration(133_000))
        assertEquals("1 h 5 分", RunMetricsCopy.duration(3_900_000))
    }

    @Test fun breakdownShowsSharesTokensAndSteps() {
        val m = RunMetrics(totalMs = 10_000, modelMs = 5_000, observeMs = 2_000, actMs = 1_000, retryWaitMs = 500,
            inputTokens = 12_345, outputTokens = 100, cachedTokens = 10_000, steps = 4, observeCalls = 3, actCalls = 2,
            allCalls = 5, allHttpCalls = 6)
        val rows = RunMetricsCopy.breakdown(m).toMap()
        assertEquals("5 秒 · 50%", rows[RunMetricsCopy.MODEL])
        assertEquals("2 秒 · 20%", rows[RunMetricsCopy.OBSERVE])
        assertEquals("1 秒 · 10%", rows[RunMetricsCopy.ACT])
        assertEquals("2 秒 · 20%", rows[RunMetricsCopy.OTHER])
        assertEquals("0.5 秒", rows["Of which retry waits"])
        assertEquals("1.23 万", rows["Input tokens"])
        assertEquals("1 万", rows["Cache hits"])
        assertEquals("4 步", rows["Steps"])
        assertEquals("3 / 2 次", rows["Reads / actions"])
        assertTrue(rows.getValue("All logical model calls").startsWith("5 次"))
        assertEquals("6 次", rows["All HTTP requests"])
    }
}
