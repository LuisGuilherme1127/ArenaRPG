package com.arenarpg.backend.dto;

import com.arenarpg.backend.model.partida.Posicao;

public record PosicaoDTO(int x, int y) {
    public static PosicaoDTO de(Posicao posicao) {
        return new PosicaoDTO(posicao.x(), posicao.y());
    }
}
