---
name: mail.google.com
display: Gmail
description: Search, read, write and label mail in Gmail on the web
domains: ["mail.google.com"]
aliases: ["Gmail", "Google Mail"]
apps: ["gmail"]
open: mail.google.com
mode: desktop
---
## What it is
Google's web email; you must be signed in to a Google account.

## Pages and URLs
Entry `https://mail.google.com/`. Layout: the search box at the top; on the left Compose, then Inbox, Sent, Drafts, Spam, Bin and custom labels; the mail list in the middle. The interface language follows the account settings, so button names may not be in English. Add `#search/<encoded-query>` after the current account's address to search directly; the account number in the address (such as `u/0`) matches the signed-in account. For search syntax, see `references/gmail-search-syntax.md`.

## Features
- **List and search**: each row has the sender, subject, date and unread status, but no body. The list uses virtual scrolling: rows at the top are removed as you scroll down, so the number of rows you have scrolled past is not the total number of emails in the mailbox.
- **Email body**: open an email to see the sender, subject, body, time and attachments. Opening an email may change its unread status.
- **Write, reply, forward**: use Compose for a new email; to reply or forward, first open that email. When replying, the recipient and subject are filled in automatically. After sending, the email appears in Sent; the compose window closing does not prove it was sent.

## Known behaviour
Signing in to a Google account and "Verify it's you" are two different checks.
