package com.arenarpg.backend.model.partida;

public enum EstadoPartida {
    EM_ANDAMENTO("em_andamento"),
    VITORIA("vitoria"),
    DERROTA("derrota");

    private final String valorApi;

    EstadoPartida(String valorApi) {
        this.valorApi = valorApi;
    }

    public String getValorApi() {
        return valorApi;
    }
}
