const CACHE_NAME = 'balsamur-cache-v3';

const FILES_TO_CACHE = [
  './',
  './index.html',
  './app.js',
  './sw-register.js',
  './manifest.webmanifest',
  './icon-512.png',
  './icon-192.png',
  './marca-agua.png'
];

// INSTALAR
self.addEventListener('install', event => {
  self.skipWaiting();

  event.waitUntil(
    caches.open(CACHE_NAME).then(async cache => {
      for (const file of FILES_TO_CACHE) {
        try {
          await cache.add(file);
        } catch (e) {
          console.warn('No se pudo cachear:', file);
        }
      }
    })
  );
});

// ACTIVAR
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys.map(key => {
          if (key !== CACHE_NAME) {
            return caches.delete(key);
          }
        })
      )
    ).then(() => self.clients.claim())
  );
});

// FETCH
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return;

  event.respondWith(
    caches.match(event.request).then(response => {
      return response || fetch(event.request);
    })
  );
});