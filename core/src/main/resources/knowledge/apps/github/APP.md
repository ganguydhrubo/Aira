---
name: github
display: GitHub
packages: ["com.github.android"]
aliases: ["GitHub", "github"]
sites: ["github.com"]
open: GitHub
description: Code hosting and open-source collaboration: repositories, issues, pull requests and notifications
---
## What it is
GitHub is a code hosting and project collaboration app for developers, package `com.github.android`. For the website, see `github.com`.

## Screens
The bottom navigation has four tabs: "Home", "Notifications", "Explore" and "Profile".
- **Home**: shows "My Work" shortcuts: Issues, Pull Requests, Discussions, Repositories, Organizations, Starred. Below them are your Pinned repositories.
- **Repository page**: the top shows owner/repository name and the Branch switcher; below are the "Code", "Issues", "Pull Requests" and "Discussions" tabs.
- **Notifications**: grouped into Inbox, Saved and Done. Swipe to quickly mark a notification as Done or save it for later.
- **Pull request (PR) and issue details**: show the title, a status badge (Open, Merged, Closed), author and timestamp, the conversation timeline, Reviewers and Checks. A PR page also has "Files Changed" and "Commits".

## Features
- **Browse code and branches**:
  - Inside a repository, tap the branch drop-down to switch to a branch or tag. Tap folders in the file tree to go deeper, and tap a source file to read the rendered code or Markdown.
- **Handle and reply to issues/PRs**:
  - Type a reply in the comment box at the bottom of the issue or PR and tap "Comment".
  - Maintainers tap "Close issue" at the bottom to close an issue. On a PR that meets the merge rules with all CI checks green, tap "Merge pull request" and confirm.
- **Two-factor sign-in (2FA / GitHub Mobile)**:
  - When someone signs in to GitHub on a computer, the phone often shows a full-screen "Are you trying to sign in?" prompt with a two-digit number to match. Tap the number on the phone that matches the one on the computer to approve.

## Known behaviour
- **Leftover state**: after reopening, the app often stays on the last issue or a code diff view. Tap "←" at top left to go back step by step to the repository home.
- **Collapsed CI checks**: if a PR has many automated workflows, the Checks section shows only a summary (such as "13/14 passed"). Tap the row to open the next page and see which one failed.
- **Choosing a merge method**: when the repository allows several merge methods, tapping Merge pull request opens a menu with "Create a merge commit", "Squash and merge" and "Rebase and merge". Pick the method the task asks for.
