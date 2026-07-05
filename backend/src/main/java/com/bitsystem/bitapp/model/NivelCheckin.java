package com.bitsystem.bitapp.model;

/**
 * Escala única de check-in emocional (CVV v2). Nota fixa por nível — é a
 * ÚNICA fonte de verdade para rótulo/emoji; nunca gravada em paralelo com a
 * nota para evitar divergência.
 */
public enum NivelCheckin {
    MUITO_FELIZ(9, "😄", "Muito feliz"),
    FELIZ(7, "🙂", "Feliz"),
    TRANQUILO(5, "😌", "Tranquilo"),
    TRISTE(3, "😔", "Triste"),
    MUITO_TRISTE(1, "😢", "Muito triste");

    private final int nota;
    private final String emoji;
    private final String rotulo;

    NivelCheckin(int nota, String emoji, String rotulo) {
        this.nota = nota;
        this.emoji = emoji;
        this.rotulo = rotulo;
    }

    public int getNota() {
        return nota;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getRotulo() {
        return rotulo;
    }

    public static NivelCheckin fromNota(int nota) {
        for (NivelCheckin nivel : values()) {
            if (nivel.nota == nota) {
                return nivel;
            }
        }
        throw new IllegalArgumentException("Nota de check-in inválida: " + nota);
    }

    public static boolean isNotaValida(int nota) {
        for (NivelCheckin nivel : values()) {
            if (nivel.nota == nota) {
                return true;
            }
        }
        return false;
    }
}