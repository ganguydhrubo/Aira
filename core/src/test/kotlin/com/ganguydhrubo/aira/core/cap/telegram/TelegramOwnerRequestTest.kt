package com.ganguydhrubo.aira.core.cap.telegram

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The explicit "send it to my Telegram" gate: matches clear requests, skips negated ones, and has no word-distance window. */
class TelegramOwnerRequestTest {
    @Test fun explicitSendMatches() {
        assertTrue(explicitTelegramOwnerRequest("summarize my emails and send the result to my Telegram"))
        assertTrue(explicitTelegramOwnerRequest("Check the parcel status and message me on Telegram"))
    }

    @Test fun longSentenceStillMatches() {
        assertTrue(explicitTelegramOwnerRequest(
            "send the result to my telegram, and also set an alarm for 8 tomorrow morning to remind me to carry an umbrella"))
    }

    @Test fun negatedRequestsDoNotMatch() {
        assertFalse(explicitTelegramOwnerRequest("don't send anything to my telegram"))
        assertFalse(explicitTelegramOwnerRequest("never forward this on Telegram"))
    }

    @Test fun readingTelegramIsNotSending() {
        assertFalse(explicitTelegramOwnerRequest("see what new messages I have in Telegram"))
    }

    @Test fun unrelatedGoalDoesNotMatch() {
        assertFalse(explicitTelegramOwnerRequest("what's the weather today"))
    }
}
