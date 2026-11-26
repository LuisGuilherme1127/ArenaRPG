package com.arenarpg.backend.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.arenarpg.backend.dto.DatabaseStatusDTO;

class PostgresDatabaseStatusServiceTest {
    private JdbcTemplate jdbcTemplate;
    private PostgresDatabaseStatusService service;

    @BeforeEach
    void configurar() {
        jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        service = new PostgresDatabaseStatusService(jdbcTemplate);
    }

    @Test
    void consultaBancoComSelectUmQuandoConexaoResponde() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        DatabaseStatusDTO status = service.verificar();

        assertAll(
                () -> assertEquals("disponivel", status.status()),
                () -> assertEquals(true, status.integracaoHabilitada()),
                () -> assertEquals(true, status.disponivel()));
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
    }

    @Test
    void retornaIndisponivelSemExporDetalhesDoDriver() {
        doThrow(new DataAccessResourceFailureException("detalhe interno de conexão"))
                .when(jdbcTemplate).queryForObject("SELECT 1", Integer.class);

        DatabaseStatusDTO status = service.verificar();

        assertAll(
                () -> assertEquals("indisponivel", status.status()),
                () -> assertEquals(true, status.integracaoHabilitada()),
                () -> assertEquals(false, status.disponivel()),
                () -> assertEquals("O PostgreSQL não está disponível.", status.mensagem()));
    }
}

