const CACHE_NAME = 'balsamur-cache-v2';   // nueva versión de caché
const FILES_TO_CACHE = [
  './',
  './index.html',
  './app.js',
  './sw-register.js',
  './manifest.webmanifest',
  './sw.js',
  './icon-512.png',
  './icon-192.png',
  './marca-agua.png'       // ojo: aquí faltaba una coma antes en tu código
];

// Instalar: cachear los archivos básicos
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
      return cache.addAll(FILES_TO_CACHE);
    })
  );
});

// Activar: limpieza de caches antiguos (por si cambias el nombre)
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
    )
  );
});

// Fetch: servir desde caché y, si no hay, ir a la red
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => {
      return response || fetch(event.request);
    })
  );
});