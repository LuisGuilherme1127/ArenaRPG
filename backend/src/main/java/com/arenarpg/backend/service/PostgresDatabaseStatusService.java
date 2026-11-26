package com.arenarpg.backend.service;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.arenarpg.backend.dto.DatabaseStatusDTO;

@Service
@Profile("postgres")
public class PostgresDatabaseStatusService implements DatabaseStatusService {
    private final JdbcTemplate jdbcTemplate;

    public PostgresDatabaseStatusService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DatabaseStatusDTO verificar() {
        try {
            Integer resultado = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (Integer.valueOf(1).equals(resultado)) {
                return new DatabaseStatusDTO("disponivel", true, true, "Conexão com o PostgreSQL disponível.");
            }
        } catch (DataAccessException exception) {
            // Não inclua URL, usuário, senha ou mensagem de driver na resposta HTTP.
        }
        return new DatabaseStatusDTO("indisponivel", true, false, "O PostgreSQL não está disponível.");
    }
}

