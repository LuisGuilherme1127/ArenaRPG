package com.arenarpg.backend.model.partida;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.arenarpg.backend.exception.ConflitoPartidaException;
import com.arenarpg.backend.model.characters.Arqueiro;
import com.arenarpg.backend.model.characters.Goblin;
import com.arenarpg.backend.model.characters.Guerreiro;
import com.arenarpg.backend.model.characters.Mago;
import com.arenarpg.backend.service.ComportamentoGoblin;

class PartidaTest {
    @Test
    void tabuleiroValidaLimitesColisoesEManhattan() {
        assertEquals(8, Tabuleiro.LINHAS);
        assertEquals(8, Tabuleiro.COLUNAS);
        assertThrows(ConflitoPartidaException.class,
                () -> new Tabuleiro(new Posicao(8, 0), new Posicao(6, 2)));
        assertThrows(IllegalArgumentException.class,
                () -> new Tabuleiro(new Posicao(1, 1), new Posicao(1, 1)));
        assertEquals(9, new Posicao(1, 6).distanciaManhattan(new Posicao(6, 2)));
    }

    @Test
    void movimentoRespeitaDistanciaOcupacaoEUmaAcaoPorTurno() {
        Partida partida = partida(new Guerreiro(), new Posicao(1, 6), new Posicao(6, 2));
        partida.moverJogador(new Posicao(4, 6));
        assertEquals(new Posicao(4, 6), partida.getTabuleiro().getPosicaoJogador());
        assertThrows(ConflitoPartidaException.class, () -> partida.moverJogador(new Posicao(4, 5)));
        assertThrows(ConflitoPartidaException.class, () -> partida.moverJogador(new Posicao(6, 2)));
        assertThrows(ConflitoPartidaException.class, () -> partida.moverJogador(new Posicao(7, 6)));
    }

    @Test
    void ataqueBasicoEHabilidadeValidamAlcanceManaEDanoSemEfeitoEmFalha() {
        Guerreiro guerreiro = new Guerreiro();
        Partida partida = partida(guerreiro, new Posicao(1, 1), new Posicao(3, 1));
        PartidaDTOEstado antes = estado(partida);
        assertThrows(ConflitoPartidaException.class, partida::atacar);
        assertEquals(antes, estado(partida));

        partida.getTabuleiro().mover(LadoPartida.JOGADOR, new Posicao(2, 1), 1);
        partida.atacar();
        assertEquals(80, partida.getGoblin().getVidaAtual());
        assertThrows(ConflitoPartidaException.class, partida::usarHabilidadeEspecial);
        assertEquals(30, guerreiro.getManaAtual());

        Partida magoPartida = partida(new Mago(), new Posicao(1, 1), new Posicao(4, 1));
        magoPartida.usarHabilidadeEspecial();
        assertEquals(70, magoPartida.getGoblin().getVidaAtual());
        assertEquals(80, magoPartida.getJogador().getManaAtual());

        Partida arqueiroPartida = partida(new Arqueiro(), new Posicao(1, 1), new Posicao(5, 1));
        arqueiroPartida.usarHabilidadeEspecial();
        assertEquals(72, arqueiroPartida.getGoblin().getVidaAtual());
        assertEquals(35, arqueiroPartida.getJogador().getManaAtual());
    }

    @Test
    void habilidadeSemManaNaoConsomeTurnoNemAlteraVida() {
        Mago mago = new Mago();
        mago.consumirMana(85);
        Partida partida = partida(mago, new Posicao(1, 1), new Posicao(4, 1));
        PartidaDTOEstado antes = estado(partida);
        assertThrows(ConflitoPartidaException.class, partida::usarHabilidadeEspecial);
        assertEquals(antes, estado(partida));
        assertEquals(15, mago.getManaAtual());
        assertEquals(100, partida.getGoblin().getVidaAtual());
    }

    @Test
    void turnoGoblinMoveEAtacaEAlternanciaReiniciaAcoes() {
        Partida partida = partida(new Guerreiro(), new Posicao(4, 4), new Posicao(6, 4));
        partida.iniciarTurnoGoblin();
        assertThrows(ConflitoPartidaException.class, partida::atacar);
        new ComportamentoGoblin().executarTurno(partida);
        partida.concluirTurnoGoblin();
        assertEquals(TurnoPartida.JOGADOR, partida.getTurnoAtual());
        assertEquals(2, partida.getRodada());
        assertEquals(108, partida.getJogador().getVidaAtual());
        assertFalse(partida.isMovimentoRealizado());
        assertFalse(partida.isAcaoOfensivaRealizada());
    }

    @Test
    void jogadorPodeVencerEInimigoPodeDerrotarJogador() {
        Guerreiro guerreiro = new Guerreiro();
        Goblin goblin = new Goblin();
        goblin.receberDano(80);
        Partida vitoria = partida(guerreiro, goblin, new Posicao(1, 1), new Posicao(2, 1));
        vitoria.usarHabilidadeEspecial();
        assertEquals(EstadoPartida.VITORIA, vitoria.getEstado());
        assertEquals(TurnoPartida.FINALIZADO, vitoria.getTurnoAtual());
        assertThrows(ConflitoPartidaException.class, vitoria::atacar);

        Guerreiro derrotado = new Guerreiro();
        derrotado.receberDano(108);
        Partida derrota = partida(derrotado, new Goblin(), new Posicao(1, 1), new Posicao(2, 1));
        derrota.iniciarTurnoGoblin();
        new ComportamentoGoblin().executarTurno(derrota);
        derrota.concluirTurnoGoblin();
        assertEquals(EstadoPartida.DERROTA, derrota.getEstado());
    }

    private static Partida partida(com.arenarpg.backend.model.Personagem jogador, Posicao pJogador, Posicao pGoblin) {
        return partida(jogador, new Goblin(), pJogador, pGoblin);
    }

    private static Partida partida(com.arenarpg.backend.model.Personagem jogador, Goblin goblin,
            Posicao pJogador, Posicao pGoblin) {
        return new Partida(UUID.randomUUID(), jogador, goblin, new Tabuleiro(pJogador, pGoblin));
    }

    private static PartidaDTOEstado estado(Partida partida) {
        return new PartidaDTOEstado(partida.getJogador().getVidaAtual(), partida.getJogador().getManaAtual(),
                partida.getGoblin().getVidaAtual(), partida.getTabuleiro().getPosicaoJogador(),
                partida.getTurnoAtual(), partida.isMovimentoRealizado(), partida.isAcaoOfensivaRealizada(),
                partida.getEventos().size());
    }

    private record PartidaDTOEstado(int vida, int mana, int vidaGoblin, Posicao posicao, TurnoPartida turno,
            boolean moveu, boolean atacou, int eventos) { }
}
