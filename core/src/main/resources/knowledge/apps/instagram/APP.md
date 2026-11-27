---
name: instagram
display: Instagram
packages: ["com.instagram.android"]
aliases: ["Instagram", "instagram", "IG", "ins"]
sites: ["instagram.com"]
open: Instagram
pace: careful
description: Photo and Reels social app: feed, stories, explore and direct messages
---
## What it is
Instagram is a global photo and short-video social app, package `com.instagram.android`. For the website, see `instagram.com`. The platform has strict anti-bot checks, so browse at a moderate pace (`pace: careful`).

## Screens
The bottom navigation has five tabs: "Home" (feed), "Search & Explore", "+" (create), "Reels" and "Profile".
- **Home (feed)**: the Instagram logo at top left; the notifications heart and the messages entry (paper plane / Messenger icon) at top right. At the very top is the side-scrolling row of round "Stories" pictures; below is the single-column feed of photos and videos.
- **Post card**: at the top the poster's picture, username, location and a three-dot more menu; in the middle a single photo, a carousel of several photos (with small dots at the bottom) or a video; below are like (heart), comment (bubble), share (paper plane) and save (bookmark); further down the like count, the caption and "View all X comments".
- **Explore**: a grid of popular photos and short videos, with the main search box at the top.
- **Reels**: full-screen vertical video feed, with like, comment, share, save and original audio buttons stacked on the right.
- **Direct messages**: opened from the paper plane at the top right of Home. Has the recent chats list, active followers' circles and a search box.

## Features
- **Avoid leftover state and go back level by level**:
  - If the app opens on a full-screen Reel or deep inside a DM chat, the bottom navigation may be hidden. Tap the back arrow "←" at top left or press the system back key to leave that section and return to Home.
- **View Stories**:
  - Tap a round profile picture to open stories. Each story shows for a few seconds and then moves on by itself. Tap the right half of the screen to skip to the next one and the left half to go back; swipe down from the top to close stories.
- **Browse carousels**:
  - For a carousel post (with small dots under it), **swipe left on the image** to see the next photos or videos.
- **Like and comment**:
  - Double-tap the photo or tap the heart to like it. Tap the bubble icon to open the comments sheet, write a reply in the box at the bottom and tap "Post".

## Known behaviour
- **Screenshots and black screens (FLAG_SECURE)**:
  - Most normal feed and public post pages can be captured on most phones, so try screenshots and visual analysis as usual.
  - But in Vanish Mode DMs, View Once media, or strict privacy modes on some phones, the app turns on `FLAG_SECURE` screenshot protection and screenshots come out completely black.
  - **If you get a black screen, do not be confused and do not treat it as a crash or freeze.** Switch to reading the accessibility node text (`interfaces`) to identify elements and content, or press back to leave the protected screen. Capturing View Once media in DMs may also notify the other person, so avoid it unless needed.
- **Rate limits (pace careful)**: liking very quickly, scrolling fast again and again, or sending many DMs in a short time triggers a "Try again later" action block. Leave gaps between actions.
- **Messages are not in the bottom bar**: Instagram's bottom bar has no separate messages tab; the messages entry is always at the top right of Home. If you are not on the Home tab, first switch to the first bottom tab, "Home", to see it.
- **Save login and contact sync prompts**: after signing in or updating, full-screen prompts such as "Save your login info?" or "Find your Facebook friends" often appear. Tap "Not now" to skip.
- **Full-screen create mode**: tapping "+" in the middle opens a full-screen camera or gallery picker. Tap "×" at top left to cancel and leave.
