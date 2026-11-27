---
name: skill-authoring
display: Write a skill
description: Use when the user wants to create, import or change a skill (a fixed way of doing one kind of task): how to write it, check for duplicates and save it
aliases: ["create a skill", "new skill", "write a skill", "save as a skill", "import a skill", "edit a skill", "skill"]
terms: ["skill", "method", "process", "steps", "template", "always do it this way", "save as skill", "import", "rewrite"]
---
## When to write one

- Write one when the user clearly asks to "create a skill", "save this way of doing it", "import this as a skill" or "change a skill". If the user is only talking about how to do something or asking whether it can be done, answer first; do not save anything on your own.
- A skill describes how to do one kind of task from now on, not a one-off fact or preference. "I don't eat coriander" is a memory, not a skill.
- Only `skill_save` can write a skill. If you do not have this tool, tell the user plainly that it cannot be saved this time; do not say it has been saved.

## First check whether one exists

1. Search once with `skill_search` using the usual words for this kind of task.
2. If there is a personal skill (personal) for this kind of task: read it in full with `skill_read`, change it on that basis, and when saving keep its name and include the version you read. Do not start a second one for the same kind of task.
3. If only a built-in skill matches: built-in skills cannot be changed. If the user wants a different way of doing it, write it as the user's own skill under a new name, and tell the user which one will be followed from now on.

## Make it clear before writing

When the user gives only one vague line ("make me a skill for ordering coffee"), first use `ask_user` to clear up the one or two points that most affect the method: which app or website, at which step to stop, and any fixed requirements. List the points to be saved in a few lines for the user to confirm, then save. If the user has already explained the method clearly or pasted the full content, no need to ask again.

## How to write it

The whole skill is one document: a front-matter header plus five sections, with the section headings copied exactly as below, and no more than 4,000 characters in total.

```
---
name: order-coffee
display: Order coffee
description: Use when the user wants to order a coffee for delivery or pickup
aliases: ["order coffee", "buy a coffee"]
---
## Entry
## Steps
## Common mistakes
## Done when (proof)
## When to stop and hand over
```

- Keep the five section headings exactly as in the template above: the save check looks for headings starting with Entry, Steps, Common mistakes, Done when and When to stop and hand over, and rejects the skill otherwise. Write the content under them in English.
- name uses lowercase English letters, digits and hyphens, and says which kind of task it is; display is a short English name.
- description is one sentence on "when to use it"; put the ways the user might ask for it into aliases: whether the skill ever gets used depends entirely on these two.
- Entry says where to start (which app, website or search); Steps are numbered short sentences, one action per step, naming buttons and pages, not screen coordinates.
- Things that change each time (names, amounts, addresses, the text to send) are written as `<parameter>` or "the contact the user names", not fixed values.
- Common mistakes covers where things go wrong and how to get around them; Done when says where the result can be seen once finished; When to stop covers the moments that need the user to sign in, pay, confirm or choose.
- Use backticks only around tool names; do not put app names or button text in backticks. Do not write passwords, verification codes, card numbers or other people's private information.
- Shorter is better: skip what the model already knows; write only the methods and traps special to this kind of task.

## Importing content the user pastes

When the user pastes a skill, notes or a process written elsewhere, rewrite it into the format above: keep the method itself, remove what does not apply here (commands for other software, tools that cannot be used on the phone, scripts), and where a section is missing, add a sentence or two in keeping with the original. Do not carry over accounts, passwords or similar from the original.

## Saving and telling the user

1. Save with `skill_save`: markdown is the whole document; quote is the user's exact words asking you to create or change the skill this time.
2. It is saved only if the result is ok. Tell the user according to the result: the skill's name, whether it is new or an existing one changed, that it can be seen in "Settings › Memory & skills", and that it will be tried first the next time this kind of task comes up.
3. If the result fails, fix it according to the reason and save once more; if it still fails, say honestly that it was not saved and why; do not say it has been created.
4. Renaming and deleting cannot be done here: ask the user to delete the old one in "Settings › Memory & skills"; to change the name, save a new copy under the new name, then ask the user to delete the old one.

## How to verify it is done

`skill_save` returns ok and the name in the result is the one meant to be saved; the name and the new-or-changed status in your answer match the result.
