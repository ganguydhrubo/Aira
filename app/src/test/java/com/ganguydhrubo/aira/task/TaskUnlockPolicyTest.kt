package com.ganguydhrubo.aira.task

import com.ganguydhrubo.aira.core.run.ChannelPeer
import com.ganguydhrubo.aira.core.run.RunOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskUnlockPolicyTest {
    private val peer = ChannelPeer("remote", "owner", "thread")

    @Test fun scheduledTasksKeepUnlock() {
        assertTrue(requireNotNull(taskUnlockAuthorization(RunOrigin.Trigger, null)).invoke())
    }

    @Test fun channelTasksNeverGetUnlock() {
        for (candidate in listOf(null, peer, peer.copy(channelId = "local"))) {
            for (on in listOf(false, true)) {
                assertNull(taskUnlockAuthorization(RunOrigin.Channel, candidate, localUnlockEnabled = { on }))
            }
        }
    }

    /** UI tasks follow the local switch live: the check runs again on every call. */
    @Test fun uiTasksFollowTheLocalUnlockSwitchInRealTime() {
        var on = true
        var checks = 0
        val authorized = requireNotNull(taskUnlockAuthorization(RunOrigin.Ui, null, localUnlockEnabled = {
            checks++
            on
        }))
        assertTrue(authorized())
        on = false
        assertFalse(authorized())
        on = true
        assertTrue(authorized())
        assertEquals(4, checks)
    }

    @Test fun uiTasksWithTheSwitchOffGetNoUnlock() {
        assertNull(taskUnlockAuthorization(RunOrigin.Ui, null, localUnlockEnabled = { false }))
    }

    /** MCP and child tasks never get unlock, whatever the local switch says. */
    @Test fun mcpAndChildTasksStayUnauthorized() {
        for (origin in listOf(RunOrigin.Mcp, RunOrigin.Child)) {
            for (on in listOf(false, true)) {
                assertNull(taskUnlockAuthorization(origin, null, localUnlockEnabled = { on }))
            }
        }
    }
}
