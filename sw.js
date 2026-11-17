// PWA version 3: always go to the network first for HTML documents.
 // Offline fallback stays available, while installed PWAs pick up map changes.
const CACHE = "whichmap-v3";
const SHELL = [
  "./",
  "index.html",
  "manifest.webmanifest",
  "img/logo.png",
  "img/zodiac.svg",
  "img/rebirth.jpg",
  "icons/icon-192.png",
  "icons/icon-512.png",
  "icons/apple-touch-icon.png",
];

self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE)
      .then(cache => cache.addAll(SHELL))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", event => {
  event.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.filter(key => key.startsWith("whichmap-") && key !== CACHE).map(key => caches.delete(key)));
    await self.clients.claim();

    // Refresh any previously opened installed app still showing outdated HTML.
    const clients = await self.clients.matchAll({ type: "window", includeUncontrolled: true });
    await Promise.all(clients
      .filter(client => client.url.startsWith(self.registration.scope))
      .map(client => client.navigate(client.url).catch(() => null)));
  })());
});

self.addEventListener("fetch", event => {
  const request = event.request;
  if (request.method !== "GET") return;

  const url = new URL(request.url);
  const sameOrigin = url.origin === self.location.origin;
  const fonts = /(^|\.)fonts\.(googleapis|gstatic)\.com$/.test(url.hostname);
  if (!sameOrigin && !fonts) return;

  event.respondWith((async () => {
    const cache = await caches.open(CACHE);
    const isPage = sameOrigin && (request.mode === "navigate" || url.pathname.endsWith("/index.html"));
    const cacheKey = isPage
      ? new Request(new URL("index.html", self.registration.scope))
      : request;

    if (isPage) {
      // Prevent stale installed-app pages when the Warzone map list changes.
      try {
        const response = await fetch(request, { cache: "no-store" });
        if (response.ok) {
          await cache.put(cacheKey, response.clone());
          return response;
        }
      } catch (_) {
        // Network unavailable: serve the latest successfully cached page.
      }
      return await cache.match(cacheKey)
        || new Response("Offline", { status: 503, statusText: "Offline" });
    }

    // Keep assets fast and available offline; refresh them in the background.
    const cached = await cache.match(cacheKey);
    const network = fetch(request).then(response => {
      if (response && (response.ok || response.type === "opaque")) {
        event.waitUntil(cache.put(cacheKey, response.clone()));
      }
      return response;
    }).catch(() => null);
    if (cached) {
      event.waitUntil(network);
      return cached;
    }
    return await network || new Response("Offline", { status: 503, statusText: "Offline" });
  })());
});
