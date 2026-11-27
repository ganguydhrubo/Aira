# Task playback: from tap to back, the detail page in between must stay visible

Signal: a tap on a list is followed by back, but playback shows the list twice in a row; or the tap circle appears on the page after going back. Picking one image per round loses both the result frame and the source of the marker, and batch actions may also share a round.

Fix: link the original image, the result image and errors by the actual call ID. When both images exist, show them in order; the marker belongs only to the original image. Report evidence finds the result screenshot through the observation and call ID. The element centre is only a position hint, and a semantic scroll can only show direction. When an old record has only screenshots, do not guess the touch point. Handle "not captured" and "referenced file lost" separately.

Scope of verification: the original [PR #348](https://github.com/ganguydhrubo/Aira/pull/348) reproduced and verified the detail page, red circle and swipe on one phone. After the split, `PlaybackTest`, `TaskReportViewModelTest` and `TaskPlaybackActionsTest` check how events are linked to frames; unit tests cannot prove that a device's estimated element position matches the real touch point.
