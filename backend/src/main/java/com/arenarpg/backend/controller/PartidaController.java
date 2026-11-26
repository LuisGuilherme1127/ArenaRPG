package com.arenarpg.backend.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.arenarpg.backend.dto.IniciarPartidaRequest;
import com.arenarpg.backend.dto.MoverPartidaRequest;
import com.arenarpg.backend.dto.PartidaDTO;
import com.arenarpg.backend.service.PartidaService;

@RestController
@RequestMapping("/api/partidas")
public class PartidaController {
    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartidaDTO iniciar(@Valid @RequestBody IniciarPartidaRequest request) {
        return partidaService.iniciarPartida(request.tipoPersonagem());
    }

    @GetMapping("/{id}")
    public PartidaDTO buscar(@PathVariable UUID id) {
        return partidaService.buscarPorId(id);
    }

    @PostMapping("/{id}/mover")
    public PartidaDTO mover(@PathVariable UUID id, @Valid @RequestBody MoverPartidaRequest request) {
        return partidaService.mover(id, request.paraPosicao());
    }

    @PostMapping("/{id}/atacar")
    public PartidaDTO atacar(@PathVariable UUID id) {
        return partidaService.atacar(id);
    }

    @PostMapping("/{id}/habilidade")
    public PartidaDTO habilidade(@PathVariable UUID id) {
        return partidaService.usarHabilidade(id);
    }

    @PostMapping("/{id}/finalizar-turno")
    public PartidaDTO finalizarTurno(@PathVariable UUID id) {
        return partidaService.finalizarTurno(id);
    }
}
