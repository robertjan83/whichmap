// Service worker: houdt de app offline beschikbaar. De rotatie wordt lokaal
// berekend, dus de pagina werkt zonder internet. Updates komen binnen bij het
// volgende openen (stale-while-revalidate). Verhoog CACHE bij grote wijzigingen.
const CACHE = "whichmap-v1";
const SHELL = [
  "./",
  "index.html",
  "manifest.webmanifest",
  "img/logo.png",
  "img/fortune.jpg",
  "img/haunted.jpg",
  "img/rebirth.jpg",
  "icons/icon-192.png",
  "icons/icon-512.png",
  "icons/apple-touch-icon.png",
];

self.addEventListener("install", e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(SHELL)).then(() => self.skipWaiting()));
});

self.addEventListener("activate", e => {
  e.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", e => {
  const req = e.request;
  if (req.method !== "GET") return;
  const url = new URL(req.url);
  const sameOrigin = url.origin === self.location.origin;
  const fonts = /(^|\.)fonts\.(googleapis|gstatic)\.com$/.test(url.hostname);
  if (!sameOrigin && !fonts) return;

  e.respondWith((async () => {
    const cache = await caches.open(CACHE);
    // Query-string (?mode=duos) negeren voor de pagina zelf
    const cacheKey = req.mode === "navigate" ? new Request(new URL("index.html", self.registration.scope)) : req;
    const cached = await cache.match(cacheKey);
    const network = fetch(req).then(res => {
      if (res && (res.ok || res.type === "opaque")) cache.put(cacheKey, res.clone());
      return res;
    }).catch(() => null);
    if (cached) { e.waitUntil(network); return cached; }
    const res = await network;
    return res || new Response("Offline", { status: 503, statusText: "Offline" });
  })());
});
