---
name: tiktok
display: TikTok
packages: ["com.zhiliaoapp.musically"]
aliases: ["TikTok", "tiktok"]
open: TikTok
pace: careful
description: Short video and live streaming social app: For You feed, create, search and messages
---
## What it is
TikTok is a popular short video and live streaming social app, package `com.zhiliaoapp.musically`. It is the international version of Douyin. It has strict automation detection, so browse at a moderate pace (`pace: careful`).

## Screens
The bottom navigation has five tabs: "Home", "Friends", "+" (create), "Inbox" and "Profile".
- **Home**: at the top centre you can switch between "For You", "Following", "Friends" and "LIVE"; the search magnifier is at top right. The main area is a single full-screen vertical feed of short videos.
- **Right-side action column**: from top to bottom: the creator's picture (with a red plus to follow), the heart like icon (with like count), the comment bubble (with comment count), the yellow bookmark save icon, the share arrow (repost, send as message, copy link), and at bottom right the spinning record for the original sound.
- **Search**: opened from the magnifier at the top right of Home. Results can be filtered by "Top", "Users", "Videos", "Sounds", "LIVE" and "Hashtags".
- **Inbox**: at the top are the direct message chats and online contacts; below are system notifications and activity (likes, comments, new followers).

## Features
- **Full-screen swiping and pausing**:
  - Swipe up to go to the next recommended video; swipe down to go back to the previous one.
  - Tap anywhere on the screen to pause or resume.
- **Like and read comments**:
  - Double-tap the video or tap the heart on the right to like it.
  - Tap the bubble icon to open the half-screen comments sheet from the bottom. Scroll to read comments, like good ones, or write a reply in the "Add comment..." box at the bottom. Swipe the sheet down or tap "×" at top left to close it.
- **Follow creators and sounds**:
  - Tap the "+" under the creator's picture to follow directly, or tap the picture or username to open their profile and see all their videos and pinned videos.
  - Tap the spinning record at bottom right to open that Sound's page and see other popular videos using the same music.

## Known behaviour
- **Anti-bot checks for fast swiping**: very fast swiping or rapid tapping in a short time causes swiping to freeze or a jigsaw puzzle check to appear. Always follow `pace: careful`.
- **Forced sign-in prompt**: when not signed in, after a few videos a full-screen "Log in to TikTok" page often appears that cannot be skipped (Google, Facebook or phone number sign-in).
- **LIVE rooms in the feed**: the For You feed sometimes shows a live room (with a red "LIVE" label and a different layout from normal videos). Swipe up to move past it quickly.
