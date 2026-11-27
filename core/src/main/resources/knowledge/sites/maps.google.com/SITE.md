---
name: maps.google.com
display: Google Maps
description: Look up place details and driving, transit and walking routes
domains: ["maps.google.com"]
aliases: ["Google Maps"]
apps: ["google-maps"]
open: maps.google.com
mode: desktop
---
## What it is
Google's web maps, for place details and routes; usually no sign-in needed. The real address is under `google.com/maps`, sharing the google.com domain with Google Flights and others.

## Pages and URLs
- Search: `https://www.google.com/maps/search/<encoded-query>`.
- Directions: `https://www.google.com/maps/dir/<encoded-start>/<encoded-destination>`.

## Features
- **Places**: name, address, rating and review count, open/closed status, full opening hours and phone number. Opening hours and phone number are only visible after expanding the details.
- **Directions**: open the directions panel, enter the start and destination, and switch travel mode (driving, transit, walking and so on). Each mode lists several options, each with time taken, distance, roads or transit lines used, and notes.

## Known behaviour
- A place name in the URL does not mean the page has selected that place. Many branches share a name, so check the name, address and city on the page.
- Pressing Enter straight in the start or destination box may pick your current location, a recent search or another place with the same name. Tapping the right item in the suggestion list is safer.
- After switching travel mode or opening another route card, the page content refreshes completely; data read before belongs to the previous state.
