package com.arenarpg.backend.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.arenarpg.backend.dto.PartidaDTO;
import com.arenarpg.backend.exception.PartidaNaoEncontradaException;
import com.arenarpg.backend.exception.RequisicaoPartidaInvalidaException;
import com.arenarpg.backend.model.Personagem;
import com.arenarpg.backend.model.TipoPersonagem;
import com.arenarpg.backend.model.characters.Goblin;
import com.arenarpg.backend.model.partida.Partida;
import com.arenarpg.backend.model.partida.Posicao;
import com.arenarpg.backend.model.partida.Tabuleiro;

@Service
public class PartidaService {
    private final ConcurrentMap<UUID, Partida> partidas = new ConcurrentHashMap<>();
    private final PersonagemService personagemService;
    private final ComportamentoGoblin comportamentoGoblin;

    public PartidaService(PersonagemService personagemService, ComportamentoGoblin comportamentoGoblin) {
        this.personagemService = personagemService;
        this.comportamentoGoblin = comportamentoGoblin;
    }

    public PartidaDTO iniciarPartida(String tipoPersonagem) {
        final TipoPersonagem tipo;
        try {
            tipo = TipoPersonagem.de(tipoPersonagem);
        } catch (IllegalArgumentException exception) {
            throw new RequisicaoPartidaInvalidaException(exception.getMessage());
        }
        if (tipo == TipoPersonagem.GOBLIN) {
            throw new RequisicaoPartidaInvalidaException("Escolha Guerreiro, Mago ou Arqueiro para iniciar.");
        }

        Personagem jogador = personagemService.criarInstancia(tipo);
        Goblin goblin = (Goblin) personagemService.criarInstancia(TipoPersonagem.GOBLIN);
        Tabuleiro tabuleiro = new Tabuleiro(new Posicao(1, 6), new Posicao(6, 2));
        Partida partida = new Partida(UUID.randomUUID(), jogador, goblin, tabuleiro);
        partidas.put(partida.getId(), partida);
        return snapshot(partida);
    }

    public PartidaDTO buscarPorId(UUID id) {
        Partida partida = obterPartida(id);
        return snapshot(partida);
    }

    public PartidaDTO mover(UUID id, Posicao destino) {
        return executar(id, partida -> {
            partida.moverJogador(destino);
            return PartidaDTO.de(partida);
        });
    }

    public PartidaDTO atacar(UUID id) {
        return executar(id, partida -> {
            partida.atacar();
            return PartidaDTO.de(partida);
        });
    }

    public PartidaDTO usarHabilidade(UUID id) {
        return executar(id, partida -> {
            partida.usarHabilidadeEspecial();
            return PartidaDTO.de(partida);
        });
    }

    public PartidaDTO finalizarTurno(UUID id) {
        return executar(id, partida -> {
            partida.iniciarTurnoGoblin();
            comportamentoGoblin.executarTurno(partida);
            partida.concluirTurnoGoblin();
            return PartidaDTO.de(partida);
        });
    }

    private PartidaDTO executar(UUID id, Function<Partida, PartidaDTO> acao) {
        Partida partida = obterPartida(id);
        // O lock é por partida: chamadas simultâneas nunca observam ou gravam estado parcial.
        synchronized (partida) {
            return acao.apply(partida);
        }
    }

    private PartidaDTO snapshot(Partida partida) {
        synchronized (partida) {
            return PartidaDTO.de(partida);
        }
    }

    private Partida obterPartida(UUID id) {
        if (id == null) {
            throw new PartidaNaoEncontradaException(null);
        }
        Partida partida = partidas.get(id);
        if (partida == null) {
            throw new PartidaNaoEncontradaException(id);
        }
        return partida;
    }
}
