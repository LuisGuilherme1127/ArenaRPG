package com.arenarpg.backend.model;

public record ResultadoAcao(boolean sucesso, int danoCausado, String mensagem) {
    public ResultadoAcao {
        if (danoCausado < 0) {
            throw new IllegalArgumentException("O dano causado não pode ser negativo.");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("A mensagem do resultado é obrigatória.");
        }
    }

    public static ResultadoAcao sucesso(int danoCausado, String mensagem) {
        return new ResultadoAcao(true, danoCausado, mensagem);
    }

    public static ResultadoAcao falha(String mensagem) {
        return new ResultadoAcao(false, 0, mensagem);
    }
}
