package com.ganguydhrubo.aira.calls.ui

import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.calls.CallConfig
import com.ganguydhrubo.aira.calls.CallNotifications
import com.ganguydhrubo.aira.calls.CallRuntimeState
import com.ganguydhrubo.aira.calls.CallStandbyService
import softassert.assertEquals
import softassert.assertFalse
import softassert.assertNull
import softassert.assertTrue
import org.junit.Test

class CallDeviceReadinessTest {
    @Test fun standbyNotificationRepairTargetsItsOwnChannelWithoutBlockingManualCalls() {
        val device = CallDeviceReadiness(emptyList(), CallNotificationBlock.StandbyChannel, CallBatteryPolicy.Managed)
        assertTrue(device.callNotificationsAllowed)
        assertFalse(device.standbyNotificationsAllowed)
        assertEquals(CallStandbyService.CHANNEL, notificationSettingsChannel(device.notificationBlock))

        val callsBlocked = device.copy(notificationBlock = CallNotificationBlock.CallChannel)
        assertFalse(callsBlocked.callNotificationsAllowed)
        assertFalse(callsBlocked.standbyNotificationsAllowed)
        assertEquals(CallNotifications.CHANNEL, notificationSettingsChannel(callsBlocked.notificationBlock))
        assertNull(notificationSettingsChannel(CallNotificationBlock.Permission))
    }

    @Test fun backgroundStandbyStatusRequiresAServiceIndependentOfAudioVerification() {
        val config = CallConfig(enabled = true, autoAnswer = true, audioVerified = true)
        val state = CallRuntimeState(prepared = true, audioReady = true)
        assertEquals(R.string.calls_background_not_running, backgroundStandbyStatus(config, state))
        assertEquals(R.string.calls_background_starting, backgroundStandbyStatus(config, state.copy(backgroundStandbyStarting = true)))

        val service = state.copy(audioReady = false, backgroundStandbyRunning = true)
        assertEquals(R.string.calls_background_running, backgroundStandbyStatus(config.copy(audioVerified = false), service))
        assertEquals(R.string.calls_background_auto_off, backgroundStandbyStatus(config.copy(autoAnswer = false), service))
    }
}
