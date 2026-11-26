package com.arenarpg.backend.dto;

import com.arenarpg.backend.model.partida.Partida;
import com.arenarpg.backend.model.partida.Tabuleiro;

public record TabuleiroDTO(
        int linhas,
        int colunas,
        PosicaoDTO posicaoJogador,
        PosicaoDTO posicaoGoblin) {

    public static TabuleiroDTO de(Partida partida) {
        Tabuleiro tabuleiro = partida.getTabuleiro();
        return new TabuleiroDTO(
                Tabuleiro.LINHAS,
                Tabuleiro.COLUNAS,
                PosicaoDTO.de(tabuleiro.getPosicaoJogador()),
                PosicaoDTO.de(tabuleiro.getPosicaoGoblin()));
    }
}
