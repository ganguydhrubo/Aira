---
name: slack
display: Slack
packages: ["com.Slack"]
aliases: ["Slack", "slack"]
open: Slack
description: Workplace chat and collaboration: workspaces, channels, direct messages and thread replies
---
## What it is
Slack is a workplace chat and collaboration app for companies and teams, package `com.Slack`. Content is kept separate by workspace; inside a workspace it is organised into public and private channels, direct messages (DMs) and threads.

## Screens
The bottom navigation usually has four tabs: "Home", "DMs", "Activity" and "You" (some versions also have Catch Up).
- **Home and channel list**: at top left the current workspace picture and name (tap or swipe left to open the workspace switcher and settings); a search bar or magnifier in the middle or on the right; below, grouped lists: unreads, Threads, public channels (prefix `#`), private channels (small lock 🔒), external shared channels and Canvases.
- **Channel chat page**: the top shows the channel name, member count, search and a headphones icon (Huddle, a live voice room); the message timeline is in the middle; the message box at the bottom supports shortcuts, `@` mentions, emoji reactions and "+" to attach files.
- **Thread page**: open it by tapping "X replies" under a message in a channel. The top says "Thread" with the channel name and has a follow bell on the right; the input box at the bottom has an "Also send to #channel" checkbox.

## Features
- **Check the current workspace and channel**:
  - The user may belong to several company or community workspaces. When the app opens, always check the workspace picture at top left and the current channel title first. If the target channel is in another workspace, tap the picture at top left to open the sidebar and switch workspace.
- **Reply in threads**:
  - Team etiquette is to reply inside threads. Long-press a message or tap the reply count under it to open the thread. Type in the input box and send. Unless needed, **do not tick "Also send to #channel"**, so you do not flood the public channel.
- **Send messages with formatting**:
  - The input box supports bold, italic, strikethrough, code blocks and bulleted lists. Use `@` to mention a member, or `@here`/`@channel` to alert everyone.
- **Search history**:
  - The main search supports filters: `in:#channel-name` to limit to a channel, `from:@member` for a sender, `has:link` to find links.

## Known behaviour
- **Leftover state and deep threads**: after reopening, the app often stays inside a thread or DM chat, so the Home channel list is not visible. Tap "←" at top left to go back step by step.
- **Starting a Huddle by mistake**: the headphones switch at the top right of a channel is a live voice room. Tapping it turns on the microphone at once and plays a join sound to others, so do not turn it on casually.
- **Permission and archive limits**: a channel archived by an admin shows a notice at the bottom that it is archived and read-only; you cannot type anything new.
- **Two-factor and SSO prompts**: on first use or when the session expires, the app may open a browser or authenticator app for SSO sign-in.
