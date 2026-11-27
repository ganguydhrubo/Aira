---
name: youtube.com
display: YouTube
domains: ["youtube.com", "youtu.be"]
aliases: ["YouTube"]
apps: ["youtube"]
open: youtube.com
mode: desktop
description: Video platform to search videos and channels, and read descriptions, comments and caption text
---
## What it is
A video platform.

## Pages and URLs
- Search: `https://www.youtube.com/results?search_query=<encoded-term>`.
- Video: `/watch?v=<id>`; for a channel use the channel link.

## Features
- **Videos**: search results show the title, channel, length, view count and upload time. On the video page you can expand the description and scroll down to load comments. Public videos need no sign-in.
- **Channels**: name, subscriber count, About description and latest videos.
- **Captions**: for videos with captions, the caption text can be fetched; long videos have very long captions.

## Known behaviour
- View and subscriber counts are often shortened, such as "120K" or "1.2 lakh"; open the details for the exact number.
- Private, members-only, age-restricted or region-blocked videos do not show their content; this is a different case from "no search results".
- Captions may be unavailable because the video has none, because it is restricted, or because of a network problem.
