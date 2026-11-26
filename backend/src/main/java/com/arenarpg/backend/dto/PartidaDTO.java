package com.arenarpg.backend.dto;

import java.util.List;
import java.util.UUID;

import com.arenarpg.backend.model.Personagem;
import com.arenarpg.backend.model.partida.Partida;

public record PartidaDTO(
        UUID id,
        String estado,
        String turnoAtual,
        int rodada,
        PersonagemDTO jogador,
        PersonagemDTO inimigo,
        TabuleiroDTO tabuleiro,
        boolean movimentoRealizado,
        boolean acaoOfensivaRealizada,
        List<EventoPartidaDTO> eventos) {

    public PartidaDTO {
        eventos = List.copyOf(eventos);
    }

    public static PartidaDTO de(Partida partida) {
        Personagem jogador = partida.getJogador();
        Personagem inimigo = partida.getGoblin();
        return new PartidaDTO(
                partida.getId(),
                partida.getEstado().getValorApi(),
                partida.getTurnoAtual().getValorApi(),
                partida.getRodada(),
                PersonagemDTO.de(jogador),
                PersonagemDTO.de(inimigo),
                TabuleiroDTO.de(partida),
                partida.isMovimentoRealizado(),
                partida.isAcaoOfensivaRealizada(),
                partida.getEventos().stream().map(EventoPartidaDTO::de).toList());
    }
}
