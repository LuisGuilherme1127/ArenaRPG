package com.arenarpg.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.arenarpg.backend.model.partida.Posicao;

public record MoverPartidaRequest(
        @NotNull @Min(0) @Max(7) Integer x,
        @NotNull @Min(0) @Max(7) Integer y) {

    public Posicao paraPosicao() {
        return new Posicao(x, y);
    }
}
