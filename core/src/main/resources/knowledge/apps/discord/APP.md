---
name: discord
display: Discord
packages: ["com.discord"]
aliases: ["Discord", "discord", "DC"]
open: Discord
description: Community voice and text chat: servers, channels, direct messages and friends
---
## What it is
Discord is a community chat app, package `com.discord`. Content is organised into servers (guilds) and channels, and it also supports direct messages (DMs) and voice calls.

## Screens
The bottom navigation usually has four tabs: "Servers", "Messages", "Notifications" and "You" (profile). Some versions have a separate search tab.
- **Server and channel drawer**: the server list is on the left (direct messages at the very top, then the icons of joined servers, and at the bottom "+" to add and "Explore"). Tapping a server shows its categories and channels (# text channels, 🔊 voice channels, 📢 announcements, 💬 forums).
- **Chat screen**: the top shows the current channel name (`#channel-name`) or the DM person; on the left a back/drawer menu button, on the right search, notifications and the member list. The input area at the bottom has "+" to attach files, Nitro gift, GIF, emoji, and the text box "Message #...".
- **Messages page**: search and a "New message" button at the top, with friends' online status and recent DM conversations.

## Features
- **Know where you are (do not be misled by the last screen)**:
  - The app often reopens exactly where it was left (for example a channel deep inside a server, a post or a DM).
  - **Do not send unrelated messages or search for unrelated things in whatever channel happens to be open.** To send a DM, first switch to the "Messages" tab at the bottom; to go to another server, first tap the back/drawer button at top left to return to the server list, then pick the target.
- **Find a channel and send a message**:
  - In the server's channel list, open a text channel.
  - Type in the bottom input box and tap send. To reply to a specific message, long-press it and choose "Reply".
- **Search scope differences**:
  - The search bar at the top of a channel searches only the current channel (`in:#...`) or the current server by default.
  - To search for people or servers everywhere, use the search/explore entry in the bottom navigation.
- **Voice channels**: tapping a voice channel opens a preview panel; you only connect after tapping "Join Voice". A green call bar then stays at the top; tap the hang-up button at top right to disconnect.

## Known behaviour
- **Leftover state**: after closing and reopening, the app stays on the last channel. Before a new task, check the title at the top and the back button at top left, go back to the top level, then navigate.
- **No permission to post**: in read-only channels (such as announcements or rules) the input box is greyed out and says you do not have permission to send messages in this channel. Do not try to type there.
- **Slow mode**: a countdown bar appears below the input box; you cannot send another message until it reaches zero.
- **Rules screening on a new server**: when you first join a server, a banner often says you need to complete a few steps before talking. Tap "Complete" and agree to the rules to unlock typing.
- **Forums and threads**: in a forum channel, first tap a specific post to open it; only then can you reply inside the post.
- **Full-screen image/file picker**: when "+" opens the media picker, the back key usually closes the picker first.
