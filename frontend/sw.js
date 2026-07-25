// Service Worker do App BiT — estratégia CONSERVADORA: network-first para
// TUDO (API e estáticos). O cache só existe como fallback quando a rede
// falha (offline). Em véspera de demo, o risco de servir JS/CSS velho é
// maior que o benefício de um cache agressivo.
const CACHE_NAME = 'bitapp-v20260724';

self.addEventListener('install', (event) => {
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((nomes) =>
            Promise.all(
                nomes
                    .filter((nome) => nome !== CACHE_NAME)
                    .map((nome) => caches.delete(nome))
            )
        ).then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', (event) => {
    const { request } = event;
    if (request.method !== 'GET') return;

    event.respondWith(
        fetch(request)
            .then((response) => {
                // Nunca cacheia resposta de erro (nem de /api, nem estática).
                if (response && response.ok) {
                    const clone = response.clone();
                    caches.open(CACHE_NAME).then((cache) => cache.put(request, clone));
                }
                return response;
            })
            .catch(() => caches.match(request))
    );
});
