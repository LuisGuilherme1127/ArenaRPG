package com.arenarpg.backend.model.partida;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.arenarpg.backend.exception.ConflitoPartidaException;
import com.arenarpg.backend.model.Personagem;
import com.arenarpg.backend.model.ResultadoAcao;
import com.arenarpg.backend.model.TipoPersonagem;

public final class Partida {
    private final UUID id;
    private final Personagem jogador;
    private final Personagem goblin;
    private final Tabuleiro tabuleiro;
    private final List<EventoPartida> eventos = new ArrayList<>();

    private EstadoPartida estado = EstadoPartida.EM_ANDAMENTO;
    private TurnoPartida turnoAtual = TurnoPartida.JOGADOR;
    private int rodada = 1;
    private boolean movimentoRealizado;
    private boolean acaoOfensivaRealizada;

    public Partida(UUID id, Personagem jogador, Personagem goblin, Tabuleiro tabuleiro) {
        if (id == null || jogador == null || goblin == null || tabuleiro == null) {
            throw new IllegalArgumentException("A partida precisa de id, personagens e tabuleiro.");
        }
        if (jogador.getTipo() == TipoPersonagem.GOBLIN || goblin.getTipo() != TipoPersonagem.GOBLIN) {
            throw new IllegalArgumentException("A partida deve ter um herói jogável e um Goblin.");
        }
        if (!jogador.estaVivo() || !goblin.estaVivo()) {
            throw new IllegalArgumentException("Os personagens precisam iniciar vivos.");
        }

        this.id = id;
        this.jogador = jogador;
        this.goblin = goblin;
        this.tabuleiro = tabuleiro;
        registrarEvento("sistema", "A batalha começou. " + jogador.getNome() + " enfrenta o Goblin.");
    }

    public UUID getId() {
        return id;
    }

    public Personagem getJogador() {
        return jogador;
    }

    public Personagem getGoblin() {
        return goblin;
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public TurnoPartida getTurnoAtual() {
        return turnoAtual;
    }

    public int getRodada() {
        return rodada;
    }

    public boolean isMovimentoRealizado() {
        return movimentoRealizado;
    }

    public boolean isAcaoOfensivaRealizada() {
        return acaoOfensivaRealizada;
    }

    public List<EventoPartida> getEventos() {
        return List.copyOf(eventos);
    }

    public void moverJogador(Posicao destino) {
        exigirTurnoDoJogador();
        if (movimentoRealizado) {
            throw new ConflitoPartidaException("O jogador já se moveu neste turno.");
        }

        tabuleiro.mover(LadoPartida.JOGADOR, destino, jogador.getMovimentoMaximo());
        movimentoRealizado = true;
        registrarEvento("movimento", jogador.getNome() + " moveu-se para " + formatar(destino) + ".");
    }

    public void atacar() {
        exigirTurnoDoJogador();
        if (acaoOfensivaRealizada) {
            throw new ConflitoPartidaException("O jogador já realizou uma ação ofensiva neste turno.");
        }

        ResultadoAcao resultado = jogador.realizarAtaqueBasico(goblin, tabuleiro.distanciaEntrePersonagens());
        aplicarAcaoOfensiva(resultado, "ataque");
    }

    public void usarHabilidadeEspecial() {
        exigirTurnoDoJogador();
        if (acaoOfensivaRealizada) {
            throw new ConflitoPartidaException("O jogador já realizou uma ação ofensiva neste turno.");
        }

        ResultadoAcao resultado = jogador.executarHabilidadeEspecial(goblin, tabuleiro.distanciaEntrePersonagens());
        aplicarAcaoOfensiva(resultado, "habilidade");
    }

    public void iniciarTurnoGoblin() {
        exigirTurnoDoJogador();
        turnoAtual = TurnoPartida.GOBLIN;
        registrarEvento("sistema", "Turno " + rodada + ": Goblin está agindo.");
    }

    public void moverGoblin(Posicao destino) {
        exigirTurnoDoGoblin();
        tabuleiro.mover(LadoPartida.GOBLIN, destino, goblin.getMovimentoMaximo());
        registrarEvento("movimento", "Goblin avançou para " + formatar(destino) + ".");
    }

    public void goblinAtacar() {
        exigirTurnoDoGoblin();
        ResultadoAcao resultado = goblin.realizarAtaqueBasico(jogador, tabuleiro.distanciaEntrePersonagens());
        if (!resultado.sucesso()) {
            throw new ConflitoPartidaException(resultado.mensagem());
        }

        registrarEvento("dano", resultado.mensagem());
        atualizarEstadoFinal();
    }

    public void registrarEventoInimigo(String tipo, String mensagem) {
        exigirTurnoDoGoblin();
        registrarEvento(tipo, mensagem);
    }

    public void concluirTurnoGoblin() {
        if (turnoAtual != TurnoPartida.GOBLIN && estado == EstadoPartida.EM_ANDAMENTO) {
            throw new ConflitoPartidaException("O Goblin não está no turno de ação.");
        }
        if (estado != EstadoPartida.EM_ANDAMENTO) {
            turnoAtual = TurnoPartida.FINALIZADO;
            return;
        }

        rodada++;
        movimentoRealizado = false;
        acaoOfensivaRealizada = false;
        turnoAtual = TurnoPartida.JOGADOR;
        registrarEvento("sistema", "Sua vez: turno " + rodada + ".");
    }

    private void aplicarAcaoOfensiva(ResultadoAcao resultado, String tipo) {
        if (!resultado.sucesso()) {
            throw new ConflitoPartidaException(resultado.mensagem());
        }

        acaoOfensivaRealizada = true;
        registrarEvento(tipo, resultado.mensagem());
        atualizarEstadoFinal();
    }

    private void atualizarEstadoFinal() {
        if (!goblin.estaVivo()) {
            estado = EstadoPartida.VITORIA;
            turnoAtual = TurnoPartida.FINALIZADO;
            registrarEvento("vitoria", "Goblin foi derrotado. A arena é sua!");
        } else if (!jogador.estaVivo()) {
            estado = EstadoPartida.DERROTA;
            turnoAtual = TurnoPartida.FINALIZADO;
            registrarEvento("derrota", jogador.getNome() + " caiu em batalha.");
        }
    }

    private void exigirTurnoDoJogador() {
        exigirPartidaAtiva();
        if (turnoAtual != TurnoPartida.JOGADOR) {
            throw new ConflitoPartidaException("A ação só pode ser realizada durante o turno do jogador.");
        }
        if (!jogador.estaVivo()) {
            throw new ConflitoPartidaException("O personagem do jogador está derrotado.");
        }
    }

    private void exigirTurnoDoGoblin() {
        exigirPartidaAtiva();
        if (turnoAtual != TurnoPartida.GOBLIN) {
            throw new ConflitoPartidaException("O Goblin não está no turno de ação.");
        }
    }

    private void exigirPartidaAtiva() {
        if (estado != EstadoPartida.EM_ANDAMENTO) {
            throw new ConflitoPartidaException("A partida já terminou.");
        }
    }

    private void registrarEvento(String tipo, String mensagem) {
        eventos.add(new EventoPartida(rodada, tipo, mensagem));
    }

    private static String formatar(Posicao posicao) {
        return "(" + posicao.x() + ", " + posicao.y() + ")";
    }
}
