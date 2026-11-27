package com.ganguydhrubo.aira.ui

import android.app.Notification
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ganguydhrubo.aira.settings.FeatureFlags
import com.ganguydhrubo.aira.settings.ScheduledQuietHours
import com.ganguydhrubo.aira.task.TaskService
import com.ganguydhrubo.aira.task.applyScheduledQuietHours
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class ScheduledQuietHoursDeviceTest {
    @Test fun quietHoursSilenceAnIndividualNotificationOnTheExistingChannel() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val flags = FeatureFlags.get(context)
        val previous = flags.scheduledQuietHours()
        val now = LocalTime.now()
        try {
            flags.setScheduledQuietHours(ScheduledQuietHours(true, now.minusHours(1), now.plusHours(1)))
            val quiet = Notification.Builder(context, TaskService.CHANNEL_RESULT)
                .applyScheduledQuietHours(context).build()
            assertEquals(TaskService.CHANNEL_RESULT, quiet.channelId)
            assertNotNull(quiet.group)
            assertEquals(Notification.GROUP_ALERT_SUMMARY, quiet.groupAlertBehavior)
            assertEquals(0, quiet.flags and Notification.FLAG_GROUP_SUMMARY)

            flags.setScheduledQuietHours(ScheduledQuietHours.DEFAULT)
            val ordinary = Notification.Builder(context, TaskService.CHANNEL_RESULT)
                .applyScheduledQuietHours(context).build()
            assertNull(ordinary.group)
        } finally {
            flags.setScheduledQuietHours(previous)
        }
    }
}
