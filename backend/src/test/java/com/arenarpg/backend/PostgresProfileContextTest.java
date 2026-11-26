package com.arenarpg.backend;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.arenarpg.backend.repository.JdbcPersonagemRepository;

@SpringBootTest(properties = {
        "DB_URL=jdbc:postgresql://127.0.0.1:1/arena_rpg?currentSchema=arena",
        "DB_USERNAME=arena_app_test",
        "DB_PASSWORD="
})
@ActiveProfiles("postgres")
class PostgresProfileContextTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcPersonagemRepository repository;

    @Test
    void ativaConfiguracaoJdbcSemExigirConexaoDuranteInicializacao() {
        assertNotNull(jdbcTemplate);
        assertNotNull(repository);
    }
}

