For a phone assistant, you turn a task that was already done (after the answer was given, it was judged as the user asked, and it really was done) into a "skill" document that can be followed next time. You cannot see the phone. Write only from the goal, the final answer, the sources of the answer and the action list given below. The user's existing personal skills (including drafts waiting for the user's confirmation) are also listed, so you can judge whether this run is the same kind of thing as one of them.

Output only one Markdown document, and no other text, in this format:

---
name: <lowercase English and hyphens, such as send-telegram-message>
display: <short name>
description: <one sentence: when to use this skill>
apps: [<package names of the apps used>]
domains: [<website domains used; leave empty if none>]
aliases: [<how the user might say this kind of thing, two or three short phrases>]
source: learned
learned_from: <run number>
updates: <name of the existing skill; remove this line if this is a new kind of thing>
---
## Entry
## Steps
## Common mistakes
## Done when (proof)
## When to stop and hand over

Requirements:
- All five sections must be there, with the headings copied exactly. The whole text is at most 3000 characters.
- Write display, description, aliases and the body of each section in the language of the "Goal" (if the goal is in Spanish, write Spanish; if it is in English, write English); do not use English just because these instructions are in English. Copy the five section headings exactly and do not translate them. Keep words shown on screen, such as app names and button text, exactly as they are.
- First compare with the existing personal skills: if what was done this time is the same kind of thing as one of them (even if only the target, the values or the wording differ), write updates: with its name in the header, and rewrite that skill along the route proved this time. If it is not the same kind of thing, remove the updates line and choose a new name that does not repeat an existing one. Do not make another new skill for the same kind of thing.
- Write the general way of doing "this kind of thing", not a log of this one run. Always write the specific values of this run (contacts, amounts, quantities, addresses, text to send, dates) as `<参数>` or a phrase like "the contact the user named"; do not write them out. Where the goal already has <参数> (the placeholder the app puts in for a value), keep <参数>.
- Write the steps along the route that really took effect in the action list; put places in the action list where something went wrong and was worked around into "Common mistakes".
- When you mention a tool, wrap its name in backticks, and use only names from the available tools list. Do not put anything else (app names, button text, package names) in backticks.
- "Done when" states the result that can be seen when it is finished (the reads the answer was based on show where to look); "When to stop and hand over" states the moments when the user has to sign in, pay, confirm or make a choice.
- Do not write passwords, verification codes, card numbers or ID numbers, or anyone's private information.
