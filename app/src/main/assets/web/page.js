/*
 * Page script: reads the element tree, reads the page and checks inputs.
 * Scripts written by the model do not run in the page: they run against this snapshot in the App's isolated JS engine, with no access to the page, network or storage.
 * The App injects it at document start where possible (before the page's own scripts), otherwise after load finishes. The whole file is one function expression;
 * the App calls it with a random name and secret: the page only keeps one bridge function that cannot be changed or deleted, and only the holder of the secret can get the API.
 * All state (reference tables, async results) lives in the closure; nothing is written to the DOM as attributes, and the App always pulls results itself.
 * All coordinates are CSS pixels of the top-level viewport.
 */
(function (NAME, SECRET) {
  'use strict';
  var W = window;
  var D = document;
  if (Object.prototype.hasOwnProperty.call(W, NAME)) return true;
  // Install only in the main document: child frames (including cross-origin ones) get no bridge function
  if (W.top !== W) return true;

  // Grab the original entry points at injection time (at document start the page scripts have not run yet), so later changes to globals by the page do not affect these
  var FP = Function.prototype;
  var uncurry = FP.bind.bind(FP.call);
  var I = {
    defineProperty: Object.defineProperty,
    hasOwn: uncurry(Object.prototype.hasOwnProperty),
    freeze: Object.freeze,
    stringify: JSON.stringify,
    setTimeout: W.setTimeout.bind(W),
    MutationObserver: W.MutationObserver,
    WeakRef: typeof WeakRef === 'function' ? WeakRef : null,
    parse: JSON.parse,
    String: String,
    lower: uncurry(String.prototype.toLowerCase),
    strSlice: uncurry(String.prototype.slice),
    strReplace: uncurry(String.prototype.replace),
    strTrim: uncurry(String.prototype.trim),
    strSplit: uncurry(String.prototype.split),
    test: uncurry(RegExp.prototype.test),
    charCodeAt: uncurry(String.prototype.charCodeAt),
    push: uncurry(Array.prototype.push),
    forEach: uncurry(Array.prototype.forEach),
    map: uncurry(Array.prototype.map),
    join: uncurry(Array.prototype.join),
    WeakSet: typeof WeakSet === 'function' ? WeakSet : null,
    weakAdd: typeof WeakSet === 'function' ? uncurry(WeakSet.prototype.add) : null,
    weakHas: typeof WeakSet === 'function' ? uncurry(WeakSet.prototype.has) : null,
    inputValue: uncurry(Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').get),
    inputType: uncurry(Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'type').get),
    querySelectorAll: uncurry(Element.prototype.querySelectorAll),
    docQuerySelectorAll: uncurry(Document.prototype.querySelectorAll),
    getAttribute: uncurry(Element.prototype.getAttribute),
    hasAttribute: uncurry(Element.prototype.hasAttribute),
    anchorHref: uncurry(Object.getOwnPropertyDescriptor(HTMLAnchorElement.prototype, 'href').get),
    addListener: uncurry(EventTarget.prototype.addEventListener),
    fetch: typeof W.fetch === 'function' ? W.fetch.bind(W) : null,
    FileReader: typeof FileReader === 'function' ? FileReader : null
  };

  var MAX_ELEMENTS = 400;
  var MAX_VISITED = 20000;
  var NAME_CHARS = 80;
  var PLACEHOLDER_CHARS = 40;
  var SHORTCUT_CHARS = 80;
  var MAX_LINKS = 50;
  var MAX_IMAGES = 20;
  var LAZY_SCREENS = 5;
  var LAZY_WAIT_MS = 250;
  var EVAL_VALUE_CHARS = 20000;

  var refs = [];
  // One reference table per screen read, keeping the latest few by snapshot key: screens the port reads for its own checks do not wipe out the references the model holds
  var MAX_SNAPSHOTS = 6;
  var snaps = {};
  var snapOrder = [];
  var DEFAULT_KEY = 'default';
  function keep(key, table) {
    if (!I.hasOwn(snaps, key)) I.push(snapOrder, key);
    snaps[key] = table;
    while (snapOrder.length > MAX_SNAPSHOTS) delete snaps[snapOrder.shift()];
  }
  var results = {};
  var mutations = 0;
  function observeMutations() {
    if (!D.documentElement) return false;
    try {
      new I.MutationObserver(function (records) { mutations += records.length; })
        .observe(D, { subtree: true, childList: true, attributes: true, characterData: true });
    } catch (e) { /* without MutationObserver the count stays 0 */ }
    return true;
  }
  // At document start documentElement may not exist yet; wait for it, then attach
  if (!observeMutations()) {
    new I.MutationObserver(function (r, o) { if (observeMutations()) o.disconnect(); }).observe(D, { childList: true });
  }

  // Remember every input that was ever type=password: if the page later switches it to type=text ("show password") it is still treated as a password box and its value is not read
  var seenPassword = I.WeakSet ? new I.WeakSet() : null;
  // An input whose autocomplete says password also counts (some pages use type=text with styling to mask the text)
  var PASSWORD_AUTOCOMPLETE = { 'current-password': 1, 'new-password': 1 };
  function autocompleteTokens(el) {
    var v = el.getAttribute && el.getAttribute('autocomplete');
    return v ? I.strSplit(I.lower(I.String(v)), /\s+/) : [];
  }
  function hasToken(el, set) {
    var t = autocompleteTokens(el);
    for (var i = 0; i < t.length; i++) if (set[t[i]]) return true;
    return false;
  }
  function markPassword(el) {
    if (!seenPassword || !el || el.tagName !== 'INPUT') return;
    if (I.lower(I.String(I.inputType(el))) === 'password' || hasToken(el, PASSWORD_AUTOCOMPLETE)) I.weakAdd(seenPassword, el);
  }
  function markPasswords(root) {
    if (!root) return;
    if (root.nodeType === 1) markPassword(root);
    if (!root.querySelectorAll) return;
    var list = root.nodeType === 9 ? I.docQuerySelectorAll(root, 'input') : I.querySelectorAll(root, 'input');
    for (var i = 0; i < list.length; i++) markPassword(list[i]);
  }
  // Main document is watched from document start; a same-origin frame's document is watched from the first time it is read
  var watchedDocs = I.WeakSet ? new I.WeakSet() : null;
  function watchDoc(doc) {
    if (!doc || (watchedDocs && I.weakHas(watchedDocs, doc))) return;
    if (watchedDocs) I.weakAdd(watchedDocs, doc);
    try {
      new I.MutationObserver(function (records) {
        for (var i = 0; i < records.length; i++) {
          var r = records[i];
          if (r.type === 'attributes') markPassword(r.target);
          else for (var j = 0; j < r.addedNodes.length; j++) markPasswords(r.addedNodes[j]);
        }
      }).observe(doc, { subtree: true, childList: true, attributes: true, attributeFilter: ['type', 'autocomplete'] });
    } catch (e) { /* without MutationObserver, rely only on marking during each screen read */ }
    markPasswords(doc);
  }
  watchDoc(D);

  var weak = I.WeakRef
    ? function (o) { return new I.WeakRef(o); }
    : function (o) { return { deref: function () { return o; } }; };

  // ---------- General ----------

  function collapse(s) { return I.strTrim(I.strReplace(I.String(s == null ? '' : s), /[\s​‌‍﻿]+/g, ' ')); }
  function cut(s, n) { return s.length > n ? I.strSlice(s, 0, n) : s; }
  function lower(el) { return el.tagName ? I.lower(I.String(el.tagName)) : ''; }
  function styleOf(el) {
    var v = el.ownerDocument && el.ownerDocument.defaultView;
    return v ? v.getComputedStyle(el) : null;
  }
  // Parent node, crossing shadow boundaries
  function parentOf(n) {
    if (n.parentNode && n.parentNode.nodeType === 11 && n.parentNode.host) return n.parentNode.host;
    return n.parentElement || (n.parentNode && n.parentNode.host) || null;
  }
  function composedContains(outer, inner) {
    for (var n = inner; n; n = parentOf(n)) if (n === outer) return true;
    return false;
  }
  function deepHit(doc, x, y) {
    var hit = doc.elementFromPoint(x, y);
    while (hit && hit.shadowRoot) {
      var inner = hit.shadowRoot.elementFromPoint(x, y);
      if (!inner || inner === hit) break;
      hit = inner;
    }
    return hit;
  }
  function intersect(a, b) {
    var l = Math.max(a.l, b.l), t = Math.max(a.t, b.t), r = Math.min(a.r, b.r), bt = Math.min(a.b, b.b);
    return r > l && bt > t ? { l: l, t: t, r: r, b: bt } : null;
  }
  function viewport() { return { l: 0, t: 0, r: W.innerWidth, b: W.innerHeight }; }

  var TEXT_INPUTS = { text: 1, search: 1, email: 1, url: 1, tel: 1, number: 1, password: 1, date: 1, 'datetime-local': 1, month: 1, time: 1, week: 1 };
  var BUTTON_INPUTS = { button: 1, submit: 1, reset: 1, image: 1 };
  var CLICK_ROLES = { link: 1, button: 1, file: 1, checkbox: 1, radio: 1, combobox: 1, 'switch': 1, tab: 1, menuitem: 1, option: 1, slider: 1, menuitemcheckbox: 1, menuitemradio: 1, treeitem: 1 };
  var SKIP_TAGS = { script: 1, style: 1, noscript: 1, template: 1, head: 1, meta: 1, link: 1, title: 1 };
  var INLINE_TAGS = { b: 1, i: 1, em: 1, strong: 1, span: 1, small: 1, sup: 1, sub: 1, code: 1, mark: 1, u: 1, s: 1, br: 1, abbr: 1, time: 1, font: 1, q: 1, cite: 1, kbd: 1, wbr: 1 };

  function inputType(el) {
    var t = el.tagName === 'INPUT' ? I.inputType(el) : el.type;
    return I.lower(I.String(t || 'text'));
  }
  function isPassword(el) {
    if (lower(el) !== 'input') return false;
    if (inputType(el) === 'password' || hasToken(el, PASSWORD_AUTOCOMPLETE)) { markPassword(el); return true; }
    return !!(seenPassword && I.weakHas(seenPassword, el));
  }
  // Inputs whose screen read reports only how many characters are filled, never the value: password boxes, and text inputs and textareas marked as card number, security code, expiry date or one-time code
  function hidesValue(el) {
    return isPassword(el) || (isTextField(el) && hasToken(el, SECRET_AUTOCOMPLETE));
  }
  // Input values use the original getter taken at document start: a page that later overrides the value getter cannot fool this
  function fieldText(el) { return String((lower(el) === 'input' ? I.inputValue(el) : el.value) || ''); }
  function isTextField(el) {
    var tag = lower(el);
    if (tag === 'textarea') return true;
    return tag === 'input' && !!TEXT_INPUTS[inputType(el)];
  }
  // The text inside a value-hidden textarea is its value (prefilled by the page, or typed text written back by page scripts), so skip it when taking an element's text.
  // If the element has no such textarea, behave as before: innerText, falling back to textContent; when plain is true use textContent directly
  function secretArea(el) { return lower(el) === 'textarea' && hidesValue(el); }
  function textOf(el, plain) {
    var ts = secretArea(el) ? [el] : I.querySelectorAll(el, 'textarea[autocomplete]'), hit = false;
    for (var i = 0; i < ts.length && !hit; i++) hit = secretArea(ts[i]);
    if (hit) return textSkipping(el);
    return plain ? el.textContent : el.innerText || el.textContent;
  }
  function textSkipping(node) {
    if (node.nodeType === 3) return node.nodeValue;
    if (node.nodeType !== 1 || secretArea(node)) return '';
    var s = '';
    for (var c = node.firstChild; c; c = c.nextSibling) s += textSkipping(c);
    return s;
  }
  function editingHost(el) {
    if (!el || !el.isContentEditable) return null;
    var h = el;
    while (parentOf(h) && parentOf(h).isContentEditable) h = parentOf(h);
    return h;
  }
  function hasDirectText(el) {
    for (var c = el.firstChild; c; c = c.nextSibling) if (c.nodeType === 3 && collapse(c.nodeValue)) return true;
    return false;
  }
  function onlyInlineChildren(el) {
    for (var c = el.firstElementChild; c; c = c.nextElementSibling) if (!INLINE_TAGS[lower(c)]) return false;
    return true;
  }
  function directText(el) {
    var s = '';
    for (var c = el.firstChild; c; c = c.nextSibling) if (c.nodeType === 3) s += c.nodeValue;
    return collapse(s);
  }

  // Role and the three flags; null when the element is not a candidate
  function classify(el, st) {
    var tag = lower(el);
    var role = null;
    var explicit = el.getAttribute && el.getAttribute('role');
    if (explicit) role = I.strSplit(collapse(explicit), ' ')[0] || null;
    if (!role) {
      if (tag === 'a' && el.hasAttribute('href')) role = 'link';
      else if (tag === 'button') role = 'button';
      else if (tag === 'input') {
        var t = inputType(el);
        if (t === 'hidden') return null;
        if (t === 'checkbox' || t === 'radio' || t === 'file') role = t;
        else if (BUTTON_INPUTS[t]) role = 'button';
        else if (t === 'range') role = 'slider';
        else role = 'textbox';
      } else if (tag === 'textarea') role = 'textbox';
      else if (tag === 'select') role = 'combobox';
      else if (tag === 'img') role = 'image';
      else if (I.test(/^h[1-6]$/, tag)) role = 'heading';
      else if (el.isContentEditable && editingHost(el) === el) role = 'textbox';
      else if (el.hasAttribute('onclick') || (el.hasAttribute('tabindex') && el.tabIndex >= 0)) role = 'button';
      else if (st.cursor === 'pointer' && !(parentOf(el) && styleOf(parentOf(el)) && styleOf(parentOf(el)).cursor === 'pointer')) role = 'button';
    }
    var scrollable = isScrollable(el, st);
    if (!role && scrollable) role = (tag === 'ul' || tag === 'ol') ? 'list' : 'group';
    if (!role) return null;
    var editable = role === 'textbox' && !el.disabled && !el.readOnly;
    var clickable = !!CLICK_ROLES[role] && !el.disabled;
    return { role: role, c: clickable, e: editable, s: scrollable };
  }
  function isScrollable(el, st) {
    if (el === D.scrollingElement || el === D.body || el === D.documentElement) return false;
    var oy = st.overflowY, ox = st.overflowX;
    return ((oy === 'auto' || oy === 'scroll') && el.scrollHeight > el.clientHeight + 1) ||
      ((ox === 'auto' || ox === 'scroll') && el.scrollWidth > el.clientWidth + 1);
  }

  function textOfIds(el, ids) {
    var root = el.getRootNode ? el.getRootNode() : D;
    var out = [];
    I.forEach(I.strSplit(collapse(ids), ' '), function (id) {
      var t = root.getElementById ? root.getElementById(id) : el.ownerDocument.getElementById(id);
      if (t) I.push(out, collapse(textOf(t)));
    });
    return collapse(I.join(out, ' '));
  }
  // Accessible name; textbox/combobox do not use their own text as the name (that is the value)
  function labelOf(el, role) {
    var v;
    if ((v = el.getAttribute('aria-labelledby')) && (v = textOfIds(el, v))) return v;
    if ((v = collapse(el.getAttribute('aria-label')))) return v;
    if (el.labels && el.labels.length) {
      var ls = [];
      for (var i = 0; i < el.labels.length; i++) I.push(ls, collapse(textOf(el.labels[i])));
      if ((v = collapse(I.join(ls, ' ')))) return v;
    }
    if ((v = collapse(el.getAttribute('alt')))) return v;
    if ((v = collapse(el.getAttribute('title')))) return v;
    if ((v = collapse(el.getAttribute('placeholder')))) return v;
    if (role === 'textbox' || role === 'combobox' || hidesValue(el)) return '';
    var tag = lower(el);
    if (tag === 'input') return collapse(I.inputValue(el));
    return collapse(textOf(el));
  }
  function valueOf(el, role) {
    if (role === 'combobox' && lower(el) === 'select') {
      var o = el.options && el.options[el.selectedIndex];
      return o ? { text: collapse(o.text) } : { text: '' };
    }
    if (hidesValue(el)) return { password: fieldText(el).length };
    if (role !== 'textbox') return null;
    if (isTextField(el)) return { text: fieldText(el) };
    if (el.isContentEditable) return { text: collapse(el.innerText) };
    return null;
  }
  function nameOf(el, role) {
    var label = cut(labelOf(el, role), NAME_CHARS);
    var v = valueOf(el, role);
    if (!v) return label;
    var shown = v.password != null ? (v.password > 0 ? '已填 ' + v.password + ' 位' : '') : cut(collapse(v.text), NAME_CHARS);
    if (!shown) return label;
    if (!label || label === shown) return shown;
    return label + '：' + shown;
  }

  function hostOf(src) {
    try { return new URL(src, D.baseURI).hostname; } catch (e) { return ''; }
  }

  // ---------- Element tree ----------

  // When key is null, read only and do not register references (for checks and peeking)
  function tree(key) {
    refs = [];
    var els = [];
    var state = { trunc: false, focused: deepActive(), visited: 0 };
    var top = { dx: 0, dy: 0, clip: viewport(), frames: [], transparent: false, named: false };
    if (D.body) visitChildren(D.body, top, els, state);
    if (key !== null) keep(key === undefined ? DEFAULT_KEY : String(key), refs);
    return {
      els: els, vw: W.innerWidth, vh: W.innerHeight, dpr: W.devicePixelRatio || 1,
      trunc: state.trunc, mut: mutations
    };
  }

  function visitChildren(el, ctx, out, state) {
    if (el.shadowRoot) {
      for (var s = el.shadowRoot.firstElementChild; s; s = s.nextElementSibling) visit(s, ctx, out, state);
    }
    for (var c = el.firstElementChild; c; c = c.nextElementSibling) visit(c, ctx, out, state);
  }

  function visibleRect(el, ctx) {
    var r = el.getBoundingClientRect();
    if (r.width <= 0 || r.height <= 0) return null;
    return intersect({ l: r.left + ctx.dx, t: r.top + ctx.dy, r: r.right + ctx.dx, b: r.bottom + ctx.dy }, ctx.clip);
  }

  function covered(el, ctx, rect) {
    var x = (rect.l + rect.r) / 2 - ctx.dx;
    var y = (rect.t + rect.b) / 2 - ctx.dy;
    var hit = deepHit(el.ownerDocument, x, y);
    if (!hit) return true;
    return !(composedContains(el, hit) || composedContains(hit, el));
  }

  function push(out, state, el, ctx, rect, role, name, flags) {
    if (out.length >= MAX_ELEMENTS) { state.trunc = true; return false; }
    var item = {
      r: role, n: name,
      x: Math.round(rect.l), y: Math.round(rect.t),
      w: Math.round(rect.r - rect.l), h: Math.round(rect.b - rect.t),
      c: flags.c ? 1 : 0, e: flags.e ? 1 : 0, s: flags.s ? 1 : 0
    };
    if (el.id) item.id = el.id;
    if (el === state.focused) item.f = 1;
    // Password, verification code and bank card style inputs: every typing into them needs the user's approval
    if (flags.e && (isPassword(el) || hasToken(el, SECRET_AUTOCOMPLETE))) item.pw = 1;
    var ph = collapse(el.getAttribute && el.getAttribute('placeholder'));
    if (ph) item.ph = cut(ph, PLACEHOLDER_CHARS);
    // A search box by the element's own semantics: Enter means search, not send
    if (flags.e && isSearchField(el)) item.sr = 1;
    // Shortcut keys declared by the page: the confirmation gate uses them to see which control a key press acts on
    var ks = collapse(el.getAttribute && el.getAttribute('aria-keyshortcuts'));
    var ak = collapse(el.getAttribute && el.getAttribute('accesskey'));
    if (ak && ak.length === 1) ks = ks ? ks + ' Alt+' + ak : 'Alt+' + ak;
    if (ks) item.k = cut(ks, SHORTCUT_CHARS);
    I.push(out, item);
    I.push(refs, { el: weak(el), frames: I.map(ctx.frames, weak) });
    return true;
  }

  /** Inputs with type=search, role=searchbox, Enter key marked as search, or inside a role=search region. */
  function isSearchField(el) {
    if (!el.getAttribute) return false;
    if (inputType(el) === 'search') return true;
    var role = collapse(el.getAttribute('role'));
    if (role === 'searchbox') return true;
    if (collapse(el.getAttribute('enterkeyhint')) === 'search' || collapse(el.getAttribute('inputmode')) === 'search') return true;
    for (var p = parentOf(el), n = 0; p && n < 8; p = parentOf(p), n++) {
      if (p.getAttribute && collapse(p.getAttribute('role')) === 'search') return true;
      if (lower(p) === 'search') return true;
    }
    return false;
  }

  function visit(el, ctx, out, state) {
    if (state.trunc) return;
    // Huge pages: also cap the number of visited nodes so a screen read does not freeze the page
    if (++state.visited > MAX_VISITED) { state.trunc = true; return; }
    var tag = lower(el);
    if (SKIP_TAGS[tag]) return;
    var st = styleOf(el);
    if (!st || st.display === 'none') return;
    var transparent = ctx.transparent || parseFloat(st.opacity) === 0;
    var shown = !transparent && st.visibility === 'visible';

    if (tag === 'iframe' || tag === 'frame') { visitFrame(el, ctx, out, state, shown); return; }

    var info = classify(el, st);
    var named = ctx.named;
    if (shown) {
      var role = null, name = '', flags = null;
      if (info) {
        role = info.role; flags = info; name = nameOf(el, role);
      } else if (!ctx.named && hasDirectText(el)) {
        role = 'text'; flags = { c: false, e: false, s: false };
        name = cut(onlyInlineChildren(el) ? collapse(el.innerText) : directText(el), NAME_CHARS);
      }
      if (role) {
        var rect = visibleRect(el, ctx);
        if (rect && !covered(el, ctx, rect)) {
          if (!push(out, state, el, ctx, rect, role, name, flags)) return;
        }
        // When an element already uses its own text as its name, do not list its descendants' plain text again
        if (role === 'text' ? onlyInlineChildren(el) : (role !== 'group' && role !== 'list')) named = true;
      }
    }
    var child = named === ctx.named && transparent === ctx.transparent ? ctx : {
      dx: ctx.dx, dy: ctx.dy, clip: ctx.clip, frames: ctx.frames, transparent: transparent, named: named
    };
    visitChildren(el, child, out, state);
  }

  function frameDoc(el) {
    var doc = null;
    try { doc = el.contentDocument; } catch (e) { return null; }
    if (doc) watchDoc(doc);
    return doc;
  }

  function visitFrame(el, ctx, out, state, shown) {
    var doc = frameDoc(el);
    var rect = shown ? visibleRect(el, ctx) : null;
    if (doc && doc.documentElement) {
      if (!rect) return;
      var r = el.getBoundingClientRect();
      var st = styleOf(el);
      var ox = ctx.dx + r.left + el.clientLeft + (parseFloat(st.paddingLeft) || 0);
      var oy = ctx.dy + r.top + el.clientTop + (parseFloat(st.paddingTop) || 0);
      var inner = { dx: ox, dy: oy, clip: rect, frames: ctx.frames.concat([el]), transparent: false, named: false };
      if (doc.body) visitChildren(doc.body, inner, out, state);
      return;
    }
    // Cross-origin frames cannot be read under the same-origin rule; only list that they exist
    if (rect && !covered(el, ctx, rect)) {
      push(out, state, el, ctx, rect, 'frame', '内嵌页面:' + hostOf(el.src || el.getAttribute('src') || ''), { c: false, e: false, s: false });
    }
  }

  // ---------- Get element by reference ----------

  function entry(ref, key) {
    var m = /^@e(\d+)$/.exec(String(ref));
    if (!m) return null;
    var table = snaps[key == null ? DEFAULT_KEY : String(key)];
    var e = table && table[parseInt(m[1], 10) - 1];
    if (!e) return null;
    var el = e.el.deref();
    if (!el || !el.isConnected) return null;
    var frames = [];
    for (var i = 0; i < e.frames.length; i++) {
      var f = e.frames[i].deref();
      if (!f || !f.isConnected) return null;
      I.push(frames, f);
    }
    return { el: el, frames: frames };
  }
  function offsetOf(frames) {
    var dx = 0, dy = 0;
    for (var i = 0; i < frames.length; i++) {
      var f = frames[i], r = f.getBoundingClientRect(), st = styleOf(f);
      dx += r.left + f.clientLeft + (parseFloat(st.paddingLeft) || 0);
      dy += r.top + f.clientTop + (parseFloat(st.paddingTop) || 0);
    }
    return { dx: dx, dy: dy };
  }
  function topRect(e) {
    var o = offsetOf(e.frames);
    var r = e.el.getBoundingClientRect();
    if (r.width <= 0 && r.height <= 0) return null;
    return { full: { l: r.left + o.dx, t: r.top + o.dy, r: r.right + o.dx, b: r.bottom + o.dy }, o: o };
  }

  // The elements of a reference table themselves: only for the App to turn into node IDs via DevTools (element objects are not serialised by evaluateJavascript).
  // When key is null, the table from the latest screen read; entries no longer in the document are null
  function refNodes(key) {
    var table = key == null ? refs : snaps[String(key)];
    if (!table) return null;
    return I.map(table, function (e) { var el = e.el.deref(); return el && el.isConnected ? el : null; });
  }

  function centerOf(ref, key) {
    var e = entry(ref, key);
    if (!e) return null;
    try { e.el.scrollIntoView({ block: 'center', inline: 'center', behavior: 'instant' }); } catch (x) { e.el.scrollIntoView(); }
    var t = topRect(e);
    if (!t) return null;
    var v = intersect(t.full, viewport()) || t.full;
    return [Math.round((v.l + v.r) / 2), Math.round((v.t + v.b) / 2)];
  }

  function describe(el) {
    for (var n = el; n && n.nodeType === 1; n = parentOf(n)) {
      var st = styleOf(n);
      var info = st ? classify(n, st) : null;
      var name = cut(labelOf(n, info ? info.role : null), 40);
      if (name) return name;
    }
    return lower(el);
  }

  function hitTest(ref, key) {
    var e = entry(ref, key);
    if (!e) return { k: 'gone' };
    var t = topRect(e);
    var v = t && intersect(t.full, viewport());
    if (!v) return { k: 'offscreen' };
    var x = (v.l + v.r) / 2, y = (v.t + v.b) / 2;
    var hit = deepHit(e.el.ownerDocument, x - t.o.dx, y - t.o.dy);
    if (hit && (composedContains(e.el, hit) || composedContains(hit, e.el))) {
      return { k: 'hit', x: Math.round(x), y: Math.round(y) };
    }
    if (!hit) return { k: 'obscured', by: '' };
    // Name the whole covering element (popup, banner), not the child that happens to be at the point: walk up to the outermost ancestor that does not contain the target
    var cover = hit;
    while (parentOf(cover) && !composedContains(parentOf(cover), e.el)) cover = parentOf(cover);
    return { k: 'obscured', by: describe(cover) };
  }

  // ---------- Input values ----------

  function deepActive() {
    var a = D.activeElement;
    for (var i = 0; a && i < 20; i++) {
      if (a.shadowRoot && a.shadowRoot.activeElement) { a = a.shadowRoot.activeElement; continue; }
      var tag = lower(a);
      if (tag === 'iframe' || tag === 'frame') {
        var doc = frameDoc(a);
        if (doc && doc.activeElement && doc.activeElement !== doc.body) { a = doc.activeElement; continue; }
      }
      break;
    }
    return a;
  }

  function fieldValue(ref, key) {
    var el;
    if (ref == null) el = deepActive();
    else { var e = entry(ref, key); el = e && e.el; }
    if (!el) return { k: 'none' };
    if (isPassword(el)) return { k: 'password', len: String(I.inputValue(el) || '').length };
    if (isTextField(el)) return { k: 'text', v: fieldText(el) };
    var host = editingHost(el);
    if (host) return { k: 'text', v: host.innerText };
    return { k: 'none' };
  }

  // HTTP status of the main document response (where the browser supports it); null if not available
  function navStatus() {
    try {
      var e = W.performance.getEntriesByType('navigation')[0];
      return e && typeof e.responseStatus === 'number' && e.responseStatus > 0 ? e.responseStatus : null;
    } catch (x) { return null; }
  }

  // Whether focus is on this element (or inside its editable area): checked before typing so text does not land elsewhere
  function isActive(ref, key) {
    var e = entry(ref, key);
    if (!e) return false;
    var a = deepActive();
    if (!a) return false;
    if (a === e.el) return true;
    var host = editingHost(a);
    return !!host && (host === e.el || composedContains(e.el, host) || composedContains(host, e.el));
  }

  function scrollState(ref, key) {
    if (ref == null) return [Math.round(W.scrollX), Math.round(W.scrollY)];
    var e = entry(ref, key);
    if (!e) return null;
    return [Math.round(e.el.scrollLeft), Math.round(e.el.scrollTop)];
  }

  // ---------- Facts for detecting blockers ----------

  // Whether the page has a password box (including same-origin frames), and the full URLs of cross-origin frames (CAPTCHAs often live there)
  function facts() {
    var password = false, frames = [];
    function scan(doc, depth) {
      if (!doc || depth > 5) return;
      var inputs = doc.querySelectorAll('input');
      for (var i = 0; i < inputs.length && !password; i++) if (isPassword(inputs[i])) password = true;
      var fs = doc.querySelectorAll('iframe, frame');
      for (var j = 0; j < fs.length; j++) {
        var inner = frameDoc(fs[j]);
        if (inner && inner.documentElement) scan(inner, depth + 1);
        else {
          var src = fs[j].src || fs[j].getAttribute('src') || '';
          if (src) { try { I.push(frames, new URL(src, doc.baseURI).href); } catch (e) { /* skip bad URLs */ } }
        }
      }
    }
    scan(D, 0);
    return { password: password, frames: frames };
  }

  // ---------- Read whole page ----------

  var READ_SKIP = { script: 1, style: 1, noscript: 1, template: 1, head: 1, svg: 1, canvas: 1, iframe: 1, frame: 1, object: 1, embed: 1 };
  var CHROME_TAGS = { nav: 1, footer: 1, aside: 1 };
  var CHROME_ROLES = { navigation: 1, contentinfo: 1, complementary: 1 };

  function isChrome(el) {
    return !!CHROME_TAGS[lower(el)] || !!CHROME_ROLES[collapse(el.getAttribute && el.getAttribute('role'))];
  }

  function contentRoot() {
    var main = D.querySelector('main, [role=main]') || D.querySelector('article');
    return main ? { el: main, chrome: false } : { el: D.body || D.documentElement, chrome: true };
  }

  function extract(maxChars, offset) {
    var root = contentRoot();
    var lines = [];
    var line = '';
    function flush() {
      var t = collapse(line);
      if (t) I.push(lines, t);
      line = '';
    }
    function walk(node) {
      if (node.nodeType === 3) { line += node.nodeValue; return; }
      if (node.nodeType !== 1) return;
      var tag = lower(node);
      if (READ_SKIP[tag] || secretArea(node)) return;
      if (root.chrome && isChrome(node)) return;
      var st = styleOf(node);
      if (!st || st.display === 'none' || st.visibility === 'hidden') return;
      var h = /^h([1-6])$/.exec(tag);
      if (h) {
        flush();
        var t = collapse(textOf(node));
        if (t) I.push(lines, I.join(new Array(parseInt(h[1], 10) + 1), '#') + ' ' + t);
        return;
      }
      if (tag === 'br') { flush(); return; }
      var block = st.display.indexOf('inline') !== 0 && st.display !== 'contents';
      if (block) flush();
      if (tag === 'slot' && node.assignedNodes) {
        var assigned = node.assignedNodes({ flatten: true });
        if (assigned.length) { I.forEach(assigned, walk); if (block) flush(); return; }
      }
      var kids = node.shadowRoot ? node.shadowRoot.childNodes : node.childNodes;
      for (var i = 0; i < kids.length; i++) walk(kids[i]);
      if (block) flush();
    }
    walk(root.el);
    flush();
    var text = I.join(lines, '\n');
    var start = Math.max(0, Math.min(offset, text.length));
    var end = Math.min(text.length, start + maxChars);

    var links = [], seen = {};
    var as = root.el.querySelectorAll('a[href]');
    for (var i = 0; i < as.length && links.length < MAX_LINKS; i++) {
      var a = as[i];
      if (root.chrome && a.closest('nav, footer, aside, [role=navigation], [role=contentinfo], [role=complementary]')) continue;
      var url = a.href;
      if (!url || I.test(/^javascript:/i, url) || seen[url]) continue;
      seen[url] = 1;
      I.push(links, { text: cut(collapse(a.innerText || a.getAttribute('aria-label') || a.title), NAME_CHARS), url: url });
    }
    var images = [];
    var imgs = root.el.querySelectorAll('img');
    for (var j = 0; j < imgs.length && images.length < MAX_IMAGES; j++) {
      var src = imgs[j].currentSrc || imgs[j].src;
      if (!src || I.test(/^data:/i, src)) continue;
      I.push(images, { alt: cut(collapse(imgs[j].alt), NAME_CHARS), url: src });
    }
    return {
      url: I.String(W.location.href), title: D.title, text: I.strSlice(text, start, end),
      links: links, images: images, truncated: end < text.length, nextOffset: end < text.length ? end : null
    };
  }

  function scrollToY(x, y) {
    try { W.scrollTo({ left: x, top: y, behavior: 'instant' }); } catch (e) { W.scrollTo(x, y); }
  }

  // First scroll down at most 5 screens to trigger lazy loading (only when reading the first page and scroll is true), then restore; reads made for checks do not scroll
  function startRead(token, maxChars, offset, scroll) {
    var sx = W.scrollX, sy = W.scrollY, n = 0;
    function finish() {
      var res;
      try { res = extract(maxChars, offset); } catch (e) { res = { error: String(e && e.message || e) }; }
      scrollToY(sx, sy);
      results[token] = res;
    }
    function step() {
      var bottom = (D.scrollingElement || D.documentElement).scrollHeight;
      if (n < LAZY_SCREENS && W.scrollY + W.innerHeight < bottom - 1) {
        n++;
        scrollToY(sx, W.scrollY + W.innerHeight);
        I.setTimeout(step, LAZY_WAIT_MS);
      } else {
        finish();
      }
    }
    if (offset > 0 || scroll === false) finish(); else step();
    return true;
  }

  // ---------- Full page read (web_read_page): sections, foldable list blocks, quality signals ----------
  //
  // This only collects by structure: landmark roles, share of link text, repeated child structures, password boxes, area covered by images/canvases, dates declared by the page.
  // Which blocks fold, and whether the page counts as a sign-in page or blocked, is decided by the App.

  var FOLD_TAGS = { nav: 'navigation', footer: 'footer', aside: 'complementary' };
  var FOLD_ROLES = { navigation: 'navigation', contentinfo: 'footer', complementary: 'complementary', menubar: 'navigation' };
  // The App passes the candidate block thresholds when calling (the same values it judges with); these are only the fallback when the App gives none
  var foldMinLinks = 5;
  var foldLinkShare = 0.5;
  var DIGEST_MAX_CHARS = 400000;
  var DIGEST_MAX_LINKS = 400;
  var MAX_DIALOGS = 3;
  var MAX_PROMINENT = 40;
  var MAX_DATES = 30;
  var PICTURE_SCREENS = 2;
  var MAX_PICTURES = 300;
  var DIALOG_SELECTOR = 'dialog[open], [role=dialog], [role=alertdialog], [aria-modal=true]';
  var DATE_META = /^(article:published_time|article:modified_time|og:published_time|og:updated_time|pubdate|publishdate|publish_date|date|dc\.date|dc\.date\.issued|dcterms\.created|dcterms\.issued|dcterms\.modified|last-modified|lastmod|revised)$/i;

  function shownBox(el) {
    var st = styleOf(el);
    if (!st || st.display === 'none' || st.visibility === 'hidden') return false;
    var r = el.getBoundingClientRect();
    return r.width > 0 && r.height > 0;
  }

  function linkStats(el) {
    var as = el.querySelectorAll('a[href]');
    var n = 0, c = 0;
    for (var i = 0; i < as.length; i++) {
      var t = collapse(as[i].textContent);
      if (t) { n++; c += t.length; }
    }
    return { n: n, c: c };
  }

  // How many times the most repeated child structure (tag + class) occurs, counting only children with links: cards, recommendations, list items
  function repeatOf(el) {
    var counts = {}, best = 0, kids = el.children;
    for (var i = 0; i < kids.length; i++) {
      var k = kids[i];
      if (!(lower(k) === 'a' || k.querySelector('a[href]'))) continue;
      var sig = lower(k) + '.' + (typeof k.className === 'string' ? k.className : '');
      counts[sig] = (counts[sig] || 0) + 1;
      if (counts[sig] > best) best = counts[sig];
    }
    return best;
  }

  function groupLabel(el) {
    var l = collapse(el.getAttribute('aria-label'));
    if (!l && el.getAttribute('aria-labelledby')) l = textOfIds(el, el.getAttribute('aria-labelledby'));
    if (!l) { var h = el.querySelector('h1, h2, h3, h4, h5, h6'); if (h) l = collapse(textOf(h, true)); }
    return cut(l || '', NAME_CHARS);
  }

  // Blocks that may be folded: landmarks, or lists where links make up most of the text; the App decides whether to fold
  function foldCandidate(el) {
    var kind = FOLD_TAGS[lower(el)] || FOLD_ROLES[collapse(el.getAttribute('role'))];
    var listy = lower(el) === 'ul' || lower(el) === 'ol' || el.children.length >= foldMinLinks;
    if (!kind && !listy) return null;
    var stats = linkStats(el);
    var chars = collapse(el.textContent).length;
    if (!kind && (stats.n < foldMinLinks || stats.c < chars * foldLinkShare)) return null;
    return { g: kind || 'links', label: groupLabel(el), lines: [], chars: chars, linkChars: stats.c, repeat: repeatOf(el), links: stats.n };
  }

  function openDialogs() {
    var out = [];
    var ds = I.docQuerySelectorAll(D, DIALOG_SELECTOR);
    for (var i = 0; i < ds.length && out.length < MAX_DIALOGS; i++) {
      var d = ds[i], inner = false;
      for (var j = 0; j < out.length; j++) if (out[j].contains(d)) inner = true;
      if (!inner && shownBox(d) && collapse(d.textContent)) I.push(out, d);
    }
    return out;
  }

  function digest() {
    var root = contentRoot();
    var dialogs = openDialogs();
    var parts = [], links = [], seenMain = {}, seenGroup = {}, total = 0, groups = 0, gi = -1;
    var cur = parts, line = '';
    function isDialog(node) {
      for (var i = 0; i < dialogs.length; i++) if (dialogs[i] === node) return true;
      return false;
    }
    function flush() {
      var t = collapse(line);
      line = '';
      if (t && total < DIGEST_MAX_CHARS) { I.push(cur, t); total += t.length + 1; }
    }
    function addLink(a) {
      var url = a.href;
      if (links.length >= DIGEST_MAX_LINKS || !url || I.test(/^javascript:/i, url) || seenMain[url]) return;
      // A URL first seen in a list block and linked again in the main text is recorded again (the main-text one stays when the block is folded)
      if (gi >= 0) { if (seenGroup[url]) return; seenGroup[url] = 1; } else seenMain[url] = 1;
      I.push(links, { text: cut(collapse(a.innerText || a.getAttribute('aria-label') || a.title), NAME_CHARS), url: url, p: gi });
    }
    function walk(node, inGroup) {
      if (node.nodeType === 3) { line += node.nodeValue; return; }
      if (node.nodeType !== 1) return;
      var tag = lower(node);
      if (READ_SKIP[tag] || secretArea(node)) return;
      if (node !== root.el && isDialog(node)) return;
      var st = styleOf(node);
      if (!st || st.display === 'none' || st.visibility === 'hidden') return;
      if (!inGroup && node !== root.el) {
        var g = foldCandidate(node);
        if (g) {
          flush();
          I.push(parts, g);
          var outer = cur;
          cur = g.lines;
          gi = groups++;
          walkKids(node, true, true);
          flush();
          cur = outer;
          gi = -1;
          return;
        }
      }
      if (tag === 'a' && node.href) addLink(node);
      var h = /^h([1-6])$/.exec(tag);
      if (h) {
        flush();
        var t = collapse(textOf(node));
        if (t) { I.push(cur, I.join(new Array(parseInt(h[1], 10) + 1), '#') + ' ' + t); total += t.length; }
        var hl = node.querySelectorAll('a[href]');
        for (var k = 0; k < hl.length; k++) addLink(hl[k]);
        return;
      }
      if (tag === 'br') { flush(); return; }
      var block = st.display.indexOf('inline') !== 0 && st.display !== 'contents';
      walkKids(node, inGroup, block);
    }
    function walkKids(node, inGroup, block) {
      if (block) flush();
      if (lower(node) === 'slot' && node.assignedNodes) {
        var assigned = node.assignedNodes({ flatten: true });
        if (assigned.length) { I.forEach(assigned, function (n) { walk(n, inGroup); }); if (block) flush(); return; }
      }
      var kids = node.shadowRoot ? node.shadowRoot.childNodes : node.childNodes;
      for (var i = 0; i < kids.length; i++) walk(kids[i], inGroup);
      if (block) flush();
    }
    walk(root.el, false);
    flush();
    var dialogLines = I.map(dialogs, function (d) {
      var mine = [];
      cur = mine;
      walkKids(d, true, true);
      flush();
      return mine;
    });
    cur = parts;

    var images = [];
    var imgs = root.el.querySelectorAll('img');
    for (var j = 0; j < imgs.length && images.length < MAX_IMAGES; j++) {
      var src = imgs[j].currentSrc || imgs[j].src;
      if (!src || I.test(/^data:/i, src)) continue;
      I.push(images, { alt: cut(collapse(imgs[j].alt), NAME_CHARS), url: src });
    }
    return {
      url: I.String(W.location.href), title: D.title, parts: parts, links: links, images: images,
      dialogs: dialogLines, signals: readSignals(root.el, dialogs, total)
    };
  }

  // Sign-in box: walk up from the password box, stop at a form or dialog; otherwise stop just before it would wrap the main content. Count only this block's own text
  function signInBox(input, rootEl) {
    var box = input.parentElement;
    while (box && box !== rootEl && !box.contains(rootEl)) {
      var t = lower(box);
      if (t === 'form' || t === 'dialog' || box.getAttribute('role') === 'dialog' || box.getAttribute('aria-modal') === 'true') return box;
      var up = box.parentElement;
      if (!up || up === D.body || up === D.documentElement || up === rootEl || up.contains(rootEl)) return box;
      box = up;
    }
    return input.parentElement;
  }

  function readSignals(rootEl, dialogs, total) {
    var f = facts();
    var stats = linkStats(rootEl);
    var password = 0, share = 0, signInText = [];
    var inputs = I.docQuerySelectorAll(D, 'input');
    for (var i = 0; i < inputs.length; i++) {
      if (!isPassword(inputs[i]) || !shownBox(inputs[i])) continue;
      if (password++ === 0) {
        var box = signInBox(inputs[i], rootEl);
        var own = box ? collapse(box.innerText || box.textContent) : '';
        share = total > 0 ? Math.min(1, own.length / total) : 1;
        I.push(signInText, cut(own, PLACEHOLDER_CHARS * 8));
      }
    }
    var prominent = [];
    function note(t) { t = collapse(t); if (t && prominent.length < MAX_PROMINENT) I.push(prominent, cut(t, NAME_CHARS)); }
    note(D.title);
    I.push(signInText, cut(collapse(D.title), NAME_CHARS));
    var hs = I.docQuerySelectorAll(D, 'h1, h2, h3, button, [role=button], input[type=submit], input[type=button]');
    for (var j = 0; j < hs.length && prominent.length < MAX_PROMINENT; j++) {
      if (!shownBox(hs[j])) continue;
      note(lower(hs[j]) === 'input' ? I.inputValue(hs[j]) : hs[j].innerText || hs[j].textContent);
    }
    I.forEach(dialogs, function (d) {
      var head = cut(collapse(d.innerText || d.textContent), PLACEHOLDER_CHARS * 4);
      note(head);
      I.push(signInText, head);
    });
    var doc = D.scrollingElement || D.documentElement;
    var rw = Math.round(W.innerWidth), rh = Math.round(Math.min(doc.scrollHeight, W.innerHeight * PICTURE_SCREENS));
    return {
      password: password, signInShare: share, signInText: signInText, prominent: prominent,
      region: [rw, rh], pictures: picturesOf(rw, rh), frames: f.frames,
      textChars: Math.max(total, collapse(rootEl.textContent).length), linkChars: stats.c, dates: declaredDates()
    };
  }

  // Images, canvases, videos and embedded objects in the top area of the page: [x, y, width, height] in document coordinates; size and overlap are worked out by the App
  function picturesOf(w, h) {
    var out = [];
    if (!(w > 0 && h > 0)) return out;
    var els = I.docQuerySelectorAll(D, 'canvas, img, video, svg, embed, object');
    for (var i = 0; i < els.length && out.length < MAX_PICTURES; i++) {
      var el = els[i];
      if (lower(el) === 'svg' && el.parentElement && el.parentElement.closest('svg')) continue;
      var r = el.getBoundingClientRect();
      var top = r.top + W.scrollY, left = r.left + W.scrollX;
      if (r.width <= 0 || r.height <= 0 || top >= h || top + r.height <= 0 || left >= w || left + r.width <= 0) continue;
      if (!shownBox(el)) continue;
      I.push(out, [Math.round(left), Math.round(top), Math.round(r.width), Math.round(r.height)]);
    }
    return out;
  }

  // Publish and update times declared by the page: meta, itemprop, JSON-LD (including inside @graph), <time>; raw values only, the App picks and normalises
  function declaredDates() {
    var out = [];
    function add(k, v) { v = collapse(v); if (v && out.length < MAX_DATES) I.push(out, { k: k, v: cut(v, 60) }); }
    var ms = I.docQuerySelectorAll(D, 'meta[name], meta[property], meta[http-equiv]');
    for (var i = 0; i < ms.length; i++) {
      var key = ms[i].getAttribute('property') || ms[i].getAttribute('name') || ms[i].getAttribute('http-equiv') || '';
      if (I.test(DATE_META, key)) add('meta:' + I.lower(key), ms[i].getAttribute('content'));
    }
    var ips = I.docQuerySelectorAll(D, '[itemprop=datePublished], [itemprop=dateModified]');
    for (var j = 0; j < ips.length; j++) {
      var e = ips[j];
      add('itemprop:' + e.getAttribute('itemprop'), e.getAttribute('content') || e.getAttribute('datetime') || e.textContent);
    }
    function ld(o, depth) {
      if (!o || typeof o !== 'object' || depth > 4) return;
      if (Array.isArray(o)) { for (var a = 0; a < o.length; a++) ld(o[a], depth + 1); return; }
      if (typeof o.datePublished === 'string') add('ld:datePublished', o.datePublished);
      if (typeof o.dateModified === 'string') add('ld:dateModified', o.dateModified);
      if (typeof o.dateCreated === 'string') add('ld:dateCreated', o.dateCreated);
      if (o['@graph']) ld(o['@graph'], depth + 1);
    }
    var ss = I.docQuerySelectorAll(D, 'script[type="application/ld+json"]');
    for (var s = 0; s < ss.length; s++) { try { ld(I.parse(ss[s].textContent || ''), 0); } catch (err) { /* skip bad JSON */ } }
    var ts = contentRoot().el.querySelectorAll('time[datetime]');
    for (var t = 0; t < ts.length && t < 5; t++) add(ts[t].hasAttribute('pubdate') ? 'time:pubdate' : 'time', ts[t].getAttribute('datetime'));
    return out;
  }

  // (length, hash) of the main text: the App takes it every so often; the text counts as stable only after several unchanged samples
  function contentSample() {
    var el = contentRoot().el;
    var t = I.String(el.innerText || el.textContent || '');
    var h = 0;
    for (var i = 0; i < t.length; i++) h = (h * 31 + I.charCodeAt(t, i)) | 0;
    return [t.length, h];
  }

  // Scroll down at most LAZY_SCREENS screens to trigger lazy loading; when done, results[token] is the number of screens scrolled; endLazy restores after reading
  var lazyOrigins = {};
  function startLazy(token) {
    var o = { x: W.scrollX, y: W.scrollY }, n = 0;
    lazyOrigins[token] = o;
    function step() {
      // The page read has already finished (endLazy took the token back): stop scrolling
      if (!I.hasOwn(lazyOrigins, token)) return;
      var bottom = (D.scrollingElement || D.documentElement).scrollHeight;
      if (n < LAZY_SCREENS && W.scrollY + W.innerHeight < bottom - 1) {
        n++;
        scrollToY(o.x, W.scrollY + W.innerHeight);
        I.setTimeout(step, LAZY_WAIT_MS);
      } else {
        results[token] = n;
      }
    }
    step();
    return true;
  }

  function endLazy(token) {
    var o = lazyOrigins[token];
    delete lazyOrigins[token];
    delete results[token];
    if (o) scrollToY(o.x, o.y);
    return !!o;
  }

  function startDigest(token, opts) {
    var res;
    if (opts && opts.minLinks > 0) foldMinLinks = opts.minLinks;
    if (opts && opts.linkShare > 0) foldLinkShare = opts.linkShare;
    try { res = digest(); } catch (e) { res = { error: String(e && e.message || e) }; }
    results[token] = res;
    return true;
  }

  // ---------- Secret inputs ----------
  //
  // Besides password boxes, inputs the screen read also marks as secret: those marked as card number, security code, expiry date or one-time code
  var SECRET_AUTOCOMPLETE = { 'cc-number': 1, 'cc-csc': 1, 'cc-exp': 1, 'cc-exp-month': 1, 'cc-exp-year': 1, 'one-time-code': 1 };

  // ---------- blob: downloads ----------
  //
  // Only the page itself can read blob: URLs: read as base64 and kept in the closure; the App fetches it in chunks (at most [BLOB_CHUNK] characters each) and it is deleted once taken.
  var BLOB_CHUNK = 1 << 20;
  // If the App never comes for it (result wait timed out, tab dropped), the read content is discarded when the time is up
  var BLOB_TTL_MS = 120000;
  var blobs = {};
  var blobsDropped = {};

  function dropBlobLater(token) {
    I.setTimeout(function () { delete blobs[token]; delete results[token]; delete blobsDropped[token]; }, BLOB_TTL_MS);
  }

  function startBlob(token, url, maxBytes) {
    // blob: is content made by the page itself; http(s) is fetched by the page through the browser's network stack when the App cannot connect by itself:
    // only URLs on the same origin as the current page, no following redirects (a redirect elsewhere is not fetched), no credentials for other sites
    var u = I.String(url);
    var web = I.test(/^https?:/i, u);
    var origin = I.String(W.location.origin);
    var sameOrigin = web && (u === origin || I.strSlice(u, 0, origin.length + 1) === origin + '/');
    if (!I.fetch || !I.FileReader || !(I.test(/^blob:/i, u) || sameOrigin)) {
      results[token] = { ok: false, error: 'unreadable' };
      return true;
    }
    dropBlobLater(token);
    var request = web ? I.fetch(u, { redirect: 'error', credentials: 'same-origin' }) : I.fetch(u);
    request.then(function (r) {
      if (web && !r.ok) throw r.status;
      return r.blob();
    }).then(function (b) {
      if (blobsDropped[token]) return;
      if (b.size > maxBytes) { results[token] = { ok: false, error: 'too_large' }; return; }
      var reader = new I.FileReader();
      reader.onload = function () {
        // The App has already given up (called blobDone after a timeout): do not keep it even after reading
        if (blobsDropped[token]) return;
        var s = I.String(reader.result);
        var b64 = s.indexOf(',') >= 0 ? I.strSlice(s, s.indexOf(',') + 1) : '';
        blobs[token] = b64;
        results[token] = { ok: true, mime: b.type || '', length: b64.length, chunk: BLOB_CHUNK };
      };
      reader.onerror = function () { results[token] = { ok: false, error: 'unreadable' }; };
      reader.readAsDataURL(b);
    }, function () { results[token] = { ok: false, error: 'unreadable' }; });
    return true;
  }

  function blobChunk(token, start) {
    var s = blobs[token];
    if (s == null) return null;
    return I.strSlice(s, start, start + BLOB_CHUNK);
  }

  function blobDone(token) {
    delete blobs[token];
    delete results[token];
    blobsDropped[token] = true;
    return true;
  }

  // ---------- Download names ----------
  //
  // The name a page gives a download lives only in the DOM (<a download="…">); the download listener cannot see it. On click (capture phase, read only) note the name for the URL,
  // and the App fetches it when the download starts; if none was noted (for example the page clicked by script), look in the document for a link to this URL with download.
  var MAX_DOWNLOAD_NAMES = 20;
  var MAX_DOWNLOAD_LINKS = 500;
  var downloadNames = {};
  var downloadOrder = [];

  function noteDownloadName(a) {
    var href = I.String(I.anchorHref(a) || '');
    if (!href) return;
    if (!I.hasOwn(downloadNames, href)) I.push(downloadOrder, href);
    downloadNames[href] = I.String(I.getAttribute(a, 'download') || '');
    while (downloadOrder.length > MAX_DOWNLOAD_NAMES) delete downloadNames[downloadOrder.shift()];
  }

  function downloadAnchor(n) {
    return n && n.nodeType === 1 && lower(n) === 'a' && I.hasAttribute(n, 'download') ? n : null;
  }

  try {
    I.addListener(W, 'click', function (e) {
      try {
        var path = typeof e.composedPath === 'function' ? e.composedPath() : [];
        var t = e.target;
        for (var i = 0; i < 20; i++) {
          var n = path.length ? path[i] : t;
          if (!n) return;
          var a = downloadAnchor(n);
          if (a) { noteDownloadName(a); return; }
          if (!path.length) t = t.parentNode;
        }
      } catch (x) { /* if it cannot be read, do not note it; the download still goes ahead with another name */ }
    }, true);
  } catch (x) { /* without the event API, rely only on searching the document */ }

  function downloadName(url) {
    url = I.String(url);
    if (I.hasOwn(downloadNames, url)) return downloadNames[url] || null;
    var list = I.docQuerySelectorAll(D, 'a[download]');
    for (var i = 0; i < list.length && i < MAX_DOWNLOAD_LINKS; i++) {
      if (I.String(I.anchorHref(list[i]) || '') === url) return I.String(I.getAttribute(list[i], 'download') || '') || null;
    }
    return null;
  }

  function take(token) {
    if (!I.hasOwn(results, token)) return null;
    var r = results[token];
    delete results[token];
    return r;
  }

  var api = I.freeze({
    ping: function () { return true; },
    tree: tree,
    facts: facts,
    mutationCount: function () { return mutations; },
    centerOf: centerOf,
    refNodes: refNodes,
    hitTest: hitTest,
    fieldValue: fieldValue,
    isActive: isActive,
    navStatus: navStatus,
    scrollState: scrollState,
    startRead: startRead,
    startDigest: startDigest,
    startLazy: startLazy,
    endLazy: endLazy,
    contentSample: contentSample,
    discard: function (token) { delete results[token]; return true; },
    startBlob: startBlob,
    blobChunk: blobChunk,
    blobDone: blobDone,
    downloadName: downloadName,
    take: take
  });
  // The page can see this function but cannot get the API without the secret; the property cannot be changed or deleted
  var bridge = I.freeze(function (key) { return key === SECRET ? api : undefined; });
  I.defineProperty(W, NAME, { value: bridge, enumerable: false, configurable: false, writable: false });
  return true;
})
