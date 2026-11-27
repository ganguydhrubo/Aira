---
name: info-research
display: Find out the facts
description: Get to the bottom of something on the web: find leads, read the original sources, cross-check, and state sources and dates
aliases: ["research", "look this up", "find out about", "help me understand"]
apps: ["reddit", "youtube", "x"]
sites: ["youtube.com"]
terms: ["research", "look up", "find out", "background", "compare", "is it reliable", "source", "latest"]
---
## Entry

First pin down what to look into: full name, region, time period, version or model. If there are same-name items or the wording is ambiguous, ask the user once, and while waiting for the answer, start on the parts that do not depend on it. For conditions the user did not give (such as a time range), choose a common set yourself and state at the start of the conclusion which set you used.

To begin, open a search page with `web_navigate` (Google, Bing), or use the in-app search of a relevant app.

## Steps

1. **Find leads**: first do two or three broad searches to see what is being said, which sites cover it, and whether there is an authoritative publisher. For in-app search use two to four keywords, not a full question.
2. **Find the original source**: for the exact fact the user asked about, trace it to where it was first published: the publisher's own website, an official announcement, the original document, the full paper, or the page of the product or service itself. Treat news and reposts only as leads, and follow the links or names they give to find the original.
3. **Add real experience and the downside**: for real user experience look at communities and video platforms (Reddit, Quora, X, YouTube, review sites); for problems look at complaint and review sites such as Trustpilot or consumer complaint forums. Do one search with negative words on purpose ("problem", "complaint", "drawbacks").
4. **Cross-check**: read each key conclusion in the original text of at least two unrelated sources. When they disagree, look at the publication dates and which side is closer to the event, and write down the disagreement itself.
5. **Write the conclusion**: after each conclusion, give the source (website or publisher) and the date of that content; keep what the source says separate from your own inference; for things you searched but did not find, say where you looked and what is still missing.

## Common pitfalls

- A search result snippet is not the original; it is often cut off or out of date. Open the page with `web_read_page` to read the body, and note which website you finally landed on.
- The same press release gets reposted by many sites; it looks like many sources but there is only one.
- Promotional articles, reviews with purchase links and anonymous leaks are less trustworthy; flag them when you use them.
- The date on a page may be the date of reposting or updating, not when the event happened.
- Finding nothing, a page that will not open, or being blocked by a sign-in page only means you could not read it this time; it does not mean "this never happened".

## How to verify it is done (evidence)

Every conclusion points back to a page actually read this time, with its source and date; key conclusions have two independent sources; weak or contradictory points have the reason explained.

## When to stop and hand over to the user

When the content needs a sign-in to view, or the next step becomes booking, buying, registering or anything else the user must decide or pay for, hand what you found to the user.
