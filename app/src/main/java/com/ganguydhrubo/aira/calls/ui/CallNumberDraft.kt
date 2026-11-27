package com.ganguydhrubo.aira.calls.ui

import com.ganguydhrubo.aira.calls.CallPolicy
import com.ganguydhrubo.aira.settings.ui.models.edit

/** Both tabs edit the same text, including unfinished manual entries. */
internal class CallNumberDraft(private val text: String) {
    private val entries = text.split(',', '，', '،', ';', '；', '\n')
        .map(String::trim).filter(String::isNotEmpty)
    private val numbers = entries.mapNotNull(CallPolicy::incomingNumber)
        .distinctBy(CallPolicy::incomingMatchKey)
    val keys = numbers.mapNotNull(CallPolicy::incomingMatchKey).toSet()
    val valid = entries.all { CallPolicy.incomingNumber(it) != null } && keys.size <= CallPolicy.MAX_ALLOWED_NUMBERS
    val selectedNumbers = numbers.toSet()

    fun toggle(number: String): String {
        val key = CallPolicy.incomingMatchKey(number) ?: return text
        if (key in keys) return entries.filterNot { CallPolicy.incomingMatchKey(it) == key }.joinToString("\n")
        if (keys.size >= CallPolicy.MAX_ALLOWED_NUMBERS) return text
        return if (text.isBlank()) number else text.trimEnd() + "\n" + number
    }
}
