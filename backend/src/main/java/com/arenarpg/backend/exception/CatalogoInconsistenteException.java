package com.arenarpg.backend.exception;

public class CatalogoInconsistenteException extends RuntimeException {
    public CatalogoInconsistenteException() {
        super("O catálogo PostgreSQL diverge das regras de personagens do backend; revise os dados antes de continuar.");
    }
}

