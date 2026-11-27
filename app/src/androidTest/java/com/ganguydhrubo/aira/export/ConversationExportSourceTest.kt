package com.ganguydhrubo.aira.export

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ganguydhrubo.aira.chat.ConversationStore
import com.ganguydhrubo.aira.chat.SharedFile
import com.ganguydhrubo.aira.core.agent.Turn
import com.ganguydhrubo.aira.core.export.ExportContent
import com.ganguydhrubo.aira.core.export.ExportDocumentBuilder
import com.ganguydhrubo.aira.core.export.ExportIds
import com.ganguydhrubo.aira.core.export.ExportLabels
import com.ganguydhrubo.aira.core.export.ExportSelection
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationExportSourceTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun exportingEverythingInThePreviewExcludesLaterTurnsAndFiles() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = ConversationStore(temp.newFolder())
        val id = "conversation"
        store.append(id, Turn("shown question", "shown answer", "succeeded"))
        val preview = ConversationExportSource(context, id, store.records(id))

        store.append(id, Turn("later question", "later answer", "succeeded"), listOf(
            SharedFile("later.pdf", 10, "later.pdf", "content://test/later"),
        ))
        val doc = ExportDocumentBuilder(preview, ExportLabels.English, "test", "en").build(
            id, "test", ExportSelection(id), ExportContent(),
        )

        assertEquals(listOf(ExportIds.user(0), ExportIds.answer(0)), doc.blocks.map { it.id })
        assertTrue(doc.assets.isEmpty())
    }
}
