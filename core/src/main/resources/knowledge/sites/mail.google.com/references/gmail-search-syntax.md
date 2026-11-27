# Gmail search syntax

Type these in the search box at the top, or encode them and put them after `#search/` in the address. Separate several conditions with spaces; all of them must match.

| What you want | How to write it |
|---|---|
| Location | `in:inbox`, `in:sent`, `in:drafts`, `in:trash`; `in:anywhere` also searches Spam and Bin |
| Status | `is:unread`, `is:starred`, `is:important` |
| Labels and categories | `label:work`, `category:promotions`, `category:social` |
| From, to, cc | `from:alice@example.com`, `to:`, `cc:` |
| Subject | `subject:invoice` |
| Attachments | `has:attachment`; `filename:pdf` by attachment name or type |
| Size | `larger:5M`, `smaller:1M` |
| Relative time | `newer_than:7d`, `older_than:30d`; units d days, m months, y years |
| Exact dates | `after:2026/02/01`, `before:2026/03/01` |
| Either one | `from:alice OR from:bob` |
| Exclude | `-from:noreply@` |
| Grouping | `(from:alice OR from:bob) subject:meeting` |
| Exact phrase | `"exact phrase"` |

Example: unread mail from the last day, excluding the Promotions category: `is:unread newer_than:1d -category:promotions`
