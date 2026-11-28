package com.ganguydhrubo.aira.ui

import com.ganguydhrubo.aira.i18n.ChineseTexts
import com.ganguydhrubo.aira.settings.AccessibilityState
import com.ganguydhrubo.aira.settings.Problem
import softassert.assertEquals
import softassert.assertFalse
import softassert.assertNull
import softassert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.ganguydhrubo.aira.settings.ui.Fix
import com.ganguydhrubo.aira.settings.ui.StatusCopy

class AccessibilityStatusCopyTest {
    @get:Rule val texts = ChineseTexts()

    @Test fun restrictedDisabledAccessibilityExplainsTheAppInfoRoute() {
        val problem = Problem.Accessibility(AccessibilityState.Off, restrictedSettings = true)
        assertTrue(StatusCopy.banner(problem).detail.contains("Allow restricted settings"))
        val explanation = StatusCopy.explain(problem, "14")
        assertTrue(explanation.body.contains("app info"))
        assertTrue(explanation.body.contains("if that option is available"))
        assertFalse(explanation.body.contains("off and on again"))
        assertEquals(Fix.OpenAccessibility, explanation.fix)
        assertEquals(Fix.OpenAppInfo, explanation.secondaryAction?.second)
    }

    @Test fun unrestrictedDisabledAccessibilityOpensAccessibilitySettings() {
        val problem = Problem.Accessibility(AccessibilityState.Off)
        assertFalse(StatusCopy.banner(problem).detail.contains("Allow restricted settings"))
        val explanation = StatusCopy.explain(problem, "14")
        assertEquals(Fix.OpenAccessibility, explanation.fix)
        assertNull(explanation.secondaryAction)
    }
}
