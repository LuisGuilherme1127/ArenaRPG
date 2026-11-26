
package com.arenarpg.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.arenarpg.backend.dto.DatabaseStatusDTO;
import com.arenarpg.backend.service.DatabaseStatusService;

@RestController
public class StatusController {
    private final DatabaseStatusService databaseStatusService;

    public StatusController(DatabaseStatusService databaseStatusService) {
        this.databaseStatusService = databaseStatusService;
    }

    @GetMapping("/api/status")
    public String verificarStatus() {
        return "Arena RPG - Backend Java funcionando!";
    }

    @GetMapping("/api/status/database")
    public ResponseEntity<DatabaseStatusDTO> verificarBanco() {
        DatabaseStatusDTO status = databaseStatusService.verificar();
        return ResponseEntity.status(status.disponivel() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(status);
    }
}
