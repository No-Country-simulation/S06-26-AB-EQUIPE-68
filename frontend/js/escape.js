// Escape de HTML para texto que vem do usuário ou da IA e é montado em
// template string antes de ir para innerHTML. Função única do frontend: use
// esta em vez de escrever outra. Para texto puro, prefira textContent.
export function escapeHtml(valor) {
    return String(valor ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
