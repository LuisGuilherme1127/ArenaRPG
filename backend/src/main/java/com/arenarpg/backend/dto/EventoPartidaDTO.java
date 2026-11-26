package com.arenarpg.backend.dto;

import com.arenarpg.backend.model.partida.EventoPartida;

public record EventoPartidaDTO(int rodada, String tipo, String mensagem) {
    public static EventoPartidaDTO de(EventoPartida evento) {
        return new EventoPartidaDTO(evento.rodada(), evento.tipo(), evento.mensagem());
    }
}
