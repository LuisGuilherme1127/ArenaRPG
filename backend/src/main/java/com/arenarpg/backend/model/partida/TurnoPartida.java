package com.arenarpg.backend.model.partida;

public enum TurnoPartida {
    JOGADOR("jogador"),
    GOBLIN("goblin"),
    FINALIZADO("finalizado");

    private final String valorApi;

    TurnoPartida(String valorApi) {
        this.valorApi = valorApi;
    }

    public String getValorApi() {
        return valorApi;
    }
}
