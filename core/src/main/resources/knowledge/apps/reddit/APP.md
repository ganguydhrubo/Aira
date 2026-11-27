---
name: reddit
display: Reddit
packages: ["com.reddit.frontpage"]
aliases: ["Reddit", "reddit"]
open: Reddit
description: Community forums: subreddits, posts, votes, comment replies and messages
---
## What it is
Reddit is a global community forum and content app, package `com.reddit.frontpage`. Content is organised into communities (subreddits, prefix `r/`). Users build community reputation (Karma) by posting, upvoting and downvoting, replying to comments and following.

## Screens
The bottom navigation has five tabs: "Home", "Communities", "+" (create post), "Chat" and "Inbox".
- **Home**: at top left you can switch between "Home" (your follows), "Popular" and "Watch" (video feed); at the right are the search magnifier and your profile picture. Post cards show the community name (`r/...`), poster, time posted, title, text or image/video, Upvote/Downvote buttons, comment count and share.
- **Communities list**: shows "Recently visited", joined communities and "Custom Feeds". Inside a community, the top has its banner, description, member count and a "Join" button.
- **Post page**: tap a card to open it. The top shows the full post text or full-width media; below is the sort switch (Best, Top, New, Controversial), and the lower half is the nested comment tree. Each comment has a collapse line on its left and upvote/downvote, Reply and share below it.
- **Post editor**: opened with "+" in the bottom bar. At the top is the community picker "Select a community"; below it the post type bar (Text, Images, Video, Link, Poll); in the middle the title and body boxes; below that "Add tags and flair" plus NSFW and Spoiler switches; the "Next / Post" button is at top right.

## Features
- **Browse and vote (Upvote/Downvote)**:
  - Posts and comments both have up and down arrows. Tapping the up arrow (Upvote) turns it orange to agree; tapping the down arrow (Downvote) turns it purple to disagree; tapping a lit arrow again removes the vote.
- **Create a post, start to finish**:
  - Tap "+" in the bottom bar, then search for and pick the target community in "Select a community" at the top.
  - Enter the required "Title"; then type the body, add images or paste a link, depending on the type chosen.
  - **Add the required flair**: many communities require a flair. If "Next" or "Post" at top right is greyed out, first tap "Add tags and flair", pick one from the list that slides up (such as Discussion or News) and tap "Apply"; then the button becomes active.
  - Tap "Post" at top right to publish.
- **Comment on a post and reply in threads**:
  - **Comment on the post**: type in the "Add a comment" box that stays at the bottom of the post page, then tap the blue paper plane / arrow on the right.
  - **Reply to someone's comment**: tap the "Reply" button (with an arrow) under that comment, write in the reply box that opens and send it; it appears as a nested reply.
  - **Collapse a comment thread**: tap the comment text or long-press to fold that comment and all its replies into one line, so you can skip quickly to the next top-level comment.
- **How Karma (reputation points) is earned**:
  - Reddit Karma is split into Post Karma and Comment Karma.
  - New accounts build Karma best in large, easy-entry communities (such as `r/AskReddit`, `r/casualiama`, `r/aww`). Write helpful, relatable comments on active threads (sorted by Rising or Hot) to earn upvotes, or share good posts in communities that allow new users.
- **Search scope**:
  - Search from Home covers the whole site. Inside a community, the search bar shows the prefix `r/community-name` and searches only that community's past posts and comments.

## Known behaviour
- **Leftover state**: if you last left the app on a post or deep in a comment thread, reopening goes straight back there. Tap "←" at top left to return to the community or Home.
- **NSFW and spoiler covers**: sensitive or spoiler content is blurred by default and the card shows an "18+" or "Spoiler" label. Tap "Yes / View" in the middle to confirm and remove the blur.
- **New accounts and Karma limits**: strict communities (such as some trading, question or technical ones) filter by account age or minimum Karma. If you do not meet the rules, the AutoModerator bot removes the post automatically and sends a message to your Inbox explaining why.
- **Rating and notification prompts**: in-app cards such as "Rate Reddit" or "Turn on notifications" pop up often. Tap "Not now" or "No thanks" to close them.
