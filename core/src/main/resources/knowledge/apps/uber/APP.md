---
name: uber
display: Uber
packages: ["com.ubercab"]
aliases: ["Uber", "uber"]
sites: []
open: Uber
description: Ride types, prices and trips
---
## What it is
Uber is a ride-hailing app, package `com.ubercab`.

## Screens
The bottom entries are Home, Services, Activity and Account. On Home, "Where to?" is where you enter the trip; entries such as Later are for booking a ride for a set time.

## Features
- **Fare estimate**: after entering the pickup point and destination you get a list of ride types, with the ride type name, the estimate in local currency, estimated arrival time, seats and surge pricing. Prices usually appear only after signing in.
- **Trips**: Activity shows past and current trips. For a booked ride you can see the driver, number plate and arrival time.

## Known behaviour
- The default pickup point is the device's GPS location, but it can be changed to an address in another city; the screen has a "Search another city" entry.
- The address box has a clear button X. After typing, choose the matching item from the suggestion list.
- Airport addresses ask follow-up questions such as "Which airline"; there is a Skip button.
- The text in the input box can change while you type, so read it again after typing.
