package com.ganguydhrubo.aira.settings

import org.junit.Assert.*
import org.junit.Test

class TaskTemplateTest {
    @Test fun repeatedNamesShareOneValueInFirstAppearanceOrder() {
        val prompt = "查看{{ Contacts }}在{{时间范围}}的消息，给{{联系人}}写回复。"
        assertEquals(listOf("People and groups", "时间范围"), TaskTemplate.fields(prompt))
        assertEquals("查看小王在Today's messages，给小王写回复。", TaskTemplate.render(prompt,
            mapOf("People and groups" to " 小王 ", "时间范围" to "Today")))
    }

    @Test fun missingOrBlankValuesCannotProduceADraft() {
        assertNull(TaskTemplate.render("{{对象}}：{{内容}}", mapOf("对象" to "Classmate")))
        assertNull(TaskTemplate.render("{{对象}}：{{内容}}", mapOf("对象" to "Classmate", "Content" to " \n ")))
    }

    @Test fun valuesAreLiteralAndNeverRecursivelyExpanded() {
        val value = "第一行\n{{另一个字段}} \\$1 \\路径"
        assertEquals(value + " / 完成", TaskTemplate.render("{{内容}} / {{另一个字段}}",
            mapOf("Content" to value, "另一个字段" to "Done")))
    }

    @Test fun ordinaryTextAndInvalidMarkersRemainLiteral() {
        val text = "JSON: {\"a\":1}; {{}}; {{  }}; {{未闭合; {{跨\n行}}"
        assertTrue(TaskTemplate.fields(text).isEmpty())
        assertEquals(text, TaskTemplate.render(text, emptyMap()))
    }

    @Test fun limitsBoundTheFormAndRequireValidNames() {
        val prompt = (1..TaskTemplate.MAX_FIELDS + 1).joinToString { "{{字段$it}}" }
        assertNull(TaskTemplate.render(prompt, (1..13).associate { "字段$it" to "Value" }))
        assertTrue(TaskTemplate.validName("People and groups"))
        assertFalse(TaskTemplate.validName(" "))
        assertFalse(TaskTemplate.validName("名字\n内容"))
        assertFalse(TaskTemplate.validName("{名字}"))
        assertFalse(TaskTemplate.validName("名".repeat(41)))
    }
}
