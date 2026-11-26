package com.arenarpg.backend.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.arenarpg.backend.model.partida.LadoPartida;
import com.arenarpg.backend.model.partida.Partida;
import com.arenarpg.backend.model.partida.Posicao;

@Component
public class ComportamentoGoblin {
    public void executarTurno(Partida partida) {
        int alcance = partida.getGoblin().getAlcance();
        if (partida.getTabuleiro().distanciaEntrePersonagens() <= alcance) {
            partida.goblinAtacar();
            return;
        }

        List<Posicao> caminho = partida.getTabuleiro().caminhoAteAlcance(LadoPartida.GOBLIN, alcance);
        if (caminho.isEmpty()) {
            partida.registrarEventoInimigo("aviso", "Goblin não encontrou uma rota para avançar.");
            return;
        }

        int passos = Math.min(partida.getGoblin().getMovimentoMaximo(), caminho.size());
        partida.moverGoblin(caminho.get(passos - 1));

        if (partida.getTabuleiro().distanciaEntrePersonagens() <= alcance) {
            partida.goblinAtacar();
        }
    }
}
