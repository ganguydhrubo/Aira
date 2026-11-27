---
name: content-transcribe
display: Turn content into text
description: Turn videos, podcasts and articles into text: summary, chapters, quotes or a rewritten article
aliases: ["summarise a video", "summarize a video", "write up a podcast", "article summary", "get the subtitles", "transcribe"]
apps: ["youtube"]
sites: ["youtube.com"]
terms: ["summary", "summarise", "transcript", "transcribe", "subtitles", "captions", "podcast", "chapters", "key points", "quotes"]
---
## Entry

First ask which result is wanted: a full transcript, a summary, chapters, quotes, or a rewrite as an article. If the user does not say, write a summary of five to ten sentences.

Take the text from where the content lives: for an article, read the page body; for a video, use the subtitles or a transcript in the description; for a podcast, check whether the episode page has a transcript or show notes. When there is only audio, first transcribe it with `audio_transcribe` (a file in `/workspace/inbox/` or one you downloaded); if that tool is not available, the user has not yet set up a provider's transcription model, so ask the user for a transcript or subtitle file.

## Steps

1. **Get the full text**: read the page body with `web_read_page`; subtitle or transcript files the user sent are in `/workspace/inbox/`, read them with `read_file`; when there is only audio, transcribe it with `audio_transcribe`.
2. **Read in parts**: for long content, read it in several parts with a little overlap between them; after reading, remove the repeats and make sure no part from start to end was missed.
3. **Write in the requested form**: how to write each form, with samples, is in `references/transcript-output-formats.md`; read it before writing. The outline of a summary is: one or two sentences of overview; three to eight topics in the order they appear, each with a rough start time and two or three sentences of content; and finally the views, numbers and books or works worth noting.
4. **Write in the language of the original content**, unless the user asks for something else.
5. **Re-read**: after writing, check against the original: is the order right, is any topic missing, and can every time and quote be found in the original.

## Common pitfalls

- Subtitles are text only; charts, demos and on-screen text in the video cannot be read, so do not write as if you had seen the picture.
- Auto-generated subtitles often have wrong words and wrong sentence breaks; check proper nouns, names and numbers against the context or the description.
- Times are estimated from subtitle positions, so write "around minute 23", not a time exact to the second.
- Mark parts that are inaudible or have no subtitles; do not fill them in yourself.

## How to verify it is done (evidence)

The result covers every part of the original content; every quote and time can be found in the subtitles or body text that was fetched.

## When to stop and hand over to the user

When the content needs a sign-in or payment to view, or there is only audio and `audio_transcribe` is not available or the transcription failed, tell the user what is missing and ask them for a transcript.
