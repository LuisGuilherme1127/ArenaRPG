package com.arenarpg.backend.exception;

import java.util.UUID;

public class PartidaNaoEncontradaException extends RuntimeException {
    public PartidaNaoEncontradaException(UUID id) {
        super("Partida não encontrada: " + id);
    }
}
