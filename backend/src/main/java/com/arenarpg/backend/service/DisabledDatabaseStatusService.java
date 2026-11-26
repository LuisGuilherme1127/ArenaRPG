package com.arenarpg.backend.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.arenarpg.backend.dto.DatabaseStatusDTO;

@Service
@Profile("!postgres")
public class DisabledDatabaseStatusService implements DatabaseStatusService {
    @Override
    public DatabaseStatusDTO verificar() {
        return new DatabaseStatusDTO(
                "desabilitado",
                false,
                false,
                "O perfil postgres está desativado; a integração com o banco não está habilitada.");
    }
}

