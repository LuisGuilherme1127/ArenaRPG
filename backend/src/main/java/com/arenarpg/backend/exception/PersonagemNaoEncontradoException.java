package com.arenarpg.backend.exception;

public class PersonagemNaoEncontradoException extends RuntimeException {
    public PersonagemNaoEncontradoException(String tipo) {
        super("Personagem não encontrado: " + tipo);
    }
}
