---
name: amazon.com
display: Amazon
domains: ["amazon.com", "amazon.sg", "amazon.co.uk", "amazon.de", "amazon.co.jp"]
aliases: ["Amazon"]
apps: ["amazon"]
open: amazon.com
mode: desktop
description: Global online shopping site to search products, see prices, variants and stock, and add to cart.
---
## What it is

A global online shopping site. Each country store (.com, .sg, .co.uk, .de, .co.jp) is a separate shop, with its own prices, stock, currency and delivery.

## Pages and URLs

- Search: on the target country store open `/s?k=<keyword>`; the keyword must be URL-encoded.
- Product page: use a clean link based on the ASIN. Cards on the search page may be rewritten by ads to redirect elsewhere.
- If no region is given, the site sends the visitor to the country store for their location.

## Features

- **Search**: public. Each result has an ASIN, title, price and currency, rating and review count; ad slots are marked.
- **Product page**: the buybox holds the main price, stock and seller terms; used and third-party seller offers are listed separately. For products with variants (colour, size and so on), the first price shown is only for the variant selected by default; choosing another variant changes the price and stock.
- **Add to cart**: the Add to Cart button on the product page; on success the page shows an "Added to Cart" message.

## Known behaviour

- Regional redirects can make the price missing, or show currencies such as SGD or GBP.
- After adding to cart, the cart badge number and button colour may update late, not at once.
- When the page layout changes or a bot check (CAPTCHA) appears, product details cannot be read.
