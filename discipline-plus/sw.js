/* Service worker de Discipline+ : fonctionnement hors ligne + notifications.
   Change VERSION à chaque mise en ligne pour vider l'ancien cache. */
const VERSION = 'discipline-plus-v3';

// La « coquille » : tout ce qui vient du dossier de l'appli, mis en cache dès l'installation.
const COQUILLE = [
  './', 'index.html', 'manifest.webmanifest',
  'img/hero.jpg', 'img/chat.jpg', 'img/chambre.jpg', 'img/haltere.jpg', 'img/livre.jpg', 'img/eau.jpg', 'img/sol.jpg',
  'icons/icon-192.png', 'icons/icon-512.png', 'icons/icon-maskable-512.png', 'icons/apple-touch-icon.png', 'icons/favicon-48.png',
];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(VERSION).then((c) => c.addAll(COQUILLE)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys()
      .then((cles) => Promise.all(cles.filter((k) => k !== VERSION).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

const gardable = (r) => r && (r.ok || r.type === 'opaque');

// Télécharge une adresse et la range dans le cache (CORS d'abord : indispensable pour les modules esm.sh).
async function ranger(cache, url) {
  if (await cache.match(url, { ignoreVary: true })) return;
  let r;
  try { r = await fetch(url, { mode: 'cors', credentials: 'omit' }); }
  catch (_) { r = await fetch(url, { mode: 'no-cors' }); }
  if (gardable(r)) await cache.put(url, r);
}

// Au tout premier lancement, la page a chargé ses bibliothèques (React, Tailwind, Babel, police)
// avant que le service worker ne la contrôle : elle nous envoie la liste pour qu'on les mette en cache.
self.addEventListener('message', (e) => {
  const d = e.data || {};
  if (d.type !== 'rechauffer' || !Array.isArray(d.urls)) return;
  e.waitUntil(caches.open(VERSION).then((cache) =>
    Promise.all(d.urls.filter((u) => /^https?:/.test(u)).map((u) => ranger(cache, u).catch(() => {})))));
});

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET' || !/^https?:/.test(req.url)) return;

  // La page elle-même : réseau d'abord (pour recevoir les mises à jour), cache si hors ligne.
  if (req.mode === 'navigate') {
    e.respondWith((async () => {
      const cache = await caches.open(VERSION);
      try {
        const r = await fetch(req);
        if (r.ok) cache.put('index.html', r.clone());
        return r;
      } catch (_) {
        return (await cache.match(req, { ignoreSearch: true })) || (await cache.match('index.html')) || Response.error();
      }
    })());
    return;
  }

  // Tout le reste (images, bibliothèques, police) : cache d'abord, rafraîchi en arrière-plan.
  e.respondWith((async () => {
    const cache = await caches.open(VERSION);
    const enCache = await cache.match(req, { ignoreVary: true });
    const reseau = fetch(req).then((r) => { if (gardable(r)) cache.put(req, r.clone()); return r; });
    if (enCache) { e.waitUntil(reseau.catch(() => {})); return enCache; }
    return reseau;
  })());
});

// Un appui sur la notification ramène l'appli au premier plan.
self.addEventListener('notificationclick', (e) => {
  e.notification.close();
  e.waitUntil((async () => {
    const fenetres = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
    if (fenetres.length) return fenetres[0].focus();
    return self.clients.openWindow('./');
  })());
});
