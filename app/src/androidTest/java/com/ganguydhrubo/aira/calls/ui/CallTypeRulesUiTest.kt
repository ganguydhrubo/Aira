package com.ganguydhrubo.aira.calls.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.calls.CallConfig
import com.ganguydhrubo.aira.calls.CallRuntimeState
import com.ganguydhrubo.aira.calls.IncomingCallType
import com.ganguydhrubo.aira.calls.IncomingScope
import com.ganguydhrubo.aira.ui.theme.AiraTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CallTypeRulesUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun ruleRowsOpenTheirEditorWithOneTap() {
        val opened = mutableListOf<String>()
        compose.setContent {
            AiraTheme(dark = false) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    CallReceivePage(CallConfig(incomingScope = IncomingScope.STRANGERS), CallRuntimeState(),
                        ready = true, missing = emptyList(), controlsEnabled = true, canStartStandby = false, standbyNeedsSetup = false,
                        onSetup = {}, onToggle = {}, onRules = { opened += "scope" }, onBypass = { opened += "bypass" }, onProfile = null,
                        onResume = {}, onInstructions = {}, onStartStandby = {})
                }
            }
        }
        compose.onNodeWithTag("call-rules-scope").performScrollTo().performClick()
        compose.onNodeWithTag("call-rules-important").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("scope", "bypass"), opened) }
    }

    @Test fun chooseTwoTypesAndKeepTheExistingNumberList() {
        val original = CallConfig(allowedIncomingNumbers = setOf("13800138000"))
        var saved: CallConfig? = null
        compose.setContent {
            AiraTheme(dark = false) {
                CallNumberRuleEditor(original, personal = false, onDismiss = {}) { mode, numbers, types, delay ->
                    saved = original.copy(incomingScope = mode, allowedIncomingNumbers = numbers,
                        allowedCallTypes = types, autoAnswerDelaySeconds = delay)
                    true
                }
            }
        }
        fun row(id: Int) = compose.onNodeWithText(compose.activity.getString(id))
        row(R.string.calls_only_types).performScrollTo().performClick()
        row(R.string.calls_save).assertIsNotEnabled()
        row(R.string.calls_type_takeout).performScrollTo().performClick()
        row(R.string.calls_type_parcel).performScrollTo().performClick()
        row(R.string.calls_save).performClick()
        compose.runOnIdle {
            assertEquals(IncomingScope.TYPES, saved?.incomingScope)
            assertEquals(setOf(IncomingCallType.TAKEOUT, IncomingCallType.PARCEL), saved?.allowedCallTypes)
            assertEquals(original.allowedIncomingNumbers, saved?.allowedIncomingNumbers)
        }
    }
}
