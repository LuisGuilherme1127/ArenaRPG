package com.arenarpg.backend.model.partida;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.arenarpg.backend.exception.ConflitoPartidaException;

public final class Tabuleiro {
    public static final int LINHAS = 8;
    public static final int COLUNAS = 8;

    private Posicao posicaoJogador;
    private Posicao posicaoGoblin;

    public Tabuleiro(Posicao posicaoJogador, Posicao posicaoGoblin) {
        validarDentro(posicaoJogador);
        validarDentro(posicaoGoblin);
        if (posicaoJogador.equals(posicaoGoblin)) {
            throw new IllegalArgumentException("Jogador e Goblin não podem iniciar na mesma célula.");
        }
        this.posicaoJogador = posicaoJogador;
        this.posicaoGoblin = posicaoGoblin;
    }

    public Posicao getPosicao(LadoPartida lado) {
        return lado == LadoPartida.JOGADOR ? posicaoJogador : posicaoGoblin;
    }

    public Posicao getPosicaoJogador() {
        return posicaoJogador;
    }

    public Posicao getPosicaoGoblin() {
        return posicaoGoblin;
    }

    public int distanciaEntrePersonagens() {
        return posicaoJogador.distanciaManhattan(posicaoGoblin);
    }

    public List<Posicao> posicoesAlcancaveis(LadoPartida lado, int maximoPassos) {
        if (maximoPassos < 0) {
            throw new IllegalArgumentException("O limite de movimento não pode ser negativo.");
        }

        Posicao origem = getPosicao(lado);
        Posicao ocupada = getPosicao(lado == LadoPartida.JOGADOR ? LadoPartida.GOBLIN : LadoPartida.JOGADOR);
        ArrayDeque<Passo> fila = new ArrayDeque<>();
        Set<Posicao> visitadas = new HashSet<>();
        List<Posicao> alcançaveis = new ArrayList<>();
        fila.add(new Passo(origem, 0));
        visitadas.add(origem);

        while (!fila.isEmpty()) {
            Passo atual = fila.removeFirst();
            if (atual.distancia() >= maximoPassos) {
                continue;
            }

            for (Posicao vizinha : vizinhas(atual.posicao())) {
                if (!vizinha.estaDentroDoTabuleiro(LINHAS, COLUNAS)
                        || vizinha.equals(ocupada)
                        || !visitadas.add(vizinha)) {
                    continue;
                }
                alcançaveis.add(vizinha);
                fila.addLast(new Passo(vizinha, atual.distancia() + 1));
            }
        }

        return List.copyOf(alcançaveis);
    }

    public void mover(LadoPartida lado, Posicao destino, int maximoPassos) {
        validarDentro(destino);
        Posicao ocupada = getPosicao(lado == LadoPartida.JOGADOR ? LadoPartida.GOBLIN : LadoPartida.JOGADOR);
        if (destino.equals(ocupada)) {
            throw new ConflitoPartidaException("A célula de destino está ocupada.");
        }
        if (!posicoesAlcancaveis(lado, maximoPassos).contains(destino)) {
            throw new ConflitoPartidaException("O destino não é alcançável com o movimento permitido.");
        }

        if (lado == LadoPartida.JOGADOR) {
            posicaoJogador = destino;
        } else {
            posicaoGoblin = destino;
        }
    }

    /** Busca o caminho em ordem direita, esquerda, baixo, cima, como o BFS do frontend. */
    public List<Posicao> caminhoAteAlcance(LadoPartida lado, int alcance) {
        if (alcance < 0) {
            throw new IllegalArgumentException("O alcance não pode ser negativo.");
        }

        Posicao origem = getPosicao(lado);
        Posicao alvo = getPosicao(lado == LadoPartida.JOGADOR ? LadoPartida.GOBLIN : LadoPartida.JOGADOR);
        ArrayDeque<Caminho> fila = new ArrayDeque<>();
        Set<Posicao> visitadas = new HashSet<>();
        fila.add(new Caminho(origem, List.of()));
        visitadas.add(origem);

        while (!fila.isEmpty()) {
            Caminho atual = fila.removeFirst();
            if (!atual.passos().isEmpty() && atual.posicao().distanciaManhattan(alvo) <= alcance) {
                return atual.passos();
            }

            for (Posicao vizinha : vizinhas(atual.posicao())) {
                if (!vizinha.estaDentroDoTabuleiro(LINHAS, COLUNAS)
                        || vizinha.equals(alvo)
                        || !visitadas.add(vizinha)) {
                    continue;
                }
                List<Posicao> passos = new ArrayList<>(atual.passos());
                passos.add(vizinha);
                fila.addLast(new Caminho(vizinha, List.copyOf(passos)));
            }
        }

        return List.of();
    }

    private static void validarDentro(Posicao posicao) {
        if (posicao == null || !posicao.estaDentroDoTabuleiro(LINHAS, COLUNAS)) {
            throw new ConflitoPartidaException("A posição precisa estar dentro do tabuleiro 8x8.");
        }
    }

    private static List<Posicao> vizinhas(Posicao posicao) {
        return List.of(
                new Posicao(posicao.x() + 1, posicao.y()),
                new Posicao(posicao.x() - 1, posicao.y()),
                new Posicao(posicao.x(), posicao.y() + 1),
                new Posicao(posicao.x(), posicao.y() - 1));
    }

    private record Passo(Posicao posicao, int distancia) {
    }

    private record Caminho(Posicao posicao, List<Posicao> passos) {
    }
}
