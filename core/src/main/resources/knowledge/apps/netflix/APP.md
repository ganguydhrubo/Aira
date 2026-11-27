---
name: netflix
display: Netflix
packages: ["com.netflix.mediaclient"]
aliases: ["Netflix", "netflix"]
open: Netflix
description: Film and TV streaming: search titles, pick episodes and My List
---
## What it is
Netflix is a global film and TV streaming app, package `com.netflix.mediaclient`. It offers films, TV series, documentaries and anime to stream online or download for offline viewing.

## Screens
On first launch or when switching back to the app, a "Who's watching?" profile picker often appears.
- **Home**: "Shows", "Movies" and "Categories" pills are always at the top, with the cast icon and search magnifier at top right. A large poster for the featured title sits at the top; below are rows such as "Continue Watching", popular picks, "Top 10 Today" and "My List".
- **Bottom navigation**: "Home", "New & Hot" and "My Netflix" (includes downloads and My List).
- **Title page**: a trailer or poster at the top; below it the title, release year, age rating, number of seasons and quality (HD/4K), followed by two large buttons: the white "Play" and the grey "Download". Lower down are the episode list (with a season drop-down), similar titles and cast and crew.
- **Full-screen player**: tap the screen to show the controls: pause/play, back 10 seconds and forward 10 seconds in the middle; the progress bar at the bottom; and a toolbar with "Lock", "Episodes", "Audio & Subtitles", "Speed" (0.5x to 1.5x) and "Next Episode".

## Features
- **Choose a profile**:
  - If the app opens on the profile grid, tap the target profile picture to enter that profile's home. If the profile has a 4-digit PIN, enter it on the number pad to unlock.
- **Search titles and genres**:
  - Tap the search magnifier at top right and type the title (in English or its original language), actor or director. Results show as a grid of posters.
- **Pick episode and season**:
  - On a series page, scroll down to "Episodes", tap the "Season 1" drop-down to switch to the right season, then tap the episode row to play it.
- **Set audio and subtitles**:
  - In the player controls, tap "Audio & Subtitles" to open the options panel. Choose the audio track (such as English 5.1) and the subtitles (such as English). Then tap apply at the top right.

## Known behaviour
- **DRM black screen (FLAG_SECURE)**: because of copy protection, the video area appears completely black in accessibility screenshots. **This is normal protection; do not treat it as a loading failure or crash.** Use the element tree and screen actions for the controls.
- **Leave the player**: in full-screen playback, press the system back key or tap the left arrow "←" at top left to leave the player and return to the title page.
- **Download limits**: some titles have no download button because of licensing. On mobile data it may say downloads are Wi-Fi only; this is a built-in safeguard.
