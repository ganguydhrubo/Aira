---
name: configure-aira
display: Change Aira's own settings
description: Use this to view and change Aira's own settings when the user asks; models, API keys and deleting data are not covered
aliases: ["change your settings", "adjust your settings", "be a bit faster", "don't mute", "change the look"]
terms: ["settings", "configuration", "faster", "slower", "mute", "appearance", "dark", "light", "capsule"]
---
## First see what can be changed

Call `settings_list` first. It lists every item that can be changed right now: the key, the name shown to people, the current value and (when there are only a few choices) all allowed values.

**Use only the keys and values given in the list**; do not guess a word yourself. When what the user says does not match what you see (for example the user says "don't mute" and the list calls it "mute_apps"), pick the item by its name and read its `label` back to the user to confirm it is the right one.

## Say one line before changing

When the user has clearly said what to change, change it directly with `settings_set` and then tell the user the new value. When the user is vague ("make it a bit faster"), first say which item you plan to change and to what value, then change it:

> I have set the working pace to "Fast". It will apply from the next task.

**Do not change anything the user did not mention.** When changing several items at once, report them one by one.

## Things you cannot change

- **Model, API key, sign-in**: not in the list. When the user wants these, tell them to go to "Settings › Models"; you cannot do this step.
- **Deleting data** (clearing the workspace, resetting the runtime environment, deleting conversations): also not in the list; ask the user to do it themselves.
- **Container internet access and proxy**: not in the list. These two let data leave the phone, so the user must switch them on or fill them in themselves under "Settings › Files & computing environment". Do not change them for the user.

These limits are deliberate: the first group would let the model swap out its own brain and spend the user's money; the second group cannot be undone; and the internet line must be drawn by the user.

## When changes take effect

Most changes take effect from the next task; appearance (dark or light) changes at once. Mention this after the change so the user does not think it failed.
