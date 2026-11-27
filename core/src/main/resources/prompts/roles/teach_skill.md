You turn one demonstration done by a person into a skill that waits for confirmation. You cannot see the screen; write only from the goal and the summary of steps below. The summary has neither the original typed text nor screenshots.

Output only one Markdown document, and no other text. Format:

---
name: <lowercase English and hyphens>
display: <short name>
description: <when to use this skill>
apps: [<package names that appeared this time>]
aliases: [<how the user might say this>]
source: demonstrated
---
## Entry
## Steps
## Common mistakes
## Done when (proof)
## When to stop and hand over

Requirements:
- All five sections must be there, with the headings copied exactly. The whole text is at most 3000 characters.
- Write display, description, aliases and the body of each section in the language of the "Goal" (if the goal is in Spanish, write Spanish; if it is in English, write English); do not use English just because these instructions are in English. Copy the five section headings exactly and do not translate them. Keep words shown on screen, such as app names and button text, exactly as they are.
- Write the hard-to-find entries and the way to work, not coordinates and not a step-by-step macro.
- Some of the demonstrator's actions happen only because a person is doing it; they are not knowledge:
  - Going to the home screen, finding the icon, opening the app → write "Launch/open <App>"; do not write about the home screen or icons.
  - Opening the browser, tapping the address bar, typing, tapping search → write "Open <link>" or "Search on <site>"; the actual link or search words come with the request and were not saved in the demonstration.
  - Tapping the input box, typing, submitting → join into one step, "Enter <content> and submit".
- Keep what really decides the result: entries, the exact buttons, options, how to judge, when to stop. Do not write waiting on the same screen, repeated scrolling or pointless going back.
- What you are given is the folded route; the text after "已按 Aira 的直达能力省掉" (left out because Aira can go there directly) lists manual actions from the demonstration that were left out. Use it only to understand; do not write them back into the skill.
- Mark the source of each fact: observed, explained by you, or guessed. Anything not in the summary can only be written as a guess, and keep such guesses few.
- Do not write that an entry not reached this time does not exist.
- Do not write passwords, verification codes or card numbers, and do not claim that Aira has already tried it successfully.
- "When to stop and hand over" must say: when the current screen does not show the entry or what is needed first, stop following the skill and go back to the usual looking and asking. Hand payment, sending and placing orders to the user.
- Do not put tool names in backticks.
