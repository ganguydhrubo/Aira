package com.ganguydhrubo.aira.core.docs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The generated sections of README.md (built-in knowledge, provider presets) match their sources; if not, run `./gradlew updateReadme`. */
class ReadmeSectionsTest {
    private val root = File("..").canonicalFile

    @Test fun readmeMatchesGenerated() {
        val readme = File(root, ReadmeSections.README).readText()
        val expected = ReadmeSections.render(readme, ReadmeSections.sections(root))
        assertEquals("README.md generated sections are out of date; run ./gradlew updateReadme", expected, readme)
    }

    @Test fun renderReplacesOnlyBetweenMarkers() {
        val text = "before\n${ReadmeSections.start("x")}\nold\n${ReadmeSections.end("x")}\nafter\n"
        assertEquals(
            "before\n${ReadmeSections.start("x")}\nnew\n${ReadmeSections.end("x")}\nafter\n",
            ReadmeSections.render(text, mapOf("x" to "new")),
        )
    }

    @Test fun missingMarkerFails() {
        val e = assertThrows(IllegalStateException::class.java) { ReadmeSections.render("no markers", mapOf("x" to "new")) }
        assertTrue(e.message.orEmpty(), "missing" in e.message.orEmpty())
    }

    @Test fun readsPresetsFromSource() {
        val src = """
            ProviderPreset("Alpha", ProviderConfig.Kind.OpenAiCompat, "https://a.example/v1", caps()),
            ProviderPreset("Beta", ProviderConfig.Kind.Gemini, "https://b.example", caps(vision = true)),
            ProviderPreset("Custom", ProviderConfig.Kind.OpenAiCompat, "", caps()),
        """.trimIndent()
        assertEquals(
            listOf(
                ReadmeSections.Preset("Alpha", "OpenAiCompat", "https://a.example/v1"),
                ReadmeSections.Preset("Beta", "Gemini", "https://b.example"),
                ReadmeSections.Preset("Custom", "OpenAiCompat", ""),
            ),
            ReadmeSections.presets(src),
        )
    }

    /** Custom also lists Responses, last (matching the screen order), whatever order the presets use. */
    @Test fun customKindsListResponsesLast() {
        val presets = listOf(
            ReadmeSections.Preset("Alpha", "OpenAiCompat", "https://a.example/v1"),
            ReadmeSections.Preset("Beta", "OpenAiResponses", "https://b.example/v1"),
            ReadmeSections.Preset("Gamma", "Gemini", "https://c.example"),
            ReadmeSections.Preset("Custom", "OpenAiCompat", ""),
        )
        assertTrue(ReadmeSections.providersEn(presets).endsWith(
            "Presets for Alpha, Beta, Gamma and more; for any other provider pick Custom, which takes any OpenAI-compatible, Gemini or OpenAI Responses endpoint."))
    }
}
