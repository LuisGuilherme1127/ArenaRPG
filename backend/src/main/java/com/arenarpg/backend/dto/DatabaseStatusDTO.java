package com.arenarpg.backend.dto;

public record DatabaseStatusDTO(
        String status,
        boolean integracaoHabilitada,
        boolean disponivel,
        String mensagem) {
}

