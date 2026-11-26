package com.arenarpg.backend.repository;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.arenarpg.backend.dto.PersonagemDTO;
import com.arenarpg.backend.service.PersonagemService;

/** Mantém o catálogo original em memória quando o perfil PostgreSQL está desligado. */
@Repository
@Profile("!postgres")
public class MemoriaPersonagemCatalogo implements PersonagemCatalogo {
    private final PersonagemService personagemService;

    public MemoriaPersonagemCatalogo(PersonagemService personagemService) {
        this.personagemService = personagemService;
    }

    @Override
    public List<PersonagemDTO> listar() {
        return personagemService.listarPersonagens();
    }

    @Override
    public PersonagemDTO buscarPorTipo(String tipo) {
        return personagemService.buscarPorTipo(tipo);
    }
}

