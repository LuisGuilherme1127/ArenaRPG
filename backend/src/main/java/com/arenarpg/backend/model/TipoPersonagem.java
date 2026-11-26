package com.arenarpg.backend.model;

import java.util.Locale;

public enum TipoPersonagem {
    GUERREIRO("guerreiro"),
    MAGO("mago"),
    ARQUEIRO("arqueiro"),
    GOBLIN("goblin");

    private final String identificador;

    TipoPersonagem(String identificador) {
        this.identificador = identificador;
    }

    public String getIdentificador() {
        return identificador;
    }

    public static TipoPersonagem de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O tipo do personagem é obrigatório.");
        }

        String normalizado = valor.trim().toLowerCase(Locale.ROOT);
        for (TipoPersonagem tipo : values()) {
            if (tipo.identificador.equals(normalizado)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de personagem inválido: " + valor);
    }
}
