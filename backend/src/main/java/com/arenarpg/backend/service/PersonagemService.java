package com.arenarpg.backend.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.arenarpg.backend.dto.PersonagemDTO;
import com.arenarpg.backend.exception.PersonagemNaoEncontradoException;
import com.arenarpg.backend.model.Personagem;
import com.arenarpg.backend.model.TipoPersonagem;
import com.arenarpg.backend.model.characters.Arqueiro;
import com.arenarpg.backend.model.characters.Goblin;
import com.arenarpg.backend.model.characters.Guerreiro;
import com.arenarpg.backend.model.characters.Mago;

@Service
public class PersonagemService {
    private final List<Supplier<Personagem>> personagensDisponiveis = List.of(
            Guerreiro::new,
            Mago::new,
            Arqueiro::new,
            Goblin::new);

    private final Map<String, Supplier<Personagem>> personagensPorTipo = Map.of(
            "guerreiro", Guerreiro::new,
            "mago", Mago::new,
            "arqueiro", Arqueiro::new,
            "goblin", Goblin::new);

    public List<PersonagemDTO> listarPersonagens() {
        return personagensDisponiveis.stream()
                .map(Supplier::get)
                .map(PersonagemDTO::de)
                .toList();
    }

    public PersonagemDTO buscarPorTipo(String tipo) {
        String tipoNormalizado = tipo == null ? "" : tipo.trim().toLowerCase(Locale.ROOT);
        Supplier<Personagem> fabrica = personagensPorTipo.get(tipoNormalizado);
        if (fabrica == null) {
            throw new PersonagemNaoEncontradoException(tipo);
        }

        return PersonagemDTO.de(fabrica.get());
    }

    /** Cria uma instância nova para uma partida, sem compartilhar estado mutável entre partidas. */
    public Personagem criarInstancia(TipoPersonagem tipo) {
        if (tipo == null) {
            throw new PersonagemNaoEncontradoException("null");
        }
        Supplier<Personagem> fabrica = personagensPorTipo.get(tipo.getIdentificador());
        if (fabrica == null) {
            throw new PersonagemNaoEncontradoException(tipo.getIdentificador());
        }
        return fabrica.get();
    }
}
