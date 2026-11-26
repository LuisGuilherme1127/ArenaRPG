package com.arenarpg.backend.repository;

import java.util.List;

import com.arenarpg.backend.dto.PersonagemDTO;

/** Fonte de leitura do catálogo, escolhida pelo perfil ativo. */
public interface PersonagemCatalogo {
    List<PersonagemDTO> listar();

    PersonagemDTO buscarPorTipo(String tipo);
}

