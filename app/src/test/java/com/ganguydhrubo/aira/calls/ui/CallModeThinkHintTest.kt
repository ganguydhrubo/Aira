package com.ganguydhrubo.aira.calls.ui

import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.voice.model.ThinkModel
import softassert.assertEquals
import softassert.assertNull
import org.junit.Test

class CallModeThinkHintTest {
    @Test
    fun choosingTheLightModelSaysItFallsBackToTheMainModel() {
        assertEquals(R.string.call_mode_think_light_note, thinkFallbackHint(ThinkModel.Light))
    }

    @Test
    fun theMainModelNeedsNoHint() {
        assertNull(thinkFallbackHint(ThinkModel.Main))
    }
}
