---
name: android-settings
display: Android Settings
packages: ["com.android.settings"]
aliases: ["Android Settings", "Settings", "Phone settings", "System settings"]
sites: []
open: Settings
description: Android system settings for network, display, permissions, accounts and system information
---
## What it is
The built-in Android Settings app, package `com.android.settings`.

## Screens
The home page is a list of categories with a search box at the top. Tapping a category opens sub-pages and detail pages, level by level. The names, categories and levels of settings differ by Android version and phone maker, and some makers add their own entries. The back button's text and look change from page to page; judge whether you are back on the home page from the page title and layout.

## Features
- **Search settings**: the search box at the top finds a setting directly by name.
- **Toggle settings**: Wi-Fi, Bluetooth, Aeroplane mode, Location and so on. A toggle's state is sometimes shown only by its colour and position; the UI tree may not contain clear "On/Off" text.
- **About phone**: model, Android version and so on. The same page may also show identifiers such as serial number, IMEI and device name.
- **Permissions, Developer options, Accounts, Security, System update, Install unknown apps, Reset**: changing these really changes the device's state.

## Known behaviour
Tapping a toggle switches it at once. If you only want to know the current state, read the accessibility attributes or look at a screenshot; do not tap it.
