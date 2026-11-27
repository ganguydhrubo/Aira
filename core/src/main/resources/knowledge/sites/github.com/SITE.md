---
name: github.com
display: GitHub
description: Code hosting and collaboration site to browse repositories, code, issues and pull requests
domains: ["github.com"]
aliases: ["GitHub", "github"]
apps: ["github"]
open: github.com
mode: desktop
---
## What it is
A code hosting and collaboration site. Repositories hold code, documentation (README), Issues and Pull requests.

## Pages and URLs
- Repository: `https://github.com/<owner>/<repo>`.
- A repository's issues and pull requests are at `/issues` and `/pulls` after the repository address.
- Search: `https://github.com/search?q=<encoded-term>&type=repositories`; you can also search code, issues and other types.

## Features
- **Repositories**: public repositories can usually be viewed without signing in (files, README, commits, branches, issues and pull requests); private repositories need an account with access.
- **Star**: the Star button at the top right of a repository page is for bookmarking and showing support, and needs sign-in. When the current account has already starred it, the button shows Starred, so the button state tells you whether it is starred.
- **Account**: what you can see and do after signing in depends on the account's permissions; button text changes with the interface language.

## Known behaviour
- If the repository does not exist, is private, or the current account has no access, the page may just show "page not found".
- Star counts may be shortened, such as `1.2k`.
