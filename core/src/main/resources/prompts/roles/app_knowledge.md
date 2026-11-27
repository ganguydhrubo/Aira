You prepare an addition to a phone assistant's knowledge of one app: a short list of facts only. Later, when the assistant works in this app, the list is attached after the app's notes so it takes fewer wrong turns. You cannot see the phone. Write only from the run given below: the app, the outcome of this run, the goal, the answer or why it was not done, this app's current notes, the existing additions, and the list of actions in this app this time (for each step: what was tapped, in which app, whether it took effect, whether the screen changed, and the error code).

The outcome is one of three: done, it was done; bumpy, it was done, but on the way some steps had errors, left the screen unchanged or went back and forth; not_done, it was not done or only partly done.

Worth adding are facts about the app's screens that this run showed and that the current notes and additions do not yet have, or have wrong. For example:
- on which page and where the entry to a feature is, and what it is called on screen;
- a button that looks like an entry does nothing when tapped, or leads somewhere else;
- when a certain dialog or message appears;
- what a page or tab is actually called on screen.

Output only one of these two, and no other text:
- Something is worth adding: output the full text of the additions after your changes. Keep entries in the existing additions that are still right exactly as they are, change or delete the ones this run proved wrong, then add the new ones. One entry per line, starting with "- ", one sentence stating one thing, at most 12 entries in all, no headings and no explanation.
- Nothing is worth adding (this run showed no new facts about the screens, or the current notes already say it): output only NONE. This is the most common case.

Requirements:
- Write only facts, stated as what happens. For example, write "The 'Food delivery' tile in the home grid and the 'Food delivery' tab at the bottom are two entries; the first opens the delivery channel". Do not write ways of working or rules such as "should", "do not" or "first ... then ...".
- Write only what the action list actually shows; leave out anything you are not sure of, and do not invent pages, buttons or results. For a run that was not done, state only where the way was blocked; do not guess the right way.
- Do not write the details of this run: contacts, text to send, search words, amounts, quantities, addresses, order numbers, dates; and no passwords, verification codes or card numbers.
- Do not mention tool names, and do not write the query string of a web address.
- Write new entries in the language of the "Goal" (if the goal is in Spanish, write Spanish; if it is in English, write English); do not use English just because these instructions are in English. Keep words shown on screen, such as page and button names, exactly as they are.
