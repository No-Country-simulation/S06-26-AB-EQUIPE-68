// Service Worker do App BiT — estratégia CONSERVADORA: network-first para os
// estáticos. O cache só existe como fallback quando a rede falha (offline).
// Em véspera de demo, o risco de servir JS/CSS velho é maior que o benefício
// de um cache agressivo.
// Respostas de /api/ NUNCA entram no cache: trazem dados pessoais (check-ins de
// saúde, assessments, mentorias) e ficariam no aparelho depois do logout.
// A troca do nome abaixo faz o activate apagar os caches antigos, que ainda
// podem conter respostas de /api/ guardadas antes desta regra.
const CACHE_NAME = 'bitapp-v20261017';

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
    // Sem respondWith o navegador segue direto para a rede, sem tocar no cache.
    if (new URL(request.url).pathname.startsWith('/api/')) return;

    event.respondWith(
        fetch(request)
            .then((response) => {
                // Nunca cacheia resposta de erro.
                if (response && response.ok) {
                    const clone = response.clone();
                    caches.open(CACHE_NAME).then((cache) => cache.put(request, clone));
                }
                return response;
            })
            .catch(() => caches.match(request))
    );
});
