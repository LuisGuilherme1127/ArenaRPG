package com.arenarpg.backend.model.partida;

public record Posicao(int x, int y) {
    public int distanciaManhattan(Posicao outra) {
        return Math.abs(x - outra.x) + Math.abs(y - outra.y);
    }

    public boolean estaDentroDoTabuleiro(int linhas, int colunas) {
        return x >= 0 && x < colunas && y >= 0 && y < linhas;
    }
}
