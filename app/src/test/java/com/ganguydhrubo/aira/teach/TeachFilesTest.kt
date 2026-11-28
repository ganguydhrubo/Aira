package com.ganguydhrubo.aira.teach

import com.ganguydhrubo.aira.core.learn.knowledge.GuideCatalog
import com.ganguydhrubo.aira.core.learn.knowledge.GuideView
import com.ganguydhrubo.aira.core.learn.skills.SkillStatus
import com.ganguydhrubo.aira.core.learn.teach.TeachDraft
import com.ganguydhrubo.aira.core.learn.teach.TeachEventKind
import com.ganguydhrubo.aira.core.learn.teach.TeachObservation
import com.ganguydhrubo.aira.core.learn.teach.TeachPhase
import com.ganguydhrubo.aira.core.learn.teach.TeachPublish
import com.ganguydhrubo.aira.core.learn.teach.TeachRoute
import com.ganguydhrubo.aira.core.learn.teach.TeachSession
import com.ganguydhrubo.aira.core.learn.teach.TeachSkill
import com.ganguydhrubo.aira.core.learn.teach.TeachTrace
import com.ganguydhrubo.aira.i18n.ResFileTexts
import com.ganguydhrubo.aira.skills.PersonalSkillFiles
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Locale

class TeachFilesTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun traceStaysLocalUntilTheDraftIsRejectedOrAccepted() = runBlocking {
        val id = "c".repeat(32)
        val secret = "验证码是 654321"
        val files = TeachFiles(tmp.root)
        var session = TeachSession(id, "找到筛选入口", TeachPhase.Recording)
        session = TeachTrace.reduce(
            session,
            TeachObservation(1_000, TeachEventKind.Text, "com.example.app", null, null, secret, "CAPTCHA", "otp", true, 0),
            "com.ganguydhrubo.aira",
        )
        session = TeachTrace.reduce(
            session,
            TeachObservation(2_000, TeachEventKind.Click, "com.example.app", "com.example.Filter", "列表", null, "筛选", null, false, 0),
            "com.ganguydhrubo.aira",
        )
        files.save(session)
        val raw = File(tmp.root, "teach/session.json").readText()
        assertFalse(raw.contains("654321"))
        assertTrue(raw.contains("筛选"))

        val skills = PersonalSkillFiles(File(tmp.root, "skills-root"))
        val drafted = TeachDraft.draft(session, TeachRoute.fold(session.steps, emptySet(), emptySet()) { it }, null, emptySet(), null, emptySet(), { false }, 5L)
        assertNotNull(drafted)
        val skill = drafted!!.skill
        assertEquals(SkillStatus.pending, skill.status)
        TeachPublish.toPending(skills, "draftc", skill)
        val hidden = GuideCatalog.withPersonal(GuideCatalog.loadBundled(), skills)
        assertTrue(hidden.items.none { it.name == skill.name })

        skills.reject("draftc")
        if (TeachSkill.retiresTrace(skill, id)) files.delete()
        assertNull(files.load())
        assertTrue(skills.pending().isEmpty())

        files.save(session)
        TeachPublish.toPending(skills, "draftd", skill)
        val accepted = skills.approve("draftd")
        assertEquals(SkillStatus.trial, accepted?.status)
        assertEquals(TeachSkill.SOURCE, accepted?.source)
        val shown = GuideCatalog.withPersonal(GuideCatalog.loadBundled(), skills).view(skill.name) as GuideView.Found
        assertTrue(shown.content.contains("not trial-run"))
        assertTrue(shown.content.contains(TeachSkill.NOT_TRIAL_RUN))
        assertFalse(shown.content.contains("trial-run succeeded"))
        if (accepted != null && TeachSkill.retiresTrace(accepted, id)) files.delete()
        assertNull(files.load())
        files.save(session)
        val other = accepted!!.copy(
            source = "user", learnedFrom = "run-1",
            markdown = accepted.markdown.replace("source: demonstrated", "source: user"),
        )
        if (TeachSkill.retiresTrace(other, id)) files.delete()
        assertEquals(id, files.load()?.id)
        if (TeachSkill.retiresTrace(accepted, id)) files.delete()
        assertNull(files.load())
    }

    @Test fun disclosureSaysARedactedSummaryMayBeUploaded() {
        val zh = ResFileTexts.chinese.strings.getValue("teach_disclosure")
        val en = ResFileTexts("values", Locale.ENGLISH).strings.getValue("teach_disclosure")
        assertTrue(zh.contains("发给Current model"))
        assertTrue(zh.contains("不会上传"))
        assertFalse(zh.contains("不会联网"))
        assertTrue(en.contains("redacted summary"))
        assertTrue(en.contains("nothing is uploaded"))
        assertFalse(en.lowercase().contains("never online"))
    }
}
