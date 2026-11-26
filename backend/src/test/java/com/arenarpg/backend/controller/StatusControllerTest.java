package com.arenarpg.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.arenarpg.backend.dto.DatabaseStatusDTO;
import com.arenarpg.backend.service.DatabaseStatusService;

class StatusControllerTest {
    @Test
    void responde200QuandoBancoEstaDisponivel() {
        StatusController controller = new StatusController(() ->
                new DatabaseStatusDTO("disponivel", true, true, "Conexão com o PostgreSQL disponível."));

        ResponseEntity<DatabaseStatusDTO> response = controller.verificarBanco();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().disponivel());
    }

    @Test
    void responde503QuandoBancoEstaIndisponivel() {
        DatabaseStatusService statusIndisponivel = () ->
                new DatabaseStatusDTO("indisponivel", true, false, "O PostgreSQL não está disponível.");
        StatusController controller = new StatusController(statusIndisponivel);

        ResponseEntity<DatabaseStatusDTO> response = controller.verificarBanco();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(false, response.getBody().disponivel());
    }
}

