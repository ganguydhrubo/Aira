---
name: gmail
display: Gmail
packages: ["com.google.android.gm"]
aliases: ["Gmail", "Google Mail", "gmail"]
sites: ["mail.google.com"]
open: Gmail
description: Google's email app for sending and reading mail, sorted by category and label, plus meetings
---
## What it is
Gmail is Google's email app, package `com.google.android.gm`. Other email accounts can also be added and managed in it.

## Screens
Two tabs at the bottom: "Mail" and "Meet".
- **Mail**: at top left the menu button ("Open navigation drawer"), in the middle the search box "Search in mail", at right the profile picture ("Account and settings"). The list starts with the current inbox name (such as "Primary"), then summary rows for categories such as "Updates" and "Promotions" (with new mail counts), then the emails. At bottom right there is a "Compose" button.
- **Drawer**: inbox categories (Primary, Promotions, Social, Updates); under "All labels": Starred, Snoozed, Important, Sent, Scheduled, Outbox, Drafts, All mail, Spam, Bin (Trash), Manage subscriptions, Create new label, plus your own labels; under "Google apps": Calendar, Contacts. At the very bottom are "Settings" and "Help & feedback"; scroll down to see them.
- **Meet**: has "New meeting" and "Join with a code".

## Features
- **Mail list**: one row per email. The element name joins "Unread", sender, subject and preview together; the date and star are separate elements. Tapping the sender's picture selects the email; it does not open it.
- **Search**: tap the search box at the top. The search page has a back button, the input box and filter buttons: Label, From, To, Attachment.
- **Account**: tap the profile picture to open the account page: Manage your Google Account, switch account, Add another account, Manage accounts on this device, storage used.
- **Settings**: open from the very bottom of the drawer. The first level shows "General settings", one row per account, and "Add account".

## Known behaviour
- On first launch or after an update, these appear one after another: a "new look" intro page ("Got it"), a page for managing all your email addresses ("Go to Gmail"), the system notification permission prompt ("Don't allow", "Allow"), a card introducing Meet ("Learn more", "Got it"), and a tip card at the top of the list ("Dismiss").
- The system notification permission prompt has no readable elements and cannot be closed with the back key; tap the buttons on the screen.
- The first time you open the "Meet" tab, a prompt asks to allow Gmail to access phone state ("Cancel", "Grant permission"). It has no readable elements.
- The account page has no readable elements; you can only use the screenshot.
- When the drawer or search page is open, the mail list behind it is still in the element list, and the drawer items come after the emails.
- Pressing back on the mail list exits the app.
