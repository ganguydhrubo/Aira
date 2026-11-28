package com.ganguydhrubo.aira.cap.phone.a11y

import org.junit.Assert.*
import softassert.assertEquals // SOFTSTAR
import softassert.assertTrue // SOFTSTAR
import softassert.assertFalse // SOFTSTAR
import softassert.assertNull // SOFTSTAR
import org.junit.Test

class ContextPastePolicyTest {
    @Test fun unchangedPointInputCanUseItsNativeMenu() {
        assertTrue(ContextPastePolicy.canRetry("point_not_typed"))
        assertTrue(ContextPastePolicy.canRetry("input_not_sent"))
        assertFalse(ContextPastePolicy.canRetry("key_input_unconfirmed"))
        assertFalse(ContextPastePolicy.canRetry("ref_expired"))
    }

    @Test fun customViewCanBeProbedButUnrelatedControlsCannot() {
        assertTrue(ContextPastePolicy.canProbe(true, false, false, "View"))
        assertTrue(ContextPastePolicy.canProbe(true, false, false, "TextView"))
        assertFalse(ContextPastePolicy.canProbe(false, false, false, "View"))
        assertFalse(ContextPastePolicy.canProbe(true, true, false, "View"))
        assertFalse(ContextPastePolicy.canProbe(true, false, true, "View"))
        assertFalse(ContextPastePolicy.canProbe(true, false, false, "Button"))
    }
}
