const CACHE_NAME = "balsamur-v1";

// Solo cache básico (NO app.js ni lógica dinámica)
const ASSETS = [
  "./",
  "./index.html",
  "./manifest.webmanifest",
  "./icon-192.png"
];

self.addEventListener("install", (event) => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS))
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

/* IMPORTANTE:
   No cacheamos JS dinámico para evitar bugs de versiones */
self.addEventListener("fetch", (event) => {
  event.respondWith(fetch(event.request));
});