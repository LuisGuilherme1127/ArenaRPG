package com.arenarpg.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record IniciarPartidaRequest(@NotBlank String tipoPersonagem) {
}
