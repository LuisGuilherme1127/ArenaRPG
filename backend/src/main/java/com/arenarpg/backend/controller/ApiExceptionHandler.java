package com.arenarpg.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.arenarpg.backend.exception.ConflitoPartidaException;
import com.arenarpg.backend.exception.PartidaNaoEncontradaException;
import com.arenarpg.backend.exception.PersonagemNaoEncontradoException;
import com.arenarpg.backend.exception.RequisicaoPartidaInvalidaException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PersonagemNaoEncontradoException.class)
    public ProblemDetail tratarPersonagemNaoEncontrado(PersonagemNaoEncontradoException exception) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problema.setTitle("Personagem não encontrado");
        return problema;
    }

    @ExceptionHandler(PartidaNaoEncontradaException.class)
    public ProblemDetail tratarPartidaNaoEncontrada(PartidaNaoEncontradaException exception) {
        return problema(HttpStatus.NOT_FOUND, "Partida não encontrada", exception.getMessage());
    }

    @ExceptionHandler(ConflitoPartidaException.class)
    public ProblemDetail tratarConflitoPartida(ConflitoPartidaException exception) {
        return problema(HttpStatus.CONFLICT, "Ação incompatível com a partida", exception.getMessage());
    }

    @ExceptionHandler({RequisicaoPartidaInvalidaException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class})
    public ProblemDetail tratarRequisicaoInvalida(Exception exception) {
        String detalhe = exception.getMessage();
        if (exception instanceof MethodArgumentNotValidException invalid) {
            detalhe = invalid.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                    .orElse("Os dados enviados são inválidos.");
        } else if (exception instanceof HttpMessageNotReadableException) {
            detalhe = "O corpo JSON está ausente ou malformado.";
        }
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida", detalhe);
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
