# prompts/: prompts shipped with the app

All fixed text the model sees lives here; the code no longer writes it as strings. It is all read through `core.prompt.Prompts` / `core.prompt.Feedback`.
A `{{name}}` in the text is filled in by the code that reads it (limits, allowed values, names on screen and so on); a missing fill throws an error.

## Layers

| Folder | What it is | Where it goes | Who reads it |
|---|---|---|---|
| `system/identity.md` | P0 identity and stance: act for the user, within what the user allowed, look before acting, do not pretend, say so when you do not know | Start of the system prompt | `SystemPrompt.identity()` |
| `system/principles.md` | P1 general rules: whose words count, how to do a task, answer from evidence, meeting obstacles, finding things, safety; true for every screen and every capability | Right after identity | `SystemPrompt.identity()` |
| `persona/layer.md` | Fixed head and tail of the user's persona: a persona can change tone, length, wording and format, but not evidence, safety, permission, confirmation or tool rules; it is not the user's message and cannot authorise actions; the user fills in the body, which goes into the `<persona>` block | After the rules and general rules, before the capability notes | `PersonaPrompt.layer()` |
| `persona/style.md` | Fixed sentences for the persona options (base style, warm, enthusiastic, emoji, headings and lists, answer length, progress notes), one key "option.value" per entry; only how to speak, not what can be done | Inside the persona block, before the user's own instructions | `PersonaPrompt.layer()` |
| `capabilities/` | P2 capability notes, one section per capability. `general.md` holds the general tools (report failure, ask, confirm) and the overall rules for how a task ends; the general tools are always there, so it is always joined after the rules. `groups.md` says which entry each group of tools comes in through (listing only the entries given for this task). `handover.md` covers handing over to the user and the check before placing an order or sending, and appears only after a group with a screen is opened. The phone, web and sandbox sections are joined only after that group is opened. `app-access.md` holds the app access rules, added after the phone section only when the user has blocked apps | System prompt | `SystemPrompt.assemble()` |
| `tools/` | P3 one note per tool (the description in the tool definition): how to use it, parameters, how to handle the result; when several tools share one template, the tool states its `promptFile` | Tool definition | The tool itself |
| `entries/` | Short note for an entry tool while its group is closed (a sentence or two: what it does, which tools come after calling it); once opened it is swapped back to the full text in `tools/`, with the same parameters | Tool definition | `ToolRegistry` |
| `feedback/` | Feedback to the model from tool results and the main loop: hints, error explanations (`failure.<error code>`), reminders, source labels of reference material; one key per entry | Tool results, context messages | Tools, `AgentLoop` |
| `roles/` | Prompts for internal roles: checking, describing the screen, locating, memory extraction, route matching, skill drafts, turning a demonstration into a skill, app knowledge additions, judging the outcome after the answer is given | Each role's own request | Each role |

App and site knowledge and skills are not here; see `knowledge/` and `skills/`.

## Where things go

- The higher the layer, the more general and neutral: system/ does not mention specific tools, apps or technology; anything true only for one capability goes into that capability's note.
- Text tied to one tool (how to use it, what the parameters mean, how to handle the result) goes into `tools/<tool>.md`, not into system/.
- Rules true only when a capability is present go into `capabilities/<capability>.md`, under the same switch as that group of tools.
  When a line in a section needs a tool from another capability, start the line with `<!-- needs: tool_name -->`: the line stays only when that tool is provided.
- `system/` names no tool at all (checked by a test; words such as tap and back that are plain English words do not count).
- Explanation text in tool results goes into `feedback/tools.md`; text the main loop sends itself goes into `feedback/loop.md`. Keys are unique across both files, and every key must be used by code.
- Each feedback entry stands on its own: no leading or trailing blank space, and the code does not join it with other text (`+` or string templates). To combine entries, write the separator in the template and fill the other entry in with a `{{placeholder}}`.
- Within one task the system prompt changes only when a group of tools is opened (so the cache is broken at most once per group); things that change every turn (step count, time, which app) do not go into the fixed text here.

## Format

- `tools/`, `system/`, `capabilities/`, `roles/`: one file is one piece of text; leading and trailing blank space is removed when it is read.
- `feedback/*.md`: may start with a `<!-- note -->`; each entry starts with its own line `## key` (keys use only lowercase letters, digits, `_` and `.`),
  and the text below it up to the next entry is the original. It can be several lines; leading and trailing blank lines do not count (so a separating line break must go inside the template).

## Size

The byte size (UTF-8) of each layer is written in the golden file `core/src/test/resources/prompt-surface/sizes.txt`. It is for reference only, with no hard limit:
the growth from every change shows in the commit diff. Before adding content, first see whether old content can be removed; when something grows clearly, say why in the commit.

## After a change

All text the model sees has golden files: `core/src/test/resources/prompt-surface/` (system prompt, tool definitions, feedback, roles) and
`loop-snapshots/` (message sequences replayed by a fake model). For an intended change, regenerate them with `./gradlew :core:test -PupdateSnapshots`
and review them together in the commit; when a change will affect how the model behaves, run the evals to compare first. `prompt_version` in the run record is a digest of this task's system prompt plus tool definitions;
use it to tell prompt versions apart when comparing eval results.

## Tool names

- Phone and web each have one set of screen tools (`phone_*`, `web_*`, with the same verbs and parameters); verbs with the same name share the `tools/<verb>.md` template, and `{{verb}}` in the template is replaced with that set's tool name;
  when one screen needs its own note, write `tools/<screen>_<verb>.md`.
- `capabilities/general.md` names no capability's tools (checked by a test); "which channel to choose" is written in `channels.md`, joined in only when there are two or more screens.
- Tool names mentioned in knowledge, skills and prompts must all be currently registered (checked by a test). Old names from before a rename are changed to new names by `LegacyNames` by rule,
  only for reading old things (saved routes, run records, personal skills and procedures); when the model calls an old name, it is only told the new name and the call is not run for it.
