---
name: documents
display: Work with PDF documents
description: Read the text of a PDF, take chosen pages, see document details, split or merge, then hand the file to the user
aliases: ["read a PDF", "PDF to text", "merge PDFs", "split a PDF"]
terms: ["PDF", "document", "extract text", "by page", "merge", "split", "scanned copy"]
---
## Entry

First find where the document is:
- A public link on the web: open it with `web_navigate`; if the built-in browser can show it directly, reading the page is enough.
- An attachment the user sent: in `/workspace/inbox/`.
- A file downloaded from a web page: in `/workspace/downloads/`; you can check the download history with `web_downloads`.

## Steps

1. For a text-based PDF, run `scripts/extract_pdf.py` with `run_bundled_script`, set `source` to `skills/documents`, and write file paths relative to `/workspace`:
   - Full text: `args: ["inbox/a.pdf"]`
   - Chosen pages (counting from 1; can be `3`, `1-5`, `1,4,7-9`): `args: ["inbox/a.pdf", "--pages", "1-5"]`
   - Title, author, page count and other details (JSON): `args: ["inbox/a.pdf", "--metadata"]`
   - For a long document, save it as text first: `args: ["inbox/a.pdf", "--out", "a.txt"]`, then read it in parts with `read_file`, or find keywords with `search_files`.
   - With a password: add `--password` and the password.
2. In the output each page starts with `--- page N ---`; use it for page numbers when quoting.
3. To split, merge or pull out pages, write a short piece of Python with `run_command` that uses pypdf; put new files under `/workspace`.
4. Hand finished files to the user with `share_file`.

## Common pitfalls

- If the script exits with code 3 and says there is no text to take, the PDF is made of scanned images; this device has no text recognition (OCR), so you can only look at page images or tell the user this. When only some pages have no text, the script reports those pages separately.
- Text taken from tables and multi-column layouts often comes out in a jumbled order; check against the original page before quoting numbers from a table.
- When a page range goes beyond the page count, the script stops with an error; check the page count first with `--metadata`.

## How to verify it is done (evidence)

The text you give can be matched to the original page in the script output, with the page number stated; for files handed over, `share_file` succeeded and the page count and content are as expected (you can run `--metadata` again on the new file to check the page count).

## When to stop and hand over to the user

When the document has a password the user has not given, or the user needs to decide which pages to keep or in what order to merge, ask the user first.
