# Project experience

| Mechanism | When it applies | What was verified |
| --- | --- | --- |
| [Task goal count and action log](patterns/task-goal-progress.md) | A cross-app task stays at 0/N, or a whole run has actions but no count | Two runs tell apart a checklist missed in the first round from later missed reports; scripts cover late creation, counting and close-out |
| [Linking playback frames to real actions](patterns/task-playback-frames.md) | Back right after a tap, several actions in one round, markers on the result image | Unit tests link events to frames; the original #348 was verified on one phone |
| [Compose device test fails while input is initialising](patterns/espresso-input-initialization.md) | Before the test touches the UI, Espresso reports that `InputManager.getInstance` does not exist | #644 reproduced on one SDK 37 phone; both tests passed after upgrading the test dependency |
| [The denominator of the cache hit rate](patterns/cache-report-coverage.md) | Mixed providers, missing cache fields, old records with only totals | Mixed reporting verified by unit tests, and the scripted session display verified on one phone |
| [Live notification and task progress on the chat page](patterns/promoted-task-notifications.md) | No capsule on the home screen, "Working" shown all the time, or the task text moves on but the count stays at 0 | System display verified on a real Android 16 phone; on a real phone it stayed at "Find address" after switching apps; interference from same-name words and stalled AI stages reproduced in unit tests |
| [Progress checklist sent by the model as a tool call](patterns/progress-as-tool-call.md) | `task_progress · failed` appears in the first round, while phone actions carry on | A real-phone joint test caught 4 calls with this name and the checklist updated correctly; scripts cover invalid arguments and parallel calls |
